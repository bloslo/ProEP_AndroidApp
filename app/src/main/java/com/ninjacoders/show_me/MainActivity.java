package com.ninjacoders.show_me;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.faucamp.simplertmp.RtmpHandler;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import java.io.IOException;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity implements RtmpHandler.RtmpListener,
        SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener {

    private static final String TAG = "show-me";
    private Button btnStream;
    private Button btnSwitchCamera;

    private SharedPreferences sp;
    private String rtmpUrl = "rtmp://40.68.124.79:1984/show/stream";

    private SrsPublisher mPublisher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        // response screen rotation event
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        // restore data
        sp = getSharedPreferences("show-me", MODE_PRIVATE);
        rtmpUrl = sp.getString("rtmpUrl", rtmpUrl);

        // initialize url
        final EditText efu = (EditText) findViewById(R.id.url);
        efu.setText(rtmpUrl);

        btnStream = (Button) findViewById(R.id.stream);
        btnSwitchCamera = (Button) findViewById(R.id.switch_cam);

        mPublisher = new SrsPublisher((SrsCameraView) findViewById(R.id.surface_view_camera));
        mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
        mPublisher.setRtmpHandler(new RtmpHandler(this));
        mPublisher.setRecordHandler(new SrsRecordHandler(this));
        mPublisher.setPreviewResolution(640, 360);
        mPublisher.setOutputResolution(360, 640);
        mPublisher.setVideoHDMode();
        mPublisher.startCamera();

        btnStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnStream.getText().toString().contentEquals("stream")) {
                    rtmpUrl = efu.getText().toString();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("rtmpUrl", rtmpUrl);
                    editor.apply();

                    mPublisher.switchToSoftEncoder();
                    mPublisher.startPublish(rtmpUrl);
                    mPublisher.startCamera();

                    Toast.makeText(getApplicationContext(), "Use soft encoder", Toast.LENGTH_SHORT).show();

                    btnStream.setText("stop");
                } else if (btnStream.getText().toString().contentEquals("stop")) {
                    mPublisher.stopPublish();
                    btnStream.setText("stream");
                }
            }
        });

        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPublisher.switchCameraFace((mPublisher.getCamraId() + 1) % Camera.getNumberOfCameras());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPublisher.stopPublish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPublisher.stopEncode();
        mPublisher.setScreenOrientation(newConfig.orientation);
        if (btnStream.getText().toString().contentEquals("stop")) {
            mPublisher.startEncode();
        }
        mPublisher.startCamera();
    }

    private void handleException(Exception e) {
        try {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            mPublisher.stopPublish();
            btnStream.setText("stream");
        } catch (Exception e1) {
            Log.e(TAG, e1.getMessage(), e1.getCause());
        }
    }

    // Implementation of SrsRtmpListener.

    @Override
    public void onRtmpConnecting(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpConnected(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoStreaming() {
    }

    @Override
    public void onRtmpAudioStreaming() {
    }

    @Override
    public void onRtmpStopped() {
        Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpDisconnected() {
        Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {
        Log.i(TAG, String.format("Output Fps: %f", fps));
    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Video bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Video bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Audio bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Audio bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpSocketException(SocketException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        handleException(e);
    }

    // Implementation of SrsEncodeHandler

    @Override
    public void onNetworkWeak() {
        Toast.makeText(getApplicationContext(), "Network weak", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNetworkResume() {
        Toast.makeText(getApplicationContext(), "Network resume", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRecordPause() {
        Toast.makeText(getApplicationContext(), "Record paused", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordResume() {
        Toast.makeText(getApplicationContext(), "Record resumed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordStarted(String msg) {
        Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordFinished(String msg) {
        Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRecordIOException(IOException e) {
        handleException(e);
    }
}
