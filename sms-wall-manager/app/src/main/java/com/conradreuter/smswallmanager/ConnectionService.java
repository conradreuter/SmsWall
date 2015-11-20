package com.conradreuter.smswallmanager;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.net.URI;

public final class ConnectionService extends IntentService {

    private static final String TAG = ConnectionService.class.getSimpleName();

    private static final String ACTION_CONNECT = "com.conradreuter.smswallmanager.action.CONNECT";

    public static final String BROADCAST_CONNECTION_SUCCEEDED = "com.conradreuter.smswallmanager.action.CONNECTION_SUCCEEDED";
    public static final String BROADCAST_CONNECTION_FAILED = "com.conradreuter.smswallmanager.action.CONNECTION_FAILED";

    public static final IntentFilter INTENT_FILTER = new IntentFilter();

    static {
        INTENT_FILTER.addAction(BROADCAST_CONNECTION_SUCCEEDED);
        INTENT_FILTER.addAction(BROADCAST_CONNECTION_FAILED);
    }

    private static final String EXTRA_SERVERADDRESS = "com.conradreuter.smswallmanager.extra.SERVERADDRESS";
    public static final String EXTRA_BASEADDRESS = "com.conradreuter.smswallmanager.extra.BASEADDRESS";
    public static final String EXTRA_ERRORMESSAGE = "com.conradreuter.smswallmanager.extra.ERROR";

    private static final int TIMEOUT = 1000;

    public static void startActionConnect(Context context, String serverAddress) {
        Intent intent = new Intent(context, ConnectionService.class);
        intent.setAction(ACTION_CONNECT);
        intent.putExtra(EXTRA_SERVERADDRESS, serverAddress);
        context.startService(intent);
    }

    public ConnectionService() {
        super(ConnectionService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CONNECT.equals(action)) {
                String serverAddress = intent.getStringExtra(EXTRA_SERVERADDRESS);
                handleActionConnect(serverAddress);
            }
        }
    }

    private void handleActionConnect(String serverAddress) {
        String baseAddressString = String.format("http://%s:8080/", serverAddress);
        Log.d(TAG, String.format("Trying to connect to %s", baseAddressString));
        URI baseAddress;
        HttpResponse response;
        try {
            baseAddress = URI.create(baseAddressString);
            HttpGet request = new HttpGet(baseAddress.resolve("/ping"));
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
            response = new DefaultHttpClient(httpParams).execute(request);
        } catch (Exception e) {
            broadcastConnectionFailed(baseAddressString, String.format(
                    "%s: %s",
                    e.getClass().getSimpleName(),
                    e.getMessage()));
            return;
        }
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            broadcastConnectionFailed(baseAddressString, String.format(
                    "Unexpected status code %d (reason: %s)",
                    response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase()));
            return;
        }
        broadcastConnectionSucceeded(baseAddress);
    }

    private void broadcastConnectionSucceeded(URI baseAddress) {
        Log.d(TAG, String.format("Connection to  %s succeeded", baseAddress));
        Intent intent = new Intent(BROADCAST_CONNECTION_SUCCEEDED);
        intent.putExtra(EXTRA_BASEADDRESS, baseAddress.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastConnectionFailed(String baseAddress, String errorMessage) {
        Log.e(TAG, String.format("Connection to %s failed: %s", baseAddress, errorMessage));
        Intent intent = new Intent(BROADCAST_CONNECTION_FAILED);
        intent.putExtra(EXTRA_BASEADDRESS, baseAddress);
        intent.putExtra(EXTRA_ERRORMESSAGE, errorMessage);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
