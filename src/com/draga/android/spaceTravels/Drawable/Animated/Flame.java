package com.draga.android.spaceTravels.Drawable.Animated;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.util.Iterator;
import java.util.List;

import com.draga.android.spaceTravels.Vector2d;

/**
 * Created with IntelliJ IDEA. User: Draga86 Date: 28/07/13 Time: 22:30 To
 * change this template use File | Settings | File Templates.
 */
public class Flame extends GameAnimated {
	/**
	 * 
	 */
	private static final double GRAVITY = 9.8;
	public static final double OFFSET = 12;
	public static final double MAX_WIDTH = 60;
	public static final double MAX_HEIGHT = 70;
	public static final double MIN_RATIO = 0.2;
	public static final double FrameDuration = 20;
	private double nextFrame;
	private List<Drawable> animation;
	private Iterator<Drawable> iterator;

	public Flame(Vector2d position, List<Drawable> animation) {
		super(position, animation.get(0));
		nextFrame = FrameDuration;
		this.animation = animation;
		iterator = animation.iterator();
		this.image = iterator.next();
	}

	public void update(Ship ship, Vector2d accelerometer, double elapsed) {
		position = ship.position;
		rotation = ship.rotation;
		// if (rotation > 360)
		// rotation %= 360;
		double ratio = accelerometer.toLinear() / GRAVITY;
		if (ratio < MIN_RATIO)
			ratio = MIN_RATIO;
		else if (ratio > 1)
			ratio = 1;
		width = MAX_WIDTH * ratio;
		height = MAX_HEIGHT * ratio;

		// updates the animation
		nextFrame -= elapsed;
		if (nextFrame <= 0) {
			nextFrame += FrameDuration;
			if (!iterator.hasNext()) {
				iterator = animation.iterator();
			}
			image = iterator.next();
		}
	}

	public void draw(Canvas canvas, double density) {
		canvas.save();
		canvas.rotate((float) rotation, (float) (position.x),
				(float) (position.y));
		int xLeft = (int) (Math.round(position.x - width / 2) / density);
		int yTop = (int) (Math.round(position.y + OFFSET) / density);
		image.setBounds(xLeft, yTop, (int) Math.round(xLeft + width),
				(int) Math.round(yTop + height));
		image.draw(canvas);
		canvas.restore();
	}
}
