package swar8080.collaborativedrawing;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

/**
 *
 */

public class BrushSizeSelectorDialogFragment extends DialogFragment {

    public interface FinishedSelectingBrushSizeListener {
        void onFinishedSelectingBrushSize(float scaleFactor);
    }

    private FinishedSelectingBrushSizeListener mFinishedListener;

    private static final String SCALE_FACTOR_KEY = "ScaleSize";
    private static final String SCALE_SIZE_BASE = "ScaleBase";
    private static final String SCALE_SIZE_MIN = "ScaleBaseMin";
    private static final String SCALE_SIZE_MAX = "ScaleBaseMax";
    private static final String BRUSH_COLOUR_KEY = "BrushColour";

    private float mScaleFactor, mMinScaleFactor, mMaxScaleFactor;
    private int mScaleSizeBase;
    private int mBrushColour;
    private ImageView mBrushPreviewView;

    public static BrushSizeSelectorDialogFragment newInstance(int scaleBase, float minScaleFactor, float maxScaleFactor,
                                                              float scaleFactor, int brushColour){
        BrushSizeSelectorDialogFragment dialogFragment = new BrushSizeSelectorDialogFragment();

        Bundle args = new Bundle();
        args.putFloat(SCALE_FACTOR_KEY, scaleFactor);
        args.putFloat(SCALE_SIZE_MIN, minScaleFactor);
        args.putFloat(SCALE_SIZE_MAX, maxScaleFactor);
        args.putInt(SCALE_SIZE_BASE,scaleBase);
        args.putInt(BRUSH_COLOUR_KEY, brushColour);
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    public void setBrushSizeSelectedListener(FinishedSelectingBrushSizeListener listener){
        mFinishedListener = listener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mFinishedListener != null)
            mFinishedListener.onFinishedSelectingBrushSize(mScaleFactor);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mScaleFactor = args.getFloat(SCALE_FACTOR_KEY);
        mMinScaleFactor = args.getFloat(SCALE_SIZE_MIN);
        mMaxScaleFactor = args.getFloat(SCALE_SIZE_MAX);
        mScaleSizeBase = args.getInt(SCALE_SIZE_BASE);
        mBrushColour = args.getInt(BRUSH_COLOUR_KEY);
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.brush_size_selector, container, false);

        mBrushPreviewView = (ImageView)view.findViewById(R.id.brushSizeCircle);
        Drawable drawable = mBrushPreviewView.getDrawable();
        drawable.setColorFilter(mBrushColour, PorterDuff.Mode.SRC_IN);

        resizeBrushPreview(mScaleFactor);

        SeekBar sizeSlider = (SeekBar)view.findViewById(R.id.brushSizeSlider);
        sizeSlider.setProgress(estimateSeekBarProgress(mScaleFactor, mMinScaleFactor, mMaxScaleFactor));
        sizeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mScaleFactor = mMinScaleFactor + ((float)progress/100)*(mMaxScaleFactor-mMinScaleFactor);
                resizeBrushPreview(mScaleFactor);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });



        view.findViewById(R.id.brushSizeSelectorParent).setVisibility(View.VISIBLE);

        return view;
    }

    private void resizeBrushPreview(float newScaleFactor){
        int newHeight, newWidth;
        newHeight = newWidth = Math.round(mScaleSizeBase * newScaleFactor);
        mBrushPreviewView.setLayoutParams(new RelativeLayout.LayoutParams(newWidth, newHeight));
    }

    private static int estimateSeekBarProgress(float currentScaleFactor, float lowerScaleLimit, float upperScaleLimit){
        float seekBarProgress = (currentScaleFactor-lowerScaleLimit)/(upperScaleLimit-lowerScaleLimit);
        int estimatedProgress = Math.round(100 * seekBarProgress);
        return estimatedProgress;
    }

}
