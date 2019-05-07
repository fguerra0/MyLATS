package com.example.mylats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
{
    android.support.v7.widget.Toolbar toolbar;
    TextView textView;
    Button btnDeleteUser, btnLogout, btnBluetooth, btnArduino, btnInfo;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView1);
        btnDeleteUser =findViewById(R.id.deleteUser);
        btnLogout = findViewById(R.id.logout);
        btnBluetooth = findViewById(R.id.connect);
        btnArduino = findViewById(R.id.arduino);
        btnInfo = findViewById(R.id.personal);
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if(user != null)
        {
            String Email = user.getEmail();
            textView.setText("Hi " + Email);
        }

        btnDeleteUser.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(user != null){
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(),"User deleted",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                        finish();
                                    }
                                }
                            });
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                firebaseAuth.signOut();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        });

        btnBluetooth.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), HeartSensor.class));
                finish();
            }
        });

        btnArduino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ArduinoScreen.class));
                finish();
            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), PersonalInfo.class));
            }
        });

    }
}
