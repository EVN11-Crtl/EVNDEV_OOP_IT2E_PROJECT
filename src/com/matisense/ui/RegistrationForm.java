package com.matisense.ui;

import com.matisense.model.User;
import com.matisense.dao.UserDAO;
import com.matisense.exception.MatisenseException;
import com.matisense.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Registration form for new user accounts
 * Implements GUI for user registration with comprehensive validation
 */
public class RegistrationForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JTextField addressField;
    private JComboBox<String> genderComboBox;
    private JTextField emailField;
    private JTextField contactNumberField;
    private JTextField birthdayField;
    private JButton birthdayPickerButton;
    private JButton registerButton;
    private JButton cancelButton;
    private JPanel mainPanel;
    private UserDAO userDAO;
    private static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    
    /**
     * Constructor
     */
    public RegistrationForm() {
        this.userDAO = new UserDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureFrame();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        fullNameField = new JTextField(20);
        addressField = new JTextField(20);
        genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        emailField = new JTextField(20);
        contactNumberField = new JTextField(20);
        birthdayField = new JTextField(20);
        birthdayField.setEditable(false);
        birthdayPickerButton = new JButton("Pick Date");
        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
    }
    
    /**
     * Setup layout
     */
    private void setupLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel titleLabel = new JLabel("User Registration");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);
        
        // Form fields
        addFormField(gbc, 1, "Username:", usernameField);
        addFormField(gbc, 2, "Password:", passwordField);
        addFormField(gbc, 3, "Confirm Password:", confirmPasswordField);
        addFormField(gbc, 4, "Full Name:", fullNameField);
        addFormField(gbc, 5, "Address:", addressField);
        addFormField(gbc, 6, "Gender:", genderComboBox);
        addFormField(gbc, 7, "Email:", emailField);
        addFormField(gbc, 8, "Contact Number:", contactNumberField);
        addFormField(gbc, 9, "Birthday (YYYY-MM-DD):", buildBirthdayFieldPanel());
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridy = 10;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel);
    }
    
    /**
     * Helper method to add form fields
     */
    private void addFormField(GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = row;
        gbc.gridx = 0;
        mainPanel.add(new JLabel(labelText), gbc);
        
        gbc.gridx = 1;
        mainPanel.add(field, gbc);
    }
    
    /**
     * Build the birthday field with picker button
     */
    private JPanel buildBirthdayFieldPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(birthdayField, BorderLayout.CENTER);
        panel.add(birthdayPickerButton, BorderLayout.EAST);
        return panel;
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });
        
        birthdayPickerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openBirthdatePicker();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    /**
     * Show calendar + time picker for birthdate selection
     */
    private void openBirthdatePicker() {
        final JDialog dialog = new JDialog(this, "Select Birthdate", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.setSize(420, 460);
        dialog.setLocationRelativeTo(this);
        
        LocalDate preselected = parseBirthdayValue(birthdayField.getText().trim());
        final YearMonth[] currentMonth = {preselected != null ? YearMonth.from(preselected) : YearMonth.now()};
        final LocalDate[] selectedDate = {preselected != null ? preselected : LocalDate.now()};
        
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 16f));
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        JPanel calendarPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(calendarPanel, BorderLayout.CENTER);
        dialog.add(centerWrapper, BorderLayout.CENTER);
        
        JButton clearButton = new JButton("Clear");
        JButton cancelButton = new JButton("Cancel");
        JButton selectButton = new JButton("Select");
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(clearButton);
        actionPanel.add(cancelButton);
        actionPanel.add(selectButton);
        
        dialog.add(actionPanel, BorderLayout.SOUTH);
        
        Runnable refreshCalendar = new Runnable() {
            @Override
            public void run() {
                calendarPanel.removeAll();
                String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
                for (String day : days) {
                    JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
                    dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD, 12f));
                    calendarPanel.add(dayLabel);
                }
                
                LocalDate firstDay = currentMonth[0].atDay(1);
                int startOffset = firstDay.getDayOfWeek().getValue() % 7; // Sunday-first
                for (int i = 0; i < startOffset; i++) {
                    calendarPanel.add(new JLabel(""));
                }
                
        int daysInMonth = currentMonth[0].lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth[0].atDay(day);
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFocusPainted(false);
            dayButton.setOpaque(true);
            dayButton.setBackground(Color.WHITE);
            dayButton.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            if (selectedDate[0] != null && date.equals(selectedDate[0])) {
                dayButton.setBackground(new Color(21, 101, 192));
                dayButton.setForeground(Color.WHITE);
            }
            dayButton.addActionListener(e -> {
                selectedDate[0] = date;
                SwingUtilities.invokeLater(this);
            });
            calendarPanel.add(dayButton);
        }
                
                calendarPanel.revalidate();
                calendarPanel.repaint();
                String monthText = currentMonth[0].getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) +
                    " " + currentMonth[0].getYear();
                monthLabel.setText(monthText);
            }
        };
        
        prevButton.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].minusMonths(1);
            refreshCalendar.run();
        });
        
        nextButton.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].plusMonths(1);
            refreshCalendar.run();
        });
        
        clearButton.addActionListener(e -> {
            birthdayField.setText("");
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        selectButton.addActionListener(e -> {
            if (selectedDate[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Please select a date.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            birthdayField.setText(selectedDate[0].format(DATE_ONLY_FORMAT));
            dialog.dispose();
        });
        
        refreshCalendar.run();
        dialog.setVisible(true);
    }
    
    private LocalDate parseBirthdayValue(String value) {
        if (!ValidationUtil.isNotEmpty(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value, DATE_ONLY_FORMAT);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
    
    /**
     * Configure frame properties
     */
    private void configureFrame() {
        setTitle("Registration - Matisense Community Report System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(550, 640);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    /**
     * Handle registration action
     */
    private void handleRegister() {
        // Get form data
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String address = addressField.getText().trim();
        String gender = (String) genderComboBox.getSelectedItem();
        String email = emailField.getText().trim();
        String contactNumber = contactNumberField.getText().trim();
        String birthday = birthdayField.getText().trim();
        
        // Validation
        StringBuilder errors = new StringBuilder();
        
        if (!ValidationUtil.isNotEmpty(username)) {
            errors.append("Username is required.\n");
        } else if (!ValidationUtil.isValidUsername(username)) {
            errors.append("Username must be 3-20 characters and contain only letters, numbers, and underscores.\n");
        }

        if (!ValidationUtil.isNotEmpty(password)) {
            errors.append("Password is required.\n");
        } else if (!ValidationUtil.isValidPassword(password)) {
            errors.append("Password must be at least 8 characters, include upper, lower, number, and special character.\n");
        }

        if (!password.equals(confirmPassword)) {
            errors.append("Passwords do not match.\n");
        }

        if (!ValidationUtil.isNotEmpty(fullName)) {
            errors.append("Full name is required.\n");
        }
        
        if (!ValidationUtil.isNotEmpty(address)) {
            errors.append("Address is required.\n");
        }
        
        if (!ValidationUtil.isNotEmpty(email)) {
            errors.append("Email is required.\n");
        } else if (!ValidationUtil.isValidEmail(email)) {
            errors.append("Invalid email format.\n");
        }
        
        if (ValidationUtil.isNotEmpty(contactNumber) && !ValidationUtil.isValidPhone(contactNumber)) {
            errors.append("Invalid phone number format.\n");
        }
        
        if (ValidationUtil.isNotEmpty(birthday) && !ValidationUtil.isValidDate(birthday)) {
            errors.append("Invalid birthdate. Use the calendar picker to select a valid date and 12-hour time.\n");
        }
        
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, errors.toString(), "Validation Errors", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Check if username already exists
            if (userDAO.findByUsername(username) != null) {
                JOptionPane.showMessageDialog(this, "Username already exists", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if email already exists
            if (userDAO.findByEmail(email) != null) {
                JOptionPane.showMessageDialog(this, "Email already exists", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create user
            User user = new User(username, password, fullName, email);
            user.setAddress(address);
            user.setGender(gender);
            user.setContactNumber(contactNumber.isEmpty() ? null : contactNumber);
            user.setBirthday(birthday.isEmpty() ? null : birthday);
            user.setUserRole(User.UserRole.RESIDENT);
            
            User createdUser = userDAO.create(user);
            
            if (createdUser != null && createdUser.getId() > 0) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (MatisenseException e) {
            JOptionPane.showMessageDialog(this, "Registration error: " + e.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
