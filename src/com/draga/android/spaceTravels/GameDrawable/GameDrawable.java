package com.draga.android.spaceTravels.GameDrawable;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.draga.android.spaceTravels.TwoD;
import com.draga.android.spaceTravels.TwoD;

/**
 * Created with doubleelliJ IDEA.
 * User: Draga86
 * Date: 27/07/13
 * Time: 18:25
 * To change this template use File | Settings | File Templates.
 */
public class GameDrawable {
    public double relativeSpeed,
            width,
            height,
            rotation,
            maxSpeed;
    public TwoD position;
    protected Drawable image;

    public GameDrawable(TwoD position, Drawable image) {
        this.position = position;
        this.image = image;
        relativeSpeed = 1;
    }

    protected void Update(TwoD TwoD, double elapsed) {
        TwoD.multiply(relativeSpeed);

        double speed = getSpeed(TwoD) * elapsed;

        // check if reached maximum velocity
        if (maxSpeed != 0 && speed > maxSpeed) {
            double exceeded = speed / maxSpeed;
            TwoD.divide(exceeded);
        }

        position.add(TwoD);
    }

    public void draw(Canvas canvas, double density) {
        int xLeft = (int) (Math.round(position.x - width / 2) / density);
        int yTop = (int) (Math.round(position.y - height / 2) / density);
        image.setBounds(xLeft, yTop, (int) Math.round(xLeft + width), (int) Math.round(yTop + height));
        image.draw(canvas);
    }

    public void move(TwoD distance){
        position.x += distance.x;
        position.y += distance.y;
    }

    /**
     * @return the combined speed
     */
    public double getSpeed(TwoD TwoD) {
        return Math.sqrt(Math.pow(TwoD.x, 2) + Math.pow(TwoD.y, 2));
    }

    public double getSize() {
        return width;
    }

    public void setSize(double size) {
        width = height = size;
    }
}
