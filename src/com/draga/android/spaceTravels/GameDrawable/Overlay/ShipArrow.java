package com.draga.android.spaceTravels.GameDrawable.Overlay;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import com.draga.android.spaceTravels.GameDrawable.GameDrawable;
import com.draga.android.spaceTravels.TwoD;

/**
 * Created with IntelliJ IDEA.
 * User: Draga86
 * Date: 27/07/13
 * Time: 19:58
 * To change this template use File | Settings | File Templates.
 */
public class ShipArrow extends GameDrawable {
    private static final int UI_BAR_SPACING = 10;
    private static final int UI_ABSOLUTE_X = 10;
    private static final int UI_DIRECTION_SIZE = 50;
    private static final int UI_BAR_HEIGHT = 10;

    private Path relativeArrowPath;
    private Paint relativeArrowPaint;
    private Point trianglePoint1 = new Point();
    private Point trianglePoint2 = new Point();
    private Point trianglePoint3 = new Point();
    private Point trianglePoint4 = new Point();
    private Paint directionPaint;

    public ShipArrow(TwoD position, Drawable image) {
        super(position, image);
        // Paint and Points to draw an arrow in the direction of the ship
        directionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        directionPaint.setStrokeWidth(2);
        directionPaint.setColor(android.graphics.Color.WHITE);
        directionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        directionPaint.setAntiAlias(true);
        // set the points of the triangle
        trianglePoint1 = new Point(UI_ABSOLUTE_X,
                (int) (UI_BAR_SPACING + UI_BAR_SPACING * (1 - UI_DIRECTION_SIZE)));
        trianglePoint2 = new Point((int) (UI_BAR_SPACING + UI_BAR_HEIGHT * UI_DIRECTION_SIZE),
                (int) (UI_BAR_SPACING + UI_BAR_HEIGHT * UI_DIRECTION_SIZE));
        trianglePoint3 = new Point(UI_BAR_SPACING + UI_BAR_HEIGHT / 2,
                UI_BAR_SPACING + UI_BAR_HEIGHT / 2);
        trianglePoint4 = new Point((int) (UI_BAR_SPACING + UI_BAR_HEIGHT * (1 - UI_DIRECTION_SIZE)),
                (int) (UI_BAR_SPACING + UI_BAR_HEIGHT * UI_DIRECTION_SIZE));
        relativeArrowPath = new Path();

        relativeArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        relativeArrowPaint.setStrokeWidth(2);
        relativeArrowPaint.setColor(android.graphics.Color.WHITE);
        relativeArrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        relativeArrowPaint.setAntiAlias(true);
    }
}
