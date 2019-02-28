package de.franziskaneum;

import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import de.franziskaneum.vplan.VPlan;
import de.franziskaneum.vplan.VPlanManager;
import de.franziskaneum.vplan.VPlanNotificationManager;

import static de.franziskaneum.vplan.VPlanNotificationManager.ACTION_NEW_VPLAN_AVAILABLE;

/**
 * Created by Niko on 22.12.2016.
 */

public class FranzFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        Log.d("main", "Token Refreshed: " + s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String from = remoteMessage.getFrom();

        if ("/topics/vplan".equalsIgnoreCase(from)) {
            Map<String, String> data = remoteMessage.getData();
            String key = "new_vplan_available";
            if (data.containsKey(key) && "true".equalsIgnoreCase(data.get(key))) {
                VPlanManager.getInstance().getVPlanAsync(VPlanManager.Mode.IF_MODIFIED, new FranzCallback() {
                    @Override
                    public void onCallback(int status, Object... objects) {
                        if (Status.OK == status && objects.length > 1 && objects[1] != null) {
                            VPlan vplan = (VPlan) objects[1];

                            Intent broadcastIntent = new Intent(ACTION_NEW_VPLAN_AVAILABLE);
                            broadcastIntent.putExtra(VPlan.EXTRA_VPLAN, (Parcelable) vplan);
                            sendBroadcast(broadcastIntent);

                            VPlanNotificationManager.getInstance().makeNotificationAsync(vplan);
                        } else
                            VPlanNotificationManager.getInstance().makeNotificationAsync(VPlanNotificationManager.Mode.DOWNLOAD);
                    }
                });
            }
        }
    }
}
