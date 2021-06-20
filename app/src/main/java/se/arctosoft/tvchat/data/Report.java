package se.arctosoft.tvchat.data;

import androidx.annotation.Nullable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Report")
public class Report extends ParseObject {
    public static final String BODY_KEY = "b";
    public static final String CHANNEL_KEY = "c";
    public static final String MESSAGE_KEY = "m";
    public static final String MESSAGE_USER_ID_KEY = "u";

    public String getBody() {
        return getString(BODY_KEY);
    }

    public ParseObject getChannel() {
        return getParseObject(CHANNEL_KEY);
    }

    public ParseObject getMessage() {
        return getParseObject(MESSAGE_KEY);
    }

    public String getUserId() {
        return getString(MESSAGE_USER_ID_KEY);
    }

    public void setBody(String body) {
        put(BODY_KEY, body);
    }

    public void setChannel(ParseObject channel) {
        put(CHANNEL_KEY, channel);
    }

    public void setMessage(ParseObject message) {
        put(MESSAGE_KEY, message);
    }

    public void setUserId(String userId) {
        put(MESSAGE_USER_ID_KEY, userId);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        return super.equals(obj);
    }
}
