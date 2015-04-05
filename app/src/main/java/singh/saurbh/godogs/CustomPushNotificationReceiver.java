package singh.saurbh.godogs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * Created by ${SAURBAH} on ${10/29/14}.
 */
public class CustomPushNotificationReceiver extends ParsePushBroadcastReceiver {

    protected static String objectId;
    public static int numMessages = 0;
    Context mContext;
    private static final String TAG = "TestPushNotification";

    NotificationCompat.Builder mBuilder;
    Intent resultIntent;

    String alert; // This is the message string that send from push console

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        mContext = context;
        Log.d(TAG, "onPushReceive");

        try {
            if (intent == null)
                Log.d(TAG, "Receiver intent null");
            else {
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                objectId = json.getString("objectId");
                alert = json.getString("alert");
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }

        //You can specify sound
        Uri notifySound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.push_icon);
        mBuilder.setContentTitle(context.getString(R.string.app_name));
        mBuilder.setContentText(alert);
        mBuilder.setTicker(alert);
        mBuilder.setNumber(numMessages);
        mBuilder.setSound(notifySound);
        mBuilder.setAutoCancel(true);

        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.parse.push.intent.OPEN"), 0x8000000);

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        int NOTIFICATION_ID = 1;
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
//        super.onPushOpen(context, intent);

        ParseAnalytics.trackAppOpenedInBackground(intent);
        numMessages = 0;

        // this is the activity that we will send the user, change this to anything you want
        resultIntent = new Intent(context, SinglePostDisplay.class);
        resultIntent.putExtra("objectId", objectId);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (context != null) {
            context.startActivity(resultIntent);
        }
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        numMessages++;
        return null;
    }
}