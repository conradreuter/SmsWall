package com.conradreuter.smswallmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.ArrayList;
import java.util.Collection;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final Collection<SmsMessage> smsMessages = getSmsMessages(intent.getExtras());
        for(SmsMessage smsMessage : smsMessages) {
            Message message = Message.fromSms(smsMessage);
            MessagesService.startActionPutMessage(context, message);
        }
    }

    private Collection<SmsMessage> getSmsMessages(Bundle bundle) {
        Collection<SmsMessage> smsMessages = new ArrayList<SmsMessage>();
        if (bundle == null) return smsMessages;
        final Object[] pdus = (Object[])bundle.get("pdus");
        for (int i = 0; i < pdus.length; ++i) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[])pdus[i]);
            smsMessages.add(smsMessage);
        }
        return smsMessages;
    }
}
