package independent_study.ultimatetictactoe.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import independent_study.ultimatetictactoe.R;
import independent_study.ultimatetictactoe.game.GameMessage;
import independent_study.ultimatetictactoe.game.UltimateTickTacToeBoard;
import independent_study.ultimatetictactoe.gui.GameActivity;
import independent_study.ultimatetictactoe.gui.GameListActivity;
import independent_study.ultimatetictactoe.sms.BroadcastReceiverSMS;
import independent_study.ultimatetictactoe.sms.ListenerSMS;

public class GameBackgroundService extends Service implements ListenerSMS
{
    public static final String PREFERENCES_KEY = "gameStore";
    private static final String BASE_STORAGE_STRING = "Game";
    private static final int ONGOING_NOTIFICATION_ID = 23;
    private static final int QUICK_NOTIFICATION_ID = 32;
    private static final String LOG_TAG = "GameBackgroundService";

    private GameBackgroundBinder binder;
    private BroadcastReceiverSMS receiverSMS;
    private Notification persistentNotification;
    private Notification instantNotification;

    private final ArrayList<UltimateTickTacToeBoard> boards = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    public class GameBackgroundBinder extends Binder
    {
        public GameBackgroundService getService()
        {
            return GameBackgroundService.this;
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        binder = new GameBackgroundBinder();
        Intent directIntent = new Intent(this, GameActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, directIntent, 0);

        sharedPreferences = getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);

        persistentNotification = new Notification.Builder(this.getApplicationContext())
                .setContentTitle("Ultimate TicTacToe Listener")
                .setContentText("Waiting for Game Messages")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(Notification.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(ONGOING_NOTIFICATION_ID, persistentNotification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        loadSavedGames();
        receiverSMS = BroadcastReceiverSMS.getInstance();
        receiverSMS.addListener(this);
        return Service.START_STICKY;
    }

    @Override
    public void onSMSReceived(SmsMessage message)
    {
        if(message != null && !message.getMessageBody().equals(""))
        {
            String messageContents = message.getMessageBody();
            String recipient = message.getOriginatingAddress();
            long phoneNumber = Long.parseLong(recipient);

            if(GameMessage.isGameMessage(messageContents))
            {
                if(instantNotification != null)
                {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                    notificationManager.cancel(QUICK_NOTIFICATION_ID);
                    instantNotification = null;
                }

                GameMessage gameMessage = new GameMessage(messageContents, phoneNumber);
                UltimateTickTacToeBoard board = gameMessage.getBoard();
                addBoard(board);

                Intent specificIntent = new Intent(this, GameListActivity.class);
                //specificIntent.putExtra(GameActivity.BOARD_TAG, board.toString());
                PendingIntent specificPendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, specificIntent, 0);

                instantNotification = new Notification.Builder(this.getApplicationContext())
                        .setContentTitle("Received Game Update!")
                        .setContentText("Click Here to Play")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setContentIntent(specificPendingIntent)
                        .build();

                instantNotification.flags |= Notification.FLAG_AUTO_CANCEL;

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(QUICK_NOTIFICATION_ID, instantNotification);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return true;
    }

    @Override
    public void onRebind(Intent intent)
    {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy()
    {
        receiverSMS.removeListener(this);
        saveLocalGames();
        super.onDestroy();
    }

    private void loadSavedGames()
    {
        int index = 0;

        try
        {
            boolean shouldStop = false;
            while (sharedPreferences.contains(BASE_STORAGE_STRING + index) && !shouldStop)
            {
                String storedString = sharedPreferences.getString(BASE_STORAGE_STRING + index, "");
                if(!storedString.equals(""))
                {
                    Log.d(LOG_TAG, storedString);
                    addBoard(UltimateTickTacToeBoard.fromString(storedString));
                }
                else
                {
                    shouldStop = true;
                }
                index++;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void saveLocalGames()
    {
        try
        {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            synchronized (boards)
            {
                for(int i = 0; i < boards.size(); i++)
                {
                    edit.putString(BASE_STORAGE_STRING + i, boards.get(i).toString());
                }
            }
            edit.commit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public ArrayList<UltimateTickTacToeBoard> getBoards()
    {
        return boards;
    }

    public void addBoard(UltimateTickTacToeBoard tickTacToeBoard)
    {
        synchronized (boards)
        {
            boards.add(tickTacToeBoard);
        }
    }

    public void removeBoard(UltimateTickTacToeBoard tickTacToeBoard)
    {
        synchronized (boards)
        {
            boards.remove(tickTacToeBoard);
        }
    }

    public ArrayList<UltimateTickTacToeBoard> getBoardsCopy()
    {
        synchronized(boards)
        {
            return new ArrayList<>(boards);
        }
    }
}
