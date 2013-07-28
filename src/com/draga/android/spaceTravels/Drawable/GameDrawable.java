package com.draga.android.spaceTravels.Drawable;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.draga.android.spaceTravels.Vector2d;

/**
 * Created with doubleelliJ IDEA.
 * User: Draga86
 * Date: 27/07/13
 * Time: 18:25
 * To change this template use File | Settings | File Templates.
 */
public class GameDrawable {
    public double width,
            height,
            rotation;
    public Vector2d position;
    protected Drawable image;

    public GameDrawable(Vector2d position, Drawable image) {
        this.position = position;
        this.image = image;
    }

    public void draw(Canvas canvas, double density) {
        int xLeft = (int) (Math.round(position.x - width / 2) / density);
        int yTop = (int) (Math.round(position.y - height / 2) / density);
        image.setBounds(xLeft, yTop, (int) Math.round(xLeft + width), (int) Math.round(yTop + height));
        image.draw(canvas);
    }

    public double getSize() {
        return width;
    }

    public void setSize(double size) {
        width = height = size;
    }
}
