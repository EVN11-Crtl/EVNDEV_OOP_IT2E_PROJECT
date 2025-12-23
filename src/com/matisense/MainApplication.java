package com.matisense;

import com.matisense.ui.LoadingDialog;
import com.matisense.ui.LoginForm;

import javax.swing.*;

/**
 * Main application class for Matisense Community Report System
 * Entry point for the application
 */
public class MainApplication {

    /**
     * Main method
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to set system look and feel: " + e.getMessage(),
                    "UI Error", JOptionPane.WARNING_MESSAGE);
        }

        // Start the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            final LoadingDialog loadingDialog = new LoadingDialog(null, "Initializing system...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() {
                    try {
                        // Simulate short loading to allow UI components to initialize
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    loadingDialog.dispose();
                    try {
                        LoginForm loginForm = new LoginForm();
                        loginForm.setVisible(true);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null,
                                "Error starting application: " + e.getMessage(),
                                "Startup Error", JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                }
            };

            worker.execute();
            loadingDialog.setVisible(true);
        });
    }
}
