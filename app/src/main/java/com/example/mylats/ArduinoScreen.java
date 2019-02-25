package com.example.mylats;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ArduinoScreen extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino);
        Button startButton = findViewById(R.id.trigger);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                startArduino();
            }
        });
    }

    private void startArduino()
    {
        Intent launchArduino = new Intent(this, ConnectActivity.class);
        startActivity(launchArduino);

    }
}
