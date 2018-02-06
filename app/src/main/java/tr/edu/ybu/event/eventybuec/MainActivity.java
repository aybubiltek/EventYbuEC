package tr.edu.ybu.event.eventybuec;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class MainActivity extends Activity {

    TextView email, pass;
    Button loginBtn;
    public static String token;
    public static int kullanici_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendPostRequest().execute();
            }
        });
    }


    public class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try {
                URL url = new URL(Helper.url + "api/login");
                JSONObject postDataParams = new JSONObject();
                String uname = email.getText().toString();
                String password = pass.getText().toString();

                postDataParams.put("login", uname);
                postDataParams.put("pass", password);
                postDataParams.put("dev_id", Helper.GetDeviceID(MainActivity.this));
                postDataParams.put("timestamp", Helper.GetTimestamp());

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
                int code = jsonObject.getInt("code");
                boolean result = jsonObject.getBoolean("result");

                if(result){
                    MainActivity.token = jsonObject.getString("token");
                    MainActivity.kullanici_id = jsonObject.getInt("kullanici_id");
                    Intent intent = new Intent(MainActivity.this , ListEvents.class);
                    startActivity(intent);
                    finish();
                } else {
                    switch (code){
                        case 100:
                            Helper.alert("Eksik alan bırakmayınız", MainActivity.this);
                            break;
                        case  101:
                            Helper.alert("İstek zaman aşımı", MainActivity.this);
                            break;
                        case  110:
                            Helper.alert("Email hatalı", MainActivity.this);
                            break;
                        case  111:
                            Helper.alert("Şifre hatalı", MainActivity.this);
                            break;
                        case  120:
                            Helper.alert("Kullanıcı bulunamadı", MainActivity.this);
                            break;
                        case  121:
                            Helper.alert("Kullanıcının giriş yetkisi yok", MainActivity.this);
                            break;
                    }
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


}
