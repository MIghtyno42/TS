package uk.wjdp.mp3player;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.support.v4.view.GestureDetectorCompat;

import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.Map;

import uk.wjdp.mp3player.SongList.Song;

public class MainActivity extends AppCompatActivity {

    private GestureDetectorCompat GDetect;
    public static final int REQUEST_CODE = 1;

    SharedPreferences sharedprf;

    final String TAG = "MainActivity";

    String SongGrabberName = null;
    private PlayerService.PlayerBinder myPlayerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GDetect = new GestureDetectorCompat(this, new LearnGesture());
        Log.d(TAG, "onCreate");
        sharedprf = getSharedPreferences("TuneScoreData", Context.MODE_PRIVATE);
        Map<String,?> keys = sharedprf.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d("MAP HERE", entry.getKey()+": " + entry.getValue().toString());
        }




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_CODE  && resultCode  == RESULT_OK) {

                String requiredValue = data.getStringExtra("Key");
                SongGrabberName = requiredValue;
                Log.d("COMPLETIO", requiredValue);
                if (requiredValue != null ) {
                    myPlayerService.hop(requiredValue);
                }
            }
        } catch (Exception ex) {

        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.GDetect.onTouchEvent(event);
        return true;
    }

    private class LearnGesture extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.d("GESTUREZONE", "ACTIVATE PLAY");
            myPlayerService.play();
            return true;
        }


        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

            float distanceX = event2.getX() - event1.getX();
            float distanceY = event2.getY() - event1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                if (distanceX > 0) {
                    myPlayerService.next();
                }
                else {
                    myPlayerService.stop();
                }
                return true;
            }
            else{
                if (distanceY > 0){
                    Intent intent = (new Intent(MainActivity.this, GestureAssigner.class));
                    Bundle b = new Bundle();

                    String currentSong = myPlayerService.getTitle();
                    Log.d("BALLBREAKER", currentSong);
                    b.putString("song's name", currentSong);
                    intent.putExtras(b);
                    startActivity(intent);

                }
                else{

                    Log.d("reminder", "now in the gesture searcher");

                    Intent intent = new Intent(MainActivity.this, GestureSearcher.class);
                    startActivityForResult(intent, REQUEST_CODE);


                   //
                    //Jump to the selected song



                }
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent event1) {
            myPlayerService.pause();
            Log.d("GESTUREZONE","ZA WARUDO");

        }


        @Override
        public boolean onDown(MotionEvent event){
            return true;
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        Intent intent= new Intent(this, PlayerService.class);
        startService(intent);

        bindService(intent, playerServiceConnection, 0);

        registerReceiver(receiver, new IntentFilter(PlayerService.NOTIFICATION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        // Unbind so activity can sleep
        unbindService(playerServiceConnection);
    }

    private ServiceConnection playerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            // Store a ref to the service
            myPlayerService = (PlayerService.PlayerBinder) service;
            final SongList songList = getMedia();
            Log.d(TAG, "Songs: " + songList.song_list.size());
            for (int i = 0; i < songList.song_list.size(); i++){
                Song song = songList.song_list.get(i);
                songSelected(song);
            }
            myPlayerService.update_state();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            myPlayerService = null;
        }
    };


    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String callback = bundle.getString(PlayerService.CALLBACK);
                String artist = bundle.getString(PlayerService.SONG_ARTIST);
                String title = bundle.getString(PlayerService.SONG_TITLE);
                int queue = bundle.getInt(PlayerService.QUEUE);

                Boolean nextState = queue > 1;

            }
        }
    };


    protected SongList getMedia() {
        SongList songList = new SongList();

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        Log.d(TAG, "Scanning thru media");

        if (musicCursor != null && musicCursor.moveToFirst()) {

            int idColumn     = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn  = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);


            do {

                long songId = musicCursor.getLong(idColumn);
                String songTitle = musicCursor.getString(titleColumn);
                String songArtist = musicCursor.getString(artistColumn);
                String songPath = musicCursor.getString(pathColumn);
                Log.d(TAG, songTitle);
            songList.addSong(new Song(songId, songTitle, songArtist, songPath));
        } while(musicCursor.moveToNext());
        }

        musicCursor.close();
        return songList;
    }


    void songSelected(Song song) {
        myPlayerService.setup(song);
    }


}


