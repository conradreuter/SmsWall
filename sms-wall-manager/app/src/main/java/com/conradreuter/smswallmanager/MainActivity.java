package com.conradreuter.smswallmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.net.URI;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String STATE_BASEADDRESS = "com.conradreuter.smswallmanager.state.BASEADDRESS";

    public static final String EXTRA_BASEADDRESS = "com.conradreuter.smswallmanager.action.BASEADDRESS";

    private final BroadcastReceiver incomingMessageBroadcastReceiver = new IncomingMessageBroadcaseReceiver();
    private URI baseAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (obtainBaseAddress(savedInstanceState)) {
            Log.d(TAG, String.format("Base address %s found", baseAddress.toString()));
        } else {
            Log.d(TAG, "No base address found. Switching to connection activity");
            switchToConnectionActivity();
            return;
        }
        setContentView(R.layout.activity_main);
        LocalBroadcastManager.getInstance(this).registerReceiver(incomingMessageBroadcastReceiver, SmsBroadcastReceiver.INTENT_FILTER);
    }

    private boolean obtainBaseAddress(Bundle savedInstanceState) {
        baseAddress = null;
        if (getIntent().hasExtra(EXTRA_BASEADDRESS)) {
            baseAddress = URI.create(getIntent().getStringExtra(EXTRA_BASEADDRESS));
        } else if (savedInstanceState != null) {
            String baseAddressString = savedInstanceState.getString(STATE_BASEADDRESS);
            if (baseAddressString != null) {
                baseAddress = URI.create(baseAddressString);
            }
        }
        return baseAddress != null;
    }

    private void switchToConnectionActivity() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(incomingMessageBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (baseAddress != null) {
            savedInstanceState.putString(STATE_BASEADDRESS, baseAddress.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    private class IncomingMessageBroadcaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SmsBroadcastReceiver.BROADCAST_INCOMING_MESSAGE.equals(action)) {
                Message message = Message.fromIntent(intent);
                handleIncomingMessage(message);
            }
        }
    }

    private void handleIncomingMessage(Message message) {
        Log.d(TAG, String.format(
                "Incoming message %s from %s",
                message.getText(),
                message.getSender()));
        MessagesService.startActionPutMessage(this, message, baseAddress);
    }
}
