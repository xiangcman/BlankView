package com.blank.simple;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toBlank(View view) {
        Intent intent = new Intent(this, BlankActivity.class);
        switch (view.getId()) {
            case R.id.goToDragBtn:
                intent.putExtra("path", "blank.json");
                break;
            case R.id.goToDrag1Btn:
                intent.putExtra("path", "blank1.json");
                break;
            case R.id.goToDrag2Btn:
                intent.putExtra("path", "blank-stu.json");
                break;
            case R.id.goToDrag3Btn:
                intent.putExtra("path", "blank2.json");
                break;
        }
        startActivity(intent);
    }
}