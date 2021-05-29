package se.arctosoft.tvchat.data;

import androidx.annotation.NonNull;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Message")
public class Message extends ParseObject {
    public static final String USER_ID_KEY = "u";
    public static final String BODY_KEY = "b";
    public static final String CHANNEL_KEY = "c";

    public String getUserId() {
        return getString(USER_ID_KEY);
    }

    public String getBody() {
        return getString(BODY_KEY);
    }

    public Channel getChannel() {
        return (Channel) getParseObject(CHANNEL_KEY);
    }

    public void setUserId(String userId) {
        put(USER_ID_KEY, userId);
    }

    public void setBody(String body) {
        put(BODY_KEY, body);
    }

    public void setChannel(Channel channel) {
        put(CHANNEL_KEY, channel);
    }

    @NonNull
    @Override
    public String toString() {
        return "user: " + getUserId() + ", body: " + getBody() + ", channel: " + getChannel();
    }
}