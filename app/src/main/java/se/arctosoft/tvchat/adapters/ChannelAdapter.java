package se.arctosoft.tvchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import se.arctosoft.tvchat.ChatActivity;
import se.arctosoft.tvchat.R;
import se.arctosoft.tvchat.data.Channel;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.MessageViewHolder> {
    private static final String TAG = "ChannelAdapter";
    private final List<Channel> mChannels;
    private final Context mContext;

    public ChannelAdapter(Context context, List<Channel> messages) {
        mChannels = messages;
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mChannels.size();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Channel channel = mChannels.get(position);
        holder.txtName.setText(channel.getName());
        Glide.with(mContext).load(channel.getIcon().getUrl())
                .centerCrop()
                .into(holder.ivChannelIcon);
        holder.clickable.setOnClickListener(v -> {
            Log.e(TAG, "onClick: " + channel.getName());
            mContext.startActivity(new Intent(mContext, ChatActivity.class)
                    .putExtra(ChatActivity.EXTRA_CHANNEL, channel));
        });
        holder.txtBody.setText(mContext.getString(R.string.channel_text, channel.getNrOfMessages()));
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        View clickable;
        ImageView ivChannelIcon;
        TextView txtBody, txtName;

        public MessageViewHolder(View itemView) {
            super(itemView);
            clickable = itemView.findViewById(R.id.rLContent);
            ivChannelIcon = itemView.findViewById(R.id.ivChannelIcon);
            txtBody = itemView.findViewById(R.id.tvBody);
            txtName = itemView.findViewById(R.id.tvName);
        }
    }

}