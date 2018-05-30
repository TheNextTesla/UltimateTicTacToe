package independent_study.ultimatetictactoe.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
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
    private static final String ONGOING_CHANNEL_ID = "TicTacToe Persistent Messages";
    private static final String INSTANT_CHANNEL_ID = "TicTacToe On-Message Notifications";
    private static final String LOG_TAG = "GameBackgroundService";
    public static volatile boolean serviceStarted;

    private GameBackgroundBinder binder;
    private BroadcastReceiverSMS receiverSMS;
    private Notification persistentNotification;
    private Notification instantNotification;

    private final ArrayList<UltimateTickTacToeBoard> boards = new ArrayList<>();
    private final ArrayList<ListenerGameUpdate> listeners = new ArrayList<>();
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
        serviceStarted = true;

        binder = new GameBackgroundBinder();
        Intent directIntent = new Intent(this, GameListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, directIntent, 0);

        sharedPreferences = getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel persistentChannel = new NotificationChannel(ONGOING_CHANNEL_ID, "Ongoing Notification", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(persistentChannel);

            persistentNotification = new Notification.Builder(this.getApplicationContext())
                    .setContentTitle("Ultimate TicTacToe Listener")
                    .setContentText("Waiting for Game Messages")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setChannelId(ONGOING_CHANNEL_ID)
                    .setContentIntent(pendingIntent)
                    .build();

        }
        else
        {
            persistentNotification = new Notification.Builder(this.getApplicationContext())
                    .setContentTitle("Ultimate TicTacToe Listener")
                    .setContentText("Waiting for Game Messages")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setContentIntent(pendingIntent)
                    .build();
        }

        loadSavedGames();
        Log.d(LOG_TAG, "onCreate");

        startForeground(ONGOING_NOTIFICATION_ID, persistentNotification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        receiverSMS = BroadcastReceiverSMS.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.setPriority(2147483647);
            intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
            this.registerReceiver(receiverSMS, intentFilter);
        }

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
            StringBuilder cleanBuilder = new StringBuilder();
            for(char character : recipient.toCharArray())
            {
                if(Character.isDigit(character))
                    cleanBuilder.append(character);
            }

            long phoneNumber = Long.parseLong(cleanBuilder.toString());
            Log.d(LOG_TAG, "Phone Number: " + phoneNumber);

            if(GameMessage.isGameMessage(messageContents))
            {
                dismissInstantNotification();

                GameMessage gameMessage = new GameMessage(messageContents, phoneNumber);
                UltimateTickTacToeBoard board = gameMessage.getBoard();
                addBoard(board);
                saveLocalGames();

                Intent specificIntent = new Intent(this, GameListActivity.class);
                PendingIntent specificPendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, specificIntent, 0);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    NotificationChannel instantChannel = new NotificationChannel(INSTANT_CHANNEL_ID, "Instant Notification", NotificationManager.IMPORTANCE_LOW);
                    instantChannel.enableLights(true);
                    instantChannel.setLightColor(Color.BLUE);
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    nm.createNotificationChannel(instantChannel);

                    instantNotification = new Notification.Builder(this.getApplicationContext())
                            .setContentTitle("Received Game Update!")
                            .setContentText("Click Here to Play")
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentIntent(specificPendingIntent)
                            .setChannelId(INSTANT_CHANNEL_ID)
                            .build();

                    instantNotification.flags |= Notification.FLAG_AUTO_CANCEL;

                    nm.notify(QUICK_NOTIFICATION_ID, instantNotification);
                }
                else
                {
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
        this.unregisterReceiver(receiverSMS);
        removeAllListeners();
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

            int index = 0;
            while (sharedPreferences.contains(BASE_STORAGE_STRING + index))
            {
                edit.remove(BASE_STORAGE_STRING + index);
                index++;
            }

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

    public void dismissInstantNotification()
    {
        if(instantNotification != null)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.cancel(QUICK_NOTIFICATION_ID);
            }
            else
            {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.cancel(QUICK_NOTIFICATION_ID);
            }
            instantNotification = null;
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
        callListeners();
    }

    public void removeBoard(UltimateTickTacToeBoard tickTacToeBoard)
    {
        synchronized (boards)
        {
            boards.remove(tickTacToeBoard);
        }
        callListeners();
    }

    public ArrayList<UltimateTickTacToeBoard> getBoardsCopy()
    {
        synchronized(boards)
        {
            return new ArrayList<>(boards);
        }
    }

    public boolean addListener(ListenerGameUpdate listenerGame)
    {
        synchronized (listeners)
        {
            for (ListenerGameUpdate listener : listeners)
            {
                if (listener == listenerGame)
                    return false;
            }
            listeners.add(listenerGame);
            Log.d(LOG_TAG, "Listeners: " + listeners.size());
            return true;
        }
    }

    public boolean removeListener(ListenerGameUpdate listenerGameUpdate)
    {
        synchronized (listeners)
        {
            return listeners.remove(listenerGameUpdate);
        }
    }

    public void removeAllListeners()
    {
        synchronized (listeners)
        {
            listeners.clear();
        }
    }

    public void callListeners()
    {
        ArrayList<UltimateTickTacToeBoard> updatedBoards = getBoardsCopy();
        synchronized (listeners)
        {
            for(ListenerGameUpdate gameListeners : listeners)
            {
                gameListeners.onGameUpdate(updatedBoards);
            }
        }
    }
}
