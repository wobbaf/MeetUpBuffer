package com.example.magda.meetupbuffer.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.magda.meetupbuffer.R;
import com.example.magda.meetupbuffer.activities.MainActivity;
import com.example.magda.meetupbuffer.helpers.SharedPreferencesUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProposeDestinationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProposeDestinationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChooseFavoritePlacesNavBar extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private GoogleMap map;
    ArrayList<String> chosenPlaces;
    double latitude=0;
    double longitude=0;
    ArrayAdapter<String> itemsAdapter;
    private static View v;
    ListView listViewFavPlaces ;
    // TODO: Rename and change types of parameters
    float historicX = Float.NaN, historicY = Float.NaN;
    static final int DELTA = 50;
    enum Direction {LEFT, RIGHT;}
    private String mParam1;
    private String mParam2;
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    private OnFragmentInteractionListener mListener;

    public ChooseFavoritePlacesNavBar() {
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
    public static ChooseFavoritePlacesNavBar newInstance(String param1, String param2) {
        ChooseFavoritePlacesNavBar fragment = new ChooseFavoritePlacesNavBar();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);


        DrawerLayout drawer = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
        // Inflate the layout for this fragment
        if (container == null) {
            return null;
        }

        v = inflater.inflate(R.layout.fragment_choose_favorite_places, container, false);

        chosenPlaces = new ArrayList<>();
        SharedPreferencesUtil.loadArray(chosenPlaces,getContext(),"fav_places_names");

        listViewFavPlaces = (ListView) v.findViewById(R.id.nav_favourite_places);
        listViewFavPlaces.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        historicX = event.getX();
                        historicY = event.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        if (event.getX() - historicX < -DELTA) {
                            return true;
                        } else if (event.getX() - historicX > DELTA) {
                            int heightOfEachItem = listViewFavPlaces.getChildAt(0).getHeight();
                            int heightOfFirstItem = -listViewFavPlaces.getChildAt(0).getTop() + listViewFavPlaces.getFirstVisiblePosition()*heightOfEachItem;
                            final int firstPosition = (int) Math.ceil(heightOfFirstItem / heightOfEachItem); // This is the same as child #0
                            final int wantedPosition = (int) Math.floor((historicY - listViewFavPlaces.getChildAt(0).getTop()) / heightOfEachItem) + firstPosition;
                            chosenPlaces.remove(wantedPosition);
                            itemsAdapter.notifyDataSetChanged();
                            MainActivity.favorite_places_id.remove(wantedPosition);
                            SharedPreferencesUtil.saveArray(MainActivity.favorite_places_id, getContext(), "fav_places_id");
                            SharedPreferencesUtil.saveArray(chosenPlaces, getContext(), "fav_places_names");
                            map.clear();
                            loadMarkers();
                            return true;
                        }
                        break;
                    default:
                        return false;
                }
                return false;
            }
        });
        itemsAdapter =
                new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, chosenPlaces);
        itemsAdapter.notifyDataSetChanged();

        loadMarkers();

        listViewFavPlaces.setAdapter(itemsAdapter);
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if(!chosenPlaces.contains(place.getName())) {
                    chosenPlaces.add(place.getName().toString());
                    MainActivity.favorite_places_id.add(place.getId());
                    itemsAdapter.notifyDataSetChanged();
                    map.addMarker(new MarkerOptions()
                            .title(place.getName().toString())
                            .snippet(place.getAddress().toString())
                            .position(place.getLatLng()));
                    SharedPreferencesUtil.saveArray(MainActivity.favorite_places_id, getContext(), "fav_places_id");
                    SharedPreferencesUtil.saveArray(chosenPlaces, getContext(), "fav_places_names");

                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });


        return v;
    }

    private void loadMarkers() {
        if(MainActivity.favorite_places_id.size()!=0)
        {
            for (String placeId: MainActivity.favorite_places_id)
            {
                Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getStatus().isSuccess()) {
                                    final Place myPlace = places.get(0);
                                    LatLng queried_location = myPlace.getLatLng();
                                    map.addMarker(new MarkerOptions()
                                            .title(myPlace.getName().toString())
                                            .snippet(myPlace.getAddress().toString())
                                            .position(myPlace.getLatLng()));
                                }
                                places.release();
                            }
                        });
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }




    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (map != null)
            setUpMap();

        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapFavorites)).getMap();
            // Check if we were successful in obtaining the map.
            if (map != null)
                setUpMap();
        }
    }
    private void setUpMap() {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(52.22968, 21.01223), 12.0f));

    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();

        DrawerLayout drawer = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);


        try {

            PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                    getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
            if (autocompleteFragment != null)
             getActivity().getFragmentManager().beginTransaction().remove(autocompleteFragment).commit();


            SupportMapFragment fragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFavorites));
            if (fragment != null)
             getFragmentManager().beginTransaction().remove(fragment).commit();

        } catch (IllegalStateException e) {
            //handle this situation because you are necessary will get
            //an exception here :-(
        }
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
