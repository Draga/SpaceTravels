package com.draga.android.spaceTravels.Drawable;

import android.graphics.drawable.Drawable;
import com.draga.android.spaceTravels.Vector2d;

public class Planet extends GameDrawable {
    public double gravForce;
    public PlanetsName name;

    public static enum PlanetsName {
        Earth, Mars, Jupiter, Venus;
    }

    public Planet(Vector2d position, Drawable image) {
        super(position, image);
    }

//    void draw(Canvas canvas) {
//        int xLeft = (int) Math.round(x - size / 2);
//        int yTop = (int) Math.round(y - size / 2);
//        getImage().setBounds(xLeft, yTop, (int) Math.round(xLeft + size), (int) Math.round(yTop + size));
//        getImage().draw(canvas);
//    }
}
