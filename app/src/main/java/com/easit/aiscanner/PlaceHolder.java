package com.easit.aiscanner;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;


    /*
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.content.FileProvider;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;
import androidx.core.content.FileProvider;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.HashMap;

import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.dictionaries.Share;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

 */

@TargetApi(Build.VERSION_CODES.KITKAT)
class PoemAudioExport {
    /*
    private static final String TAG = Constants.TAG + PoemAudioExport.class.getSimpleName();
    private static final int EXPORT_PROGRESS_NOTIFICATION_ID = 1336;
    private static final int EXPORT_FINISH_NOTIFICATION_ID = 1337;

    private static final String EXPORT_FOLDER_PATH = "export";
    private static final String TEMP_AUDIO_FILE = "poem.wav";

    private final Context mContext;
    private final Handler mHandler;

    PoemAudioExport(Context context) {
        mContext = context;
        mHandler = new Handler();
    }

    void speakToFile(TextToSpeech textToSpeech, String text) {
        final File audioFile = getAudioFile();
        if (audioFile == null) {
            notifyPoemAudioFailed();
        } else {
            EventBus.getDefault().register(this);
            notifyPoemAudioInProgress();
            String textToRead = text.substring(0, Math.min(text.length(), TextToSpeech.getMaxSpeechInputLength()));
            Completable.fromRunnable(() -> deleteExistingAudioFile(audioFile)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> speakToFile(textToSpeech, textToRead, audioFile));
        }
    }

    @WorkerThread
    private void deleteExistingAudioFile(File audioFile) {
        if (audioFile.exists()) {
            if (audioFile.delete()) {
                Log.v(TAG, "Deleted existing file " + audioFile + ".");
            } else {
                Log.v(TAG, "Couldn't delete existing file " + audioFile + ". What will happen next?");
            }
        }
    }

    @MainThread
    private void speakToFile(TextToSpeech textToSpeech, String textToRead, File audioFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speakToFile21(textToSpeech, textToRead, audioFile);
        } else {
            speakToFile4(textToSpeech, textToRead, audioFile);
        }
    }

    private void speakToFile4(TextToSpeech textToSpeech, String text, File audioFile) {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TEMP_AUDIO_FILE);
        //noinspection deprecation
        textToSpeech.synthesizeToFile(text, params, audioFile.getAbsolutePath());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speakToFile21(TextToSpeech textToSpeech, String text, File audioFile) {
        Bundle params = new Bundle();
        textToSpeech.synthesizeToFile(text, params, audioFile, TEMP_AUDIO_FILE);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onTtsUtteranceCompleted(Tts.OnUtteranceCompleted event) {
        Log.d(TAG, "onTtsUtteranceCompleted() called with: " + "event = [" + event + "]");
        if (TEMP_AUDIO_FILE.equals(event.utteranceId)) {
            mHandler.post(() -> {
                EventBus.getDefault().unregister(this);
                File audioFile = getAudioFile();
                if (event.success && audioFile != null && audioFile.exists())
                    notifyPoemAudioReady();
                else
                    notifyPoemAudioFailed();
            });
        }
    }

    private void cancelNotifications() {
        Log.v(TAG, "cancelNotifications");
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(EXPORT_PROGRESS_NOTIFICATION_ID);
        notificationManager.cancel(EXPORT_FINISH_NOTIFICATION_ID);
    }

    private void notifyPoemAudioInProgress() {
        Log.v(TAG, "notifyPoemAudioInProgress");
        cancelNotifications();
        Notification notification = new NotificationCompat.Builder(mContext).setAutoCancel(false).setOngoing(true)
                .setContentIntent(getMainActivityIntent())
                .setContentTitle(mContext.getString(R.string.share_poem_audio_progress_notification_title))
                .setContentText(mContext.getString(R.string.share_poem_audio_progress_notification_message))
                .setSmallIcon(Share.getNotificationIcon()).build();
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(EXPORT_PROGRESS_NOTIFICATION_ID, notification);
    }

    private void notifyPoemAudioReady() {
        Log.v(TAG, "notifyPoemAudioReady");
        cancelNotifications();
        PendingIntent shareIntent = getFileShareIntent();
        Notification notification = new NotificationCompat.Builder(mContext).setAutoCancel(true)
                .setContentIntent(shareIntent)
                .setContentTitle(mContext.getString(R.string.share_poem_audio_ready_notification_title))
                .setContentText(mContext.getString(R.string.share_poem_audio_ready_notification_message))
                .setSmallIcon(Share.getNotificationIcon())
                .addAction(Share.getShareIconId(), mContext.getString(R.string.share), shareIntent).build();
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(EXPORT_FINISH_NOTIFICATION_ID, notification);
    }

    private void notifyPoemAudioFailed() {
        Log.v(TAG, "notifyPoemAudioFailed");
        cancelNotifications();
        Notification notification = new NotificationCompat.Builder(mContext).setAutoCancel(true)
                .setContentTitle(mContext.getString(R.string.share_poem_audio_error_notification_title))
                .setContentText(mContext.getString(R.string.share_poem_audio_error_notification_message))
                .setContentIntent(getMainActivityIntent()).setSmallIcon(Share.getNotificationIcon()).build();
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(EXPORT_FINISH_NOTIFICATION_ID, notification);
    }

    private PendingIntent getFileShareIntent() {
        // Bring up the chooser to share the file.
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        Uri uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".fileprovider",
                getAudioFile());
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType("audio/x-wav");
        return PendingIntent.getActivity(mContext, 0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getMainActivityIntent() {
        Intent intent = new Intent(mContext, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Nullable
    private File getAudioFile() {
        File exportFolder = new File(mContext.getFilesDir(), EXPORT_FOLDER_PATH);
        if (!exportFolder.exists() && !exportFolder.mkdirs()) {
            Log.v(TAG, "Couldn't find or create export folder " + exportFolder);
            return null;
        }
        return new File(exportFolder, TEMP_AUDIO_FILE);
    }

     */

}

