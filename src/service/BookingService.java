package service;

import model.Appointment;
import model.Doctor;
import model.Patient;
import repository.CSVHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BookingService encapsulates all business logic for the appointment
 * booking workflow.
 *
 * Responsibilities:
 *  - Patient validation (login)
 *  - Doctor retrieval
 *  - Slot availability checking
 *  - Patient history lookup
 *  - Appointment creation with duplicate prevention
 */
public class BookingService {

    // ── Dependencies ─────────────────────────────────────────

    private final CSVHandler csvHandler;

    // ── In-memory caches (loaded once per session) ───────────

    private List<Patient> patients;
    private List<Doctor> doctors;
    private List<Appointment> appointments;

    // ── Predefined slot definitions ──────────────────────────

    /** The 10 fixed appointment slots as specified in the requirements. */
    private static final Map<Integer, String> SLOT_MAP = new LinkedHashMap<>();

    static {
        SLOT_MAP.put(1,  "12:00-12:30");
        SLOT_MAP.put(2,  "12:30-1:00");
        SLOT_MAP.put(3,  "1:00-1:30");
        SLOT_MAP.put(4,  "1:30-2:00");
        SLOT_MAP.put(5,  "2:00-2:30");
        SLOT_MAP.put(6,  "2:30-3:00");
        SLOT_MAP.put(7,  "3:00-3:30");
        SLOT_MAP.put(8,  "3:30-4:00");
        SLOT_MAP.put(9,  "4:00-4:30");
        SLOT_MAP.put(10, "4:30-5:00");
    }

    // ══════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ══════════════════════════════════════════════════════════

    /**
     * Initialises the BookingService by loading all CSV data into memory.
     */
    public BookingService() {
        this.csvHandler   = new CSVHandler();
        this.patients     = csvHandler.loadPatients();
        this.doctors      = csvHandler.loadDoctors();
        this.appointments = csvHandler.loadAppointments();
    }

    // ══════════════════════════════════════════════════════════
    //  1. PATIENT LOGIN / VALIDATION
    // ══════════════════════════════════════════════════════════

