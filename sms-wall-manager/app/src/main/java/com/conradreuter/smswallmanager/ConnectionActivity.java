package com.conradreuter.smswallmanager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.URI;

public final class ConnectionActivity extends ActionBarActivity {

    private static final String TAG = ConnectionActivity.class.getSimpleName();

    private final BroadcastReceiver connectionBroadcastReceiver = new ConnectionBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        attachEventListeners();
        LocalBroadcastManager.getInstance(this).registerReceiver(connectionBroadcastReceiver, ConnectionService.INTENT_FILTER);
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(connectionBroadcastReceiver);
        super.onStop();
    }

    private void attachEventListeners() {
        final Button connectButton = (Button)findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });
    }

    private void connect() {
        EditText serverAddressEditText = (EditText)findViewById(R.id.serverAddressEditText);
        String serverAddress = serverAddressEditText.getText().toString();
        ConnectionService.startActionConnect(this, serverAddress);
    }

    private class ConnectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectionService.BROADCAST_CONNECTION_SUCCEEDED.equals(action)) {
                URI baseAddress = URI.create(intent.getStringExtra(ConnectionService.EXTRA_BASEADDRESS));
                handleConnectionSucceeded(baseAddress);
            } else if (ConnectionService.BROADCAST_CONNECTION_FAILED.equals(action)) {
                String baseAddress = intent.getStringExtra(ConnectionService.EXTRA_BASEADDRESS);
                String errorMessage = intent.getStringExtra(ConnectionService.EXTRA_ERRORMESSAGE);
                handleConnectionFailed(baseAddress, errorMessage);
            }
        }
    }

    private void handleConnectionSucceeded(URI baseAddress) {
        Log.d(TAG, String.format("Connection to %s succeeded", baseAddress.toString()));
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_BASEADDRESS, baseAddress.toString());
        startActivity(intent);
        finish();
    }

    private void handleConnectionFailed(String baseAddress, String errorMessage) {
        Log.e(TAG, String.format("Connection to %s failed: %s", baseAddress, errorMessage));
        new AlertDialog.Builder(this)
                .setTitle("Connection failed!")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(errorMessage)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }
}
