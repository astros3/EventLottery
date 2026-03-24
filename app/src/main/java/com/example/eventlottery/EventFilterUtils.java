package com.example.eventlottery;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client-side filtering for entrant event list (US 01.01.04–01.01.06).
 */
public final class EventFilterUtils {

    private EventFilterUtils() {}

    public interface FilterResultCallback {
        void onResult(@NonNull List<Event> filtered);
    }

    /**
     * Applies keyword + sync filters; optionally excludes events whose waiting list is full
     * (requires one Firestore read per event when {@link EventFilterCriteria#isHideFullWaitingList()}).
     */
    public static void apply(
            FirebaseFirestore db,
            List<Event> allEvents,
            EventFilterCriteria c,
            FilterResultCallback callback) {
        if (allEvents == null || allEvents.isEmpty()) {
            callback.onResult(new ArrayList<>());
            return;
        }
        List<Event> step1 = new ArrayList<>();
        for (Event e : allEvents) {
            if (matchesSync(e, c)) {
                step1.add(e);
            }
        }
        if (!c.isHideFullWaitingList()) {
            callback.onResult(step1);
            return;
        }
        if (step1.isEmpty()) {
            callback.onResult(step1);
            return;
        }

        List<Event> out = new ArrayList<>();
        AtomicInteger pending = new AtomicInteger(step1.size());
        Runnable finishIfDone = () -> {
            if (pending.decrementAndGet() == 0) {
                callback.onResult(out);
            }
        };

        for (Event e : step1) {
            int limit = e.getWaitingListLimit();
            if (limit <= 0) {
                synchronized (out) {
                    out.add(e);
                }
                finishIfDone.run();
                continue;
            }
            db.collection("events")
                    .document(e.getEventId())
                    .collection("waitingList")
                    .get()
                    .addOnSuccessListener(q -> {
                        if (q != null && q.size() < limit) {
                            synchronized (out) {
                                out.add(e);
                            }
                        }
                        finishIfDone.run();
                    })
                    .addOnFailureListener(err -> finishIfDone.run());
        }
    }

    /** Synchronous match (no waiting-list size check). */
    public static boolean matchesSync(Event e, EventFilterCriteria c) {
        if (e == null || c == null) return false;
        if (!matchesKeyword(e, c.getKeyword())) return false;
        if (!matchesDateRange(e, c)) return false;
        if (c.isRegistrationOpenOnly() && !e.isRegistrationOpen()) return false;
        if (!matchesMinCapacity(e, c.getMinCapacity())) return false;
        return true;
    }

    private static boolean matchesKeyword(Event e, String keyword) {
        if (keyword == null || keyword.isEmpty()) return true;
        String k = keyword.toLowerCase(Locale.ROOT);
        String title = e.getTitle() != null ? e.getTitle().toLowerCase(Locale.ROOT) : "";
        String desc = e.getDescription() != null ? e.getDescription().toLowerCase(Locale.ROOT) : "";
        String loc = e.getLocation() != null ? e.getLocation().toLowerCase(Locale.ROOT) : "";
        return title.contains(k) || desc.contains(k) || loc.contains(k);
    }

    private static boolean matchesDateRange(Event e, EventFilterCriteria c) {
        Long from = c.getEventDateFromMillis();
        Long to = c.getEventDateToMillis();
        if (from == null && to == null) return true;
        long t = e.getEventDateMillis();
        if (from != null && t < from) return false;
        if (to != null && t > to) return false;
        return true;
    }

    private static boolean matchesMinCapacity(Event e, Integer min) {
        if (min == null || min <= 0) return true;
        int cap = e.getCapacity();
        if (cap == 0) return true;
        return cap >= min;
    }
}
