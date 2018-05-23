package independent_study.ultimatetictactoe.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadcastReceiverStartUp extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent serviceIntent = new Intent(context, GameBackgroundService.class);
        context.startService(serviceIntent);
    }
}
