package se.arctosoft.tvchat.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Message")
public class Message extends ParseObject {
    public static final String USER_KEY = "u";
    public static final String BODY_KEY = "b";
    public static final String CHANNEL_KEY = "c";

    public ParseUser getUser() {
        return getParseUser(USER_KEY);
    }

    public String getBody() {
        return getString(BODY_KEY);
    }

    public Channel getChannel() {
        return (Channel) getParseObject(CHANNEL_KEY);
    }

    public void setUser(ParseUser user) {
        put(USER_KEY, user);
    }

    public void setBody(String body) {
        put(BODY_KEY, body);
    }

    public void setChannel(Channel channel) {
        put(CHANNEL_KEY, channel);
    }
}