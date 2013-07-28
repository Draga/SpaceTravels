package com.draga.android.spaceTravels;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.TextView;
import com.draga.android.spaceTravels.GameDrawable.BlackHole;
import com.draga.android.spaceTravels.GameDrawable.Explosion;
import com.draga.android.spaceTravels.GameDrawable.Planet;
import com.draga.android.spaceTravels.GameDrawable.Planet.PlanetsName;
import com.draga.android.spaceTravels.GameDrawable.Ship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpaceTravelsGame extends SurfaceView implements SurfaceHolder.Callback {
    /*
     * Game constants
     */
    public static final int GAME_LOST_DISTANCE = 500;
    public static final int GAME_MAX_LANDING_SPEED = 50;
    /*
     * Game Physics constants
     */
    public static final double PHYS_GRAVITATIONAL_CONSTANTS = 2000;
    /*
     * UI constants (i.e. the speed & fuel bars)
     */
    public static final double UI_DIRECTION_SIZE = 0.85; // from 0 to 1
    public static final int UI_RELATIVE_ARROW_WIDTH = 6;
    public static final int UI_RELATIVE_ARROW_HEIGHT = 10;
    private static final double GAME_PLANET_SCALE = 3;
    //    public static final int UI_ABSOLUTE_X = UI_BAR_SPACING + UI_BAR_HEIGHT / 2;
//    public static final int UI_ABSOLUTE_Y = UI_ABSOLUTE_X;
    private static List<Planet> planets;
    private static Iterator<Planet> planetIterator;
    private static WindowManager windowManager;
    /*
     * Canvas and variables
     */
    int canvasHeight = 1;
    int canvasWidth = 1;
    TwoD TwoD;
    private Display display;
    private DisplayMetrics displayMetrics;
    /**
     * this is the particle system
     */
    private Ship ship;
    private BlackHole blackHole;
    private Context context;
    /**
     * the resource
     */
    private Resources resources;
    /**
     * The drawable to use as the background of the animation canvas
     */
    private Bitmap backgroundImage;
    /**
     * Pointer to the text view to display "Paused.." etc.
     */
    private TextView statusText;
    /**
     * The thread that actually draws the animation
     */
    private SpaceTravelsThread thread;
    /*
     * draw variables
     */
    private Paint speedPaint;
    //    private Path absoluteArrowPath;
    //private RectF rect;
//    private Point trianglePoint1 = new Point();
//    private Point trianglePoint2 = new Point();
//    private Point trianglePoint3 = new Point();
//    private Point trianglePoint4 = new Point();
    private List<Explosion> explosions;

    public SpaceTravelsGame(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        statusText = (TextView) ((Activity) context).findViewById(R.id.message);
        // create thread only; it's started in surfaceCreated()
        thread = new SpaceTravelsThread(holder, this, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                statusText.setVisibility(m.getData().getInt("viz"));
                statusText.setText(m.getData().getString("text"));
                super.handleMessage(m);
            }
        });
        // Make the game focusable so it can handle events
        setFocusable(true);
        // Get an instance of the WindowManager
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        canvasWidth = displayMetrics.widthPixels;
        canvasHeight = displayMetrics.heightPixels;

        resources = context.getResources();

        // load background image as a Bitmap instead of a GameDrawable b/c
        // we don't need to transform it and it's faster to draw this way
        backgroundImage = BitmapFactory.decodeResource(resources,
                R.drawable.background);

        speedPaint = new Paint();
        speedPaint.setAntiAlias(true);
        speedPaint.setARGB(255, 0, 255, 0);

        //rect = new RectF(255, 0, 255, 0);

        // fill the path with the points
