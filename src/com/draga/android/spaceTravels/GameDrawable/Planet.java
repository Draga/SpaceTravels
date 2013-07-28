package com.draga.android.spaceTravels.GameDrawable;

import android.graphics.drawable.Drawable;
import com.draga.android.spaceTravels.TwoD;

public class Planet extends GameDrawable{
    public double gravForce;
    public PlanetsName name;

    public static enum PlanetsName {
        Earth, Mars, Jupiter, Venus;
    }

    public Planet(TwoD position, Drawable image) {
        super(position, image);
    }

//    void draw(Canvas canvas) {
//        int xLeft = (int) Math.round(x - size / 2);
//        int yTop = (int) Math.round(y - size / 2);
//        getImage().setBounds(xLeft, yTop, (int) Math.round(xLeft + size), (int) Math.round(yTop + size));
//        getImage().draw(canvas);
//    }
}
