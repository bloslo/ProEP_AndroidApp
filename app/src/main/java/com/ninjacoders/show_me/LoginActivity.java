package com.ninjacoders.show_me;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button btnLogin;
    private Button btnRegister;

    private static final String TAG = "Login";

    private Boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.usernameInput);
        password = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.login);
        btnRegister = findViewById(R.id.register);

        Singleton.getInstance().getSocket().on("login", onLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject user = new JSONObject();
                String name = username.getText().toString().replace(" ", "");
                String pass = password.getText().toString().replace(" ", "");

                if (isOnline(getApplicationContext())) {
                    if (!name.isEmpty() && !pass.isEmpty()) {
                        try {
                            user.put("name", username.getText());
                            user.put("pass", password.getText());
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }

                        Singleton.getInstance().getSocket().emit("login", user);
                    } else {
                        Toast.makeText(getApplicationContext(), "Fill in username and password!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Make sure you are connected to the Internet!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove listeners.
        Singleton.getInstance().getSocket().off("login");
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject data = (JSONObject) args[0];

            try {
                if (data.has("succeed") && data.getBoolean("succeed")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            username.setText("");
                            password.setText("");
                            Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_LONG).show();
                        }
                    });

                    // Clear fields


                    // Go to the MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                } else if (data.has("succeed") && !data.getBoolean("succeed")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(getApplicationContext(), data.getString("message"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }
}
