package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;

public class WaitingEntryAdapter extends ArrayAdapter<WaitingListEntry> {

    private final FragmentActivity activity;
    private final ArrayList<WaitingListEntry> entries;

    public WaitingEntryAdapter(FragmentActivity activity, ArrayList<WaitingListEntry> entries) {
        super(activity, 0, entries);
        this.activity = activity;
        this.entries = entries;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(activity)
                    .inflate(R.layout.item_waiting_entry, parent, false);
        }

        WaitingListEntry entry = entries.get(position);

        TextView textEntrantName = view.findViewById(R.id.textEntrantName);
        ImageView buttonLocation = view.findViewById(R.id.buttonLocation);

        // Since WaitingListEntry only stores deviceId
        String entrantName = entry.getDeviceId();
        textEntrantName.setText(entrantName);

        buttonLocation.setOnClickListener(v -> {

            Bundle bundle = new Bundle();
            bundle.putString("entrant_name", entrantName);

            // No location info exists in WaitingListEntry
            bundle.putString("location_address", "");

            NavController navController =
                    Navigation.findNavController(activity, R.id.nav_host_fragment);

            navController.navigate(
                    R.id.Waiting_list_to_GeolocationFragment,
                    bundle
            );
        });

        return view;
    }
}