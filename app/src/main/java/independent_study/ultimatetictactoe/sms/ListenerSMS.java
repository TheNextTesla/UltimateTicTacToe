package independent_study.ultimatetictactoe.sms;

import android.telephony.SmsMessage;

public interface ListenerSMS
{
    void onSMSReceived(SmsMessage message);
}
