package com.example.verifier;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    @SuppressLint({"NotifyDataSetChanged", "StaticFieldLeak"})
    public static TinyDB tinyDB;
    public static Context context;


    public static String FB_ERROR = "Error Fetching Documents";
    public static String NO_DATA = "No Data";

    public static String SS_ADMIN = "SmartScan_Admin";
    public static String SS_SUPERVISOR = "SmartScan_Supervisor";
    public static String SS_OPERATOR = "SmartScan_Operator";

    public static String SS_MASTER = "SmartScan_Master";

    public static String SERVER_URL = "server_url";
    public static String PROCESS_DELAY = "process_delay";
    public static String CAMERA_TYPE = "camera_type";
    public static String GRADE_TYPE = "grade_type";

    public interface ArrayListCallback {
        void onCallback(ArrayList value, String e);
    }
    public interface BooleanCallback {
        void onCallback(Boolean aBoolean, String e);
    }

    public interface JsonCallback {
        void onCallback(JsonObject object, String e);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tinyDB = new TinyDB(this);
        context = this;

        if (tinyDB.objectExists("isAuto")){
            // Do nothing
        }else {
            tinyDB.putString("isAuto", "false");
        }

        if (!tinyDB.objectExists(PROCESS_DELAY)){
            tinyDB.putString(PROCESS_DELAY, "false");
        }
        if (!tinyDB.objectExists(GRADE_TYPE)){
            tinyDB.putString(GRADE_TYPE, "false");
        }
    }

    public static void toast(String s){
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
        Log.e("BMRX", s);
    }

    public static void dlog(String s){
        Log.e("BMRX", s);
    }

    public static Boolean isUrlDefined(){
        if (tinyDB.objectExists("server_url")){
            return tinyDB.getString("server_url") != null;
        }else {
            return false;
        }
    }

    public static void setStyleText(TextView textView, String header, String content){
        String string = "<b>" + header + ": </b>" + content;

        textView.setText(Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY));
    }

    public static String getImageUrl(String batch_key, String image_name){
        //   return  tinyDB.getString("server_url") + "/images/"  + batch_key + "/" + image_name;
        return  tinyDB.getString("server_url") + "/images/" + tinyDB.getString("site") + "/" + batch_key + "/" + image_name;
    }


    public static boolean isResultSuccess(JsonObject result, Exception e){
        if (e != null){
            return false;
        }else {
            return result.has("status") && result.get("status").getAsString().equals("success");
        }
    }


    //Setting Recyclerview Visibility
    public static void RcViewVisibility(int size, View rcView, TextView textView, String e){
        textView.setText(e);
        if (size == 0){
            rcView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }else {
            rcView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    /*  public static String addIfValid(DocumentSnapshot snapshot, String key){
          if (snapshot.getData().get(key) != null){
              return snapshot.getData().get(key).toString();
          }else {
              return "";
          }
      }

      public static Boolean addIfValidBoolean(DocumentSnapshot snapshot, String key){
          if (snapshot.getData().get(key) != null){
              return snapshot.getBoolean(key);
          }else {
              return false;
          }
      }

      public static Timestamp addIfValidTime(DocumentSnapshot snapshot, String key){
          if (snapshot.getData().get(key) != null){
              return snapshot.getTimestamp(key);
          }else {
              return null;
          }
      }

      public static ArrayList<String> addIfValidArray(DocumentSnapshot snapshot, String key){
          if (snapshot.getData().get(key) != null){
              return (ArrayList<String>) snapshot.getData().get(key);
          }else {
              return null;
          }
      }
  */
    public static String set(JsonObject object, String s){
        if (object != null && s != null){
            if (object.has(s)){
                return object.get(s).toString().trim().replace("\"", "");
            }else {
                return "";
            }
        }else {
            return "";
        }
    }

    public static int setInt(JsonObject object, String s){
        if (object != null && s != null){
            if (object.has(s) && !object.get(s).toString().equals("")){
                return Integer.parseInt(object.get(s).toString().trim().replace("\"", ""));
            }else {
                return 0;
            }
        }else {
            return 0;
        }
    }

    public static double setDouble(JsonObject object, String s){
        if (object != null && s != null){
            if (object.has(s)){
                dlog(object.get(s).toString());
                return Double.parseDouble(object.get(s).toString().trim().replace("\"", ""));
            }else {
                return 0;
            }
        }else {
            return 0;
        }
    }

    public static boolean setBoolean(JsonObject object, String s){
        if (object != null && s != null){
            if (object.has(s)){
                return Boolean.parseBoolean(object.get(s).toString().trim().replace("\"", ""));
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    public static boolean setImageBoolean(JsonObject object, String s){
        if (object != null && s != null){
            if (object.has(s)){
                return Boolean.parseBoolean(object.get(s).toString().trim().replace("\"", ""));
            }else {
                return true;
            }
        }else {
            return true;
        }
    }

    public static ArrayList<String> setStringArray(JsonObject object, String s){
        ArrayList<String> strings = new ArrayList<>();
        if (object != null && s != null){
            if (object.has(s)){
                JsonArray array = object.get(s).getAsJsonArray();
                for (JsonElement element : array){
                    strings.add(element.toString());
                }
            }
            return strings;
        }else {
            return strings;
        }
    }




    public static Date setDate(JsonObject object, String s) throws ParseException {
        if (object != null && s != null){
            if (object.has(s)){
                String d = object.get(s).getAsString().trim().replace("\"", "");
                Log.e("date", ":" + d);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH);
                /* String d = object.get(s).toString().trim();
                Date date = new Date(d);*/
                return sdf.parse(d);
            }else {
                return new Date();
            }
        }else {
            return new Date();
        }
    }

    public static Bitmap cropImage(Bitmap bitmap_in){

        if (tinyDB.objectExists("crop_margin")){
            int x = Integer.parseInt(tinyDB.getString("crop_margin"));
            Bitmap bitmap_con = Bitmap.createBitmap(bitmap_in, x, x, bitmap_in.getWidth() - (2*x), bitmap_in.getHeight() - (2*x));
            return bitmap_con;
        }else {
            tinyDB.putString("crop_margin", "10");
            int x = Integer.parseInt(tinyDB.getString("crop_margin"));
            Bitmap bitmap_con = Bitmap.createBitmap(bitmap_in, x, x, bitmap_in.getWidth() - (2*x), bitmap_in.getHeight() - (2*x));
            return bitmap_con;
        }

    }

    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        String path = "";
        try {
          /* ByteArrayOutputStream bytes = new ByteArrayOutputStream();
           inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
           String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
           File file2 = new File(inContext.getCacheDir(), "temp.jpg");
           String path2 = new File(inContext.getCacheDir(), "Temp.jpg").getPath();*/
            String date = new Date().getTime() + ".jpg";
            tinyDB.putString("last_image", date);
            dlog(date);
            File file = new File(context.getCacheDir(), date);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();
            path = file.getPath();
        }catch (Exception e){
            toast(e.toString());
        }
        return Uri.parse(path);
    }


    public static boolean checkIfReport(String s){
        return s != null && s.length() > 0;
    }

//    public boolean imagesAreEqual(Image i1, Image i2)
//    {
//        if (i1.getHeight() != i2.getHeight()) return false;
//        if (i1.getWidth() != i2.getWidth()) return false;
//
//        for (int y = 0; y < i1.getHeight(); ++y)
//            for (int x = 0; x < i1.getWidth(); ++x)
//                if (i1.getPixel(x, y) != i2.getPixel(x, y)) return false;
//
//        return true;
//    }


    // For image checking

    public static boolean checkImage(ArrayList<String> locs){
        try {
            float x1, x2, x3, x4, y1, y2, y3, y4;

            x1 = floatToString(locs.get(0));
            x2 = floatToString(locs.get(2));
            x3 = floatToString(locs.get(4));
            x4 = floatToString(locs.get(6));

            y1 = floatToString(locs.get(1));
            y2 = floatToString(locs.get(3));
            y3 = floatToString(locs.get(5));
            y4 = floatToString(locs.get(7));
//
//            dlog(x1 + ":" + x3);
//            dlog(x2 + ":" + x4);
//            dlog(y1 + ":" + y2);
//            dlog(y3 + ":" + y4);

            if (x1 == x3 && x2 == x4 && y1 == y2 && y3 == y4){
                return false;
            }else {
                return checkLength(x2, x1) && checkLength(x4, x3) &&
                        checkLength(y3, y1) && checkLength(y4, y2)
                        && checkParallel(x3, x1) && checkParallel(x4, x2)
                        && checkParallel(y2, y1) && checkParallel(y4, y3);
            }


        }catch (Exception e){
            dlog(e.toString());
            return true;
        }
    }

    public static float floatToString(String s){
        float f = 0;
        try {
            f = Float.parseFloat(s);
            return f;
        }catch (Exception e){
            return f;
        }
    }

    public static boolean checkLength(float a, float b){
        int x =  (tinyDB.getString("check_accuracy").equals("Low")) ? 500 :
                (tinyDB.getString("check_accuracy").equals("Medium")) ? 700 : 900;
        return Math.abs(a - b) > x;
    }
    public static boolean checkParallel(float a, float b){
        int x =  (tinyDB.getString("check_accuracy").equals("Low")) ? 600 :
                (tinyDB.getString("check_accuracy").equals("Medium")) ? 400 : 200;
        return Math.abs(a - b) < x;
    }



}
