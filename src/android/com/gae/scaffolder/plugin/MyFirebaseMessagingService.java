package com.gae.scaffolder.plugin;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.outsystemsenterprise.sonaedev.CartaoContinente.MainActivity;
import com.outsystemsenterprise.sonaedev.CartaoContinente.R;
import com.pushwoosh.firebase.PushwooshFcmHelper;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMPlugin";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New token: " + token);
        FCMPlugin.sendTokenRefresh(token);
        PushwooshFcmHelper.onTokenRefresh(token);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "==> MyFirebaseMessagingService onMessageReceived");
        
        if(remoteMessage.getNotification() != null){
            Log.d(TAG, "\tNotification Title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "\tNotification Message: " + remoteMessage.getNotification().getBody());
        }
        
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("wasTapped", false);
        
        if(remoteMessage.getNotification() != null){
            data.put("title", remoteMessage.getNotification().getTitle());
            data.put("body", remoteMessage.getNotification().getBody());
        }

        for (String key : remoteMessage.getData().keySet()) {
            Object value = remoteMessage.getData().get(key);
            Log.d(TAG, "\tKey: " + key + " Value: " + value);
            data.put(key, value);
        }

        Log.d(TAG, "\tNotification Data: " + data.toString());
        
        // buildNotification("MEA_CARD_NOTIFICATION", "New card registered", "check it out");

        if (PushwooshFcmHelper.isPushwooshMessage(remoteMessage)) {
            //this is a Pushwoosh push, SDK will handle it automatically
            PushwooshFcmHelper.onMessageReceived(this, remoteMessage);
        } else {
            FCMPlugin.sendPushPayload(data);
        }
    }
    // [END receive_message]

    private void buildNotification(String channel, String title, String message) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        createNotificationChannel(this, channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.apdu_service_banner)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(69, builder.build());
    }

    private void createNotificationChannel(Context context, String channelId) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(channelId);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
