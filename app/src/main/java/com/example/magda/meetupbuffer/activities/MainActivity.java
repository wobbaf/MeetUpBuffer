package com.example.magda.meetupbuffer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import jade.android.MicroRuntimeServiceBinder;
import jade.android.RuntimeCallback;
import com.example.magda.meetupbuffer.R;
import com.example.magda.meetupbuffer.adapters.FriendListAdapter;
import com.example.magda.meetupbuffer.agent.DummyAgent;
import com.example.magda.meetupbuffer.async.DownloadImageTask;
import com.example.magda.meetupbuffer.fragments.ChooseFriendsFragment;
import com.example.magda.meetupbuffer.fragments.ChoosePlacesFragment;
import com.example.magda.meetupbuffer.fragments.DestinationFoundFragment;
import com.example.magda.meetupbuffer.fragments.StartFragment;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import jade.android.MicroRuntimeService;
import jade.core.Profile;
import jade.util.leap.Properties;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,
        ChooseFriendsFragment.OnFragmentInteractionListener,
        ChoosePlacesFragment.OnFragmentInteractionListener,
        DestinationFoundFragment.OnFragmentInteractionListener,
        StartFragment.OnFragmentInteractionListener {
    String nickname = "dummy";
    String host = "192.168.0.11";
    String port = "1099";
    ServiceConnection serviceConnection = null;
    MicroRuntimeServiceBinder microRuntimeService = null;
    boolean bind = false;
    public static ArrayList<JSONObject> friendsListData = new ArrayList();
    JSONArray list = null;
    ListView firendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fragment fragment = new StartFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            list = response.getJSONObject().getJSONArray("data");
                            for (int i = 0; i < list.length(); i++) {
                                friendsListData.add(list.getJSONObject(i));
                            }
                            ;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView view = (NavigationView) findViewById(R.id.nav_view);
        View headerLayout =
                view.inflateHeaderView(R.layout.nav_header_main);
        ImageView image = (ImageView) (headerLayout.findViewById(R.id.imageView));
        TextView name = (TextView) (headerLayout.findViewById(R.id.nameTextView));

        com.facebook.Profile profile = com.facebook.Profile.getCurrentProfile();
        new DownloadImageTask(image).execute("https://graph.facebook.com/" + profile.getId() + "/picture?type=large");
        name.setText(profile.getFirstName());
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //final ListView navigationView = (ListView) findViewById(R.id.left_drawer);
        //navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            LoginManager.getInstance().logOut();
            friendsListData.clear();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }
        if (id == R.id.start_main_container) {
            bindService();
        }

        return super.onOptionsItemSelected(item);
    }

    private void bindService() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                microRuntimeService = (MicroRuntimeServiceBinder) service;

                Properties pp = new Properties();
                pp.setProperty(Profile.MAIN_HOST, host);
                pp.setProperty(Profile.MAIN_PORT, port);
                pp.setProperty(Profile.JVM, Profile.ANDROID);

                microRuntimeService.startAgentContainer(pp, new RuntimeCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Split container startup successfull
                        microRuntimeService.startAgent(nickname, DummyAgent.class.getName(), new Object[]{getApplicationContext()}, new RuntimeCallback<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Agent succesfully started
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                //Agent startup error
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        // Split container startup error
                    }
                });
                ;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                microRuntimeService = null;
            }
        };
        bindService(new Intent(getApplicationContext(), MicroRuntimeService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_friends) {
            // Handle the camera action
        } else if (id == R.id.nav_preferences) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
