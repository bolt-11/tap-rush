package com.boltz.maliciousapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

public class GameView extends View {

    private static final long GAME_DURATION = 30_000;
    private static final float CIRCLE_RADIUS_DP = 32f;

    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final int[] colors = {
            0xFFE91E63, 0xFF2196F3, 0xFF4CAF50,
            0xFFFF9800, 0xFF9C27B0, 0xFFFF5722,
            0xFF00BCD4, 0xFFFFEB3B
    };

    private float cx, cy;
    private float radius;
    private int score;
    private boolean active;
    private CountDownTimer timer;
    private GameListener listener;

    public interface GameListener {
        void onScoreUpdate(int score);
        void onTick(long secondsLeft);
        void onFinish(int finalScore);
    }

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        radius = CIRCLE_RADIUS_DP * getResources().getDisplayMetrics().density;
        glowPaint.setStyle(Paint.Style.FILL);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setShadowLayer(radius * 0.3f, 0, 0, 0x40000000);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }

    public void startGame() {
        score = 0;
        active = true;
        if (listener != null) listener.onScoreUpdate(0);
        spawnCircle();

        if (timer != null) timer.cancel();
        timer = new CountDownTimer(GAME_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (listener != null) listener.onTick(millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                active = false;
                invalidate();
                if (listener != null) listener.onFinish(score);
            }
        };
        timer.start();
    }

    public void stopGame() {
        active = false;
        if (timer != null) timer.cancel();
        invalidate();
    }

    private void spawnCircle() {
        float padding = radius + 16;
        float w = getWidth();
        float h = getHeight();
        if (w <= padding * 2 || h <= padding * 2) return;

        cx = padding + random.nextFloat() * (w - 2 * padding);
        cy = padding + random.nextFloat() * (h - 2 * padding);

        int idx = random.nextInt(colors.length);
        circlePaint.setColor(colors[idx]);
        glowPaint.setColor(colors[idx]);
        glowPaint.setAlpha(50);

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (active) spawnCircle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!active) return;
        canvas.drawCircle(cx, cy, radius * 1.4f, glowPaint);
        canvas.drawCircle(cx, cy, radius, circlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!active || event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }

        float dx = event.getX() - cx;
        float dy = event.getY() - cy;
        float tapRadius = radius * 1.3f;
        if (dx * dx + dy * dy <= tapRadius * tapRadius) {
            score++;
            if (listener != null) listener.onScoreUpdate(score);
            spawnCircle();
            return true;
        }
        return true;
    }
}
