package com.draga.android.spaceTravels.Drawable.Animated;

import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import com.draga.android.spaceTravels.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Draga86
 * Date: 28/07/13
 * Time: 22:30
 * To change this template use File | Settings | File Templates.
 */
public class Flame extends GameAnimated {
    public static final double OFFSET = 22;
    public static final double MAX_WIDTH = 100;
    public static final double MAX_HEIGHT = 100;
    public static final double MIN_RATIO = 0.05;
    AnimationDrawable animation;
    private double nextFrame;

    public Flame(Vector2d position, AnimationDrawable animation) {
        super(position, animation.getFrame(0));
        this.animation = animation;
        animation.start();
        nextFrame = animation.getDuration(0);
    }

    public void update(Ship ship, Vector2d accelerometer, double elapsed) {
        rotation = ship.rotation;
        rotation += 180;
        if (rotation > 360)
            rotation %= 360;
        position = ship.position;
        double ratio = accelerometer.toLinear() / 9.8 * 9.8;
        if (ratio < MIN_RATIO)
            ratio = MIN_RATIO;
        width = MAX_WIDTH * ratio;
        height = MAX_HEIGHT * ratio;
    }

    public void draw(Canvas canvas, double density) {
        canvas.save();
        canvas.rotate((float) rotation, (float) (position.x), (float) (position.y));
        int xLeft = (int) (Math.round(position.x - width / 2) / density);
        int yTop = (int) (Math.round(position.y - height - OFFSET / 2) / density);
        animation.setBounds(xLeft, yTop, (int) Math.round(xLeft + width), (int) Math.round(yTop + height));
        animation.draw(canvas);
        canvas.restore();
    }
}
