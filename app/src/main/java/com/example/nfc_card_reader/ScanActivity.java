
package com.example.nfc_card_reader;

import android.content.Context;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;


public class ScanActivity extends AppCompatActivity {
    private static final String TAG = "ScanActivity";

    private String mId;         // ID of the card
    private byte[][][] mData;   // Data in the card (stored as sectors containing blocks containing a byte array of data)
    private NfcAdapter nfcAdapter;

    /* UI elements */
    private Button mBtnAddCard;
    private TextView mTxtCardId;    // The text that displays a cards ID when scanned
    private View mMainView;         // The main layout of the activity

    /* Constants */
    public static final String CARD_ID = "id";
    public static final String CARD_DATA = "data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        this.initViews();

        // The button is disabled until a valid card is scanned
        mBtnAddCard.setEnabled(false);

        if(savedInstanceState != null) { // An instance to restore
            mId = savedInstanceState.getString(CARD_ID);
            mData = (byte[][][])savedInstanceState.getSerializable(CARD_DATA);

            if(mId != null && !mId.isEmpty()) {
                mTxtCardId.setText("ID: " + mId);
                mBtnAddCard.setEnabled(true);
            }
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // No NFC adapter found, or it is disabled, quit the activity
        if(nfcAdapter == null || !nfcAdapter.isEnabled()) {
            setResult(MainActivity.RESULT_NO_NFC);
            finish();
        }


        mBtnAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ScanActivity.this, AddCardActivity.class);
                i.putExtra(CARD_ID, mId);
                i.putExtra(CARD_DATA, mData);
                startActivityForResult(i, MainActivity.REQUEST_ADD_CARD);
            }
        });
    }

    /**
     * Initializes all UI views
     */
    private void initViews() {
        mBtnAddCard = findViewById(R.id.btn_addCardScan);
        mTxtCardId = findViewById(R.id.txt_cardId);
        mMainView = findViewById(R.id.scanActivityMainView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, ScanActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == MainActivity.RESULT_OK) {
            if(requestCode == MainActivity.REQUEST_ADD_CARD) {
                // Return to MainActivity with the same data (a new card added)
                setResult(MainActivity.RESULT_OK, data);

                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Needs to be overridden for when the app has been opened by scanning a card
        // so it doesn't quit the app, but instead goes to MainActivity
        startActivity(new Intent(ScanActivity.this, MainActivity.class));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mf = MifareClassic.get(tag);

            boolean cardSupported = false;

            // Vibrate to indicate a card was recognised
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            }

            // If a card was scanned, then a new card is scanned reset the UI elements
            // (a card can fail scanning with the button still enabled and the data corrupted)
            mBtnAddCard.setEnabled(false);
            mTxtCardId.setText("");

            if(mf != null) { // Card is a Mifare Classic card
                if(mf.getSize() == 1024) { // Only Mifare Classic 1K support
                    cardSupported = true;

                    mId = ByteArrayToHexString(tag.getId());
                    mData = new byte[Card.CARD_SECTORS][Card.CARD_BLOCKS][];

                    try {
                        mf.connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(mf.isConnected()) { // A connection to the card was made
                        boolean errorReadingCard = false;

                        int sectorCount = mf.getSectorCount();
                        for(int i = 0; i < sectorCount; i++) {
                            try {
                                if(mf.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)) {
                                    int blockCount = mf.getBlockCountInSector(i);
                                    for(int j = 0; j < blockCount; j++) {
                                        int index = mf.sectorToBlock(i);
                                        mData[i][j] = mf.readBlock(index);
                                    }
                                }
                            } catch (IOException e) {
                                // If the card is removed before the connection is closed
                                // an IOException is thrown, show an error message and exit
                                Snackbar sb = Snackbar.make(mMainView, R.string.hold_card_longer, Snackbar.LENGTH_LONG);
                                View sbView = sb.getView();
                                sbView.setBackgroundColor(ContextCompat.getColor(ScanActivity.this, R.color.colorDark));
                                sb.show();

                                errorReadingCard = true;

                                e.printStackTrace();

                                break; // Exit the loop
                            }
                        }

                        if(!errorReadingCard) { // Everything was read correctly
                            mBtnAddCard.setEnabled(true);
                            mTxtCardId.setText("ID: " + mId);
                        }
                    }

                    try {
                        mf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(!cardSupported) {
                // If a valid card was scanned previously, disable the button if a new card is not valid
                mBtnAddCard.setEnabled(false);
                mTxtCardId.setText("");

                Snackbar sb = Snackbar.make(mMainView, R.string.card_not_supported, Snackbar.LENGTH_LONG);
                View sbView = sb.getView();
                sbView.setBackgroundColor(ContextCompat.getColor(ScanActivity.this, R.color.colorDark));
                sb.show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(CARD_ID, mId);
        outState.putSerializable(CARD_DATA, mData);
    }

    /**
     * Converts a byte array to a human readable hex string
     * @param inarray The byte array to convert
     * @return A hex string representation of the array
     */
    public static String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";

        for(j = 0 ; j < inarray.length ; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }
}
