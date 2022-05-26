package com.example.hanumanchalisa;

import static com.example.hanumanchalisa.ApplicationClass.ACTION_NEXT;
import static com.example.hanumanchalisa.ApplicationClass.ACTION_PLAY;
import static com.example.hanumanchalisa.ApplicationClass.ACTION_PREV;
import static com.example.hanumanchalisa.ApplicationClass.CHANNEL_ID_2;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    TextView playerPosition, playerDuration, title;
    SeekBar seekbar;
    ImageView btn_play;
    MediaPlayer mediaPlayer;
    Handler handler = new Handler();
    Runnable runnable;
    boolean isPlaying = false;
    ScrollView scrolltext;
    Notification notification;
    MediaPlayerService mediaPlayerService;
    NotificationManager notificationManager;
    AdView adView;
    private AdView mAdView;
    MediaSessionCompat mediaSession;
    private int seekForwardTime = 10 * 1000;
    private static final int REQUEST_CODE = 100;
    private int seekBackwardTime = 10 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.statusbar_color));
        startService(new Intent(this, KillNotificationService.class));
        setContentView(R.layout.activity_main);
        if (!isconnected()) {
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.networkstate);
            dialog.setCancelable(false);
            Button btn_reftesh = dialog.findViewById(R.id.btn_reftesh);
            btn_reftesh.setOnClickListener(v -> {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            });
            dialog.show();
        } else {

            playerPosition = findViewById(R.id.playerPosition);
            playerDuration = findViewById(R.id.playerDuration);
            scrolltext = findViewById(R.id.scrolltext);
            MobileAds.initialize(this);

            mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            seekbar = findViewById(R.id.seekbar);
            btn_play = findViewById(R.id.btn_play);
            title = findViewById(R.id.title);
            mediaPlayer = MediaPlayer.create(this, Uri.parse("https://firebasestorage.googleapis.com/v0/b/hanuman-chalisa-6e997.appspot.com/o/mp3%2F02%20hanuman%20chalisha.mp3?alt=media&token=33d59f36-ce17-4fe2-9959-0917c48e6819"));
            mediaSession = new MediaSessionCompat(this, "PlayerAudio");
            runnable = new Runnable() {
                @Override
                public void run() {
                    seekbar.setProgress(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 500);
                }
            };
            findViewById(R.id.btn_info).setOnClickListener(view -> {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.dialog_info);
                dialog.setCancelable(true);

                dialog.findViewById(R.id.shareviaFb).setOnClickListener(shareWithService("facebook"));
                dialog.findViewById(R.id.shareviagoogle_plus).setOnClickListener(shareWithService("apps.plus"));
                dialog.findViewById(R.id.shareviatwitter).setOnClickListener(shareWithService("twitter"));
                dialog.findViewById(R.id.shareviawhatsapp).setOnClickListener(shareWithService("whatsapp"));
                RatingBar rateus = dialog.findViewById(R.id.rateus);
                rateus.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                        final String appPackageName = getApplication().getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                });
                dialog.show();

            });
            mediaPlayer.start();
            playClicked();
            TextView Lyrics_hanuman = findViewById(R.id.Lyrics_hanuman);
            Lyrics_hanuman.setText(Html.fromHtml(Hanuman_Lyrics.Hanuman_Lyrics));

            seekbar.setMax(mediaPlayer.getDuration());
            handler.postDelayed(runnable, 0);
            int duration = mediaPlayer.getDuration();
            String sduration = convertFormat(duration);
            playerDuration.setText(sduration);
            btn_play.setOnClickListener(v -> {
                playClicked();
            });
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress);
                    }
                    playerPosition.setText(convertFormat(mediaPlayer.getCurrentPosition()));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mediaplayerComplet();

        }
    }
    private View.OnClickListener shareWithService(final String service) {
        return v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_message), getString(R.string.app_name), getApplication().getPackageName()));
            PackageManager pm = getApplication().getPackageManager();
            List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent, 0);
            boolean isFound=false;
            for (final ResolveInfo app : activityList) {
                if ((app.activityInfo.name).contains(service)) {
                    isFound=true;
                    final ActivityInfo activity = app.activityInfo;
                    final ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
                    shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    shareIntent.setComponent(name);
                    startActivity(shareIntent);
                    break;
                }
            }
            if(!isFound)
                Toast.makeText(MainActivity.this, "Please install app to share", Toast.LENGTH_SHORT).show();
        };

    }
    private void mediaplayerComplet() {
        mediaPlayer.setOnCompletionListener(mp -> {
            btn_play.setImageResource(R.drawable.btn_play);
            mediaPlayer.seekTo(0);
            showNotification(R.drawable.btn_play);
        });
    }

    @SuppressLint("DefaultLocale")
    private String convertFormat(int duration) {
        return String.format("%02d:%02d"
                , TimeUnit.MILLISECONDS.toMinutes(duration)
                , TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    @Override
    public void onBackPressed() {
        btn_play.setImageResource(R.drawable.btn_play);
        mediaPlayer.pause();
        handler.removeCallbacks(runnable);
        notificationManager.cancelAll();
        super.onBackPressed();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MediaPlayerService.Mybinder mybinder = (MediaPlayerService.Mybinder) iBinder;
        mediaPlayerService = mybinder.getService();
        mediaPlayerService.setCallBack(MainActivity.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mediaPlayerService = null;
    }


    @Override
    public void nextClicked() {

        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            if (currentPosition + seekForwardTime <= mediaPlayer.getDuration()) {
                mediaPlayer.seekTo(currentPosition + seekForwardTime);
            } else {
                mediaPlayer.seekTo(mediaPlayer.getDuration());
            }
        }
    }

    @Override
    public void prevClicked() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            if (currentPosition - seekBackwardTime >= 0) {
                mediaPlayer.seekTo(currentPosition - seekBackwardTime);
            } else {
                mediaPlayer.seekTo(0);
            }
        }
    }

    @Override
    public void playClicked() {
        if (!isPlaying) {
            isPlaying = true;
            btn_play.setImageResource(R.drawable.btn_pause);
            showNotification(R.drawable.btn_pause);
            mediaPlayer.start();
            seekbar.setMax(mediaPlayer.getDuration());
            handler.postDelayed(runnable, 0);
        } else {
            isPlaying = false;
            btn_play.setImageResource(R.drawable.btn_play);
            showNotification(R.drawable.btn_play);
            mediaPlayer.pause();
            handler.removeCallbacks(runnable);
        }
    }
    public void showNotification(int PlayPauseBtn) {
        Intent notificationIntent = new Intent(MainActivity. this, MainActivity. class );
        notificationIntent.addCategory(Intent. CATEGORY_LAUNCHER ) ;
        notificationIntent.setAction(Intent. ACTION_MAIN ) ;
        notificationIntent.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP | Intent. FLAG_ACTIVITY_SINGLE_TOP );
        PendingIntent resultIntent = PendingIntent. getActivity (MainActivity. this, 0 , notificationIntent , 0 ) ;

//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent prevIntent = new Intent(this, NotificationReciver.class).setAction(ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(this, NotificationReciver.class).setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReciver.class).setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap picture = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bg);
        notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(R.drawable.music)
                .setLargeIcon(picture)
                .setContentTitle("Hanuman Chalisa")
                .setContentText("Hari Cassettes")
                .addAction(R.drawable.previous, "previous", prevPendingIntent)
                .addAction(PlayPauseBtn, "play", playPendingIntent)
                .addAction(R.drawable.next, "next", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2)
                        .setMediaSession(mediaSession.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(resultIntent)
                .setAutoCancel(false)
                .build();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
    @Override
    public void onResume() {
        if (adView != null) {
            adView.resume();
        }
        super.onResume();
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
        unbindService(this);
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        stopService(new Intent(MainActivity.this,MediaPlayerService.class));
        super.onDestroy();
    }

    private boolean isconnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}