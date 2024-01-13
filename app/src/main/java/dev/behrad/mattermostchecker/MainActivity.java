package dev.behrad.mattermostchecker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    SharedPreferences prefs;
    boolean onDuty = false;
    Intent svcIntent;

    EditText baseUrlET, loginIdET, passwordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("mattermost_checker_data", MODE_PRIVATE);

        Button loginButton = findViewById(R.id.login_btn);
        baseUrlET = findViewById(R.id.mattermost_base_url);
        loginIdET = findViewById(R.id.mattermost_login_id);
        passwordET = findViewById(R.id.mattermost_password);

        baseUrlET.setText(prefs.getString("base_url", ""));
        loginIdET.setText(prefs.getString("login_id", ""));
        passwordET.setText(prefs.getString("password", ""));

        svcIntent = new Intent(MainActivity.this, CheckerService.class);


        loginButton.setOnClickListener(v -> {
            if (!onDuty) {
                try {
                    ProgressDialog dialog = ProgressDialog.show(this, "",
                            "Loading. Please wait...", true);
                    getLoginToken(baseUrlET.getText().toString() + "/api/v4/users/login", loginIdET.getText().toString(), passwordET.getText().toString()
                            , new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    dialog.cancel();
                                    Toast.makeText(MainActivity.this, "FAILED to start", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.cancel();
                                            if (!response.isSuccessful()) {
                                                Toast.makeText(MainActivity.this, "FAILED to start", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            loginButton.setText(R.string.button_svc_enabled);
                                            baseUrlET.setEnabled(!baseUrlET.isEnabled());
                                            loginIdET.setEnabled(!loginIdET.isEnabled());
                                            passwordET.setEnabled(!passwordET.isEnabled());
                                            prefs.edit().putString("base_url", baseUrlET.getText().toString()).apply();
                                            prefs.edit().putString("login_id", loginIdET.getText().toString()).apply();
                                            prefs.edit().putString("password", passwordET.getText().toString()).apply();
                                            svcIntent.putExtra("token", response.header("token"));
                                            svcIntent.putExtra("base_url", baseUrlET.getText().toString());
                                            startService(svcIntent);
                                            Toast.makeText(MainActivity.this, "STARTED", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }

                            });
                    onDuty = !onDuty;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                stopService(svcIntent);
                loginButton.setText(R.string.button_svc_disabled);
                onDuty = !onDuty;
                baseUrlET.setEnabled(!baseUrlET.isEnabled());
                loginIdET.setEnabled(!loginIdET.isEnabled());
                passwordET.setEnabled(!passwordET.isEnabled());

            }
        });
    }

    private void getLoginToken(String url, String loginId, String password, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(String.format("{\"login_id\":\"%s\",\"password\":\"%s\"}", loginId, password).getBytes(StandardCharsets.UTF_8));

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }
}