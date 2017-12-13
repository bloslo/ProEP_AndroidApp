package com.ninjacoders.show_me;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button btnLogin;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.usernameInput);
        password = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.login);
        btnRegister = findViewById(R.id.register);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject user = new JSONObject();

                try {
                    user.put("name", username.getText());
                    user.put("pass", password.getText());
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

//                Singleton.getInstance().getSocket().emit("login",  user);
//
//                Singleton.getInstance().getSocket().on("login", new Emitter.Listener() {
//                    @Override
//                    public void call(Object... args) {
//                        // Check if login is successful.
//                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                        startActivity(intent);
//                    }
//                });
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
}
