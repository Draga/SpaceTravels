package com.draga.android.spaceTravels.Drawable;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.draga.android.spaceTravels.R;
import com.draga.android.spaceTravels.SpaceTravelsGame;
import com.draga.android.spaceTravels.SpaceTravelsThread;
import com.draga.android.spaceTravels.SpaceTravelsThread.GameState;

public class SpaceTravels extends Activity {
	private WakeLock wakeLock;
	private static PowerManager powerManager;

	private SpaceTravelsGame spaceTravelsGame;
	private SpaceTravelsThread thread;

	private Menu menu;

	/**
	 * Called when the activity is first created.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SpaceTravelsGame spaceTravelsGame;
		setContentView(R.layout.game);
		if (savedInstanceState != null)
			spaceTravelsGame = (SpaceTravelsGame) savedInstanceState
					.get("SpaceTravelsGame");
		else
			// tell system to use the layout defined in our XML file
			spaceTravelsGame = (SpaceTravelsGame) findViewById(R.id.spaceTravelsGame);

		TextView messageView = (TextView) findViewById(R.id.message);
		spaceTravelsGame.setMessageView(messageView);

		thread = spaceTravelsGame.getThread();

		// give the LunarView a handle to the TextView used for messages
		// spaceTravelsGame.setTextView((TextView) findViewById(R.id.text));

		// Get an instance of the PowerManager
		powerManager = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		// Create a bright wake lock
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				getClass().getName());
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		wakeLock.acquire();
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
		// WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		thread.doStart();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null)
			spaceTravelsGame = (SpaceTravelsGame) savedInstanceState
					.get("SpaceTravelsGame");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu _menu) {
		super.onCreateOptionsMenu(_menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, _menu);

		menu = _menu;

		/*
		 * menu.add(0, MENU_PAUSE, 0, R.string.menu_pause); menu.add(0,
		 * MENU_RESTART, 1, R.string.menu_restart); menu.add(0, MENU_EXIT, 2,
		 * R.string.menu_exit);
		 */

		return true;
	}

	/**
	 * Invoked when the user selects an item from the Menu.
	 * 
	 * @param item
	 *            the Menu entry which was selected
	 * @return true if the Menu item was legit (and we consumed it), false
	 *         otherwise
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.restart:
			thread.doStart();
			return true;
		case R.id.exit:
			// thread.setState(SpaceTravelsThread.STATE_LOSE,
			// getText(R.string.message_stopped));
			// startActivity(new Intent(SpaceTravels.this,
			// SpaceTravelsMain.class));
			finish();
			return true;
		case R.id.resume:
			thread.setState(GameState.Running);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// running for when the thread state didn't update in time,
		// pause for any other case
		if (thread.getGameState() == SpaceTravelsThread.GameState.Pause
				|| thread.getGameState() == SpaceTravelsThread.GameState.Running)
			menu.findItem(R.id.resume).setVisible(true);
		else
			menu.findItem(R.id.resume).setVisible(false);
		return true;
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
		// menu.findItem(R.id.resume).setVisible(false);
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		wakeLock.acquire();
		thread.unpause();
	}

	/**
	 * Invoked when the Activity loses user focus.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		wakeLock.release();
		thread.pause();// pause game when Activity pauses
	}

	/**
	 * Notification that something is about to happen, to give the Activity a
	 * chance to save state.
	 * 
	 * @param outState
	 *            a Bundle into which this Activity should save its state
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// just have the View's thread save its state into our Bundle
		super.onSaveInstanceState(outState);
		thread.saveState(outState);
		Log.w(this.getClass().getName(), "SIS called");
	}
}