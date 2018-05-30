package independent_study.ultimatetictactoe.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BroadcastReceiverStartUp extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent serviceIntent = new Intent(context, GameBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            context.startForegroundService(serviceIntent);
        }
        else
        {
            context.startService(serviceIntent);
        }
    }
}
