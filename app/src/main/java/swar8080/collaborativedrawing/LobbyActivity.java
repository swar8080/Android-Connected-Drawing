package swar8080.collaborativedrawing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

import swar8080.collaborativedrawing.connection.AutoManagedGoogleApiActivity;
import swar8080.collaborativedrawing.connection.AvailableSession;
import swar8080.collaborativedrawing.connection.NearbyConnectionsUtil;
import swar8080.collaborativedrawing.message.HandshakeIdentifier;
import swar8080.collaborativedrawing.message.HandshakeTranslator;
import swar8080.collaborativedrawing.message.UserCountResponse;
import swar8080.collaborativedrawing.util.PreferenceUtil;
import swar8080.collaborativedrawing.util.SerialExecutor;


public class LobbyActivity extends AutoManagedGoogleApiActivity implements AvailableSessionAdapter.OnSessionClickHandler  {
    private AvailableSessionAdapter mAvailableSessionAdapter;
    private SerialExecutor mSerialUserCountUpdateExecutor;

    private RecyclerView mAvailableSessionRecyclerView;
    private ProgressBar mDiscoverProgressBar;

    @Override
    protected GoogleApiClient.Builder getGoogleApiClientBuilder() {
        return new GoogleApiClient.Builder(this)
                .addApi(Nearby.CONNECTIONS_API);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        mAvailableSessionAdapter = new AvailableSessionAdapter(this);

        mAvailableSessionRecyclerView = (RecyclerView)findViewById(R.id.availableSessionList);
        mAvailableSessionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAvailableSessionRecyclerView.setAdapter(mAvailableSessionAdapter);

        mSerialUserCountUpdateExecutor = new SerialExecutor();

        mDiscoverProgressBar = (ProgressBar)findViewById(R.id.lobbyDiscoverProgressBar);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);
        Nearby.Connections.startDiscovery(mGoogleApiClient,
                NearbyConnectionsUtil.getServiceId(this),
                Nearby.Connections.DURATION_INDEFINITE,
                new Connections.EndpointDiscoveryListener() {
                    @Override
                    public void onEndpointFound(String hostId, String serviceId, String hostName) {
                        if (NearbyConnectionsUtil.getServiceId(LobbyActivity.this).equals(serviceId)){
                            AvailableSession sessionWithoutUserCount = new AvailableSession(hostId,hostName);
                            mSerialUserCountUpdateExecutor.execute(new GetSessionUserCountRunnable(sessionWithoutUserCount));
                        }
                    }

                    @Override
                    public void onEndpointLost(String hostId) {
                        mAvailableSessionAdapter.removeSession(hostId);
                        if (mAvailableSessionAdapter.getItemCount() == 0){
                            mDiscoverProgressBar.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }


    private void onUserCountReceived(AvailableSession sessionBeingUpdated, UserCountResponse response){
        sessionBeingUpdated.setPlayerCount(response.getUserCount());
        mAvailableSessionAdapter.updateOrInsertSession(sessionBeingUpdated);
        mDiscoverProgressBar.setVisibility(View.GONE);
        //TODO check if still discovering after one successful connection, may need to restart discover and find another way to determine if host is still advertising
    }


    private class GetSessionUserCountRunnable implements Runnable{

        private AvailableSession sessionToUpdate;
        private final String TAG = getClass().getSimpleName();

        public GetSessionUserCountRunnable(AvailableSession session) {
            sessionToUpdate = session;
        }

        @Override
        public void run() {
            byte[] requestMessage = HandshakeTranslator
                    .encodeIdentifierHeader(HandshakeIdentifier.USER_COUNT_REQUEST);

            PendingResult status = Nearby.Connections.sendConnectionRequest(mGoogleApiClient,
                    PreferenceUtil.getDisplayName(LobbyActivity.this),
                    sessionToUpdate.getHostId(),
                    requestMessage,
                    new Connections.ConnectionResponseCallback() {
                        @Override
                        public void onConnectionResponse(String endPointId, com.google.android.gms.common.api.Status status, byte[] response) {
                            if (status.isSuccess()){
                                //decode response to get user count
                                Result<UserCountResponse> result = HandshakeTranslator.decodeUserCountResponse(response);
                                if (result.isSuccesful()){
                                    UserCountResponse userCountResponse = result.getResult();
                                    onUserCountReceived(sessionToUpdate,userCountResponse);
                                    //Log.d(TAG, String.format("Received user count from %s of %d", sessionToUpdate.getHostId(), userCountResponse.toString()));
                                }

                                //disconnect so that other hosts can be polled for their user count
                                Nearby.Connections.disconnectFromEndpoint(mGoogleApiClient ,endPointId);
                            }
                            else {
                                //Log.d(TAG, String.format("Failed to receive user count from %s, status: %s", sessionToUpdate.getHostId(), status.getStatusMessage()));
                            }
                        }
                    },
                    null);

            //block until response received
            status.await();
        }
    }


    public void onClickDiscoverOrAdvertise(View view){
        if (R.id.startAdvertiseButton == view.getId()){
            startAdvertising();
        }
    }


    private void startAdvertising() {
        Intent intent = new Intent();
        intent.setClass(this, HostDrawingActivity.class);
        super.disconnectAndStartActivity(intent);
    }

    @Override
    public void onSessionSelected(AvailableSession sessionSelected) {
        Intent joinDrawingSessionIntent = new Intent();
        joinDrawingSessionIntent.setClass(this,ClientDrawingActivity.class);

        joinDrawingSessionIntent.putExtra(ClientDrawingActivity.HOST_ID_EXTRA, sessionSelected.getHostId());
        joinDrawingSessionIntent.putExtra(ClientDrawingActivity.HOST_NAME_EXTRA, sessionSelected.getSessionName());

        super.disconnectAndStartActivity(joinDrawingSessionIntent);
    }
}


