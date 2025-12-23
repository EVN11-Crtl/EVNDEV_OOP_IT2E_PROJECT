package com.matisense.ui;

import com.matisense.model.User;
import com.matisense.dao.UserDAO;
import com.matisense.exception.MatisenseException;
import com.matisense.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Login form for user authentication
 * Implements GUI for login with validation
 */
public class LoginForm extends JFrame {
    private static final Dimension DEFAULT_BUTTON_SIZE = new Dimension(140, 36);
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField passwordTextField;
    private JPanel passwordCardPanel;
    private JCheckBox showPasswordCheckBox;
    private JButton loginButton;
    private JButton registerButton;
    private JButton forgotPasswordButton;
    private JPanel mainPanel;
    private UserDAO userDAO;
    
    /**
     * Constructor
     */
    public LoginForm() {
        this.userDAO = new UserDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureFrame();
    }
    
    /**
     * Apply consistent sizing to buttons
     */
    private void applyButtonSizing(JButton... buttons) {
        for (JButton button : buttons) {
            if (button != null) {
                button.setPreferredSize(DEFAULT_BUTTON_SIZE);
                button.setMinimumSize(DEFAULT_BUTTON_SIZE);
            }
        }
    }
    
    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        passwordTextField = new JTextField(20);
        passwordCardPanel = new JPanel(new CardLayout());
        showPasswordCheckBox = new JCheckBox("Show Password");
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        forgotPasswordButton = new JButton("Forgot Password?");
        mainPanel = new JPanel(new BorderLayout());
        applyButtonSizing(loginButton, registerButton, forgotPasswordButton);
        
        // Add password fields to card panel
        passwordCardPanel.add(passwordField, "HIDDEN");
        passwordCardPanel.add(passwordTextField, "VISIBLE");
    }
    
    /**
     * Setup layout
     */
    private void setupLayout() {
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(21, 101, 192));
        infoPanel.setPreferredSize(new Dimension(250, 0));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));
        
        JLabel appNameLabel = new JLabel("<html><span style='font-size:16pt;font-weight:bold;color:#FFFFFF;'>Matisense</span></html>");
        appNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitleLabel = new JLabel("<html><span style='color:#E3F2FD;'>Community Report System</span></html>");
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea description = new JTextArea("Report incidents, receive announcements, and stay informed in your community.");
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setEditable(false);
        description.setFocusable(false);
        description.setOpaque(false);
        description.setForeground(new Color(227, 242, 253));
        description.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(appNameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(subtitleLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(description);
        infoPanel.add(Box.createVerticalGlue());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel formTitle = new JLabel("Sign in to your account");
        formTitle.setFont(new Font("Arial", Font.BOLD, 18));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(formTitle, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy++;
        formPanel.add(new JLabel("Username"), gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Password"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordCardPanel, gbc);
        
        gbc.gridy++;
        gbc.gridx = 1;
        formPanel.add(showPasswordCheckBox, gbc);
        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        loginButton.setPreferredSize(new Dimension(0, 32));
        formPanel.add(loginButton, gbc);
        
        gbc.gridy++;
        JPanel secondaryActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        secondaryActions.setOpaque(false);
        secondaryActions.add(registerButton);
        secondaryActions.add(forgotPasswordButton);
        formPanel.add(secondaryActions, gbc);
        
        mainPanel.add(infoPanel, BorderLayout.WEST);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });
        
        forgotPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleForgotPassword();
            }
        });
        
        // Enter key support
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        
        passwordTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        
        // Show password checkbox handler
        showPasswordCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout)(passwordCardPanel.getLayout());
                if (showPasswordCheckBox.isSelected()) {
                    // Show password - sync text then switch
                    passwordTextField.setText(new String(passwordField.getPassword()));
                    cl.show(passwordCardPanel, "VISIBLE");
                } else {
                    // Hide password - sync text then switch
                    passwordField.setText(passwordTextField.getText());
                    cl.show(passwordCardPanel, "HIDDEN");
                }
            }
        });
    }
    
    /**
     * Configure frame properties
     */
    private void configureFrame() {
        setTitle("Login - Matisense Community Report System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 360);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    /**
     * Handle login action
     */
    private void handleLogin() {
        final String username = usernameField.getText().trim();
        final String password = showPasswordCheckBox.isSelected()
            ? passwordTextField.getText().trim()
            : new String(passwordField.getPassword());

        // Validation
        if (!ValidationUtil.isNotEmpty(username)) {
            JOptionPane.showMessageDialog(this, "Please enter username", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!ValidationUtil.isNotEmpty(password)) {
            JOptionPane.showMessageDialog(this, "Please enter password", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        LoadingDialog loadingDialog = new LoadingDialog(this, "Signing you in...");
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            private MatisenseException authException;

            @Override
            protected User doInBackground() {
                try {
                    return userDAO.authenticate(username, password);
                } catch (MatisenseException e) {
                    authException = e;
                    return null;
                }
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
                if (authException != null) {
                    JOptionPane.showMessageDialog(LoginForm.this, "Error during login: " + authException.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    User user = get();
                    if (user != null) {
                        JOptionPane.showMessageDialog(LoginForm.this, "Login successful!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        if (user.isAdmin()) {
                            AdminDashboard adminDashboard = new AdminDashboard(user);
                            adminDashboard.setVisible(true);
                        } else {
                            ResidentDashboard residentDashboard = new ResidentDashboard(user);
                            residentDashboard.setVisible(true);
                        }
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginForm.this, "Invalid username or password",
                            "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginForm.this,
                        "Unexpected error during login: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        loadingDialog.setVisible(true);
    }
    
    /**
     * Handle register action
     */
    private void handleRegister() {
        RegistrationForm registrationForm = new RegistrationForm();
        registrationForm.setVisible(true);
    }
    
    /**
     * Handle forgot password action
     */
    private void handleForgotPassword() {
        JDialog dialog = new JDialog(this, "Reset Password", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField usernameInput = new JTextField(20);
        JTextField emailInput = new JTextField(20);
        JPasswordField newPasswordInput = new JPasswordField(20);
        JPasswordField confirmPasswordInput = new JPasswordField(20);
        
        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        formPanel.add(usernameInput, gbc);
        
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailInput, gbc);
        
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(newPasswordInput, gbc);
        
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(confirmPasswordInput, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton resetButton = new JButton("Reset");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameInput.getText().trim();
                String email = emailInput.getText().trim();
                String newPassword = new String(newPasswordInput.getPassword());
                String confirmPassword = new String(confirmPasswordInput.getPassword());
                
                if (!ValidationUtil.isNotEmpty(username)) {
                    JOptionPane.showMessageDialog(dialog, "Please enter username", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!ValidationUtil.isNotEmpty(email) || !ValidationUtil.isValidEmail(email)) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid email", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(dialog, "Passwords do not match", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    User user = userDAO.findByUsername(username);
                    if (user == null) {
                        JOptionPane.showMessageDialog(dialog, "Username not found", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(email)) {
                        JOptionPane.showMessageDialog(dialog, "Email does not match our records", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    boolean updated = userDAO.updatePassword(user.getId(), newPassword);
                    if (updated) {
                        JOptionPane.showMessageDialog(dialog, "Password reset successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to reset password", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (MatisenseException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error resetting password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        
        dialog.setVisible(true);
    }
    
    /**
     * Main method for testing
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true);
            }
        });
    }
}
