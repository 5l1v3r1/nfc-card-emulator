package com.example.nfc_card_reader;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import java.util.ArrayList;

public class CardSelectionActivity extends AppCompatActivity {
    private static final String TAG = "CardSelectionActivity";

    private boolean mFirstLoad; // First time the activity is loaded
    private CardListAdapter mCardListAdapter;
    private Thread mLocationChecker;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;

    /* UI elements */
    private RecyclerView mCardList;
    private Spinner mSpnSort;
    private ProgressBar mPrgLocation;

    public static final int REQUEST_PERMISSION_GPS = 1;
    private static final String USER_LOCATION = "location";
    private static final String FIRST_LOAD = "first_load";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_selection);

        this.initViews();

        if(savedInstanceState != null) {
            mFirstLoad = savedInstanceState.getBoolean(FIRST_LOAD);
            mLocation =  savedInstanceState.getParcelable(USER_LOCATION);
        } else {
            mLocation = null;
            mFirstLoad = true;
        }

        Bundle extras = getIntent().getExtras();
        ArrayList<Card> cards = extras.getParcelableArrayList("cards");

        //if we came from the Edit button we set edit to true so clicking a card lets us edit it
        //if we came from the Emulate button we set edit to false so clicking a card lets us emulate it
        mCardListAdapter = new CardListAdapter(cards, this, extras.getBoolean("edit"));
        mCardList.setLayoutManager(new LinearLayoutManager(this));
        mCardList.setAdapter(mCardListAdapter);

        mSpnSort.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                CardListAdapter.SORTING_METHODS
        ));


        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocation = location;
            }

            @Override
            public void onProviderDisabled(String provider) { /* Not implemented */ }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { /* Not implemented */ }

            @Override
            public void onProviderEnabled(String provider) { /* Not implemented */ }
        };


        mSpnSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // This listener is called when the activity first loads, so ignore that
                if (mFirstLoad) {
                    mFirstLoad = false;
                    return;
                }

                final String sortingMethod = CardListAdapter.SORTING_METHODS[position];

                // Sort on location, find current location
                if (sortingMethod.equals(CardListAdapter.SORT_LOCATION)) {
                    if(mLocation == null) { // Only get location if it hasn't been found already
                        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            mLocationChecker.start();
                            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, null);
                        } else { // No permission, request it
                            requestPermissions(
                                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                                    REQUEST_PERMISSION_GPS
                            );
                        }
                    }
                }

                // Start a new thread which waits for the location to be finished
                // When the location thread is joined, the cards are sorted
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mLocationChecker.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        mCardListAdapter.sortCards(sortingMethod, mLocation);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCardListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* Not implemented */ }
        });


        mLocationChecker = new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPrgLocation.setVisibility(ProgressBar.VISIBLE);
                    }
                });

                Log.d(TAG, "run: " + mPrgLocation.getVisibility());

                while(mLocation == null) { /* Wait until the LOCATION is retrieved */ }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPrgLocation.setVisibility(ProgressBar.GONE);
                    }
                });
            }
        });
    }

    /**
     * Initializes all UI elements
     */
    private void initViews() {
        mCardList = findViewById(R.id.rcl_cardList);
        mSpnSort = findViewById(R.id.spn_cardSort);
        mPrgLocation = findViewById(R.id.prg_locationCardSelection);

        mPrgLocation.setVisibility(ProgressBar.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(FIRST_LOAD, mFirstLoad);
        outState.putParcelable(USER_LOCATION, mLocation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_GPS:
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) { // Permission was granted
                    mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, null);
                    mLocationChecker.start();
                }
                break;
            default:
                break;
        }
    }
}
