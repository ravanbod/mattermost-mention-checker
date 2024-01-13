package dev.behrad.mattermostchecker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("mattermost_checker_data", MODE_PRIVATE);

        Button loginButton = findViewById(R.id.login_btn);
        EditText baseUrlET = findViewById(R.id.mattermost_base_url);
        EditText loginIdET = findViewById(R.id.mattermost_login_id);
        EditText passwordET = findViewById(R.id.mattermost_password);

        baseUrlET.setText(prefs.getString("base_url", ""));
        loginIdET.setText(prefs.getString("login_id", ""));
        passwordET.setText(prefs.getString("password", ""));



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putString("base_url", baseUrlET.getText().toString()).apply();
                prefs.edit().putString("login_id", loginIdET.getText().toString()).apply();
                prefs.edit().putString("password", passwordET.getText().toString()).apply();



            }
        });
    }
}