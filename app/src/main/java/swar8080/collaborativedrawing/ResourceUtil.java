package swar8080.collaborativedrawing;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by Steven on 2017-03-07.
 */

public class ResourceUtil {

    private ResourceUtil(){}

    public static float getFloatResourceFromDimen(Resources resources, int resourceId){
        TypedValue outValue = new TypedValue();
        resources.getValue(resourceId, outValue, true);
        return outValue.getFloat();
    }
}
