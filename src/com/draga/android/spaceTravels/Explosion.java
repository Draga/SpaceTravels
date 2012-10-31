package com.draga.android.spaceTravels;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import com.draga.android.spaceTravels.R;

public class Explosion {
	private final static double PHYS_EXPLOSION_SPEED = 1;
	private final static double PHYS_EXPLOSION_MIN_SIZE = 1;
	private final static double PHYS_EXPLOSION_MAX_SIZE = 50;
	private double size;
	private Point position;
	
    private Drawable image;
    
    Explosion(Context context, Point _position) {
	    image = context.getResources().getDrawable(
                R.drawable.explosion);
	    position = _position;
	    size = PHYS_EXPLOSION_MIN_SIZE;
    }
	
	void draw (Canvas canvas){
		int xLeft = (int) Math.round(position.x - size / 2);
		int yTop = (int) Math.round(position.y - size / 2);
        image.setBounds(xLeft, yTop, (int)Math.round(xLeft + size), (int)Math.round(yTop + size));
        image.draw(canvas);
        size += 1 * PHYS_EXPLOSION_SPEED;
	}
	
	void update(){
		//TODO code proper updates of size
	}
	
	public boolean isAnimating() {
		if(size >= PHYS_EXPLOSION_MAX_SIZE)
			return false;
		return true;
	}
}
