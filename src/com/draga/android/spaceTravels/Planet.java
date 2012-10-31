package com.draga.android.spaceTravels;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.draga.android.spaceTravels.R;

public class Planet {
	private int size;
	private Drawable image;
	private double gravForce;
	private PlanetsName name;
	private double x;
	private double y;
	public static enum PlanetsName {
		Earth, Mars, Jupiter, Venus;
	}
	
	/**
	 * @param size
	 * @param image
	 * @param gravForce
	 * @param name
	 * @param x
	 * @param y
	 */
	public Planet(/*int size, Drawable image, double gravForce, */PlanetsName name/*,
			double x, double y*/) {
		super();
		/*this.size = size;
		this.image = image;
		this.gravForce = gravForce;
		*/this.name = name;/*
		this.x = x;
		this.y = y;*/
	}
	
	void draw (Canvas canvas){
		int xLeft = (int) Math.round(x - size / 2);
		int yTop = (int) Math.round(y - size / 2);
        getImage().setBounds(xLeft, yTop, (int)Math.round(xLeft + size), (int)Math.round(yTop + size));
        getImage().draw(canvas);        
	}

	/**
	 * @return the size
	 */
	protected int getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the gravForce
	 */
	public double getGravForce() {
		return gravForce;
	}

	/**
	 * @param gravForce the gravForce to set
	 */
	public void setGravForce(double gravForce) {
		this.gravForce = gravForce;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return the image
	 */
	public Drawable getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(Drawable image) {
		this.image = image;
	}

	/**
	 * @return the name
	 */
	public PlanetsName getName() {
		return name;
	}
}
