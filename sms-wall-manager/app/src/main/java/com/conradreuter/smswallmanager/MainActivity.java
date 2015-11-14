package com.conradreuter.smswallmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    private final BroadcastReceiver messagesBroadcastReceiver = new MessagesBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocalBroadcastManager.getInstance(this).registerReceiver(messagesBroadcastReceiver, Message.INTENT_FILTER);
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messagesBroadcastReceiver);
        super.onStop();
    }

    private class MessagesBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Message message = Message.fromIntent(intent);
            showMessage(message);
        }
    }

    private void showMessage(Message message) {
        TextView textView = (TextView)findViewById(R.id.textView);
        textView.setText(message.getText());
    }
}
