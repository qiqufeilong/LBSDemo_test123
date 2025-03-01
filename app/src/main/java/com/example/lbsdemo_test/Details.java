package com.example.lbsdemo_test;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Details extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // 获取传递过来的 Intent
        Intent intent = getIntent();
        if (intent != null) {
            // 从 Intent 中获取 Equipment 对象
            Equipment equipment = (Equipment) intent.getSerializableExtra("equipment");

            if (equipment != null) {
                // 获取布局中的控件
                TextView textViewLatitude = findViewById(R.id.textViewLatitude);
                TextView textViewLongitude = findViewById(R.id.textViewLongitude);
                TextView textViewCustomerName = findViewById(R.id.textViewCustomerName);
                TextView textViewCustomerPhone = findViewById(R.id.textViewCustomerPhone);
                TextView textViewUserAddr = findViewById(R.id.textViewUserAddr);

                // 设置控件的文本内容
                textViewLatitude.setText("Latitude: " + equipment.getLatitude());
                textViewLongitude.setText("Longitude: " + equipment.getLongitude());
                textViewCustomerName.setText("Customer Name: " + equipment.getCustomerName());
                textViewCustomerPhone.setText("Customer Phone: " + equipment.getCustomerPhone());
                textViewUserAddr.setText("User Address: " + equipment.getUserAddr());
            }
        }
    }
}