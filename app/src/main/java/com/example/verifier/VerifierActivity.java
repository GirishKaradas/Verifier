package com.example.verifier;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class VerifierActivity extends BaseActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 200;

    private ArrayList<DataGrade> arrayList = new ArrayList<>();
    private GradeAdapter adapter;

    private ConstraintLayout layoutHead;

    private Bitmap imageBitmap;

    private ImageView imageView;
    private ExtendedFloatingActionButton fab;
    private RecyclerView recyclerView;

    private TextView tvHeader;

    public static String TAG = "SOME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifier);

        imageView = findViewById(R.id.activity_verifier_imageview);
        fab = findViewById(R.id.activity_verifier_fab);
        recyclerView = findViewById(R.id.activity_verifier_rcView);
        tvHeader = findViewById(R.id.activity_verifier_tvTitle);
        layoutHead = findViewById(R.id.activity_verifier_layouthead);

        adapter = new GradeAdapter(this, arrayList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        fab.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(VerifierActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(VerifierActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                captureImage();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (imageBitmap == null){
            imageView.setImageDrawable(getDrawable(R.drawable.baseline_image_24));
            layoutHead.setVisibility(View.GONE);
            tvHeader.setText("Scan New Image");
        }else {
            layoutHead.setVisibility(View.VISIBLE);
        }
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                imageBitmap = (Bitmap) data.getExtras().get("data");
               // imageView.setImageBitmap(imageBitmap);

                // Get the image file and send it to the API
                Uri imageUri = getImageUri(imageBitmap);
                File imageFile = new File(imageUri.getPath());

                Log.e(TAG, imageUri.getPath());
                imageView.setImageURI(imageUri);
                sendToIon(imageFile);
             //   sendFile(imageFile.getAbsolutePath());
              //  sendImageToAPI(imageFile);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
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

    private void sendToIon(File file){
        String url = tinyDB.getString(SERVER_URL) +  ":8080/api/file";
        dlog(url);
        Ion.with(VerifierActivity.this)
                .load(url)
                //  .addHeader("authorization", "Bearer " + tinyDB.getString("email").trim())
               // .addHeader("Content-Type", "multipart/form-data")
                .setMultipartParameter("name", "source")
                .setMultipartFile("file", "image/png", file)
                .asJsonObject()
                .setCallback((e, result) -> {
                    if (e == null){
                        Log.e(TAG, result.toString());
                        try {
                            if (result.isJsonObject() && result.has("jsonData")){
                                String s = result.get("jsonData").getAsString();
                                JsonParser jsonParser = new JsonParser();
                                JsonObject object = (JsonObject) jsonParser.parse(s);
                                arrayList.clear();
                                for (Map.Entry<String, JsonElement> entry : object.entrySet()){
                                    DataGrade grade = new DataGrade(1, entry.getKey(), entry.getValue().toString() + "");
                                    arrayList.add(grade);
                                    adapter.notifyDataSetChanged();
                                }
                                tvHeader.setText("Average : " + overallGrade(arrayList) + " : " + getGrade(new DataGrade(1, "All", overallGrade(arrayList))));
                                Log.e(TAG, "Size; " + arrayList.size());
                            }else {
                                toast("Failed to connect");
                            }
                        }catch (Exception exception){
                            Log.e(TAG, exception.toString());
                            adapter.notifyDataSetChanged();
                            tvHeader.setText("Average : " + overallGrade(arrayList) + " : " + getGrade(new DataGrade(1, "All", overallGrade(arrayList))));
                        }

                    }else {
                        Log.e(TAG, e.toString());
                    }
                });
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
            holder.tvGrade.setText(getGrade(list.get(position)));
            holder.tvPerc.setText((Objects.equals(list.get(position).getTitle(), "Contrast Uniformity") || Objects.equals(list.get(position).getTitle(), "Aperture")) ?
                    ((Float.parseFloat(list.get(position).getValue()) * 100) + "") :
                    ((Float.parseFloat(list.get(position).getValue()) * 25) + ""));
            holder.tvGrade.setBackgroundColor(getColor(getColor(list.get(position))));

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
        if (score >= 1) return "A";
        else if (score >= 0.75) return "B";
        else if (score >= 0.5) return "C";
        else if (score >= 0.25) return "D";
        else return "F";
    }

    private int getColor(DataGrade grade) {
        float score = Float.parseFloat(grade.getValue());
        score = (grade.getTitle().equals("Contrast Uniformity") || grade.getTitle().equals("Aperture") ? score : grade.getTitle().equals("All") ? score/100 : score/4);
        if (score >= 1) return R.color.color_accept;
        else if (score >= 0.75) return R.color.color_normal;
        else if (score >= 0.5) return R.color.design_orange;
        else if (score >= 0.25) return R.color.color_reject;
        else return R.color.white;
    }


}
