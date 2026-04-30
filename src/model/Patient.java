package model;

/**
 * Represents a Patient entity in the Doctor Appointment Booking System.
 * Each patient has a unique ID, a name, and an age.
 */
public class Patient {

    private final String patientId;
    private final String name;
    private final int age;

    /**
     * Constructs a Patient with the given details.
     *
     * @param patientId unique identifier for the patient (e.g., P001)
     * @param name      full name of the patient
     * @param age       age of the patient
     */
    public Patient(String patientId, String name, int age) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
    }

    // ── Getters ──────────────────────────────────────────────

    public String getPatientId() {
        return patientId;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    // ── Display ──────────────────────────────────────────────

    @Override
    public String toString() {
        return "Patient{" +
                "ID='" + patientId + '\'' +
                ", Name='" + name + '\'' +
                ", Age=" + age +
                '}';
    }
}
