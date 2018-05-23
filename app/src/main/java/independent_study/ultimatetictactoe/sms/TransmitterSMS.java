package independent_study.ultimatetictactoe.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.Locale;

public class TransmitterSMS
{
    public static final short DATA_SMS_PORT = 8789;
    public static final int SENT = 1;

    private static TransmitterSMS transmitterSMS;
    private SmsManager smsManager;

    private TransmitterSMS()
    {
        smsManager = SmsManager.getDefault();
    }

    public static TransmitterSMS getInstance()
    {
        if(transmitterSMS  == null)
            transmitterSMS = new TransmitterSMS();
        return transmitterSMS;
    }

    public boolean sendSMS(long phoneNumber, String message, Activity context)
    {
        if(phoneNumber < 0)
            return false;
        else if(phoneNumber > 99999999999L)
            return false;

        String phoneNumberText = String.format(Locale.US, "%010d", phoneNumber);
        Log.d("Transmitter", "Phone " + phoneNumberText);
        PendingIntent sent = context.createPendingResult(SENT, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        smsManager.sendTextMessage(phoneNumberText, null, message, sent, null);
        return true;
    }

    public boolean sendDataSMS(long phoneNumber, byte[] message, Activity context)
    {
        if(phoneNumber < 0)
            return false;
        else if(phoneNumber > 99999999999L)
            return false;

        String phoneNumberText = String.format(Locale.US, "%010d", phoneNumber);
        Log.d("Transmitter", "Phone " + phoneNumberText);
        PendingIntent sent = context.createPendingResult(SENT, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        smsManager.sendDataMessage(phoneNumberText, null, DATA_SMS_PORT, message, sent, null);
        return true;
    }
}
