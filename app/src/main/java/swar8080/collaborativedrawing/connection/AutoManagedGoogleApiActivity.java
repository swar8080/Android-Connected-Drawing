package swar8080.collaborativedrawing.connection;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 *
 */

public abstract class AutoManagedGoogleApiActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private final String LOG_TAG = getClass().getSimpleName();

    protected GoogleApiClient mGoogleApiClient;
    protected abstract GoogleApiClient.Builder getGoogleApiClientBuilder();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleApiClient.Builder childBuilder = getGoogleApiClientBuilder();
        childBuilder.enableAutoManage(this,this);

        mGoogleApiClient = childBuilder.build();
        mGoogleApiClient.registerConnectionCallbacks(this);
        mGoogleApiClient.registerConnectionFailedListener(this);
    }

    protected final void disconnectAndStartActivity(Intent intent){
        //addresses issue with starting a new activity that also connects to google api
        mGoogleApiClient.stopAutoManage(this);
        startActivity(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG_TAG, "Connected to GoogleAPI");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(LOG_TAG, String.format("Connection to GoogleApi Suspended, cause: %d", cause));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "Connection to GoogleApi Failed");
    }
}
