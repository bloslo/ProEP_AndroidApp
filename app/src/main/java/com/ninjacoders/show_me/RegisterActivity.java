package com.ninjacoders.show_me;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.socket.emitter.Emitter;

public class RegisterActivity extends AppCompatActivity {

    private Button register;
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText repeatPassword;

    private static final String TAG = "Register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        register = findViewById(R.id.regButton);
        username = findViewById(R.id.registerUsername);
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.registerPassword);
        repeatPassword = findViewById(R.id.repeatPassword);

        Singleton.getInstance().getSocket().on("register", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final JSONObject data = (JSONObject) args[0];

                try {
                    if (data.has("succeed") && data.getBoolean("succeed")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_LONG).show();
                            }
                        });

                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
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
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailRegex = "^[a-zA-z0-9!.#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,3}$";
                Pattern pattern = Pattern.compile(emailRegex);

                JSONObject newUser = new JSONObject();
                String name = username.getText().toString().replace(" ", "");
                String pass = password.getText().toString().replace(" ", "");
                String repeatPass = repeatPassword.getText().toString().replace(" ", "");
                String mail = email.getText().toString().replace(" ", "");

                Matcher matcher = pattern.matcher(mail);

                if (isOnline(getApplicationContext())) {
                    if (name.isEmpty() && pass.isEmpty() && repeatPass.isEmpty() && mail.isEmpty()) {
                        if (name.length() > 3) {
                            if (pass.length() >= 8) {
                                if (pass.equals(repeatPass)) {
                                    if (matcher.matches()) {
                                        try {
                                            newUser.put("name", username.getText());
                                            newUser.put("email", email.getText());
                                            newUser.put("pass", password.getText());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        Singleton.getInstance().getSocket().emit("register", newUser);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Email is not valid",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "The password doesn't match",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Password should be at least 8 characyers long",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Username should be at least 3 characters long",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "All fields are required",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Make sure you are connected to the Internet!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove listeners.
        Singleton.getInstance().getSocket().off("register");
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }
}
