package com.example.magda.meetupbuffer.fragments;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.magda.meetupbuffer.R;
import com.example.magda.meetupbuffer.activities.MainActivity;
import com.example.magda.meetupbuffer.agent.AgentInterface;
import com.example.magda.meetupbuffer.parsers.XMLParse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jade.core.MicroRuntime;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChoosePlacesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChoosePlacesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChoosePlacesFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    int chosenTypePlaceIdx =0;
    List<String> EngToDisplayPlaces = Arrays.asList("Pub", "Restaurant", "Theater", "Club","Swimming pool","Theater","Bowling Club","Kebab");
    List<String> PlToServerPlaces = Arrays.asList("pub","restauracja","teatr","klub","pływalnia","tratr","kręgielnia","kebab");
    String place = "Pub" ;
    protected Location mLastLocation;
    protected GoogleApiClient mGoogleApiClient;
    final int DATEPICKER_FRAGMENT = 1;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private AgentInterface agentInterface;
    private OnFragmentInteractionListener mListener;
    public static ArrayList<String> friendsID;
    public ChoosePlacesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChoosePlacesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChoosePlacesFragment newInstance(String param1, String param2) {
        ChoosePlacesFragment fragment = new ChoosePlacesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        buildGoogleApiClient();
        try {
			agentInterface = MicroRuntime.getAgent(MainActivity.getNickname())
					.getO2AInterface(AgentInterface.class);
	} catch (StaleProxyException e) {
			//showAlertDialog(getString(R.string.msg_interface_exc), true);
		} catch (ControllerException e) {
			//showAlertDialog(getString(R.string.msg_controller_exc), true);
		}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_choose_places, container, false);
        ListView listViewPlaces = (ListView) v.findViewById(R.id.listViewPlaces);
        //listViewPlaces.setAdapter(new PlacesAdapter(getActivity(), R.layout.places_list_item, places));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                EngToDisplayPlaces );
        listViewPlaces.setAdapter(arrayAdapter);
        listViewPlaces.setChoiceMode(listViewPlaces.CHOICE_MODE_SINGLE);
        listViewPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chosenTypePlaceIdx = position;
                //CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(R.id.placesItem);
                //checkedTextView.toggle();
                //Toast.makeText(getActivity().getApplicationContext(), checkedTextView.getText().toString(), Toast.LENGTH_SHORT).show();
                //set place String
            }
        });
        listViewPlaces.setChoiceMode(listViewPlaces.CHOICE_MODE_MULTIPLE);
        Button buttonGo = (Button) v.findViewById(R.id.buttonFragmentDestination);
        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.setTargetFragment(ChoosePlacesFragment.this, DATEPICKER_FRAGMENT);
                newFragment.show(getActivity().getSupportFragmentManager(), "TimePicker");
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode) {
            case DATEPICKER_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {
                    // here the part where I get my selected date from the saved variable in the intent and the displaying it.

                    Bundle bundle = data.getExtras();
                    String resultDate = bundle.getString("selectedDate", "error");
                    Toast.makeText(getActivity(),resultDate
                            ,Toast.LENGTH_LONG).show();
                    getLocation();

                    friendsID=((MainActivity)getActivity()).getFriendsID();
                    //LastLocation
                    String localization = mLastLocation.getLatitude() + " " + mLastLocation.getLongitude();
                    //String type, String id, String location, String state, String time, String placeId, String placeType, ArrayList<String> friends, ArrayList<String> favPlaces)
                    String content = setContent("0", MainActivity.getNickname(), localization, "accepting", resultDate, null, PlToServerPlaces.get(chosenTypePlaceIdx), friendsID, MainActivity.favorite_places_id);
                    try {
                        agentInterface = MicroRuntime.getAgent(MainActivity.getNickname())
                                .getO2AInterface(AgentInterface.class);
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    }
                    agentInterface.sendMessage(content, ACLMessage.INFORM);
                    Fragment fragment = new WaitForFriendsFragment();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    /*String loc = agentInterface.getDestination();
                    Bundle bundle1 = new Bundle();
                    String myMessage = loc;
                    bundle1.putString("message", myMessage );
                    Fragment fragment = new DestinationFoundFragment();
                    fragment.setArguments(bundle1);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();*/
                }
                break;
            }
        }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
        } else {
            Toast.makeText(getActivity(), "No location detected", Toast.LENGTH_LONG).show();
        }
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


}
