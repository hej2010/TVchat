package se.arctosoft.tvchat.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class Toaster {
    private static Toaster toastMaker;
    private static android.widget.Toast toast;
    private final WeakReference<Context> weakReference;

    private Toaster(@NonNull Context context) {
        weakReference = new WeakReference<>(context);
    }

    public static Toaster getInstance(@NonNull Context context) {
        if (toastMaker == null) {
            toastMaker = new Toaster(context.getApplicationContext());
        }
        return toastMaker;
    }

    public void showShort(@NonNull String message) {
        show(message, false);
    }

    public void showLong(@NonNull String message) {
        show(message, true);
    }

    private void show(@NonNull String message, boolean _long) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(weakReference.get(), message, _long ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.show();
    }
}
