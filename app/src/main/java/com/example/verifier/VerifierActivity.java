package com.example.verifier;

import static android.graphics.Bitmap.createBitmap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.gif.GifDecoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class VerifierActivity extends BaseActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 200;
    private static final int CAMERA_NORMAL_REQUEST_CODE = 300;

    private static final int CAMERA_CAPTURE_NORM = 400;
    private static final int CAMERA_CAPTURE_NORM2 = 401;

    private static final int CAMERA_CAPTURE_COPY = 500;
    private static final int REQUEST_SELECT_PICTURE = 1;

    private ArrayList<DataGrade> arrayList = new ArrayList<>();
    private ArrayList<String> titles  = new ArrayList<>(Arrays.asList( "Decode", "Contrast", "Overall Quality", "Axial Nonuniformity", "Modulation", "Grid Nonuniformity", "Unused Error Correction", "Fixed Pattern Damage", "NA", "Aperture"));
    private ArrayList<String> barTitles  = new ArrayList<>(Arrays.asList( "Overall Quality", "Decode", "Symbol Contrast",  "Minimal Reflectance", "Minimal Edge Contrast", "Modulation", "Defects", "Decodability", "Additional Requirements"));

    private GradeAdapter adapter;

    private ConstraintLayout layoutHead;

    private Bitmap imageBitmap;

    private ImageView imageView;
    private ExtendedFloatingActionButton fab, fabPick, fabTest, fabBar, fabNorm, fabWhite, fabCopy;
    private RecyclerView recyclerView;

    private TextView tvHeader;
    private Uri uriNorm1, uriNorm2, uriCopy1, uriCopy2, uriCopy3;
    private int copyCount = 0;

    public static String TAG = "SOME";
    private boolean isBar = false;
    private ConstraintLayout layoutMain;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifier);

        imageView = findViewById(R.id.activity_verifier_imageview);
        fab = findViewById(R.id.activity_verifier_fab);
        fabBar = findViewById(R.id.activity_verifier_fabBar);
        recyclerView = findViewById(R.id.activity_verifier_rcView);
        tvHeader = findViewById(R.id.activity_verifier_tvTitle);
        layoutHead = findViewById(R.id.activity_verifier_layouthead);
        fabPick = findViewById(R.id.activity_verifier_fabPick);
        fabTest = findViewById(R.id.activity_verifier_fabTest);
        fabNorm = findViewById(R.id.activity_verifier_fabNorm);
        fabWhite = findViewById(R.id.activity_verifier_fabWhite);
        layoutMain = findViewById(R.id.activity_verifier_layoutMain);
        fabCopy = findViewById(R.id.activity_verifier_fabCopy);

        adapter = new GradeAdapter(this, arrayList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        fab.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(VerifierActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(VerifierActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                isBar = false;
                captureImage();
            }
        });
        fabBar.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(VerifierActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(VerifierActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                isBar = true;
                captureImage();
            }
        });
        fabNorm.setOnClickListener(view -> {
            if (uriNorm1 == null){
              //  toast("Take Plane Uniform Picture first");
                Snackbar.make(layoutMain, "Take Plane Uniform Picture first", Snackbar.LENGTH_SHORT).setAction("Okay", null).show();
            }else {
                startActivityForResult(new Intent(VerifierActivity.this, CameraActivity.class), CAMERA_CAPTURE_NORM2);
            }
        });
        fabWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(VerifierActivity.this, CameraActivity.class), CAMERA_CAPTURE_NORM);
            }
        });
        fabCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyCount = 0;
                startActivityForResult(new Intent(VerifierActivity.this, CameraActivity.class), CAMERA_CAPTURE_COPY);
            }
        });



        fabPick.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(VerifierActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(VerifierActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                selectPictureFromGallery(this);
            }
        });
        fabTest.setOnClickListener(view -> {

           // sendToIon(new File(getImageUri(BitmapFactory.decodeResource(context.getResources(), R.drawable.test)).getPath()));

            androidx.appcompat.widget.PopupMenu popupMenu = new PopupMenu(VerifierActivity.this, fabTest);


            // Inflating popup menu from popup_menu.xml file
            popupMenu.getMenuInflater().inflate(R.menu.menu_tests, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                // Toast message on menu item clicked
                CharSequence title = menuItem.getTitle();
                if ("Test 1".contentEquals(title)) {
                    imageView.setImageDrawable(getDrawable(R.drawable.test));
                    layoutHead.setVisibility(View.VISIBLE);
                    sendToIon(new File(getImageUri(BitmapFactory.decodeResource(context.getResources(), R.drawable.test)).getPath()));
                } else if ("Test 2".contentEquals(title)) {
                    imageView.setImageDrawable(getDrawable(R.drawable.test2));
                    layoutHead.setVisibility(View.VISIBLE);
                    sendToIon(new File(getImageUri(BitmapFactory.decodeResource(context.getResources(), R.drawable.test2)).getPath()));
                } else if ("Test 3".contentEquals(title)) {
                    imageView.setImageDrawable(getDrawable(R.drawable.test3));
                    layoutHead.setVisibility(View.VISIBLE);
                    sendToIon(new File(getImageUri(BitmapFactory.decodeResource(context.getResources(), R.drawable.test3)).getPath()));
                } else if ("Test 4".contentEquals(title)) {
                    imageView.setImageDrawable(getDrawable(R.drawable.test4));
                    layoutHead.setVisibility(View.VISIBLE);
                    sendToIonBar(new File(getImageUri(BitmapFactory.decodeResource(context.getResources(), R.drawable.test4)).getPath()));
                }
                return true;
            });
            // Showing the popup menu
            popupMenu.show();

        });
    }


    private void captureImage() {
        if (tinyDB.objectExists(CAMERA_TYPE)){
            if (tinyDB.getString(CAMERA_TYPE).equals("Normal")){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_NORMAL_REQUEST_CODE);
            }else {

                Intent intent = new Intent(VerifierActivity.this, CameraActivity.class);
                startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }
        }else {

            Intent intent = new Intent(VerifierActivity.this, CameraActivity.class);
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }

    }

    public static Bitmap cropCenter(Bitmap source) {
        // Calculate the new dimensions
        int originalWidth = source.getWidth();
        int originalHeight = source.getHeight();

        int newWidth = originalWidth * 2 / 3;  // Crop 2/3 of the original width
        int newHeight = originalHeight / 2;   // Crop half of the original height

        // Calculate the starting point for cropping
        int startX = (originalWidth - newWidth) / 2;
        int startY = originalHeight / 4;  // Start from half the height

        // Create a new cropped bitmap
        Bitmap croppedBitmap = createBitmap(source, startX, startY, newWidth, newHeight);

        // Return the cropped bitmap
        return croppedBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*
        if (imageBitmap == null){
            imageView.setImageDrawable(getDrawable(R.drawable.baseline_image_24));
            layoutHead.setVisibility(View.GONE);
            tvHeader.setText("Scan New Image");
        }else {
            layoutHead.setVisibility(View.VISIBLE);
        }*/

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                layoutHead.setVisibility(View.VISIBLE);
                process(data);
            } else if (resultCode == RESULT_CANCELED) {
                layoutHead.setVisibility(View.GONE);
                tvHeader.setText("Scan New Image");
                Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
            } else {
                tvHeader.setText("Scan New Image");
                layoutHead.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }else if(requestCode == CAMERA_NORMAL_REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                layoutHead.setVisibility(View.VISIBLE);

                imageBitmap = (Bitmap) data.getExtras().get("data");
                Uri imageUri;
                File imageFile;

                if (tinyDB.getString(CROP_CENTER).equals("true")){
                    dlog("Cropping");
                    Bitmap cBitmap = cropCenter(imageBitmap);
                    imageUri = getImageUri(cBitmap);
                }else {
                    dlog("Cropping not");
                    imageUri = getImageUri(imageBitmap);
                }
                imageFile = new File(imageUri.getPath());

                Log.e(TAG, imageUri.getPath());
                imageView.setImageURI(imageUri);


                if (isBar){
                    sendToIonBar(imageFile);
                }else {
                   sendToIon(imageFile);
                }
                //   sendFile(imageFile.getAbsolutePath());
                //  sendImageToAPI(imageFile);
            } else if (resultCode == RESULT_CANCELED) {
                layoutHead.setVisibility(View.GONE);
                tvHeader.setText("Scan New Image");
                Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
            } else {
                tvHeader.setText("Scan New Image");
                layoutHead.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == CAMERA_CAPTURE_NORM) {
            if (resultCode == RESULT_OK) {
                layoutHead.setVisibility(View.VISIBLE);
                String path = data.getStringExtra("file");
                File file = new File(path);
                if (tinyDB.getString(CROP_CENTER).equals("true")){
                    Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                    Bitmap cbmp = cropCenter(bmp);
                    uriNorm1 = getImageUri(cbmp);
                }else {
                    uriNorm1 = Uri.fromFile(file);
                }

                dlog(path);
              //  startActivityForResult(new Intent(VerifierActivity.this, CameraActivity.class), CAMERA_CAPTURE_NORM2);
              //  process(data);
            } else if (resultCode == RESULT_CANCELED) {
                layoutHead.setVisibility(View.GONE);
                tvHeader.setText("Scan New Image");
                Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
            } else {
                tvHeader.setText("Scan New Image");
                layoutHead.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == CAMERA_CAPTURE_NORM2) {
            if (resultCode == RESULT_OK) {
                layoutHead.setVisibility(View.VISIBLE);
                String path = data.getStringExtra("file");
                File file = new File(path);

                if (tinyDB.getString(CROP_CENTER).equals("true")){
                    Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                    Bitmap cbmp = cropCenter(bmp);
                    uriNorm2 = getImageUri(cbmp);
                }else {
                    uriNorm2 = Uri.fromFile(file);
                }

                File file1 = new File(uriNorm1.getPath());
                File file2 = new File(uriNorm2.getPath());
                Bitmap bmp1 = BitmapFactory.decodeFile(file1.getAbsolutePath());
                Bitmap bmp2 = BitmapFactory.decodeFile(file2.getAbsolutePath());
                imageView.setImageURI(uriNorm2);
                if (bmp1.getWidth() == bmp2.getWidth() && bmp2.getHeight() == bmp2.getHeight()){
                    sendToIonNorm(file1, file2);
                }else {
                    toast("Images are not of same size");
                }
                dlog(path);
                //sendToIonNorm();
                //  process(data);
            } else if (resultCode == RESULT_CANCELED) {
                layoutHead.setVisibility(View.GONE);
                tvHeader.setText("Scan New Image");
                Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
            } else {
                tvHeader.setText("Scan New Image");
                layoutHead.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == CAMERA_CAPTURE_COPY) {
            if (resultCode == RESULT_OK) {
                layoutHead.setVisibility(View.VISIBLE);
                String path = data.getStringExtra("file");
                File file = new File(path);
                switch (copyCount){
                    case 0:
                        if (tinyDB.getString(CROP_CENTER).equals("true")){
                            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                            Bitmap cbmp = cropCenter(bmp);
                            uriCopy1 = getImageUri(cbmp);
                        }else {
                            uriCopy1 = Uri.fromFile(file);
                        }
                        dlog(path);
                        copyCount++;
                        startActivityForResult(new Intent(VerifierActivity.this, CameraActivity.class), CAMERA_CAPTURE_COPY);
                        //  process(data);
                        break;
                    case 1:
                        if (tinyDB.getString(CROP_CENTER).equals("true")){
                            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                            Bitmap cbmp = cropCenter(bmp);
                            uriCopy2 = getImageUri(cbmp);
                        }else {
                            uriCopy2 = Uri.fromFile(file);
                        }
                        dlog(path);
                        copyCount++;
                        startActivityForResult(new Intent(VerifierActivity.this, CameraActivity.class), CAMERA_CAPTURE_COPY);
                        //  process(data);
                        break;

                    case 2:
                        if (tinyDB.getString(CROP_CENTER).equals("true")){
                            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                            Bitmap cbmp = cropCenter(bmp);
                            uriCopy3 = getImageUri(cbmp);
                        }else {
                            uriCopy3 = Uri.fromFile(file);
                        }
                        dlog(path);
                        copyCount++;
                       // startActivityForResult(new Intent(VerifierActivity.this, CameraActivity.class), CAMERA_CAPTURE_COPY);
                        //  process(data);
                        File file1, file2, file3;
                        file1 = new File(uriCopy1.getPath());
                        file2 = new File(uriCopy2.getPath());
                        file3 = new File(uriCopy3.getPath());
                        imageView.setImageURI(uriCopy3);
                        sendtoIonCopy(file1, file2, file3);
                        break;

                    default:
                        // DO nothing
                        break;
                }

            } else if (resultCode == RESULT_CANCELED) {
                layoutHead.setVisibility(View.GONE);
                tvHeader.setText("Scan New Image");
                Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
            } else {
                tvHeader.setText("Scan New Image");
                layoutHead.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == REQUEST_SELECT_PICTURE && resultCode == RESULT_OK && data != null) {
            layoutHead.setVisibility(View.VISIBLE);
            process(data);
        }else {
            layoutHead.setVisibility(View.GONE);
            toast("Invalid Image");
        }
    }

    private void process(Intent data){
        String path = data.getStringExtra("file");
        File file = new File(path);
        File imageFile;

        Uri uri;

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        if (tinyDB.getString(CROP_CENTER).equals("true")){
            Bitmap cBitmap = cropCenter(bitmap);
            uri = getImageUri(cBitmap);
            imageFile = new File(uri.getPath());
        }else {
            uri = Uri.fromFile(file);
            imageFile = new File(uri.getPath());
        }
        dlog(path);


        imageView.setImageURI(uri);

        if (isBar){
            sendToIonBar(imageFile);
        }else {
            sendToIon(imageFile);
        }




        // imageView.setImageURI(Uri.fromFile(new File(data.getStringExtra("file"))));

      //  Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
       // imageView.setImageBitmap(bitmap);

      //  imageBitmap = (Bitmap) data.getExtras().get("data");
        // imageView.setImageBitmap(imageBitmap);

        // Get the image file and send it to the API
//        Uri imageUri = getImageUri(imageBitmap);
//        File imageFile = new File(imageUri.getPath());
//
//        Log.e(TAG, imageUri.getPath());
//        imageView.setImageURI(imageUri);
        //   sendFile(imageFile.getAbsolutePath());
        //  sendImageToAPI(imageFile);
    }

    private Uri getImageUri(Bitmap bitmap) {
        /*Uri imageUri = null;
        try {
            String name  = (new Date().toString());
            imageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, name, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageUri;*/

        String path = "";
        try {
            String date = new Date().getTime() + ".png";
            Log.e(TAG, date);
            File file = new File(this.getCacheDir(), date);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
            path = file.getPath();
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }
        return Uri.parse(path);
    }

    private void sendImageToAPI(File imageFile) {
        // Code to send the image file to the API and handle the response
        // Replace the API URL with the actual API endpoint
        String apiUrl = "http://localhost:8080/api/file";

        // Create a JSON object to hold the response parameters
        JSONObject responseJson = new JSONObject();
        try {
            responseJson.put("param1", "value1");
            responseJson.put("param2", "value2");
            responseJson.put("param3", "value3");
            // Add more parameters as needed
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Set the response parameters in the RecyclerView table below the image
        // Code to set the response parameters in the RecyclerView table
    }

    private boolean isinTitle(String s){
        boolean flag = false;
        for (String s1 : titles){
            if (s1.equals(s)){
                flag = true;
                break;
            }
        }
        return flag;
    }

    private void updateOverall(){
        int low = 4;
        for (DataGrade grade : arrayList){
            dlog("TITLES: " + grade.getTitle() + ":" + grade.getValue());
            if (grade.getTitle().equals("Aperture") || grade.getTitle().equals("Overall Quality")){
                // Do nothing
            }else {
//                if (Integer.parseInt(grade.getValue()) < low){
//                    low  = Integer.parseInt(grade.getValue());
//                }
                low = Math.min(Integer.parseInt(grade.getValue().replaceAll("\"", "")), low);
            }
        }
        arrayList.set(0, new DataGrade(0, "Overall Quality", "" + low));
        adapter.notifyDataSetChanged();
    }
    private void updateOverallFloat(){
        float low = 4f;
        for (DataGrade grade : arrayList){
            dlog("TITLES: " + grade.getTitle() + ":" + grade.getValue());
            if (grade.getTitle().equals("Aperture") || grade.getTitle().equals("Overall Quality")){
                // Do nothing
            }else {
//                if (Integer.parseInt(grade.getValue()) < low){
//                    low  = Integer.parseInt(grade.getValue());
//                }
                low = Math.min(Float.parseFloat(grade.getValue().replaceAll("\"", "")), low);
            }
        }
        arrayList.set(0, new DataGrade(0, "Overall Quality", "" + low));
        adapter.notifyDataSetChanged();
    }

    public static float[] extractFloatArray(String jsonString) {
        try {
            // Parse the JSON array
            JSONArray jsonArray = new JSONArray(jsonString);

            // Extract values into a float array
            float[] resultArray = new float[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                resultArray[i] = (float) jsonArray.getDouble(i);
            }

            return resultArray;
        } catch (JSONException e) {
            e.printStackTrace(); // Handle JSON parsing errors here
            return null;
        }
    }

    private void sendToIon(File file){
        tvHeader.setText("Processing");
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Verifying Image");
        progressDialog.show();
        String url;
        if (tinyDB.getString(RED_PLANE).equals("true")){
            url = tinyDB.getString(SERVER_URL) +  (tinyDB.getString(CONTRAST_IMP).equals("true") ? ":8080/api/fileRCon" : ":8080/api/fileR");
        }else {
            url = tinyDB.getString(SERVER_URL) +  (tinyDB.getString(CONTRAST_IMP).equals("true") ? ":8080/api/fileCon" : ":8080/api/file");
        }

        JsonObject objectx = new JsonObject();
        objectx.addProperty("rplane", tinyDB.getString(RED_PLANE));
        objectx.addProperty("contrast", tinyDB.getString(CONTRAST_IMP));
        objectx.addProperty("multi_factor", tinyDB.getString(MULTI_FACTOR));

        dlog(url);
        Ion.with(VerifierActivity.this)
                .load(url)
                //  .addHeader("authorization", "Bearer " + tinyDB.getString("email").trim())
               // .addHeader("Content-Type", "multipart/form-data")
                .setMultipartParameter("data", objectx.toString())
                .setMultipartParameter("name", "source")
                .setMultipartFile("file", "image/png", file)
                .asJsonObject()
                .setCallback((e, result) -> {
                    progressDialog.dismiss();
                    arrayList.clear();
                    if (e == null){
                        Log.e(TAG, result.toString());
                        try {
                            if (result.isJsonObject() && result.has("jsonData")){
                                String s = result.get("jsonData").getAsString();
                                JsonParser jsonParser = new JsonParser();
                                JsonObject object = (JsonObject) jsonParser.parse(s);
                                for (Map.Entry<String, JsonElement> entry : object.entrySet()){
                                    DataGrade grade = new DataGrade(1, entry.getKey(), entry.getValue().toString() + "");
                                    if (isinTitle(entry.getKey())){
                                        arrayList.add(grade);
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                                updateOverall();
                                tvHeader.setText("Decoded Successfully");
                                tvHeader.setTextColor(getColor(R.color.color_accept));
                             //   tvHeader.setText("Average : " + overallGrade(arrayList) + " : " + getGrade(new DataGrade(1, "All", overallGrade(arrayList))));
                                Log.e(TAG, "Size; " + arrayList.size());
                                if (result.has("decoded")){
                                    String decoded = result.get("decoded").getAsString();
                                    tvHeader.setText("Decoded Successfully \n" + decoded);
                                }
                            }else {
                                tvHeader.setText("Code 2: Failed to Identify Code");
                                tvHeader.setTextColor(getColor(R.color.color_normal));
                               // toast("Code 2: Failed to Test");
                                adapter.notifyDataSetChanged();
                            }
                        }catch (Exception exception){
                            Log.e(TAG, exception.toString());
                            adapter.notifyDataSetChanged();
                            tvHeader.setTextColor(getColor(R.color.design_orange));
                            tvHeader.setText("Code 3: Data Format Error");
                            dlog(exception.toString());
                         //   tvHeader.setText("Average : " + overallGrade(arrayList) + " : " + getGrade(new DataGrade(1, "All", overallGrade(arrayList))));
                        }

                    }else {
                        tvHeader.setText("API Call Failed\n" + e.toString());
                        tvHeader.setTextColor(getColor(R.color.color_reject));
                        Log.e(TAG, e.toString());
                        adapter.notifyDataSetChanged();

                    }
                });
    }

    private void sendToIonNorm(File file, File file2){
        tvHeader.setText("Processing");
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Verifying Image");
        progressDialog.show();
        String url;
        url = tinyDB.getString(SERVER_URL) + ":8080/api/fileNorm";
       /* if (tinyDB.getString(RED_PLANE).equals("true")){
            url = tinyDB.getString(SERVER_URL) +  (tinyDB.getString(CONTRAST_IMP).equals("true") ? ":8080/api/fileRCon" : ":8080/api/fileR");
        }else {
            url = tinyDB.getString(SERVER_URL) +  (tinyDB.getString(CONTRAST_IMP).equals("true") ? ":8080/api/fileCon" : ":8080/api/file");
        }*/

        dlog(url);
        Ion.with(VerifierActivity.this)
                .load(url)
                //  .addHeader("authorization", "Bearer " + tinyDB.getString("email").trim())
                // .addHeader("Content-Type", "multipart/form-data")
                .setMultipartParameter("name", "source")
                .setMultipartFile("file", "image/png", file)
                .setMultipartFile("file2", "image/png", file2)
                .asJsonObject()
                .setCallback((e, result) -> {
                    progressDialog.dismiss();
                    arrayList.clear();
                    if (e == null){
                        Log.e(TAG, result.toString());
                        try {
                            if (result.isJsonObject() && result.has("jsonData")){
                                String s = result.get("jsonData").getAsString();
                                JsonParser jsonParser = new JsonParser();
                                JsonObject object = (JsonObject) jsonParser.parse(s);
                                for (Map.Entry<String, JsonElement> entry : object.entrySet()){
                                    DataGrade grade = new DataGrade(1, entry.getKey(), entry.getValue().toString() + "");
                                    if (isinTitle(entry.getKey())){
                                        arrayList.add(grade);
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                                updateOverall();
                                tvHeader.setText("Decoded Successfully");
                                tvHeader.setTextColor(getColor(R.color.color_accept));
                                //   tvHeader.setText("Average : " + overallGrade(arrayList) + " : " + getGrade(new DataGrade(1, "All", overallGrade(arrayList))));
                                Log.e(TAG, "Size; " + arrayList.size());
                                if (result.has("decoded")){
                                    String decoded = result.get("decoded").getAsString();
                                    tvHeader.setText("Decoded Successfully \n" + decoded);
                                }
                            }else {
                                tvHeader.setText("Code 2: Failed to Identify Code");
                                tvHeader.setTextColor(getColor(R.color.color_normal));
                                // toast("Code 2: Failed to Test");
                                adapter.notifyDataSetChanged();
                            }
                        }catch (Exception exception){
                            Log.e(TAG, exception.toString());
                            adapter.notifyDataSetChanged();
                            tvHeader.setTextColor(getColor(R.color.design_orange));
                            tvHeader.setText("Code 3: Data Format Error");
                            dlog(exception.toString());
                            //   tvHeader.setText("Average : " + overallGrade(arrayList) + " : " + getGrade(new DataGrade(1, "All", overallGrade(arrayList))));
                        }

                    }else {
                        tvHeader.setText("API Call Failed\n" + e.toString());
                        tvHeader.setTextColor(getColor(R.color.color_reject));
                        Log.e(TAG, e.toString());
                        adapter.notifyDataSetChanged();

                    }
                });
    }


    private void sendtoIonCopy(File file, File file2, File file3){
        tvHeader.setText("Processing");
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Verifying Image");
        progressDialog.show();
        String url;
        url = tinyDB.getString(SERVER_URL) + ":8080/api/fileCopy";
       /* if (tinyDB.getString(RED_PLANE).equals("true")){
            url = tinyDB.getString(SERVER_URL) +  (tinyDB.getString(CONTRAST_IMP).equals("true") ? ":8080/api/fileRCon" : ":8080/api/fileR");
        }else {
            url = tinyDB.getString(SERVER_URL) +  (tinyDB.getString(CONTRAST_IMP).equals("true") ? ":8080/api/fileCon" : ":8080/api/file");
        }*/

        dlog(url);
        Ion.with(VerifierActivity.this)
                .load(url)
                //  .addHeader("authorization", "Bearer " + tinyDB.getString("email").trim())
                // .addHeader("Content-Type", "multipart/form-data")
                .setMultipartParameter("name", "source")
                .setMultipartFile("file", "image/png", file)
                .setMultipartFile("file2", "image/png", file2)
                .setMultipartFile("file3", "image/png", file3)
                .asJsonObject()
                .setCallback((e, result) -> {
                    progressDialog.dismiss();
                    arrayList.clear();
                    if (e == null){
                        Log.e(TAG, result.toString());
                        try {
                            if (result.isJsonObject() && result.has("jsonData")){
                                ArrayList<DataGrade> arrayList1 = new ArrayList<>();
                                ArrayList<DataGrade> arrayList2 = new ArrayList<>();
                                ArrayList<DataGrade> arrayList3 = new ArrayList<>();
                                JsonParser jsonParser = new JsonParser();

                                try {
                                    String s = result.get("jsonData").getAsString();
                                    JsonObject object = (JsonObject) jsonParser.parse(s);
                                    for (Map.Entry<String, JsonElement> entry : object.entrySet()){
                                        DataGrade grade = new DataGrade(1, entry.getKey().replaceAll("\"", ""), entry.getValue().toString().replaceAll("\"", "") + "");
                                        if (isinTitle(entry.getKey())){
                                            arrayList1.add(grade);
                                        }
                                    }
                                }catch (Exception e1){
                                    dlog(e1 + "Copy1");
                                }

                                try {
                                    String s2 = result.get("jsonData").getAsString();
                                    JsonObject object2 = (JsonObject) jsonParser.parse(s2);
                                    for (Map.Entry<String, JsonElement> entry : object2.entrySet()){
                                        DataGrade grade = new DataGrade(1, entry.getKey().replaceAll("\"", ""), entry.getValue().toString().replaceAll("\"", "") + "");
                                        if (isinTitle(entry.getKey())){
                                            arrayList2.add(grade);
                                        }
                                    }
                                }catch (Exception e1){
                                    dlog(e1 + "Copy2");
                                }

                                try {
                                    String s3 = result.get("jsonData").getAsString();
                                    JsonObject object3 = (JsonObject) jsonParser.parse(s3);
                                    for (Map.Entry<String, JsonElement> entry : object3.entrySet()){
                                        DataGrade grade = new DataGrade(1, entry.getKey().replaceAll("\"", ""), entry.getValue().toString().replaceAll("\"", "") + "");
                                        if (isinTitle(entry.getKey())){
                                            arrayList3.add(grade);
                                        }
                                    }
                                }catch (Exception e1){
                                    dlog(e1 + "Copy3");
                                }
                                for (int i=0; i<arrayList1.size(); i++){
                                    dlog("phase 1: " + i);
                                    float a = Float.parseFloat(arrayList1.get(i).getValue());
                                    float b = Float.parseFloat(arrayList2.get(i).getValue());
                                    float c = Float.parseFloat(arrayList3.get(i).getValue());
                                    float avg = (a + b + c)/3;
                                    arrayList.add(new DataGrade(1, arrayList1.get(i).getTitle(), avg + ""));
                                    adapter.notifyDataSetChanged();
                                }

                                updateOverallFloat();
                                tvHeader.setText("Decoded Successfully");
                                tvHeader.setTextColor(getColor(R.color.color_accept));
                                //   tvHeader.setText("Average : " + overallGrade(arrayList) + " : " + getGrade(new DataGrade(1, "All", overallGrade(arrayList))));
                                Log.e(TAG, "Size; " + arrayList.size());
                                if (result.has("decoded")){
                                    String decoded = result.get("decoded").getAsString();
                                    tvHeader.setText("Decoded Successfully \n" + decoded);
                                }
                            }else {
                                tvHeader.setText("Code 2: Failed to Identify Code");
                                tvHeader.setTextColor(getColor(R.color.color_normal));
                                // toast("Code 2: Failed to Test");
                                adapter.notifyDataSetChanged();
                            }
                        }catch (Exception exception){
                            Log.e(TAG, exception.toString());
                            adapter.notifyDataSetChanged();
                            tvHeader.setTextColor(getColor(R.color.design_orange));
                            tvHeader.setText("Code 3: Data Format Error");
                            dlog(exception.toString());
                            //   tvHeader.setText("Average : " + overallGrade(arrayList) + " : " + getGrade(new DataGrade(1, "All", overallGrade(arrayList))));
                        }

                    }else {
                        tvHeader.setText("API Call Failed\n" + e.toString());
                        tvHeader.setTextColor(getColor(R.color.color_reject));
                        Log.e(TAG, e.toString());
                        adapter.notifyDataSetChanged();

                    }
                });
    }




    private void sendToIonBar(File file){
        tvHeader.setText("Processing");
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Verifying Image");
        progressDialog.show();
        String url = tinyDB.getString(SERVER_URL) +  ":8080/api/barcode";
        dlog(url);
        Ion.with(VerifierActivity.this)
                .load(url)
                //  .addHeader("authorization", "Bearer " + tinyDB.getString("email").trim())
                // .addHeader("Content-Type", "multipart/form-data")
                .setMultipartParameter("name", "source")
                .setMultipartFile("file", "image/png", file)
                .asJsonObject()
                .setCallback((e, result) -> {
                    progressDialog.dismiss();
                    arrayList.clear();
                    if (e == null){
                        Log.e(TAG, result.toString());
                        try {
                            if (result.isJsonObject() && result.has("jsonData")){
                                String s = result.get("jsonData").getAsString();
                                dlog("DATA : " + s);
                                float[] grades = extractFloatArray(s);
                                for (int i = 0; i< Objects.requireNonNull(grades).length; i++){
                                    DataGrade grade = new DataGrade(1, barTitles.get(i), grades[i] + "");
                                    arrayList.add(grade);
                                    adapter.notifyDataSetChanged();
                                }
                              //  updateOverall();
                                tvHeader.setText("Decoded Successfully");
                                tvHeader.setTextColor(getColor(R.color.color_accept));
                                //   tvHeader.setText("Average : " + overallGrade(arrayList) + " : " + getGrade(new DataGrade(1, "All", overallGrade(arrayList))));
                                Log.e(TAG, "Size; " + arrayList.size());
                                if (result.has("decoded")){
                                    String decoded = result.get("decoded").getAsString();
                                    tvHeader.setText("Decoded Successfully \n" + decoded);
                                }
                            }else {
                                tvHeader.setText("Code 2: Failed to Identify Code");
                                tvHeader.setTextColor(getColor(R.color.color_normal));
                                // toast("Code 2: Failed to Test");
                                adapter.notifyDataSetChanged();
                            }
                        }catch (Exception exception){
                            Log.e(TAG, exception.toString());
                            adapter.notifyDataSetChanged();
                            tvHeader.setTextColor(getColor(R.color.design_orange));
                            tvHeader.setText("Code 3: Data Format Error");
                            dlog(exception.toString());
                            //   tvHeader.setText("Average : " + overallGrade(arrayList) + " : " + getGrade(new DataGrade(1, "All", overallGrade(arrayList))));
                        }

                    }else {
                        tvHeader.setText("API Call Failed\n" + e.toString());
                        tvHeader.setTextColor(getColor(R.color.color_reject));
                        Log.e(TAG, e.toString());
                        adapter.notifyDataSetChanged();

                    }
                });
    }


    public static void selectPictureFromGallery(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, REQUEST_SELECT_PICTURE);
    }

    public static void sendFile(String filePath) {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        File file = new File(filePath);
        Log.e(TAG, filePath + " : " + file.getName().toString());
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"), file))
                .build();

        Request request = new Request.Builder()
                .url("http://localhost:8080/api/file")
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                Log.d(TAG, "Response: " + jsonObject.toString());
            } else {
                Log.e(TAG, "Request failed with code: " + response.code());
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.ViewHolder>{

        private Context context;
        private ArrayList<DataGrade> list=new ArrayList<>();

        public GradeAdapter(Context context, ArrayList<DataGrade> list) {
            this.context=context;
            this.list=list;
        }

        @NonNull
        @Override
        public GradeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_grade, parent, false);
            return new GradeAdapter.ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull GradeAdapter.ViewHolder holder, final int position) {

            holder.tvTitle.setText(list.get(position).getTitle());
            holder.tvValue.setText(list.get(position).getValue());

            holder.tvGrade.setText(list.get(position).getTitle().equals("Aperture") ? "-" : getGrade(list.get(position)));
            // condition ? Yes/True : No/False

           /* if (list.get(position).getTitle().equals("Aperture")){
                // Do nothing
                holder.tvGrade.setText("-");
            }else {
                holder.tvGrade.setText(getGrade(list.get(position)));
            }*/
            holder.tvPerc.setText((Objects.equals(list.get(position).getTitle(), "Contrast Uniformity") || Objects.equals(list.get(position).getTitle(), "Aperture")) ?
                    ((Float.parseFloat(list.get(position).getValue()) * 100) + "") :
                    ((Float.parseFloat(list.get(position).getValue()) * 25) + ""));

            holder.tvGrade.setBackgroundColor(list.get(position).getTitle().equals("Aperture") ? getColor(R.color.color_accept) :getColor(getColor(list.get(position))));

            holder.constraintLayout.setOnClickListener(view -> {
                showGradeDialog(list.get(position));
            });



        }

        @Override
        public int getItemCount() {
            return list.size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvValue, tvGrade, tvPerc;
            ConstraintLayout constraintLayout;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.list_grade_tv1);
                tvValue = itemView.findViewById(R.id.list_grade_tv2);
                constraintLayout = itemView.findViewById(R.id.list_grade_layout);
                tvGrade = itemView.findViewById(R.id.list_grade_tv3);
                tvPerc = itemView.findViewById(R.id.list_grade_tv4);

            }
        }
    }

    private void showGradeDialog(DataGrade grade){
        AlertDialog.Builder builder = new AlertDialog.Builder(VerifierActivity.this)
                .setTitle("Grade Info");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_grade, null);
        builder.setView(dialogView);

      //  AlertDialog alertDialog = builder.create();
        String info = "";
        String cause = "";
        String desc = "";
        switch (grade.getTitle()){
            case "Overall Quality":
    /*            info = "<b>" + grade.getTitle() +":</b><br><br> " +
                        "<b>Result: </b> "+grade.getValue()+"<br><br> " +
                        "<b>Details:</b> Gives Overall Quality of the Code";*/
                desc = "<b>Details:</b><br> Gives Overall Quality of the Code";
                cause = "";
                break;

            case "Decode":

                desc = "<b>Details:</b><br> This is the first step in the verification and applies the reference decode algorithm";
                cause = "<b>Causes:</b><br> Many factors can cause the symbol to fail to decode. A major failure in any of the tested parameters or software errors in the printing system should be checked first.";
                break;

            case "Aperture":
           /*     info ="<b>" + grade.getTitle() +":</b><br><br> " +
                        "<b>Result: </b> <br>"+grade.getValue()+"<br><br> " +*/
                       desc = "<b>Details:</b><br> <br> Gives Aperture of the Code";
                       cause = "";
                break;
            case "Contrast":
                /*info = "<b>" + grade.getTitle() +":</b><br><br> " +
                        "<b>Result: </b> "+grade.getValue()+"<br><br> " +*/
                        desc= "<b>Details:</b><br> The Symbol Contrast is the difference between the highest and the lowest reflectance values in the profile";
                        cause = "<b>Causes:</b><br> Low background or light area reflectance, due to:<br> ■ Poor choice of substrate (e.g., dark background)<br> ■ Glossy laminate or overwrap<br>High dark module reflectance, due to:<br> ■ Unsuitable formulation or colour of ink<br> ■ Insufficient ink coverage (e.g., on-overlapping dots)<br> Inappropriate angle of illumination particularly for symbols printed using Direct Part Marking (DPM).";

                break;
            case "Axial Nonuniformity":
               /* info ="<b>" + grade.getTitle() +":</b><br><br> " +
                        "<b>Result: </b> "+grade.getValue()+"<br><br> " +*/
                       desc = "<b>Details:</b><br> Axial Nonuniformity measures and grades (on the 4 to 0 scale) the spacing of the mapping centres and tests for uneven scaling of the symbol along the X or Y axis";
                       cause = "<b>Causes:</b><br> Mismatch of transport speed in printing with symbol dimensions<br> Printing software errors<br> Verifier axis not perpendicular to symbol plane";
                break;
            case "Modulation":
                /*info ="<b>" + grade.getTitle() +":</b><br><br> " +
                        "<b>Result: </b> "+grade.getValue()+"<br><br> " +*/
                        desc = "<b>Details:</b><br> Modulation is related to Symbol Contrast in the sense that it measures the consistency of the reflectance of dark to light areas throughout the symbol";
                        cause = "<b>Causes:</b><br> Print growth or loss<br> Verifier aperture set too large for X-dimension used<br> Defects - print spots or voids (see defects)<br> Irregular substrate reflectance<br><br> Variation in ink coverage<br> Show-through (often caused by printing on a transparent background)<br> Transparency";
                break;
            case "Grid Nonuniformity":
             /*   info ="<b>" + grade.getTitle() +":</b><br><br> " +
                        "<b>Result: </b> "+grade.getValue()+"<br><br> " +*/
                        desc = "<b>Details:</b><br> Grid Nonuniformity measures and grades (on the 4 to 0 scale) the largest vector deviation of the grid intersections, determined by the theoretical position prescribed by the reference decode algorithm and the actual measured result.";
                        cause = "<b>Causes:</b><br> Problems with the speed during printing (accelerations, decelerations, vibration, or slippage)<br> Variable distance between the print head and the print surface<br> Verifier axis not perpendicular to symbol plane";
                break;
            case "Unused Error Correction":
                /*info ="<b>" + grade.getTitle() +":</b><br><br> " +
                        "<b>Result: </b> "+grade.getValue()+"<br><br> " +*/
                        desc = "<b>Details:</b><br> Measures and grades (on the 4 to 0 scale) the reading safety margin that error correction provides. Unused error correction indicates the amount of available Error Correction in a symbol. ";
                        cause = "<b>Causes:</b><br> Physical damage due to:<br> ■ Scuffing<br> ■ Tearing<br> ■ Deletions<br>  Bit errors due to print defects<br>  Excessive print Growth<br>  Local deformation Misplaced Modules";
                break;
            case "Fixed Pattern Damage":
                /*info ="<b>" + grade.getTitle() +":</b><br><br> " +
                        "<b>Result: </b> "+grade.getValue()+"<br><br> " +*/
                        desc = "<b>Details:</b><br> Measures and grades (on the 4 to 0 scale) any damage to the Finder Pattern, Quiet Zone and Clock Track in the symbol.";
                        cause = "<b>Causes:</b><br> Spots of ink or other dark marks on the background<br> Voids in printed areas<br> Faulty print head elements or other print setup fault.<br> Verifier aperture set too large for Xdimension used";
                break;

            case "Print Growth":
                desc = "<b>Details:</b><br> Print growth is not a graded parameter but is a very informative measure for the purposes of process control. It is a measure of how symbols may have grown or shrunk from target size. If the growth or shrinkage is too large, then scanning performance will be impacted.";
                cause = "<b>Causes:</b><br><br> Largely dependent upon the exact print process used. Factors may include:<br> ■ Ink absorbency of the substrate <br> ■ Dot size (Inkjet and DPM)<br> ■ Incorrect thermal print head settings";
                break;

            default:
                // No actions
                break;
        }

        String title = "<b>Grade Title: </b> <br>" + grade.getTitle();
        String value = "<b>Grade Value: </b> <br>" + grade.getValue() + "<b>" + getGrade(grade) + "</b>";

        final TextView tvDesc = dialogView.findViewById(R.id.dialog_grade_tvDesc);
        final TextView tvTitle = dialogView.findViewById(R.id.dialog_grade_tvTitle);
        final TextView tvValue = dialogView.findViewById(R.id.dialog_grade_tvValue);
        final TextView tvCause = dialogView.findViewById(R.id.dialog_grade_tvCause);

        tvDesc.setText(Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY));
        tvCause.setText(Html.fromHtml(cause, Html.FROM_HTML_MODE_LEGACY));
        tvTitle.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
        tvValue.setText(Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY));

     /*   alertDialog.setMessage(Html.fromHtml(info, Html.FROM_HTML_MODE_LEGACY));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Close", (dialogInterface, i) -> alertDialog.dismiss());

        alertDialog.show();*/
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private String overallGrade(ArrayList<DataGrade> arrayList){
        float average = 0;
        for (DataGrade grade : arrayList){
            average += ((Objects.equals(grade.getTitle(), "Contrast Uniformity") || Objects.equals(grade.getTitle(), "Aperture")) ? (Float.parseFloat(grade.getValue()) * 100) : (Float.parseFloat(grade.getValue()) * 25));
        }
        if (arrayList.size() > 0){
            return (average/arrayList.size()) + "";
        }else {
            return average + "";
        }
    }

    private String getGrade(DataGrade grade){
        float score = Float.parseFloat(grade.getValue());
        score = (grade.getTitle().equals("Contrast Uniformity") || grade.getTitle().equals("Aperture") ? score : grade.getTitle().equals("All") ? score/100 : score/4);
        if (tinyDB.getString(GRADE_TYPE).equals("true")){
            if (score >= 1) return "A";
            else if (score >= 0.75) return "B";
            else if (score >= 0.5) return "C";
            else if (score >= 0.25) return "D";
            else return "F";
        }else {
            if (score >= 0.875) return "A"; // 3.5  we get from 0 to 4 4
            else if (score >= 0.625) return "B"; // 2.5 >>> 3
            else if (score >= 0.375) return "C"; // 1.5 >>>> 2
            else if (score >= 0.125) return "D"; // 0.5 >>>> 1
            else return "F";
        }
    }

    private int getColor(DataGrade grade) {
        float score = Float.parseFloat(grade.getValue());
        score = (grade.getTitle().equals("Contrast Uniformity") || grade.getTitle().equals("Aperture") ? score : grade.getTitle().equals("All") ? score/100 : score/4);
        if (tinyDB.getString(GRADE_TYPE).equals("true")){
            if (score >= 1) return R.color.color_accept;
            else if (score >= 0.75) return R.color.color_normal;
            else if (score >= 0.5) return R.color.design_orange;
            else if (score >= 0.25) return R.color.color_reject;
            else return R.color.white;
        }else {
            if (score >= 0.875) return R.color.color_accept;
            else if (score >= 0.625) return R.color.color_normal;
            else if (score >= 0.375) return R.color.design_orange;
            else if (score >= 0.125) return R.color.color_reject;
            else return R.color.white;
        }
    }


}
