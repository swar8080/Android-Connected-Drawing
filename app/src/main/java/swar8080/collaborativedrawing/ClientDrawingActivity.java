package swar8080.collaborativedrawing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

/**
 * Created by Steven on 2017-02-21.
 */

public class ClientDrawingActivity extends DrawingParticipantActivity
{

    private static final long TIMEOUT_DISCOVERY = Nearby.Connections.DURATION_INDEFINITE;

    private ProgressBar mDiscoverProgressBar;
    private String mHostId;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_client_drawing;
    }

    @Override
    protected void afterOnCreateCallback(Bundle savedInstanceState) {
        mDiscoverProgressBar = (ProgressBar)findViewById(R.id.discoverProgressBar);
    }


    @Override
    //connection to GoogleApi
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);
        startDiscovery();
    }

    private void startDiscovery(){
        final String serviceIdClientIsLookingFor = getString(R.string.service_id);

        PendingResult<Status> pendingDiscoveryResult = Nearby.Connections.startDiscovery(mGoogleApiClient,
                serviceIdClientIsLookingFor,
                TIMEOUT_DISCOVERY,
                new Connections.EndpointDiscoveryListener() {
                    @Override
                    public void onEndpointFound(String hostId, String serviceId, String hostName) {
                        if (serviceId.equals(serviceIdClientIsLookingFor)){
                            clientRequestConnection(hostId,hostName,null);
                        }
                    }

                    @Override
                    public void onEndpointLost(String hostId) {

                    }
                });

        pendingDiscoveryResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log(String.format("Discovery result: %s",status.getStatus()));
            }
        });
    }

    private void clientRequestConnection(final String hostId, String hostName, byte[] connectionMessage){

        //TODO have client send the dimensions of its DrawingView on connection

        Nearby.Connections.sendConnectionRequest(mGoogleApiClient,
                PreferenceUtil.getDisplayName(this),
                hostId,
                connectionMessage,
                new Connections.ConnectionResponseCallback(){

                    @Override
                    public void onConnectionResponse(String hostId, Status status, byte[] message) {
                        Log( String.format("Connection Response from host [%s]:[%s]:[%s]",hostId,status.getStatusMessage(),status.getStatus()));
                        if (status.isSuccess()){
                            mHostId = hostId;
                            mDiscoverProgressBar.setVisibility(View.GONE);
                        }
                    }
                },
                this);


    }

    @Override
    public void onDrawMessageReceived(DrawMessage drawMessage) {
        Pair<Float,Float>[] scaledPointsToDrawAt = DrawScalingUtil.scaleToScreenSize(drawMessage.getRelativePointsDrawn(),
                mDrawingView.getHeight(),
                mDrawingView.getWidth()
        );

        mDrawingView.drawBulkAt(mDrawingBrush, scaledPointsToDrawAt, true);
    }

    @Override
    public void onResetMessageReceived() {
        super.resetDrawing();
        sendMessageToHost(DrawMessageTranslator.encodeResetMessage());
    }

    @Override
    public void onDisconnected(String hostId) {
        Log(String.format("Client disconnected from host [%s]",hostId));
    }

    @Override
    public void onUserDrawAt(Pair<Float, Float>[] pointsDrawnAt) {
        mDrawingView.drawBulkAt(mDrawingBrush, pointsDrawnAt, true);

        Pair<Float, Float>[] relativePointsDrawnAt = DrawScalingUtil.getRelativePointLocations(pointsDrawnAt,
                mDrawingView.getHeight(),
                mDrawingView.getWidth());

        byte[][] messages = DrawMessageTranslator.encodeDrawMessages(mDrawingBrush.getPaintColour(),
                mDrawingBrush.getScaledShapeScaleFactor(),
                relativePointsDrawnAt,
                Connections.MAX_RELIABLE_MESSAGE_LEN );

        for (byte[] message : messages)
            sendMessageToHost(message);
    }

    private void sendMessageToHost(byte[] message){
        Nearby.Connections.sendReliableMessage(mGoogleApiClient, mHostId, message);
    }
}
