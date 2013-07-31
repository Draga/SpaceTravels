/**
 *
 */
package com.draga.android.spaceTravels.Drawable.Animated;

import android.content.Context;
import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

import com.draga.android.spaceTravels.R;
import com.draga.android.spaceTravels.Vector2d;
import com.draga.android.spaceTravels.Drawable.GameDrawable;

/**
 * @author Draga
 */
public class BlackHole extends GameDrawable {
	private static final int PHYS_BLACKHOLE_SIZE = 50;
	private static final int PHYS_BLACKHOLE_GRAV_FORCE = 50;
	private static final double PHYS_ROTATION_SPEED = 0.01;// degrees per second
	private static final double PHYS_CAUGHT_DISTANCE = 0.4;// % of the radius
	private static final double PHYS_INNER_RING_ROTATION = 10;// ratio lost
																// per every
																// ring
	private static final int PHYS_RINGS = 10;// number of rings

	public double gravForce;
	private List<Double> rotationList;

	public BlackHole(Vector2d position, Context context) {
		super(position, context.getResources()
				.getDrawable(R.drawable.blackhole));

		gravForce = PHYS_BLACKHOLE_GRAV_FORCE;
		// setSize(PHYS_BLACKHOLE_SIZE);
		rotationList = new ArrayList<Double>();
		for (double i = 0; i < PHYS_RINGS; i++)
			rotationList.add(i);
	}

	public void draw(Canvas canvas, double density) {
		setSize(PHYS_BLACKHOLE_SIZE);
		for (int i = 0; i < rotationList.size(); i++) {
			canvas.save();
			float rotation = rotationList.get(i).floatValue();
			canvas.rotate(rotation, (float) position.x, (float) position.y);
			super.draw(canvas, density);
			canvas.restore();
			setSize(getSize() - getSize() / PHYS_RINGS);
		}
	}

	public void update(double elapsed) {
		double elapsedRotation = PHYS_ROTATION_SPEED * elapsed;

		for (int i = 0; i < rotationList.size(); i++) {
			rotation = rotationList.get(i) + elapsedRotation;
			if (rotation > 360)
				rotation %= 360;
			rotationList.set(i, rotation);
			elapsedRotation *= PHYS_INNER_RING_ROTATION;
		}
	}

	public boolean isCaught(Vector2d position) {
		double distanceX = position.x - this.position.x, distanceY = position.y
				- this.position.y;
		if (Math.sqrt(distanceX * distanceX + distanceY * distanceY) < getSize()
				/ 2 * PHYS_CAUGHT_DISTANCE)
			return true;
		return false;
	}

}
