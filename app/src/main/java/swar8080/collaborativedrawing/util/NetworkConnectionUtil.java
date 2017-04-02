package swar8080.collaborativedrawing.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 *
 */

public class NetworkConnectionUtil {

    private static final int[] NETWORK_TYPES = {ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_ETHERNET};

    //todo check whether users are connected to internet
    public static boolean isConnectedToNetwork(Context context) {
        ConnectivityManager connManager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        for (int networkType : NETWORK_TYPES) {
            NetworkInfo info = connManager.getNetworkInfo(networkType);
            if (info != null && info.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }
}
