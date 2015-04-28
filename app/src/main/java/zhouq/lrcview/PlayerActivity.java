package zhouq.lrcview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;

import zhouq.lrcview.view.LrcScrollView;
import zhouq.lrcview.view.LrcView;


public class PlayerActivity extends Activity {

    private String path;
    private int duratoin;

    private LrcView lrcView;
    private Fragment mLrcFragment;
    private SeekBar seekBar;
    private Button playPauseBtn;

    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        duratoin = intent.getIntExtra("duration", 0);

        initPlayer();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateHandler.removeCallbacksAndMessages(null);
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        lrcView.reset();
    }

    private void initView() {
        lrcView = (LrcView) findViewById(R.id.lrcview);
        lrcView.setLrcContent(duratoin, path);
        lrcView.setOnSeekToListener(lrcViewSeekToListener);
        lrcView.setOnLrcClickListener(mLrcClickListener);

        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setMax(duratoin);
        seekBar.setOnSeekBarChangeListener(playerSeekbarListener);

        playPauseBtn = (Button) findViewById(R.id.play_pause_button);
        playPauseBtn.setOnClickListener(mPlayPauseClickListener);
    }

    private View.OnClickListener mPlayPauseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private LrcScrollView.OnLrcClickListener mLrcClickListener = new
            LrcScrollView.OnLrcClickListener() {
        @Override
        public void onClick() {
                mLrcFragment = getFragmentManager().findFragmentByTag
                        ("LRC");
                FragmentTransaction ft = getFragmentManager()
                        .beginTransaction();
                if (mLrcFragment == null){
                    mLrcFragment = LrcFragment.newInstance();
                }
                if (mLrcFragment.isVisible()) {
                    ft.remove(mLrcFragment).commit();
                } else {
                    ft.add(R.id.lrc_content,mLrcFragment,"LRC").commit();
                }
        }
    };

    private LrcScrollView.OnSeekToListener lrcViewSeekToListener = new LrcScrollView.OnSeekToListener() {
        @Override
        public void onSeekTo(int progress) {
            mPlayer.seekTo(progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener playerSeekbarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            lrcView.seekTo(progress, true, fromUser);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            updateHandler.removeMessages(UPDATE_SEEKBAR);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mPlayer.seekTo(seekBar.getProgress());
            updateHandler.sendEmptyMessageDelayed(UPDATE_SEEKBAR, 100);
        }
    };

    private void initPlayer() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(path);
            mPlayer.prepare();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    startPlay();
                }
            });

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playPauseBtn.setText("play");
                    seekBar.setProgress(0);
                    updateHandler.removeMessages(UPDATE_SEEKBAR);
                    lrcView.reset();
                    mPlayer.stop();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startPlay() {
        mPlayer.start();
        updateHandler.sendEmptyMessage(UPDATE_SEEKBAR);
        playPauseBtn.setText("pause");
    }

    private final int UPDATE_SEEKBAR = 0;
    Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            seekBar.setProgress(mPlayer.getCurrentPosition());
            updateHandler.sendEmptyMessageDelayed(UPDATE_SEEKBAR, 100);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
