package com.example.osorekoxuan.cardiact;

/**
 * Created by osorekoxuan on 16/3/25.
 */
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomPushReceiver extends ParsePushBroadcastReceiver {
    private final String TAG = CustomPushReceiver.class.getSimpleName();

    private Intent parseIntent;

    public CustomPushReceiver() {
        super();
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);

        if (intent == null) {
            return;
        }
        //JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

        Bundle extras = intent.getExtras();
        String json = extras.getString("com.parse.Data");

        Log.e(TAG, "Push received: " + json);

        parseIntent = intent;

        parsePushJson(context, json);

    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }

    /**
     * Parses the push notification json
     *
     * @param context
     * @param json
     */
    private void parsePushJson(Context context, String json) {

        try {
            JSONObject data = new JSONObject(json);
            String title = data.getString("myTitle");
            String message = data.getString("message");
            String objectId = data.getString("objectId");

            Log.e(TAG, "message: " + message);

            Intent resultIntent = new Intent(context, FullscreenActivity.class);
            resultIntent.putExtras(parseIntent.getExtras());
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            showNotificationMessage(context, title, message, objectId,resultIntent);
        } catch (JSONException e) {
            Log.e(TAG, "Push message json exception: " + e.getMessage());
        }
    }


    /**
     * Shows the notification message in the notification bar
     * If the app is in background, launches the app
     *
     * @param context
     * @param title
     * @param message
     * @param intent
     */
    private void showNotificationMessage(Context context, String title, String message, String objectId, Intent intent) {

        NotificationUtils notificationUtils = new NotificationUtils(context);

        intent.putExtras(parseIntent.getExtras());

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        notificationUtils.showNotificationMessage(title, message, objectId, intent);

        Log.e(TAG, "Custom notification!");
    }
}