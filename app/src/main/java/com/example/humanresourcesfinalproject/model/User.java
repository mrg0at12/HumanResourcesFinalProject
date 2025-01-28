package com.example.humanresourcesfinalproject.model;

public class User {
    String id;
    String fname, lname, phone, email, password,shirtSize,kidId,school,parentName,parentPhone,schoolYear;
    String healthFund,Healthproblems,foodType;
    Boolean isTeacher,isGuide;
    //String courses;



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




    public String getId() {
        return id;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getShirtSize() {
        return shirtSize;
    }

    public String getKidId() {
        return kidId;
    }

    public String getSchool() {
        return school;
    }

    public String getParentName() {
        return parentName;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public String getSchoolYear() {
        return schoolYear;
    }

    public String getHealthFund() {
        return healthFund;
    }

    public String getHealthProblems() {
        return Healthproblems;
    }

    public String getFoodType() {
        return foodType;
    }

    public Boolean getIsTeacher() {
        return isTeacher;
    }

    public Boolean getIsGuide() {
        return isGuide;
    }
}
