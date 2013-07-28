package com.draga.android.spaceTravels;

/**
 * Created with IntelliJ IDEA.
 * User: Draga86
 * Date: 27/07/13
 * Time: 19:19
 * To change this template use File | Settings | File Templates.
 */
public class TwoD {
    public double x,
            y;

    public TwoD(double x, double y) {
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

    public void add(TwoD twoD) {
        x += twoD.x;
        y += twoD.y;
    }
}
