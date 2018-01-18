package tr.edu.ybu.event.eventybuec;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends Activity {

    TextView email, pass;
    Button loginBtn;

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
                Login login = new Login();
                String[] request = {
                        "http://event.ybu.edu.tr/api/login",
                        email.getText().toString(),
                        pass.getText().toString()
                };
                login.execute(request);
            }
        });


    }

    private class Login extends AsyncTask<String, Void, String> {
        int resCode;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... str) {
            InputStream in;
            URL url;
            StringBuilder text = new StringBuilder();
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("login", str[1]);
                jsonObject.put("pass", str[2]);
                jsonObject.put("dev_id", Helper.GetDeviceID(MainActivity.this.getApplicationContext()));
                jsonObject.put("timestamp", Helper.GetTimestamp());

                url = new URL(str[0] + "?basehash=" + Helper.Base64Encode(jsonObject.toString()));
                URLConnection urlConn = url.openConnection();
                HttpURLConnection httpConn = (HttpURLConnection)urlConn;
                httpConn.setAllowUserInteraction(false);
                httpConn.setInstanceFollowRedirects(true);
                httpConn.setRequestMethod("GET");
                httpConn.connect();
                resCode = httpConn.getResponseCode();
                if (resCode == HttpURLConnection.HTTP_OK) {
                    in = httpConn.getInputStream();
                    InputStreamReader isr = new InputStreamReader(in);
                    int charRead;
                    char[] inputBuffer = new char[2000];
                    while ((charRead = isr.read(inputBuffer))>0) {
                        String readString = String.copyValueOf(inputBuffer, 0, charRead);
                        text.append(readString);
                        inputBuffer = new char[2000];
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return text.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            JSONObject responseBody;
            try {
                responseBody = new JSONObject(result);
                String resCode = responseBody.get("code").toString();

                    /*
                    result codes:
                        100: Eksik POST parametresi
                        101: İstek zaman aşımı. formdaki timestamp verilerini güncelleyerek tekrar deneyin
                        102: istek gövdesinde eksik bilgi
                        103: basehash çözülemedi
                        104: istek gövdesi hatalı.
                        110: Email hatalı
                        111: Şifre Hatalı
                        120: Kullanıcı Bulunamadı
                        121: Kullanıcının giriş yetkisi yok.
                     */
                switch (resCode){
                    case "101":
                        Log.d("Hata " , "Zaman aşımı");
                        break;
                    case "110":
                        Log.d("Hata " , "Email hatalı");
                        break;
                    case "111":
                        Log.d("Hata " , "Şifre hatalı");
                        break;
                    case "120":
                        Log.d("Hata " , "Kullanıcı bulunamadı");
                        break;
                    case "121":
                        Log.d("Hata " , "Kullanıcının giriş yetkisi yok");
                        break;
                    default:
                        Log.d("Res Code: " , "hata");
                        break;
                }
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
