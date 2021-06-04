package se.arctosoft.tvchat.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.arctosoft.tvchat.R;
import se.arctosoft.tvchat.utils.Settings;
import se.arctosoft.tvchat.data.Message;
import se.arctosoft.tvchat.utils.Dialogs;
import se.arctosoft.tvchat.utils.Toaster;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private static final String TAG = "ChatAdapter";
    private static final int MESSAGE_OUTGOING = 123;
    private static final int MESSAGE_INCOMING = 321;
    private final List<Message> mMessages;
    private final AppCompatActivity mActivity;
    private final String mUserId;
    private static final Map<String, String> iconMap = new HashMap<>();
    private final float[] lastTouchDownXY;
    private final boolean isAdmin;

    public ChatAdapter(AppCompatActivity context, String mUserId, List<Message> messages, float[] lastTouchDownXY) {
        mMessages = messages;
        this.mUserId = mUserId;
        mActivity = context;
        this.lastTouchDownXY = lastTouchDownXY;
        this.isAdmin = new Settings(context).isAdmin();
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
        boolean isOwnMessage = message.getUserId().equals(ParseUser.getCurrentUser().getObjectId());

        Glide.with(holder.body.getContext())
                .load(getProfileUrl(message.getUserId()))
                .circleCrop() // create an effect of a round profile picture
                .into(holder.icon);
        holder.body.setText(message.getBody());
        holder.name.setText(message.getUserName()); // in addition to message show user ID
        holder.root.setOnClickListener(v -> {
            Log.e(TAG, "onBindViewHolder: pos " + position);
            float x = lastTouchDownXY[0];
            float y = lastTouchDownXY[1];

            showPopup(holder, message, x, y, isOwnMessage);
        });
    }

    private void showPopup(MessageViewHolder holder, Message message, float x, float y, boolean isOwnMessage) {
        final ViewGroup root = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);

        final View view = new View(mActivity);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setBackgroundColor(Color.TRANSPARENT);

        root.addView(view);

        view.setX(x);
        view.setY(y);

        PopupMenu popupMenu = new PopupMenu(mActivity, view, Gravity.TOP);

        popupMenu.getMenuInflater().inflate(isOwnMessage ? R.menu.menu_message_outgoing :
                (isAdmin ? R.menu.menu_message_incoming_admin : R.menu.menu_message_incoming), popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.copy) {
                ClipboardManager clipboard = mActivity.getSystemService(ClipboardManager.class);
                clipboard.setPrimaryClip(ClipData.newPlainText("message", holder.body.getText().toString()));
            } else if (id == R.id.report) {
                Dialogs.showReportConfirm(mActivity, () -> {
                    ParseObject report = new ParseObject("Report");
                    ParseACL acl = new ParseACL();
                    acl.setPublicReadAccess(true);
                    acl.setPublicWriteAccess(false);
                    report.setACL(acl);
                    report.put("u", message.getUserId());
                    report.put("m", message);
                    report.put("b", message.getBody().trim());
                    Toaster.getInstance(mActivity).showShort(mActivity.getString(R.string.message_reported));
                    report.saveInBackground(e -> {
                        int pos = holder.getBindingAdapterPosition();
                        if (e != null) {
                            e.printStackTrace();
                        }
                        removeMessageAt(pos);
                    });
                });
            } else if (id == R.id.delete) {
                message.deleteInBackground(e -> {
                    if (e != null) {
                        e.printStackTrace();
                        Toaster.getInstance(mActivity).showShort(mActivity.getString(R.string.message_deleted_error));
                    } else {
                        removeMessageAt(holder.getBindingAdapterPosition());
                        Toaster.getInstance(mActivity).showShort(mActivity.getString(R.string.message_deleted));
                    }
                });
            } else if (id == R.id.purge) {
                Dialogs.showPurgeConfirm(mActivity, () -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("u", message.getUserId());
                    ParseCloud.callFunctionInBackground("purge", map, (object, e) -> {
                        if (e != null) {
                            e.printStackTrace();
                            Toaster.getInstance(mActivity).showShort("Failed to purge for user: " + e.getMessage());
                        } else {
                            Toaster.getInstance(mActivity).showShort("Purged");
                        }
                    });
                });
            } else if (id == R.id.block) {
                Dialogs.showBlockDialog(mActivity, time -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("u", message.getUserId());
                    map.put("t", time);
                    ParseCloud.callFunctionInBackground("block", map, (object, e) -> {
                        if (e != null) {
                            e.printStackTrace();
                            Toaster.getInstance(mActivity).showShort("Failed to block user: " + e.getMessage());
                        } else {
                            Toaster.getInstance(mActivity).showShort("Blocked user for " + ((int) (time / 60000)) + " minutes");
                        }
                    });
                });
            } else if (id == R.id.unblock) {
                Dialogs.showUnblockConfirm(mActivity, () -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("u", message.getUserId());
                    ParseCloud.callFunctionInBackground("unblock", map, (object, e) -> {
                        if (e != null) {
                            e.printStackTrace();
                            Toaster.getInstance(mActivity).showShort("Failed to unblock user: " + e.getMessage());
                        } else {
                            Toaster.getInstance(mActivity).showShort("Unblocked user");
                        }
                    });
                });
            }
            return false;
        });

        popupMenu.setOnDismissListener(menu -> root.removeView(view));
        popupMenu.show();
    }

    private void removeMessageAt(int pos) {
        if (pos < 0 || mMessages.size() <= pos) {
            return;
        }
        mMessages.remove(pos);
        notifyItemRemoved(pos);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView body, name;
        private final View root;

        private MessageViewHolder(View itemView) {
            super(itemView);
            root = itemView;
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
        Log.e(TAG, "getProfileUrl: get for " + hex);
        String s = "https://www.gravatar.com/avatar/" + hex + "?d=identicon";
        //String s = "https://avatars.dicebear.com/api/human/" + userId + ".svg";
        iconMap.put(userId, s);
        return s;
    }
}