package com.amazonaws.demo.polly;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChooseLevel extends Activity implements View.OnClickListener {

    public String[] words = {""};   //array to store the words retrieved from the server

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


        //Fetch the words from the server
        Fetch f=new Fetch();
        f.execute(level);
    }

    //Asynchronous class to fetch words from the server
    public class Fetch extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            try {

                URL url = new URL("http://ec2-13-126-241-54.ap-south-1.compute.amazonaws.com/test.php?name="+strings[0]);

                HttpURLConnection con=(HttpURLConnection)url.openConnection();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String inputLine;

                inputLine = in.readLine();
                words= inputLine.split(",");

            }finally{
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Intent k = new Intent(ChooseLevel.this, MainActivity.class);

            Bundle bundle = new Bundle();
            bundle.putStringArray("MyArray", words);

            k.putExtras(bundle);
            startActivity(k);
        }


    }
}
