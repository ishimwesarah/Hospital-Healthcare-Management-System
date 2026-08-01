package com.hospital.model;

public class PrescriptionItem {

    private int prescriptionItemId;
    private int prescriptionId; // foreign key to prescriptions.prescription_id
    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;
    private int quantity;

    public PrescriptionItem() {
    }

    // Use for a NEW item (id not yet known); prescriptionId set separately once the parent exists
    public PrescriptionItem(String medicationName, String dosage, String frequency, String duration, int quantity) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.quantity = quantity;
    }

    // Use when reading FROM the database (all ids known)
    public PrescriptionItem(int prescriptionItemId, int prescriptionId, String medicationName,
                            String dosage, String frequency, String duration, int quantity) {
        this.prescriptionItemId = prescriptionItemId;
        this.prescriptionId = prescriptionId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.quantity = quantity;
    }

    public int getPrescriptionItemId() {
        return prescriptionItemId;
    }

    public void setPrescriptionItemId(int prescriptionItemId) {
        this.prescriptionItemId = prescriptionItemId;
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "PrescriptionItem{" +
                "medicationName='" + medicationName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", duration='" + duration + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}