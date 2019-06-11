package com.example.nfc_card_reader;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface cardDao {
    @Insert
    public void insert(Card... cards);

    @Update
    public void update(Card... cards);

    @Delete
    public void  delete(Card card);

    //gets all cards
    @Query("Select * FROM Cards")
    public List<Card> getCards();

    //checks if the card send in allready exists
    //if so, returns said card
    @Query("Select * FROM Cards WHERE id = :id")
    public Card cardExist(String id);
}
