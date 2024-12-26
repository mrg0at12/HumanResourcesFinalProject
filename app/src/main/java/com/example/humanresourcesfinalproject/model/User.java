package com.example.humanresourcesfinalproject.model;

public class User {
    String id;
    String fname, lname, phone, email, password;

    public User() {
        // Empty constructor
    }

    public User(String id, String fname, String lname, String phone, String email, String password) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.phone = phone;
        this.email = email;
        this.password = password;
    }

    public User(String id, String fname, String lname, String phone, String email) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.phone = phone;
        this.email = email;
    }

    public String getFname() {
        return fname;
    }
}
