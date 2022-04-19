package com.example.doctorapointment;

public class TimeSlot {
    String availableSlot,date,patientName,time;

    public TimeSlot() {
    }

    public TimeSlot(String availableSlot, String date, String patientName, String time) {
        this.availableSlot = availableSlot;
        this.date = date;
        this.patientName = patientName;
        this.time = time;
    }

    public String getAvailableSlot() {
        return availableSlot;
    }

    public void setAvailableSlot(String availableSlot) {
        this.availableSlot = availableSlot;
    }

    public String getDate() {
        return date;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
