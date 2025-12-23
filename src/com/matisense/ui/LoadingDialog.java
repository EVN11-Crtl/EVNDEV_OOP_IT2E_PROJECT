package com.matisense.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable loading dialog with indeterminate progress indicator.
 * Can be used across the application to show blocking progress while
 * background tasks are running.
 */
public class LoadingDialog extends JDialog {
    private final JLabel messageLabel;
    private final JProgressBar progressBar;

    public LoadingDialog(Window owner, String message) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(255, 255, 255, 240));

        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(250, 252, 255));
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Please wait...");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setPreferredSize(new Dimension(220, 18));

        contentPanel.add(titleLabel);
        contentPanel.add(messageLabel);
        contentPanel.add(progressBar);

        add(contentPanel, BorderLayout.CENTER);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }
}