    /**
     * Validates whether a patient with the given ID exists in the system.
     *
     * @param patientId the ID to look up (e.g., "P001")
     * @return the matching Patient, or null if not found
     */
    public Patient validatePatient(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            return null;
        }
        for (Patient p : patients) {
            if (p.getPatientId().equalsIgnoreCase(patientId.trim())) {
                return p;
            }
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════
    //  2. RETRIEVE DOCTOR
    // ══════════════════════════════════════════════════════════

    /**
     * Retrieves a doctor by their unique ID.
     *
     * @param doctorId the ID to look up (e.g., "D001")
     * @return the matching Doctor, or null if not found
     */
    public Doctor retrieveDoctor(String doctorId) {
        if (doctorId == null || doctorId.trim().isEmpty()) {
            return null;
        }
        for (Doctor d : doctors) {
            if (d.getDoctorId().equalsIgnoreCase(doctorId.trim())) {
                return d;
            }
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════
    //  3. DISPLAY SLOTS
    // ══════════════════════════════════════════════════════════

    /**
     * Returns the complete map of predefined slots (1–10).
     *
     * @return ordered map of slot number → time range
     */
    public Map<Integer, String> getAllSlots() {
        return SLOT_MAP;
    }

    /**
     * Checks whether a given slot number is within the valid range (1–10).
     *
     * @param slotId the slot number entered by the patient
     * @return true if valid, false otherwise
     */
    public boolean isValidSlot(int slotId) {
        return SLOT_MAP.containsKey(slotId);
    }

    // ══════════════════════════════════════════════════════════
    //  4. CHECK TIME (SLOT AVAILABILITY)
    // ══════════════════════════════════════════════════════════

    /**
     * Verifies whether a specific slot is still available for a given doctor.
     * A slot is unavailable if any existing appointment already occupies it
     * for the same doctor.
     *
     * @param doctorId the doctor's ID
     * @param slotId   the slot number (1–10)
     * @return true if the slot is available, false if already booked
     */
    public boolean checkTime(String doctorId, int slotId) {
        for (Appointment a : appointments) {
            if (a.getDoctorId().equalsIgnoreCase(doctorId) && a.getSlotId() == slotId) {
                return false; // Slot is already taken
            }
        }
        return true; // Slot is available
    }

    /**
     * Returns a list of slot IDs that are already booked for a given doctor.
     *
     * @param doctorId the doctor's ID
     * @return list of booked slot numbers
     */
    public List<Integer> getBookedSlots(String doctorId) {
        List<Integer> booked = new ArrayList<>();
        for (Appointment a : appointments) {
            if (a.getDoctorId().equalsIgnoreCase(doctorId)) {
                booked.add(a.getSlotId());
            }
        }
        return booked;
    }

    /**
     * Checks whether ALL 10 slots for a given doctor are booked.
     *
     * @param doctorId the doctor's ID
     * @return true if all 10 slots are occupied
     */
    public boolean areAllSlotsBooked(String doctorId) {
        return getBookedSlots(doctorId).size() >= SLOT_MAP.size();
    }

    // ══════════════════════════════════════════════════════════
    //  5. GET PATIENT HISTORY
    // ══════════════════════════════════════════════════════════

    /**
     * Retrieves all past/current appointments for a specific patient.
     *
     * @param patientId the patient's ID
     * @return list of Appointment objects belonging to the patient
     */
    public List<Appointment> getPatientHistory(String patientId) {
        List<Appointment> history = new ArrayList<>();
        for (Appointment a : appointments) {
            if (a.getPatientId().equalsIgnoreCase(patientId)) {
                history.add(a);
            }
        }
        return history;
    }

    // ══════════════════════════════════════════════════════════
    //  6. ADD APPOINTMENT
    // ══════════════════════════════════════════════════════════

    /**
     * Creates and persists a new appointment.
     *
     * Pre-conditions (must be validated by caller):
     *  - Patient and Doctor exist
     *  - Slot is available for this doctor
     *
     * @param patient the authenticated patient
     * @param doctor  the selected doctor
     * @param slotId  the chosen slot number (1–10)
     * @return the newly created Appointment object
     */
    public Appointment addAppointment(Patient patient, Doctor doctor, int slotId) {
        String appointmentId = csvHandler.generateAppointmentId(appointments);
        String time          = SLOT_MAP.get(slotId);
        String history       = csvHandler.getMedicalHistory(patient.getPatientId());

        Appointment appointment = new Appointment(
                appointmentId,
                patient.getPatientId(),
                doctor.getDoctorId(),
                slotId,
                time,
                history
        );

        // Persist to CSV
        csvHandler.saveAppointment(appointment);

        // Update in-memory list so subsequent checks are accurate
        appointments.add(appointment);

        return appointment;
    }

    // ══════════════════════════════════════════════════════════
    //  7. APPOINTMENT SET (CONFIRMATION DISPLAY)
    // ══════════════════════════════════════════════════════════

    /**
     * Produces a formatted confirmation message for a booked appointment.
     *
     * @param appointment the booked appointment
     * @param patient     the patient who booked
     * @param doctor      the doctor for the appointment
     * @return multi-line confirmation string
     */
    public String appointmentSet(Appointment appointment, Patient patient, Doctor doctor) {
        String historyInfo = "";
        String medHistory = appointment.getMedicalHistory();
        if (medHistory != null && !medHistory.isEmpty()) {
            historyInfo = "║  Medical Hist.  : " + padRight(medHistory, 26) + "║\n";
        }
        
        return "\n╔══════════════════════════════════════════════╗\n" +
               "║       ✅  APPOINTMENT CONFIRMED!             ║\n" +
               "╠══════════════════════════════════════════════╣\n" +
               "║  Appointment ID : " + padRight(appointment.getAppointmentId(), 26) + "║\n" +
               "║  Patient        : " + padRight(patient.getName() + " (" + patient.getPatientId() + ")", 26) + "║\n" +
               "║  Doctor         : " + padRight(doctor.getName(), 26) + "║\n" +
               "║  Specialization : " + padRight(doctor.getSpecialization(), 26) + "║\n" +
               "║  Time Slot      : " + padRight(appointment.getTime(), 26) + "║\n" +
               historyInfo +
               "╚══════════════════════════════════════════════╝\n";
    }

    // ── Helper ───────────────────────────────────────────────

    /**
     * Right-pads a string with spaces to the specified length.
     */
    private String padRight(String text, int length) {
        if (text.length() >= length) return text;
        return text + " ".repeat(length - text.length());
    }

    // ══════════════════════════════════════════════════════════
    //  UTILITY — LIST ALL DOCTORS (for display)
    // ══════════════════════════════════════════════════════════

    /**
     * Returns the full list of doctors loaded from the CSV.
     *
     * @return list of all Doctor objects
     */
    public List<Doctor> getAllDoctors() {
        return doctors;
    }
}
