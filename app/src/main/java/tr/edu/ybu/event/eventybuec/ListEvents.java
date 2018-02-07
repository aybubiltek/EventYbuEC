package tr.edu.ybu.event.eventybuec;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by DeXCoder on 06-Feb-18.
 */

public class ListEvents extends Activity {
    ListView listView;
    String[] idList = new String[30];
    List<String> items = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_events);
        Intent i = getIntent();
        new SendPostRequest().execute(Integer.toString(MainActivity.kullanici_id), MainActivity.token);
        listView = findViewById(R.id.list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ListEvents.this, QR_Reader.class);
                intent.putExtra("event_id", idList[i]);
                startActivity(intent);
            }
        });
    }



    public class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try {
                URL url = new URL(Helper.url + "api/events");
                JSONObject postDataParams = new JSONObject();

                postDataParams.put("kullanici_id", arg0[0]);
                postDataParams.put("token", arg0[1]);

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
                boolean result = jsonObject.getBoolean("result");
                int code = jsonObject.getInt("code");
                if(result){
                    JSONArray data = jsonObject.getJSONArray("data");
                    for(int i=0;i<data.length();i++)
                    {
                        JSONObject object= data.getJSONObject(i);
                        items.add(
                                object.getString("ad") +
                                        System.getProperty("line.separator")+ "("+
                                        object.getString("kulup_ad")+")"
                        );

                        idList[i] = object.getString("id");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, items);
                    if (listView != null) {
                        listView.setAdapter(adapter);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /*
            try {
                JSONObject jsonObject = new JSONObject(res);
                int code = jsonObject.getInt("code");
                boolean result = jsonObject.getBoolean("result");

                if(result){
                    String token = jsonObject.getString("token");
                    int kullanici_id = jsonObject.getInt("kullanici_id");

                } else {

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            */
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
