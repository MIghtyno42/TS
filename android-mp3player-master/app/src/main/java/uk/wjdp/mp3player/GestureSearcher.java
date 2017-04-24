package uk.wjdp.mp3player;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.GestureDetectorCompat;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;


import java.io.InputStreamReader;

public class GestureSearcher extends AppCompatActivity {

    private GestureDetectorCompat GDetect;
    String gestureString = "";

    SharedPreferences sharedprf = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_searcher);
        sharedprf = getSharedPreferences("TuneScoreData", Context.MODE_PRIVATE);

        GDetect = new GestureDetectorCompat(this, new GestureSearcher.LearnGesture());

    }





    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.GDetect.onTouchEvent(event);

        return true;
    }

    class LearnGesture extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            gestureString = gestureString + "TAP";
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

            float distanceX = event2.getX() - event1.getX();
            float distanceY = event2.getY() - event1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                if (distanceX > 0) {
                    gestureString = gestureString + "RIGHT";
                    Log.d("response",gestureString);

                } else {
                    gestureString = gestureString + "LEFT";
                    Log.d("response",gestureString);

                }
                return true;
            }
            else{
                if (distanceY > 0){
                    gestureString = gestureString + "DOWN";
                    Log.d("response",gestureString);
                }
                else{
                    gestureString = gestureString + "UP";
                    Log.d("response",gestureString);
                }
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent event1) {

                String songselected = sharedprf.getString(gestureString, null);
                Intent intent = getIntent();
                intent.putExtra("Key",songselected);
                setResult(RESULT_OK, intent);
                finish();

                onBackPressed();



        }


    }


}
