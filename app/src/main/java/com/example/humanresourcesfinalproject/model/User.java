package com.example.humanresourcesfinalproject.model;

public class User {
    String id;
    String fname, lname, phone, email, password,shirtSize,kidId,school,parentName,parentPhone,schoolYear;
    String healthFund,Healthproblems,foodType;
    Boolean isTeacher,isGuide;



    public User() {
        // Empty constructor
    }

    public User(String id, String fname, String lname, String phone, String email, String password,
                String shirtSize, String kidId, String school, String parentName, String parentPhone,
                String schoolYear, String healthFund, String healthProblems, String foodType,
                Boolean isTeacher, Boolean isGuide) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.shirtSize = shirtSize;
        this.kidId = kidId;
        this.school = school;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
        this.schoolYear = schoolYear;
        this.healthFund = healthFund;
        this.Healthproblems = healthProblems;
        this.foodType = foodType;
        this.isTeacher = isTeacher;
        this.isGuide = isGuide;
    }




    public String getFname() {
        return fname;
    }
}
