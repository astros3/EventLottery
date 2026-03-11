package com.example.eventlottery;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import android.widget.ImageView;

public class SelectedEntryAdapter extends ArrayAdapter<WaitingListEntry> {

    public interface CancelListener {
        void onCancel(WaitingListEntry entry);
    }

    private CancelListener cancelListener;

    public SelectedEntryAdapter(Activity context,
                                ArrayList<WaitingListEntry> entries,
                                CancelListener listener) {
        super(context, 0, entries);
        this.cancelListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_selected_entry, parent, false);
        }

        WaitingListEntry entry = getItem(position);

        TextView nameText = convertView.findViewById(R.id.textEntrantName);
        ImageView cancelButton = convertView.findViewById(R.id.buttonDelete);

        if (entry != null) {

            nameText.setText(entry.getDeviceId());

            cancelButton.setOnClickListener(v -> {
                if (cancelListener != null) {
                    cancelListener.onCancel(entry);
                }
            });
        }

        return convertView;
    }
}