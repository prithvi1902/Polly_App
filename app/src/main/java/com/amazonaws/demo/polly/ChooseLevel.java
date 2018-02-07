package com.amazonaws.demo.polly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

public class ChooseLevel extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_level);

        ImageButton easy = (ImageButton) findViewById(R.id.easy_btn);
        ImageButton med = (ImageButton) findViewById(R.id.med_btn);
        ImageButton hard = (ImageButton) findViewById(R.id.hard_btn);

        easy.setOnClickListener(this);
        med.setOnClickListener(this);
        hard.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        String level=null;

        if (v.getId() == R.id.easy_btn) {
            level="easy";
        } else if (v.getId() == R.id.med_btn) {
            level="medium";
        } else if (v.getId() == R.id.hard_btn) {
            level="hard";
        }

        // This method will be executed once the timer is over
        // Start your app main activity
        Intent k = new Intent(ChooseLevel.this, MainActivity.class);
        k.putExtra("key", level);
        startActivity(k);

        // close this activity
        //finish();
    }
}
