package swar8080.collaborativedrawing.util;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 *
 */

public class ResourceUtil {

    private ResourceUtil(){}

    public static float getFloatResourceFromDimen(Resources resources, int resourceId){
        TypedValue outValue = new TypedValue();
        resources.getValue(resourceId, outValue, true);
        return outValue.getFloat();
    }
}
