/**
 *
 */
package com.draga.android.spaceTravels;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.draga.android.spaceTravels.Drawable.SpaceTravels;

/**
 * @author Draga
 */
public class SpaceTravelsMain extends Activity implements OnItemSelectedListener {
    private int selectedLevel = 1;

    /*
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Spinner spinner = (Spinner) findViewById(R.id.level_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.levels_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new SpaceTravelsMain());

        Button playButton = (Button) this.findViewById(R.id.playButton);
        playButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                play();
            }
        });
    }

    private void play() {
        Intent intent = new Intent(SpaceTravelsMain.this, SpaceTravels.class);
        startActivity(intent);
    }

    public void onItemSelected(AdapterView<?> parent,
                               View view, int pos, long id) {
        selectedLevel = pos + 1;
    }

    /* (non-Javadoc)
     * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
     */
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }
}
