package com.example.humanresourcesfinalproject.model;
import java.time.LocalDate;
import java.util.Date;

public class Course {
    private String id;
    private String courseId;
    private String courseName;
    private Date startDate;
    private Date endDate;
    private double pricePupil;
    private double priceTeach;

    public Course() {
    }

    // Constructor
    public Course(String id, String courseId, String courseName, Date startDate, Date endDate, double pricePupil, double priceTeach) {
        this.id = id;
        this.courseId = courseId;
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pricePupil = pricePupil;
        this.priceTeach = priceTeach;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public double getPricePupil() { return pricePupil; }
    public void setPricePupil(double pricePupil) { this.pricePupil = pricePupil; }

    public double getPriceTeach() { return priceTeach; }
    public void setPriceTeach(double priceTeach) { this.priceTeach = priceTeach; }

}
