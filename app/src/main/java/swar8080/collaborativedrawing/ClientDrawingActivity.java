package swar8080.collaborativedrawing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

import swar8080.collaborativedrawing.connection.NearbyConnectionsUtil;
import swar8080.collaborativedrawing.drawing.DrawScalingUtil;
import swar8080.collaborativedrawing.message.EncodedMessage;
import swar8080.collaborativedrawing.message.HandshakeIdentifier;
import swar8080.collaborativedrawing.message.HandshakeTranslator;
import swar8080.collaborativedrawing.message.MessageAccumulator;
import swar8080.collaborativedrawing.message.MessageProgress;
import swar8080.collaborativedrawing.message.MessageProgressIdentifier;
import swar8080.collaborativedrawing.message.MessageStatus;
import swar8080.collaborativedrawing.message.DrawingMessagesTranslator;
import swar8080.collaborativedrawing.util.PreferenceUtil;

/**
 *
 */

public class ClientDrawingActivity extends DrawingParticipantActivity
{
    public static final String HOST_ID_EXTRA = "HOST_ID_EXTRA";
    public static final String HOST_NAME_EXTRA = "HOST_NAME_EXTRA";

    private String mHostId, mSessionName;
    private MessageAccumulator<MessageProgressIdentifier> mMessageAccumulator;

    private ProgressBar mConnectingProgressBar;

