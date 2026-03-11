package com.example.eventlottery;

import com.google.firebase.Timestamp;

public class WaitingListEntry {

    public enum Status {
        WAITING,
        PENDING,
        SELECTED,
        ACCEPTED,
        DECLINED,
        CANCELLED
    }

    private String deviceId;
    private String status;
    private Timestamp joinTimestamp;

    public WaitingListEntry() {
        // Required empty constructor for Firestore
    }

    public WaitingListEntry(String deviceId, Status status) {
        this.deviceId = deviceId;
        this.status = status.name();
        this.joinTimestamp = Timestamp.now();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getJoinTimestamp() {
        return joinTimestamp;
    }

    public void setJoinTimestamp(Timestamp joinTimestamp) {
        this.joinTimestamp = joinTimestamp;
    }
}