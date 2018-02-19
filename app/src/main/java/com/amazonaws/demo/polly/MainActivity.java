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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.polly.model.Voice;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "PollyDemo";

    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // Amazon Polly permissions.
    private static final String COGNITO_POOL_ID = "ap-south-1:47757fa1-db3b-4b75-a084-72b6a6482c3e";

    // Region of Amazon Polly.
    private static final Regions MY_REGION = Regions.AP_SOUTH_1;

    CognitoCachingCredentialsProvider credentialsProvider;

    private AmazonPollyPresigningClient client;
    private List<Voice> voices;
    MediaPlayer mediaPlayer;

    //Content Resolver
    final Uri CONTENT_URI = Uri.parse("content://com.amazonaws.demo.polly.WordProvider");

    ContentResolver resolver;

    private static int k = 0;

    public String[] words = {""};

    ImageButton play, prev, next;

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

            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (k > 0) {
                        k--;
                        setupPlayButton(words[k]);
                    } else {
                        setupPlayButton(words[0]);
                    }
                }
            });

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupPlayButton(words[k]);
                }
            });

            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (k < words.length-1 ) {
                        k++;
                        setupPlayButton(words[k]);
                    } else {
                        setupPlayButton(words[words.length - 1]);
                    }
                }
            });

            return null;
        }
    }

        public String[] getData(String level) {

            String[] projection = new String[]{"id", "word"};

            Cursor cursor = resolver.query(CONTENT_URI, projection, "level = ?", new String[]{level}, null);

            ArrayList<String> words = new ArrayList<>();

            int i = 0;

            cursor.moveToFirst();

            while(!cursor.isAfterLast()) {
                words.add(cursor.getString(cursor.getColumnIndex("word")));
                cursor.moveToNext();
            }
            cursor.close();
            return words.toArray(new String[words.size()]);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_main);

            //Get the level from the extras sent by ChooseLevel
            Bundle bundle = getIntent().getExtras();
            String level = bundle.getString("key");

            initPollyClient();
            setupNewMediaPlayer();

            resolver = getContentResolver();

            words = getData(level);

            play=(ImageButton)findViewById(R.id.playButton);
            prev=(ImageButton)findViewById(R.id.prevButton);
            next=(ImageButton)findViewById(R.id.nextButton);

            GetPollyVoices gt=new GetPollyVoices();
            gt.execute();

            EditText ed = (EditText) findViewById(R.id.check_word);
            ImageButton submit=(ImageButton)findViewById(R.id.submit);

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String cword = ed.getText().toString().trim();

                    if (cword.equals(null)) {
                        setupPlayButton("Type the word");
                        Toast.makeText(getApplicationContext(), "Type the word", Toast.LENGTH_SHORT).show();
                    } else if (cword.equalsIgnoreCase(words[k])) {
                        setupPlayButton("Good Job!");
                        ed.setText("");
                        Toast.makeText(getApplicationContext(), "Good Job!", Toast.LENGTH_LONG).show();
                    } else if(!(cword.equalsIgnoreCase(words[k]))){
                        setupPlayButton("Oops! Try Again!");
                        ed.setText("");
                        Toast.makeText(getApplicationContext(), "Try again!", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            ImageButton home=(ImageButton)findViewById(R.id.home);
            home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent k=new Intent(MainActivity.this,ChooseLevel.class);
                    startActivity(k);
                }
            });
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
                            // Set voice to Indian English Aditi.
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
    }