    private static final long TIMEOUT_DISCOVERY = Nearby.Connections.DURATION_INDEFINITE;
    private final String LOG_TAG = getClass().getSimpleName();

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_client_drawing;
    }

    @Override
    protected void afterOnCreateCallback(Bundle savedInstanceState) {
        Bundle connectionDetailsExtra = getIntent().getExtras();
        if (connectionDetailsExtra != null){
            mHostId = connectionDetailsExtra.getString(HOST_ID_EXTRA);
            mSessionName = connectionDetailsExtra.getString(HOST_NAME_EXTRA);
        }

        mConnectingProgressBar = (ProgressBar)findViewById(R.id.clientConnectingProgressBar);
        mMessageAccumulator = new MessageAccumulator<>();

        disableControls(getString(R.string.drawing_status_connecting_api));
    }

    @Override
    protected void onStop() {
        super.onStop();

        //while stopped, a reset message may be sent.
        //when reconnecting, the host will send all DrawingActions since the last reset
        //which will make the client up-to-date if starting in a reset state
        reset();
    }

    public void disableControls(String statusMessage){
        mDrawingView.setDrawingEnabled(false);
        mConnectingProgressBar.setVisibility(View.VISIBLE);
        showToolbarStatus(statusMessage);
    }

    public void enableControls(){
        mConnectingProgressBar.setVisibility(View.GONE);
        showControls();
    }

    @Override
    //connection to GoogleApi
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);

        super.showToolbarStatus(getString(R.string.drawing_status_connecting_host));

        if (mHostId != null && mSessionName != null){
            //must first find session through discovery before requesting a connection
            PendingResult<Status> resultPromise = discoverHost();

            resultPromise.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    Log.d(LOG_TAG,"Discover result " + status.toString());
                }
            });
        }
        else {
            disableControls("Error finding hodt");
        }
    }

    @Override
    //disconnected from host
    public void onDisconnected(String hostId) {
        disableControls(getString(R.string.drawing_status_reconnecting));
        discoverHost();
        Log.d(LOG_TAG,String.format("Client disconnected from host [%s]",hostId));
    }

    @Override
    //connection to google api suspended
    public void onConnectionSuspended(int cause){
        super.onConnectionSuspended(cause);
        disableControls(getString(R.string.drawing_status_reconnecting));
    }

    @Override
    //connection to google api failed
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult){
        super.onConnectionFailed(connectionResult);
        disableControls(getString(R.string.drawing_status_connecting_api_failed));
    }

    private PendingResult<Status> discoverHost(){
        return Nearby.Connections.startDiscovery(mGoogleApiClient,
                NearbyConnectionsUtil.getServiceId(this),
                TIMEOUT_DISCOVERY,
                new Connections.EndpointDiscoveryListener() {
                    @Override
                    public void onEndpointFound(String hostId, String serviceId, String hostName) {

                        Log.d(LOG_TAG,"Found endpoint during discover");
                        //if the correct session is found, request a connection
                        if (mHostId.equals(hostId) && mSessionName.equals(hostName)){
                            clientRequestConnection(mHostId, mSessionName);
                        }
                    }

                    @Override
                    public void onEndpointLost(String s) {
                        Log.d(LOG_TAG,"Lost endpoint during discover");
                    }
                });
    }

    private void clientRequestConnection(final String hostId, String hostName){

        byte[] connectionMessage = HandshakeTranslator.encodeIdentifierHeader(
                HandshakeIdentifier.PARTICIPANT);

        Nearby.Connections.sendConnectionRequest(mGoogleApiClient,
                PreferenceUtil.getPrefScreenName(this),
                hostId,
                connectionMessage,
                new Connections.ConnectionResponseCallback(){
                    @Override
                    public void onConnectionResponse(String hostId, Status status, byte[] message) {
                        Log.d(LOG_TAG,String.format("Connection Response from host [%s]:[%s]:[%s]",hostId,status.getStatusMessage(),status.getStatus()));
                        if (status.isSuccess()){
                            mDrawingView.setDrawingEnabled(true);
                            mConnectingProgressBar.setVisibility(View.GONE);
                            enableControls();
                        }
                    }
                },
                this).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.d(LOG_TAG,String.format("Client request connection result: %s ",status.toString()));
            }
        });


    }

    @Override
    public void onMessageReceived(String senderId, byte[] payload, boolean isReliable) {
        MessageProgress messageProgress = null;

        messageProgress = DrawingMessagesTranslator.getMessageProgress(senderId, payload);

        mMessageAccumulator.addMessage(messageProgress.getMessageIdentifier(), payload);

        if (MessageStatus.DONE == messageProgress.getMessageStatus()){
            EncodedMessage message = mMessageAccumulator.removeMessage(messageProgress.getMessageIdentifier());
            DrawingMessagesTranslator.decodeMessage(message, this);
        }
    }

    @Override
    protected void onResetDrawingPressed() {
        //request a reset, reset() only after host acknowledges by sending back a reset message of its own
        sendMessageToHost(DrawingMessagesTranslator.encodeResetMessage());
    }

    @Override
    public void onResetMessageReceived() {
        reset();
    }

    private void reset(){
        mDrawingView.reset();
    }

    @Override
    public void onUserDrawAt(Pair<Float, Float>[] pointsDrawnAt) {
        mDrawingView.drawBulkAt(mDrawingBrush, pointsDrawnAt, true);

        Pair<Float, Float>[] relativePointsDrawnAt = DrawScalingUtil.getRelativePointLocations(pointsDrawnAt,
                mDrawingView.getHeight(),
                mDrawingView.getWidth());

        EncodedMessage message = DrawingMessagesTranslator.encodeDrawMessages(mDrawingBrush.getPaintColour(),
                mDrawingBrush.getScaledShapeScaleFactor(),
                relativePointsDrawnAt,
                Connections.MAX_RELIABLE_MESSAGE_LEN );

        sendMessageToHost(message);
    }

    private void sendMessageToHost(EncodedMessage message){
        byte[][] payloads = message.getMessage();
        for (byte[] payload : payloads)
            Nearby.Connections.sendReliableMessage(mGoogleApiClient, mHostId, payload);
    }


    @Override
    public void onInternetConnectionChanged(boolean isConnectedOrConnecting) {
        if (isConnectedOrConnecting){
            enableControls();
        }
        else {
            disableControls(getString(R.string.drawing_status_no_connection));
        }
    }
}
