package org.techtown.test;

import android.Manifest;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.view.ChatView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringWriter;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.GsonFactory;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIEvent;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;

import static java.lang.Thread.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getName();
    private Gson gson = GsonFactory.getGson();
    private AIDataService aiDataService;
    private ChatView chatView;
    private User myAccount;
    private User droidKaigiBot;
    private GPSTracker gps;
    private String speech, position_cd, nanum_so_code, nanum_so_allCnt, nanum_so_cnt, nanum_gr_code, nanum_gr_allCnt, nanum_gr_cnt, addr;
    private double lat, lng, lat1, lat2, lat3, lng1, lng2, lng3;

    private TextView txtLat;
    private TextView txtLon;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*if (!isPermission) {
            callPermission();
            return;
        }*/

        View view = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view != null) {
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(Color.parseColor("#f2f2f2"));
            }
        }

        initChatView();

        //Language, Dialogflow Client access token
        //final LanguageConfig config = new LanguageConfig("ja", "f5dfc697c01c47b7bab5215704793918");
        final AIConfiguration config = new AIConfiguration("8a7f06ff24a94785b408321f60ee3f88",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(this, config);
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("Hello");

        //initService(config);
        callPermission();
    }

    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }


    @Override
    public void onClick(View v) {
        gps = new GPSTracker(MainActivity.this);
        Log.i("gps enable", "gps enable");
        // GPS 사용유무 가져오기

        double latitude = 0;
        double longitude = 0;

        if (gps.isGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            Log.i("gps 1", "gps 1");

        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
            Log.i("gps n", "gps n");
        }
        //new message
        final Message message = new Message.Builder()
                .setUser(myAccount)
                .setRightMessage(true)
                .setMessageText(chatView.getInputText())
                .hideIcon(true)
                .build();
        //Set to chat view
        chatView.send(message);
        sendRequest(chatView.getInputText() + " \\ " + latitude + " \\ " + longitude);
        //Reset edit text
        chatView.setInputText("");
    }

    /*
     * AIRequest should have query OR event
     */
    private void sendRequest(String text) {
        Log.d(TAG, text);

        final String queryString = String.valueOf(text);
        final String eventString = null;
        final String contextString = null;


        if (TextUtils.isEmpty(queryString) && TextUtils.isEmpty(eventString)) {
            //onError(new AIError(getString(R.string.non_empty_query)));
            return;
        }

        new AiTask().execute(queryString, eventString, contextString);
    }

    public class AiTask extends AsyncTask<String, Void, AIResponse> {
        private AIError aiError;

        @Override
        protected AIResponse doInBackground(final String... params) {
            final AIRequest request = new AIRequest();
            String query = params[0];
            String event = params[1];
            String context = params[2];

            if (!TextUtils.isEmpty(query)){
                request.setQuery(query);
            }

            if (!TextUtils.isEmpty(event)){
                request.setEvent(new AIEvent(event));
            }

            RequestExtras requestExtras = null;
            if (!TextUtils.isEmpty(context)) {
                final List<AIContext> contexts = Collections.singletonList(new AIContext(context));
                requestExtras = new RequestExtras(contexts, null);
            }

            try {
                return aiDataService.request(request, requestExtras);
            } catch (final AIServiceException e) {
                aiError = new AIError(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(final AIResponse response) {
            if (response != null) {
                onResult(response);
            } else {
                onError(aiError);
            }
        }
    }


    private void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Variables
                gson.toJson(response);
                final Status status = response.getStatus();
                final Result result = response.getResult();
                speech = result.getFulfillment().getSpeech();

                final Metadata metadata = result.getMetadata();
                final HashMap<String, JsonElement> params = result.getParameters();

                // Logging
                Log.d(TAG, "onResult");
                Log.i(TAG, "Received success response");
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());
                Log.i(TAG, "Action: " + result.getAction());
                Log.i(TAG, "Speech: " + speech);


                if (metadata != null) {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                if (params != null && !params.isEmpty()) {
                    Log.i(TAG, "Parameters: ");
                    for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                        Log.i(TAG, String.format("%s: %s",
                                entry.getKey(), entry.getValue().toString()));
                    }
                }

                if(metadata.getIntentName().equals("closest")) {
                    final String[] speechList = speech.split(";");
                    Log.i(TAG, "intent name: " + metadata.getIntentName());
                    Log.i(TAG, "Speech0(msg): " + speechList[0]);
                    Log.i(TAG, "Speech1(location.lat): " + speechList[1]);
                    Log.i(TAG, "Speech2(location.lng): " + speechList[2]);
                    Log.i(TAG, "Speech3(location.position_cd): " + speechList[3]);
                    Log.i(TAG, "Speech4(location.adres): " + speechList[4]);
                    speech = speechList[0];
                    lat = Double.parseDouble(speechList[1]);
                    lng = Double.parseDouble(speechList[2]);
                    position_cd = speechList[3];
                    addr = speechList[4];

                    Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);

                    receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText("지도로 이동하여 표시합니다...")
                            .build();
                    chatView.receive(receivedMessage);
                    //add map
                    String value = lat+";"+lng+";"+addr;
                    Intent intent = new Intent(MainActivity.this, MapActivity.class).putExtra("info", value);
                    startActivity(intent);
                }

                if(metadata.getIntentName().equals("top3_closest")) {
                    final String[] speechList = speech.split(";");
                    Log.i(TAG, "intent name: " + metadata.getIntentName());
                    Log.i(TAG, "Speech0(msg): " + speechList[0]);
                    Log.i(TAG, "Speech1(location.lat): " + speechList[1]);
                    Log.i(TAG, "Speech2(location.lng): " + speechList[2]);
                    Log.i(TAG, "Speech3(location.position_cd): " + speechList[3]);
                    Log.i(TAG, "Speech4(location.adres): " + speechList[4]);
                    speech = speechList[0];
                    lat = Double.parseDouble(speechList[1]);
                    lng = Double.parseDouble(speechList[2]);
                    position_cd = speechList[3];
                    addr = speechList[4];

                    Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);

                    receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText("지도로 이동하여 표시합니다...")
                            .build();
                    chatView.receive(receivedMessage);
                    //add map
                    String value = lat+";"+lng+";"+addr;
                    Intent intent = new Intent(MainActivity.this, MapActivity.class).putExtra("info", value);
                    startActivity(intent);
                }

                if(metadata.getIntentName().equals("top3")) {
                    final String[] speechList = speech.split(";");
                    Log.i(TAG, "intent name: " + metadata.getIntentName());
                    Log.i(TAG, "Speech0(msg): " + speechList[0]);
                    Log.i(TAG, "Speech1(location1.lat): " + speechList[1]);
                    Log.i(TAG, "Speech2(location1.lng): " + speechList[2]);
                    Log.i(TAG, "Speech3(location2.lat): " + speechList[3]);
                    Log.i(TAG, "Speech4(location2.lng): " + speechList[4]);
                    Log.i(TAG, "Speech5(location3.lat): " + speechList[5]);
                    Log.i(TAG, "Speech6(location3.lng): " + speechList[6]);
                    Log.i(TAG, "Speech7(location1.adres): " + speechList[7]);
                    Log.i(TAG, "Speech8(location2.adres): " + speechList[8]);
                    Log.i(TAG, "Speech9(location3.adres): " + speechList[9]);
                    speech = speechList[0];
                    lat1 = Double.parseDouble(speechList[1]);
                    lng1 = Double.parseDouble(speechList[2]);
                    lat2 = Double.parseDouble(speechList[3]);
                    lng2 = Double.parseDouble(speechList[4]);
                    lat3 = Double.parseDouble(speechList[5]);
                    lng3 = Double.parseDouble(speechList[6]);
                    String addr1 = speechList[7];
                    String addr2 = speechList[8];
                    String addr3 = speechList[9];

                    Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);

                    receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText("지도로 이동하여 표시합니다...")
                            .build();
                    chatView.receive(receivedMessage);
                    //add map
                    String value = lat1+";"+lng1+";"+lat2+";"+lng2+";"+lat3+";"+lng3+";"+addr1+";"+addr2+";"+addr3;
                    Intent intent = new Intent(MainActivity.this, MapActivity.class).putExtra("info1", value);
                    startActivity(intent);
                }

                if(metadata.getIntentName().equals("status")) {
                    String url_so = "http://openapi.seoul.go.kr:8088/sample/xml/NanumcarCarList/1/5/"+position_cd+"/so";
                    NetworkTask networkTask = new NetworkTask(url_so, null, "socar");
                    networkTask.execute();

                    String url_gr = "http://openapi.seoul.go.kr:8088/sample/xml/NanumcarCarList/1/5/"+position_cd+"/gr";
                    NetworkTask networkTask2 = new NetworkTask(url_gr, null, "gr");
                    try {
                        networkTask2.execute().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    position_cd = "";
                }

                if(metadata.getIntentName().equals("spec_addr")) {
                    String addr = speech;
                    String url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/xml?input="+addr+"&inputtype=textquery&fields=photos,formatted_address,name,rating,opening_hours,geometry&key=AIzaSyD2h9IEq_ZZBVcPQ8OJl3cvUBDHZ2oo9UU";
                    NetworkTask networkTask = new NetworkTask(url, null, "search");
                    networkTask.execute();
                }

                if(metadata.getIntentName().equals("spec_electric")){
                    final String[] speechList = speech.split(";");
                    Log.i(TAG, "intent name: " + metadata.getIntentName());
                    Log.i(TAG, "Speech0(msg): " + speechList[0]);
                    Log.i(TAG, "Speech1(location.lat): " + speechList[1]);
                    Log.i(TAG, "Speech2(location.lng): " + speechList[2]);
                    Log.i(TAG, "Speech3(location.position_cd): " + speechList[3]);
                    Log.i(TAG, "Speech4(location.adres): " + speechList[4]);
                    speech = speechList[0];
                    lat = Double.parseDouble(speechList[1]);
                    lng = Double.parseDouble(speechList[2]);
                    position_cd = speechList[3];
                    addr = speechList[4];

                    Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);

                    receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText("지도로 이동하여 표시합니다...")
                            .build();
                    chatView.receive(receivedMessage);
                    //add map
                    String value = lat+";"+lng+";"+addr;
                    Intent intent = new Intent(MainActivity.this, MapActivity.class).putExtra("info", value);
                    startActivity(intent);
                }

                if(metadata.getIntentName().equals("spec_agency")){
                    final String[] speechList = speech.split(";");
                    Log.i(TAG, "intent name: " + metadata.getIntentName());
                    Log.i(TAG, "Speech0(msg): " + speechList[0]);
                    Log.i(TAG, "Speech1(location.lat): " + speechList[1]);
                    Log.i(TAG, "Speech2(location.lng): " + speechList[2]);
                    Log.i(TAG, "Speech3(location.position_cd): " + speechList[3]);
                    Log.i(TAG, "Speech4(location.adres): " + speechList[4]);
                    speech = speechList[0];
                    lat = Double.parseDouble(speechList[1]);
                    lng = Double.parseDouble(speechList[2]);
                    position_cd = speechList[3];
                    addr = speechList[4];

                    Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);

                    receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText("지도로 이동하여 표시합니다...")
                            .build();
                    chatView.receive(receivedMessage);
                    //add map
                    String value = lat+";"+lng+";"+addr;
                    Intent intent = new Intent(MainActivity.this, MapActivity.class).putExtra("info", value);
                    startActivity(intent);
                }

                if(metadata.getIntentName().equals("closest_EVcharge")){
                    final String[] speechList = speech.split(";");
                    Log.i(TAG, "intent name: " + metadata.getIntentName());
                    Log.i(TAG, "Speech0(msg): " + speechList[0]);
                    Log.i(TAG, "Speech1(location.lat): " + speechList[1]);
                    Log.i(TAG, "Speech2(location.lng): " + speechList[2]);
                    Log.i(TAG, "Speech3(location.name): " + speechList[3]);
                    speech = speechList[0]+" 지도로 이동하여 표시합니다...";
                    lat = Double.parseDouble(speechList[1]);
                    lng = Double.parseDouble(speechList[2]);
                    addr = speechList[3];

                    Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);

                    receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText("지도로 이동하여 표시합니다...")
                            .build();
                    chatView.receive(receivedMessage);
                    //add map
                    String value = lat+";"+lng+";"+addr;
                    Intent intent = new Intent(MainActivity.this, MapActivity.class).putExtra("info", value);
                    startActivity(intent);
                }

                if(metadata.getIntentName().equals("search_toilet")) {
                    final String[] speechList = speech.split(";");
                    Log.i(TAG, "intent name: " + metadata.getIntentName());
                    Log.i(TAG, "Speech0(msg): " + speechList[0]);
                    Log.i(TAG, "Speech1(location.lat): " + speechList[1]);
                    Log.i(TAG, "Speech2(location.lng): " + speechList[2]);
                    speech = speechList[0];
                    lat = Double.parseDouble(speechList[1]);
                    lng = Double.parseDouble(speechList[2]);

                    Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);

                    receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText("지도로 이동하여 표시합니다...")
                            .build();
                    chatView.receive(receivedMessage);
                    //add map
                    String value = lat+";"+lng+";"+"가장 가까운 화장실";
                    Intent intent = new Intent(MainActivity.this, MapActivity.class).putExtra("info", value);
                    startActivity(intent);
                }

                if(metadata.getIntentName().equals("closest_park1")){
                    final String[] speechList = speech.split(";");
                    Log.i(TAG, "intent name: " + metadata.getIntentName());
                    Log.i(TAG, "Speech0(msg): " + speechList[0]);
                    Log.i(TAG, "Speech1(location.lat): " + speechList[1]);
                    Log.i(TAG, "Speech2(location.lng): " + speechList[2]);
                    Log.i(TAG, "Speech3(location.name): " + speechList[3]);
                    Log.i(TAG, "Speech4(location.etc): " + speechList[4]);
                    speech = speechList[0] +" 주차장 이름: "+ speechList[3]+" , 정보: " + speechList[4];
                    lat = Double.parseDouble(speechList[1]);
                    lng = Double.parseDouble(speechList[2]);
                    addr = speechList[3];

                    Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);

                    receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText("지도로 이동하여 표시합니다...")
                            .build();
                    chatView.receive(receivedMessage);
                    //add map
                    String value = lat+";"+lng+";"+addr;
                    Intent intent = new Intent(MainActivity.this, MapActivity.class).putExtra("info", value);
                    startActivity(intent);
                }

                if(metadata.getIntentName().equals("closest_park0")){
                    final String[] speechList = speech.split(";");
                    Log.i(TAG, "intent name: " + metadata.getIntentName());
                    Log.i(TAG, "Speech0(msg): " + speechList[0]);
                    Log.i(TAG, "Speech1(location.lat): " + speechList[1]);
                    Log.i(TAG, "Speech2(location.lng): " + speechList[2]);
                    Log.i(TAG, "Speech3(location.name): " + speechList[3]);
                    Log.i(TAG, "Speech4(location.msg2): " + speechList[4]);
                    speech = speechList[0] +" "+ speechList[4];
                    lat = Double.parseDouble(speechList[1]);
                    lng = Double.parseDouble(speechList[2]);
                    addr = speechList[3];

                    Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);

                    receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText("지도로 이동하여 표시합니다...")
                            .build();
                    chatView.receive(receivedMessage);
                    //add map
                    String value = lat+";"+lng+";"+addr;
                    Intent intent = new Intent(MainActivity.this, MapActivity.class).putExtra("info", value);
                    startActivity(intent);
                }
            }
        });
    }

    private void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,error.toString());
            }
        });
    }

    private void initChatView() {
        int myId = 0;
        Bitmap icon1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_user);
        Bitmap icon2 = BitmapFactory.decodeResource(getResources(), R.drawable.chat_bot);
        String myName = " ";
        myAccount = new User(myId, myName, icon1);


        int botId = 1;
        String botName = "해치";
        droidKaigiBot = new User(botId, botName, icon2);

        chatView = findViewById(R.id.chat_view);
        chatView.setRightBubbleColor(ContextCompat.getColor(this, R.color.chatcolor));
        chatView.setLeftBubbleColor(ContextCompat.getColor(this, R.color.boxbox));
        chatView.setBackgroundColor(ContextCompat.getColor(this, R.color.chatviewcolor));
        chatView.setSendButtonColor(ContextCompat.getColor(this, R.color.chatcolor));
        chatView.setSendIcon(R.drawable.ic_action_send);
        chatView.setRightMessageTextColor(Color.WHITE);
        chatView.setLeftMessageTextColor(Color.WHITE);
        chatView.setUsernameTextColor(Color.BLACK);
        chatView.setSendTimeTextColor(R.color.chatcolor);
        chatView.setDateSeparatorColor(R.color.chatcolor);
        chatView.setMessageMarginTop(5);
        chatView.setMessageMarginBottom(5);
        chatView.setOnClickSendButtonListener(this);
        chatView.setInputTextHint("내용을 입력하세요.");
    }

    private void initService(final LanguageConfig languageConfig) {
        final AIConfiguration.SupportedLanguages lang =
                AIConfiguration.SupportedLanguages.fromLanguageTag(languageConfig.getLanguageCode());
        final AIConfiguration config = new AIConfiguration(languageConfig.getAccessToken(),
                lang,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(this, config);
    }

    public class NetworkTask extends AsyncTask<Void, Void, Document> {

        private String url;
        private ContentValues values;
        private String type;

        public NetworkTask(String url, ContentValues values, String type) {
            this.url = url;
            this.values = values;
            this.type = type;
            Log.i(TAG, "url: "+ url);
        }

        @Override
        protected Document doInBackground(Void... params) {

            URL url_;
            Document doc = null;
            try {
                url_ = new URL (url);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(new InputSource(url_.openStream()));
                doc.getDocumentElement().normalize();
                Log.i(TAG, "doc info: " + String.valueOf(doc));
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document doc) {
            String s = "";

            if(type.equals("socar")) {
                NodeList resList = doc.getElementsByTagName("RESULT");
                Node resNode = resList.item(0);
                Element resElmt = (Element) resNode;
                NodeList code = resElmt.getElementsByTagName("CODE");
                s += "code = " + code.item(0).getChildNodes().item(0).getNodeValue() + "\n";
                nanum_so_code = code.item(0).getChildNodes().item(0).getNodeValue();
                Log.i(TAG, s);

                if (!code.item(0).getChildNodes().item(0).getNodeValue().equals("INFO-200")) {
                    NodeList rowList = doc.getElementsByTagName("row");
                    Node rowNode = rowList.item(0);
                    Element rowElmt = (Element) rowNode;
                    NodeList allCnt = rowElmt.getElementsByTagName("reservAbleAllCnt");
                    s += "allCnt = " + allCnt.item(0).getChildNodes().item(0).getNodeValue() + "\n";
                    nanum_so_allCnt = allCnt.item(0).getChildNodes().item(0).getNodeValue();
                    Log.i(TAG, s);
                    NodeList cnt = rowElmt.getElementsByTagName("reservAbleCnt");
                    nanum_so_cnt = cnt.item(0).getChildNodes().item(0).getNodeValue();
                    s += "Cnt = " + cnt.item(0).getChildNodes().item(0).getNodeValue() + "\n";
                    Log.i(TAG, s);
                }
                if(!nanum_so_code.equals("INFO-200") ){
                    speech = "현재 주차장의 쏘카 현황은 총 차량 대수: " + nanum_so_allCnt + "대, 이용 가능한 차량 대수: " + nanum_so_cnt + "대 입니다.";
                    final Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);
                }
            }

            if(type.equals("gr")) {
                NodeList resList = doc.getElementsByTagName("RESULT");
                Node resNode = resList.item(0);
                Element resElmt = (Element) resNode;
                NodeList code = resElmt.getElementsByTagName("CODE");
                s += "code = " + code.item(0).getChildNodes().item(0).getNodeValue() + "\n";
                nanum_gr_code = code.item(0).getChildNodes().item(0).getNodeValue();
                Log.i(TAG, s);

                if (!code.item(0).getChildNodes().item(0).getNodeValue().equals("INFO-200")) {
                    NodeList rowList = doc.getElementsByTagName("row");
                    Node rowNode = rowList.item(0);
                    Element rowElmt = (Element) rowNode;
                    NodeList allCnt = rowElmt.getElementsByTagName("reservAbleAllCnt");
                    s += "allCnt = " + allCnt.item(0).getChildNodes().item(0).getNodeValue() + "\n";
                    nanum_gr_allCnt = allCnt.item(0).getChildNodes().item(0).getNodeValue();
                    Log.i(TAG, s);
                    NodeList cnt = rowElmt.getElementsByTagName("reservAbleCnt");
                    nanum_gr_cnt = cnt.item(0).getChildNodes().item(0).getNodeValue();
                    s += "Cnt = " + cnt.item(0).getChildNodes().item(0).getNodeValue() + "\n";
                    Log.i(TAG, s);
                }

                if (!nanum_gr_code.equals("INFO-200") ){
                    speech = "현재 주차장의 그린카 현황은 총 차량 대수: " + nanum_gr_allCnt + "대, 이용 가능한 차량 대수: " + nanum_gr_cnt + "대 입니다.";
                    final Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);
                }
            }

            if(type.equals("search")){
                NodeList statusList = doc.getElementsByTagName("status");
                String statusNode = statusList.item(0).getChildNodes().item(0).getNodeValue();
                Log.i(TAG, "status: "+statusNode);
                if (statusNode.equals("OK") ){
                    NodeList resList = doc.getElementsByTagName("location");
                    Node resNode = resList.item(0);
                    Element resElmt = (Element) resNode;
                    NodeList lat = resElmt.getElementsByTagName("lat");
                    s += "lat = " + lat.item(0).getChildNodes().item(0).getNodeValue() + "\n";
                    String lat_ = lat.item(0).getChildNodes().item(0).getNodeValue();
                    NodeList lng = resElmt.getElementsByTagName("lng");
                    s += "lng = " + lng.item(0).getChildNodes().item(0).getNodeValue() + "\n";
                    String lng_ = lng.item(0).getChildNodes().item(0).getNodeValue();
                    Log.i(TAG, s);

                    speech = "찾았어요 ! 지도로 이동하여 표시합니다...";
                    final Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);
                    //add map
                } else {
                    speech = "그런 장소는 찾지 못했어요 ㅠㅠ... 다시 입력해주세요 !";
                    final Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();
                    chatView.receive(receivedMessage);
                }
            }

            super.onPostExecute(doc);
        }
    }
}