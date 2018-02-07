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
import android.content.Context;
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
import android.view.View;
import android.view.ViewGroup;
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

public class MainActivity extends Activity{

    private static final String TAG = "PollyDemo";
    private static final String KEY_SELECTED_VOICE_POSITION = "SelectedVoicePosition";
    private static final String KEY_VOICES = "Voices";
    private static final String KEY_SAMPLE_TEXT = "SampleText";

    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // Amazon Polly permissions.
    private static final String COGNITO_POOL_ID = "ap-south-1:47757fa1-db3b-4b75-a084-72b6a6482c3e";

    // Region of Amazon Polly.
    private static final Regions MY_REGION = Regions.AP_SOUTH_1;

    CognitoCachingCredentialsProvider credentialsProvider;

    private AmazonPollyPresigningClient client;
    private List<Voice> voices;
    private Spinner voicesSpinner;
    private int selectedPosition;
    MediaPlayer mediaPlayer;

    private class SpinnerVoiceAdapter extends BaseAdapter {
        
		private LayoutInflater inflater;
        private List<Voice> voices;

        SpinnerVoiceAdapter(Context ctx, List<Voice> voices) {
            this.inflater = LayoutInflater.from(ctx);
            this.voices = voices;
        }

        @Override
        public int getCount() {
            return voices.size();
        }

        @Override
        public Object getItem(int position) {
            return voices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.voice_spinner_row, parent, false);
            }
            Voice voice = voices.get(position);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.voiceName);
            nameTextView.setText(voice.getName());

            TextView languageCodeTextView = (TextView) convertView.findViewById(R.id.voiceLanguageCode);
            languageCodeTextView.setText(voice.getLanguageName() +
                    " (" + voice.getLanguageCode() + ")");

            return convertView;
        }
    }

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

            voicesSpinner.setAdapter(new SpinnerVoiceAdapter(MainActivity.this, voices));

 //         findViewById(R.id.voicesProgressBar).setVisibility(View.INVISIBLE);
            
			voicesSpinner.setVisibility(View.VISIBLE);

            voicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (view == null) {
                        return;
                    }
        }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            // Restore previously selected voice (e.g. after screen orientation change).
            voicesSpinner.setSelection(selectedPosition);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupVoicesSpinner();
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
		//TODO setupPlayButton();

        //Connect to Database
        /*DataBaseAccess databaseAccess = DataBaseAccess.getInstance(this);
        databaseAccess.open();

        //Retrieve the set of words from the table based on the level
        List<String> words = databaseAccess.getData(level);

        //Close the connection
        databaseAccess.close();*/

		Uri CONTENT_URI=Uri.parse("content://com.amazonaws.demo.polly.spellit.wordlist");

        List<String> words= new ArrayList<String>();
		
		try {
            Cursor cursor = getContentResolver().query(CONTENT_URI, word , level, null, null, null);

            int j=0;
            if (cursor != null) {

                while (cursor.moveToNext()) {
                    words.set(j++, cursor.getString(0));
                    //words.append(cursor.getInt(0) + "\t" + cursor.getString(1) + "\t " + cursor.getString(2) + "\n");
                }
            }
            cursor.close();

        }
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(),e.getMessage().toString(),Toast.LENGTH_SHORT).show();
        }		

        //int k=0;
        for(int k=0;k<words.size();k++) {

            setupPlayButton(words.get(k));

            /*EditText ed = (EditText) findViewById(R.id.check_word);

            ed.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    String cword = ed.getText().toString();

                    if (cword.equals(null)) {
                        setupPlayButton("Type the word");
                        Toast.makeText(getApplicationContext(), "Type the word", Toast.LENGTH_SHORT);
                    } else if (cword.equalsIgnoreCase(words.get(0))) {
                        setupPlayButton("Good Job!");
                        ed.setText("");
                        Toast.makeText(getApplicationContext(), "Good Job!", Toast.LENGTH_LONG).show();
                    } else {
                        setupPlayButton("Oops! Try Again!");
                        ed.setText("");
                        Toast.makeText(getApplicationContext(), "Try again!", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        ImageButton play=(ImageButton)findViewById(R.id.playButton);
            play.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                        setupPlayButton(words.get(0));

                                            ImageButton prev = (ImageButton) findViewById(R.id.prevButton);
                                            ImageButton next = (ImageButton) findViewById(R.id.nextButton);

                                            prev.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Toast.makeText(getApplicationContext(), "Next Word", Toast.LENGTH_SHORT).show();
                                                    //k--;
                                                }
                                            });

                                            next.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Toast.makeText(getApplicationContext(), "Next Word", Toast.LENGTH_SHORT).show();
                                                    //   k++;
                                                }
                                            });

                                        }
                                    });
        }//close for
*/
        //
        //next.setOnClickListener((View.OnClickListener) this);

        //Button btn = (Button) findViewById(R.id.check_btn);
        //btn.setVisibility(View.INVISIBLE);


        //EditText ed=(EditText)findViewById(R.id.check_word);
        //ed.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt(KEY_SELECTED_VOICE_POSITION, voicesSpinner.getSelectedItemPosition());
        outState.putSerializable(KEY_VOICES, (Serializable) voices);
    //  outState.putString(KEY_SAMPLE_TEXT, sampleTextEditText.getText().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        selectedPosition = savedInstanceState.getInt(KEY_SELECTED_VOICE_POSITION);
        voices = (List<Voice>) savedInstanceState.getSerializable(KEY_VOICES);

        //String sampleText = savedInstanceState.getString(KEY_SAMPLE_TEXT);
        //if (sampleText.isEmpty()) {
        //    defaultTextButton.setVisibility(View.GONE);
        //} else {
        //    sampleTextEditText.setText(sampleText);
        //    defaultTextButton.setVisibility(View.VISIBLE);
        //}
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
                //playButton.setEnabled(true);
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                return false;
            }
        });
    }

    void setupVoicesSpinner() {
        voicesSpinner = (Spinner) findViewById(R.id.voicesSpinner);
       // findViewById(R.id.voicesProgressBar).setVisibility(View.VISIBLE);

        // Asynchronously get available Polly voices.

        new GetPollyVoices().execute();
    }

    void setupPlayButton(String word) {

                Voice selectedVoice = (Voice) voicesSpinner.getSelectedItem();

                // Create speech synthesis request.
                SynthesizeSpeechPresignRequest synthesizeSpeechPresignRequest =
                        new SynthesizeSpeechPresignRequest()
                        // Set text to synthesize.
                        .withText(word)
                        // Set voice selected by the user.
                        .withVoiceId(selectedVoice.getId())
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
