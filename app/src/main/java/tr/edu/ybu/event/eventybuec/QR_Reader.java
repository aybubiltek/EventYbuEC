package tr.edu.ybu.event.eventybuec;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;


public class QR_Reader extends Activity implements View.OnClickListener {

    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView barcodeValue;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    Button btn;

    public static String event_id = "";
    public static String qrhash = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_reader);

        statusMessage = (TextView)findViewById(R.id.status_message);
        barcodeValue = (TextView)findViewById(R.id.barcode_value);

        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);

        btn = findViewById(R.id.read_barcode);
        btn.setOnClickListener(this);

        Intent i = getIntent();
        event_id = i.getStringExtra("event_id");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btn.getId()) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());
            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText("Başarılı");
                    QR_Reader.qrhash = barcode.displayValue;
                    new SendPostRequest().execute();
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try {
                URL url = new URL(Helper.url + "api/qr");
                JSONObject postDataParams = new JSONObject();

                //token, kullanici_id, etkinlik_id, qrhash
                postDataParams.put("token", MainActivity.token);
                postDataParams.put("kullanici_id", MainActivity.kullanici_id);
                postDataParams.put("qrhash", QR_Reader.qrhash);
                postDataParams.put("etkinlik_id", event_id);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader in=new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";
                    while((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    in.close();
                    return sb.toString();
                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                JSONObject jsonObject = new JSONObject(res);
                if(jsonObject.getBoolean("result")){
                   String name = jsonObject.getString("data");

                    AlertDialog.Builder builder = new AlertDialog.Builder(QR_Reader.this);
                    builder.setMessage(name)
                            .setCancelable(true)
                            .setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new SendPostRequestConfirm().execute();
                                }
                            });
                    builder.setNegativeButton("İPTAL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Helper.alert("CANCEL", QR_Reader.this);
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();

                } else {

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator<String> itr = params.keys();
        while(itr.hasNext()){
            String key= itr.next();
            Object value = params.get(key);
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }



    public class SendPostRequestConfirm extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try {
                URL url = new URL(Helper.url + "api/qr_confirm");
                JSONObject postDataParams = new JSONObject();

                //token, kullanici_id, etkinlik_id, qrhash
                postDataParams.put("token", MainActivity.token);
                postDataParams.put("kullanici_id", MainActivity.kullanici_id);
                postDataParams.put("qrhash", QR_Reader.qrhash);
                postDataParams.put("etkinlik_id", event_id);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader in=new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";
                    while((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    in.close();
                    return sb.toString();
                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                JSONObject jsonObject = new JSONObject(res);
                if(jsonObject.getBoolean("result")){
                    boolean result = jsonObject.getBoolean("result");
                    if(result){

                        AlertDialog.Builder builder = new AlertDialog.Builder(QR_Reader.this);
                        builder.setMessage("İşleminiz Tamamlanmıştır")
                                .setCancelable(false)
                                .setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }

                } else {

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
