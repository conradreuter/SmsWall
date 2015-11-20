package com.conradreuter.smswallmanager;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.net.URI;

public final class MessagesService extends IntentService {

    private static final String TAG = MessagesService.class.getSimpleName();

    private static final String ACTION_PUT_MESSAGE = "com.conradreuter.smswallmanager.action.PUT_MESSAGE";

    private static final String EXTRA_BASEADDRESS = "com.conradreuter.smswallmanager.action.BASEADDRESS";

    public static void startActionPutMessage(Context context, Message message, URI baseAddress) {
        Intent intent = new Intent(context, MessagesService.class);
        intent.setAction(ACTION_PUT_MESSAGE);
        message.fillIntent(intent);
        intent.putExtra(EXTRA_BASEADDRESS, baseAddress.toString());
        context.startService(intent);
    }

    public MessagesService() {
        super(MessagesService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PUT_MESSAGE.equals(action)) {
                Message message = Message.fromIntent(intent);
                URI baseAddress = URI.create(intent.getStringExtra(EXTRA_BASEADDRESS));
                handleActionPutMessage(message, baseAddress);
            }
        }
    }

    private void handleActionPutMessage(Message message, URI baseAddress) {
        Log.d(TAG, String.format(
                "Trying to put message %s from %s",
                message.getSender(),
                message.getText()));
        if (message.put(baseAddress)) {
            Log.d(TAG, String.format(
                    "Putting message %s from %s succeeded",
                    message.getSender(),
                    message.getText()));
        } else {
            Log.e(TAG, String.format(
                    "Putting message %s from %s failed",
                    message.getSender(),
                    message.getText()));
        }
    }
}
