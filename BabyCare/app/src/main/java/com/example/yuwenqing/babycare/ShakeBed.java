package com.example.yuwenqing.babycare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ShakeBed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake_bed);

        Button button=(Button)findViewById(R.id.shakeBed);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ShakeBed.this,"Sure",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
