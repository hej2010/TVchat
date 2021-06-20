package se.arctosoft.tvchat.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.DeleteCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

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

    public ReportsAdapter(Activity context, List<Report> reports) {
        mReports = reports;
        mActivity = context;
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
        holder.clickable.setOnClickListener(v -> Dialogs.showReportClickedDialog(mActivity, new Dialogs.IOnReportClickedListener() {
            @Override
            public void onDelete() {
                Dialogs.showDeleteConfirm(mActivity, () -> {
                    if (report.getMessage() != null) {
                        report.getMessage().deleteInBackground(e -> {
                            if (e != null) {
                                e.printStackTrace();
                                Toaster.getInstance(mActivity).showShort("Error: " + e.getMessage());
                            } else {
                                onIgnore();
                                Toaster.getInstance(mActivity).showShort("Deleted");
                            }
                        });
                    } else {
                        Toaster.getInstance(mActivity).showShort("Message is null");
                    }
                });
            }

            @Override
            public void onBlock() {
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

            @Override
            public void onEdit(String newString) {
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

            @Override
            public void onIgnore() {
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
        }, report));
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