package com.conradreuter.smswallmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public final class MessagesService extends Service {

    private static final String TAG = MessagesService.class.getSimpleName();

    public static final String ACTION_SET_BASEADDRESS = "com.conradreuter.smswallmanager.action.SET_BASEADDRESS";

    public static final String EXTRA_BASEADDRESS = "com.conradreuter.smswallmanager.extra.BASEADDRESS";

    private static final int NOTIFICATION = R.string.app_name;
    private static boolean isRunning;
    private static Uri baseAddress;

    private final BroadcastReceiver incomingMessageBroadcastReceiver = new IncomingMessageBroadcaseReceiver();

    public static boolean isRunning() {
        return isRunning;
    }

    public static Uri getBaseAddress() {
        return baseAddress;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        isRunning = true;
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION, createNotification());
        LocalBroadcastManager.getInstance(this).registerReceiver(incomingMessageBroadcastReceiver, SmsBroadcastReceiver.INTENT_FILTER);
    }

    private Notification createNotification() {
        return new Notification.Builder(this)
                .setTicker(getText(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setContentText(getText(R.string.app_name))
                .build();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        isRunning = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(incomingMessageBroadcastReceiver);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIFICATION);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(TAG, String.format("onStartCommand(%d, %s)", startId, intent));
        handleIntent(intent);
        return START_STICKY;
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (ACTION_SET_BASEADDRESS.equals(action)) {
            Uri baseAddress = intent.getParcelableExtra(EXTRA_BASEADDRESS);
            handleSetBaseAddress(baseAddress);
        }
    }

    private void handleSetBaseAddress(Uri baseAddress) {
        Log.d(TAG, String.format("Setting base address to %s", baseAddress));
        MessagesService.baseAddress = baseAddress;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class IncomingMessageBroadcaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SmsBroadcastReceiver.BROADCAST_INCOMING_MESSAGE.equals(action)) {
                Message message = intent.getParcelableExtra(SmsBroadcastReceiver.EXTRA_MESSAGE);
                handleIncomingMessage(message);
            }
        }
    }

    private void handleIncomingMessage(Message message) {
        Log.d(TAG, String.format("Incoming message %s", message));
        if (baseAddress != null) {
            Log.d(TAG, String.format("Putting message on base address %s", baseAddress));
            ServerCommunicationService.startActionPutMessage(this, message, baseAddress);
        } else {
            Log.e(TAG, "Cannot put message, because the base address is not set");
        }
    }
}
