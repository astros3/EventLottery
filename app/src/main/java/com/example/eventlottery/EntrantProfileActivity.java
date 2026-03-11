package com.example.eventlottery;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantProfileActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput;
    private TextView displayName, displayEmail;
    private FirebaseFirestore db;
    private String deviceId;

    private Double currentLatitude;
    private Double currentLongitude;
    private String currentLocationAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(this);

        nameInput = findViewById(R.id.edit_profile_name);
        emailInput = findViewById(R.id.edit_profile_email);
        phoneInput = findViewById(R.id.edit_profile_phone);

        displayName = findViewById(R.id.profile_display_name);
        displayEmail = findViewById(R.id.profile_display_email);

        loadProfile();

        findViewById(R.id.btn_save_changes).setOnClickListener(v -> updateProfile());
    }

    private void loadProfile() {
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Entrant existing = documentSnapshot.toObject(Entrant.class);
                        if (existing != null) {
                            String fullName = existing.getFullName();
                            nameInput.setText(fullName);
                            emailInput.setText(existing.getEmail());
                            phoneInput.setText(existing.getPhone());

                            displayName.setText(fullName);
                            displayEmail.setText(existing.getEmail());

                            currentLatitude = existing.getLatitude();
                            currentLongitude = existing.getLongitude();
                            currentLocationAddress = existing.getLocationAddress();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to load profile", e);  // full stack trace
                    e.printStackTrace(); // prints stack trace in Logcat

                    Toast.makeText(this,
                            "Failed to load profile: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void updateProfile() {
        String fullName = nameInput.getText().toString().trim();

        Entrant entrant = new Entrant(
                deviceId,
                fullName,
                emailInput.getText().toString().trim(),
                phoneInput.getText().toString().trim(),
                "entrant"
        );

        entrant.setLatitude(currentLatitude);
        entrant.setLongitude(currentLongitude);
        entrant.setLocationAddress(currentLocationAddress);

        db.collection("users").document(deviceId)
                .set(entrant)
                .addOnSuccessListener(aVoid -> {
                    displayName.setText(entrant.getFullName());
                    displayEmail.setText(entrant.getEmail());
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
