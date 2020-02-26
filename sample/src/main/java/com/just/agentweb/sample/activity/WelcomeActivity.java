package com.just.agentweb.sample.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.just.agentweb.sample.R;

import static com.just.agentweb.sample.activity.MainActivity.FLAG_GUIDE_DICTIONARY_USE_IN_FRAGMENT;

public class WelcomeActivity extends AppCompatActivity {

   // LottieAnimationView lottieAnimationView ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!this.isTaskRoot() && getIntent() != null) {
            String action = getIntent().getAction();
            if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                finish();
                return;
            }
        } else {

            setContentView(R.layout.activity_welcome);
           //lottieAnimationView = findViewById(R.id.splash_animation);
            //startAnimation(lottieAnimationView, "7782-drone.json");

            CommonActivity.startActivity(this,"");
            finish();
          /* new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();

                }
            },2500);*/

        }

    }

   /* private void startAnimation(LottieAnimationView mLottieAnimationView, String animationName) {
        mLottieAnimationView.setAnimation(animationName);
        mLottieAnimationView.playAnimation();
    }
    private void cancelAnimation(LottieAnimationView mLottieAnimationView) {
        if (mLottieAnimationView != null) {
            mLottieAnimationView.cancelAnimation();
        }
    }*/
}
