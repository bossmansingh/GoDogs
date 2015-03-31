package singh.saurbh.godogs;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by ${SAURBAH} on ${10/29/14}.
 */
public class ReceiveNotification extends ParsePushBroadcastReceiver {


    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        Log.d("onPushReceive", intent.toString());
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
        Log.d("onPushDismiss", intent.toString());
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
        Log.d("onPushOpen", intent.toString());
        intent = new Intent(context, AddPost.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
