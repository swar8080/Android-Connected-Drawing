package swar8080.collaborativedrawing;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

/**
 * Created by Steven on 2017-02-22.
 */

public abstract class DrawingParticipantActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.MessageListener,
        DrawingView.onUserDrawEventListener,
        DrawMessageTranslator.onDrawMessageHandler,
        BrushSizeSelectorDialogFragment.FinishedSelectingBrushSizeListener{

    protected GoogleApiClient mGoogleApiClient;

    protected DrawingView mDrawingView;
    protected TextView mColourPickerIcon;
    protected ColorPickerDialog mColourPickerDialog;

    protected DrawingBrush mDrawingBrush;
    private ScaledShapeDrawer mScaledShapeDrawer;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();

        Toolbar toolbar = (Toolbar)findViewById(R.id.drawingToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        mDrawingView = (DrawingView)findViewById(R.id.drawingView);
        mDrawingView.registerOnDrawEventListener(this);

        //once the view is fully inflated the run() will be called, allowing the
        //ScaledShapeDrawer to be initalized with the DrawingView's dimensions
        mDrawingView.post(new Runnable() {
            @Override
            public void run() {

                mScaledShapeDrawer = new ScaledCircleDrawer(mDrawingView.getHeight(), mDrawingView.getWidth(),
                        ResourceUtil.getFloatResourceFromDimen(getResources(), R.dimen.shape_drawing_size_default_percent));

                Paint defaultPaint = new Paint();
                defaultPaint.setColor(ContextCompat.getColor(DrawingParticipantActivity.this, R.color.defaultDrawingColour));
                defaultPaint.setStrokeCap(Paint.Cap.ROUND);
                mDrawingBrush = new DrawingBrush(defaultPaint, mScaledShapeDrawer);
            }
        });



        final ColorPickerSwatch.OnColorSelectedListener onColorSelectedListener = new ColorPickerSwatch.OnColorSelectedListener(){
            @Override
            public void onColorSelected(int colour) {
                mDrawingBrush.setPaintColour(colour);
                mColourPickerIcon.setBackgroundColor(colour);
                mColourPickerDialog.dismiss();
            }
        };

        mColourPickerIcon = (TextView)findViewById(R.id.colourPickingIcon);
        mColourPickerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColourPickerDialog = ColorPickerDialog.newInstance(R.string.colour_picker_title,
                        ColourPickerColors.getColours(DrawingParticipantActivity.this),
                        mDrawingBrush.getPaintColour(),
                        getResources().getInteger(R.integer.color_pallete_column_count),
                        ColorPickerDialog.SIZE_LARGE);

                mColourPickerDialog.setOnColorSelectedListener(onColorSelectedListener);

                mColourPickerDialog.show(getFragmentManager(), "foo");
            }
        });

        findViewById(R.id.brushSizeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayBrushSizeDialog();
            }
        });

        findViewById(R.id.resetButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetDrawing();
            }
        });

        //TODO move flag to keep screen on to more appropriate location
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        afterOnCreateCallback(savedInstanceState);
    }

    protected abstract int getLayoutResourceId();
    protected abstract void afterOnCreateCallback(Bundle savedInstanceState);


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawing_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_reset:
                resetDrawing();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mGoogleApiClient.isConnected()){
            Log( "Connecting to GoogleAPIClient");

            //connect is asynchronous and will call one of these callbacks when finished
            mGoogleApiClient.registerConnectionCallbacks(this);
            mGoogleApiClient.registerConnectionFailedListener(this);

            mGoogleApiClient.connect();
        } else { Log( "Already connected to GoogleAPIClient"); }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()){
            mGoogleApiClient.unregisterConnectionCallbacks(this);
            mGoogleApiClient.unregisterConnectionFailedListener(this);
            mGoogleApiClient.disconnect();
            Log( "Disconnecting from GoogleAPIClient");
        } else { Log( "Already disconnected from GoogleAPIClient"); }
    }

    private final static String BRUSH_SIZE_DIALOG_TAG = "BRUSH_SIZE_DIALOG_TAG";
    protected void displayBrushSizeDialog(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(BRUSH_SIZE_DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        BrushSizeSelectorDialogFragment brushSizeFragment = BrushSizeSelectorDialogFragment.newInstance(mDrawingView.getHeight(),
                ResourceUtil.getFloatResourceFromDimen(getResources(), R.dimen.shape_drawing_size_min_percent),
                ResourceUtil.getFloatResourceFromDimen(getResources(), R.dimen.shape_drawing_size_max_percent),
                mDrawingBrush.getScaledShapeScaleFactor(),
                mDrawingBrush.getPaintColour()
        );
        brushSizeFragment.setBrushSizeSelectedListener(this);
        brushSizeFragment.show(ft, BRUSH_SIZE_DIALOG_TAG);
    }

    @Override
    public void onFinishedSelectingBrushSize(float scaleFactor) {
        mDrawingBrush.setScaledShapeScaleFactor(scaleFactor);
    }

    @Override
    //connection to GoogleApi
    public void onConnected(@Nullable Bundle bundle) {
        Log("Connected to GoogleAPI");
    }

    @Override
    //Connection to GoogleAPI Suspended, GoogleAPI automatically tries to reconnect
    //but its a good idea to alert the user that it's attempting to reconnect and disable all UI
    //dependant on the connection
    public void onConnectionSuspended(int i) {
        Log("Connection to GoogleAPI suspended");
    }

    @Override
    //called when connection to GoogleApi fails
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log("Connection to GoogleAPI failed");
        //display some error message or prompt to try again
    }

    @Override
    public void onMessageReceived(String senderId, byte[] message, boolean isReliable) {
        try {
            DrawMessageTranslator.decodeMessage(message, senderId, this);
        }
        catch (DrawMessageTranslator.DrawMessageDecodingException e){
            Log(String.format("Error decoding message from %s: %s", senderId, e.getMessage()));
        }
    }

    @Override
    public void onDrawMessageReceived(DrawMessage drawMessage) {
        int drawingAreaWidth, drawingAreaHeight;
        Paint messagePaintUsed;
        ScaledShapeDrawer messageShapeDrawerUsed;

        drawingAreaWidth = mDrawingView.getWidth();
        drawingAreaHeight = mDrawingView.getHeight();

        Pair<Float,Float>[] scaledPointsToDrawAt = DrawScalingUtil.scalePointsToScreenSize(drawMessage.getRelativePointsDrawn(),
                drawingAreaWidth,
                drawingAreaHeight
        );

        messageShapeDrawerUsed = new ScaledCircleDrawer(drawingAreaHeight,
                drawingAreaWidth,
                drawMessage.getRelativeBrushSize());
        messagePaintUsed = new Paint();
        messagePaintUsed.setColor(drawMessage.getDrawColour());

        mDrawingView.drawBulkAt(new DrawingBrush(messagePaintUsed, messageShapeDrawerUsed), scaledPointsToDrawAt, true);
    }

    protected void Log(String message){
        Log.d(getClass().getSimpleName(),message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void resetDrawing(){
        mDrawingView.reset();
    }
}
