package schedule.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import schedule.Appointment;
import schedule.DAO.AppointmentDAO;
import schedule.DAO.CustomerDAO;
import schedule.LogFile;
import schedule.User;
import schedule.customer;

public class NewAppointmentController implements Initializable {

    @FXML
    private DatePicker appointmentDatePicker;
    @FXML
    private ComboBox<String> customerComboBox;
    @FXML
    private ComboBox<String> contactComboBox;
    @FXML
    private TextField appointmentTitle;
    @FXML
    private TextArea appointmentDescription;
    @FXML
    private ComboBox<String> locationComboBox;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private TextField appointmentURLTF;
    @FXML
    private ComboBox<String> startTimeComboBox;
    @FXML
    private ComboBox<String> endTimeComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    
    public Stage primaryStage;
    private User currentUser = LogInScreenController.getCurrentUser();
    private CustomerDAO customerDao;
    private Stage mainStage;
    private ObservableList<customer> customerList = MainController.getCustomerList();
    private List<User> userList = LogInScreenController.getUsers();
    private AppointmentDAO appointmentDao;
    private DateTimeFormatter timeFormatter;

    
    public NewAppointmentController() throws SQLException, IOException {
        this.appointmentDao = new AppointmentDAO();
    }
    public void handleSaveButton(ActionEvent event) throws SQLException, IOException {
        if (checkValidInput()){
            int customer = customerComboBox.getSelectionModel().getSelectedIndex();
            int contact = contactComboBox.getSelectionModel().getSelectedIndex();
            int type = typeComboBox.getSelectionModel().getSelectedIndex();
            int startTime = startTimeComboBox.getSelectionModel().getSelectedIndex();
            int endTime = endTimeComboBox.getSelectionModel().getSelectedIndex();
            int location = locationComboBox.getSelectionModel().getSelectedIndex();
            customer selectedCustomer = customerList.get(customer);
            User selectedContact = userList.get(contactComboBox.getSelectionModel().getSelectedIndex());
            
            String startDateTime = appointmentDatePicker.getValue().toString() + " " + startTimeComboBox.getSelectionModel().getSelectedItem();
            String endDateTime = appointmentDatePicker.getValue().toString() + " " + endTimeComboBox.getSelectionModel().getSelectedItem();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a");
            LocalDateTime startLDT = LocalDateTime.parse(startDateTime, format);
            LocalDateTime endLDT = LocalDateTime.parse(endDateTime, format);
            
            
            
            Appointment appointment = new Appointment(
                    0,
                    selectedCustomer.getId(), 
                    currentUser.getUserId(), 
                    appointmentTitle.getText().toString(), 
                    appointmentDescription.getText().toString(), 
                    locationComboBox.getSelectionModel().getSelectedItem().toString(), 
                    selectedContact.getUserId(),
                    typeComboBox.getSelectionModel().getSelectedItem(), 
                    appointmentURLTF.getText(), 
                    startLDT, 
                    endLDT);
            
            appointmentDao.add(appointment, currentUser);
            loadScene("main.fxml");
            String customerName = customerList.get(customer).getName();
            System.out.println(appointment.getStart());
        }
    }
    
    public boolean checkValidInput(){
        if (!startTimeComboBox.getSelectionModel().isEmpty()) {
            if (!endTimeComboBox.getSelectionModel().isEmpty()) {
                if (!customerComboBox.getSelectionModel().isEmpty()) {
                    if (!contactComboBox.getSelectionModel().isEmpty()) {
                        if (!locationComboBox.getSelectionModel().isEmpty()) {
                            if (!typeComboBox.getSelectionModel().isEmpty()) {
                                if (!appointmentTitle.getText().isEmpty()) {
                                    if (!appointmentDescription.getText().isEmpty()) {
                                        if (appointmentDatePicker.getValue() != null){
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("ALL FIELDS EXCEPT URL MUST BE FILLED");
        alert.show();
        return false;
    }
    
    public void handleCancelButton(ActionEvent event){
        loadScene("main.fxml");
    }
    
    public void loadScene(String sceneName){
        sceneName = "schedule/views/" + sceneName;
         try {
            Parent mainParent = FXMLLoader.load(getClass().getClassLoader().getResource(sceneName));
            Scene mainScene = new Scene(mainParent);
            mainStage.setScene(mainScene);
            mainStage.show();
        } catch (IOException ex) {
            Logger.getLogger(LogInScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        //START AND END TIME COMBO BOX LOADING
            timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
            List<String> time = new ArrayList<>();
            String minString = "";
            String hourString = "";
            String timeString = "";
            for (int hour=0; hour<24; hour++)
            {
                for (int min=0; min<60; min+=15){
                    if (min==0){
                        minString = "00";
                    } else {
                        minString = String.valueOf(min);
                    }
                    if (hour < 10) {
                        hourString = "0" + String.valueOf(hour) + ":";
                    } else {
                        hourString = String.valueOf(hour) + ":";
                    }

                    timeString = hourString + minString;
                    LocalTime timeStringDate = LocalTime.parse(timeString);
                    timeString = timeFormatter.format(timeStringDate);
                    time.add(timeString);
                }
            }
            time.forEach(t -> startTimeComboBox.getItems().add(t));
            time.forEach(t -> endTimeComboBox.getItems().add(t));
        //LOCALTION COMBO BOX LOADING
            List<String> locationList = new ArrayList<>();
            locationList.add("London");
            locationList.add("France");
            locationList.add("New York");
            locationList.add("Dallas");
            locationList.add("Los Angeles");
            locationList.add("Portland");
            locationList.forEach(l -> locationComboBox.getItems().add(l));
        //TYPE COMBO BOX LOADING
            List<String> typeList = new ArrayList<>();
            typeList.add("Laser Tag");
            typeList.add("VR Game Room");
            typeList.add("BUMPER CARS");
            typeList.add("MINI GOLF");
            typeList.forEach(t -> typeComboBox.getItems().add(t));
        
        //LOADING CUSTOMERS AND USERS WITH LAMBDA TO LOAD FROM LIST TO THE COMBO BOX
            customerList.forEach(c -> customerComboBox.getItems().add(c.getName()));
            userList.forEach(u -> contactComboBox.getItems().add(u.getUserName()));
        
        
        //Post Load processing
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            Platform.runLater(() -> {
                primaryStage = (Stage)saveButton.getScene().getWindow();
                primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
                primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);    
                primaryStage.setResizable(false);
                mainStage = (Stage) saveButton.getScene().getWindow();
                });    
    }    
    
}