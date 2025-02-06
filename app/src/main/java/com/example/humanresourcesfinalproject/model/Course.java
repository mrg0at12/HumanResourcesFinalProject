package com.example.humanresourcesfinalproject.model;
import java.time.LocalDate;
import java.util.Date;

public class Course {
    private String id;
    private String courseId;
    private String courseName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double pricePupil;
    private Double priceTeach;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getPricePupil() {
        return pricePupil;
    }

    public void setPricePupil(Double pricePupil) {
        this.pricePupil = pricePupil;
    }

    public Double getPriceTeach() {
        return priceTeach;
    }

    public void setPriceTeach(Double priceTeach) {
        this.priceTeach = priceTeach;
    }

}
