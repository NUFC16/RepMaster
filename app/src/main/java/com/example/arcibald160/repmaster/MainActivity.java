package com.example.arcibald160.repmaster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView showValue;
    RepManager mRepManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showValue = (TextView) findViewById(R.id.rep_number);
        final Button toggleButton = (Button) findViewById(R.id.toggle_btn);

        mRepManager = new RepManager(this);

        // inital state
        toggleButton.setText("START");
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggleButton.getText().toString() == "START") {
                    mRepManager.register();
                    toggleButton.setText("END");
                } else {
                    mRepManager.unregister();
                    toggleButton.setText("START");
                    showValue.setText(String.format("%d", mRepManager.getReps()));
                }
            }
        });
    }
}
