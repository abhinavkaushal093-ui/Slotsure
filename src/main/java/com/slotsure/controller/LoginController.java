package com.slotsure.controller;

import com.slotsure.App;
import com.slotsure.config.Database;
import com.slotsure.model.Session;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label errorLabel;

    @FXML
    private void initialize() {
        roleCombo.getItems().addAll("Student / Staff", "Faculty / Admin");
        roleCombo.setValue("Student / Staff");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String selectedRole = roleCombo.getValue();
        
        // Map UI selection to Database ENUM
        String dbRoleTarget = selectedRole.equals("Faculty / Admin") ? "FACULTY" : "STAFF";

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password.");
            return;
        }

        String sql = "SELECT id, full_name, role FROM app_users WHERE email = ? AND password_hash = ? AND role = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, dbRoleTarget);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Valid credentials found
                    Session.loginUser(rs.getLong("id"), rs.getString("full_name"), rs.getString("role"));
                    App.setRoot("dashboard"); // Switch to dashboard
                } else {
                    errorLabel.setText("Invalid credentials or incorrect role selected.");
                }
            }
        } catch (Exception e) {
            errorLabel.setText("Database connection error: " + e.getMessage());
        }
    }
}
