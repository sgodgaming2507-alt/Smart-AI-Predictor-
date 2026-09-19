package com.paglu.predictor.pro;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int OVERLAY_PERMISSION_REQ_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Android 15 ke liye safe UI layout taaki app bilkul crash na ho
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(0xFF0F0F19);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("Paglu AI Pro - Android 15");
        title.setTextColor(0xFF00F2FE);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        Button startBtn = new Button(this);
        startBtn.setText("START FLOATING OVERLAY");
        startBtn.setBackgroundColor(0xFF00F2FE);
        startBtn.setTextColor(0xFF111111);
        startBtn.setPadding(20, 20, 20, 20);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkOverlayPermissionAndStart();
            }
        });
        layout.addView(startBtn);

        setContentView(layout);
    }

    private void checkOverlayPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            startFloatingService();
        }
    }

    private void startFloatingService() {
        Intent serviceIntent = new Intent(this, FloatingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        // Service shuru hone ke baad app minimize ho jayegi aur overlay game ke upar aa jayega
        moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                startFloatingService();
            }
        }
    }
}
