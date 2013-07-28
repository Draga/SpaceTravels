package com.draga.android.spaceTravels.GameDrawable.Overlay;

import android.graphics.drawable.Drawable;
import com.draga.android.spaceTravels.GameDrawable.GameDrawable;
import com.draga.android.spaceTravels.TwoD;

/**
 * Created with IntelliJ IDEA.
 * User: Draga86
 * Date: 27/07/13
 * Time: 18:10
 * To change this template use File | Settings | File Templates.
 */
public class SpeedBar extends GameDrawable {
    public SpeedBar(TwoD position, Drawable image) {
        super(position, image);
    }
    // draw x speed
        /*rect.set(UI_BAR_SPACING + UI_BAR_HEIGHT, UI_BAR_SPACING,
                (double) (UI_BAR_SPACING + UI_BAR_HEIGHT + UI_BAR_WIDTH * (Math.abs(ship.getVelocityX()) / Ship.PHYS_SPEED_MAX)),
        		UI_BAR_SPACING + UI_BAR_HEIGHT);
        canvas.drawRect(rect, speedPaint);
        // draw y speed
        rect.set(UI_BAR_SPACING, UI_BAR_SPACING + UI_BAR_HEIGHT,
        		UI_BAR_SPACING + UI_BAR_HEIGHT,
        		(double) (UI_BAR_SPACING + UI_BAR_HEIGHT + UI_BAR_WIDTH * (Math.abs(ship.getVelocityY()) / Ship.PHYS_SPEED_MAX)));
        canvas.drawRect(rect, speedPaint);*/
}
