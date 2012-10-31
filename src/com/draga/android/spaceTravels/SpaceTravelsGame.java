package com.draga.android.spaceTravels;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.draga.android.spaceTravels.Planet.PlanetsName;

public class SpaceTravelsGame extends SurfaceView implements SurfaceHolder.Callback {
    /*
     * Game constants
     */
    public static final int GAME_LOST_DISTANCE = 500; // distance of the bar from the corner
    public static final int GAME_MAX_LANDING_SPEED = 50; // distance of the bar from the corner
	private static final double GAME_PLANET_SCALE = 3;
    /*
     * Game Physics constants
     */
    public static final double PHYS_GRAVITATIONAL_CONSTANTS = 2000;
    public static final int PHYS_BLACKHOLE_SIZE = 50;

    /*
     * UI constants (i.e. the speed & fuel bars)
     */
    public static final int UI_BAR_SPACING = 5; // distance of the bar from the corner
    public static final int UI_BAR = 100; // width of the bar(s)
    public static final int UI_BAR_HEIGHT = 10; // height of the bar(s)
    public static final int UI_BAR_WIDTH = 100; // height of the bar(s)
    public static final double UI_DIRECTION_SIZE = 0.85; // from 0 to 1
    public static final int UI_RELATIVE_ARROW_WIDTH = 6;
    public static final int UI_RELATIVE_ARROW_HEIGHT = 10;
    public static final int UI_ABSOLUTE_X = UI_BAR_SPACING + UI_BAR_HEIGHT / 2;
    public static final int UI_ABSOLUTE_Y = UI_ABSOLUTE_X;

    private static List<Planet> planets;
    private static Iterator<Planet> planetIterator;
    
    private static WindowManager windowManager;
    private Display display;
    /** this is the particle system*/
	private Ship ship;
	private BlackHole blackHole;
	private Context context;
    /*
     * Canvas and variables
     */
    int canvasHeight = 1;
    int canvasWidth = 1;
    
    double [] force;
	/** the resource */
	private Resources resources;
    /** The drawable to use as the background of the animation canvas */
    private Bitmap backgroundImage;

    /** Pointer to the text view to display "Paused.." etc. */
    private TextView statusText;

    /** The thread that actually draws the animation */
    private SpaceTravelsThread thread;
    /*
     * Draw variables
     */
    private Paint speedPaint;
    private Path relativeArrowPath;
    private Path absoluteArrowPath;
    //private RectF rect;
    private Paint directionPaint;
    private Paint relativeArrowPaint;
    private Point trianglePoint1 = new Point();        
    private Point trianglePoint2 = new Point();    
    private Point trianglePoint3 = new Point();
    private Point trianglePoint4 = new Point();
    private Point collisionPoint;
    
    private Explosion explosion;

