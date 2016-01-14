package com.alcatel.mobilevoicemail;

import android.app.AlertDialog;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import com.intervigil.wave.WaveWriter;

import java.io.IOException;

public class RecordMessageActivity extends ActionBarActivity {
    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_CHANNELS_COUNT = 1;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDER_SAMPLEBITS = 16;
    int mBufferSize = 0;
    short[] mBuffer = null;

    private AudioRecord mAudioRecord = null;
    private Button mRecordButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_message);

        mRecordButton = (Button)findViewById(R.id.record_button);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });

        mBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
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
    }

    protected void startRecording() {
        mRecordButton.setText("Terminer");
        mAudioRecord.startRecording();
        Thread writerThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        writerThread.start();
    }

    protected void stopRecording() {
        mAudioRecord.stop();
        mRecordButton.setText("Enregistrer");
    }

    private void writeAudioDataToFile() {
        WaveWriter writer = new WaveWriter(Environment.getExternalStorageDirectory().getPath(),
                "test.wav", RECORDER_SAMPLERATE, RECORDER_CHANNELS_COUNT, RECORDER_SAMPLEBITS);
        try {
            writer.createWaveFile();
            while(mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
                    int numSamples = mAudioRecord.read(mBuffer, 0, mBuffer.length);
                    writer.write(mBuffer, 0, numSamples);
                    System.out.println("numSamples = " + numSamples);
            }
            writer.closeWaveFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
