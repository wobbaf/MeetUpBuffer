package com.example.magda.meetupbuffer.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.magda.meetupbuffer.R;
import com.example.magda.meetupbuffer.activities.MainActivity;
import com.example.magda.meetupbuffer.agent.AgentInterface;
import com.example.magda.meetupbuffer.parsers.XMLParse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import jade.core.MicroRuntime;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProposeDestinationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProposeDestinationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProposeDestinationFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static GoogleMap map;
    double latitude=0;
    double longitude=0;
    static FragmentManager fm;
    private AgentInterface agentInterface;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    static Marker marker;
    protected static GoogleApiClient mGoogleApiClient;
    static FragmentActivity activity;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    private OnFragmentInteractionListener mListener;

    public ProposeDestinationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DestinationFoundFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DestinationFoundFragment newInstance(String param1, String param2) {
        DestinationFoundFragment fragment = new DestinationFoundFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        try {
            agentInterface = MicroRuntime.getAgent(MainActivity.getNickname())
                    .getO2AInterface(AgentInterface.class);
        } catch (StaleProxyException e) {
            //showAlertDialog(getString(R.string.msg_interface_exc), true);
        } catch (ControllerException e) {
            //showAlertDialog(getString(R.string.msg_controller_exc), true);
        }
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String placeId = this.getArguments().getString("message");
        fm = getActivity().getSupportFragmentManager();
        // Inflate the layout for this fragment
        activity = getActivity();
        View v = inflater.inflate(R.layout.fragment_propose_destination, container, false);
        map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess()) {
                            final Place myPlace = places.get(0);
                            LatLng queried_location = myPlace.getLatLng();
                            marker = map.addMarker(new MarkerOptions()
                                    .title(myPlace.getName().toString())
                                    .snippet(myPlace.getAddress().toString())
                                    .position(myPlace.getLatLng()));
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(queried_location, 15));
                        }
                        places.release();
                    }
                });
        Button buttonYes = (Button) v.findViewById(R.id.buttonLove);
        Button buttonNo = (Button) v.findViewById(R.id.buttonHate);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String localization = mLastLocation.getLatitude() + " " + mLastLocation.getLongitude();
                //String type, String id, String location, String state, String time, String placeId, String placeType, ArrayList<String> friends, ArrayList<String> favPlaces)
                String content = setContent("4", MainActivity.getNickname(), localization, "accepting", null, null, null, null, null);
                agentInterface.sendMessage(content, ACLMessage.ACCEPT_PROPOSAL);
            }
        });
        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = setContent("4", MainActivity.getNickname(), null, "decline", null, null, null, null, null);
                agentInterface.sendMessage(content, ACLMessage.REJECT_PROPOSAL);
            }
        });
        // Move the camera instantly to hamburg with a zoom of 15.

        // Zoom in, animating the camera.
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public static void nextlocationFound(final String placeId){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                map.clear();
                Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getStatus().isSuccess()) {
                                    final Place myPlace = places.get(0);
                                    LatLng queried_location = myPlace.getLatLng();
                                    marker = map.addMarker(new MarkerOptions()
                                            .title(myPlace.getName().toString())
                                            .snippet(myPlace.getAddress().toString())
                                            .position(myPlace.getLatLng()));
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(queried_location, 15));
                                }
                                places.release();
                            }
                        });
            }
        });

    }

    public static void locationFound(String location){
        Bundle bundle1 = new Bundle();
        bundle1.putString("message", location);
        Fragment fragment = new DestinationFoundFragment();
        fragment.setArguments(bundle1);
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //Marker gpsLocation = map.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
            //map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 15));
        } else {
            //Toast.makeText(this, "No location detected", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        //Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        //Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    private String setContent(String type, String id, String location, String state, String time, String placeId, String placeType, ArrayList<String> friends, ArrayList<String> favPlaces){
        String content = null;
        XMLParse p = new XMLParse();
        if (type != null)
            p.instance().type = type;
        if (id != null)
            p.instance().id = id;
        if (location != null)
            p.instance().location = location;
        if (state != null)
            p.instance().state = state;
        if (time != null)
            p.instance().time = time;
        if (placeType != null)
            p.instance().placeType = placeType;
        if (placeId != null)
            p.instance().placeId = placeId;
        if (friends != null)
            p.instance().friends = new ArrayList<>(friends);
        if (favPlaces != null)
            p.instance().favPlaces = new ArrayList<>(favPlaces);
        content = p.Parse();
        return content;
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
