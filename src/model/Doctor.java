package model;

/**
 * Represents a Doctor entity in the Doctor Appointment Booking System.
 * Each doctor has a unique ID, a name, and a specialization.
 */
public class Doctor {

    private final String doctorId;
    private final String name;
    private final String specialization;

    /**
     * Constructs a Doctor with the given details.
     *
     * @param doctorId       unique identifier for the doctor (e.g., D001)
     * @param name           full name of the doctor
     * @param specialization medical specialization of the doctor
     */
    public Doctor(String doctorId, String name, String specialization) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
    }

    // ── Getters ──────────────────────────────────────────────

    public String getDoctorId() {
        return doctorId;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    // ── Display ──────────────────────────────────────────────

    @Override
    public String toString() {
        return "Doctor{" +
                "ID='" + doctorId + '\'' +
                ", Name='" + name + '\'' +
                ", Specialization='" + specialization + '\'' +
                '}';
    }
}
