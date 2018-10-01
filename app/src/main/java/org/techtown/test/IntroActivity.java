package org.techtown.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class IntroActivity extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        handler = new Handler();
        handler.postDelayed(mrun, 2000);
    }

    Runnable mrun = new Runnable() {

        @Override
        public void run() {
            Intent HomeActivity = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(HomeActivity);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    };

    @Override
    public  void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(mrun);
    }
}
