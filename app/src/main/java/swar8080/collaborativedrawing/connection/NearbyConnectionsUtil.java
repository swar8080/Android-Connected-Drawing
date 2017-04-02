package swar8080.collaborativedrawing.connection;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

import swar8080.collaborativedrawing.R;

/**
 *
 */

public class NearbyConnectionsUtil {

    private static String cachedServiceId;

    public static String getServiceId(Context context){
        if (cachedServiceId == null)
            cachedServiceId = context.getString(R.string.service_id);

        return cachedServiceId;
    }
}
