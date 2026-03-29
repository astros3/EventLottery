package com.example.eventlottery;

/**
 * Admin self-profile fields stored in Firestore at {@code admins/{adminId}}.
 * Document may exist for access control only; profile fields are optional until edited.
 */
public class AdminProfile {

    private String adminId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    public AdminProfile() {
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
