package com.example.humanresourcesfinalproject.model;

public class Enroll
{
    protected  String id;
    protected  String userId;
    protected  Course course;

    public Enroll(String id, String userId, Course course) {
        this.id = id;
        this.userId = userId;
        this.course = course;
    }

    public Enroll() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @Override
    public String toString() {
        return "Enroll{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", course=" + course +
                '}';
    }
}
