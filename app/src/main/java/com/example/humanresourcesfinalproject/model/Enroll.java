package com.example.humanresourcesfinalproject.model;

import java.io.Serializable;

public class Enroll  implements Serializable
{

    protected  String userId;
    protected  Course course;

    public Enroll(String userId, Course course) {

        this.userId = userId;
        this.course = course;
    }

    public Enroll() {
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

                ", userId='" + userId + '\'' +
                ", course=" + course +
                '}';
    }
}
