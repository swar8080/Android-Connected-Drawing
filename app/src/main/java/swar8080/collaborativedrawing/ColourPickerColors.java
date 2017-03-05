package swar8080.collaborativedrawing;

import android.content.Context;
import android.support.v4.content.ContextCompat;

/**
 * Created by Steven on 2017-02-25.
 */

public final class ColourPickerColors {

    private ColourPickerColors(){}

    private static int[] COLOURS;

    private static final int[] COLOUR_RESOURCES = {
            R.color.defaultDrawingColour,
            R.color.red,
            R.color.blue,
            R.color.green,
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
