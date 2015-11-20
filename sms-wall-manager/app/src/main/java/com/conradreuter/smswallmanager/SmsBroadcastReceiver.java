package com.conradreuter.smswallmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

public final class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = SmsBroadcastReceiver.class.getSimpleName();

    public static final String BROADCAST_INCOMING_MESSAGE = "com.conradreuter.smswallmanager.action.INCOMING_MESSAGE";

    public static final String EXTRA_MESSAGE = "com.conradreuter.smswallmanager.extra.MESSAGE";

    public static final IntentFilter INTENT_FILTER = new IntentFilter(BROADCAST_INCOMING_MESSAGE);

    @Override
    public void onReceive(Context context, Intent intent) {
        final Collection<SmsMessage> smsMessages = getSmsMessages(intent.getExtras());
        for(SmsMessage smsMessage : smsMessages) {
            Message message = Message.fromSms(smsMessage);
            broadcastMessage(context, message);
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

    private void broadcastMessage(Context context, Message message) {
        Log.d(TAG, String.format("Incoming message %s ", message));
        Intent intent = new Intent(BROADCAST_INCOMING_MESSAGE);
        intent.putExtra(EXTRA_MESSAGE, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
