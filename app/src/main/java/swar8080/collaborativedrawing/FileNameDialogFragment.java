package swar8080.collaborativedrawing;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import swar8080.collaborativedrawing.io.DrawingFileManager;

/**
 *
 */

public class FileNameDialogFragment extends DialogFragment {

    private FileNameResultHandler mResultHandler;
    private EditText mFileNameEditText;
    private ViewGroup mErrorContainer;

    private DrawingFileManager mDrawingFileManager;

    public interface FileNameResultHandler {
        void onValidFileName(String fileName);
    }

    public static FileNameDialogFragment newInstance(){
        return new FileNameDialogFragment();
    }

    public FileNameDialogFragment() {
        super();
        mDrawingFileManager = new DrawingFileManager();
    }


    public void registerFileNameResultHandler(FileNameResultHandler handler){
        mResultHandler = handler;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.file_name_dialog, container);

        mFileNameEditText = (EditText)v.findViewById(R.id.fileName);
        mFileNameEditText.setText("Drawing " + String.valueOf(mDrawingFileManager.getDrawingFileCount()));

        mErrorContainer = (ViewGroup)v.findViewById(R.id.invalidFileNameContainer);

        v.findViewById(R.id.submitFileNameButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFileName(mFileNameEditText.getText().toString());
            }
        });

        v.findViewById(R.id.cancelFileNameButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        return v;
    }

    private void submitFileName(String fileName){
        if (mDrawingFileManager.isValidFileName(fileName)){
            mErrorContainer.setVisibility(View.GONE);
            mResultHandler.onValidFileName(fileName);
            dismiss();
        }
        else {
            mErrorContainer.setVisibility(View.VISIBLE);
        }
    }

}
