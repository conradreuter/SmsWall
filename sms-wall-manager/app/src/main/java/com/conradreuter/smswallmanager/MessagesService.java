package com.conradreuter.smswallmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

public final class MessagesService extends Service {

    private static final String TAG = MessagesService.class.getSimpleName();

    public static final String ACTION_INIT = "com.conradreuter.smswallmanager.action.INIT";
    public static final String ACTION_MESSAGES = "com.conradreuter.smswallmanager.action.MESSAGES";

    public static final String BROADCAST_MESSAGES = "com.conradreuter.smswallmanager.broadcast.MESSAGES";

    public static final String EXTRA_BASEADDRESS = "com.conradreuter.smswallmanager.extra.BASEADDRESS";
    public static final String EXTRA_MESSAGES = "com.conradreuter.smswallmanager.extra.MESSAGES";

    public static final IntentFilter INTENT_FILTER = new IntentFilter(BROADCAST_MESSAGES);

    private static final int NOTIFICATION = R.string.app_name;
    private static boolean isRunning;
    private static Uri baseAddress;

    private final BroadcastReceiver broadcastReceiver = new MessagesServiceBroadcaseReceiver();
    private ArrayList<Message> messages = new ArrayList<Message>();

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
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, SmsBroadcastReceiver.INTENT_FILTER);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, ServerCommunicationService.INTENT_FILTER);
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
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
        if (ACTION_INIT.equals(action)) {
            Uri baseAddress = intent.getParcelableExtra(EXTRA_BASEADDRESS);
            handleInit(baseAddress);
        } else if (ACTION_MESSAGES.equals(action)) {
            handleMessages();
        }
    }

    private void handleInit(Uri baseAddress) {
        Log.d(TAG, String.format("Initialising with base address %s", baseAddress));
        MessagesService.baseAddress = baseAddress;
        ServerCommunicationService.startActionGetMessages(this, baseAddress);
    }

    private void handleMessages() {
        Log.d(TAG, "Broadcasting messages");
        broadcastMessages();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class MessagesServiceBroadcaseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SmsBroadcastReceiver.BROADCAST_INCOMING_MESSAGE.equals(action)) {
                Message message = intent.getParcelableExtra(SmsBroadcastReceiver.EXTRA_MESSAGE);
                handleIncomingMessage(message);
            } else if (ServerCommunicationService.BROADCAST_PUT_MESSAGE_SUCCEEDED.equals(action)) {
                Message message = intent.getParcelableExtra(SmsBroadcastReceiver.EXTRA_MESSAGE);
                handlePutMessageSucceeded(message);
            } else if (ServerCommunicationService.BROADCAST_GET_MESSAGES_SUCCEEDED.equals(action)) {
                ArrayList<Message> messages = intent.getParcelableArrayListExtra(ServerCommunicationService.EXTRA_MESSAGES);
                handleGetMessagesSucceeded(messages);
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

    private void handlePutMessageSucceeded(Message message) {
        Log.d(TAG, String.format("New message %s", message));
        messages.add(message);
        broadcastMessages();
    }

    private void handleGetMessagesSucceeded(ArrayList<Message> messages) {
        Log.d(TAG, String.format("Updated messages (count: %d)", messages.size()));
        this.messages = messages;
        broadcastMessages();
    }

    private void broadcastMessages() {
        Intent intent = new Intent(BROADCAST_MESSAGES);
        intent.putParcelableArrayListExtra(EXTRA_MESSAGES, messages);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
