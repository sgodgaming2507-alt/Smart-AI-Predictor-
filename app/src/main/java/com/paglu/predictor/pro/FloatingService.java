package com.paglu.predictor.pro;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.core.app.NotificationCompat;

public class FloatingService extends Service {
    private WindowManager windowManager;
    private WebView webView;
    private WindowManager.LayoutParams params;
    private static final String CHANNEL_ID = "FloatingServiceChannel";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Paglu AI Pro Running")
                .setContentText("Floating overlay is active")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .build();

        startForeground(1, notification);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        webView = new WebView(this);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setBackgroundColor(0x00000000); // Transparent background

        webView.addJavascriptInterface(new AppBridge(), "Android");
        webView.loadUrl("file:///android_asset/injector.html");

        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        int windowSize = (int) (250 * getResources().getDisplayMetrics().density);

        params = new WindowManager.LayoutParams(
                windowSize,
                windowSize,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 100;

        windowManager.addView(webView, params);
    }

    public class AppBridge {
        @JavascriptInterface
        public void closeApp() {
            stopSelf();
        }

        @JavascriptInterface
        public void minimizePanel() {
            if(webView != null) {
                webView.setVisibility(View.GONE);
            }
        }

        @JavascriptInterface
        public void requestFocusable(final boolean focusable) {
            // Keyboard kholne ke liye window ko focusable banate hain
            if (windowManager != null && webView != null) {
                if (focusable) {
                    params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                } else {
                    params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                }
                windowManager.updateViewLayout(webView, params);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Floating Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            windowManager.removeView(webView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
