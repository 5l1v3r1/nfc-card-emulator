package com.example.nfc_card_reader;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardListViewHolder> {
    public static final SimpleDateFormat cardDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final String TAG = "CardListAdapter";

    private ArrayList<Card> mCards;
    private Context mContext;
    private boolean mEditActivity;
    private Location mLocation;

    public static final String SORT_TIME = "Time";
    public static final String SORT_LOCATION = "Location";
    public static final String SORT_NAME = "Name";
    public static final String[] SORTING_METHODS = {SORT_TIME, SORT_LOCATION, SORT_NAME};

    public static final String CARD = "card";

    public CardListAdapter(ArrayList<Card> cards, Context context, boolean editActivity) {
        mCards = cards;
        mContext = context;
        mEditActivity = editActivity;
    }

    @NonNull
    @Override
    public CardListAdapter.CardListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(
                viewGroup.getContext()).inflate(R.layout.card_list_item,
                viewGroup,
                false
        );

        return new CardListViewHolder(view);
    }

    /**
     * Sorts the list
     * @param sortingMethod How to sort the list
     */
    public void sortCards(String sortingMethod, final Location location) {
        Log.d(TAG, "sortCards: sorting");
        switch(sortingMethod) {
            case SORT_LOCATION:
                Collections.sort(mCards, new Comparator<Card>() {
                    @Override
                    public int compare(Card c1, Card c2) {
                        Location l1 = c1.getLocation();
                        Location l2 = c2.getLocation();

                        if(l1 == null && l2 == null) { // return 0 = equal
                            return 0;
                        } else if(l1 == null) { // return 1 = first is largest (we want to find the smallest)
                            return 1;
                        } else if(l2 == null) { // return -1 = second is largest
                            return -1;
                        }

                        if(location == null) {
                            Log.d(TAG, "compare: LOCATION IS NULL!");
                            return 0;
                        }

                        Log.d(TAG, "compare: returning" + (int)(c1.getLocation().distanceTo(location) - c2.getLocation().distanceTo(location)));

                        return (int)(c1.getLocation().distanceTo(location) - c2.getLocation().distanceTo(location));
                    }
                });
                break;

            case SORT_NAME:
                Collections.sort(mCards, new Comparator<Card>() {
                    @Override
                    public int compare(Card c1, Card c2) {
                        return c1.getName().toLowerCase().compareTo(c2.getName().toLowerCase());
                    }
                });
                break;

            case SORT_TIME: default:
                Collections.sort(mCards, new Comparator<Card>() {
                    @Override
                    public int compare(Card c1, Card c2) {
                        return c1.getDateCreated().compareTo(c2.getDateCreated());
                    }
                });
                break;
        }
    }

    @Override
    public void onBindViewHolder(CardListViewHolder viewHolder, final int i) {
        final Card c = mCards.get(i);

        viewHolder.tvName.setText(String.format(Locale.getDefault(), "%s (%s)", c.getName(), c.getId()));
        viewHolder.tvCreatedDate.setText(cardDateFormat.format(c.getDateCreated()));

        // Set unknown LOCATION by default
        viewHolder.tvLocation.setText(R.string.location_unknown);

        Location loc = c.getLocation();
        if(loc != null) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

            try {
                List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);

                Address address = addresses.get(0);

                String countryCode = address.getCountryCode();
                String municipality = address.getSubAdminArea();
                String zip = address.getPostalCode();

                viewHolder.tvLocation.setText(String.format(
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

        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEditActivity)
                {
                    Intent intent = new Intent(mContext, EditActivity.class);
                    intent.putExtra("card",i);
                    intent.putExtra("cards",mCards);
                    mContext.startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, EmulateActivity.class);
                    intent.putExtra(CARD, c);
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }


    public static class CardListViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout parentLayout;
        private TextView tvName;
        private TextView tvCreatedDate;
        private TextView tvLocation;

        public CardListViewHolder(@NonNull View itemView) {
            super(itemView);

            parentLayout = itemView.findViewById(R.id.cardListItemParentLayout);
            tvName = itemView.findViewById(R.id.tvCardName);
            tvCreatedDate = itemView.findViewById(R.id.tvCardDateCreated);
            tvLocation = itemView.findViewById(R.id.tvCardLocation);
        }
    }
}

