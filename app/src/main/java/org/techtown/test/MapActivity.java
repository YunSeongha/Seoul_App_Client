package org.techtown.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;

public class MapActivity extends NMapActivity
        implements NMapView.OnMapStateChangeListener, NMapPOIdataOverlay.OnStateChangeListener {
    private final String CLIENT_ID = "n7AHSzZ7rbcixSSSEgEz";// 애플리케이션 클라이언트 아이디 값

    private NMapController mMapController;
    private NMapView mMapView;

    private NMapResourceProvider nMapResourceProvider;
    private NMapOverlayManager mapOverlayManager;

    private double lat;
    private double lng;
    private String addr;
    private double lat1, lat2, lat3;
    private double lng1, lng2, lng3;
    private String addr1, addr2, addr3;

    private int flag=0;

    public static final String TAG = MapActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
//        String s = intent.getExtras().getString("info");
//        Log.i(TAG,intent.getExtras().getString("info1"));
        if(intent.getExtras().getString("info1")==null || intent.getExtras().getString("info1").equals("")) {
            String sList[] = intent.getExtras().getString("info").split(";");
            lat = Double.parseDouble(sList[0]);
            lng = Double.parseDouble(sList[1]);
            addr = sList[2];
            flag = 0;
        } else {
            String sList[] = intent.getExtras().getString("info1").split(";");
            lat1 = Double.parseDouble(sList[0]);
            lng1 = Double.parseDouble(sList[1]);
            lat2 = Double.parseDouble(sList[2]);
            lng2 = Double.parseDouble(sList[3]);
            lat3 = Double.parseDouble(sList[4]);
            lng3 = Double.parseDouble(sList[5]);
            addr1 = sList[6];
            addr2 = sList[7];
            addr3 = sList[8];

            flag = 1;
        }

        mMapView = new NMapView(this);
        setContentView(mMapView);

        init();

        nMapResourceProvider = new NMapViewerResourceProvider(this);
        mapOverlayManager = new NMapOverlayManager(this, mMapView, nMapResourceProvider);

        mMapController = mMapView.getMapController();
        if(flag == 0){
            mMapController.setMapCenter(new NGeoPoint(lng, lat), 11);
        }
        if(flag == 1){
            mMapController.setMapCenter(new NGeoPoint(lng1, lat1), 11);
        }

    }
    @Override
    public void onStart() {
        super.onStart();
        mMapView.setBuiltInZoomControls(true, null);
        mMapView.setOnMapStateChangeListener(this);
        mMapView.setScalingFactor(2.0f);
        moveMapCenter();
    }

    private void init(){
        mMapView.setClientId(CLIENT_ID); // 클라이언트 아이디 값 설정
        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        mMapView.setFocusable(true);
        mMapView.setFocusableInTouchMode(true);
        mMapView.setScalingFactor(2.0f);
        mMapView.requestFocus();
    }

    @Override
    public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {
        if (nMapError == null) {
            moveMapCenter();
        } else {
            Log.e("map init error", nMapError.message);
        }
    }

    private void moveMapCenter() {
        NGeoPoint currentPoint = null;
        if(flag==0){
            currentPoint = new NGeoPoint(lng, lat);
        } else {
            currentPoint = new NGeoPoint(lng1, lat1);
        }
        mMapController.setMapCenter(currentPoint);

        NMapPOIdata poiData = new NMapPOIdata(2, nMapResourceProvider);
        if(flag==0){
            poiData.addPOIitem(lng, lat, addr, NMapPOIflagType.PIN, 0);
        }
        if(flag==1){
            poiData.addPOIitem(lng1, lat1, addr1, NMapPOIflagType.PIN, 0);
            poiData.addPOIitem(lng2, lat2, addr2, NMapPOIflagType.PIN, 0);
            poiData.addPOIitem(lng3, lat3, addr3, NMapPOIflagType.PIN, 0);
        }
//        for(int i=0; i<lat.length; i++) {
//            poiData.addPOIitem(lng[i], lat[i], "스폿스폿 !", NMapPOIflagType.PIN, 0);
//        }
        poiData.endPOIdata();

        NMapPOIdataOverlay poiDataOverlay = mapOverlayManager.createPOIdataOverlay(poiData, null);
        poiDataOverlay.showAllPOIdata(0);
        poiDataOverlay.setOnStateChangeListener(this);
    }

    @Override
    public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {

    }

    @Override
    public void onMapCenterChangeFine(NMapView nMapView) {

    }

    @Override
    public void onZoomLevelChange(NMapView nMapView, int i) {

    }

    @Override
    public void onAnimationStateChange(NMapView nMapView, int i, int i1) {

    }

    @Override
    public void onFocusChanged(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {

    }

    @Override
    public void onCalloutClick(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {

    }
}
