package com.example.nfc_card_reader;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Date;

@Entity(tableName = "Cards")
public class Card implements Parcelable {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    private String mId;         // The id of the card
    @ColumnInfo(name = "name")
    private String mName;       // The name the user gave the card
    @ColumnInfo(name = "data")
    private byte[][][] mData;   // The data on the card
    @ColumnInfo(name = "timestamp")
    private Date mDateCreated;  // What time it was created
    @ColumnInfo(name = "location")
    private Location mLocation; // Location it was created at

    /* Constants */
    // Mifare Classic 1K cards have 16 sectors that each have
    // 4 blocks of 16 bytes, 16 * 4 * 16 = 1024 = 1KB
    public static final int CARD_SECTORS = 16;
    public static final int CARD_BLOCKS = 4;


    /**
     * Constructor that sets the NAME and tag
     * @param name The NAME of the card
     * @param dateCreated The TIME/time the card was created
     * @param location The LOCATION the card was registered at
     */
    public Card(String name, byte[][][] data, String id, Date dateCreated, Location location) {
        mName = name;
        mData = data;
        mId = id;
        mDateCreated = dateCreated;
        mLocation = location;
    }


    @Override
    public String toString() {
        return "Card{" +
                "mName='" + mName + '\'' +
                ", mId='" + mId + '\'' +
                ", mData=" + Arrays.toString(mData) +
                ", mDateCreated=" + mDateCreated +
                ", mLocation=" + mLocation +
                '}';
    }

    /**
     * Returns the card name
     * @return The name
     */
    public String getName() {
        return mName;
    }

    public void setName(String i_name) {mName = i_name;}
    /**
     * Returns the time created
     * @return The time
     */
    public Date getDateCreated() {
        return mDateCreated;
    }

    /**
     * Returns the location the card was created at
     * @return The location
     */
    public Location getLocation() {
        return mLocation;
    }

    /**
     * Returns the cards ID in hex format
     * @return The ID as a hex string, empty string if tag is empty
     */
    public String getId() {
        return mId;
    }

    /**
     * Returns the data of the card
     * @return The data of the card
     */
    public byte[][][] getData() {
        return mData;
    }


    /* Everything below this has to do with implementing the Parcelable interface */
    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mId);
        dest.writeSerializable(mData);
        dest.writeLong(mDateCreated.getTime());
        dest.writeParcelable(mLocation, flags);
    }

    protected Card(Parcel in) {
        mName = in.readString();
        mId = in.readString();
        mData = (byte[][][])in.readSerializable();
        mDateCreated = new Date(in.readLong());
        mLocation = in.readParcelable(Location.class.getClassLoader());
    }
}