//    	absoluteArrowPath = new Path();
//    	absoluteArrowPath.setFillType(Path.FillType.EVEN_ODD);
//        absoluteArrowPath.moveTo(trianglePoint1.x,trianglePoint1.y);
//        absoluteArrowPath.lineTo(trianglePoint2.x,trianglePoint2.y);
//        absoluteArrowPath.lineTo(trianglePoint3.x,trianglePoint3.y);
//        absoluteArrowPath.lineTo(trianglePoint4.x,trianglePoint4.y);
//        absoluteArrowPath.lineTo(trianglePoint1.x,trianglePoint1.y);
//        absoluteArrowPath.close();

    }

    void init() {
        Planet planet;
        planets = new ArrayList<Planet>();
        for (PlanetsName planetName : Planet.PlanetsName.values()) {
            switch (planetName) {
                default:
                case Earth:
                    planet = new Planet(new TwoD(canvasWidth * 0.97f, canvasHeight * 0.5f), context.getResources().getDrawable(R.drawable.earth));
                    planet.gravForce = 1;
                    planet.setSize((int) (12.756 * GAME_PLANET_SCALE));
                    break;
                case Mars:
                    planet = new Planet(new TwoD(canvasWidth * 0.4f, canvasHeight * 0.85f), context.getResources().getDrawable(R.drawable.mars));
                    planet.gravForce = 0.38f;
                    planet.setSize((int) (12.756 * GAME_PLANET_SCALE));
                    break;
                case Jupiter:
                    planet = new Planet(new TwoD(canvasWidth * 0.55f, canvasHeight * 0.35f), context.getResources().getDrawable(R.drawable.jupiter));
                    planet.gravForce = 2.54f;
//				this.size = (int) (142.800 * GAME_PLANET_SCALE);
                    planet.setSize((int) (30 * GAME_PLANET_SCALE));
                    break;
                case Venus:
                    planet = new Planet(new TwoD(canvasWidth * 0.7f, canvasHeight * 0.7f), context.getResources().getDrawable(R.drawable.jupiter));
                    planet.gravForce = 0.91f;
                    planet.setSize((int) (12.104 * GAME_PLANET_SCALE));
                    break;
            }
            planets.add(planet);
        }
        ship = new Ship(context, new TwoD(canvasHeight * 0.5, canvasHeight * 0.5));

        explosions = new ArrayList<Explosion>();
        blackHole = new BlackHole(new TwoD(Math.round(canvasWidth * 0.4), Math.round(canvasHeight * 0.6)), context);
    }

    /**
     * Figures the Ball state (x, y, fuel, ...) based on the passage of
     * realtime. Does not invalidate(). Called at the start of draw().
     * Detects the end-of-game and sets the UI to the next state.
     *
     * @param elapsed
     */
    void update(int mode, double elapsed) {
        if (mode == SpaceTravelsThread.STATE_RUNNING) {
            TwoD = getForceOnShip();
            TwoD = new TwoD(0, 0);
            TwoD.x += thread.getSensorX();
            TwoD.y += thread.getSensorY();
            ship.update(TwoD, elapsed);
            // whether the ship has been lost
            if (shipOutScreen() > 1) {
                thread.setState(SpaceTravelsThread.STATE_LOSE, getResources().getString(R.string.lost_lostShip));
            }
            // whether the earth has crashed into another planet
            // TODO: fix collision detection with width and height
            planetIterator = planets.iterator();
            while (planetIterator.hasNext()) {
                Planet planet = planetIterator.next();
                TwoD collisionPoint = isShipCollided(planet);
                if (collisionPoint != null) {
                    explosions.add(new Explosion(context, collisionPoint));
                }
            }
            if (isShipCaught(blackHole)) {
                ship.setLanding(blackHole.position.x - ship.position.x, blackHole.position.y - ship.position.y);
                thread.setState(SpaceTravelsThread.STATE_LOSE, getResources().getString(R.string.lost_blackHole));
            }
        }
        for (int i = 0; i < explosions.size(); ) {
            if (explosions.get(i).isAnimating()) {
                explosions.get(i).update();
                i++;
            } else {
                explosions.remove(i);
            }
        }
        blackHole.update(elapsed);
    }

    /**
     * Draws the ship, planets, gauges, arrows and explosion
     */
    void doDraw(Canvas canvas) {
        // draw the background image. Operations on the Canvas accumulate
        // so this is like clearing the screen.
        canvas.drawBitmap(backgroundImage, 0, 0, null);

        planetIterator = planets.iterator();
        while (planetIterator.hasNext()) {
            planetIterator.next().draw(canvas, displayMetrics.density);
        }

        blackHole.draw(canvas, displayMetrics.density);

        ship.draw(canvas, displayMetrics.density);

        for (int i = 0; i < explosions.size(); ) {
            explosions.get(i).draw(canvas, displayMetrics.density);
        }

        /*
         * draw a white triangle pointing to the direction the ship is going
         */
        // set the color, the faster the reddish, the slower the greenish
//        double shipSpeedRatio = ship.getSpeed() / Ship.PHYS_SPEED_MAX;
//        directionPaint.setARGB(255,
//                (int) (255 * shipSpeedRatio),
//                (int) (255 * (1 - shipSpeedRatio)),
//                0);
//
//        //determine the rotation to make it point towards the ship
//        double shipVelocityX = ship.getVelocityX();
//        double shipVelocityY = ship.getVelocityY();
//        double combinedVelocity = Math.sqrt(shipVelocityX * shipVelocityX
//                + shipVelocityY * shipVelocityY);
//        double normalizedshipVelocityX = shipVelocityX / combinedVelocity;
//        double normalizedshipVelocityY = -shipVelocityY / combinedVelocity;
//        double directionRotation = Math.toDegrees(Math.atan2(normalizedshipVelocityX * Math.PI,
//                normalizedshipVelocityY * Math.PI));
//        // draw the triangle with its current rotation
//        canvas.save();
//        canvas.rotate((double) directionRotation, UI_ABSOLUTE_X, UI_ABSOLUTE_Y);
//        canvas.drawPath(absoluteArrowPath, directionPaint);
//        canvas.restore();


        /*
         * Draws a rectangle on the edge of the screen
         * pointing to the ship if it's out f the screen
         */
        double shipOutScreen = shipOutScreen();
        if (shipOutScreen > 0) {
//            int[] directionArrowPosition = getDirectionPosition();
//            trianglePoint1.set(directionArrowPosition[0],
//                    directionArrowPosition[1] - UI_RELATIVE_ARROW_HEIGHT / 2);
//            trianglePoint2.set(directionArrowPosition[0] - UI_RELATIVE_ARROW_WIDTH / 2,
//                    directionArrowPosition[1] + UI_RELATIVE_ARROW_HEIGHT / 2);
//            trianglePoint3.set(directionArrowPosition[0] + UI_RELATIVE_ARROW_WIDTH / 2,
//                    directionArrowPosition[1] + UI_RELATIVE_ARROW_HEIGHT / 2);
//
//            relativeArrowPath.reset();
//            relativeArrowPath.setFillType(Path.FillType.EVEN_ODD);
//            relativeArrowPath.moveTo(trianglePoint1.x, trianglePoint1.y);
//            relativeArrowPath.lineTo(trianglePoint2.x, trianglePoint2.y);
//            relativeArrowPath.lineTo(trianglePoint3.x, trianglePoint3.y);
//            relativeArrowPath.lineTo(trianglePoint1.x, trianglePoint1.y);
//            relativeArrowPath.close();
//
//            // set the color, the more distant the reddish
//            relativeArrowPaint.setARGB(255,
//                    (int) Math.round(255 * shipOutScreen),
//                    (int) Math.round(255 * (1 - shipOutScreen)),
//                    0);
//
//            //determine the rotation to make it point towards the ship
//            double shipDirectionX = ship.position.x - canvasWidth / 2;
//            double shipDirectionY = ship.position.y - canvasHeight / 2;
//            double distance = Math.sqrt(shipDirectionX * shipDirectionX
//                    + shipDirectionY * shipDirectionY);
//            double normalizedShipDirectionX = shipDirectionX / distance;
//            double normalizedShipDirectionY = -shipDirectionY / distance;
//            double rotation = Math.toDegrees(Math.atan2(normalizedShipDirectionX * Math.PI,
//                    normalizedShipDirectionY * Math.PI));
//            // draw the triangle with its current rotation
//            canvas.save();
//            canvas.rotate((double) rotation, directionArrowPosition[0], directionArrowPosition[1]);
//            canvas.drawPath(relativeArrowPath, relativeArrowPaint);
//            canvas.restore();
        }
        // TODO: use a Path for the trail of the ship
    }

    private int[] getDirectionPosition() {
        int[] directionPosition = new int[2];
        int shipX = (int) Math.round(ship.position.x);
        int shipY = (int) Math.round(ship.position.y);
        directionPosition[0] = canvasWidth / 2;
        directionPosition[1] = canvasHeight / 2;
        int spacing = Math.max(UI_RELATIVE_ARROW_HEIGHT, UI_RELATIVE_ARROW_WIDTH);

        // Split the possible position outside the screen in 8
        // like the # symbol with the screen in the middle
        if (shipX >= canvasWidth) {
            if (shipY <= 0) {
                directionPosition[0] = canvasWidth - spacing;
                directionPosition[1] = spacing;
            } else if (shipY >= canvasHeight) {
                directionPosition[0] = canvasWidth - spacing;
                directionPosition[1] = canvasHeight - spacing;
            } else {
                directionPosition[0] = canvasWidth - spacing;
                directionPosition[1] = shipY;
            }
        } else if (shipX <= 0) {
            if (shipY <= 0) {
                directionPosition[0] = spacing;
                directionPosition[1] = spacing;
            } else if (shipY >= canvasHeight) {
                directionPosition[0] = spacing;
                directionPosition[1] = canvasHeight - spacing;
            } else {
                directionPosition[0] = spacing;
                directionPosition[1] = shipY;
            }
        } else {
            if (shipY <= 0) {
                directionPosition[0] = shipX;
                directionPosition[1] = spacing;
            } else {
                directionPosition[0] = shipX;
                directionPosition[1] = canvasHeight - spacing;
            }
        }

        return directionPosition;
    }

    public boolean isShipCaught(BlackHole blackHole) {
        if (blackHole.isCaught(ship.position))
            return true;
        return false;
    }

    public TwoD isShipCollided(Planet planet) {
        double distanceX = 0,
                distanceY = 0,
                distance,
                shipX = ship.position.x,
                shipY = ship.position.y,
                size = ship.height;
        distanceX = planet.position.x - shipX;
        distanceY = planet.position.y - shipY;
        distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        // Check if they have collided
        if (distance //distance among them
                < (size + planet.getSize()) / 2) { //and the game is running
            // if it's the earth ...
            if (planet.name == Planet.PlanetsName.Earth) {
                // and the speed is low enough the game is won
                if (ship.speed <= GAME_MAX_LANDING_SPEED) {
                    ship.setLanding(distanceX, distanceY);
                    thread.setState(SpaceTravelsThread.STATE_WIN,
                            "You have landed the ship!\n"
//                                    + "Speed: " + Math.round(ship.getSpeed()) + "\n"
                                    + " Goal:" + GAME_MAX_LANDING_SPEED);
                    return null;
                } else {
                    ((SpaceTravelsThread) thread).setState(SpaceTravelsThread.STATE_LOSE,
                            "You have landed too fast!\n"
//                                    + "Speed: " + Math.round(ship.getSpeed()) + "\n"
                                    + " Goal:" + GAME_MAX_LANDING_SPEED);
                    TwoD point = new TwoD((int) Math.round(planet.position.x - (planet.getSize() / 2 * distanceX / distance)),
                            (int) Math.round(planet.position.y - (planet.getSize() / 2 * distanceY / distance)));
                    return point;
                }
            } else {
                ((SpaceTravelsThread) thread).setState(SpaceTravelsThread.STATE_LOSE,
                        "You have crashed into a planet!");
                TwoD point = new TwoD((int) Math.round(planet.position.x - (planet.getSize() / 2 * distanceX / distance)),
                        (int) Math.round(planet.position.y - (planet.getSize() / 2 * distanceY / distance)));
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
    public double shipOutScreen() {
        // Split the possible position outside the screen in 9
        // like the # symbol with the screen in the middle
        if (ship.position.x > canvasWidth)
            if (ship.position.x < 0)
                return Math.sqrt((ship.position.x - canvasWidth) * (ship.position.x - canvasWidth)
                        + ship.position.x * ship.position.x)
                        / GAME_LOST_DISTANCE;
            else if (ship.position.x > canvasHeight)
                return Math.sqrt((ship.position.x - canvasWidth) * (ship.position.x - canvasWidth)
                        + (ship.position.x - canvasHeight) * (ship.position.x - canvasHeight))
                        / GAME_LOST_DISTANCE;
            else
                return (ship.position.x - canvasWidth) / GAME_LOST_DISTANCE;
        else if (ship.position.x < 0)
            if (ship.position.x < 0)
                return Math.sqrt(ship.position.x * ship.position.x + ship.position.x * ship.position.x)
                        / GAME_LOST_DISTANCE;
            else if (ship.position.x > canvasHeight)
                return Math.sqrt(ship.position.x * ship.position.x
                        + (ship.position.x - canvasHeight) * (ship.position.x - canvasHeight))
                        / GAME_LOST_DISTANCE;
            else
                return -ship.position.x / GAME_LOST_DISTANCE;
        else if (ship.position.x < 0)
            return -ship.position.x / GAME_LOST_DISTANCE;
        else if (ship.position.x > canvasHeight)
            return (ship.position.x - canvasHeight) / GAME_LOST_DISTANCE;
        else
            return 0;
    }

    public TwoD getForceOnShip() {
        planetIterator = planets.iterator();
        double distanceX = 0,
                distanceY = 0,
                TwoD = 0,
                forceX = 0,
                forceY = 0;
        Planet planet;

        // planets TwoD
        while (planetIterator.hasNext()) {
            planet = planetIterator.next();
            distanceX = planet.position.x - ship.position.x;
            distanceY = planet.position.y - ship.position.y;
            /*
             *  gravitation TwoD is
			 *  gravitational_constant * first_mass * second_mass / distance ^ 2
			 *  as the ship mass is always 1 i dont consider it
			 */
            // TODO avoid division by 0
            TwoD = PHYS_GRAVITATIONAL_CONSTANTS
                    * planet.gravForce
                    / Math.sqrt(distanceX * distanceX + distanceY * distanceY);
            forceX += TwoD * distanceX / (Math.abs(distanceX) + Math.abs(distanceY));
            forceY += TwoD * distanceY / (Math.abs(distanceX) + Math.abs(distanceY));
        }

        //black hole TwoD
        distanceX = blackHole.position.x - ship.position.x;
        distanceY = blackHole.position.x - ship.position.y;
        TwoD = PHYS_GRAVITATIONAL_CONSTANTS
                * blackHole.gravForce
                / Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        forceX += TwoD * distanceX / (Math.abs(distanceX) + Math.abs(distanceY));
        forceY += TwoD * distanceY / (Math.abs(distanceX) + Math.abs(distanceY));

        return new TwoD(forceX, forceY);
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
        if (!thread.isAlive()) {
            thread.setRunning(true);
            thread.start();
        } else {
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
}
