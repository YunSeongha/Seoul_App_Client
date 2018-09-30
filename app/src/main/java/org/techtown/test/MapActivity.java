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

    private double[] lat;
    private double[] lng;

    public static final String TAG = MapActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String s = intent.getExtras().getString("info");
        String sList[] = s.split(";");
        for(int i=0; i<sList.length; i++) {
            if(i%2==0) {
                lat[i/2] = Double.parseDouble(sList[i]);
            }else {
                lng[i/2] = Double.parseDouble(sList[i]);
            }
        }
        for(int i=0; i<sList.length; i++) {
            Log.i(TAG, "lat: " + Double.toString(lat[i]) + ", lng: " + Double.toString(lng[i]));
        }

        mMapView = new NMapView(this);
        setContentView(mMapView);

        init();

        nMapResourceProvider = new NMapViewerResourceProvider(this);
        mapOverlayManager = new NMapOverlayManager(this, mMapView, nMapResourceProvider);

        mMapController = mMapView.getMapController();
        mMapController.setMapCenter(new NGeoPoint(lng[0], lat[0]), 11);
    }
    @Override
    public void onStart() {
        super.onStart();
        mMapView.setBuiltInZoomControls(true, null);
        mMapView.setOnMapStateChangeListener(this);
        moveMapCenter();
    }

    private void init(){
        mMapView.setClientId(CLIENT_ID); // 클라이언트 아이디 값 설정
        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        mMapView.setFocusable(true);
        mMapView.setFocusableInTouchMode(true);
        mMapView.setScalingFactor(1.7f);
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
        NGeoPoint currentPoint = new NGeoPoint(lng[0], lat[0]);
        mMapController.setMapCenter(currentPoint);

        NMapPOIdata poiData = new NMapPOIdata(2, nMapResourceProvider);
        for(int i=0; i<lat.length; i++) {
            poiData.addPOIitem(lng[i], lat[i], "스폿스폿 !", NMapPOIflagType.PIN, 0);
        }
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
