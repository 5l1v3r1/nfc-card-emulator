package com.example.nfc_card_reader;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


public class EmulateActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {
    private static final String TAG = "EmulateActivity";

    private NfcAdapter mNfcAdapter;
    private Card mCard;

    /* UI elements */
    TextView mTxtId;
    TextView mTxtName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emulate);

        this.initViews();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(mNfcAdapter == null || !mNfcAdapter.isEnabled()) {
            Toast.makeText(this, R.string.nfc_not_available, Toast.LENGTH_SHORT).show();
        }

        mCard = getIntent().getExtras().getParcelable(CardListAdapter.CARD);

        mTxtId.setText(mCard.getId());
        mTxtName.setText(mCard.getName());

        NdefRecord[] records = new NdefRecord[Card.CARD_SECTORS * Card.CARD_BLOCKS];

        // Register NFC callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);
    }

    /**
     * Initializes all UI elements
     */
    private void initViews() {
        mTxtId = findViewById(R.id.txtEmulate_id);
        mTxtName = findViewById(R.id.txtEmulate_cardName);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        Toast.makeText(this, "Beaming", Toast.LENGTH_LONG).show();

        byte[][][] data = mCard.getData();
        NdefRecord[] records = new NdefRecord[Card.CARD_SECTORS * Card.CARD_BLOCKS];

        for(int i = 0; i < Card.CARD_SECTORS; i++) {
            for(int j = 0; j < Card.CARD_BLOCKS; j++) {
                String k = ScanActivity.ByteArrayToHexString(data[i][j]);
                k = k.substring(2, k.length());
                byte[] d = k.getBytes();
                Log.d(TAG, "createNdefMessage: " + d);

                try {
                    // Conversion from 2d array to 1d array
                    records[i * Card.CARD_BLOCKS + j] = new NdefRecord(d);
                    Log.d(TAG, "onCreate: record created");
                } catch (FormatException e) {
                    Log.d(TAG, "onCreate: Failed creating record");
                    e.printStackTrace();
                }
            }
        }

        return new NdefMessage(records);
    }
}
