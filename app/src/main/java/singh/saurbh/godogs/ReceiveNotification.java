package singh.saurbh.godogs;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by ${SAURBAH} on ${10/29/14}.
 */
public class ReceiveNotification extends BroadcastReceiver {

    private String objectId;
    private static final int NOTIFICATION_ID = 1;
    public static int numMessages = 0;

    private static final String TAG = "ReceiveNotification";

    NotificationCompat.Builder mBuilder;
    Intent resultIntent;
    int mNotificationId = 001;
    Uri notifySound;

    String alert; // This is the message string that send from push console


    @Override
    public void onReceive(Context context, Intent intent) {

        //Get JSON data and put them into variables
        try {
            if (intent == null)
            {
                Log.d(TAG, "Receiver intent null");
            }
            else
            {
                String action = intent.getAction();
                Log.d(TAG, "got action " + action );
                if (action.equals("singh.saurbh.godogs.MESSAGE"))
                {
                    String channel = intent.getExtras().getString("Channels");
                    JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                    objectId = json.getString("objectId");
                    alert = json.getString("alert");
                    Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
                    Iterator itr = json.keys();
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals("objectId"))
                        {
//                            Intent pupInt = new Intent(context, SinglePostDisplay.class);
//                            pupInt.putExtra("objectId", key.equals("objectId"));
//                            pupInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
//                            context.getApplicationContext().startActivity(pupInt);
                        }
                        Log.d(TAG, "..." + key + " => " + json.getString(key));
                    }
                }
            }

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }


        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.push_icon); //You can change your icon
        mBuilder.setContentText(alert);
        mBuilder.setContentTitle(context.getString(R.string.app_name));
        mBuilder.setNumber(++numMessages);
        mBuilder.setAutoCancel(true);

// this is the activity that we will send the user, change this to anything you want
        resultIntent = new Intent(context, SinglePostDisplay.class);
        resultIntent.putExtra("objectId", objectId);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());

//
//        try {
//            if (intent == null)
//            {
//                Log.d(TAG, "Receiver intent null");
//            }
//            else
//            {
//                String action = intent.getAction();
//                Log.d(TAG, "got action " + action );
//                if (action.equals("singh.saurbh.godogs.CUSTOM_NOTIFICATION"))
//                {
//                    String channel = intent.getExtras().getString("Channels");
//                    JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
//
//                    Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
//                    Iterator itr = json.keys();
//                    while (itr.hasNext()) {
//                        String key = (String) itr.next();
//                        if (key.equals("objectId"))
//                        {
////                            Intent pupInt = new Intent(context, SinglePostDisplay.class);
////                            pupInt.putExtra("objectId", key.equals("objectId"));
////                            pupInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
////                            context.getApplicationContext().startActivity(pupInt);
//                        }
//                        Log.d(TAG, "..." + key + " => " + json.getString(key));
//                    }
//                }
//            }
//
//        } catch (JSONException e) {
//            Log.d(TAG, "JSONException: " + e.getMessage());
//        }
    }


//    @Override
//    protected void onPushOpen(Context context, Intent intent) {
//        super.onPushOpen(context, intent);
//
//        try {
//            if (intent == null)
//            {
//                Log.d(TAG, "Receiver intent null");
//            }
//            else
//            {
//                String action = intent.getAction();
//                Log.d(TAG, "got action " + action );
//                if (action.equals("com.parse.push.intent.RECEIVE"))
//                {
//                    String channel = intent.getExtras().getString("Channels");
//                    JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
//
//                    Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
//                    Iterator itr = json.keys();
//                    while (itr.hasNext()) {
//                        String key = (String) itr.next();
//                        if (key.equals("objectId"))
//                        {
//                            Intent pupInt = new Intent(context, SinglePostDisplay.class);
//                            pupInt.putExtra("objectId", key.equals("objectId"));
//                            pupInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
//                            context.getApplicationContext().startActivity(pupInt);
//                        }
//                        Log.d(TAG, "..." + key + " => " + json.getString(key));
//                    }
//                }
//            }
//
//        } catch (JSONException e) {
//            Log.d(TAG, "JSONException: " + e.getMessage());
//        }
////        Bundle extras = intent.getExtras();
////        String temp = null;
////        if (extras != null) {
////            temp = extras.getString("com.parse.Data");
////        }
////        JSONObject jsonObject;
////        try {
////            jsonObject = new JSONObject(temp);
////            objectId = jsonObject.getString("objectId");
////        } catch (JSONException e) {
////            e.printStackTrace();
////        }
////        generateNotification(context, "My Title", "Content");
////        intent = new Intent(context, SinglePostDisplay.class);
////        intent.putExtra("objectId", objectId);
////        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////        Intent i = new Intent(context, SinglePostDisplay.class);
////        i.putExtra("objectId", objectId);
////        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////        context.startActivity(i);
////        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
////        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
////        NotificationCompat.Builder xyz = new NotificationCompat.Builder(context);
////        xyz.setContentIntent(pIntent);
////        xyz.addAction(R.drawable.push_icon, "New Notification", pIntent);
////        try {
////            pIntent.send(context, 0, intent);
////        } catch (PendingIntent.CanceledException e) {
////            Log.e("Error", e.getMessage());
////        }
//    }
//
//    private void generateNotification(Context context, String title, String contentText) {
//        Intent intent = new Intent(context, SinglePostDisplay.class);
//        intent.putExtra("objectId", objectId);
//        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
//
//        NotificationManager mNotifM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.push_icon).setContentTitle(title).setContentText(contentText).setNumber(++numMessages);
//        mBuilder.setContentIntent(contentIntent);
//        mNotifM.notify(NOTIFICATION_ID, mBuilder.build());
//
//    }
}
