package swar8080.collaborativedrawing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

import java.util.ArrayList;

/**
 * Created by Steven on 2017-02-21.
 */

public class HostDrawingActivity extends DrawingParticipantActivity
{
    //discover indefinitley
    private static final long TIMEOUT_ADVERTISE = Nearby.Connections.DURATION_INDEFINITE;

    private ArrayList<String> mConnectedClientIds;
    private ArrayList<byte[]> mDrawingHistorySinceLastReset;

    @Override
    protected void afterOnCreateCallback(Bundle savedInstanceState) {
        mConnectedClientIds = new ArrayList<>();
        mDrawingHistorySinceLastReset = new ArrayList<>();
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
                Log( String.format("Advertising Result: [Staus:%s] [Host's Endpoint:%s]",startAdvertisingResult.getStatus(),startAdvertisingResult.getLocalEndpointName()));
            }
        });
    }

    //Called when a client requests connection this host
    public void handleConnectionRequest(final String clientId, final String clientName, byte[] message) {
        Log(String.format("Host handling connection request from client [%s]:[%s] requesting connection",clientName,clientId));

        byte[] acceptedConnectionMessage = null;

        PendingResult<Status> acceptConnectionResult =
                Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, clientId, acceptedConnectionMessage, this);


        acceptConnectionResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log(String.format("Accept connection result status: %s",status.getStatusMessage()));
                if (status.isSuccess()){
                    mConnectedClientIds.add(clientId);

                    Object[] drawingMessagesToCatchUpOn = mDrawingHistorySinceLastReset.toArray();
                    for (Object pastMessage : drawingMessagesToCatchUpOn){
                        sendMessageToClient(clientId, (byte[])pastMessage);
                    }


                    Toast.makeText(HostDrawingActivity.this, String.format("%s has joined",clientName),Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    //called when client disconnects
    public void onDisconnected(String clientId) {
        Log(String.format("Client [%s] disconnected from host",clientId));
        if (mConnectedClientIds.remove(clientId)){
            Toast.makeText(this, "Client " + clientId + " disconnected", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUserDrawAt(int paintColour, float brushSizeScaleFactor, Pair<Float, Float>[] pointsDrawnAt) {
        mDrawingView.drawBulkAt(pointsDrawnAt, true);


        Pair<Float, Float>[] relativePointsDrawnAt = DrawScalingUtil.getRelativePointLocations(pointsDrawnAt,
                mDrawingView.getHeight(),
                mDrawingView.getWidth());

        byte[][] messages = DrawMessageTranslator.encodeDrawMessages(mDrawingView.getDrawingColour(),
                brushSizeScaleFactor,
                relativePointsDrawnAt,
                Connections.MAX_RELIABLE_MESSAGE_LEN );

        for (byte[] message : messages){
            sendMessageToAllClients(message);
            mDrawingHistorySinceLastReset.add(message);
        }
    }

    private void sendMessageToAllClients(byte[] message){
        for (String clientId : mConnectedClientIds)
            sendMessageToClient(clientId, message);
    }

    private void sendMessageToClient(String clientId, byte[] message){
        Nearby.Connections.sendReliableMessage(mGoogleApiClient, clientId, message);
    }

    @Override
    public void onMessageReceived(String senderId, byte[] message, boolean isReliable) {
        super.onMessageReceived(senderId, message, isReliable);
        for (String clientId : mConnectedClientIds){
            if (clientId != senderId){
                //send client's message to all other clients
                sendMessageToClient(clientId, message);
            }
        }

        if (DrawMessageTranslator.isEvent(message,DrawMessageTranslator.DRAW_EVENT)){
            mDrawingHistorySinceLastReset.add(message);
        }

    }

    @Override
    public void onDrawMessageReceived(DrawMessage drawMessage) {
        Pair<Float,Float>[] scaledPointsToDrawAt = DrawScalingUtil.scaleToScreenSize(drawMessage.getRelativePointsDrawn(),
                mDrawingView.getHeight(),
                mDrawingView.getWidth()
        );

        mDrawingView.drawBulkAt(scaledPointsToDrawAt, drawMessage.getDrawColour(), drawMessage.getRelativeBrushSize(), true);
    }

    @Override
    public void onResetMessageReceived() {
        super.resetDrawing();
        byte[] resetMessage = DrawMessageTranslator.encodeResetMessage();
        sendMessageToAllClients(resetMessage);
        mDrawingHistorySinceLastReset.clear();
    }

}
