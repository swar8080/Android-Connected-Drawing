package swar8080.collaborativedrawing;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.support.v4.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

import swar8080.collaborativedrawing.connection.AutoManagedGoogleApiActivity;
import swar8080.collaborativedrawing.connection.InternetConnectionStatus;
import swar8080.collaborativedrawing.drawing.DrawScalingUtil;
import swar8080.collaborativedrawing.drawing.DrawingAction;
import swar8080.collaborativedrawing.drawing.DrawingBrush;
import swar8080.collaborativedrawing.drawing.ScaledCircleDrawer;
import swar8080.collaborativedrawing.drawing.ScaledShapeDrawer;
import swar8080.collaborativedrawing.io.DrawingFileManager;
import swar8080.collaborativedrawing.message.DecodedMessageHandler;
import swar8080.collaborativedrawing.util.ResourceUtil;

public abstract class DrawingParticipantActivity extends AutoManagedGoogleApiActivity implements
        Toolbar.OnMenuItemClickListener,
        Connections.MessageListener,
        DrawingView.onUserDrawEventListener,
        BrushSizeSelectorDialogFragment.FinishedSelectingBrushSizeListener,
        DecodedMessageHandler,
        InternetConnectionStatus.InternetConnectionChangeListener,
        FileNameDialogFragment.FileNameResultHandler {

    public static final String SCREEN_NAME = "SCREEN_NAME";
    protected String mScreenName;
    protected String mDrawingFileName;

    protected DrawingView mDrawingView;
    protected TextView mColourPickerIcon;
    protected ColorPickerDialog mColourPickerDialog;
    private TextView mConnectionStatusTextView;
    private ViewGroup mToolbarControlGroup;

    protected DrawingBrush mDrawingBrush;
    private ScaledShapeDrawer mScaledShapeDrawer;

    private DrawingFileManager mDrawingFileManager;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        Toolbar toolbar = (Toolbar)findViewById(R.id.drawingToolbar);
        toolbar.inflateMenu(R.menu.drawing_menu);
        toolbar.setOnMenuItemClickListener(this);

        mConnectionStatusTextView = (TextView) findViewById(R.id.drawingToolbarStatus);
        mToolbarControlGroup = (ViewGroup)findViewById(R.id.drawingToolbarControls);

        mScreenName = getIntent().getStringExtra(SCREEN_NAME);

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


        InternetConnectionStatus icm = new InternetConnectionStatus(this);
        icm.registerInternetConnectionChangeListener(this);

        mDrawingFileManager = new DrawingFileManager();

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

    private static final String FILE_NAME_DIALOG_TAG = FileNameDialogFragment.class.getSimpleName();
    public void promptForFileName(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(FILE_NAME_DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        FileNameDialogFragment fileNameDialogFragment = FileNameDialogFragment.newInstance();
        fileNameDialogFragment.registerFileNameResultHandler(this);
        fileNameDialogFragment.show(ft, FILE_NAME_DIALOG_TAG);
    }

    @Override
    public void onValidFileName(String fileName) {
        if (saveDrawing(fileName, mDrawingView.getBitmap())){
            mDrawingFileName = fileName;
        }
    }

    public boolean saveDrawing(String fileName, Bitmap bitmap){
        getSavePermissions();
        boolean saved = mDrawingFileManager.saveFileFromBitmap(this, bitmap, fileName);

        if (saved)
            Toast.makeText(this, "Drawing saved", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Failed to save drawing", Toast.LENGTH_SHORT).show();

        return saved;
    }

    //http://stackoverflow.com/questions/33162152/storage-permission-error-in-marshmallow
    private static final int SAVE_PERMISSION_REQUEST_CODE = 1;
    public void getSavePermissions(){
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    SAVE_PERMISSION_REQUEST_CODE);

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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_reset:
                onResetDrawingPressed();
                break;
            case R.id.action_save:
                if (mDrawingFileName == null)
                    promptForFileName();
                else
                    saveDrawing(mDrawingFileName, mDrawingView.getBitmap());
                break;
            case R.id.action_save_as:
                promptForFileName();
                break;
        }

        return true;
    }
}