    public SpaceTravelsGame(Context _context, AttributeSet attrs) {
        super(_context, attrs);
        context = _context;
        
        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        
        // create thread only; it's started in surfaceCreated()
        thread = new SpaceTravelsThread(holder, this, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                statusText.setVisibility(m.getData().getInt("viz"));
                statusText.setText(m.getData().getString("text"));
            }
        });
        // Make the game focusable so it can handle events
        setFocusable(true);
		// Get an instance of the WindowManager
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        canvasWidth = displayMetrics.widthPixels;
        canvasHeight = displayMetrics.heightPixels;
        
        resources = context.getResources();

        // load background image as a Bitmap instead of a Drawable b/c
        // we don't need to transform it and it's faster to draw this way
        setBackgroundImage(BitmapFactory.decodeResource(resources,
                R.drawable.background));

        speedPaint = new Paint();
        speedPaint.setAntiAlias(true);
        speedPaint.setARGB(255, 0, 255, 0);

        //rect = new RectF(255, 0, 255, 0);
        
        // Paint and Points to draw an arrow in the direction of the ship
        directionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        directionPaint.setStrokeWidth(2);
        directionPaint.setColor(android.graphics.Color.WHITE);     
        directionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        directionPaint.setAntiAlias(true);
        // set the points of the triangle
    	trianglePoint1 = new Point(UI_ABSOLUTE_X,
    			(int) (UI_BAR_SPACING + UI_BAR_SPACING * (1 - UI_DIRECTION_SIZE)));
    	trianglePoint2 = new Point((int) (UI_BAR_SPACING + UI_BAR_HEIGHT * UI_DIRECTION_SIZE),
    			(int) (UI_BAR_SPACING + UI_BAR_HEIGHT * UI_DIRECTION_SIZE));
    	trianglePoint3 = new Point(UI_BAR_SPACING + UI_BAR_HEIGHT / 2,
    			UI_BAR_SPACING + UI_BAR_HEIGHT / 2);
    	trianglePoint4 = new Point((int) (UI_BAR_SPACING + UI_BAR_HEIGHT * (1 - UI_DIRECTION_SIZE)),
    			(int) (UI_BAR_SPACING + UI_BAR_HEIGHT * UI_DIRECTION_SIZE));
    	// fill the path with the points
    	absoluteArrowPath = new Path();
    	absoluteArrowPath.setFillType(Path.FillType.EVEN_ODD);
        absoluteArrowPath.moveTo(trianglePoint1.x,trianglePoint1.y);
        absoluteArrowPath.lineTo(trianglePoint2.x,trianglePoint2.y);
        absoluteArrowPath.lineTo(trianglePoint3.x,trianglePoint3.y);
        absoluteArrowPath.lineTo(trianglePoint4.x,trianglePoint4.y);
        absoluteArrowPath.lineTo(trianglePoint1.x,trianglePoint1.y);
        absoluteArrowPath.close();

    	relativeArrowPath = new Path();
        
        relativeArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        relativeArrowPaint.setStrokeWidth(2);
        relativeArrowPaint.setColor(android.graphics.Color.WHITE);     
        relativeArrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        relativeArrowPaint.setAntiAlias(true);
    }
    
    void init(){
    	Planet planet;
		planets = new ArrayList<Planet>();
	    for (PlanetsName planetName : Planet.PlanetsName.values()) {
			planet = new Planet(planetName);
			switch (planetName){
			case Earth :
				planet.setImage(context.getResources().getDrawable(
		                R.drawable.earth));
				planet.setGravForce(1);
				planet.setSize((int) (12.756 * GAME_PLANET_SCALE));
				planet.setX(canvasWidth * 0.97 - planet.getSize() / 2);
				planet.setY(canvasHeight * 0.5);
				break;
			case Mars :
				planet.setImage(context.getResources().getDrawable(
		                R.drawable.mars));
				planet.setGravForce(0.38);
				planet.setSize((int) (6.787 * GAME_PLANET_SCALE));
				planet.setX(canvasWidth * 0.4);
				planet.setY(canvasHeight * 0.85);
				break;
			case Jupiter :
				planet.setImage(context.getResources().getDrawable(
		                R.drawable.jupiter));
				planet.setGravForce(2.54);
//				this.size = (int) (142.800 * GAME_PLANET_SCALE);
				planet.setSize((int) (30.800 * GAME_PLANET_SCALE));
				planet.setX(canvasWidth * 0.55);
				planet.setY(canvasHeight * 0.35);
				break;
			case Venus :
				planet.setImage(context.getResources().getDrawable(
		                R.drawable.venus));
				planet.setGravForce(0.91);
				planet.setSize((int) (12.104 * GAME_PLANET_SCALE));
				planet.setX(canvasWidth * 0.7);
				planet.setY(canvasHeight * 0.7);
				break;
			default :
				break;
			}
			planets.add(planet);
	    }
        ship = new Ship(context, 0, canvasHeight * 0.5);
        explosion = null;
        blackHole = new BlackHole(context, (int) (canvasWidth * 0.4),
        						(int) Math.round(canvasHeight * 0.6), PHYS_BLACKHOLE_SIZE);
    }

    /**
     * Figures the Ball state (x, y, fuel, ...) based on the passage of
     * realtime. Does not invalidate(). Called at the start of draw().
     * Detects the end-of-game and sets the UI to the next state.
     * @param elapsed 
     */
    void update(int mode, double elapsed) {
		if(mode == SpaceTravelsThread.STATE_RUNNING) {
			force = getForce();
			ship.update(((SpaceTravelsThread) thread).getSensorX(),
				((SpaceTravelsThread) thread).getSensorY(),
				force,
				((SpaceTravelsThread) thread).getSensorAccuracy(),
				elapsed);
			// wheater the ship has been lost
			if(shipOutScreen() > 1){
				((SpaceTravelsThread) thread).setState(SpaceTravelsThread.STATE_LOSE, "You have lost the ship!");
			}
			// wheater the earth has crashed into another planet
			// TODO: fix collision detection with width and height
			planetIterator = planets.iterator();
			while(planetIterator.hasNext()) {
				Planet planet = planetIterator.next();
				collisionPoint = isShipCollided(planet);
				if(collisionPoint != null){
					explosion = new Explosion(context, collisionPoint);
				}
			}
			if(isShipCaught(blackHole)) {
				ship.setLanding(blackHole.getX() - ship.getX(), blackHole.getY() - ship.getY());
				thread.setState(SpaceTravelsThread.STATE_LOSE, "You have been caught by a black hole!");
			}
		}
		if(explosion != null && explosion.isAnimating()){
			explosion.update();
		}
		if(ship.isAnimating())
			ship.update(((SpaceTravelsThread) thread).getSensorX(),
					((SpaceTravelsThread) thread).getSensorY(),
					force,
					((SpaceTravelsThread) thread).getSensorAccuracy(),
					elapsed);
    	blackHole.update(elapsed);
    }

    /**
     * Draws the ship, planets, gauges, arrows and explosion
     */
    void doDraw(Canvas canvas) {
        // Draw the background image. Operations on the Canvas accumulate
        // so this is like clearing the screen.
        canvas.drawBitmap(getBackgroundImage(), 0, 0, null);
        
		planetIterator = planets.iterator();
		while(planetIterator.hasNext()) {
			planetIterator.next().draw(canvas);
		}
		blackHole.draw(canvas);
        ship.draw(canvas);
		if(explosion != null && explosion.isAnimating())
			explosion.draw(canvas);
        // draw x speed
        /*rect.set(UI_BAR_SPACING + UI_BAR_HEIGHT, UI_BAR_SPACING,
        		(float) (UI_BAR_SPACING + UI_BAR_HEIGHT + UI_BAR_WIDTH * (Math.abs(ship.getVelocityX()) / Ship.PHYS_SPEED_MAX)),
        		UI_BAR_SPACING + UI_BAR_HEIGHT);
        canvas.drawRect(rect, speedPaint);
        // draw y speed
        rect.set(UI_BAR_SPACING, UI_BAR_SPACING + UI_BAR_HEIGHT,
        		UI_BAR_SPACING + UI_BAR_HEIGHT,
        		(float) (UI_BAR_SPACING + UI_BAR_HEIGHT + UI_BAR_WIDTH * (Math.abs(ship.getVelocityY()) / Ship.PHYS_SPEED_MAX)));
        canvas.drawRect(rect, speedPaint);*/
        
        /*
         * Draw a white triangle pointing to the direction the ship is going
         */        
        // set the color, the faster the reddish, the slower the greenish
        double shipSpeedRatio = ship.getSpeed() / Ship.PHYS_SPEED_MAX;
        directionPaint.setARGB(255,
        		(int) (255 * shipSpeedRatio),
        		(int) (255 * (1 - shipSpeedRatio)),
        		0);
        
        //determine the rotation to make it point towards the ship
    	double shipVelocityX = ship.getVelocityX();
    	double shipVelocityY = ship.getVelocityY();
        double combinedVelocity = Math.sqrt(shipVelocityX * shipVelocityX
        		+ shipVelocityY * shipVelocityY);
        double normalizedshipVelocityX = shipVelocityX / combinedVelocity;
        double normalizedshipVelocityY = - shipVelocityY / combinedVelocity;
        double directionRotation = Math.toDegrees(Math.atan2(normalizedshipVelocityX * Math.PI,
        		normalizedshipVelocityY * Math.PI));
        // Draw the triangle with its current rotation
        canvas.save();
        canvas.rotate((float) directionRotation, UI_ABSOLUTE_X, UI_ABSOLUTE_Y);
        canvas.drawPath(absoluteArrowPath, directionPaint);
        canvas.restore();
        
        
        /*
         * Draws a rectangle on the edge of the screen
         * pointing to the ship if it's out f the screen
         */
        double shipOutScreen = shipOutScreen();
        if(shipOutScreen > 0) {
        	int [] directionArrowPosition = getDirectionPosition();
        	trianglePoint1.set(directionArrowPosition[0],
        			directionArrowPosition[1] - UI_RELATIVE_ARROW_HEIGHT / 2);
        	trianglePoint2.set(directionArrowPosition[0] - UI_RELATIVE_ARROW_WIDTH / 2,
        			directionArrowPosition[1] + UI_RELATIVE_ARROW_HEIGHT / 2);
        	trianglePoint3.set(directionArrowPosition[0] + UI_RELATIVE_ARROW_WIDTH / 2,
        			directionArrowPosition[1] + UI_RELATIVE_ARROW_HEIGHT / 2);
	
        	relativeArrowPath.reset();
	        relativeArrowPath.setFillType(Path.FillType.EVEN_ODD);
	        relativeArrowPath.moveTo(trianglePoint1.x,trianglePoint1.y);
	        relativeArrowPath.lineTo(trianglePoint2.x,trianglePoint2.y);
	        relativeArrowPath.lineTo(trianglePoint3.x,trianglePoint3.y);
	        relativeArrowPath.lineTo(trianglePoint1.x,trianglePoint1.y);
	        relativeArrowPath.close();
	        
	        // set the color, the more distant the reddish
	        relativeArrowPaint.setARGB(255,
	        		(int) Math.round(255 * shipOutScreen),
	        		(int) Math.round(255 * (1 - shipOutScreen)),
	        		0);
	        
	        //determine the rotation to make it point towards the ship
	    	double shipDirectionX = ship.getX() - canvasWidth / 2;
	    	double shipDirectionY = ship.getY() - canvasHeight / 2;
	        double distance = Math.sqrt(shipDirectionX * shipDirectionX
	        		+ shipDirectionY * shipDirectionY);
	        double normalizedShipDirectionX = shipDirectionX / distance;
	        double normalizedShipDirectionY = - shipDirectionY / distance;
	        double rotation = Math.toDegrees(Math.atan2(normalizedShipDirectionX * Math.PI,
	        		normalizedShipDirectionY * Math.PI));
	        // Draw the triangle with its current rotation
	        canvas.save();
	        canvas.rotate((float) rotation, directionArrowPosition[0], directionArrowPosition[1]);
	        canvas.drawPath(relativeArrowPath, relativeArrowPaint);
	        canvas.restore();
        }        
        // TODO: use a Path for the trail of the ship
    }
    
    private int [] getDirectionPosition() {
    	int [] directionPosition = new int [2];
    	int shipX = (int) Math.round(ship.getX());
    	int shipY = (int) Math.round(ship.getY());
		directionPosition[0] = canvasWidth / 2;
		directionPosition[1] = canvasHeight / 2;
		int spacing = Math.max(UI_RELATIVE_ARROW_HEIGHT, UI_RELATIVE_ARROW_WIDTH);
    	
    	// Split the possible position outside the screen in 8
    	// like the # symbol with the screen in the middle
    	if (shipX >= canvasWidth) {
    		if(shipY <= 0) {
    			directionPosition[0] = canvasWidth - spacing;
    			directionPosition[1] = spacing;
    		}
    		else if(shipY >= canvasHeight) {
    			directionPosition[0] = canvasWidth - spacing;
    			directionPosition[1] = canvasHeight - spacing;
    		}
    		else {
    			directionPosition[0] = canvasWidth - spacing;
    			directionPosition[1] = shipY;
    		}
    	}
	    else if (shipX <= 0) {
			if(shipY <= 0) {
				directionPosition[0] = spacing;
				directionPosition[1] = spacing;
			}
			else if(shipY >= canvasHeight) {
				directionPosition[0] = spacing;
				directionPosition[1] = canvasHeight - spacing;
			}
			else {
				directionPosition[0] = spacing;
				directionPosition[1] = shipY;
			}
		}
	    else {
			if(shipY <= 0) {
				directionPosition[0] = shipX;
				directionPosition[1] = spacing;
			}
			else {
				directionPosition[0] = shipX;
				directionPosition[1] = canvasHeight - spacing;
			}
	    }
    	
    	return directionPosition;
    }
    
    public boolean isShipCaught (BlackHole blackHole){
		if(blackHole.isCaught(ship.getX(), ship.getY()))
			return true;
		return false;
    }
	
	public Point isShipCollided(Planet planet) {
		double distanceX = 0,
				distanceY = 0,
				distance,
				shipX = ship.getX(),
				shipY = ship.getY(),
				size = ship.getHeight();
		distanceX = planet.getX() - shipX;
		distanceY = planet.getY() - shipY;
		distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
		// Check if they have collided
		if (distance //distance among them
				< (size + planet.getSize()) / 2){ //and the game is running
			// if it's the earth ...
			if (planet.getName() == Planet.PlanetsName.Earth) {
				// and the speed is low enough the game is won
				if (ship.getSpeed() <= GAME_MAX_LANDING_SPEED) {
					ship.setLanding(distanceX, distanceY);
					thread.setState(SpaceTravelsThread.STATE_WIN,
							"You have landed the ship!\n"
									+ "Speed: " + Math.round(ship.getSpeed()) + "\n"
									+ " Goal:" + GAME_MAX_LANDING_SPEED);
					return null;
				}
				else{
					((SpaceTravelsThread) thread).setState(SpaceTravelsThread.STATE_LOSE,
							"You have landed too fast!\n"
							+ "Speed: " + Math.round(ship.getSpeed()) + "\n"
							+ " Goal:" + GAME_MAX_LANDING_SPEED);
					Point point = new Point();
					point.set((int) Math.round(planet.getX() - (planet.getSize() / 2 * distanceX / distance)),
							(int) Math.round(planet.getY() - (planet.getSize() / 2 * distanceY / distance)));
					return point;
				}
			}
			else {
				((SpaceTravelsThread) thread).setState(SpaceTravelsThread.STATE_LOSE,
						"You have crashed into a planet!");
				Point point = new Point();
				point.set((int) Math.round(planet.getX() - (planet.getSize() / 2 * distanceX / distance)),
						(int) Math.round(planet.getY() - (planet.getSize() / 2 * distanceY / distance)));
				return point;
			}
		}
		return null;
	}
	
	/*
	 * Return 0 if not, a value from 0 to 1 if true and in range,
	 * over 1 if it is over the maximum distance (game lost).
	 * The returned value is the distance ration between the
	 * actual and the maximum (for wich the game is lost)
	 */
	public double shipOutScreen(){
		double shipX = ship.getX();
		double shipY = ship.getY();
    	// Split the possible position outside the screen in 9
    	// like the # symbol with the screen in the middle
    	if (shipX > canvasWidth)
    		if(shipY < 0)
    			return Math.sqrt((shipX - canvasWidth) * (shipX - canvasWidth)
    					+ shipY * shipY)
    					/ GAME_LOST_DISTANCE;
    		else if(shipY > canvasHeight)
    			return Math.sqrt((shipX - canvasWidth) * (shipX - canvasWidth)
    					+ (shipY - canvasHeight) * (shipY - canvasHeight))
    					/ GAME_LOST_DISTANCE;
    		else
    			return (shipX - canvasWidth) / GAME_LOST_DISTANCE;
	    else if (shipX < 0)
			if(shipY < 0)
    			return Math.sqrt(shipX * shipX + shipY * shipY)
    					/ GAME_LOST_DISTANCE;
			else if(shipY > canvasHeight)
    			return Math.sqrt(shipX * shipX
    					+ (shipY - canvasHeight) * (shipY - canvasHeight))
    					/ GAME_LOST_DISTANCE;
			else
    			return - shipX / GAME_LOST_DISTANCE;
	    else
			if(shipY < 0)
    			return - shipY / GAME_LOST_DISTANCE;
			else if(shipY > canvasHeight)
    			return (shipY - canvasHeight) / GAME_LOST_DISTANCE;
			else
		    	return 0;
	}
	
	public double[] getForce (){
		planetIterator = planets.iterator();
		double distanceX = 0,
				distanceY = 0,
				force = 0,
				forceX = 0,
				forceY = 0;
		Planet planet;
		double [] forceArray = new double[2];
		
		// planets force
		while(planetIterator.hasNext()) {
			planet = planetIterator.next();
			distanceX = planet.getX() - ship.getX();
			distanceY = planet.getY() - ship.getY();
			/*
			 *  gravitation force is
			 *  gravitational_constant * first_mass * second_mass / distance ^ 2
			 *  as the ship mass is always 1 i dont consider it
			 */
			// TODO avoid division by 0
			force = PHYS_GRAVITATIONAL_CONSTANTS 
					* planet.getGravForce()
					/ Math.sqrt(distanceX * distanceX + distanceY * distanceY);
			forceX += force * distanceX / (Math.abs(distanceX) + Math.abs(distanceY));
			forceY += force * distanceY / (Math.abs(distanceX) + Math.abs(distanceY));
		}
		
		//black hole force
		distanceX = blackHole.getX() - ship.getX();
		distanceY = blackHole.getY() - ship.getY();
		force = PHYS_GRAVITATIONAL_CONSTANTS 
				* BlackHole.getGravforce()
				/ Math.sqrt(distanceX * distanceX + distanceY * distanceY);
		forceX += force * distanceX / (Math.abs(distanceX) + Math.abs(distanceY));
		forceY += force * distanceY / (Math.abs(distanceX) + Math.abs(distanceY));
		
		forceArray[0] = forceX;
		forceArray[1] = forceY;
		return forceArray;
	}


    /**
     * Fetches the animation thread corresponding to this LunarView.
     * 
     * @return the animation thread
     */
    public SpaceTravelsThread getThread() {
        return thread;
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        if(! thread.isAlive()) {
        	thread.setRunning(true);
        	thread.start();
        }
        else {
        	thread.unpause();
        }
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the backgroundImage
	 */
	public Bitmap getBackgroundImage() {
		return backgroundImage;
	}

	/**
	 * @param backgroundImage the backgroundImage to set
	 */
	public void setBackgroundImage(Bitmap backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextView(TextView textView) {
        statusText = textView;
    }
}
