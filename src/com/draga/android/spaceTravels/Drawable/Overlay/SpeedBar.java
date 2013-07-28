package com.draga.android.spaceTravels.Drawable.Overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.draga.android.spaceTravels.Drawable.Animated.Ship;

/**
 * Created with IntelliJ IDEA.
 * User: Draga86
 * Date: 27/07/13
 * Time: 18:10
 * To change this template use File | Settings | File Templates.
 */
public class SpeedBar {
    private static final int UI_BAR_HEIGHT = 10;
    private static final int UI_BAR_WIDTH = 70;
    private static final int UI_BAR_SPACING = 10;
    private static final int UI_BAR_PADDING = 1;
    private final Paint speedPaint,
            backgroundPaint;
    private RectF rect;

    public SpeedBar() {
        speedPaint = new Paint();
        speedPaint.setAntiAlias(true);
        speedPaint.setARGB(255, 0, 255, 0);

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setARGB(255, 255, 255, 255);

        rect = new RectF(255, 0, 255, 0);
    }

    public void draw(Ship ship, Canvas canvas, double density) {
        rect.set(UI_BAR_SPACING + UI_BAR_HEIGHT,
                UI_BAR_SPACING,
                UI_BAR_SPACING + UI_BAR_HEIGHT + UI_BAR_WIDTH,
                UI_BAR_SPACING + UI_BAR_HEIGHT);
        canvas.drawRect(rect, backgroundPaint);
        double shipSpeed = ship.getSpeed();
        double shipSpeedOnMax = shipSpeed / Ship.maxSpeed;
        speedPaint.setARGB(255, (int) Math.round(255 * shipSpeedOnMax), (int) Math.round(255 * (1 - shipSpeedOnMax)), 0);
        rect.set(UI_BAR_SPACING + UI_BAR_HEIGHT + UI_BAR_PADDING,
                UI_BAR_SPACING + UI_BAR_PADDING,
                (float) (UI_BAR_SPACING + UI_BAR_HEIGHT + UI_BAR_WIDTH * shipSpeedOnMax) - UI_BAR_PADDING,
                UI_BAR_SPACING + UI_BAR_HEIGHT - UI_BAR_PADDING);
        canvas.drawRect(rect, speedPaint);
    }
}
