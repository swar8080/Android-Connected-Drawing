package swar8080.collaborativedrawing.connection;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetConnectionStatus extends BroadcastReceiver {


    public interface InternetConnectionChangeListener{
        void onInternetConnectionChanged(boolean isConnectedOrConnecting);
    }

    private InternetConnectionChangeListener listener;
    private Context context;

    public InternetConnectionStatus(Context context){
        this.context = context;

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this, filter);
    }


    public void registerInternetConnectionChangeListener(InternetConnectionChangeListener listener){
        this.listener = listener;
    }


    //https://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html#MonitorChanges
    public boolean isConnectedOrConnecting(){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //don't update the listener if the broadcast is sticky (sent immediately after registering the intent)
        if (intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION
                && listener != null
                && !this.isInitialStickyBroadcast())
        {
            listener.onInternetConnectionChanged(isConnectedOrConnecting());
        }
    }
}