package com.draga.android.spaceTravels.Drawable.Animated;

import android.content.Context;
import com.draga.android.spaceTravels.Drawable.GameDrawable;
import com.draga.android.spaceTravels.R;
import com.draga.android.spaceTravels.Vector2d;

public class Explosion extends GameDrawable {
    private final static double PHYS_EXPLOSION_SPEED = 1;
    private final static double PHYS_EXPLOSION_MIN_SIZE = 1;
    private final static double PHYS_EXPLOSION_MAX_SIZE = 50;

    public Explosion(Context context, Vector2d position) {
        super(position, context.getResources().getDrawable(R.drawable.explosion));
        setSize(PHYS_EXPLOSION_MIN_SIZE);
    }

    public void update() {
        if (isAnimating()) {
            width += 1 * PHYS_EXPLOSION_SPEED;
            height += 1 * PHYS_EXPLOSION_SPEED;
        }
    }

    public boolean isAnimating() {
        if (getSize() >= PHYS_EXPLOSION_MAX_SIZE)
            return false;
        return true;
    }
}
