package com.ninjacoders.show_me;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

public class RegisterActivity extends AppCompatActivity {

    private Button register;
    private EditText username;
    private EditText email;
    private EditText password;

    private static final String TAG = "Register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        register = findViewById(R.id.regButton);
        username = findViewById(R.id.registerUsername);
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.registerPassword);

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
                JSONObject newUser = new JSONObject();

                try {
                    newUser.put("name", username.getText());
                    newUser.put("email", email.getText());
                    newUser.put("pass", password.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Singleton.getInstance().getSocket().emit("register", newUser);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove listeners.
        Singleton.getInstance().getSocket().off("register");
    }
}
