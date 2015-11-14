package com.conradreuter.smswallmanager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.JsonReader;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Message {

    private static final String BROADCAST_NEW_MESSAGE = "com.conradreuter.smswallmanager.broadcast.NEW_MESSAGE";

    private static final String EXTRA_ID = "com.conradreuter.smswallmanager.extra.MESSAGE_ID";
    private static final String EXTRA_SENDER = "com.conradreuter.smswallmanager.extra.MESSAGE_SENDER";
    private static final String EXTRA_TEXT = "com.conradreuter.smswallmanager.extra.MESSAGE_TEXT";

    public static final IntentFilter INTENT_FILTER = new IntentFilter(BROADCAST_NEW_MESSAGE);

    private Integer id;
    private String sender;
    private String text;

    private Message(Integer id, String sender, String text) {
        this.id = id;
        this.sender = sender;
        this.text = text;
    }

    public static Message fromSms(SmsMessage smsMessage) {
        String sender = smsMessage.getDisplayOriginatingAddress();
        String text = smsMessage.getDisplayMessageBody();
        return new Message(null, sender, text);
    }

    public static Message fromHttpResponse(HttpResponse response) throws IOException {
        InputStream stream = response.getEntity().getContent();
        JsonReader jsonReader = new JsonReader(new InputStreamReader(stream));
        jsonReader.beginObject();
        Message message = new Message(null, null, null);
        while (jsonReader.hasNext()) {
            String propertyName = jsonReader.nextName();
            if (propertyName.equals("id")) {
                message.id = jsonReader.nextInt();
            } else if (propertyName.equals("sender")) {
                message.sender = jsonReader.nextString();
            } else if (propertyName.equals("text")) {
                message.text = jsonReader.nextString();
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return message;
    }

    public static Message fromIntent(Intent intent) {
        int id = intent.getIntExtra(EXTRA_ID, -1);
        String sender = intent.getStringExtra(EXTRA_SENDER);
        String text = intent.getStringExtra(EXTRA_TEXT);
        return new Message(id == -1 ?  null : id, sender, text);
    }

    public void fillIntent(Intent intent) {
        intent.putExtra(EXTRA_ID, getId());
        intent.putExtra(EXTRA_SENDER, getSender());
        intent.putExtra(EXTRA_TEXT, getText());
    }

    public Integer getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public void broadcast(Context context) {
        Intent intent = new Intent(BROADCAST_NEW_MESSAGE);
        fillIntent(intent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
