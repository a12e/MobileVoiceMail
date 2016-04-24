package com.alcatel.mobilevoicemail;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.alcatel.mobilevoicemail.opentouch.Identifier;
import com.intervigil.wave.WaveWriter;

import java.io.File;
import java.io.IOException;

public class RecordMessageActivity extends ActionBarActivity {
    public static final String INTENT_EXTRA_DESTINATION = "destination";

    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_CHANNELS_COUNT = 1;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDER_SAMPLEBITS = 16;

    int mBufferSize = 0;
    short[] mBuffer = null;
    private AudioRecord mAudioRecord = null;
    Boolean mIsRecording = false;
    Thread mWavWriterThread;
    private LocalVoicemail mCurrentlyRecordedVoicemail;

    private BroadcastReceiver mMessageSentReceiver;
    private BroadcastReceiver mMessageSentErrorReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_message);

        ImageButton recordButton = (ImageButton)findViewById(R.id.stop_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording) {
                    stopRecording();
                } else {
                    throw new RuntimeException("Stop button pressed but recording was not started");
                }
            }
        });

        // Lorsque le message a été correctement envoyé à l'OpenTouch on affiche une petite
        // bulle et on ferme cette activité
        registerReceiver(mMessageSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(App.getContext(), R.string.message_sent, Toast.LENGTH_SHORT).show();
                RecordMessageActivity.this.finish();
            }
        }, new IntentFilter("MESSAGE_SENT"));
        registerReceiver(mMessageSentErrorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AlertDialog.Builder ab = new AlertDialog.Builder(RecordMessageActivity.this);
                ab.setTitle(R.string.message_sent_error_title);
                ab.setMessage(R.string.message_sent_error + "\n" + intent.getStringExtra("message"));
                ab.show();
                RecordMessageActivity.this.finish();
            }
        }, new IntentFilter("MESSAGE_SENT_ERROR"));

        // Récupération du destinataire et création du message
        Identifier destination = (Identifier)getIntent().getSerializableExtra(INTENT_EXTRA_DESTINATION);
        if(destination == null) throw new NullPointerException("No destination !");
        mCurrentlyRecordedVoicemail = new LocalVoicemail();
        mCurrentlyRecordedVoicemail.setDestination(destination);
        setTitle(destination.getDisplayName());

        // Démarre l'enregistrement dès que l'activité est crée
        startRecording();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageSentReceiver);
        unregisterReceiver(mMessageSentErrorReceiver);
    }

    protected void startRecording() {
        mBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING);

        mAudioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                mBufferSize);
        if(mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Cannot start audio recording").create();
            alertDialog.show();
        }
        mBuffer = new short[mBufferSize];

        Log.i(getClass().getSimpleName(), "Will write into wave file " + mCurrentlyRecordedVoicemail.getPath());

        mAudioRecord.startRecording();
        mIsRecording = true;
        Log.i(getClass().getSimpleName(), "Started recording");

        mWavWriterThread = new Thread(new Runnable() {
            public void run() {
                WaveWriter writer = new WaveWriter(new File(mCurrentlyRecordedVoicemail.getPath()),
                        RECORDER_SAMPLERATE, RECORDER_CHANNELS_COUNT, RECORDER_SAMPLEBITS);
                try {
                    writer.createWaveFile();
                    Log.i(getClass().getSimpleName(), "Created wave file");
                    while(mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
                        int numSamples = mAudioRecord.read(mBuffer, 0, mBuffer.length);

                        if(numSamples == AudioRecord.ERROR_INVALID_OPERATION) {
                            Log.e(getClass().getSimpleName(), "AudioRecord.ERROR_INVALID_OPERATION (not initialized)");
                        }
                        else if(numSamples == AudioRecord.ERROR_BAD_VALUE) {
                            Log.e(getClass().getSimpleName(), "AudioRecord.ERROR_BAD_VALUE");
                        }
                        else {
                            writer.write(mBuffer, 0, numSamples);
                        }
                    }
                    writer.closeWaveFile();
                    Log.i(getClass().getSimpleName(), "Finished writing into wave file");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "AudioRecorder Thread");
        mWavWriterThread.start();
    }

    protected void stopRecording() {
        mAudioRecord.stop();
        mAudioRecord.release();
        mIsRecording = false;
        Log.i(getClass().getSimpleName(), "Stopped recording");

        try {
            mWavWriterThread.join(); // wait for thread to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // For linking the app to the account
        //DropboxClient.getInstance().startOAuth2Authentication(RecordMessageActivity.this);

        Log.d(getClass().getSimpleName(), "Will send message to Dropbox");
        DropboxClient.getInstance().uploadVoicemail(mCurrentlyRecordedVoicemail);
    }

    protected void onResume() {
        super.onResume();

        if (DropboxClient.getInstance().getApi().getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                DropboxClient.getInstance().getApi().getSession().finishAuthentication();

                String accessToken = DropboxClient.getInstance().getApi().getSession().getOAuth2AccessToken();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }

            DropboxClient.getInstance().uploadVoicemail(mCurrentlyRecordedVoicemail);
        }
    }
}
