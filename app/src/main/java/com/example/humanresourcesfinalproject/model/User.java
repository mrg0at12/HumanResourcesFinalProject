package com.example.humanresourcesfinalproject.model;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String id;
    private String fname, lname, phone, email, password, shirtSize, kidId, school, parentName, parentPhone, schoolYear;
    private String healthFund, healthProblems, foodType;
    private Boolean isTeacher, isGuide;
    private ArrayList<Course> enrolledCourses;

    public User() {
        // Empty constructor
        this.enrolledCourses = new ArrayList<>();
    }

    public User(String id, String fname, String lname, String phone, String email, String password, String shirtSize, String kidId, String school, String parentName, String parentPhone, String schoolYear, String healthFund, String healthProblems, String foodType, Boolean isTeacher, Boolean isGuide, ArrayList<Course> enrolledCourses) {
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
        this.healthProblems = healthProblems;
        this.foodType = foodType;
        this.isTeacher = isTeacher;
        this.isGuide = isGuide;
        this.enrolledCourses = enrolledCourses;
    }


    public User(String id, String fname, String lname, String phone, String email, String password, String shirtSize, String kidId, String school, String parentName, String parentPhone, String schoolYear, String healthFund, String healthProblems, String foodType, Boolean isTeacher, Boolean isGuide) {
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
        this.healthProblems = healthProblems;
        this.foodType = foodType;
        this.isTeacher = isTeacher;
        this.isGuide = isGuide;

    }

    public User(User user){
    this.id = user.id;
        this.fname = user.fname;
        this.lname = user.lname;
        this.phone = user.phone;
        this.email = user.email;

        this.shirtSize = user.shirtSize;
        this.kidId = user.kidId;
        this.school = user.school;
        this.parentName = user.parentName;
        this.parentPhone = user.parentPhone;
        this.schoolYear = user.schoolYear;
        this.healthFund = user.healthFund;
        this.healthProblems = user.healthProblems;
        this.foodType = user.foodType;
        this.isTeacher = user.isTeacher;
        this.isGuide = user.isGuide;

    }

    public User(String id, String fname, String lname, String phone, String email, String shirtSize, String kidId, String school, String parentName, String parentPhone, String schoolYear, String healthFund, String healthProblems, String foodType, Boolean isTeacher, Boolean isGuide) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.phone = phone;
        this.email = email;
        this.shirtSize = shirtSize;
        this.kidId = kidId;
        this.school = school;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
        this.schoolYear = schoolYear;
        this.healthFund = healthFund;
        this.healthProblems = healthProblems;
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
        return healthProblems;
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

    public ArrayList<Course> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void enrollInCourse(Course course) {
        enrolledCourses.add(course);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setShirtSize(String shirtSize) {
        this.shirtSize = shirtSize;
    }

    public void setKidId(String kidId) {
        this.kidId = kidId;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public void setSchoolYear(String schoolYear) {
        this.schoolYear = schoolYear;
    }

    public void setHealthFund(String healthFund) {
        this.healthFund = healthFund;
    }

    public void setHealthProblems(String healthProblems) {
        this.healthProblems = healthProblems;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public Boolean getTeacher() {
        return isTeacher;
    }

    public void setTeacher(Boolean teacher) {
        isTeacher = teacher;
    }

    public Boolean getGuide() {
        return isGuide;
    }

    public void setGuide(Boolean guide) {
        isGuide = guide;
    }

    public void setEnrolledCourses(ArrayList<Course> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", shirtSize='" + shirtSize + '\'' +
                ", kidId='" + kidId + '\'' +
                ", school='" + school + '\'' +
                ", parentName='" + parentName + '\'' +
                ", parentPhone='" + parentPhone + '\'' +
                ", schoolYear='" + schoolYear + '\'' +
                ", healthFund='" + healthFund + '\'' +
                ", healthProblems='" + healthProblems + '\'' +
                ", foodType='" + foodType + '\'' +
                ", isTeacher=" + isTeacher +
                ", isGuide=" + isGuide +
                ", enrolledCourses=" + enrolledCourses +
                '}';
    }
}
