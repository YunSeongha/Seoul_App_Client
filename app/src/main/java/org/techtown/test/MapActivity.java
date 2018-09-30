package org.techtown.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;

public class MapActivity extends NMapActivity {

    private NMapView mMapView;// 지도 화면 View
    private final String CLIENT_ID = "n7AHSzZ7rbcixSSSEgEz";// 애플리케이션 클라이언트 아이디 값
    private NMapController mMapController;
    private NMapResourceProvider nMapResourceProvider;
    private NMapOverlayManager mapOverlayManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String s = intent.getExtras().getString("info");
        double lat = Double.parseDouble(s.split(";")[0]);
        double lng = Double.parseDouble(s.split(";")[1]);
        mMapView = new NMapView(this);
        // create resource provider
        setContentView(mMapView);


        Log.i("TAG", "mapview");
        mMapView.setClientId(CLIENT_ID); // 클라이언트 아이디 값 설정
        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        Log.i("TAG", "enable");
        mMapView.setFocusable(true);
        mMapView.setFocusableInTouchMode(true);
        mMapView.setScalingFactor(1.7f);
        mMapView.requestFocus();

        mMapController = mMapView.getMapController();
        mMapController.setMapCenter(new NGeoPoint(lng, lat), 11);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setMarker();
            }
        }, 5000);
    }

    private void setMarker() {
        Intent intent = getIntent();
        String s = intent.getExtras().getString("info");
        double lat = Double.parseDouble(s.split(";")[0]);
        double lng = Double.parseDouble(s.split(";")[1]);

        int markerId = NMapPOIflagType.PIN;

// set POI data
        NMapPOIdata poiData = new NMapPOIdata(2, nMapResourceProvider);
        poiData.beginPOIdata(2);
        poiData.addPOIitem(lng, lat, "Toilet", markerId, 0);
        Log.i("lat,lang", "lat :" + lat + "lng" + lng);
        poiData.endPOIdata();

        Log.i("poiData", "poiData :" + poiData);

// create POI data overlay
        NMapPOIdataOverlay poiDataOverlay = mapOverlayManager.createPOIdataOverlay(poiData, null);
        poiDataOverlay.showAllPOIdata(0);


    }

    private NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {
        @Override
        public void onFocusChanged(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {

        }

        @Override
        public void onCalloutClick(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
            if (nMapPOIitem != null) {
                Log.e("TAG", "onFocusChanged: " + nMapPOIitem.toString());
            } else {
                Log.e("TAG", "onFocusChanged: ");

            }
        }
    };
}
