package com.example.humanresourcesfinalproject.model;

public class Admin extends User {

    private Boolean isAdmin;

    // Default constructor
    public Admin() {
        super();
    }

    // Constructor for admin registration
    public Admin(String id, String fname, String lname, String phone, String email, String password,
                 String shirtSize, String kidId, String school, String parentName, String parentPhone,
                 String schoolYear, String healthFund, String healthProblems, String foodType,
                 Boolean isTeacher, Boolean isGuide, Boolean isAdmin) {
        super(id, fname, lname, phone, email, password, shirtSize, kidId, school, parentName, parentPhone,
                schoolYear, healthFund, healthProblems, foodType, isTeacher, isGuide);
        this.isAdmin = isAdmin;
    }

    // Getter and Setter for isAdmin
    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
