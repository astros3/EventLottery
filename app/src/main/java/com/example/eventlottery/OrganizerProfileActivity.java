package com.example.eventlottery;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Edit organizer profile (firstName, lastName, email, phone) in organizers collection.
 * Includes notification preference toggle.
 */
public class OrganizerProfileActivity extends AppCompatActivity {

    private static final String COLLECTION_ORGANIZERS = "organizers";

    private FirebaseFirestore db;
    private String deviceId;

    private TextInputEditText editFirstName;
    private TextInputEditText editLastName;
    private TextInputEditText editEmail;
    private TextInputEditText editPhone;

    private MaterialSwitch switchNotifications;

    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_profile);

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(this);

        bindViews();
        setupToolbar();
        setupSaveButton();

        loadProfile();
    }

    private void bindViews() {
        editFirstName = findViewById(R.id.edit_organizer_first_name);
        editLastName = findViewById(R.id.edit_organizer_last_name);
        editEmail = findViewById(R.id.edit_organizer_email);
        editPhone = findViewById(R.id.edit_organizer_phone);

        switchNotifications = findViewById(R.id.switch_notifications);

        btnSave = findViewById(R.id.btn_save_organizer);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_organizer_profile);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> saveProfile());
    }

    /**
     * Load organizer profile from Firestore
     */
    private void loadProfile() {
        db.collection(COLLECTION_ORGANIZERS).document(deviceId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Organizer organizer = doc.toObject(Organizer.class);

                        if (organizer != null) {

                            editFirstName.setText(organizer.getFirstName());
                            editLastName.setText(organizer.getLastName());
                            editEmail.setText(organizer.getEmail());
                            editPhone.setText(organizer.getPhoneNumber());

                            switchNotifications.setChecked(
                                    organizer.isNotificationsEnabled()
                            );
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load profile",
                                Toast.LENGTH_SHORT).show());
    }

    /**
     * Save organizer profile to Firestore
     */
    private void saveProfile() {

        String firstName = editFirstName.getText() != null ?
                editFirstName.getText().toString().trim() : "";

        String lastName = editLastName.getText() != null ?
                editLastName.getText().toString().trim() : "";

        String email = editEmail.getText() != null ?
                editEmail.getText().toString().trim() : "";

        String phone = editPhone.getText() != null ?
                editPhone.getText().toString().trim() : "";

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this,
                    "First and last name are required",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Organizer organizer = new Organizer();

        organizer.setOrganizerId(deviceId);
        organizer.setFirstName(firstName);
        organizer.setLastName(lastName);
        organizer.setEmail(email);
        organizer.setPhoneNumber(phone);

        organizer.setNotificationsEnabled(
                switchNotifications.isChecked()
        );

        db.collection(COLLECTION_ORGANIZERS)
                .document(deviceId)
                .set(organizer)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            R.string.organizer_profile_saved,
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to save profile",
                                Toast.LENGTH_SHORT).show());
    }
}