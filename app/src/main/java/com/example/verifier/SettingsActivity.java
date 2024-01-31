package com.example.verifier;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends BaseActivity {

    private TextInputLayout urlTextInputLayout;
    private TextInputEditText urlEditText;
    private CheckBox checkBox, checkDelay, checkGrade, checkRed, checkCrop, checkContrast, checkCompress;
    private Button bSubmit;
    private FloatingActionButton fabInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        urlTextInputLayout = findViewById(R.id.activity_settings_etLayout);
        urlEditText = findViewById(R.id.activity_settings_etUrl);
        checkBox = findViewById(R.id.activity_settings_check);
        checkDelay = findViewById(R.id.activity_settings_delay);
        checkGrade = findViewById(R.id.activity_settings_grades);
        checkRed = findViewById(R.id.activity_settings_red);
        checkCrop = findViewById(R.id.activity_settings_crop);
        checkContrast = findViewById(R.id.activity_settings_contrast);
        checkCompress = findViewById(R.id.activity_settings_compress);
        bSubmit = findViewById(R.id.activity_settings_bSubmit);
        fabInfo = findViewById(R.id.activity_settings_fabInfo);


        if (tinyDB.objectExists(SERVER_URL)){
            urlEditText.setText(tinyDB.getString(SERVER_URL));
        }else {
            urlEditText.setText("http://");
        }
        if (tinyDB.objectExists(CAMERA_TYPE)){
            checkBox.setChecked(tinyDB.getString(CAMERA_TYPE).equals("Normal"));
        }else {
            tinyDB.putString(CAMERA_TYPE, "ADV");
        }
        if (!tinyDB.objectExists(PROCESS_DELAY)){
            tinyDB.putString(PROCESS_DELAY, "false");
        }
        if (!tinyDB.objectExists(GRADE_TYPE)){
            tinyDB.putString(GRADE_TYPE, "false");
        }
        if (!tinyDB.objectExists(RED_PLANE)){
            tinyDB.putString(RED_PLANE, "false");
        }
        if (!tinyDB.objectExists(CROP_CENTER)){
            tinyDB.putString(CROP_CENTER, "false");
        }
        if (!tinyDB.objectExists(CONTRAST_IMP)){
            tinyDB.putString(CONTRAST_IMP, "false");
        }
        if (!tinyDB.objectExists(COMPRESS)){
            tinyDB.putString(COMPRESS, "false");
        }
        checkDelay.setChecked(tinyDB.getString(PROCESS_DELAY).equals("true"));
        checkGrade.setChecked(tinyDB.getString(GRADE_TYPE).equals("true"));
        checkRed.setChecked(tinyDB.getString(RED_PLANE).equals("true"));
        checkCrop.setChecked(tinyDB.getString(CROP_CENTER).equals("true"));
        checkContrast.setChecked(tinyDB.getString(CONTRAST_IMP).equals("true"));
        checkCompress.setChecked(tinyDB.getString(COMPRESS).equals("true"));

        checkDelay.setOnCheckedChangeListener((compoundButton, b) -> {
            tinyDB.putString(PROCESS_DELAY, b ? "true" : "false");
            toast("Process Delay " + (b ? "Enabled" : "Disabled"));
        });
        checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            tinyDB.putString(CAMERA_TYPE, b ? "Normal" : "ADV");
            toast(b ? "Normal Camera Selected" : "App Camera Selected");
        });

        checkGrade.setOnCheckedChangeListener((compoundButton, b) -> {
            tinyDB.putString(GRADE_TYPE, b ? "true" : "false");
            toast(b ? "Old Calculation Selected" : "New Calculation Selected");
        });

        checkRed.setOnCheckedChangeListener((compoundButton, b) -> {
            tinyDB.putString(RED_PLANE, b ? "true" : "false");
            toast(b ? "Red Plane Enabled" : "Red Plane Disabled");
        });

        checkCrop.setOnCheckedChangeListener((compoundButton, b) -> {
            tinyDB.putString(CROP_CENTER, b? "true" : "false");
            toast(b ? "Crop Enabled" : "Crop Disabled");
        });
        checkContrast.setOnCheckedChangeListener((compoundButton, b) -> {
            tinyDB.putString(CONTRAST_IMP, b ? "true" : "false");
            toast(b ? "Enabled" : "Disabled");
        });
        checkCompress.setOnCheckedChangeListener((compoundButton, b) -> {
            tinyDB.putString(COMPRESS, b ? "true" : "false");
            toast(b ? "Enabled" : "Disabled");
        });

        bSubmit.setOnClickListener(v -> {
            String enteredUrl = urlEditText.getText().toString();

            // Save the URL or perform further actions with it
            tinyDB.putString(SERVER_URL, enteredUrl);
            toast("Updated");
        });

        fabInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
    }

    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Settings Info");

        AlertDialog alertDialog = builder.create();
        String info = "<b>Server URL:</b> Enter Server URL for Verifier<br><br> " +
                "<b>Process Delay: </b> Delay while capturing the image from camera<br><br> " +
                "<b>Grade Calculation(Old):</b> Grade calculation based on new and old<br><br> " +
                "<b>Red Plane Extraction:</b> Used for converting RGB images to Grayscale images<br><br> " +
                "<b>Center Crop:</b> Crops the image between the indicator while capturing the image<br><br> " +
                "<b>Improve Contrast:</b> Improves the contrast of the image while decoding <br><br> " +
                "<b>Normal Camera:</b> Switch between Device camera and App camera<br><br> " +
                "<b>Image Compression:</b> Compress images captured from camera before sending for decoding";

        alertDialog.setMessage(Html.fromHtml(info, Html.FROM_HTML_MODE_LEGACY));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Close", (dialogInterface, i) -> alertDialog.dismiss());

        alertDialog.show();
    }
}