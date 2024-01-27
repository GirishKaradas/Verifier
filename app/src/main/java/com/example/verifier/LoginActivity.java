package com.example.verifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends BaseActivity {

    private EditText usernameEditText, passwordEditText;
    private CheckBox showPasswordCheckBox;
    private Button loginButton;
    private FloatingActionButton settingsFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);
        loginButton = findViewById(R.id.loginButton);
        settingsFab = findViewById(R.id.settingsFab);

        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Show/hide password logic
            int inputType = isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            passwordEditText.setInputType(inputType);
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle login logic here
                startActivity(new Intent(getApplicationContext(), VerifierActivity.class));
            }
        });

        settingsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingsDialog();
            }
        });
    }

    private void openSettingsDialog() {
        // Create a dialog for entering the server URL
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server Settings");

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_server_settings, null);
        builder.setView(dialogView);

        final TextInputLayout urlTextInputLayout = dialogView.findViewById(R.id.urlTextInputLayout);
        final TextInputEditText urlEditText = dialogView.findViewById(R.id.urlEditText);
        final CheckBox checkBox = dialogView.findViewById(R.id.dialog_settings_check);
        final CheckBox checkDelay = dialogView.findViewById(R.id.dialog_settings_delay);
        final CheckBox checkGrade = dialogView.findViewById(R.id.dialog_settings_grades);

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
        checkDelay.setChecked(tinyDB.getString(PROCESS_DELAY).equals("true"));
        checkGrade.setChecked(tinyDB.getString(GRADE_TYPE).equals("true"));

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

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String enteredUrl = urlEditText.getText().toString();
            Toast.makeText(LoginActivity.this, "Updated", Toast.LENGTH_SHORT).show();
            // Save the URL or perform further actions with it
            tinyDB.putString(SERVER_URL, enteredUrl);
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
