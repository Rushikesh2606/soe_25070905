package repository;

import model.Appointment;
import model.Doctor;
import model.Patient;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CSVHandler is the data-access layer responsible for all read/write
 * operations against the CSV-based persistence files.
 *
 * It handles:
 *  - Loading patients, doctors, and appointments from CSV
 *  - Saving new appointments (append mode)
 *  - Generating unique appointment IDs
 *  - Graceful handling of missing files and malformed rows
 */
public class CSVHandler {

    // ── File paths (relative to project root) ────────────────

    private static final String PATIENTS_FILE     = "data/patients.csv";
    private static final String DOCTORS_FILE      = "data/doctors.csv";
    private static final String APPOINTMENTS_FILE = "data/appointments.csv";
    private static final String MEDICAL_HISTORY_FILE = "data/medical_history.csv";

    // ══════════════════════════════════════════════════════════
    //  LOAD PATIENTS
    // ══════════════════════════════════════════════════════════

    /**
     * Reads all patients from the patients CSV file.
     *
     * @return list of Patient objects; empty list if file is missing or empty
     */
    public List<Patient> loadPatients() {
        List<Patient> patients = new ArrayList<>();
        File file = new File(PATIENTS_FILE);

        if (!file.exists()) {
            System.out.println("[WARNING] patients.csv not found. No patients loaded.");
            return patients;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {
                // Skip header row
                if (header) {
                    header = false;
                    continue;
                }

                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 3) {
                    System.out.println("[WARNING] Skipping malformed patient row: " + line);
                    continue;
                }

                try {
                    String id   = parts[0].trim();
                    String name = parts[1].trim();
                    int age     = Integer.parseInt(parts[2].trim());
                    patients.add(new Patient(id, name, age));
                } catch (NumberFormatException e) {
                    System.out.println("[WARNING] Invalid age in patient row: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to read patients.csv: " + e.getMessage());
        }

        return patients;
    }

    // ══════════════════════════════════════════════════════════
    //  LOAD DOCTORS
    // ══════════════════════════════════════════════════════════

    /**
     * Reads all doctors from the doctors CSV file.
     *
     * @return list of Doctor objects; empty list if file is missing or empty
     */
    public List<Doctor> loadDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        File file = new File(DOCTORS_FILE);

        if (!file.exists()) {
            System.out.println("[WARNING] doctors.csv not found. No doctors loaded.");
            return doctors;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }

                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 3) {
                    System.out.println("[WARNING] Skipping malformed doctor row: " + line);
                    continue;
                }

                String id             = parts[0].trim();
                String name           = parts[1].trim();
                String specialization = parts[2].trim();
                doctors.add(new Doctor(id, name, specialization));
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to read doctors.csv: " + e.getMessage());
        }

        return doctors;
    }

    // ══════════════════════════════════════════════════════════
    //  LOAD APPOINTMENTS
    // ══════════════════════════════════════════════════════════

    /**
     * Reads all existing appointments from the appointments CSV file.
     *
     * @return list of Appointment objects; empty list if file is missing or empty
     */
    public List<Appointment> loadAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        File file = new File(APPOINTMENTS_FILE);

        if (!file.exists()) {
            // Create the file with header if it doesn't exist
            try {
                file.getParentFile().mkdirs();
                try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                    pw.println("AppointmentID,PatientID,DoctorID,SlotID,Time,MedicalHistory");
                }
            } catch (IOException e) {
                System.out.println("[ERROR] Failed to create appointments.csv: " + e.getMessage());
            }
            return appointments;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }

                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 5) {
                    System.out.println("[WARNING] Skipping malformed appointment row: " + line);
                    continue;
                }

                try {
                    String appointmentId = parts[0].trim();
                    String patientId     = parts[1].trim();
                    String doctorId      = parts[2].trim();
                    int slotId           = Integer.parseInt(parts[3].trim());
                    String time          = parts[4].trim();
                    String medicalHistory = parts.length > 5 ? parts[5].trim() : "";
                    
                    // Remove quotes if present
                    if (medicalHistory.startsWith("\"") && medicalHistory.endsWith("\"") && medicalHistory.length() >= 2) {
                        medicalHistory = medicalHistory.substring(1, medicalHistory.length() - 1);
                    }
                    
                    appointments.add(new Appointment(appointmentId, patientId, doctorId, slotId, time, medicalHistory));
                } catch (NumberFormatException e) {
                    System.out.println("[WARNING] Invalid slot ID in appointment row: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to read appointments.csv: " + e.getMessage());
        }

        return appointments;
    }

    // ══════════════════════════════════════════════════════════
    //  SAVE APPOINTMENT
    // ══════════════════════════════════════════════════════════

    /**
     * Appends a new appointment record to appointments.csv.
     * Creates the file with a header row if it does not exist.
     *
     * @param appointment the Appointment object to persist
     */
    public void saveAppointment(Appointment appointment) {
        File file = new File(APPOINTMENTS_FILE);

        try {
            // Ensure the data directory exists
            file.getParentFile().mkdirs();

            boolean fileExists = file.exists() && file.length() > 0;

            try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
                // Write header if file is new / empty
                if (!fileExists) {
                    pw.println("AppointmentID,PatientID,DoctorID,SlotID,Time,MedicalHistory");
                }
                pw.println(appointment.toCSV());
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to save appointment: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  GET MEDICAL HISTORY
    // ══════════════════════════════════════════════════════════

    /**
     * Reads the medical history for a given patient from medical_history.csv.
     *
     * @param patientId the patient's ID
     * @return a formatted string of the patient's medical history, or empty string if none
     */
    public String getMedicalHistory(String patientId) {
        File file = new File(MEDICAL_HISTORY_FILE);
        if (!file.exists()) {
            return "";
        }

        StringBuilder history = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String pid = parts[0].trim();
                    if (pid.equalsIgnoreCase(patientId)) {
                        String condition = parts[1].trim();
                        String diagnosis = parts[2].trim();
                        if (history.length() > 0) {
                            history.append(" | ");
                        }
                        history.append(condition).append(" (").append(diagnosis).append(")");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to read medical_history.csv: " + e.getMessage());
        }

        return history.toString();
    }

    // ══════════════════════════════════════════════════════════
    //  GENERATE APPOINTMENT ID
    // ══════════════════════════════════════════════════════════

    /**
     * Generates the next appointment ID based on the count of existing
     * appointments. Format: A001, A002, …
     *
     * @param existingAppointments current list of appointments
     * @return a new unique appointment ID string
     */
    public String generateAppointmentId(List<Appointment> existingAppointments) {
        int next = existingAppointments.size() + 1;
        return String.format("A%03d", next);
    }
}
