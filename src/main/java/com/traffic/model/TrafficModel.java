package com.traffic.model;

import com.traffic.db.DatabaseHandler;
import java.util.Random;

public class TrafficModel {
    private volatile boolean running = false;

    // Emergency State
    private volatile boolean emergencyActive = false;
    private volatile String emergencyDirection = "NONE"; // "MAIN" or "CROSS"

    // Density State
    private double mainDensity = 0.5;
    private double crossDensity = 0.5;
    private final Random random = new Random();

    private final DatabaseHandler dbHandler;
    private final UpdateCallback onUpdate;
    private Thread simulationThread; // Keep reference to interrupt it

    public interface UpdateCallback {
        void accept(LightColor main, LightColor cross, double mainDensity, double crossDensity);
    }

    public TrafficModel(UpdateCallback onUpdate) {
        this.onUpdate = onUpdate;
        this.dbHandler = new DatabaseHandler();
    }

    // --- NEW: CLEAN INTERRUPT LOGIC ---
    public void triggerEmergency(String direction) {
        this.emergencyActive = true;
        this.emergencyDirection = direction;

        dbHandler.logEvent("EMERGENCY_TRIGGER", "Direction: " + direction);

        // WAKE UP the simulation thread immediately!
        if (simulationThread != null && simulationThread.isAlive()) {
            simulationThread.interrupt();
        }
    }

    public void startSimulation() {
        if (running) return;
        running = true;
        dbHandler.logEvent("SYSTEM_START", "Dynamic Density System Started");

        simulationThread = new Thread(() -> {
            try {
                while (running) {
                    // Check for emergency before starting a cycle
                    handleEmergencyIfActive();

                    // --- PHASE 1: MAIN GREEN ---
                    // We calculate duration, but we update density live inside the wait
                    long duration = calculateSmartDuration(mainDensity);
                    update(LightColor.GREEN, LightColor.RED);
                    waitAndFluctuateDensity(duration);

                    // --- PHASE 2: MAIN YELLOW ---
                    update(LightColor.YELLOW, LightColor.RED);
                    waitAndFluctuateDensity(3000);

                    // --- PHASE 3: SAFETY RED ---
                    update(LightColor.RED, LightColor.RED);
                    waitAndFluctuateDensity(2000);

                    handleEmergencyIfActive(); // Check again

                    // --- PHASE 4: CROSS GREEN ---
                    duration = calculateSmartDuration(crossDensity);
                    update(LightColor.RED, LightColor.GREEN);
                    waitAndFluctuateDensity(duration);

                    // --- PHASE 5: CROSS YELLOW ---
                    update(LightColor.RED, LightColor.YELLOW);
                    waitAndFluctuateDensity(3000);

                    // --- PHASE 6: SAFETY RED ---
                    update(LightColor.RED, LightColor.RED);
                    waitAndFluctuateDensity(2000);
                }
            } catch (InterruptedException e) {
                // If interrupted here, loop back to handle emergency
                Thread.currentThread().interrupt();
            }
        });
        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    //Update Density constantly while waiting
    private void waitAndFluctuateDensity(long duration) throws InterruptedException {
        long elapsed = 0;
        int step = 500; // Update every 0.5s

        while (elapsed < duration) {
            // 1. Check if we need to stop for emergency immediately
            if (emergencyActive) throw new InterruptedException("Emergency Interrupt");

            fluctuateDensity();


            Thread.sleep(step);
            elapsed += step;
        }
    }

    // --- EMERGENCY HANDLER (Runs on the same thread!) ---
    private void handleEmergencyIfActive() {
        while (emergencyActive) {
            try {
                System.out.println("🚨 HANDLING EMERGENCY: " + emergencyDirection);

                // 1. Force Safety Red first
                update(LightColor.RED, LightColor.RED);
                Thread.sleep(1500);

                // 2. Turn Green for the requested direction
                if (emergencyDirection.equals("MAIN")) {
                    update(LightColor.GREEN, LightColor.RED);
                } else {
                    update(LightColor.RED, LightColor.GREEN);
                }

                // 3. Hold for 6 seconds
                Thread.sleep(6000);

                // 4. Back to Red
                update(LightColor.RED, LightColor.RED);
                Thread.sleep(1000);

                // 5. Done
                emergencyActive = false;
                dbHandler.logEvent("EMERGENCY_END", "Resuming Normal Cycle");

            } catch (InterruptedException e) {
                // Ignore interrupts during emergency, finish the job
            }
        }
    }

    private long calculateSmartDuration(double density) {
        // Base 5s + up to 10s based on density
        return 5000 + (long)(density * 10000);
    }

    private void fluctuateDensity() {
        // Randomly nudge density up or down by 0.05
        mainDensity = clamp(mainDensity + (random.nextDouble() - 0.5) * 0.1);
        crossDensity = clamp(crossDensity + (random.nextDouble() - 0.5) * 0.1);

        // We have to 'refresh' the UI even if lights didn't change
        // We will do this by calling the update with nulls or tracking state?
        // Let's rely on the controller to handle the update.
        onUpdate.accept(null, null, mainDensity, crossDensity);
    }

    private double clamp(double val) {
        return Math.max(0.1, Math.min(1.0, val));
    }

    private void update(LightColor main, LightColor cross, double... ignored) {
        // Main update method for light changes
        onUpdate.accept(main, cross, mainDensity, crossDensity);
    }

    private void update(LightColor main, LightColor cross) {
        onUpdate.accept(main, cross, mainDensity, crossDensity);
    }
}