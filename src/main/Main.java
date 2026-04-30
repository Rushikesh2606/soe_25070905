package main;

import model.Appointment;
import model.Doctor;
import model.Patient;
import service.BookingService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Main entry point for the Doctor Appointment Booking System.
 *
 * This class handles ALL user interaction through the CLI.
 * It follows the strict flow defined in the requirements:
 *
 *   1. Patient Login
 *   2. Ask for Doctor ID → retrieveDoctor()
 *   3. Display 10 predefined slots
 *   4. Patient selects slot
 *   5. checkTime() → verify availability
 *   6. getPatientHistory()
 *   7. addAppointment()
 *   8. appointmentSet() → show confirmation
 *
 * Input validation is enforced at every step — the system
 * NEVER crashes and NEVER accepts invalid input.
 */
public class Main {

    // ── Constants ─────────────────────────────────────────────

    private static final String SEPARATOR = "══════════════════════════════════════════════";
    private static final String THIN_SEP  = "──────────────────────────────────────────────";

    // ══════════════════════════════════════════════════════════
    //  SAFE INPUT HELPER
    // ══════════════════════════════════════════════════════════

    /**
     * Safely reads a line from the scanner.
     * Returns null if input stream is exhausted (NoSuchElementException)
     * or if an unexpected error occurs, allowing the caller to exit gracefully.
     *
     * @param scanner the Scanner to read from
     * @return trimmed input string, or null if input is unavailable
     */
    private static String safeReadLine(Scanner scanner) {
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine().trim();
            }
            return null;
        } catch (NoSuchElementException | IllegalStateException e) {
            return null;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ══════════════════════════════════════════════════════════

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        BookingService bookingService;

        // ── Initialise service (load CSV data) ───────────────
        try {
            bookingService = new BookingService();
        } catch (Exception e) {
            System.out.println("[FATAL] Failed to initialise the system: " + e.getMessage());
            System.out.println("Please ensure data files exist in the 'data/' directory.");
            return;
        }

        // ── Welcome banner ───────────────────────────────────
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("   🏥  DOCTOR APPOINTMENT BOOKING SYSTEM");
        System.out.println(SEPARATOR);
        System.out.println();

        // ── Main loop — allows multiple bookings ─────────────
        boolean running = true;
        while (running) {

            // ──────────────────────────────────────────────────
            // STEP 1: Patient Login
            // ──────────────────────────────────────────────────
            Patient patient = null;
            while (patient == null) {
                System.out.print("Enter Patient ID (e.g., P001): ");
                String patientId = safeReadLine(scanner);

                if (patientId == null) {
                    // Input stream exhausted — exit gracefully
                    System.out.println("\n[INFO] Input stream closed. Exiting.");
                    running = false;
                    break;
                }

                if (patientId.isEmpty()) {
                    System.out.println("⚠  Input cannot be empty. Please enter a valid Patient ID.");
                    continue;
                }

                patient = bookingService.validatePatient(patientId);
                if (patient == null) {
                    System.out.println("❌  Invalid patient. Try again.");
                }
            }

            if (!running || patient == null) break;

            System.out.println();
            System.out.println(THIN_SEP);
            System.out.println("✅  Welcome, " + patient.getName() + "! (Age: " + patient.getAge() + ")");
            System.out.println(THIN_SEP);
            System.out.println();

            // ──────────────────────────────────────────────────
            // STEP 2: Ask for Doctor ID → retrieveDoctor()
            // ──────────────────────────────────────────────────

            // Display available doctors for convenience
            System.out.println("📋  Available Doctors:");
            System.out.println(THIN_SEP);
            System.out.printf("  %-8s %-22s %s%n", "ID", "Name", "Specialization");
            System.out.println(THIN_SEP);
            for (Doctor d : bookingService.getAllDoctors()) {
                System.out.printf("  %-8s %-22s %s%n",
                        d.getDoctorId(), d.getName(), d.getSpecialization());
            }
            System.out.println(THIN_SEP);
            System.out.println();

            Doctor doctor = null;
            while (doctor == null) {
                System.out.print("Enter Doctor ID (e.g., D001): ");
                String doctorId = safeReadLine(scanner);

                if (doctorId == null) {
                    System.out.println("\n[INFO] Input stream closed. Exiting.");
                    running = false;
                    break;
                }

                if (doctorId.isEmpty()) {
                    System.out.println("⚠  Input cannot be empty. Please enter a valid Doctor ID.");
                    continue;
                }

                doctor = bookingService.retrieveDoctor(doctorId);
                if (doctor == null) {
                    System.out.println("❌  Invalid doctor. Try again.");
                }
            }

            if (!running || doctor == null) break;

            System.out.println();
            System.out.println("🩺  Selected Doctor: " + doctor.getName()
                    + " (" + doctor.getSpecialization() + ")");
            System.out.println();

            // ── Check if ALL slots are booked for this doctor ──
            if (bookingService.areAllSlotsBooked(doctor.getDoctorId())) {
                System.out.println("❌  All slots for " + doctor.getName()
                        + " are fully booked. No appointments available.");
                System.out.println();
                System.out.print("Would you like to book another appointment? (yes/no): ");
                String again = safeReadLine(scanner);
                if (again != null && (again.equalsIgnoreCase("yes") || again.equalsIgnoreCase("y"))) {
                    continue;
                } else {
                    running = false;
                    continue;
                }
            }

            // ──────────────────────────────────────────────────
            // STEP 3 & 4: Display 10 slots & Patient selects
            // ──────────────────────────────────────────────────
            Map<Integer, String> allSlots        = bookingService.getAllSlots();
            List<Integer>        bookedSlots     = bookingService.getBookedSlots(doctor.getDoctorId());

            System.out.println("🕐  Available Appointment Slots:");
            System.out.println(THIN_SEP);
            for (Map.Entry<Integer, String> entry : allSlots.entrySet()) {
                String status = bookedSlots.contains(entry.getKey()) ? " [BOOKED]" : "";
                System.out.printf("  %2d. %s%s%n", entry.getKey(), entry.getValue(), status);
            }
            System.out.println(THIN_SEP);
            System.out.println();

            // ──────────────────────────────────────────────────
            // STEP 5: checkTime() → verify availability
            // ──────────────────────────────────────────────────
            int selectedSlot = -1;
            boolean slotConfirmed = false;

            while (!slotConfirmed) {
                System.out.print("Select a slot (1-10): ");
                String slotInput = safeReadLine(scanner);

                if (slotInput == null) {
                    System.out.println("\n[INFO] Input stream closed. Exiting.");
                    running = false;
                    break;
                }

                if (slotInput.isEmpty()) {
                    System.out.println("❌  Invalid slot selection. Please enter a number between 1 and 10.");
                    continue;
                }

                // ── Parse & validate ─────────────────────────
                try {
                    selectedSlot = Integer.parseInt(slotInput);
                } catch (NumberFormatException e) {
                    System.out.println("❌  Invalid slot selection. Please enter a number between 1 and 10.");
                    continue;
                }

                if (!bookingService.isValidSlot(selectedSlot)) {
                    System.out.println("❌  Invalid slot selection. Please enter a number between 1 and 10.");
                    continue;
                }

                // ── Check availability via checkTime() ──────
                if (!bookingService.checkTime(doctor.getDoctorId(), selectedSlot)) {
                    System.out.println("❌  Slot unavailable. Choose another.");
                    continue;
                }

                slotConfirmed = true;
            }

            if (!running || !slotConfirmed) break;

            System.out.println();

            // ──────────────────────────────────────────────────
            // STEP 6: getPatientHistory()
            // ──────────────────────────────────────────────────
            List<Appointment> history = bookingService.getPatientHistory(patient.getPatientId());

            System.out.println("📂  Your Appointment History:");
            System.out.println(THIN_SEP);
            if (history.isEmpty()) {
                System.out.println("  No previous appointments found.");
            } else {
                System.out.printf("  %-6s %-10s %-10s %-6s %s%n",
                        "AptID", "PatientID", "DoctorID", "Slot", "Time");
                System.out.println("  " + THIN_SEP);
                for (Appointment a : history) {
                    System.out.printf("  %-6s %-10s %-10s %-6d %s%n",
                            a.getAppointmentId(),
                            a.getPatientId(),
                            a.getDoctorId(),
                            a.getSlotId(),
                            a.getTime());
                }
            }
            System.out.println(THIN_SEP);
            System.out.println();

            // ──────────────────────────────────────────────────
            // STEP 7: addAppointment()
            // ──────────────────────────────────────────────────
            Appointment newAppointment = bookingService.addAppointment(patient, doctor, selectedSlot);

            // ──────────────────────────────────────────────────
            // STEP 8: appointmentSet() → show confirmation
            // ──────────────────────────────────────────────────
            System.out.println(bookingService.appointmentSet(newAppointment, patient, doctor));

            // ── Ask to continue or exit ──────────────────────
            System.out.print("Would you like to book another appointment? (yes/no): ");
            String again = safeReadLine(scanner);
            if (again == null || !(again.equalsIgnoreCase("yes") || again.equalsIgnoreCase("y"))) {
                running = false;
            }

            System.out.println();
        }

        // ── Goodbye ──────────────────────────────────────────
        System.out.println(SEPARATOR);
        System.out.println("   Thank you for using the Appointment System!");
        System.out.println("   Stay healthy! 🌟");
        System.out.println(SEPARATOR);

        scanner.close();
    }
}
