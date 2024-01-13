package com.example.verifier;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {


    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SeekBar seekBar;
    private boolean isCameraInitialized = false;
    private boolean isFlashOn = false;
    private boolean isFrontCamera = false;
    private boolean isAutoFocusSupported = false;
    private boolean isTouchFocusEnabled = false;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mZoomFactor = 1.0f;

    private boolean isCaptured = false;

    private FloatingActionButton captureButton, switchCameraButton, flashButton, focusButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);


        captureButton = findViewById(R.id.captureButton);
        switchCameraButton = findViewById(R.id.switchCameraButton);
        flashButton = findViewById(R.id.flashButton);
        focusButton = findViewById(R.id.focusButton);
        seekBar = findViewById(R.id.seekBar);

      //  mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());


        captureButton.setOnClickListener(v -> captureImage());
        switchCameraButton.setOnClickListener(v -> switchCamera());
        flashButton.setOnClickListener(v -> toggleFlash());
        focusButton.setOnClickListener(v -> toggleFocus());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int progress = seekBar.getProgress();
                Log.e("Progress:" , ""+ progress);
            //    camera.startSmoothZoom((int) progress);
//                camera.zoom
                Camera.Parameters parameters = camera.getParameters();
                parameters.setZoom((camera.getParameters().getMaxZoom()/10) * (progress));
                Log.e("Progress 2 : " , ":" + (camera.getParameters().getMaxZoom()/10) * progress);
                camera.setParameters(parameters);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            mZoomFactor *= scaleFactor;

            // Limit the zoom factor to some reasonable range if needed
            // For example, you can set a minimum and maximum zoom level
            mZoomFactor = Math.max(1.0f, Math.min(mZoomFactor, getMaxZoom()));

            Camera.Parameters params = camera.getParameters();
            params.setZoom((int) (params.getMaxZoom() * mZoomFactor));
            try {
                camera.setParameters(params);
            } catch (RuntimeException e) {
                Log.e("CameraActivity", "Failed to set camera parameters: " + e.getMessage());
            }

            return true;
        }
    }

    private int getMaxZoom() {
        Camera.Parameters params = camera.getParameters();
        return params.getMaxZoom();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        mScaleGestureDetector.onTouchEvent(event);
//        return true;
//    }


    private void captureImage() {
        if (isCaptured){
            Toast.makeText(this, "Already Captured", Toast.LENGTH_SHORT).show();
        }else {
            isCaptured = true;
            if (isCameraInitialized) {
                camera.takePicture(null, null, pictureCallback);
            }
        }
    }


    private void switchCamera() {
        if (isCameraInitialized) {
            releaseCamera();
            if (isFrontCamera) {
                openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                isFrontCamera = false;
            } else {
                openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                isFrontCamera = true;
            }
        }
    }


    private void toggleFlash() {
        if (isCameraInitialized) {
            Camera.Parameters parameters = camera.getParameters();
            if (isFlashOn) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                isFlashOn = false;
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                isFlashOn = true;
            }
            camera.setParameters(parameters);
        }
    }


    private void toggleFocus() {
        if (isCameraInitialized && isAutoFocusSupported) {
            isTouchFocusEnabled = !isTouchFocusEnabled;
            if (isTouchFocusEnabled) {
                Toast.makeText(this, "Touch focus enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Touch focus disabled", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void openCamera(int cameraFacing) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                camera = Camera.open(cameraFacing);
                camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                isCameraInitialized = true;
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(parameters);
                isAutoFocusSupported = camera.getParameters().getFocusMode().contains(Camera.Parameters.FOCUS_MODE_AUTO);
                //parameters.setFocusMode("continuous-picture");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }


    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
            isCameraInitialized = false;
        }
    }


    private Camera.PictureCallback pictureCallback = (data, camera) -> {
        Toast.makeText(this, "Saving Image", Toast.LENGTH_SHORT).show();
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Toast.makeText(CameraActivity.this, "Failed to save image", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Image");
        progressDialog.show();

        try {
            // Decode the byte array to obtain a Bitmap
            Bitmap rotatedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            // Obtain the device's orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();

            // Rotate the Bitmap based on the device's orientation
            Matrix matrix = new Matrix();
            switch (rotation) {
                case Surface.ROTATION_0:
                    matrix.setRotate(90);
                    break;
                case Surface.ROTATION_90:
                    // No rotation needed
                    break;
                case Surface.ROTATION_180:
                    matrix.setRotate(270);
                    break;
                case Surface.ROTATION_270:
                    matrix.setRotate(180);
                    break;
            }

            // Apply rotation to the Bitmap
            rotatedBitmap = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), matrix, true);

            // Save the rotated Bitmap to a file
            FileOutputStream fos = new FileOutputStream(pictureFile);
            rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            // Notify the system that a new image is saved
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(pictureFile));
            sendBroadcast(mediaScanIntent);

            progressDialog.dismiss();
            // Finish the activity with the result
            Intent resultIntent = new Intent();
            resultIntent.putExtra("file", pictureFile.getAbsolutePath());
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (IOException e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(CameraActivity.this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    };


    private File getOutputMediaFile() {
        File mediaStorageDir = new File(getExternalFilesDir(null), "CameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }


        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".png");
        return mediaFile;
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }


    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (isCameraInitialized) {
            if (surfaceHolder.getSurface() == null) {
                return;
            }


            try {
                camera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        releaseCamera();
    }
}
