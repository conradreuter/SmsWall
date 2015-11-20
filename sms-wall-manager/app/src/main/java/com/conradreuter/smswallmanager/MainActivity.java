package com.conradreuter.smswallmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String STATE_BASEADDRESS = "com.conradreuter.smswallmanager.state.BASEADDRESS";

    public static final String EXTRA_BASEADDRESS = "com.conradreuter.smswallmanager.action.BASEADDRESS";

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
        if (!MessagesService.isRunning()) {
            startMessagesService();
        }
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
        intent.setAction(MessagesService.ACTION_SET_BASEADDRESS);
        intent.putExtra(MessagesService.EXTRA_BASEADDRESS, baseAddress);
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
        if (MessagesService.isRunning()) {
            stopMessagesService();
        }
        finish();
    }

    private void stopMessagesService() {
        Intent intent = new Intent(getBaseContext(), MessagesService.class);
        stopService(intent);
    }
}
