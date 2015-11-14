package com.conradreuter.smswallmanager;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.URI;

public class MessagesService extends IntentService {

    private static final String ACTION_PUT_MESSAGE = "com.conradreuter.smswallmanager.action.PUT_MESSAGE";

    public static void startActionPutMessage(Context context, Message message) {
        Intent intent = new Intent(context, MessagesService.class);
        intent.setAction(ACTION_PUT_MESSAGE);
        message.fillIntent(intent);
        context.startService(intent);
    }

    private final HttpClient httpClient = new DefaultHttpClient();
    private final URI baseAddress;

    public MessagesService() {
        super("MessagesService");
        this.baseAddress = URI.create("http://10.0.2.2:8080/");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PUT_MESSAGE.equals(action)) {
                final Message message = Message.fromIntent(intent);
                handleActionPutMessage(message);
            }
        }
    }

    private void handleActionPutMessage(Message message) {
        try {
            HttpPut request = new HttpPut(baseAddress.resolve("/message"));
            request.setEntity(new StringEntity(message.getText()));
            HttpResponse response = httpClient.execute(request);
            Message.fromHttpResponse(response).broadcast(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
