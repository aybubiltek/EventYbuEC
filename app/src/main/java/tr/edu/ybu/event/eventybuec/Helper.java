package tr.edu.ybu.event.eventybuec;


import android.content.Context;
import android.util.Base64;
import android.provider.Settings.Secure;
import java.nio.charset.Charset;

/**
 * Created by DeXCoder on 18-Jan-18.
 */

public class Helper {
    public static String Base64Encode(String text){
        return Base64.encodeToString(text.getBytes(Charset.forName("UTF-8")), Base64.DEFAULT);
    }

    public static String GetDeviceID(Context ctx){
        return Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
    }

    public static  String GetTimestamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        return tsLong.toString();
    }


}
