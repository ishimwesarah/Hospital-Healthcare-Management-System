package com.hospital.model;

import java.time.LocalDate;

public class PatientFeedback {

    private int feedbackId;
    private int patientId;
    private Integer doctorId; // nullable — feedback may be general, not doctor-specific
    private int rating; // 1-5
    private String comments;
    private LocalDate feedbackDate;

    // Display-only, populated by joined queries
    private String patientName;
    private String doctorName;

    public PatientFeedback() {
    }

    // Use for a NEW feedback entry (id not yet known)
    public PatientFeedback(int patientId, Integer doctorId, int rating, String comments, LocalDate feedbackDate) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.rating = rating;
        this.comments = comments;
        this.feedbackDate = feedbackDate;
    }

    // Use when reading FROM the database (id is known)
    public PatientFeedback(int feedbackId, int patientId, Integer doctorId, int rating,
                           String comments, LocalDate feedbackDate) {
        this.feedbackId = feedbackId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.rating = rating;
        this.comments = comments;
        this.feedbackDate = feedbackDate;
    }

    public int getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDate getFeedbackDate() {
        return feedbackDate;
    }

    public void setFeedbackDate(LocalDate feedbackDate) {
        this.feedbackDate = feedbackDate;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    @Override
    public String toString() {
        return "PatientFeedback{" +
                "feedbackId=" + feedbackId +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", rating=" + rating +
                ", comments='" + comments + '\'' +
                ", feedbackDate=" + feedbackDate +
                '}';
    }
}