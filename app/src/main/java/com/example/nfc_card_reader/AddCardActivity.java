package com.example.nfc_card_reader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddCardActivity extends AppCompatActivity {
    private static final String TAG = "AddCardActivity";

    /* Card related variables */
    private String mId;
    private byte[][][] mData;
    private Date mDate;
    private String mName;
    private Location mLocation;

    /* Misc */
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Thread mLocationChecker;

    /* UI elements */
    private EditText mEditTxtName;
    private TextView mTxtCardId;
    private TextView mTxtCardDateCreated;
    private TextView mTxtCardLocation;
    private Button mBtnAddCard;
    private Button mBtnCancel;
    private ProgressBar mPrgLocation;


    /* Constants */
    private static final String CARD_NAME = "name";
    private static final String CARD_ID = "id";
    private static final String CARD_DATA = "data";
    private static final String CARD_DATE = "date";
    private static final String CARD_LOCATION = "location";

    public static final int REQUEST_PERMISSION_GPS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        this.initViews();

        mBtnAddCard.setEnabled(false);
        mPrgLocation.setVisibility(ProgressBar.GONE);

        if(savedInstanceState != null) { // Instance to restore
            mId = savedInstanceState.getString(CARD_ID);
            mName = savedInstanceState.getString(CARD_NAME);
            mData = (byte[][][])savedInstanceState.getSerializable(CARD_DATA);
            mDate = (Date)savedInstanceState.getSerializable(CARD_DATE);
            mLocation = savedInstanceState.getParcelable(CARD_LOCATION);

            if(mName != null && !mName.isEmpty()) {
                mBtnAddCard.setEnabled(true);
            }

            if(mLocation != null) {
                setLocationText();
            }
        } else {
            Bundle extras = getIntent().getExtras();

            mId = extras.getString(ScanActivity.CARD_ID, "");
            mData = (byte[][][])extras.getSerializable(ScanActivity.CARD_DATA);
            mDate = new Date();
            mLocation = null;
        }

        mTxtCardId.setText(mId);

        String dateFormated = CardListAdapter.cardDateFormat.format(mDate);
        mTxtCardDateCreated.setText(dateFormated);


        /* Event listeners */
        mBtnAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getIntent();
                i.putExtra("card", new Card(mName, mData, mId, mDate, mLocation));

                setResult(MainActivity.RESULT_OK, i);
                finish();
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddCardActivity.this, MainActivity.class));
            }
        });

        mEditTxtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mName = s.toString();

                // Enable the button if there is something entered
                if(mName.isEmpty()) {
                    mBtnAddCard.setEnabled(false);
                } else {
                    mBtnAddCard.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Not implemented */}

            @Override
            public void afterTextChanged(Editable s) { /* Not implemented */ }
        });

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocation = location;

                Log.d(TAG, "onLocationChanged: Setting LOCATION");

                setLocationText();
            }

            @Override
            public void onProviderDisabled(String provider) {
                // GPS on the phone is disabled, ask to enable it
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { /* Not implemented */ }

            @Override
            public void onProviderEnabled(String provider) { /* Not implemented */ }
        };


        // If the user has enabled GPS permissions wait until the GPS location has been found
        mLocationChecker = new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTxtCardLocation.setText(R.string.addCard_waitForLocation);
                        mPrgLocation.setVisibility(ProgressBar.VISIBLE);
                    }
                });

                while(mLocation == null) { /* Wait until the LOCATION is retrieved */ }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPrgLocation.setVisibility(ProgressBar.GONE);
                    }
                });
            }
        });

        if(this.hasGpsPermissions() && mLocation == null) {
            mLocationChecker.start();
        }

        if(mLocation == null) { // Only request location if there isn't one found already
            requestLocation();
        }
    }

    /**
     * Initializes all UI elements
     */
    private void initViews() {
        mEditTxtName = findViewById(R.id.editTxt_cardName);
        mTxtCardId = findViewById(R.id.txtAddCard_cardId);
        mTxtCardDateCreated = findViewById(R.id.txtAddCard_dateCreated);
        mTxtCardLocation = findViewById(R.id.txtAddCard_location);
        mBtnAddCard = findViewById(R.id.btn_addCard);
        mBtnCancel = findViewById(R.id.btn_cancelAddCard);
        mPrgLocation = findViewById(R.id.prg_location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_GPS:
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) { // Permission was granted
                    if(mLocation == null) {
                        mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, null);
                        mLocationChecker.start();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(CARD_NAME, mName);
        outState.putString(CARD_ID, mId);
        outState.putSerializable(CARD_DATA, mData);
        outState.putSerializable(CARD_DATE, mDate);
        outState.putParcelable(CARD_LOCATION, mLocation);
    }


    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_GPS
            );
        } else {
            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, null);
        }
    }

    /**
     * Checks if GPS permissions are set
     * @return boolean
     */
    private boolean hasGpsPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Sets the text of txtAddCard_location to the current location
     */
    private void setLocationText() {
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);

            Address address = addresses.get(0);

            String countryCode = address.getCountryCode();
            String municipality = address.getSubAdminArea();
            String zip = address.getPostalCode();

            mTxtCardLocation.setText(String.format(
                    Locale.getDefault(),
                    "%s, %s, %s",
                    zip,
                    municipality,
                    countryCode)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
