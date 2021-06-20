package se.arctosoft.tvchat.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseCloud;
import com.parse.ParseObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.arctosoft.tvchat.R;
import se.arctosoft.tvchat.data.Channel;
import se.arctosoft.tvchat.data.Report;
import se.arctosoft.tvchat.utils.Dialogs;
import se.arctosoft.tvchat.utils.Toaster;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {
    private static final String TAG = "ReportsAdapter";
    private final List<Report> mReports;
    private final Activity mActivity;

    private final float[] lastTouchDownXY;

    public ReportsAdapter(Activity context, List<Report> reports, float[] lastTouchDownXY) {
        mReports = reports;
        mActivity = context;

        this.lastTouchDownXY = lastTouchDownXY;
    }

    @Override
    public int getItemCount() {
        return mReports.size();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        Report report = mReports.get(position);
        holder.tvBody.setText(report.getBody());
        ParseObject channelObj = report.getChannel();
        if (channelObj instanceof Channel) {
            holder.tvChannel.setText(((Channel) channelObj).getName());
        } else if (channelObj != null) {
            holder.tvChannel.setText(channelObj.getObjectId());
        }
        holder.tvName.setText(report.getUserId());
        holder.tvDate.setText(report.getCreatedAt().toString());
        holder.clickable.setOnClickListener(v -> {
            float x = lastTouchDownXY[0];
            float y = lastTouchDownXY[1];

            showPopup(report, x, y);
        });
    }

    private void showPopup(Report report, float x, float y) {
        final ViewGroup root = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);

        final View view = new View(mActivity);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setBackgroundColor(Color.TRANSPARENT);

        root.addView(view);

        view.setX(x);
        view.setY(y);

        PopupMenu popupMenu = new PopupMenu(mActivity, view, Gravity.TOP);

        popupMenu.getMenuInflater().inflate(R.menu.menu_report_clicked, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (report.getMessage() == null) {
                ignoreReport(report);
            } else {
                if (id == R.id.delete_message) {
                    deleteMessage(report);
                } else if (id == R.id.edit_message) {
                    Dialogs.showEditMessageDialog(mActivity, newString -> editMessage(report, newString), report.getBody());
                } else if (id == R.id.block_user) {
                    blockUser(report);
                } else if (id == R.id.ignore_report) {
                    ignoreReport(report);
                }
            }

            return false;
        });

        popupMenu.setOnDismissListener(menu -> root.removeView(view));
        popupMenu.show();
    }

    private void ignoreReport(Report report) {
        report.deleteInBackground(e -> {
            if (e != null) {
                e.printStackTrace();
                Toaster.getInstance(mActivity).showShort("Failed to delete: " + e.getMessage());
            } else {
                Toaster.getInstance(mActivity).showShort("Deleted");
                int index = mReports.indexOf(report);
                mReports.remove(index);
                notifyItemRemoved(index);
            }
        });
    }

    private void editMessage(Report report, String newString) {
        report.getMessage().put("b", newString);
        report.getMessage().saveInBackground(e -> {
            if (e != null) {
                e.printStackTrace();
                Toaster.getInstance(mActivity).showShort("Failed to save: " + e.getMessage());
            } else {
                Toaster.getInstance(mActivity).showShort("Saved");
            }
        });
    }

    private void blockUser(Report report) {
        Dialogs.showBlockDialog(mActivity, time -> {
            Map<String, Object> map = new HashMap<>();
            map.put("u", report.getUserId());
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
    }

    private void deleteMessage(Report report) {
        Dialogs.showDeleteConfirm(mActivity, () -> {
            if (report.getMessage() != null) {
                report.getMessage().deleteInBackground(e -> {
                    if (e != null) {
                        e.printStackTrace();
                        Toaster.getInstance(mActivity).showShort("Error: " + e.getMessage());
                    } else {
                        ignoreReport(report);
                        Toaster.getInstance(mActivity).showShort("Deleted");
                    }
                });
            } else {
                Toaster.getInstance(mActivity).showShort("Message is null");
            }
        });
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        View clickable;
        TextView tvBody, tvName, tvDate, tvChannel;

        public ReportViewHolder(View itemView) {
            super(itemView);
            clickable = itemView;
            tvBody = itemView.findViewById(R.id.tvBody);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvChannel = itemView.findViewById(R.id.tvChannel);
        }
    }

}