package com.example.nfc_card_reader;

import android.arch.persistence.room.TypeConverter;
import android.location.Location;
import android.util.Log;

import java.util.Date;
import java.util.Locale;

public class CardConverter {
    private static final String TAG = "CardConverter";

    @TypeConverter
    public static Long dateToLong(Date value)
    {
        if(value == null)
            return null;
        return value.getTime();
    }

    @TypeConverter
    public static Date longToDate(Long value)
    {
        if(value == null)
            return null;
        return new Date(value);
    }

    @TypeConverter
    public static String locationToString(Location value)
    {
        if(value == null)
            return null;

        return(String.format(Locale.getDefault(),"%f,%f",value.getLatitude(),value.getLongitude()));
    }

    @TypeConverter
    public static Location stringtoLocation(String value)
    {
        if(value == null)
            return  null;
        String[] pieces = value.split(",");
        Location result = new Location("");

        result.setLatitude(Double.parseDouble(pieces[0]));
        result.setLongitude(Double.parseDouble(pieces[1]));

        return result;
    }


   @TypeConverter
    public static String bytesToString(byte [][][] value)
   {
       if (value == null)
           return null;

       String result = "";
       for (byte[][] bytes2d: value) {
           for (byte [] bytes1d : bytes2d) {
               for (byte mByte: bytes1d) {
                   result+=mByte;
               }
               result+=";";
           }
       }

       Log.d(TAG, "fromBytes: " + result);
       return result;
   }

   @TypeConverter
    public static byte[][][] stringToBytes(String value)
   {
       Log.d(TAG, "toBytes: " + value);
       if(value == null)
           return null;

       byte [][][] result = new byte[Card.CARD_SECTORS][Card.CARD_BLOCKS][];
       int piece_index = 0;
       String [] pieces = value.split(";");
       for(int i = 0; i < Card.CARD_SECTORS; i++)
       {
           for(int j = 0; j < Card.CARD_BLOCKS; j++)
           {
               result[i][j] = pieces[piece_index].getBytes();
           }
       }
       return result;
   }

}
