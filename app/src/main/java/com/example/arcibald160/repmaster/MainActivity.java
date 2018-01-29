package com.example.arcibald160.repmaster;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    TextView showCountedRepNumber, showExercise;
    RepManager mRepManager = null;
    ProgressBar progressBar;

    String username, choosenExerise, realRepNumber;

    ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    CountDownTimer timer = new CountDownTimer(5000,1200) {
        @Override
        public void onTick(long l) {
            // show loading bar
            progressBar.setVisibility(View.VISIBLE);
            // start tone
            tone.startTone(ToneGenerator.TONE_DTMF_3, 500);
        }

        @Override
        public void onFinish() {
            // hide loading bar
            progressBar.setVisibility(View.INVISIBLE);
            // last beep of start tone
            tone.startTone(ToneGenerator.TONE_DTMF_P, 1000);
            // show label for counting reps after sound is finished
            showCountedRepNumber.setVisibility(View.VISIBLE);

            mRepManager.register(username, choosenExerise, realRepNumber);
            // enable stop button for manual stop
            Button toggleButton = (Button) findViewById(R.id.toggle_btn);
            toggleButton.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // set initial values
        username = getString(R.string.default_username);
        choosenExerise = getString(R.string.default_exercise);
        realRepNumber = getString(R.string.default_realRepNumber);

        // permission for writing data
        int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        // current counted rep number label
        showCountedRepNumber = (TextView) findViewById(R.id.rep_number);
        // show exercise label
        showExercise = (TextView) findViewById(R.id.show_exercise);
        // loading circle
        progressBar = (ProgressBar)  findViewById(R.id.loadingCircle);
        // start/stop button
        final Button toggleButton = (Button) findViewById(R.id.toggle_btn);

        mRepManager = new RepManager(this, showCountedRepNumber);

        // if value is equal to desired value than stop
        showCountedRepNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (realRepNumber.equals(showCountedRepNumber.getText().toString())) {
                    finishCounting();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggleButton.getText().toString() == getString(R.string.start)) {
                    showCountedRepNumber.setVisibility(View.INVISIBLE);
                    toggleButton.setVisibility(View.INVISIBLE);
                    timer.start();
                    toggleButton.setText(getString(R.string.end));
                } else {
                    // manually stop
                    finishCounting();
                }
            }
        });
    }


    public void finishCounting() {
        // notification tone for end
        tone.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER, 3000);
        mRepManager.unregister();

        // set button text to start
        final Button toggleButton = (Button) findViewById(R.id.toggle_btn);
        toggleButton.setText(getString(R.string.start));

        // show calculated exercise
        showExercise.setText(mRepManager.getExcersise());
        // make experiment data appear on PC
        mRepManager.makeFilesVisibleOnPC(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        This function is used for setting expected values of the experiment such as:
        1. participant username
        2. type of exercise
        3. desired number of reps
         */
        switch(item.getItemId()) {
            case R.id.settings_button:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                // for custom layout
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                // Pass null as the parent view because its going in the dialog layout
                View mView = inflater.inflate(R.layout.settings_main, null);

                builder.setTitle(R.string.settings);
                // settings: username
                final TextView user = (TextView) mView.findViewById(R.id.settings_username);
                // settings: type of exercise
                final Spinner exercise = (Spinner) mView.findViewById(R.id.settings_exercise);
                // settings: desired number of reps
                final TextView rNumber = (TextView) mView.findViewById(R.id.settings_realRepNumber);

                builder.setView(mView);

                // get current username
                if (username != getString(R.string.default_username)) {
                    user.setText(username);
                }
                // get current desired rep number
                if (realRepNumber != getString(R.string.default_realRepNumber)) {
                    rNumber.setText(realRepNumber);
                }
                // get current type of exercise
                if (choosenExerise != getString(R.string.default_exercise)) {
                    int spinnerPosition = Arrays.asList(getResources().getStringArray(R.array.exercise_array)).indexOf(choosenExerise);
                    exercise.setSelection(spinnerPosition);
                }

                // apply
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        username = (user.getText().toString().equals("")) ? username : user.getText().toString();
                        choosenExerise = exercise.getSelectedItem().toString();
                        realRepNumber = rNumber.getText().toString();
                    }
                });
                // dismiss
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
        return true;
    }
}
