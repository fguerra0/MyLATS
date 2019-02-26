package com.example.mylats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity
{
    EditText user_email, user_password;
    Button registerButton, loginButton;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        user_email = findViewById(R.id.email_user);
        user_password = findViewById(R.id.password_user);
        registerButton = findViewById(R.id.newUserButton);
        loginButton = findViewById(R.id.memberLoginButton);

        firebaseAuth = FirebaseAuth.getInstance();
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String email = user_email.getText().toString();
                String password = user_password.getText().toString();
                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
                {
                    Toast.makeText(getApplicationContext(), "Please fill in the required fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length() < 6)
                {
                    Toast.makeText(getApplicationContext(), "Password needs to be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                try {
                                    if (task.isSuccessful()) {
                                        finish();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    } else
                                        Toast.makeText(getApplicationContext(), "Email or password is wrong", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), AlreadyActivity.class));
            }
        });
        if(firebaseAuth.getCurrentUser() != null)
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

    }
}
