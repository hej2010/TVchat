package se.arctosoft.tvchat.data;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("Channel")
public class Channel extends ParseObject {
    public static final String NAME_KEY = "c";
    public static final String ORDER_KEY = "o";
    public static final String ICON_KEY = "i";
    public static final String MESSAGES_KEY = "m";

    public String getName() {
        return getString(NAME_KEY);
    }

    public int getOrder() {
        return getInt(ORDER_KEY);
    }

    public ParseFile getIcon() {
        return getParseFile(ICON_KEY);
    }

    public int getNrOfMessages() {
        return getInt(MESSAGES_KEY);
    }

    public void setName(String name) {
        put(NAME_KEY, name);
    }

    public void setOrder(int order) {
        put(ORDER_KEY, order);
    }

    public void setMessages(int messages) {
        put(MESSAGES_KEY, messages);
    }

    public void setIcon(ParseFile icon) {
        put(ICON_KEY, icon);
    }
}