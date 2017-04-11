package swar8080.collaborativedrawing;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.support.v4.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

import swar8080.collaborativedrawing.connection.AutoManagedGoogleApiActivity;
import swar8080.collaborativedrawing.drawing.DrawScalingUtil;
import swar8080.collaborativedrawing.drawing.DrawingAction;
import swar8080.collaborativedrawing.drawing.DrawingBrush;
import swar8080.collaborativedrawing.drawing.ScaledCircleDrawer;
import swar8080.collaborativedrawing.drawing.ScaledShapeDrawer;
import swar8080.collaborativedrawing.message.DecodedMessageHandler;
import swar8080.collaborativedrawing.util.ResourceUtil;

/**
 *
 */

public abstract class DrawingParticipantActivity extends AutoManagedGoogleApiActivity implements
        Connections.MessageListener,
        DrawingView.onUserDrawEventListener,
        BrushSizeSelectorDialogFragment.FinishedSelectingBrushSizeListener,
        DecodedMessageHandler {


    protected DrawingView mDrawingView;
    protected TextView mColourPickerIcon;
    protected ColorPickerDialog mColourPickerDialog;

    protected DrawingBrush mDrawingBrush;
    private ScaledShapeDrawer mScaledShapeDrawer;

    private TextView mConnectionStatusTextView;
    private ViewGroup mToolbarControlGroup;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        Toolbar toolbar = (Toolbar)findViewById(R.id.drawingToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        mConnectionStatusTextView = (TextView)findViewById(R.id.drawingToolbarStatus);
        mToolbarControlGroup = (ViewGroup)findViewById(R.id.drawingToolbarControls);

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


        //set up callbacks for colour picking dialog
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
                        ColourPickerColours.getColours(DrawingParticipantActivity.this),
                        mDrawingBrush.getPaintColour(),
                        getResources().getInteger(R.integer.color_pallete_column_count),
                        ColorPickerDialog.SIZE_LARGE);

                mColourPickerDialog.setOnColorSelectedListener(onColorSelectedListener);

                mColourPickerDialog.show(getFragmentManager(), "ColourPickerDialog");
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
                onResetDrawingPressed();
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        afterOnCreateCallback(savedInstanceState);
    }

    @Override
    protected GoogleApiClient.Builder getGoogleApiClientBuilder() {
        return new GoogleApiClient.Builder(this)
                .addApi(Nearby.CONNECTIONS_API);
    }

    //returns the layout resource id that should be inflated for this activity
    protected abstract int getLayoutResourceId();

    //subclasses do not override onCreate directly, instead they implement this method
    protected abstract void afterOnCreateCallback(Bundle savedInstanceState);

    protected abstract void onResetDrawingPressed();

    private final static String BRUSH_SIZE_DIALOG_TAG = "BRUSH_SIZE_DIALOG_TAG";
    protected void displayBrushSizeDialog(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(BRUSH_SIZE_DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }

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
    public void onDrawMessageReceived(DrawingAction[] drawActions) {
        int drawingAreaWidth, drawingAreaHeight;
        Paint messagePaintUsed;
        ScaledShapeDrawer messageShapeDrawerUsed;

        drawingAreaWidth = mDrawingView.getWidth();
        drawingAreaHeight = mDrawingView.getHeight();

        for (DrawingAction drawAction : drawActions){
            Pair<Float,Float>[] scaledPointsToDrawAt = DrawScalingUtil.scalePointsToScreenSize(drawAction.getRelativePointsDrawn(),
                    drawingAreaHeight,
                    drawingAreaWidth
            );

            messageShapeDrawerUsed = new ScaledCircleDrawer(drawingAreaHeight,
                    drawingAreaWidth,
                    drawAction.getRelativeBrushSize());
            messagePaintUsed = new Paint();
            messagePaintUsed.setColor(drawAction.getDrawColour());

            mDrawingView.drawBulkAt(new DrawingBrush(messagePaintUsed, messageShapeDrawerUsed), scaledPointsToDrawAt, true);
        }
    }

    protected final void showControls(){
        mConnectionStatusTextView.setVisibility(View.GONE);
        mToolbarControlGroup.setVisibility(View.VISIBLE);
    }

    protected final void showToolbarStatus(String statusMessage){
        mToolbarControlGroup.setVisibility(View.GONE);

        mConnectionStatusTextView.setText(statusMessage);
        mConnectionStatusTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClass(this,LobbyActivity.class);
        super.disconnectAndStartActivity(intent);
    }
}
