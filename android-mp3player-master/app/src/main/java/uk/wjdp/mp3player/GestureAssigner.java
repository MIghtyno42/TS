package uk.wjdp.mp3player;

import android.content.Context;

import android.content.SharedPreferences;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.GestureDetectorCompat;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;



public class GestureAssigner extends AppCompatActivity {

    private GestureDetectorCompat GDetect;
    String songname = null;
    String gestureString ="";
    SharedPreferences sharedprf = null;
    SharedPreferences.Editor editor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_assigner);

        GDetect = new GestureDetectorCompat(this, new GestureAssigner.LearnGesture());

        Bundle b = getIntent().getExtras();

        if (b !=  null){
            songname = b.getString("song's name");
        }
        sharedprf = getSharedPreferences("TuneScoreData", Context.MODE_PRIVATE);
        editor = sharedprf.edit();


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

            Log.d("THIS IS GOING IN", gestureString);
            editor.putString(gestureString, songname);
            editor.commit();

                finish();

                onBackPressed();



        }


    }


}
