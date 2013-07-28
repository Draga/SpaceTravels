/**
 *
 */
package com.draga.android.spaceTravels.Drawable.Animated;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import com.draga.android.spaceTravels.R;
import com.draga.android.spaceTravels.Vector2d;

/**
 * @author Draga
 */
public class Ship extends GameAnimated {
    /*
     * Physics constants
     */
    public static final double PHYS_ACCEL_SEC = 15;
    public static final int PHYS_MAX_SPEED = 3;
    public static final double PHYS_PULL_FORCE = 2;
    public static final double PHYS_PULL_EXPONENTIAL = 1;
    public static final double PHYS_MIN_ACCEL = 0.1;
    public static final double SHIP_HEIGHT = 48;
    public static final double SHIP_WIDTH = 36;
    public static final double SHIP_TURN_SPEED = 0.5;
    /*
     * Game constants
     */
    public static final double GAME_LANDING_TIME = 3; // In seconds
    /*
     * Landing animation variables
     */
    public boolean isLanding = false,
            isAnimating = false;
    private double landingX,
            landingY,
            remainingLandingTime;
    private Flame flame;

    public Ship(Context context, Vector2d position) {
        super(position, context.getResources().getDrawable(
                R.drawable.spaceship));
        width = SHIP_WIDTH;
        height = SHIP_HEIGHT;
        speedMultiplier = PHYS_ACCEL_SEC;
        maxSpeed = PHYS_MAX_SPEED;
        flame = new Flame(position, (AnimationDrawable) context.getResources().getDrawable(R.drawable.flame));
    }

    public void update(Vector2d acceleration, Vector2d accelerometer, double elapsed) {
        flame.update(this, accelerometer, elapsed);

        if (!isLanding) {
            Vector2d totalAcceleration = acceleration.copy();
            totalAcceleration.add(accelerometer);

            super.update(totalAcceleration, elapsed);

            // Calculate the rotation of the ship
//            Vector2d temp = new Vector2d((1 - SHIP_TURN_SPEED) * rotation.x + SHIP_TURN_SPEED * accelerometer.x,
//                    -((1 - SHIP_TURN_SPEED) * rotation.y + SHIP_TURN_SPEED * accelerometer.y));
//            temp.normalize();
            // calculate the rotation base on the accelerometer
//            Math.toDegrees(Math.atan2(temp.x * Math.PI, temp.y * Math.PI))
            rotation = Math.toDegrees(Math.atan2(accelerometer.x * Math.PI, -accelerometer.y * Math.PI));
        } else {
            remainingLandingTime -= elapsed;
            if (remainingLandingTime > 0) {
                width = SHIP_WIDTH * remainingLandingTime / GAME_LANDING_TIME;
                height = SHIP_HEIGHT * remainingLandingTime / GAME_LANDING_TIME;
                position.x += landingX * elapsed / GAME_LANDING_TIME;
                position.y += landingY * elapsed / GAME_LANDING_TIME;
            } else
                isAnimating = false;
        }
    }

    public void draw(Canvas canvas, double density) {
        super.draw(canvas, density);
        flame.draw(canvas, density);
    }

    public void setLanding(double _landingX, double _landingY) {
        isLanding = true;
        isAnimating = true;
        landingX = _landingX;
        landingY = _landingY;
        remainingLandingTime = GAME_LANDING_TIME;
    }
}
