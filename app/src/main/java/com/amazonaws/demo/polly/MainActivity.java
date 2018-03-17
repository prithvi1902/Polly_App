/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.demo.polly;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.polly.model.Voice;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener{
    
	private static final String TAG = "PollyDemo";

	// Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // Amazon Polly permissions.
    private static final String COGNITO_POOL_ID = "ap-south-1:47757fa1-db3b-4b75-a084-72b6a6482c3e";

    // Region of Amazon Polly.
    private static final Regions MY_REGION = Regions.AP_SOUTH_1;
    private static int k = 0;   //global variable to manipulate the words array
    public String[] words = {""};   //array to store the words retrieved from the server
    CognitoCachingCredentialsProvider credentialsProvider;
    MediaPlayer mediaPlayer;
    ImageButton play, prev, next, submit, home;   //listen to their clicks from the second activity
    EditText ed;
    private AmazonPollyPresigningClient client;
    private List<Voice> voices;

    //Method to handle the onClickListener for the ImageButtons
    @Override
    public void onClick(View view) {

        Play p = new Play();

        switch(view.getId()) {
            case R.id.playButton:   p.execute(view);
                                    break;

            case R.id.nextButton:   p.execute(view);
                                    break;

            case R.id.prevButton:   p.execute(view);
                                    break;

            case R.id.submit:{

                                String cword = ed.getText().toString().trim();

                                if (!(cword.equalsIgnoreCase(words[k]))) {
                                    setupPlayButton("Oops! Try Again!");
                                    ed.setText("");
                                    Toast.makeText(getApplicationContext(), "Oops! Try again!", Toast.LENGTH_SHORT).show();
                                } else if (cword.equalsIgnoreCase(words[k])) {
                                    setupPlayButton("Good Job!");
                                    ed.setText("");
                                    Toast.makeText(getApplicationContext(), "Good Job!", Toast.LENGTH_SHORT).show();
                                } else if(cword.equals("")){
                                    setupPlayButton("Type the word");
                                    Toast.makeText(getApplicationContext(), "Type the word", Toast.LENGTH_SHORT).show();
                                }
                            }
                            break;

            case R.id.home: {
                Intent k=new Intent(MainActivity.this,ChooseLevel.class);
                startActivity(k);
                }
                break;
            }
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get the available Polly voices
  //      GetPollyVoices gt=new GetPollyVoices();
  //      gt.execute();

        initPollyClient();
        setupNewMediaPlayer();

        //Get the level from the extras sent by ChooseLevel
        Bundle bundle = getIntent().getExtras();
        words = bundle.getStringArray("MyArray");

        //Create objects for the ImageButtons
        play=(ImageButton)findViewById(R.id.playButton);
        prev=(ImageButton)findViewById(R.id.prevButton);
        next=(ImageButton)findViewById(R.id.nextButton);
        submit=(ImageButton)findViewById(R.id.submit);
        home=(ImageButton)findViewById(R.id.home);
        ed = (EditText) findViewById(R.id.check_word);

        //set OnClickListeners to them
        play.setOnClickListener(this);
        prev.setOnClickListener(this);
        next.setOnClickListener(this);

        //Check for the word entered; if correct or wrong
        submit.setOnClickListener(this);
        home.setOnClickListener(this);
    }

    void initPollyClient() {
        // Initialize the Amazon Cognito credentials provider.
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                COGNITO_POOL_ID,
                MY_REGION
        );

        // Create a client that supports generation of presigned URLs.
        client = new AmazonPollyPresigningClient(credentialsProvider);
    }

    void setupNewMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                setupNewMediaPlayer();
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    void setupPlayButton(String word) {

                // Create speech synthesis request.
                SynthesizeSpeechPresignRequest synthesizeSpeechPresignRequest =
                        new SynthesizeSpeechPresignRequest()
                        // Set text to synthesize.
                        .withText(word)
                        // Set voice selected by the user.
                        .withVoiceId("Aditi")
                        // Set format to MP3.
                        .withOutputFormat(OutputFormat.Mp3);

                // Get the presigned URL for synthesized speech audio stream.
                URL presignedSynthesizeSpeechUrl =
                        client.getPresignedSynthesizeSpeechUrl(synthesizeSpeechPresignRequest);

                Log.i(TAG, "Playing speech from presigned URL: " + presignedSynthesizeSpeechUrl);

                // Create a media player to play the synthesized audio stream.
                if (mediaPlayer.isPlaying()) {
                    setupNewMediaPlayer();
                }
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                try {
                    // Set media player's data source to previously obtained URL.
                    mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString());
                } catch (IOException e) {
                    Log.e(TAG, "Unable to set data source for the media player! " + e.getMessage());
                }

                // Start the playback asynchronously (since the data source is a network stream).
                mediaPlayer.prepareAsync();
    }

/*
    private class GetPollyVoices extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (voices != null) {
                return null;
            }

            // Create describe voices request.
            DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

            DescribeVoicesResult describeVoicesResult;
            try {
                // Synchronously ask the Polly Service to describe available TTS voices.
                describeVoicesResult = client.describeVoices(describeVoicesRequest);
            } catch (RuntimeException e) {
                Log.e(TAG, "Unable to get available voices. " + e.getMessage());
                return null;
            }

            // Get list of voices from the result.
            voices = describeVoicesResult.getVoices();

            // Log a message with a list of available TTS voices.
            Log.i(TAG, "Available Polly voices: " + voices);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (voices == null) {
                return;
            }
        }
    }
*/
    //Class to call the setUpPlayButton method based on the option selected [play,prev,next]
    public class Play extends AsyncTask<View, Void, Void>{

        Intent i=new Intent(getApplicationContext(),ChooseLevel.class);
        @Override
        protected Void doInBackground(View... views) {

            if(views[0].getId() == R.id.prevButton) {
                if (k > 0) {
                    k--;
                    setupPlayButton(words[k]);
                } else if(k==words.length-1) {
                    k=0;
                    setupPlayButton("You have completed this level! Choose the next level!");
                    startActivity(i);
                } else{
                    setupPlayButton(words[0]);
                }
            }
            else if(views[0].getId()==R.id.playButton) {
                setupPlayButton(words[k]);
            }else if(views[0].getId()==R.id.nextButton) {
                if (k < words.length - 1) {
                    k++;
                    setupPlayButton(words[k]);
                } else if(k==words.length-1) {
                    k=0;
                    setupPlayButton("You have completed this level! Choose the next level!");
                    startActivity(i);
                } else{
                    setupPlayButton(words[words.length - 1]);
                }
            }
            return null;
        }
    }
}
