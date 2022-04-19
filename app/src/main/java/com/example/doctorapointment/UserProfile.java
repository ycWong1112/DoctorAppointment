package com.example.doctorapointment;


public class UserProfile {
    public String userName;
    public String userAddress;
    public String userEmail;

    public UserProfile() {
    }

    public UserProfile(String name, String email, String address) {
        this.userName = userName;
        this.userAddress = userAddress;
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /*public String getAccountType() { return accountType; }

    public void setAccountType(String accountType) { this.accountType = accountType; }*/
}


