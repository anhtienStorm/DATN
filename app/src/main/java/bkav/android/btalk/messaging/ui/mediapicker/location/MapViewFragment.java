package bkav.android.btalk.messaging.ui.mediapicker.location;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.util.BtalkGPSReceiver;
import bkav.android.btalk.messaging.util.BtalkNetworkChangeReceiver;

/**
 * Created by quangnd on 14/05/2017.
 * Fragment google map cho ng dung chon vi tri tren ban do
 */

public class MapViewFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, Observer {

    private String mLinkMap;
    MapView mMapView;
    private CameraPosition mCameraPosition;
    private static final String TAG = "MapViewFragment";
    private GoogleMap mMap;

    private final LatLng mDefaultLocation = new LatLng(21.022362, 105.786849);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;


    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    // Lay ra 5 vi tri gan nhat
    private final int mMaxEntries = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    private MarkerTask mMarkerTask;

    private View mLayoutIssue;
    private TextView mTextIssue;
    private Button mTryAgain;

    public interface OnMapConnected {

        void onReadyConnected(boolean isConnect);
    }

    private OnMapConnected mHost;

    public void setHost(OnMapConnected host) {
        this.mHost = host;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        mMarkerTask = new MarkerTask(this);
        View rootView = inflater.inflate(R.layout.location_fragment, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mLayoutIssue = rootView.findViewById(R.id.layout_issue);
        mTextIssue = (TextView) rootView.findViewById(R.id.txt_issue);
        mTryAgain = (Button) rootView.findViewById(R.id.btn_try_again);
        mTryAgain.setOnClickListener(this);
        connectToMap();
        return rootView;
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        // connected to the internet
        return activeNetwork != null && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    private boolean isGPSTurnOn() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Bkav QuangNDb connect toi google map
     */
    private void connectToMap() {
        if (!isConnected()) {
            mLayoutIssue.setVisibility(View.VISIBLE);
            mMapView.setVisibility(View.GONE);
            mTextIssue.setText(R.string.network_issue);
            if (mHost != null) {
                mHost.onReadyConnected(false);
            }
            return;
        }
        if (!isGPSTurnOn()) {
            mLayoutIssue.setVisibility(View.VISIBLE);
            mMapView.setVisibility(View.GONE);
            mTextIssue.setText(R.string.gps_issue);
            if (mHost != null) {
                mHost.onReadyConnected(false);
            }
            return;
        }
        mLayoutIssue.setVisibility(View.GONE);
        mMapView.setVisibility(View.VISIBLE);
        mMapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .enableAutoManage((FragmentActivity) getActivity() /* FragmentActivity */,
                            this /* OnConnectionFailedListener */)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }
        mGoogleApiClient.connect();
        if (mHost != null) {
            mHost.onReadyConnected(true);
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mMapView.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Turn on the My Location layer and the related control on the map.
        mMap.setOnMapClickListener(mMapOnclick);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (mLikelyPlaceNames != null) {
                    for (int i = 0; i < mMaxEntries; i++) {
                        if (mLikelyPlaceNames[i] != null) {
                            // Show a dialog offering the user the list of likely places, and add a
                            // marker at the selected place.
                            LatLng markerLatLng = mLikelyPlaceLatLngs[i];
                            String markerSnippet = mLikelyPlaceAddresses[i];
                            markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[i];
                            // Add a marker for the selected place, with an info window
                            // showing information about that place.
                            String latitude = markerLatLng.latitude + "";
                            String longtitude = markerLatLng.longitude + "";
                            mLinkMap = getString(R.string.link_map, mLikelyPlaceNames[i], markerSnippet, latitude, longtitude);
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions()
                                    .title(mLikelyPlaceNames[i])
                                    .position(markerLatLng)
                                    .snippet(markerSnippet)).showInfoWindow();

                            // Position the map's camera at the location of the marker.
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                                    DEFAULT_ZOOM));
                            return true;
                        }
                    }
                } else {
                    getDeviceLocation();
                    return true;
                }
                return false;
            }
        });
        updateLocationUI();
        getCurrentLocation();
    }

    GoogleMap.OnMapClickListener mMapOnclick = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng point) {
            mMap.clear();
            mMarkerTask.cancel(false);
            if (mMarkerTask.isCancelled()) {
                mMarkerTask = new MarkerTask(MapViewFragment.this);
            }
            if (mMarkerTask.getStatus() != AsyncTask.Status.RUNNING) {
                mMarkerTask.execute(point);
            }
        }
    };

    private void getCurrentLocation() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission")
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                    Status status = likelyPlaces.getStatus();
                    if (status.isSuccess()) {
                        int i = 0;
                        mLikelyPlaceNames = new String[mMaxEntries];
                        mLikelyPlaceAddresses = new String[mMaxEntries];
                        mLikelyPlaceAttributions = new String[mMaxEntries];
                        mLikelyPlaceLatLngs = new LatLng[mMaxEntries];
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            // Build a list of likely places to show the user. Max 5.
                            mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                            mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace().getAddress();
                            mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                    .getAttributions();
                            mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                            if (mLikelyPlaceNames[i] != null) {
                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                LatLng markerLatLng = mLikelyPlaceLatLngs[i];
                                String markerSnippet = mLikelyPlaceAddresses[i];
                                markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[i];
                                // Add a marker for the selected place, with an info window
                                // showing information about that place.
                                mLinkMap = getString(R.string.link_map,mLikelyPlaceNames[i], markerSnippet, mLikelyPlaceLatLngs[i].latitude + "", mLikelyPlaceLatLngs[i].longitude + "");
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions()
                                        .title(mLikelyPlaceNames[i])
                                        .position(markerLatLng)
                                        .snippet(markerSnippet)).showInfoWindow();

                                // Position the map's camera at the location of the marker.
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                                        DEFAULT_ZOOM));
                                break;
                            }
                            i++;
                            if (i > (mMaxEntries - 1)) {
                                break;
                            }
                        }
                    } else {
                        getDeviceLocation();
                    }
                    // Release the place likelihood buffer, to avoid memory leaks.
                    likelyPlaces.release();
                }
            });
        } else {
            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet))).showInfoWindow();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }

        // Set the map's camera position to the current location of the device.
