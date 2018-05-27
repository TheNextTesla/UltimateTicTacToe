package independent_study.ultimatetictactoe.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsMessage;

import independent_study.ultimatetictactoe.R;
import independent_study.ultimatetictactoe.game.GameMessage;
import independent_study.ultimatetictactoe.game.UltimateTickTacToeBoard;
import independent_study.ultimatetictactoe.gui.GameActivity;
import independent_study.ultimatetictactoe.gui.GameListActivity;
import independent_study.ultimatetictactoe.sms.BroadcastReceiverSMS;
import independent_study.ultimatetictactoe.sms.ListenerSMS;

public class GameBackgroundService extends Service implements ListenerSMS
{
    private static final int ONGOING_NOTIFICATION_ID = 23;
    private static final int QUICK_NOTIFICATION_ID = 32;

    private BroadcastReceiverSMS receiverSMS;
    private Notification persistentNotification;
    private Notification instantNotification;

    @Override
    public void onCreate()
    {
        super.onCreate();

        Intent directIntent = new Intent(this, GameActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, directIntent, 0);

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

                Intent specificIntent = new Intent(this, GameListActivity.class);
                specificIntent.putExtra(GameActivity.BOARD_TAG, board.toString());
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
        return null;
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
        super.onDestroy();
    }
}
