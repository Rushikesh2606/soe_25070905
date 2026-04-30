package model;

/**
 * Represents an Appointment entity linking a Patient, a Doctor, and a Time Slot.
 * Each appointment has a unique auto-generated ID.
 */
public class Appointment {

    private final String appointmentId;
    private final String patientId;
    private final String doctorId;
    private final int slotId;
    private final String time;
    private final String medicalHistory;

    /**
     * Constructs an Appointment with the given details.
     *
     * @param appointmentId unique identifier for the appointment (e.g., A001)
     * @param patientId     ID of the patient who booked
     * @param doctorId      ID of the doctor being booked
     * @param slotId        slot number (1–10) selected by the patient
     * @param time          human-readable time range (e.g., "12:00-12:30")
     * @param medicalHistory patient's medical history (can be empty)
     */
    public Appointment(String appointmentId, String patientId, String doctorId, int slotId, String time, String medicalHistory) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.slotId = slotId;
        this.time = time;
        this.medicalHistory = medicalHistory;
    }

    // ── Getters ──────────────────────────────────────────────

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public int getSlotId() {
        return slotId;
    }

    public String getTime() {
        return time;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    // ── CSV Serialization ────────────────────────────────────

    /**
     * Converts this appointment to a CSV row string.
     *
     * @return comma-separated values ready for file write
     */
    public String toCSV() {
        String historyPart = (medicalHistory != null && !medicalHistory.isEmpty()) ? ",\"" + medicalHistory + "\"" : ",";
        return appointmentId + "," + patientId + "," + doctorId + "," + slotId + "," + time + historyPart;
    }

    // ── Display ──────────────────────────────────────────────

    @Override
    public String toString() {
        return "Appointment{" +
                "ID='" + appointmentId + '\'' +
                ", PatientID='" + patientId + '\'' +
                ", DoctorID='" + doctorId + '\'' +
                ", Slot=" + slotId +
                ", Time='" + time + '\'' +
                ", MedicalHistory='" + medicalHistory + '\'' +
                '}';
    }
}
