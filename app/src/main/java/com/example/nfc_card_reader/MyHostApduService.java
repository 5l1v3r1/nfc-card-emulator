package com.example.nfc_card_reader;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

public class MyHostApduService extends HostApduService {
    private static final String TAG = "MyHostApduService";

    private int messageCounter = 0;

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        Log.d(TAG, "processCommandApdu: card found");

        messageCounter++;

        // Needs to have a "main card" and use "card.getData()",
        // return that data (using messageCounter to send the correct one)

        return null;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.i(TAG, "Deactivated: " + reason);
    }
}