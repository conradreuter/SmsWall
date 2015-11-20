package com.conradreuter.smswallmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String STATE_BASEADDRESS = "com.conradreuter.smswallmanager.state.BASEADDRESS";

    public static final String EXTRA_BASEADDRESS = "com.conradreuter.smswallmanager.action.BASEADDRESS";

    private final BroadcastReceiver broadcastReceiver = new MainActivityBroadcaseReceiver();
    private Uri baseAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (obtainBaseAddress(savedInstanceState)) {
            Log.d(TAG, String.format("Base address %s found", baseAddress));
        } else {
            Log.d(TAG, "No base address found. Switching to connection activity");
            switchToConnectionActivity();
            return;
        }
        setContentView(R.layout.activity_main);
        startMessagesService();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, MessagesService.INTENT_FILTER);
    }

    private boolean obtainBaseAddress(Bundle savedInstanceState) {
        baseAddress = null;
        if (getIntent().hasExtra(EXTRA_BASEADDRESS)) {
            baseAddress = getIntent().getParcelableExtra(EXTRA_BASEADDRESS);
        } else if (MessagesService.isRunning()) {
            baseAddress = MessagesService.getBaseAddress();
        } else if (savedInstanceState != null) {
            baseAddress = savedInstanceState.getParcelable(STATE_BASEADDRESS);
        }
        return baseAddress != null;
    }

    private void startMessagesService() {
        Intent intent = new Intent(getBaseContext(), MessagesService.class);
        if (!MessagesService.isRunning()) {
            intent.setAction(MessagesService.ACTION_INIT);
            intent.putExtra(MessagesService.EXTRA_BASEADDRESS, baseAddress);
        } else {
            intent.setAction(MessagesService.ACTION_MESSAGES);
        }
        startService(intent);
    }

    private void switchToConnectionActivity() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (baseAddress != null) {
            savedInstanceState.putParcelable(STATE_BASEADDRESS, baseAddress);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.quit) {
            quit();
        }
        return super.onOptionsItemSelected(item);
    }

    private void quit() {
        stopMessagesService();
        finish();
    }

    private void stopMessagesService() {
        if (MessagesService.isRunning()) {
            Intent intent = new Intent(getBaseContext(), MessagesService.class);
            stopService(intent);
        }
    }

    private class MainActivityBroadcaseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MessagesService.BROADCAST_MESSAGES.equals(action)) {
                ArrayList<Message> messages = intent.getParcelableArrayListExtra(MessagesService.EXTRA_MESSAGES);
                handleMessages(messages);
            }
        }
    }

    private void handleMessages(ArrayList<Message> messages) {
        Log.d(TAG, String.format("Received %d message(s)", messages.size()));
        ListView messagesListView = (ListView)findViewById(R.id.messagesListView);
        messagesListView.setAdapter(new MessageListAdapter(messages));
    }

    private class MessageListAdapter extends ArrayAdapter<Message> {

        public MessageListAdapter(List<Message> messages) {
            super(MainActivity.this, -1, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Message message = getItem(position);
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            View messageView = layoutInflater.inflate(R.layout.message_list_item, parent, false);
            TextView textTextView = (TextView)messageView.findViewById(R.id.textTextView);
            textTextView.setText(message.getText());
            return messageView;
        }
    }
}
