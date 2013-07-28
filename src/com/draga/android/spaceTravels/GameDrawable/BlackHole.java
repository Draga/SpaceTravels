/**
 *
 */
package com.draga.android.spaceTravels.GameDrawable;

import android.content.Context;
import android.graphics.Canvas;
import com.draga.android.spaceTravels.R;
import com.draga.android.spaceTravels.TwoD;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Draga
 */
public class BlackHole extends GameDrawable {
    private static final int PHYS_BLACKHOLE_SIZE = 50;
    private static final int PHYS_BLACKHOLE_GRAV_FORCE = 50;
    private static final double PHYS_ROTATION_SPEED = 10;// degrees per second
    private static final double PHYS_CAUGHT_DISTANCE = 0.4;// % of the radius
    private static final double PHYS_INNER_RING_ROTATION = 2.2f;// ratio lost per every ring
    private static final int PHYS_RINGS = 10;// number of rings

    public double gravForce;
    private List<Double> rotationList;


    public BlackHole(TwoD position, Context context) {
        super(position, context.getResources().getDrawable(
                R.drawable.blackhole));

        gravForce = PHYS_BLACKHOLE_GRAV_FORCE;
        setSize(PHYS_BLACKHOLE_SIZE);
        rotationList = new ArrayList<Double>();
        for (int i = 0; i < PHYS_RINGS; i++)
            rotationList.add(0d);
    }

    public void draw(Canvas canvas, double density) {
        double tmpSize = getSize();
        for (int i = 0; i < rotationList.size(); i++) {
            canvas.save();
            canvas.rotate(rotationList.get(i).floatValue(), (float) position.x, (float) position.y);
            int xLeft = (int) (position.x - tmpSize / 2);
            int yTop = (int) (position.y - tmpSize / 2);
            image.setBounds(xLeft, yTop, (int) (xLeft + tmpSize), (int) (yTop + tmpSize));
            super.draw(canvas, density);
            canvas.restore();
            tmpSize -= getSize() / PHYS_RINGS;
        }
    }

    public void update(double elapsed) {
        double elapsedRotation = PHYS_ROTATION_SPEED * elapsed;

        for (int i = 0; i < rotationList.size(); i++) {
            rotation = rotationList.get(i) + elapsedRotation;
            if (rotation > 360)
                rotation -= 360;
            rotationList.set(i, rotation);
            elapsedRotation *= PHYS_INNER_RING_ROTATION;
        }
    }

    public boolean isCaught(TwoD position) {
        double distanceX = position.x - this.position.x,
                distanceY = position.y - this.position.y;
        if (Math.sqrt(distanceX * distanceX + distanceY * distanceY)
                < getSize() / 2 * PHYS_CAUGHT_DISTANCE)
            return true;
        return false;
    }

}
