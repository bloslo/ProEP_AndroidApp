package com.ninjacoders.show_me;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Ack;
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
                JSONObject data = (JSONObject) args[0];

                if (data.has("succeed")) {
                    try {
                        Log.i(TAG, data.getString("succeed"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (data.has("name")) {
                    Log.i(TAG, "Registration successful");
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
}
