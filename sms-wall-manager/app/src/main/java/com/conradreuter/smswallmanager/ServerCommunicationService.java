package com.conradreuter.smswallmanager;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.URI;

public final class ServerCommunicationService extends IntentService {

    private static final String TAG = ServerCommunicationService.class.getSimpleName();

    private static final String ACTION_TEST_CONNECTION = "com.conradreuter.smswallmanager.action.TEST_CONNECTION";
    private static final String ACTION_PUT_MESSAGE = "com.conradreuter.smswallmanager.action.PUT_MESSAGE";

    public static final String BROADCAST_CONNECTION_SUCCEEDED = "com.conradreuter.smswallmanager.action.CONNECTION_SUCCEEDED";
    public static final String BROADCAST_CONNECTION_FAILED = "com.conradreuter.smswallmanager.action.CONNECTION_FAILED";

    public static final IntentFilter INTENT_FILTER = new IntentFilter();
    static {
        INTENT_FILTER.addAction(BROADCAST_CONNECTION_SUCCEEDED);
        INTENT_FILTER.addAction(BROADCAST_CONNECTION_FAILED);
    }

    private static final String EXTRA_SERVERADDRESS = "com.conradreuter.smswallmanager.extra.SERVERADDRESS";
    private static final String EXTRA_MESSAGE = "com.conradreuter.smswallmanager.extra.MESSAGE";
    public static final String EXTRA_BASEADDRESS = "com.conradreuter.smswallmanager.extra.BASEADDRESS";
    public static final String EXTRA_ERRORMESSAGE = "com.conradreuter.smswallmanager.extra.ERROR";

    private static final int TIMEOUT = 1000;

    public static void startActionTestConnection(Context context, String serverAddress) {
        Intent intent = new Intent(context, ServerCommunicationService.class);
        intent.setAction(ACTION_TEST_CONNECTION);
        intent.putExtra(EXTRA_SERVERADDRESS, serverAddress);
        context.startService(intent);
    }

    public static void startActionPutMessage(Context context, Message message, Uri baseAddress) {
        Intent intent = new Intent(context, ServerCommunicationService.class);
        intent.setAction(ACTION_PUT_MESSAGE);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(EXTRA_BASEADDRESS, baseAddress);
        context.startService(intent);
    }

    public ServerCommunicationService() {
        super(ServerCommunicationService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TEST_CONNECTION.equals(action)) {
                String serverAddress = intent.getStringExtra(EXTRA_SERVERADDRESS);
                handleActionTestConnection(serverAddress);
            } else if (ACTION_PUT_MESSAGE.equals(action)) {
                Message message = intent.getParcelableExtra(EXTRA_MESSAGE);
                Uri baseAddress = intent.getParcelableExtra(EXTRA_BASEADDRESS);
                handleActionPutMessage(message, baseAddress);
            }
        }
    }

    private void handleActionTestConnection(String serverAddress) {
        String baseAddressString = String.format("http://%s:8080/", serverAddress);
        Log.d(TAG, String.format("Trying to connect to %s", baseAddressString));
        Uri baseAddress;
        HttpResponse response;
        try {
            baseAddress = Uri.parse(baseAddressString);
            response = sendRequest(new HttpGet(), baseAddress, "ping");
        } catch (Exception e) {
            broadcastConnectionFailed(baseAddressString, String.format(
                    "%s: %s",
                    e.getClass().getSimpleName(),
                    e.getMessage()));
            return;
        }
        if (!checkStatusCode(response, HttpStatus.SC_OK)) {
            broadcastConnectionFailed(baseAddressString, String.format(
                    "Unexpected status code %d (%s)",
                    response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase()));
            return;
        }
        broadcastConnectionSucceeded(baseAddress);
    }

    private void broadcastConnectionSucceeded(Uri baseAddress) {
        Log.d(TAG, String.format("Connection to  %s succeeded", baseAddress));
        Intent intent = new Intent(BROADCAST_CONNECTION_SUCCEEDED);
        intent.putExtra(EXTRA_BASEADDRESS, baseAddress);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastConnectionFailed(String baseAddress, String errorMessage) {
        Log.e(TAG, String.format("Connection to %s failed: %s", baseAddress, errorMessage));
        Intent intent = new Intent(BROADCAST_CONNECTION_FAILED);
        intent.putExtra(EXTRA_BASEADDRESS, baseAddress);
        intent.putExtra(EXTRA_ERRORMESSAGE, errorMessage);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void handleActionPutMessage(Message message, Uri baseAddress) {
        Log.d(TAG, String.format("Trying to put message %s to %s", message, baseAddress));
        HttpResponse response;
        try {
            HttpPut request = new HttpPut();
            request.setEntity(new StringEntity(message.getText()));
            response = sendRequest(request, baseAddress, "message");
        } catch (Exception e) {
            Log.e(TAG, String.format("Putting message %s failed", message), e);
            return;
        }
        if (checkStatusCode(response, HttpStatus.SC_CREATED)) {
            Log.d(TAG, String.format("Putting message %s succeeded", message));
        }
    }

    private HttpResponse sendRequest(HttpRequestBase request, Uri baseAddress, String path) throws IOException {
        Uri uri = baseAddress
                .buildUpon()
                .appendPath(path)
                .build();
        request.setURI(URI.create(uri.toString()));
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
        Log.d(TAG, String.format("Sending request %s", request.getURI()));
        return new DefaultHttpClient(httpParams).execute(request);
    }

    private boolean checkStatusCode(HttpResponse response, int expectedStatusCode) {
        if (response.getStatusLine().getStatusCode() != expectedStatusCode) {
            Log.e(TAG, String.format(
                    "Unexpected status code %d (%s)",
                    response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase()));
            return false;
        }
        return true;
    }
}
