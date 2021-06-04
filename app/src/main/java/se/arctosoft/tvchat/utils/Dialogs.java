package se.arctosoft.tvchat.utils;

import android.app.Activity;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import se.arctosoft.tvchat.R;

public class Dialogs {

    public static void showLoginDialog(@NonNull Activity activity, @NonNull IOnLoginListener listener) {
        EditText inputEditTextField = new EditText(activity);
        inputEditTextField.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputEditTextField.setMaxLines(1);
        new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.about_login))
                .setView(inputEditTextField)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    listener.onPositive(inputEditTextField.getText().toString());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static void showBlockDialog(@NonNull Activity activity, @NonNull IOnBlockListener listener) {
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add(activity.getString(R.string.block_for_2min));
        arrayAdapter.add(activity.getString(R.string.block_for_5min));
        arrayAdapter.add(activity.getString(R.string.block_for_15min));
        arrayAdapter.add(activity.getString(R.string.block_for_30min));
        arrayAdapter.add(activity.getString(R.string.block_for_60min));
        arrayAdapter.add(activity.getString(R.string.block_for_3h));
        arrayAdapter.add(activity.getString(R.string.block_for_6h));
        arrayAdapter.add(activity.getString(R.string.block_for_12h));
        arrayAdapter.add(activity.getString(R.string.block_for_24h));
        arrayAdapter.add(activity.getString(R.string.block_for_3d));
        arrayAdapter.add(activity.getString(R.string.block_for_7d));
        arrayAdapter.add(activity.getString(R.string.block_for_14d));
        arrayAdapter.add(activity.getString(R.string.block_for_30d));
        arrayAdapter.add(activity.getString(R.string.block_for_3mon));

        new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.about_login))
                .setAdapter(arrayAdapter, (dialog, which) -> {
                    long[] time = new long[]{120000, 300000, 900000, 1800000, 3600000, 10800000, 21600000, 43200000, 86400000, 259200000, 604800000, 1209600000, 2592000000L, 7884000000L};
                    showBlockConfirm(activity, listener, time[which]);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static void showBlockConfirm(@NonNull Activity activity, @NonNull IOnBlockListener listener, long time) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.message_menu_block))
                .setMessage(activity.getString(R.string.message_menu_block_confirm))
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                    listener.onBlock(time);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static void showUnblockConfirm(@NonNull Activity activity, @NonNull IOnPositiveListener listener) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.message_menu_unblock))
                .setMessage(activity.getString(R.string.message_menu_unblock_confirm))
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                    listener.onPositive();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public interface IOnLoginListener {
        void onPositive(String pwd);
    }

    public interface IOnBlockListener {
        void onBlock(long time);
    }

    public interface IOnPositiveListener {
        void onPositive();
    }

}
