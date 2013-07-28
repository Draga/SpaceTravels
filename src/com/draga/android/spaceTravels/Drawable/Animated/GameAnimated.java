package com.draga.android.spaceTravels.Drawable.Animated;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.draga.android.spaceTravels.Drawable.GameDrawable;
import com.draga.android.spaceTravels.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Draga86
 * Date: 28/07/13
 * Time: 20:00
 * To change this template use File | Settings | File Templates.
 */
public class GameAnimated extends GameDrawable {
    public Vector2d acceleration;
    public double speedMultiplier = 1;
    public static double maxSpeed;

    public GameAnimated(Vector2d position, Drawable image) {
        super(position, image);
        acceleration = new Vector2d();
    }

    protected void update(Vector2d force, double elapsed) {
        force.multiply(elapsed * speedMultiplier);
        acceleration.add(force);
        acceleration.max(maxSpeed);

        position.add(acceleration);
    }

    public void draw(Canvas canvas, double density) {
        // draw the ship with its current rotation
        canvas.save();
        canvas.rotate((float) rotation, (float) position.x, (float) position.y);
        super.draw(canvas, density);
        canvas.restore();
    }

    public double getSpeed() {
        return acceleration.toLinear();
    }
}
