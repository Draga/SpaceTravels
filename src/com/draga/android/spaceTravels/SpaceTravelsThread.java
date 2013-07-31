/**
 *
 */
package com.draga.android.spaceTravels;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;

/**
 * @author Draga
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class SpaceTravelsThread extends Thread implements SensorEventListener {
	/*
	 * Difficulty setting constants
	 */
	public static final int DIFFICULTY_EASY = 0;
	public static final int DIFFICULTY_MEDIUM = 1;
	public static final int DIFFICULTY_HARD = 2;

	/*
	 * State-tracking constants
	 */
	public static enum GameState {
		Lose, Pause, Ready, Running, Win, Error, Animating
	}

	/*
	 * Game graphics constants
	 */
	private final static int MAX_FPS = 50;// desired fps
	private final static int MAX_FRAME_SKIPS = 10;// maximum number of frames to
													// be skipped
	private final static int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period
	/*
	 * Sensor variables
	 */
	private static Sensor sensor;
	private static SensorManager sensorManager;
	/*
	 * other handlers
	 */
	private static WindowManager windowManager;
	private static Handler handler;
	private static Context context;
	private double sensorX;
	private double sensorY;
	private int sensorAccuracy;
	private Display display;
	private Canvas canvas;
	/*
	 * Parameters to be received in the constructor
	 */
	private SurfaceHolder surfaceHolder;
	private SpaceTravelsGame spaceTravelsGame;

	/**
	 * Current difficulty -- amount of fuel, allowed angle, etc. Default is
	 * MEDIUM.
	 */
	// private int difficulty;
	/**
	 * The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN
	 */
	private GameState state;
	/**
	 * Indicate whether the surface has been created & is ready to draw
	 */
	private boolean running = false;
	/**
	 * Store the starting time to calculate the total playing time
	 */
	private long gameStartTime;
	private long lastUpdate = 0;
	private double elapsed;
	private boolean ThreadPause;

	public SpaceTravelsThread(SurfaceHolder _surfaceHolder,
			SpaceTravelsGame spaceTravelsGame, Context _context,
			Handler _handler) {
		super();
		surfaceHolder = _surfaceHolder;
		handler = _handler;
		// context = _context;
		this.spaceTravelsGame = spaceTravelsGame;
		context = _context;

		// register sensor listener

		// Get an instance of the SensorManager
		sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, sensor,
				SensorManager.SENSOR_DELAY_GAME);

		// Get an instance of the WindowManager
		windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		display = windowManager.getDefaultDisplay();
		ThreadPause = false;
	}

	/**
	 * Used to signal the thread whether it should be running or not. Passing
	 * true allows the thread to run; passing false will shut it down if it's
	 * already running. Calling start() after this was most recently called with
	 * false will result in an immediate shutdown.
	 * 
	 * @param running
	 *            true to run, false to shut down
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/*
	 * Starts the game, setting parameters for the current difficulty.
	 */
	public void doStart() {
		synchronized (surfaceHolder) {
			spaceTravelsGame.init();
			gameStartTime = SystemClock.uptimeMillis();
			ThreadPause = false;
			setState(GameState.Running);
			surfaceHolder.notifyAll();
		}
	}

	/**
	 * Pauses the physics update & animation.
	 */
	public void pause() {
		synchronized (surfaceHolder) {
			if (getGameState() == GameState.Running)
				setState(GameState.Pause);
			ThreadPause = true;
		}
	}

	/**
	 * Restores game state from the indicated Bundle. Typically called when the
	 * Activity is being restored after having been previously destroyed.
	 * 
	 * @param savedState
	 *            Bundle containing the game state
	 */
	public synchronized void restoreState(Bundle savedState) {
		synchronized (surfaceHolder) {
			setState(GameState.Running);
			/* mDifficulty = savedState.getInt(KEY_DIFFICULTY); */
		}
	}

	@Override
	public void run() {
		long beginTime; // the time when the cycle begun
		long timeDiff; // the time it took for the cycle to execute
		long sleepTime; // ms to sleep (<0 if we're behind)
		int framesSkipped; // number of frames being skipped
		sleepTime = 0;
		gameStartTime = SystemClock.uptimeMillis();

		while (running) {
			canvas = null;
			try {
				canvas = this.surfaceHolder.lockCanvas();
				synchronized (surfaceHolder) {
					beginTime = SystemClock.uptimeMillis();
					if (lastUpdate == 0)
						elapsed = 0;
					else {
						elapsed = SystemClock.uptimeMillis();
						elapsed -= lastUpdate;
					}
					framesSkipped = 0; // resetting the frames skipped
					spaceTravelsGame.update(getGameState(), elapsed);
					lastUpdate = SystemClock.uptimeMillis();
					spaceTravelsGame.doDraw(canvas);
					// calculate how long did the cycle take
					timeDiff = SystemClock.uptimeMillis() - beginTime;
					// calculate sleep time
					sleepTime = (FRAME_PERIOD - timeDiff);

					if (sleepTime > 0) {
						// if sleepTime > 0 we're OK
						try {
							// send the thread to sleep for a short period
							// very useful for battery saving
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
						}
					}

					// while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS)
					// {
					// // we need to catch up
					// // update without rendering
					// // spaceTravelsGame.update(getGameState(), elapsed);
					// // add frame period to check if in next frame
					// sleepTime += FRAME_PERIOD;
					// framesSkipped++;
					// }

					while (ThreadPause) {
						try {
							long pausedTime = SystemClock.uptimeMillis();
							surfaceHolder.wait();
							lastUpdate += (SystemClock.uptimeMillis() - pausedTime);
						} catch (Exception e) {
						}
					}
				}
			} catch (Exception e) {
				setState(GameState.Error, e.getMessage());
				e.printStackTrace();
				running = false;
			} finally {
				// do this in a finally so that if an exception is thrown
				// during the above, we don't leave the Surface in an
				// inconsistent state
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	/**
	 * Dump game state to the provided Bundle. Typically called when the
	 * Activity is being suspended.
	 * 
	 * @return Bundle with this view's state
	 */
	public Bundle saveState(Bundle map) {
		synchronized (surfaceHolder) {
			if (map != null) {
				/* map.putInt(KEY_DIFFICULTY, Integer.valueOf(mDifficulty)); */
				map.putSerializable("SpaceTravelsGame", spaceTravelsGame);
			}
		}
		return map;
	}

	/**
	 * Sets the game mode. That is, whether we are running, paused, in the
	 * failure state, in the victory state, etc.
	 * 
	 * @param state
	 *            one of the STATE_* constants
	 * @see #setState(int, CharSequence)
	 */
	public void setState(GameState state) {
		synchronized (surfaceHolder) {
			setState(state, null);
		}
	}

	/**
	 * Sets the game mode. That is, whether we are running, paused, in the
	 * failure state, in the victory state, etc.
	 * 
	 * @param state
	 *            one of the STATE_* constants
	 * @param message
	 *            string to add to screen or null
	 */
	public void setState(GameState state, CharSequence message) {
		/*
		 * This method optionally can cause a text message to be displayed to
		 * the user when the mode changes. Since the View that actually renders
		 * that text is part of the main View hierarchy and not owned by this
		 * thread, we can't touch the state of that View. Instead we use a
		 * Message + Handler to relay commands to the main thread, which updates
		 * the user-text View.
		 */
		synchronized (surfaceHolder) {
			this.state = state;

			Message msg = handler.obtainMessage();
			Bundle bundle = new Bundle();

			if (state == GameState.Running) {
				bundle.putString("text", "");
				bundle.putInt("viz", View.INVISIBLE);
			} else {
				Resources res = context.getResources();
				CharSequence str = "";
				switch (state) {
				case Running:
					str = res.getText(R.string.mode_ready);
					break;
				case Pause:
					str = res.getText(R.string.mode_pause);
					break;
				case Lose:
					str = res.getText(R.string.mode_lose);
					break;
				case Error:
					str = res.getText(R.string.mode_error) + "\n" + message;
					break;
				case Win:
					double gameTime = SystemClock.uptimeMillis();
					gameTime -= gameStartTime;
					gameTime /= 1000;
					str = res.getString(R.string.mode_win) + "\n" + "Time: "
							+ gameTime + " sec";
					break;
				default:
					setState(GameState.Error, "Invalid Game State :" + state);
					break;
				}

				if (message != null) {
					str = str + "\n" + message;
				}
				bundle.putString("text", str.toString());
				bundle.putInt("viz", View.VISIBLE);
			}
			msg.setData(bundle);
			handler.sendMessage(msg);
			surfaceHolder.notifyAll();
		}
	}

	/* Callback invoked when the surface dimensions change. */
	/*
	 * public void setSurfaceSize(int width, int height) { // synchronized to
	 * make sure these all change atomically synchronized (surfaceHolder) {
	 * spaceTravelsGame.canvasWidth = width; spaceTravelsGame.canvasHeight =
	 * height;
	 * 
	 * // don't forget to resize the background image
	 * spaceTravelsGame.setBackgroundImage(Bitmap.createScaledBitmap(
	 * spaceTravelsGame.getBackgroundImage(), width, height, true)); } }
	 */

	/**
	 * Resumes from a pause.
	 */
	public void unpause() {
		synchronized (surfaceHolder) {
			// setState(GameState.Running);
			ThreadPause = false;
			surfaceHolder.notifyAll();
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			sensorAccuracy = accuracy;
		return;
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;
		/*
		 * record the accelerometer data, the event's timestamp as well as the
		 * current time. The latter is needed so we can calculate the "present"
		 * time during rendering. In this application, we need to take into
		 * account how the screen is rotated with respect to the sensors (which
		 * always return data in a coordinate space aligned to with the screen
		 * in its native orientation).
		 */
		switch (display.getRotation()) {
		case Surface.ROTATION_0:
			sensorX = event.values[0];
			sensorY = event.values[1];
			break;
		case Surface.ROTATION_90:
			sensorX = event.values[1];
			sensorY = event.values[0];
			break;
		case Surface.ROTATION_180:
			sensorX = -event.values[0];
			sensorY = -event.values[1];
			break;
		case Surface.ROTATION_270:
			sensorX = event.values[1];
			sensorY = -event.values[0];
			break;
		}
	}

	public Vector2d getSensorForce() {
		return new Vector2d(sensorX, sensorY);
	}

	/**
	 * @return the sensorX
	 */
	public double getSensorX() {
		return sensorX;
	}

	/**
	 * @return the sensorY
	 */
	public double getSensorY() {
		return sensorY;
	}

	public int getSensorAccuracy() {
		return sensorAccuracy;
	}

	/**
	 * @return the state
	 */
	public GameState getGameState() {
		return state;
	}
}
