package com.conradreuter.smswallmanager;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsMessage;
import android.util.JsonReader;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class Message implements Parcelable {

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {

        @Override
        public Message createFromParcel(Parcel parcel) {
            int id = parcel.readInt();
            String sender = parcel.readString();
            String text = parcel.readString();
            return new Message(id, sender, text);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

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
        return fromJson(jsonReader);
    }

    public static ArrayList<Message> multipleFromHttpResponse(HttpResponse response) throws IOException {
        InputStream stream = response.getEntity().getContent();
        JsonReader jsonReader = new JsonReader(new InputStreamReader(stream));
        jsonReader.beginArray();
        ArrayList<Message> messages = new ArrayList<Message>();
        while (jsonReader.hasNext()) {
            Message message = fromJson(jsonReader);
            messages.add(message);
        }
        jsonReader.endArray();
        return messages;
    }

    public static Message fromJson(JsonReader jsonReader) throws IOException {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        int id = getId() != null ? getId().intValue() : -1;
        parcel.writeInt(id);
        parcel.writeString(getSender());
        parcel.writeString(getText());
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

    @Override
    public String toString() {
        return String.format("\"%s\" from %s", getText(), getSender());
    }
}
