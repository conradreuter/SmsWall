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

public final class Message implements Parcelable {

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {

        @Override
        public Message createFromParcel(Parcel parcel) {
            Message message = new Message();
            message.id = parcel.readInt();
            message.timestamp = parcel.readLong();
            message.sender = parcel.readString();
            message.text = parcel.readString();
            return message;
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    private Integer id;
    private Long timestamp;
    private String sender;
    private String text;

    private Message() {
    }

    public static Message fromSms(SmsMessage smsMessage) {
        String sender = smsMessage.getDisplayOriginatingAddress();
        String text = smsMessage.getDisplayMessageBody();
        Message message = new Message();
        message.sender = sender;
        message.text = text;
        return message;
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
        Message message = new Message();
        while (jsonReader.hasNext()) {
            String propertyName = jsonReader.nextName();
            if (propertyName.equals("id")) {
                message.id = jsonReader.nextInt();
            } else if (propertyName.equals("timestamp")) {
                message.timestamp = jsonReader.nextLong();
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
        parcel.writeInt(getId());
        parcel.writeLong(getTimestamp());
        parcel.writeString(getSender());
        parcel.writeString(getText());
    }

    public int getId() {
        return id != null ? id.intValue() : -1;
    }

    public long getTimestamp() {
        return timestamp != null ? timestamp.longValue() : -1L;
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
