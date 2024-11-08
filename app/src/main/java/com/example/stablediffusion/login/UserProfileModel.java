package com.example.stablediffusion.login;

public class UserProfileModel {
    public String displayName;
    public String phoneNumber;
    public String address;

    public UserProfileModel() {
        // Default constructor required for calls to DataSnapshot.getValue(UserProfile.class)
    }

    public UserProfileModel(String displayName, String phoneNumber, String address) {
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}
