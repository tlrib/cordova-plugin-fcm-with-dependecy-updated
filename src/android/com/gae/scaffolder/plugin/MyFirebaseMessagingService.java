package com.gae.scaffolder.plugin;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
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
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
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

        if (PushwooshFcmHelper.isPushwooshMessage(remoteMessage)) {
            PushwooshFcmHelper.onMessageReceived(this, remoteMessage);
        } else {
            FCMPlugin.sendPushPayload(data);
        }
    }
}
