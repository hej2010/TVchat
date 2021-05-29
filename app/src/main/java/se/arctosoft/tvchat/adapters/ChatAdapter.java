package se.arctosoft.tvchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.arctosoft.tvchat.R;
import se.arctosoft.tvchat.data.Message;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private static final int MESSAGE_OUTGOING = 123;
    private static final int MESSAGE_INCOMING = 321;
    private final List<Message> mMessages;
    private final Context mContext;
    private final String mUserId;
    private static final Map<String, String> iconMap = new HashMap<>();

    public ChatAdapter(Context context, String mUserId, List<Message> messages) {
        mMessages = messages;
        this.mUserId = mUserId;
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isMe(position)) {
            return MESSAGE_OUTGOING;
        } else {
            return MESSAGE_INCOMING;
        }
    }

    private boolean isMe(int position) {
        Message message = mMessages.get(position);
        return message.getUserId() != null && message.getUserId().equals(mUserId);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView;
        if (viewType == MESSAGE_INCOMING) {
            contactView = inflater.inflate(R.layout.message_incoming, parent, false);
        } else if (viewType == MESSAGE_OUTGOING) {
            contactView = inflater.inflate(R.layout.message_outgoing, parent, false);
        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
        return new MessageViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = mMessages.get(position);

        Glide.with(holder.body.getContext())
                .load(getProfileUrl(message.getUserId()))
                .circleCrop() // create an effect of a round profile picture
                .into(holder.icon);
        holder.body.setText(message.getBody());
        holder.name.setText(message.getUserId()); // in addition to message show user ID
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView body, name;

        private MessageViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivIcon);
            body = itemView.findViewById(R.id.tvBody);
            name = itemView.findViewById(R.id.tvName);
        }
    }

    // Create a gravatar image based on the hash value obtained from userId
    private static String getProfileUrl(final String userId) {
        if (iconMap.containsKey(userId)) {
            return iconMap.get(userId);
        }
        String hex = "";
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final byte[] hash = digest.digest(userId.getBytes());
            final BigInteger bigInt = new BigInteger(hash);
            hex = bigInt.abs().toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String s = "https://www.gravatar.com/avatar/" + hex + "?d=identicon";
        iconMap.put(userId, s);
        return s;
    }
}