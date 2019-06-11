package com.example.nfc_card_reader;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ArrayList<Card> mCards;

    /* Database */
    private static final String DATABASE_NAME = "CardDB";
    private CardDataBase dataBase;

    /* UI elements */
    private Button scanButton;
    private Button emuButton;
    private Button editButton;
    private ImageButton languageButton;
    private View mMainView;


    /* Public constants */
    public static final int RESULT_OK = 1;
    public static final int RESULT_NO_NFC = 2;

    public static final int REQUEST_ADD_CARD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCards = new ArrayList<>();
        new getDataTask().execute();

        scanButton = findViewById(R.id.scanActivityButton);
        emuButton = findViewById(R.id.emulateActivityButton);
        editButton = findViewById(R.id.editActivityButton);
        languageButton = findViewById(R.id.languageButton);
        mMainView = findViewById(R.id.mainActivity_mainView);

        //make sure the flag matches the selected language
        if(LocaleChanger.getPersistentData(this,"en").equals("en"))
        {
            languageButton.setBackgroundResource(R.drawable.england_flag);
        } else {
            languageButton.setBackgroundResource(R.drawable.norway_flag);
        }

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(MainActivity.this,ScanActivity.class),
                        REQUEST_ADD_CARD
                );
            }
        });

        emuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CardSelectionActivity.class);
                intent.putExtra("cards", mCards);
                intent.putExtra("edit",false);
                startActivity(intent);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CardSelectionActivity.class);
                intent.putExtra("cards", mCards);
                intent.putExtra("edit",true);
                startActivity(intent);
            }
        });

        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LocaleChanger.getPersistentData(MainActivity.this,"en").equals("en")) {
                    LocaleChanger.setLocale(MainActivity.this,"no");
                    //We have to recreate the activity to make the changes appear
                    recreate();
                } else {
                    LocaleChanger.setLocale(MainActivity.this,"en");
                    //same as above
                    recreate();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.REQUEST_ADD_CARD) {
            switch (resultCode) {
                case MainActivity.RESULT_OK:
                    Card c = data.getExtras().getParcelable("card");
                    new insertDataTask().execute(c);
                    break;

                case MainActivity.RESULT_NO_NFC:
                    Snackbar sb = Snackbar.make(mMainView, R.string.nfc_not_available, Snackbar.LENGTH_LONG);
                    View sbView = sb.getView();
                    sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorDark));
                    sb.show();

                    break;

                default:
                    break;
            }
        }
    }


    //Asynctask to get the list of stored cards from the Database
    private class getDataTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {
            dataBase =  Room.databaseBuilder(getApplicationContext(),CardDataBase.class,DATABASE_NAME).build();
            mCards = new ArrayList<>(dataBase.daoAccess().getCards());
            return null;
        }
    }

    //Asynctask to check if the scanned card already exists in the database
    //and add it to the database if not
    private class insertDataTask extends AsyncTask<Card,Void,Void>
    {
        @Override
        protected Void doInBackground(Card... cards) {
            dataBase = Room.databaseBuilder(getApplicationContext(),CardDataBase.class,DATABASE_NAME).build();
            if(dataBase.daoAccess().cardExist(cards[0].getId()) == null) {
                dataBase.daoAccess().insert(cards);
                mCards.add(cards[0]);
            } else {
                Snackbar sb = Snackbar.make(mMainView, R.string.card_in_database, Snackbar.LENGTH_LONG);
                View sbView = sb.getView();
                sbView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorDark));
                sb.show();
            }
            return null;
        }
    }
}
