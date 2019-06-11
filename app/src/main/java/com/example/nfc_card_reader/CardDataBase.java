package com.example.nfc_card_reader;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {Card.class},version = 1,exportSchema = false)
@TypeConverters(CardConverter.class)
public abstract class CardDataBase extends RoomDatabase {
    public abstract cardDao daoAccess();
}
