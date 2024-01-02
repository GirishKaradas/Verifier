package com.example.verifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

        if (tinyDB.objectExists(SERVER_URL)){
            urlEditText.setText(tinyDB.getString(SERVER_URL));
        }else {
            urlEditText.setText("http://");
        }
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredUrl = urlEditText.getText().toString();
                Toast.makeText(LoginActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                // Save the URL or perform further actions with it
                tinyDB.putString(SERVER_URL, enteredUrl);
            }
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
