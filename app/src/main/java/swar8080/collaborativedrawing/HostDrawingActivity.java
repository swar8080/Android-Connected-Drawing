package swar8080.collaborativedrawing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

import java.util.ArrayList;
import java.util.LinkedList;

import swar8080.collaborativedrawing.drawing.DrawScalingUtil;
import swar8080.collaborativedrawing.message.EncodedMessage;
import swar8080.collaborativedrawing.message.HandshakeIdentifier;
import swar8080.collaborativedrawing.message.HandshakeTranslator;
import swar8080.collaborativedrawing.message.MessageAccumulator;
import swar8080.collaborativedrawing.message.MessageProgress;
import swar8080.collaborativedrawing.message.MessageProgressIdentifier;
import swar8080.collaborativedrawing.message.MessageStatus;
import swar8080.collaborativedrawing.message.MessageTranslator;
import swar8080.collaborativedrawing.message.UserCountResponse;
import swar8080.collaborativedrawing.util.PreferenceUtil;

/**
 *
 */

public class HostDrawingActivity extends DrawingParticipantActivity
{
    //discover indefinitley
    private static final long TIMEOUT_ADVERTISE = Nearby.Connections.DURATION_INDEFINITE;

    private ArrayList<String> mConnectParticipantIds;
    private LinkedList<EncodedMessage> mDrawingHistorySinceLastReset;

    private MessageAccumulator<MessageProgressIdentifier> mMessageAccumulator;

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void afterOnCreateCallback(Bundle savedInstanceState) {
        mConnectParticipantIds = new ArrayList<>();
        mDrawingHistorySinceLastReset = new LinkedList<EncodedMessage>();
        mMessageAccumulator = new MessageAccumulator<>();

        super.showControls();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_host_drawing;
    }

    @Override
    //connection to GoogleApi
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);

        startAdvertising();
    }

    public void startAdvertising(){
        PendingResult<Connections.StartAdvertisingResult> pendingAdvertisingResult =
                Nearby.Connections.startAdvertising(mGoogleApiClient,
                PreferenceUtil.getDisplayName(this),
                null,
                TIMEOUT_ADVERTISE,
                new Connections.ConnectionRequestListener() {
                    @Override
                    public void onConnectionRequest(String s, String s1, byte[] bytes) {
                        handleConnectionRequest(s,s1,bytes);
                    }
                });

        pendingAdvertisingResult.setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
            @Override
            public void onResult(@NonNull Connections.StartAdvertisingResult startAdvertisingResult) {
                Log.d(TAG,String.format("Advertising Result: [Staus:%s] [Host's Endpoint:%s]",startAdvertisingResult.getStatus(),startAdvertisingResult.getLocalEndpointName()));
            }
        });
    }

    //Called when a client requests connection this host
    public void handleConnectionRequest(final String clientId, final String clientName, byte[] message) {
        Log.d(TAG, String.format("Host handling connection request from client [%s]:[%s] requesting connection",clientName,clientId));

        HandshakeIdentifier identifier = HandshakeTranslator.decodeMessageIdentifier(message);

        if (HandshakeIdentifier.PARTICIPANT == identifier){

            PendingResult<Status> acceptConnectionResult =
                Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, clientId, null, this);

            acceptConnectionResult.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    Log.d(TAG, String.format("Accept connection result status: %s",status.getStatusMessage()));
                    if (status.isSuccess()){
                        mConnectParticipantIds.add(clientId);

                        sendMessageToClient(clientId, MessageTranslator.mergeMessages(mDrawingHistorySinceLastReset));

                        Toast.makeText(HostDrawingActivity.this, String.format("%s has joined",clientName),Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else if (HandshakeIdentifier.USER_COUNT_REQUEST == identifier){
            UserCountResponse userCount = new UserCountResponse(1 + mConnectParticipantIds.size());
            byte[] encodedResponse = HandshakeTranslator.encodeUserCountResponse(userCount);

            Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, clientId, encodedResponse, this);
        }
        else {
            //TODO log when unhandled or null identifier
        }
    }

    @Override
    //called when client disconnects
    public void onDisconnected(String clientId) {
        Log.d(TAG, String.format("Client [%s] disconnected from host",clientId));
        if (mConnectParticipantIds.remove(clientId)){
            Toast.makeText(this, "Client " + clientId + " disconnected", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUserDrawAt(Pair<Float, Float>[] pointsDrawnAt) {
        mDrawingView.drawBulkAt(mDrawingBrush,pointsDrawnAt, true);

        Pair<Float, Float>[] relativePointsDrawnAt = DrawScalingUtil.getRelativePointLocations(pointsDrawnAt,
                mDrawingView.getHeight(),
                mDrawingView.getWidth());

        EncodedMessage message = MessageTranslator.encodeDrawMessages(mDrawingBrush.getPaintColour(),
                mDrawingBrush.getScaledShapeScaleFactor(),
                relativePointsDrawnAt,
                Connections.MAX_RELIABLE_MESSAGE_LEN );

        mDrawingHistorySinceLastReset.add(message);
        sendMessageToAllClients(message);
    }

    private void sendMessageToAllClients(EncodedMessage message){
        for (String clientId : mConnectParticipantIds)
            sendMessageToClient(clientId, message);
    }

    private void sendMessageToClient(String clientId, EncodedMessage message){
        for (byte[] payload : message.getMessage())
            Nearby.Connections.sendReliableMessage(mGoogleApiClient, clientId, payload);
    }

    @Override
    public void onMessageReceived(String senderId, byte[] payload, boolean isReliable) {

        try {
            MessageProgress messageProgress = MessageTranslator.getMessageProgress(senderId, payload);
            mMessageAccumulator.addMessage(messageProgress.getMessageIdentifier(), payload);

            if (MessageStatus.DONE == messageProgress.getMessageStatus()){
                EncodedMessage message = mMessageAccumulator.removeMessage(messageProgress.getMessageIdentifier());
                MessageTranslator.decodeMessage(message, this);
            }

            for (String clientId : mConnectParticipantIds){
                if (!clientId.equals(senderId)){
                    //send client's message to all other clients
                    sendMessageToClient(clientId, new EncodedMessage(payload));
                }
            }
        } catch (MessageTranslator.MessageDecodingException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void reset(){
        mDrawingView.reset();
        mDrawingHistorySinceLastReset = new LinkedList<EncodedMessage>();
    }

    @Override
    public void onResetMessageReceived() {
        reset();
        sendMessageToAllClients(MessageTranslator.encodeResetMessage());
    }

    @Override
    protected void onResetDrawingPressed() {
        reset();
        sendMessageToAllClients(MessageTranslator.encodeResetMessage());
    }
}
