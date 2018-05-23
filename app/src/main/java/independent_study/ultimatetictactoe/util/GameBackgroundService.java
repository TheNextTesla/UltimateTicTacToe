package independent_study.ultimatetictactoe.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsMessage;

import independent_study.ultimatetictactoe.R;
import independent_study.ultimatetictactoe.gui.GameActivity;
import independent_study.ultimatetictactoe.sms.BroadcastReceiverSMS;
import independent_study.ultimatetictactoe.sms.ListenerSMS;

public class GameBackgroundService extends Service implements ListenerSMS
{
    private static final int ONGOING_NOTIFICATION_ID = 23;

    private BroadcastReceiverSMS receiverSMS;

    @Override
    public void onCreate()
    {
        super.onCreate();

        Intent directIntent = new Intent(this, GameActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, directIntent, 0);

        Notification notification = new Notification.Builder(this.getApplicationContext())
                .setContentTitle("Ultimate TicTacToe Listener")
                .setContentText("Waiting for Game Messages")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        //receiverSMS = new BroadcastReceiverSMS();
        //this
        return Service.START_STICKY;
    }

    @Override
    public void onSMSReceived(SmsMessage message)
    {
        startActivity(new Intent(this, GameActivity.class));
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
        super.onDestroy();
    }
}