//        if (mCameraPosition != null) {
//            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
//        } else
        if (mLastKnownLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            mMap.clear();
            addMarker(new LatLng(mLastKnownLocation.getLatitude(),
                    mLastKnownLocation.getLongitude()));

        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.clear();
            addMarker(mDefaultLocation);
        }
    }

    private void addMarker(LatLng latLng) {
        String title = getAddressLocation(latLng.latitude, latLng.longitude);
        String snippet = getSnippetLocation(latLng.latitude, latLng.longitude);
        mLinkMap = getString(R.string.link_map,title, snippet, latLng.latitude + "", latLng.longitude + "");
        mMap.clear();
        mMap.addMarker(new MarkerOptions().title(title).position(latLng).snippet(snippet)).showInfoWindow();
    }

    /**
     * Bkav QuangNDb: Lấy tên địa điểm theo vị trí
     */
    private String getAddressLocation(double lat, double lng) {
        Geocoder coder = new Geocoder(getActivity());
        try {
            ArrayList<Address> adress = (ArrayList<Address>) coder.getFromLocation(lat, lng, 5);
            for (Address add : adress) {
                int mMinLengAddress = 8;
                if (add.getAddressLine(0).length() < mMinLengAddress
                        && add.getAddressLine(1) != null) {
                    return add.getAddressLine(0) + ", "
                            + add.getAddressLine(1);
                } else {
                    return add.getAddressLine(0);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    private String getSnippetLocation(double lat, double lng) {
        Geocoder coder = new Geocoder(getActivity());
        String snippet = "";
        try {
            ArrayList<Address> address = (ArrayList<Address>) coder.getFromLocation(lat, lng, 1);
            if (address != null && address.size() > 0) {
                Address obj = address.get(0);
                for (int i = 1; i < 10; i++) {
                    if (obj.getAddressLine(i) == null) {
                        break;
                    }
                    if (obj.getAddressLine(i) != null && obj.getAddressLine(i + 1) != null) {
                        snippet += obj.getAddressLine(i) + ", ";
                    } else {
                        snippet += obj.getAddressLine(i);
                    }
                }
            }
            return snippet;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_try_again:
                connectToMap();
                break;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        // Network state change or gps change se vao day
        connectToMap();
    }

    /**
     * Bkav QuangNDb: AsyncTask hiển thị vị trí của maker<br>
     */
    private static class MarkerTask extends AsyncTask<LatLng, Void, LatLng> {

        WeakReference<MapViewFragment> mFragment;

        public MarkerTask(MapViewFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        protected LatLng doInBackground(LatLng... point) {
            try {
                return point[0];
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: Error task", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            super.onPostExecute(latLng);
            if (mFragment != null) {
                mFragment.get().addMarker(latLng);

            }
        }
    }

    /**
     * Bkav QuangNDb lay link map
     */
    public String getLinkMap() {
        return mLinkMap.replace("null", "").replaceAll("\\s+$", "");
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        // Dang ky lang nghe network, gps change state
        BtalkNetworkChangeReceiver.getObservable().addObserver(this);
        BtalkGPSReceiver.getObservable().addObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        // Huy Dang ky lang nghe network, gps change state
        BtalkNetworkChangeReceiver.getObservable().deleteObserver(this);
        BtalkGPSReceiver.getObservable().deleteObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
