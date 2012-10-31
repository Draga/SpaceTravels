/**
 * 
 */
package com.draga.android.spaceTravels;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import com.draga.android.spaceTravels.R;

/**
 * @author Draga
 *
 */
public class Ship{
    /*
     * Physics constants
     */
    public static final double PHYS_ACCEL_SEC = 2 * 9.8;
    public static final int PHYS_SPEED_MAX = 150;
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
    private boolean isLanding = false,
    				isAnimating = false;
    private double landingX,
    				landingY;
    private double remainingLandingTime;

	private double velocityX,
		velocityY,
		x = 0,
		y = 0,
		width,
		height,
		oldAccelX = 0,
		oldAccelY = 0;
	private Drawable image;
	/**the rotation in degree 0° to 360° */
	private double rotation = 0;
    
	public Ship(Context context, double _x, double _y) {
		x = _x;
		y = _y;
		velocityX = 0;
		velocityY = 0;
		setImage(context.getResources().getDrawable(
                R.drawable.spaceship));
		width = SHIP_WIDTH;
		height = SHIP_HEIGHT;
	}
	
	public void update(double accelX, double accelY, double[] force,
			int sensorAccuracy, double elapsed){
        if( ! isLanding) {
	        // save the velocities for later calculation
			/*double oldVelocityX = this.velocityX;
			double oldVelocityY = this.velocityY;*/
	        // figure speeds for the end of the period
			velocityX -= (accelX * PHYS_ACCEL_SEC - force[0]) * elapsed;
			velocityY += (accelY * PHYS_ACCEL_SEC + force[1]) * elapsed;
			
			// check if reached maximum velocity
			if(getSpeed() > PHYS_SPEED_MAX) {
				velocityX /= getSpeed() / PHYS_SPEED_MAX;
				velocityY /= getSpeed() / PHYS_SPEED_MAX;
			}
	        
	        // Update the ship position
	        x += elapsed * velocityX;
	        y += elapsed * velocityY; 
	        
	        // Calculate the rotation of the ship
	        double distance = Math.sqrt(accelX * accelX + accelY * accelY);
			accelX = (1 - SHIP_TURN_SPEED ) * oldAccelX + SHIP_TURN_SPEED * accelX;
			accelY = (1 - SHIP_TURN_SPEED) * oldAccelY + SHIP_TURN_SPEED * accelY;
	        double normalizedAccelX = - accelX / distance;
	        double normalizedAccelY = - accelY / distance;
	        // calculate the rotation base on the accelerometer
	    	rotation = Math.toDegrees(Math.atan2(normalizedAccelX * Math.PI,
	    			normalizedAccelY * Math.PI));
	        oldAccelX = accelX;
	        oldAccelY = accelY;
        }
        else {
			remainingLandingTime -= elapsed;
			if (remainingLandingTime > 0) {
				width = SHIP_WIDTH * remainingLandingTime / GAME_LANDING_TIME;
				height = SHIP_HEIGHT * remainingLandingTime / GAME_LANDING_TIME;
				x += landingX * elapsed / GAME_LANDING_TIME;
				y += landingY * elapsed / GAME_LANDING_TIME;
			}
			else
				isAnimating = false;
		}
	}
	
	public void draw(Canvas canvas){
        // Draw the ship with its current rotation
        canvas.save();
        canvas.rotate((float) rotation, (float) this.getX(), (float) this.getY());		
        int xLeft = (int) Math.round(this.getX() - this.getWidth() / 2);
		int yTop = (int) Math.round(this.getY() - this.getHeight()/ 2);
        image.setBounds(xLeft, yTop, (int)Math.round(xLeft + this.getWidth()),
        		(int)Math.round(yTop + this.getHeight()));
        image.draw(canvas);
        canvas.restore();
	}

	public void setLanding(double _landingX, double _landingY) {
		isLanding = true;
		isAnimating = true;
	    landingX = _landingX;
		landingY = _landingY;
		remainingLandingTime = GAME_LANDING_TIME;
	}

	/**
	 * @return the velocityX
	 */
	public double getVelocityX() {
		return velocityX;
	}

	/**
	 * @param velocityX the velocityX to set
	 */
	public void setVelocityX(double velocityX) {
		this.velocityX = velocityX;
	}

	/**
	 * @return the velocityY
	 */
	public double getVelocityY() {
		return velocityY;
	}

	/**
	 * @param velocityY the velocityY to set
	 */
	public void setVelocityY(double velocityY) {
		this.velocityY = velocityY;
	}
	
    /**
	 * @return the isRunning
	 */
	/*public int getState() {
		return state;
	}*/

	/**
	 * @param isRunning the isRunning to set
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	 */

	/**
	 * @return the combined speed
	 */
	public double getSpeed() {
		return Math.sqrt(velocityX * velocityX + velocityY * velocityY);
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return the image
	 */
	public Drawable getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(Drawable image) {
		this.image = image;
	}

	/**
	 * @return the rotation
	 */
	public double getRotation() {
		return rotation;
	}

	/**
	 * @param rotation the rotation to set
	 */
	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	/**
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(double height) {
		this.height = height;
	}
	/**
	* @return the isAnimating
	*/
	public boolean isAnimating() {
	return isAnimating;
	}
}
