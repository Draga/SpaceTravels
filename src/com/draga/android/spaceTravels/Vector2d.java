package com.draga.android.spaceTravels;

/**
 * Created with IntelliJ IDEA. User: Draga86 Date: 27/07/13 Time: 19:19 To
 * change this template use File | Settings | File Templates.
 */
public class Vector2d {
	public double x, y;

	public Vector2d() {
		this(0, 0);
	}

	public Vector2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void multiply(double relativeSpeed) {
		x *= relativeSpeed;
		y *= relativeSpeed;
	}

	public void divide(double relativeSpeed) {
		x /= relativeSpeed;
		y /= relativeSpeed;
	}

	public void add(Vector2d vector2d) {
		x += vector2d.x;
		y += vector2d.y;
	}

	public double toLinear() {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}

	public Vector2d copy() {
		return new Vector2d(x, y);
	}

	public void subtract(Vector2d vector2d) {
		x -= vector2d.x;
		y -= vector2d.y;
	}

	public void abs() {
		x = Math.abs(x);
		y = Math.abs(y);
	}

	public void max(double maxSpeed) {
		double linearSpeed = toLinear();
		// check if reached maximum velocity
		if (maxSpeed != 0 && linearSpeed > maxSpeed) {
			double exceeded = linearSpeed / maxSpeed;
			divide(exceeded);
		}
	}

	public void set(Vector2d vector2d) {
		x = vector2d.x;
		y = vector2d.y;
	}

	public void normalize() {
		double distance = Math.abs(toLinear());
		x /= distance;
		y /= distance;
	}

	public double getDegrees() {
		double degrees = Math.toDegrees(Math.atan2(x * Math.PI, y * Math.PI));
		if (degrees > 360)
			degrees %= 360;
		else if (degrees < 0)
			degrees += 360;
		return degrees;
	}
}
