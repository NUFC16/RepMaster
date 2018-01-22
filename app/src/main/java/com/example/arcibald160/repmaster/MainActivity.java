package com.example.arcibald160.repmaster;

import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView showValue, showExercise;
    RepManager mRepManager = null;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        showValue = (TextView) findViewById(R.id.rep_number);
        showExercise = (TextView) findViewById(R.id.show_exercise);

        final Button toggleButton = (Button) findViewById(R.id.toggle_btn);
        progressBar = (ProgressBar)  findViewById(R.id.loadingCircle);

        mRepManager = new RepManager(this, showValue);

        // inital state
        toggleButton.setText("START");
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggleButton.getText().toString() == "START") {
                    showValue.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    mRepManager.register();
                    toggleButton.setText("END");
                } else {
                    mRepManager.unregister();
                    showValue.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    toggleButton.setText("START");
                    showExercise.setText(mRepManager.getExcersise());
                    mRepManager.makeFilesVisibleOnPC(MainActivity.this);

                }
            }
        });
    }
}
