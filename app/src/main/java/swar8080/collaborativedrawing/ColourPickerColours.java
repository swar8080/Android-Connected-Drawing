package swar8080.collaborativedrawing;

import android.content.Context;
import android.support.v4.content.ContextCompat;

/**
 *
 */

public final class ColourPickerColours {

    private ColourPickerColours(){}

    private static int[] COLOURS;

    private static final int[] COLOUR_RESOURCES = {
            R.color.defaultDrawingColour,
            R.color.red,
            R.color.blue,

            R.color.orange,
            R.color.purple,
            R.color.green,

            R.color.white,
            R.color.grey,
            R.color.black
    };

    public static int[] getColours(Context context){
        if (COLOURS == null)
            loadColourResources(context);
        return COLOURS;
    }

    private static void loadColourResources(Context context) {
        int colour_count = COLOUR_RESOURCES.length;
        COLOURS = new int[colour_count];

        for (int i=0; i<colour_count; i++)
            COLOURS[i] = ContextCompat.getColor(context, COLOUR_RESOURCES[i]);
    }

}
