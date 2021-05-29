package se.arctosoft.tvchat.data;

import androidx.annotation.NonNull;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("Channel")
public class Channel extends ParseObject {
    public static final String NAME_KEY = "c";
    public static final String ORDER_KEY = "o";
    public static final String ICON_KEY = "i";

    public String getName() {
        return getString(NAME_KEY);
    }

    public int getOrder() {
        return getInt(ORDER_KEY);
    }

    public ParseFile getIcon() {
        return getParseFile(ICON_KEY);
    }

    public void setName(String name) {
        put(NAME_KEY, name);
    }

    public void setOrder(int order) {
        put(ORDER_KEY, order);
    }

    public void setIcon(ParseFile icon) {
        put(ICON_KEY, icon);
    }

    @NonNull
    @Override
    public String toString() {
        return "name: " + getName() + ", order: " + getOrder() + ", icon: " + getIcon();
    }
}