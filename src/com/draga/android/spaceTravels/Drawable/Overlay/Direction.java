package com.draga.android.spaceTravels.Drawable.Overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import com.draga.android.spaceTravels.Drawable.Animated.Ship;
import com.draga.android.spaceTravels.Drawable.GameDrawable;
import com.draga.android.spaceTravels.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Draga86
 * Date: 27/07/13
 * Time: 19:58
 * To change this template use File | Settings | File Templates.
 */
public class Direction extends GameDrawable {
    private static final int UI_BAR_SPACING = 10;
    private static final int UI_MARGIN = 10;
    private static final int UI_DIRECTION_SIZE = 50;
    private static final int UI_BAR_HEIGHT = 10;
    private final Path absoluteArrowPath;

    private Path relativeArrowPath;
    private Paint relativeArrowPaint;
    private Point trianglePoint1 = new Point();
    private Point trianglePoint2 = new Point();
    private Point trianglePoint3 = new Point();
    private Point trianglePoint4 = new Point();
    private Paint directionPaint;

    public Direction(Vector2d position, Drawable image) {
        super(position, image);
        // Paint and Points to draw an arrow in the direction of the ship
        directionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        directionPaint.setStrokeWidth(2);
        directionPaint.setColor(android.graphics.Color.WHITE);
        directionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        directionPaint.setAntiAlias(true);
        // set the points of the triangle
        trianglePoint1 = new Point(UI_MARGIN,
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

        // fill the path with the points
        absoluteArrowPath = new Path();
        absoluteArrowPath.setFillType(Path.FillType.EVEN_ODD);
        absoluteArrowPath.moveTo(trianglePoint1.x, trianglePoint1.y);
        absoluteArrowPath.lineTo(trianglePoint2.x, trianglePoint2.y);
        absoluteArrowPath.lineTo(trianglePoint3.x, trianglePoint3.y);
        absoluteArrowPath.lineTo(trianglePoint4.x, trianglePoint4.y);
        absoluteArrowPath.lineTo(trianglePoint1.x, trianglePoint1.y);
        absoluteArrowPath.close();
    }

    public void draw(Ship ship, Canvas canvas, double density) {
        double shipSpeedRatio = ship.acceleration.toLinear() / Ship.PHYS_MAX_SPEED;
        directionPaint.setARGB(255,
                (int) (255 * shipSpeedRatio),
                (int) (255 * (1 - shipSpeedRatio)),
                0);

        //determine the rotation to make it point towards the ship
        double shipVelocityX = ship.acceleration.x;
        double shipVelocityY = ship.acceleration.y;
        double combinedVelocity = Math.sqrt(shipVelocityX * shipVelocityX
                + shipVelocityY * shipVelocityY);
        double normalizedshipVelocityX = shipVelocityX / combinedVelocity;
        double normalizedshipVelocityY = -shipVelocityY / combinedVelocity;
        double directionRotation = Math.toDegrees(Math.atan2(normalizedshipVelocityX * Math.PI,
                normalizedshipVelocityY * Math.PI));
        // draw the triangle with its current rotation
        canvas.save();
        canvas.rotate((float) directionRotation, UI_MARGIN, UI_MARGIN);
        canvas.drawPath(absoluteArrowPath, directionPaint);
        canvas.restore();
    }
}
