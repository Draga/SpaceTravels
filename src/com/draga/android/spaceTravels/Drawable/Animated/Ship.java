/**
 *
 */
package com.draga.android.spaceTravels.Drawable.Animated;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

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
	public static final int PHYS_MAX_SPEED = 5;
	public static final double PHYS_PULL_FORCE = 2;
	public static final double PHYS_PULL_EXPONENTIAL = 1;
	public static final double PHYS_MIN_ACCEL = 0.1;
	public static final double SHIP_HEIGHT = 48;
	public static final double SHIP_WIDTH = 36;
	public static final double TURN_DEGREES_PER_MS = 1;
	public static final double PHYS_GRAVITATIONAL_CONSTANT = 0.000001;
	private static final double PHY_ACCELEROMETER_CONSTANT = 0.0001;
	/*
	 * Game constants
	 */
	public static final double GAME_LANDING_TIME = 3; // In seconds
	/*
	 * Landing animation variables
	 */
	public boolean isLanding = false, isAnimating = false;
	private double landingX, landingY, remainingLandingTime;
	private Flame flame;

	public Ship(Context context, Vector2d position) {
		super(position, context.getResources()
				.getDrawable(R.drawable.spaceship));
		width = SHIP_WIDTH;
		height = SHIP_HEIGHT;
		speedMultiplier = PHYS_ACCEL_SEC;
		maxSpeed = PHYS_MAX_SPEED;
		// Load the ImageView that will host the animation and
		// set its background to our AnimationDrawable XML resource.
		ArrayList<Drawable> animation = new ArrayList<Drawable>();
		animation.add(context.getResources().getDrawable(R.drawable.flame01));
		animation.add(context.getResources().getDrawable(R.drawable.flame02));
		animation.add(context.getResources().getDrawable(R.drawable.flame03));
		animation.add(context.getResources().getDrawable(R.drawable.flame04));
		animation.add(context.getResources().getDrawable(R.drawable.flame05));
		animation.add(context.getResources().getDrawable(R.drawable.flame06));
		animation.add(context.getResources().getDrawable(R.drawable.flame07));
		animation.add(context.getResources().getDrawable(R.drawable.flame08));
		animation.add(context.getResources().getDrawable(R.drawable.flame09));
		animation.add(context.getResources().getDrawable(R.drawable.flame10));
		animation.add(context.getResources().getDrawable(R.drawable.flame11));
		animation.add(context.getResources().getDrawable(R.drawable.flame12));
		animation.add(context.getResources().getDrawable(R.drawable.flame13));
		flame = new Flame(position, animation);
	}

	public void update(Vector2d acceleration, Vector2d accelerometer,
			double elapsed) {

		if (!isLanding) {
			Vector2d totalAcceleration = acceleration.copy();
			totalAcceleration.multiply(PHYS_GRAVITATIONAL_CONSTANT);
			Vector2d tmp = accelerometer.copy();
			tmp.multiply(PHY_ACCELEROMETER_CONSTANT);
			totalAcceleration.add(tmp);

			super.update(totalAcceleration, elapsed);
			Vector2d thrust = new Vector2d(accelerometer.x, -accelerometer.y);
			flame.update(this, accelerometer, elapsed);

			double thrustRotation = thrust.getDegrees();
			double diffRotation = thrustRotation - rotation;
			// Avoid ship turning 360 when rotation close to 0 degrees
			if (diffRotation < -180)
				diffRotation += 360;
			else if (diffRotation > 180)
				diffRotation -= 360;
			// bring the rotation to the max if it's over it
			double maxTurn = TURN_DEGREES_PER_MS * elapsed
					* (Math.min(9.8, accelerometer.toLinear()) / 9.8);
			if (Math.abs(diffRotation) > maxTurn) {
				diffRotation = diffRotation > 0 ? maxTurn : -maxTurn;
			}
			// Apply rotation
			rotation += diffRotation;
			// brings the rotation between 0 and 360 degrees
			if (rotation > 360)
				rotation %= 360;
			else if (rotation < 0)
				rotation += 360;
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
