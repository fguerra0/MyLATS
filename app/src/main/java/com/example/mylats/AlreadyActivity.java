package com.example.mylats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.*;
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
import com.google.firebase.auth.FirebaseUser;

public class AlreadyActivity extends AppCompatActivity
{
    EditText loginEmail, loginPassword;
    Button loginButton, newPassButton;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.userEmail);
        loginPassword = findViewById(R.id.userPassword);
        loginButton = findViewById(R.id.loginButton);
        newPassButton = findViewById(R.id.newPasswordButton);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(v == loginButton)
                    loginUser();
            }
        });
    }

    public void loginUser()
    {
        String Email = loginEmail.getText().toString().trim();
        String Password = loginPassword.getText().toString().trim();
        if(TextUtils.isEmpty(Email) || TextUtils.isEmpty(Password))
        {
            Toast.makeText(getApplicationContext(), "Please fill in the required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            currentUser = firebaseAuth.getCurrentUser();
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                        else
                        {
                            Toast.makeText(AlreadyActivity.this, "Could not login", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
