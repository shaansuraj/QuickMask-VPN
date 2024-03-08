package com.liberty.apps.studio.libertyvpn.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.liberty.apps.studio.libertyvpn.model.Server;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Jhon Kenneth Carino on 10/18/15.
 */
public class OvpnUtils {

    private static final String FILE_EXTENSION = ".ovpn";
    private static final String OPENVPN_PKG_NAME = "net.openvpn.openvpn";
    private static final String OPENVPN_MIME_TYPE = "application/x-openvpn-profile";

    public static String humanReadableCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp-1);
        return String.format("%.2f %s" + (si ? "bps" : "B"),
                bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Writes and saves OVPN profile to a file
     *
     * @param context The context of an application
     * @param server The {@link Server} that contains OVPN profile
     */
    private static void saveConfigData(@NonNull Context context, @NonNull Server server) {
        File file;
        FileOutputStream outputStream;

        try {
            file = getFile(context, server);
            outputStream = new FileOutputStream(file);
            outputStream.write(server.ovpnConfigData.getBytes("UTF-8"));
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an empty file for OVPN profile
     *
     * @param context The context of an application
     * @param server The {@link Server} that contains OVPN profile
     */
    private static File getFile(@NonNull Context context, @NonNull Server server) {
        File filePath;
        if (!Environment.isExternalStorageRemovable() || isExternalStorageWritable()) {
            filePath = context.getExternalCacheDir();
        } else {
            filePath = context.getCacheDir();
        }
        return new File(filePath, server.countryShort + "_" + server.hostName + "_" +
                server.protocol.toUpperCase() + FILE_EXTENSION);
    }

    /**
     * @return Whether the external storage is available for read and write.
     */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static int getDrawableResource(@NonNull Context context, @NonNull String resource) {
        return context.getResources()
                .getIdentifier(resource, "drawable", context.getPackageName());
    }

    /**
     * Shows an intent chooser to share OVPN profile.
     *
     * @param activity The context of an activity
     * @param server The {@link Server} that contains OVPN profile
     */
    public static void shareOvpnFile(@NonNull Activity activity, @NonNull Server server) {
        File file = getFile(activity, server);
        if (!file.exists()) {
            saveConfigData(activity, server);
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(getFile(activity, server)));
        activity.startActivity(Intent.createChooser(intent, "Share Profile using"));
    }
}
