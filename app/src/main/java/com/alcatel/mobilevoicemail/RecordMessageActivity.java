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
import android.widget.Button;
import android.widget.Toast;

import com.alcatel.mobilevoicemail.opentouch.Identifier;
import com.intervigil.wave.WaveWriter;

import java.io.File;
import java.io.IOException;

public class RecordMessageActivity extends ActionBarActivity {
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_CHANNELS_COUNT = 1;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDER_SAMPLEBITS = 16;

    int mBufferSize = 0;
    short[] mBuffer = null;
    private AudioRecord mAudioRecord = null;
    Boolean mRecording = false;
    private Button mRecordButton = null;
    Thread mWriterThread;
    private LocalVoicemail mRecordedVoicemail;

    private BroadcastReceiver mMessageSentReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_message);

        mRecordButton = (Button)findViewById(R.id.record_button);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecording) {
                    stopRecording();
                } else {
                    startRecording();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageSentReceiver);
    }

    protected void startRecording() {
        mRecordButton.setText(R.string.finish);

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

        mRecordedVoicemail = new LocalVoicemail();
        mRecordedVoicemail.setDestination(new Identifier("tpvoip3"));
        Log.i(getClass().getSimpleName(), "Will write into wave file " + mRecordedVoicemail.getPath());

        mAudioRecord.startRecording();
        mRecording = true;
        Log.i(getClass().getSimpleName(), "Started recording");

        mWriterThread = new Thread(new Runnable() {
            public void run() {
                WaveWriter writer = new WaveWriter(new File(mRecordedVoicemail.getPath()),
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
        mWriterThread.start();
    }

    protected void stopRecording() {
        mAudioRecord.stop();
        mAudioRecord.release();
        mRecording = false;
        mRecordButton.setText(R.string.save);
        Log.i(getClass().getSimpleName(), "Stopped recording");

        try {
            mWriterThread.join(); // wait for thread to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // For linking the app to the account
        //DropboxClient.getInstance().startOAuth2Authentication(RecordMessageActivity.this);

        Log.d(getClass().getSimpleName(), "Will send message to Dropbox");
        DropboxClient.getInstance().uploadVoicemail(mRecordedVoicemail);
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

            DropboxClient.getInstance().uploadVoicemail(mRecordedVoicemail);
        }
    }
}
