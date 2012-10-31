/**
 * 
 */
package com.draga.android.spaceTravels;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Draga
 *
 */
public class BlackHole {
    private static final float PHYS_ROTATION_SPEED = 10;// degrees per second
    private static final double PHYS_CAUGHT_DISTANCE = 0.4;// % of the radius
    private static final float PHYS_INNER_RING_ROTATION = 2.2f ;// ratio lost per every ring
    private static final int PHYS_RINGS = 10;// number of rings
	private static final double gravForce = 10;
	private Point position;
	private int size;
	private Drawable image;
	private float rotation = 0; // rotation in degrees
    private List<Float> rotationList;
	
    
	BlackHole (Context context, int x, int y, int _size){
		position = new Point(x,y);
		setSize(_size);
		image = context.getResources().getDrawable(
                R.drawable.blackhole);
		rotationList = new ArrayList<Float>();
		for (int i = 0; i < PHYS_RINGS; i++)
			rotationList.add(0f);
	}
	
	public void draw(Canvas canvas){
		double tmpSize = size;
        for(int i = 0; i < rotationList.size(); i++) {
	        canvas.save();
	        canvas.rotate(rotationList.get(i) , (float) position.x, (float) position.y);
	        int xLeft = (int) (position.x - tmpSize / 2);
	        int yTop = (int) (position.y - tmpSize / 2);
	        image.setBounds(xLeft, yTop, (int)(xLeft + tmpSize), (int) (yTop + tmpSize));
	        image.draw(canvas);   
	        canvas.restore();
	        
	        tmpSize -= size / PHYS_RINGS;
        }
	}
	
	public void update (double elapsed){
		float elapsedRotation = (float) (PHYS_ROTATION_SPEED * elapsed);
		
        for(int i = 0; i < rotationList.size(); i++){
        	rotation = rotationList.get(i) + elapsedRotation;
        	if (rotation > 360)
        		rotation -= 360;
        	rotationList.set(i, rotation);
        	elapsedRotation *= PHYS_INNER_RING_ROTATION;
        }
	}

	/**
	 * @return the gravforce
	 */
	public static double getGravforce() {
		return gravForce;
	}

	/**
	 * @return the x of the position
	 */
	public double getX() {
		return position.x;
	}

	/**
	 * @return the y of the position
	 */
	public double getY() {
		return position.y;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}
	
	public boolean isCaught(double x, double y){
		double distanceX = x - position.x,
				distanceY = y - position.y;
		if (Math.sqrt(distanceX * distanceX + distanceY * distanceY)
				< size / 2 * PHYS_CAUGHT_DISTANCE)
			return true;
		return false;
	}

}
