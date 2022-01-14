package com.gae.scaffolder.plugin;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.salesforce.marketingcloud.MarketingCloudSdk;
import com.salesforce.marketingcloud.messages.push.PushMessageManager;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

  private static final String TAG = "FCMPlugin";

  @Override
  public void onNewToken(String token) {
    super.onNewToken(token);
    Log.d(TAG, "New Firebase token: " + token);
    FCMPlugin.sendTokenRefresh(token);
    MarketingCloudSdk.requestSdk(marketingCloudSdk -> marketingCloudSdk.getPushMessageManager().setPushToken(token));
  }

  /**
   * Called when message is received.
   *
   * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
   */
  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    Log.d(TAG, "==> MyFirebaseMessagingService onMessageReceived");

    // when "content-available" is present, it is a silent push sent from Marketing Cloud
    boolean isSilentPush = remoteMessage.getData().get("content-available") != null;

    Map<String, Object> data = buildNotificationData(remoteMessage, isSilentPush);

    if (PushMessageManager.isMarketingCloudPush(remoteMessage)) {
      if (!isSilentPush) {
        MarketingCloudSdk.requestSdk(marketingCloudSdk -> marketingCloudSdk.getPushMessageManager().handleMessage(remoteMessage));
      } else {
        FCMPlugin.sendPushPayload(data);
      }
    } else {
      FCMPlugin.sendPushPayload(data);
    }
  }

  private Map<String, Object> buildNotificationData(RemoteMessage remoteMessage, boolean isSilentPush) {
    Map<String, Object> data = new HashMap<>();

    for (String key : remoteMessage.getData().keySet()) {
      Object value = remoteMessage.getData().get(key);
      Log.d(TAG, "\tKey: " + key + " Value: " + value);
      data.put(key, value);
    }
    if (isSilentPush) {
      data.remove("title");
      data.remove("message");
      data.remove("alert");
      data.remove("content-available");
      data.remove("sound");
      data.put("isSilent", true);
    } else {
      data.put("isSilent", false);
    }
    Log.d(TAG, "\tNotification Data: " + data.toString());
    return data;
  }
}
