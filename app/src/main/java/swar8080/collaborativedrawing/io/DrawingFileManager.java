package swar8080.collaborativedrawing.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;


public class DrawingFileManager {

    public static final String DEFAULT_PICTURE_DIR = "Drawings";
    private String mPictureDir;

    private static final Pattern INVALID_NAME_CHAR_PATTERN = Pattern.compile("[^\\w\\- ]");
    private static final String FILE_EXT = ".png";
    private static final String MIME_TYPE = "image/png";

    public DrawingFileManager(){
        mPictureDir = DEFAULT_PICTURE_DIR;
    }

    public DrawingFileManager(String pictureDir){
        mPictureDir = pictureDir;
    }


    public boolean saveFileFromBitmap(Context context, Bitmap bitmap, String imageName){

        File imageDir =  new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "/" + mPictureDir
        );
        imageDir.mkdirs();

        File imageFile = new File(imageDir, imageName + FILE_EXT);

        try {
            //file cannot already exist or else FileOutputStream throws an exception
            imageFile.delete();

            FileOutputStream fos = new FileOutputStream(imageFile);

            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)){
                return false;
            }

            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        MediaScannerConnection.scanFile(context, new String[]{imageFile.getPath()}, new String[]{MIME_TYPE}, null);
        return imageFile.exists();
    }

    public boolean isValidFileName(String filename){
        return filename != null
        && filename.length() > 0
        && !INVALID_NAME_CHAR_PATTERN.matcher(filename).find();
    }

    public int getDrawingFileCount(){
        File imageDir =  new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "/" + mPictureDir
        );

        String[] fileNames = imageDir.list();
        if (fileNames != null){
            return fileNames.length;
        }
        else {
            return 0;
        }
    }


}