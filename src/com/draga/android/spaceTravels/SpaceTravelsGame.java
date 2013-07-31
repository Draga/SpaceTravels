package com.draga.android.spaceTravels;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.Serializable;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.draga.android.spaceTravels.SpaceTravelsThread.GameState;
import com.draga.android.spaceTravels.Drawable.Planet;
import com.draga.android.spaceTravels.Drawable.Planet.PlanetsName;
import com.draga.android.spaceTravels.Drawable.Animated.BlackHole;
import com.draga.android.spaceTravels.Drawable.Animated.Explosion;
import com.draga.android.spaceTravels.Drawable.Animated.Ship;
import com.draga.android.spaceTravels.Drawable.Overlay.SpeedBar;

public class SpaceTravelsGame extends SurfaceView implements
		SurfaceHolder.Callback, Serializable {
	/*
	 * Game constants
	 */
	public static final int GAME_LOST_DISTANCE = 500;
	public static final int GAME_MAX_LANDING_SPEED = 50;
	/*
	 * Game Physics constants
	 */
	public static final double PHYS_GRAVITATIONAL_CONSTANT = 0.000001;
	/*
	 * UI constants (i.e. the acceleration & fuel bars)
	 */
	public static final double UI_DIRECTION_SIZE = 0.85; // from 0 to 1
	public static final int UI_RELATIVE_ARROW_WIDTH = 6;
	public static final int UI_RELATIVE_ARROW_HEIGHT = 10;
	private static final double GAME_PLANET_SCALE = 3;
	// public static final int UI_ABSOLUTE_X = UI_BAR_SPACING + UI_BAR_HEIGHT /
	// 2;
	// public static final int UI_ABSOLUTE_Y = UI_ABSOLUTE_X;
	private static List<Planet> planets;
	private static Iterator<Planet> planetIterator;
	/*
	 * Canvas and variables
	 */
	int canvasHeight = 1;
	int canvasWidth = 1;
	private DisplayMetrics displayMetrics;
	/**
	 * this is the particle system
	 */
	private Ship ship;
	private BlackHole blackHole;
	private SpeedBar speedBar;
	private Context context;
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

	private List<Explosion> explosions;

	public SpaceTravelsGame(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		// register our interest in hearing about changes to our surface

		// create thread only; it's started in surfaceCreated()
		thread = createThread();
		// Make the game focusable so it can handle events
		setFocusable(true);
		// Get an instance of the WindowManager
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);
		canvasWidth = displayMetrics.widthPixels;
		canvasHeight = displayMetrics.heightPixels;

		/*
		 * the resource
		 */
		Resources resources = context.getResources();

		// load background image as a Bitmap instead of a Drawable b/c
		// we don't need to transform it and it's faster to draw this way
		backgroundImage = BitmapFactory.decodeResource(resources,
				R.drawable.background);
	}

	/**
	 * @param context
	 * @param holder
	 * @return
	 */
	private SpaceTravelsThread createThread() {
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		return new SpaceTravelsThread(holder, this, getContext(),
				new Handler() {
					@Override
					public void handleMessage(Message m) {
						statusText.setVisibility(m.getData().getInt("viz"));
						statusText.setText(m.getData().getString("text"));
						super.handleMessage(m);
					}
				});
	}

	void init() {
		Planet planet;
		planets = new ArrayList<Planet>();
		for (PlanetsName planetName : Planet.PlanetsName.values()) {
			switch (planetName) {
			default:
			case Earth:
				planet = new Planet(new Vector2d(canvasWidth * 0.97f,
						canvasHeight * 0.5f), context.getResources()
						.getDrawable(R.drawable.earth));
				planet.gravForce = 1;
				planet.setSize((int) (12.756 * GAME_PLANET_SCALE));
				break;
			case Mars:
				planet = new Planet(new Vector2d(canvasWidth * 0.4f,
						canvasHeight * 0.85f), context.getResources()
						.getDrawable(R.drawable.mars));
				planet.gravForce = 0.38f;
				planet.setSize((int) (12.756 * GAME_PLANET_SCALE));
				break;
			case Jupiter:
				planet = new Planet(new Vector2d(canvasWidth * 0.55f,
						canvasHeight * 0.35f), context.getResources()
						.getDrawable(R.drawable.jupiter));
				planet.gravForce = 2.54f;
				// this.size = (int) (142.800 * GAME_PLANET_SCALE);
				planet.setSize((int) (30 * GAME_PLANET_SCALE));
				break;
			case Venus:
				planet = new Planet(new Vector2d(canvasWidth * 0.7f,
						canvasHeight * 0.7f), context.getResources()
						.getDrawable(R.drawable.jupiter));
				planet.gravForce = 0.91f;
				planet.setSize((int) (12.104 * GAME_PLANET_SCALE));
				break;
			}
			planets.add(planet);
		}

		ship = new Ship(context, new Vector2d(canvasHeight * 0.5,
				canvasHeight * 0.5));

		speedBar = new SpeedBar();

		explosions = new ArrayList<Explosion>();
		blackHole = new BlackHole(new Vector2d(Math.round(canvasWidth * 0.4),
				Math.round(canvasHeight * 0.6)), context);
	}

	void update(GameState state, double elapsed) {
		if (state == SpaceTravelsThread.GameState.Running) {
			Vector2d gravityOnShip = getGravity(ship.position);
			Vector2d accelerometerForce = thread.getSensorForce();
			ship.update(gravityOnShip, accelerometerForce, elapsed);
			// whether the ship has been lost
			if (shipOutScreen() > 1) {
				thread.setState(SpaceTravelsThread.GameState.Lose,
						getResources().getString(R.string.lost_lostShip));
			}
			// whether the earth has crashed into another planet
			// TODO: fix collision detection with width and height
			planetIterator = planets.iterator();
			while (planetIterator.hasNext()) {
				Planet planet = planetIterator.next();
				if (isShipCollided(planet) != null) {
					explosions.add(new Explosion(context, ship.position));
				}
			}
			if (isShipCaught(blackHole)) {
				ship.setLanding(blackHole.position.x - ship.position.x,
						blackHole.position.y - ship.position.y);
				thread.setState(SpaceTravelsThread.GameState.Lose,
						getResources().getString(R.string.lost_blackHole));
			}
		}
		blackHole.update(elapsed);
		ArrayList<Explosion> exaustedExplosions = new ArrayList<Explosion>();
		for (int i = 0; i < explosions.size(); i++) {
			if (explosions.get(i).isAnimating()) {
				explosions.get(i).update();
			} else {
				exaustedExplosions.add(explosions.get(i));
			}
		}
		for (int i = 0; i < exaustedExplosions.size(); i++) {
			explosions.remove(exaustedExplosions.get(i));
		}
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

		speedBar.draw(ship, canvas, displayMetrics.density);

		for (int i = 0; i < explosions.size(); i++) {
			explosions.get(i).draw(canvas, displayMetrics.density);
		}

		/*
		 * Draws a rectangle on the edge of the screen pointing to the ship if
		 * it's out f the screen
		 */
		double shipOutScreen = shipOutScreen();
		if (shipOutScreen > 0) {
			// int[] directionArrowPosition = getDirectionPosition();
			// trianglePoint1.set(directionArrowPosition[0],
			// directionArrowPosition[1] - UI_RELATIVE_ARROW_HEIGHT / 2);
			// trianglePoint2.set(directionArrowPosition[0] -
			// UI_RELATIVE_ARROW_WIDTH / 2,
			// directionArrowPosition[1] + UI_RELATIVE_ARROW_HEIGHT / 2);
			// trianglePoint3.set(directionArrowPosition[0] +
			// UI_RELATIVE_ARROW_WIDTH / 2,
			// directionArrowPosition[1] + UI_RELATIVE_ARROW_HEIGHT / 2);
			//
			// relativeArrowPath.reset();
			// relativeArrowPath.setFillType(Path.FillType.EVEN_ODD);
			// relativeArrowPath.moveTo(trianglePoint1.x, trianglePoint1.y);
			// relativeArrowPath.lineTo(trianglePoint2.x, trianglePoint2.y);
			// relativeArrowPath.lineTo(trianglePoint3.x, trianglePoint3.y);
			// relativeArrowPath.lineTo(trianglePoint1.x, trianglePoint1.y);
			// relativeArrowPath.close();
			//
			// // set the color, the more distant the reddish
			// relativeArrowPaint.setARGB(255,
			// (int) Math.round(255 * shipOutScreen),
			// (int) Math.round(255 * (1 - shipOutScreen)),
			// 0);
			//
			// //determine the rotation to make it point towards the ship
			// double shipDirectionX = ship.position.x - canvasWidth / 2;
			// double shipDirectionY = ship.position.y - canvasHeight / 2;
			// double distance = Math.sqrt(shipDirectionX * shipDirectionX
			// + shipDirectionY * shipDirectionY);
			// double normalizedShipDirectionX = shipDirectionX / distance;
			// double normalizedShipDirectionY = -shipDirectionY / distance;
			// double rotation =
			// Math.toDegrees(Math.atan2(normalizedShipDirectionX * Math.PI,
			// normalizedShipDirectionY * Math.PI));
			// // draw the triangle with its current rotation
			// canvas.save();
			// canvas.rotate((double) rotation, directionArrowPosition[0],
			// directionArrowPosition[1]);
			// canvas.drawPath(relativeArrowPath, relativeArrowPaint);
			// canvas.restore();
		}
		// TODO: use a Path for the trail of the ship
	}

	private int[] getDirectionPosition() {
		int[] directionPosition = new int[2];
		int shipX = (int) Math.round(ship.position.x);
		int shipY = (int) Math.round(ship.position.y);
		directionPosition[0] = canvasWidth / 2;
		directionPosition[1] = canvasHeight / 2;
		int spacing = Math.max(UI_RELATIVE_ARROW_HEIGHT,
				UI_RELATIVE_ARROW_WIDTH);

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

	public Vector2d isShipCollided(Planet planet) {
		double distanceX, distanceY, distance, shipX = ship.position.x, shipY = ship.position.y, size = ship.height;
		distanceX = planet.position.x - shipX;
		distanceY = planet.position.y - shipY;
		distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
		// Check if they have collided
		if (distance // distance among them
		< (size + planet.getSize()) / 2) { // and the game is running
			// if it's the earth ...
			if (planet.name == Planet.PlanetsName.Earth) {
				// and the acceleration is low enough the game is won
				if (ship.acceleration.toLinear() <= GAME_MAX_LANDING_SPEED) {
					ship.setLanding(distanceX, distanceY);
					thread.setState(
							SpaceTravelsThread.GameState.Win,
							"You have landed the ship!\n" + "Speed: "
									+ Math.round(ship.acceleration.toLinear())
									+ "\n" + " Goal:" + GAME_MAX_LANDING_SPEED);
					return null;
				} else {
					thread.setState(
							SpaceTravelsThread.GameState.Lose,
							"You have landed too fast!\n" + "Speed: "
									+ Math.round(ship.acceleration.toLinear())
									+ "\n" + " Goal:" + GAME_MAX_LANDING_SPEED);
					Vector2d point = new Vector2d(
							(int) Math
									.round(planet.position.x
											- (planet.getSize() / 2 * distanceX / distance)),
							(int) Math.round(planet.position.y
									- (planet.getSize() / 2 * distanceY / distance)));
					return point;
				}
			} else {
				thread.setState(SpaceTravelsThread.GameState.Lose,
						"You have crashed into a planet!");
				Vector2d point = new Vector2d(
						(int) Math
								.round(planet.position.x
										- (planet.getSize() / 2 * distanceX / distance)),
						(int) Math.round(planet.position.y
								- (planet.getSize() / 2 * distanceY / distance)));
				return point;
			}
		}
		return null;
	}

	/*
	 * Return 0 if not, a value from 0 to 1 if true and in range, over 1 if it
	 * is over the maximum distance (game lost). The returned value is the
	 * distance ration between the actual and the maximum (for which the game is
	 * lost)
	 */
	public double shipOutScreen() {
		// Split the possible position outside the screen in 9
		// like the # symbol with the screen in the middle
		if (ship.position.x > canvasWidth)
			if (ship.position.x < 0)
				return Math.sqrt((ship.position.x - canvasWidth)
						* (ship.position.x - canvasWidth) + ship.position.x
						* ship.position.x)
						/ GAME_LOST_DISTANCE;
			else if (ship.position.x > canvasHeight)
				return Math.sqrt((ship.position.x - canvasWidth)
						* (ship.position.x - canvasWidth)
						+ (ship.position.x - canvasHeight)
						* (ship.position.x - canvasHeight))
						/ GAME_LOST_DISTANCE;
			else
				return (ship.position.x - canvasWidth) / GAME_LOST_DISTANCE;
		else if (ship.position.x < 0)
			if (ship.position.x < 0)
				return Math.sqrt(ship.position.x * ship.position.x
						+ ship.position.x * ship.position.x)
						/ GAME_LOST_DISTANCE;
			else if (ship.position.x > canvasHeight)
				return Math.sqrt(ship.position.x * ship.position.x
						+ (ship.position.x - canvasHeight)
						* (ship.position.x - canvasHeight))
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

	public Vector2d getGravity(Vector2d position) {
		planetIterator = planets.iterator();
		double linearForce;
		Vector2d force = new Vector2d(), distance = new Vector2d();
		Planet planet;

		// planets Vector2d
		while (planetIterator.hasNext()) {
			planet = planetIterator.next();
			distance.x = planet.position.x - position.x;
			distance.y = planet.position.y - position.y;
			/*
			 * gravitation Vector2d is gravitational_constant * first_mass *
			 * second_mass / distance ^ 2 as the ship mass is always 1 I don't
			 * consider it
			 */
			// TODO avoid division by 0
			linearForce = PHYS_GRAVITATIONAL_CONSTANT * planet.gravForce
					/ force.toLinear();
			force.x += linearForce * force.x
					/ (Math.abs(force.x) + Math.abs(force.y));
			force.y += linearForce * force.y
					/ (Math.abs(force.x) + Math.abs(force.y));
		}

		// black hole force
		force.x = blackHole.position.x - position.x;
		force.y = blackHole.position.x - position.y;
		linearForce = PHYS_GRAVITATIONAL_CONSTANT * blackHole.gravForce
				/ force.toLinear();
		force.x += linearForce * force.x
				/ (Math.abs(force.x) + Math.abs(force.y));
		force.y += linearForce * force.y
				/ (Math.abs(force.x) + Math.abs(force.y));

		return force;
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
		if (!hasWindowFocus)
			thread.pause();
	}

	/*
	 * Callback invoked when the Surface has been created and is ready to be
	 * used.
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		// start the thread here so that we don't busy-wait in run()
		// waiting for the surface to be created
		if (!thread.isAlive()) {
			if (thread.getState() == State.NEW)
				thread.start();
			thread.setRunning(true);
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

	public void setMessageView(TextView message) {
		statusText = message;
	}
}
