package com.example.nfc_card_reader;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {
    //Views
    private EditText name;
    private TextView error;
    private Button save;
    private Button cancel;

    //Card Data
    ArrayList<Card> mCards;

    //Database
    private static final String DATABASE_NAME = "CardDB";
    private CardDataBase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //make sure the language matches that chosen in MainActivity
        LocaleChanger.setLocale(this,LocaleChanger.getPersistentData(this,"en"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mCards = getIntent().getParcelableArrayListExtra("cards");
        final int card = getIntent().getIntExtra("card",1);

        name = findViewById(R.id.edit_edittxt);
        name.setText(mCards.get(card).getName());
        error= findViewById(R.id.edit_txt_error);

        save = findViewById(R.id.edit_btn_save);
        cancel = findViewById(R.id.edit_btn_cancel);

        //makes sure that you can not have a card with a empty name by disableing the save button and
        //showing an error message
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = name.getText().toString();
                if(str.length() == 0){
                    error.setText(getResources().getString(R.string.empty_name));
                    save.setEnabled(false);
                } else {
                    error.setText("");
                    save.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Card new_card = mCards.get(card);
                new_card.setName(name.getText().toString());

                mCards.get(card).setName(name.getText().toString());
                startActivity(new Intent(EditActivity.this,CardSelectionActivity.class).putExtra("cards",mCards).putExtra("edit",true));
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    //Asynctask that updates the database entry for the card that gets sent in
    private class updateData extends AsyncTask<Card,Void,Void>
    {

        @Override
        protected Void doInBackground(Card... cards) {
            dataBase = Room.databaseBuilder(getApplicationContext(),CardDataBase.class,DATABASE_NAME).build();
            dataBase.daoAccess().update(cards);
            return null;
        }
    }
}
