package com.example.mylats;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PersonalInfo extends AppCompatActivity {

    EditText text_name, text_email, text_weight, text_age;
    Button info_submit_button;
    Button main_button;
    Spinner gender_spinner;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        text_email = findViewById(R.id.EditTextEmail);
        text_name = findViewById(R.id.EditTextName);
        text_weight = findViewById(R.id.EditTextWeight);
        text_age = findViewById(R.id.EditTextAge);
        info_submit_button = findViewById(R.id.ButtonSendInfo);
        gender_spinner = findViewById(R.id.SpinnerPersonalInfo);
        main_button = findViewById(R.id.GoBackMain);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        info_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = text_email.getText().toString();
                String name = text_name.getText().toString();
                String weight = text_weight.getText().toString();
                String age = text_age.getText().toString();
                mDatabase.child("Name").setValue(name);
                mDatabase.child("Email").setValue(email);
                mDatabase.child("Weight").setValue(weight);
                mDatabase.child("Age").setValue(age);
                Toast.makeText(getApplicationContext(), "Personal Information Updated", Toast.LENGTH_SHORT).show();
            }
        });

        main_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

}

