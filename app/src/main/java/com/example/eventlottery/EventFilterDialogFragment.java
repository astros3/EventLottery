package com.example.eventlottery;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * US 01.01.04–01.01.06: Search keyword + filters for the entrant dashboard.
 */
public class EventFilterDialogFragment extends DialogFragment {

    public static final String TAG = "EventFilterDialogFragment";
    private static final String ARG_CRITERIA = "criteria";

    public interface Listener {
        void onFilterApplied(EventFilterCriteria criteria);

        void onFilterCleared();
    }

    private TextInputEditText inputKeyword;
    private TextInputEditText inputMinCapacity;
    private Button buttonDateFrom;
    private Button buttonDateTo;
    private SwitchMaterial switchRegistrationOpen;
    private SwitchMaterial switchHideFullWaiting;

    private Long pendingFromMillis;
    private Long pendingToMillis;

    public static EventFilterDialogFragment newInstance(EventFilterCriteria criteria) {
        EventFilterDialogFragment f = new EventFilterDialogFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_CRITERIA, criteria != null ? criteria : EventFilterCriteria.empty());
        f.setArguments(b);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_event_filter, null);

        inputKeyword = view.findViewById(R.id.input_keyword);
        inputMinCapacity = view.findViewById(R.id.input_min_capacity);
        buttonDateFrom = view.findViewById(R.id.button_date_from);
        buttonDateTo = view.findViewById(R.id.button_date_to);
        switchRegistrationOpen = view.findViewById(R.id.switch_registration_open);
        switchHideFullWaiting = view.findViewById(R.id.switch_hide_full_waiting);

        EventFilterCriteria c = readCriteriaFromArgs();
        inputKeyword.setText(c.getKeyword());
        switchRegistrationOpen.setChecked(c.isRegistrationOpenOnly());
        switchHideFullWaiting.setChecked(c.isHideFullWaitingList());
        pendingFromMillis = c.getEventDateFromMillis();
        pendingToMillis = c.getEventDateToMillis();
        updateDateButtonLabels();

        Integer minCap = c.getMinCapacity();
        if (minCap != null && minCap > 0) {
            inputMinCapacity.setText(String.valueOf(minCap));
        }

        buttonDateFrom.setOnClickListener(v -> showDatePicker(true));
        buttonDateTo.setOnClickListener(v -> showDatePicker(false));

        view.findViewById(R.id.button_clear_filter).setOnClickListener(v -> {
            if (getListener() != null) {
                getListener().onFilterCleared();
            }
            dismiss();
        });

        view.findViewById(R.id.button_apply_filter).setOnClickListener(v -> applyAndClose());

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .create();
    }

    @Nullable
    private Listener getListener() {
        if (getActivity() instanceof Listener) {
            return (Listener) getActivity();
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    private EventFilterCriteria readCriteriaFromArgs() {
        Bundle args = getArguments();
        if (args == null) return EventFilterCriteria.empty();
        Object o = args.getSerializable(ARG_CRITERIA);
        if (o instanceof EventFilterCriteria) {
            return (EventFilterCriteria) o;
        }
        return EventFilterCriteria.empty();
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = Calendar.getInstance();
        long initial = isFrom
                ? (pendingFromMillis != null ? pendingFromMillis : cal.getTimeInMillis())
                : (pendingToMillis != null ? pendingToMillis : cal.getTimeInMillis());
        cal.setTimeInMillis(initial);

        DatePickerDialog dlg = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, dayOfMonth);
                    if (isFrom) {
                        pendingFromMillis = startOfDay(picked.getTimeInMillis());
                    } else {
                        pendingToMillis = endOfDay(picked.getTimeInMillis());
                    }
                    updateDateButtonLabels();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dlg.show();
    }

    private void updateDateButtonLabels() {
        DateFormat fmt = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        buttonDateFrom.setText(pendingFromMillis != null
                ? fmt.format(pendingFromMillis)
                : getString(R.string.filter_date_any));
        buttonDateTo.setText(pendingToMillis != null
                ? fmt.format(pendingToMillis)
                : getString(R.string.filter_date_any));
    }

    private static long startOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private static long endOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    private void applyAndClose() {
        EventFilterCriteria c = EventFilterCriteria.empty();
        c.setKeyword(inputKeyword.getText() != null ? inputKeyword.getText().toString() : "");
        c.setEventDateFromMillis(pendingFromMillis);
        c.setEventDateToMillis(pendingToMillis);
        c.setRegistrationOpenOnly(switchRegistrationOpen.isChecked());
        c.setHideFullWaitingList(switchHideFullWaiting.isChecked());

        String capStr = inputMinCapacity.getText() != null ? inputMinCapacity.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(capStr)) {
            try {
                int v = Integer.parseInt(capStr);
                if (v > 0) {
                    c.setMinCapacity(v);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), R.string.filter_invalid_capacity, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (pendingFromMillis != null && pendingToMillis != null && pendingFromMillis > pendingToMillis) {
            Toast.makeText(requireContext(), R.string.filter_invalid_date_range, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getListener() != null) {
            getListener().onFilterApplied(c);
        }
        dismiss();
    }
}
