package tr.edu.ybu.event.eventybuec;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings.Secure;

/**
 * Created by DeXCoder on 18-Jan-18.
 */

public class Helper {

    public static String url = "http://event.ybu.edu.tr/";
    public static String GetDeviceID(Context ctx){
        return Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
    }

    public static  String GetTimestamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        return tsLong.toString();
    }

    public static void alert(String msg, Context ctx){

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
