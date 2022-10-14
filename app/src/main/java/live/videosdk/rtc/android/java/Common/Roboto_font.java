package live.videosdk.rtc.android.java.Common;

import android.content.Context;
import android.graphics.Typeface;

public class Roboto_font {
    private static Typeface fromAsset;

    public static Typeface getTypeFace(Context context) {
        if (fromAsset == null) {
            fromAsset = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
        }
        return fromAsset;
    }

    public static Typeface getTypeFaceMedium(Context context) {
        if (fromAsset == null) {
            fromAsset = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
        }
        return fromAsset;
    }
}
