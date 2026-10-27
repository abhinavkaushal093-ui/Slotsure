package com.slotsure.controller;

import com.slotsure.model.Resource;
import com.slotsure.model.Session;
import com.slotsure.repository.BookingRepository;
import com.slotsure.repository.ResourceRepository;
import com.slotsure.service.BookingService;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private TabPane mainTabPane;
    @FXML private Tab adminScheduleTab;
    @FXML private Tab adminResourceTab;

    // Booking Form
    @FXML private ComboBox<Resource> resourceCombo;
    @FXML private TextField purposeField;
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private Label messageLabel;

    // Admin Schedule Table
    @FXML private TableView<BookingRepository.BookingDisplay> scheduleTable;
    @FXML private TableColumn<BookingRepository.BookingDisplay, String> colRes, colUser, colStart, colEnd, colPurp, colStat;

    // Admin Inventory Table
    @FXML private TableView<Resource> inventoryTable;
    @FXML private TableColumn<Resource, String> invName, invType, invLoc;
    @FXML private TableColumn<Resource, Integer> invCap;
    @FXML private TableColumn<Resource, String> invActive;

    private final ResourceRepository resourceRepo = new ResourceRepository();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final BookingService bookingService = new BookingService();
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        userNameLabel.setText(Session.getName());
        userRoleLabel.setText(Session.getRole());

        // Enforce RBAC: Remove admin tabs if user is not an admin
        if (!Session.isAdmin()) {
            mainTabPane.getTabs().remove(adminScheduleTab);
            mainTabPane.getTabs().remove(adminResourceTab);
        } else {
            setupAdminTables();
        }

        // Fix Blank Dropdown Issue
        resourceCombo.setConverter(new StringConverter<Resource>() {
            @Override
            public String toString(Resource resource) {
                if (resource == null) return null;
                return resource.name() + " (" + resource.capacity() + " seats) - " + resource.location();
            }
            @Override
            public Resource fromString(String string) {
                return null;
            }
        });

        startDatePicker.setValue(java.time.LocalDate.now());
        loadDropdownResources();
    }

    private void setupAdminTables() {
        colRes.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().resourceName()));
        colUser.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().bookedBy()));
        colStart.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().start()));
        colEnd.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().end()));
        colPurp.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().purpose()));
        colStat.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().status()));

       invName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().name()));
        invType.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().resourceType()));
        invLoc.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().location()));
        invCap.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().capacity()));
        invActive.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().active() ? "🟢 Online & Ready" : "🔴 Maintenance / Offline"));
    }

    private void loadDropdownResources() {
        resourceCombo.setDisable(true);
        Task<java.util.List<Resource>> task = new Task<>() {
            @Override protected java.util.List<Resource> call() throws Exception {
                return resourceRepo.findActive();
            }
        };
        task.setOnSucceeded(e -> {
            resourceCombo.getItems().setAll(task.getValue());
            resourceCombo.setDisable(false);
        });
        new Thread(task).start();
    }

    @FXML
    private void handleBooking() {
        try {
            Resource res = resourceCombo.getValue();
            if (res == null) throw new IllegalArgumentException("Please select a facility from the dropdown.");
            
            LocalTime startTime = LocalTime.parse(startTimeField.getText().trim(), TIME_FORMAT);
            LocalTime endTime = LocalTime.parse(endTimeField.getText().trim(), TIME_FORMAT);
            
            LocalDateTime start = LocalDateTime.of(startDatePicker.getValue(), startTime);
            LocalDateTime end = LocalDateTime.of(startDatePicker.getValue(), endTime);
            
            long id = bookingService.createBooking(res.id(), Session.getUserId(), start, end, purposeField.getText());
            messageLabel.setText("Success! Booking #" + id + " secured.");
            messageLabel.setStyle("-fx-text-fill: #10b981;"); // Green text for success
            purposeField.clear();
            
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #ef4444;"); // Red text for errors
        }
    }

    @FXML
    private void loadAdminSchedule() {
      if (!Session.isAdmin() || !adminScheduleTab.isSelected()) return;
        
        Task<java.util.List<BookingRepository.BookingDisplay>> task = new Task<>() {
            @Override protected java.util.List<BookingRepository.BookingDisplay> call() throws Exception {
                return bookingRepo.fetchMasterSchedule();
            }
        };
        
        task.setOnSucceeded(e -> {
            scheduleTable.getItems().setAll(task.getValue());
            scheduleTable.refresh();
        });
        
        // Catch and print any silent SQL errors to the VS Code terminal
        task.setOnFailed(e -> {
            System.err.println("CRITICAL: Failed to load schedule data from MySQL.");
            task.getException().printStackTrace();
        });
        
        new Thread(task).start();
    }

    @FXML
    private void handleCancelBooking() {
        BookingRepository.BookingDisplay selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        try {
            bookingRepo.cancelBooking(selected.id());
            loadAdminSchedule(); 
        } catch (Exception e) {
            System.err.println("Failed to cancel: " + e.getMessage());
        }
    }

    @FXML
    private void loadAdminResources() {
        if (!Session.isAdmin() || !adminResourceTab.isSelected()) return;
        Task<java.util.List<Resource>> task = new Task<>() {
            @Override protected java.util.List<Resource> call() throws Exception {
                return resourceRepo.findAll();
            }
        };
        task.setOnSucceeded(e -> inventoryTable.getItems().setAll(task.getValue()));
        new Thread(task).start();
    }

    @FXML
    private void handleToggleResource() {
        Resource selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            resourceRepo.toggleResourceStatus(selected.id(), !selected.active());
            loadAdminResources(); 
            loadDropdownResources(); 
        } catch (Exception e) {
            System.err.println("Failed to toggle: " + e.getMessage());
        }
    }
    @FXML
    private void handleLogout() {
        try {
            Session.logout();
            com.slotsure.App.setRoot("login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}