/**
 *
 */
package com.draga.android.spaceTravels.GameDrawable;

import android.content.Context;
import android.graphics.Canvas;
import com.draga.android.spaceTravels.TwoD;
import com.draga.android.spaceTravels.R;

/**
 * @author Draga
 */
public class Ship extends GameDrawable {
    /*
     * Physics constants
     */
    public static final double PHYS_ACCEL_SEC = 2 * 9.8;
    public static final int PHYS_MAX_SPEED = 150;
    public static final double PHYS_PULL_FORCE = 2;
    public static final double PHYS_PULL_EXPONENTIAL = 1;
    public static final double PHYS_MIN_ACCEL = 0.1;
    public static final double SHIP_HEIGHT = 48;
    public static final double SHIP_WIDTH = 36;
    public static final double SHIP_TURN_SPEED = 0.1;
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
    private TwoD oldAccelleration;
    public double speed;

    public Ship(Context context, TwoD position) {
        super(position, context.getResources().getDrawable(
                R.drawable.spaceship));
        width = SHIP_WIDTH;
        height = SHIP_HEIGHT;
        maxSpeed = PHYS_MAX_SPEED;
        oldAccelleration = new TwoD(0,0);
    }

    public void update(TwoD TwoD, double elapsed) {
        speed = getSpeed(TwoD);
        super.Update(TwoD, elapsed);
        if (!isLanding) {
            // figure speeds for the end of the period
            TwoD acceleration = new TwoD(TwoD.x * elapsed, TwoD.y * elapsed);
            move(acceleration);


            // Calculate the rotation of the ship
            double distance = getSpeed(acceleration);
            acceleration.x *= (1 - SHIP_TURN_SPEED) * oldAccelleration.x + SHIP_TURN_SPEED;
            acceleration.y *= (1 - SHIP_TURN_SPEED) * oldAccelleration.y + SHIP_TURN_SPEED;
            double normalizedAccelX = -acceleration.x / distance;
            double normalizedAccelY = -acceleration.y / distance;
            // calculate the rotation base on the accelerometer
            rotation = Math.toDegrees(Math.atan2(normalizedAccelX * Math.PI,
                    normalizedAccelY * Math.PI));
            oldAccelleration.x = acceleration.x;
            oldAccelleration.y = acceleration.y;
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

    public void Draw(Canvas canvas, double density) {
        // draw the ship with its current rotation
        canvas.save();
        canvas.rotate((float)rotation, (float)position.x, (float)position.x);
        super.draw(canvas, density);
        canvas.restore();
    }

    public void setLanding(double _landingX, double _landingY) {
        isLanding = true;
        isAnimating = true;
        landingX = _landingX;
        landingY = _landingY;
        remainingLandingTime = GAME_LANDING_TIME;
    }
}
