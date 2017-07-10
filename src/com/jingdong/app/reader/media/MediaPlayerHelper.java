package com.jingdong.app.reader.media;

import java.io.IOException;
import java.util.Locale;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.text.TextUtils;
import android.webkit.URLUtil;

public class MediaPlayerHelper {
    private static MediaPlayer mediaPlayer;
    public static String playSource = "";
    private static boolean isPrepared = false;

    private static MediaPlayer instance() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    playSource = "";
                }
                
            });
            mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    isPrepared = true;
                }
                
            });
        }
        return mediaPlayer;
    }
    
    public static String getLocalAudioPath(String audioDir, String fileUrl) {
        if (fileUrl.toLowerCase(Locale.getDefault()).startsWith("http") && !TextUtils.isEmpty(audioDir)) {
            String fileName = URLUtil.guessFileName(fileUrl, null, null);
            return audioDir + fileName;
            
        } else {
            return fileUrl;
        }
    }
    
    public static boolean isPlaying(String source) {
        if (mediaPlayer == null || TextUtils.isEmpty(playSource) || TextUtils.isEmpty(source)) {
            return false;
        }
        if (playSource.equals(source) && mediaPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    public static boolean play(String source) {
        if (playSource.equals(source)) {
            if (instance().isPlaying()) {
                instance().pause();
                return false;
            } else {
                instance().start();
            }

        } else {
            playSource = source;
            try {
                isPrepared = false;
                instance().reset();
                instance().setDataSource(source);
                instance().prepare();
                instance().start();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    
    public static int getDuration() {
        if (isPrepared) {
            return instance().getDuration();
        } else {
            return 0;
        }
    }
    
    public static int getCurrentPosition() {
        if (isPrepared) {
            return instance().getCurrentPosition();
        } else {
            return 0;
        }
    }
    
    public static void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        playSource = "";
        mediaPlayer = null;
        isPrepared = false;
    }
}
