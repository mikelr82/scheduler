package schedule.controllers;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import static java.nio.file.Files.size;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import schedule.Appointment;
import schedule.DAO.AppointmentDAO;
import schedule.DAO.CustomerDAO;
import schedule.DAO.UserDAO;
import schedule.DataBase;
import schedule.LogFile;
import schedule.User;
import schedule.customer;

public class MainController implements Initializable {

    //CustomerTV FXML variables
    @FXML private TableColumn<customer, String> nameCol;
    @FXML private TableColumn<customer, String> addressCol;
    @FXML private TableColumn<customer, String> address2Col;
    @FXML private TableColumn<customer, String> cityCol;
    @FXML private TableColumn<customer, String> countryCol;
    @FXML private TableColumn<customer, Integer> zipCol;
    @FXML private TableColumn<customer, String> phoneCol;
    @FXML private TableView customerTV;
    @FXML private TextField customerSearchTF;
    @FXML private Button customerSearchButton;
    @FXML private Button addCustomerButton;
    @FXML private Button modifyCustomerButton;
    @FXML private Button deleteCustomerButton;
    
    //MONTH VIEW FXML VARIABLES
    @FXML private GridPane monthGP;
    @FXML private ComboBox monthComboBox;
    @FXML private ComboBox yearComboBox;
    
    //WEEK VIEW VARIABLEs
    @FXML private GridPane weekGP;
    @FXML private Label weekRangeLabel;
    @FXML private Button previousWeekButton;
    @FXML private Button nextWeekButton;
    LocalDate startOfWeek;
    LocalDate endOfWeek;
    
    //DAY VIEW VARIABLES
    @FXML private DatePicker dateLabel;
    @FXML private TableView dayViewTable;
    @FXML private TableColumn<Appointment, String> customerNameCol;
    @FXML private TableColumn<Appointment, String> titleCol;
    @FXML private TableColumn<Appointment, String> descriptionCol;
    @FXML private TableColumn<Appointment, String> locationCol;
    @FXML private TableColumn<Appointment, String> typeCol;
    @FXML private TableColumn<Appointment, String> urlCol;
    @FXML private TableColumn<Appointment, String> startCol;
    @FXML private TableColumn<Appointment, String> endCol;
    @FXML private TableColumn<Appointment, String> contactCol;
    @FXML private Button modifyAppointmentButton;
    @FXML private Button deleteAppointmentButton;
    private ObservableList<Appointment> todaysAppointments;
    
    //USER MANAGEMENT VARIABLES
    @FXML private TableView userTV;
    @FXML private TableColumn<User, String> userIdCol;
    @FXML private TableColumn<User, String> userNameCol;
    @FXML private TableColumn<User, String> userActiveCol;
    private ObservableList<User> users;
    private UserDAO userDao;
    private static User selectedUser;
    
    //GENERAL MAIN FXML VARIABLES
    @FXML Button newAppointmentButton;
    @FXML TabPane schedulePane;
    @FXML TabPane mainTabPane;
    @FXML Label messageLabel;
    
    //REPORTING TAB VARIABLES
    @FXML private GridPane bookingByTypeGP;
    @FXML private GridPane bookingByLocationGP;
    @FXML private TableView consultantScheduleTV;
    @FXML private ComboBox bookingsByTypeMonthComboBox;
    @FXML private ComboBox bookingsByTypeYearComboBox;
    @FXML private ComboBox consultantScheduleComboBox;
    @FXML private ComboBox bookingsByLocationMonthComboBox;
    @FXML private ComboBox bookingsByLocationYearComboBox;
    private ObservableList<Appointment> consultantSchedule = FXCollections.observableArrayList();
    @FXML private TableColumn<Appointment, String> consultantScheduleDateCol;
    @FXML private TableColumn<Appointment, String> consultantScheduleTimeCol;
    @FXML private TableColumn<Appointment, String> consultantScheduleCustomerCol;
    @FXML private TableColumn<Appointment, String> consultantScheduleEventCol;
    public int typeGPRow=0;
    public int locationGPRow=0;
    
    //MISC VARIABLES
    private User currentUser = LogInScreenController.getCurrentUser();
    private final CustomerDAO customerDao;
    private static customer selectedCustomer;
    private static Appointment selectedAppointment;
    private Stage mainStage;
    private static int mainTabSelection = -1;
    public static ObservableList<customer> customerList = FXCollections.observableArrayList();
    private AppointmentDAO appointmentDAO;
    private List<Appointment> appointment = new ArrayList<>();
    SingleSelectionModel<Tab> viewTabSelect; 
    SingleSelectionModel<Tab> mainTabSelect;
    private LocalDate viewDayTabDate;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter monthFormatter;
    private DateTimeFormatter yearFormatter;
    private DateTimeFormatter timeFormatter;
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    
    //DEFAULT CONSTRUCTOR
    public MainController() throws SQLException, IOException {
        customerDao = new CustomerDAO();
        appointmentDAO = new AppointmentDAO();
    }
    
    public static ObservableList getCustomerList(){
        return customerList;
    }
    public static Appointment getSelectedAppointment(){
        return selectedAppointment;
    }
    public static customer getSelectedCustomer(){
        return selectedCustomer;
    }
    public static User getSelectedUser(){
        return selectedUser;
    }
    
    //BUTTON HANDLERS
    public void handleNewUserButton(ActionEvent event) {
        loadScene("addUser.fxml");
    }
    public void handleEditUserButton(ActionEvent event){
        selectedUser = (User) userTV.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            loadScene("modifyUser.fxml");
        }
    }
    public void handleActivateUserButton(ActionEvent event){
        selectedUser = (User) userTV.getSelectionModel().getSelectedItem();
        if (selectedUser.getUserId() != 1){
            int active=-1;
            if (selectedUser.getActive()==1) {
                active = 0;
            } else {
                active = 1;
            }
            int select = users.indexOf(selectedUser);
            selectedUser.setActive(active);
            users.get(select).setActive(active);
            userDao.setActive(selectedUser);
            userTV.refresh();
            
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("ADMIN USER CAN NOT BE DEACTIVATED");
            alert.show();
        }
    }
    public void handlePreviousWeekButton(ActionEvent event){
        startOfWeek = startOfWeek.minusDays(7);
        endOfWeek = endOfWeek.minusDays(7);
        
        String weekRange = dateFormatter.format(startOfWeek) + " - " + dateFormatter.format(endOfWeek);
        
        weekRangeLabel.setText(weekRange);
        updateWeekView();
    }
    public void handleNextWeekButton(ActionEvent event){
        startOfWeek = startOfWeek.plusDays(7);
        endOfWeek = endOfWeek.plusDays(7);
        
        String weekRange = dateFormatter.format(startOfWeek) + " - " + dateFormatter.format(endOfWeek);
        
        weekRangeLabel.setText(weekRange);
        
        updateWeekView();
    }
    public void handleModifyAppointmentButton(ActionEvent event){
        selectedAppointment = (Appointment) dayViewTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            loadScene("modifyAppointment.fxml");
        }
    }
    public void handleDeleteAppointmentButton(ActionEvent event){
        selectedAppointment = (Appointment) dayViewTable.getSelectionModel().getSelectedItem();
        appointmentDAO.delete(selectedAppointment);
        todaysAppointments.remove(selectedAppointment);
        appointments.remove(selectedAppointment);
        appointment.remove(selectedAppointment);
        refreshMonth();
        updateWeekView();
    }
    public void handleCustomerDeleteButton(ActionEvent event) throws SQLException, IOException{
        selectedCustomer = (customer) customerTV.getSelectionModel().getSelectedItem();
        appointmentDAO.deleteCustomer(selectedCustomer);
        customerDao.delete(selectedCustomer);
        customerList.remove(selectedCustomer);
        refreshMonth();
        updateWeekView();
        refreshDayTab();
    }
    public void handleNewAppointmentButton () {
        loadScene("newAppointment.fxml");
    }
    public void handleCustomerModifyButton(ActionEvent event){
        selectedCustomer = (customer) customerTV.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null){
            loadScene("modifyCustomer.fxml");
        }
    }
    public void handleCustomerSearchButton(ActionEvent event){
        if (customerSearchButton.getText().equals("Search")) {
            customerSearchButton.setText("Clear Search");
            customerSearchTF.editableProperty().set(false);
            String searchString = customerSearchTF.getText();
            
            //Stream & Lambda to filter the customer list based on search terms
            ObservableList<customer> filteredList = customerList.stream()
                    .filter(s -> s.getName().toLowerCase().contains(searchString.toLowerCase()) || 
                                 s.getAddress().toLowerCase().contains(searchString.toLowerCase()) ||
                                 s.getCity().toLowerCase().contains(searchString.toLowerCase()) ||
                                 s.getCountry().toLowerCase().contains(searchString.toLowerCase()) ||
                                 String.valueOf(s.getZip()).contains(searchString) ||
                                 s.getPhone().contains(searchString))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            customerTV.setItems(filteredList);
        } else {
            customerTV.setItems(customerList);
            customerSearchTF.setText("");
            customerSearchTF.editableProperty().set(true);
            customerSearchButton.setText("Search");
        }
    }
    public void handleAddCustomerButton(ActionEvent event){
       loadScene("addCustomer.fxml");
    }
    public void handleExitButton(ActionEvent event) throws IOException {
        String logFile = "User ID: " + currentUser.getUserId() + "(" + currentUser.getUserName() + ") Exited the system.\n";
        new LogFile(logFile);
        System.exit(0);
    }
    
    public void updateWeekView(){
        //appointment = appointmentDAO.getAll();
        Node node = weekGP.getChildren().get(0);
        weekGP.getChildren().clear();
        weekGP.getChildren().add(0,node);
        
        for (int day = 0; day < 7; day++){
            int row=2;
            for (Appointment a : appointment)
            {
                String apptDate = dateFormatter.format(a.getStart());
                LocalDate passDate = startOfWeek.plusDays(day);
                
                String weekDate = dateFormatter.format(startOfWeek.plusDays(day));
                //Is the current appointment on today? - Add it to the grid
                if (apptDate.equals(weekDate)){
                    Label eventLabel = new Label();
                    
                    eventLabel.setOnMouseClicked(event -> toDayView(passDate));

                    eventLabel.setText(a.getTitle());
                    eventLabel.setFont(Font.font(null,FontWeight.NORMAL, 15));
                    weekGP.setHalignment(eventLabel, HPos.CENTER);
                    weekGP.setValignment(eventLabel, VPos.CENTER);
                    row++;
                    weekGP.add(eventLabel, day, row);
                }
            }              
        }
        
        String weekRange = dateFormatter.format(startOfWeek) + " - " + dateFormatter.format(endOfWeek);
        
        weekRangeLabel.setText(weekRange);
        weekRangeLabel.setFont(Font.font(null, FontWeight.BOLD, 25));
        
        for (int col=0; col < 7; col++){
            Label dayName = new Label();
            switch (col) {
                case 0:
                    dayName.setText("SUNDAY");
                    break;
                case 1:
                    dayName.setText("MONDAY");
                    break;
                case 2:
                    dayName.setText("TUESDAY");
                    break;
                case 3:
                    dayName.setText("WEDNESDAY");
                    break;
                case 4:
                    dayName.setText("THURSDAY");
                    break;
                case 5:
                    dayName.setText("FRIDAY");
                    break;
                case 6:
                    dayName.setText("SATURDAY");
                    break;
            }
            dayName.setFont(Font.font(null, FontWeight.BOLD, 25));
            LocalDate passDate = startOfWeek.plusDays(col);

            dayName.setOnMouseClicked(event -> toDayView(passDate));

            weekGP.setHalignment(dayName, HPos.CENTER);
            weekGP.setValignment(dayName, VPos.CENTER);
            weekGP.add(dayName, col, 1);
        }
        for (int col = 0; col < 7; col++){
            Label date = new Label();
            date.setText(dateFormatter.format(startOfWeek.plusDays(col)));
            date.setFont(Font.font(null,FontWeight.BOLD, 15));
            LocalDate passDate = startOfWeek.plusDays(col);

            date.setOnMouseClicked(event -> toDayView(passDate));
            weekGP.setHalignment(date, HPos.CENTER);
            weekGP.setValignment(date, VPos.CENTER);
            weekGP.add(date, col, 0);
        }
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
    public void toDayView(LocalDate date){
        viewDayTabDate = date;
        viewTabSelect.select(0);
        refreshDayTab();
    }
    public void refreshDayTab(){
        todaysAppointments = FXCollections.observableArrayList();
    //    appointment = appointmentDAO.getAll();
        todaysAppointments.clear();
        appointment.forEach(a -> {
            String apptDate = dateFormatter.format(a.getStart());
            String nowDate = dateFormatter.format(viewDayTabDate);
            if (apptDate.equals(nowDate)) {
                todaysAppointments.add(a);
            } 
        });
        dayViewTable.setItems(todaysAppointments);
        dateLabel.setValue(viewDayTabDate);
        viewTabSelect = schedulePane.getSelectionModel();
                //viewTabSelect.select(2);
                mainTabSelect = mainTabPane.getSelectionModel();
                
                schedulePane.setOnMouseClicked(event -> refreshDayTab());

//                int calendarSelectedTab = viewTabSelect.getSelectedIndex();
//                int mainSelectedTab =     mainTabSelect.getSelectedIndex();
//                
//                if (mainTabSelection == 0) {
//                    newAppointmentButton.setVisible(true);
//                    if (calendarSelectedTab==0) {
//                        modifyAppointmentButton.setVisible(true);
//                        deleteAppointmentButton.setVisible(true);
//                    } else {
//                        modifyAppointmentButton.setVisible(false);
//                        deleteAppointmentButton.setVisible(false);
//                    }
//                } else {
//                    modifyAppointmentButton.setVisible(false);
//                    deleteAppointmentButton.setVisible(false);
//                    newAppointmentButton.setVisible(false);
//                }
        int selectedTab = viewTabSelect.getSelectedIndex();
            
            if (selectedTab==0) {
                modifyAppointmentButton.setVisible(true);
                deleteAppointmentButton.setVisible(true);
            } else {
                modifyAppointmentButton.setVisible(false);
                deleteAppointmentButton.setVisible(false);
            }
    }
    public void refreshMonth(){
       // appointment = appointmentDAO.getAll();
        LocalDateTime thisDate = LocalDateTime.now();
        int selectedMonth = monthComboBox.getSelectionModel().getSelectedIndex() + 1;
        int selectedYear = yearComboBox.getSelectionModel().getSelectedIndex() + 2010;
        
        LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonth, 1);
        java.time.DayOfWeek dayWeek = selectedDate.getDayOfWeek();
        int dayOfWeek;
        switch (dayWeek){
            case SUNDAY:
                dayOfWeek=0;
                break;
            case MONDAY:
                dayOfWeek=1;
                break;
            case TUESDAY:
                dayOfWeek=2;
                break;
            case WEDNESDAY:
                dayOfWeek=3;
                break;
            case THURSDAY:
                dayOfWeek=4;
                break;
            case FRIDAY:
                dayOfWeek=5;
                break;
            case SATURDAY:
                dayOfWeek=6;
                break;
            default:
                dayOfWeek=0;
                break;
        }
        
        YearMonth lengthOfMonth = YearMonth.of(selectedYear, selectedMonth);
        int numOfDays = lengthOfMonth.lengthOfMonth();

        if (selectedMonth==2){
            if(lengthOfMonth.isLeapYear()){
                numOfDays+=1;
            }
        }
        
        //1st day of month
        int day = 1;
        boolean started=false;
        
        Node node = monthGP.getChildren().get(0);
        monthGP.getChildren().clear();
        monthGP.getChildren().add(0,node);
       
        for (int col=0; col < 7; col++){
            Label dayName = new Label();
            switch (col) {
                case 0:
                    dayName.setText("SUNDAY");
                    break;
                case 1:
                    dayName.setText("MONDAY");
                    break;
                case 2:
                    dayName.setText("TUESDAY");
                    break;
                case 3:
                    dayName.setText("WEDNESDAY");
                    break;
                case 4:
                    dayName.setText("THURSDAY");
                    break;
                case 5:
                    dayName.setText("FRIDAY");
                    break;
                case 6:
                    dayName.setText("SATURDAY");
                    break;
            }
            dayName.setFont(Font.font(null, FontWeight.BOLD, 25));
            monthGP.setHalignment(dayName, HPos.CENTER);
            monthGP.setValignment(dayName, VPos.CENTER);
            monthGP.add(dayName, col, 0);
        }
        
        for (int row = 1; row < 7; row++) 
        {
            for (int col = 0; col < 7; col++)
            {
                //Find where the first of the month falls on
                if (!started){
                    started=true;
                    col=dayOfWeek;
                }
                if (day <= numOfDays){
                    
                    //Day number label - Formatted to upper right of gridPane cell
                    Label label = new Label();
                    Label appointmentsLabel = new Label();
                    label.setText(String.valueOf(day));
                    label.setFont(Font.font(null, FontWeight.BOLD, 25));
                    label.setPadding(new Insets(2,5,0,0)); //top, right, bottom, left
                    
                    //Determine which day the gridPane cell represents and format it
                    LocalDate gridDate = LocalDate.of(selectedYear, selectedMonth, day);
                    
                    //LocalDateTime gridDate = LocalDateTime.of(thisDate.getYear(), thisDate.getMonth(), day, 10,10,30);
                    String gridDateString = dateFormatter.format(gridDate);
                    
                    monthGP.setHalignment(label, HPos.RIGHT);
                    monthGP.setValignment(label, VPos.TOP);
                    monthGP.add(label, col, row);
                    appointmentsLabel.setOnMouseClicked(event -> toDayView(gridDate));

                    String eventString = "";
                    String thisDateString = dateFormatter.format(thisDate);

                    for (Appointment a : appointment){

                        String apptDate = dateFormatter.format(a.getStart());
                        String apptTime = timeFormatter.format(a.getStart());

                        //Is the current appointment on today? - Add it to the grid
                        if (apptDate.equals(gridDateString)){
                            eventString = eventString + "\n" + a.getTitle();
                        }

                        //Is this grid today? - Set the text to red
                        if (gridDateString.equals(thisDateString)) {
                            label.setTextFill(Color.web("#FF0000"));
                        }
                    }                    
                    appointmentsLabel.setText(eventString);
                    appointmentsLabel.setPadding(new Insets(0,0,0,5));
                    appointmentsLabel.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);
                    monthGP.setValignment(appointmentsLabel, VPos.BOTTOM);
                    monthGP.add(appointmentsLabel, col, row);
                    day++;
                }
            }
        }
    }
    
    public void updateConsultantSchedule(User u){
        consultantSchedule.clear();
        consultantSchedule = appointmentDAO.getConsultantSchedule(u);
        consultantSchedule.forEach(c -> System.out.println(c.getTitle()));
        consultantScheduleTV.setItems(consultantSchedule);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // *************
        // GENERAL SETUP
        //**************
           
            List<String> typeList = new ArrayList<>();
            typeList.add("Laser Tag");
            typeList.add("VR Game Room");
            typeList.add("BUMPER CARS");
            typeList.add("MINI GOLF");
            typeList.forEach(t -> bookingByTypeGP.add(new Label(t + ": " + String.valueOf(appointmentDAO.getReportByType(t))), 0, typeGPRow++));
            
            List<String> locationList = new ArrayList<>();
            locationList.add("London");
            locationList.add("France");
            locationList.add("New York");
            locationList.add("Dallas");
            locationList.add("Los Angeles");
            locationList.add("Portland");
            locationList.forEach(l -> bookingByLocationGP.add(new Label(l + ": " + String.valueOf(appointmentDAO.getReportByLocation(l))), 0, locationGPRow++));
            
            //Change Listener for constultant schedule reporting
                consultantScheduleComboBox.getSelectionModel().selectedItemProperty().addListener(c -> updateConsultantSchedule(users.get(consultantScheduleComboBox.getSelectionModel().getSelectedIndex())));  
            //Change listener for current selected tab
                mainTabPane.getSelectionModel().selectedItemProperty().addListener((ob, oldTab, newTab) -> {
                    mainTabSelection = mainTabPane.getSelectionModel().getSelectedIndex();
                    if (mainTabSelection != 0) {
                        newAppointmentButton.setVisible(false);
                        modifyAppointmentButton.setVisible(false);
                        deleteAppointmentButton.setVisible(false);
                    } else {
                        newAppointmentButton.setVisible(true);
                        refreshMonth();
                        updateWeekView();
                        refreshDayTab();
                        
                    }
                });

            //Set tab to previously selected tab on load
                SingleSelectionModel<Tab> mainTab = mainTabPane.getSelectionModel();
                mainTab.select(mainTabSelection);
                
                if (currentUser.getUserId() != 1) {
                    mainTabPane.getTabs().get(2).setDisable(true);
                    mainTabPane.getTabs().get(2).setText("Admin - Restricted to admin users only");
                }

            //Set schedule view to monthly by default
                viewTabSelect = schedulePane.getSelectionModel();
                viewTabSelect.select(2);
                mainTabSelect = mainTabPane.getSelectionModel();
                
                schedulePane.setOnMouseClicked(event -> refreshDayTab());

                modifyAppointmentButton.setVisible(false);
                deleteAppointmentButton.setVisible(false);
            
            //Load customers from DB
                customerList = customerDao.getAll();
            //Load appointments from DB 
                appointment = appointmentDAO.getAll();

             //Get todays date
                LocalDateTime thisDate = LocalDateTime.now();
                viewDayTabDate = thisDate.toLocalDate();
            
            //Format date as Month-Day-Year
            //Format time as Hours:Minutes
                dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH);
                timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
                monthFormatter = DateTimeFormatter.ofPattern("MM", Locale.ENGLISH);
                yearFormatter = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH);

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();

            //delay execution until after initialized - centers window in the screen.  Using lambda to make code more readable and not require anon inner class
                Platform.runLater(() -> {
                    mainStage = (Stage) schedulePane.getScene().getWindow();
                    mainStage.setX((primScreenBounds.getWidth() - mainStage.getWidth()) / 2);
                    mainStage.setY((primScreenBounds.getHeight() - mainStage.getHeight()) / 2);
                    mainStage.setOnCloseRequest(event -> {
                        try {
                            handleExitButton(new ActionEvent());
                        } catch (IOException ex) {
                            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                    mainStage.setResizable(false);
                    users.forEach(u -> consultantScheduleComboBox.getItems().add(u.getUserName()));
                });
        
                
        //***********************
        //REPORTING Specific Setup
        //***********************
            consultantScheduleCustomerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            consultantScheduleDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
            consultantScheduleEventCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            consultantScheduleTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
            
        //****************
        //Day View Specific Setup
        //***********************
            ObservableList<Appointment> todaysAppointments = FXCollections.observableArrayList();
            appointment.forEach(a -> {
                String apptDate = dateFormatter.format(a.getStart());
                String nowDate = dateFormatter.format(viewDayTabDate);
                if (apptDate.equals(nowDate)) {
                    todaysAppointments.add(a);
                } 
            });
            dateLabel.setValue(viewDayTabDate);
            dateLabel.setOnAction(event -> toDayView(dateLabel.getValue()));
            
            //Populate the columns of the appointment table
                customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
                titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
                descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
                locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
                typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
                urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));
                startCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
                endCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
                contactCol.setCellValueFactory(new PropertyValueFactory<>("contactName"));
                dayViewTable.setItems(todaysAppointments);
            
        //**************************
        //Month View Specific Setup
        //**************************
            List<String> months = new ArrayList<>();
            List<Integer> years = new ArrayList<>();
            months.add("January");
            months.add("February");
            months.add("March");
            months.add("April");
            months.add("May");
            months.add("June");
            months.add("July");
            months.add("August");
            months.add("September");
            months.add("October");
            months.add("November");
            months.add("December");
            months.forEach(m -> monthComboBox.getItems().add(m));
            
            for (int year = 2010; year <= 2050; year++){
                years.add(year);
            }
            years.forEach(y -> yearComboBox.getItems().add(y));
            
            String nowMonth = monthFormatter.format(viewDayTabDate);
            String nowYear = yearFormatter.format(viewDayTabDate);
            
            int currentMonth = Integer.parseInt(nowMonth) - 1;
            int currentYear = Integer.parseInt(nowYear) - 2010;
            
            monthComboBox.getSelectionModel().select(currentMonth);
            yearComboBox.getSelectionModel().select(currentYear);
            
            monthComboBox.valueProperty().addListener(c -> refreshMonth());
            yearComboBox.valueProperty().addListener(c -> refreshMonth());
            
            refreshMonth();

        //***************
        //WEEK VIEW SETUP
        //***************
            LocalDate today = LocalDate.now();
            java.time.DayOfWeek todaysDay = today.getDayOfWeek();
            int dayWeek=0;
            switch (todaysDay){
                case SUNDAY:
                    dayWeek=1;
                    break;
                case MONDAY:
                    dayWeek=2;
                    break;
                case TUESDAY:
                    dayWeek=3;
                    break;
                case WEDNESDAY:
                    dayWeek=4;
                    break;
                case THURSDAY:
                    dayWeek=5;
                    break;
                case FRIDAY:
                    dayWeek=6;
                    break;
                case SATURDAY:
                    dayWeek=7;
                    break;
            }
            int diff = dayWeek - 1;

            startOfWeek = today.minusDays(diff);
            endOfWeek = startOfWeek.plusDays(6);
            updateWeekView();
            
        //******************
        //Customer Tab Setup
        //******************
            //Populate the columns of the customer table
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
            address2Col.setCellValueFactory(new PropertyValueFactory<>("address2"));
            cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
            countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
            zipCol.setCellValueFactory(new PropertyValueFactory<>("zip"));
            phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
            customerTV.setItems(customerList);
            
        
        //*********************
        //USER MANAGEMENT SETUP
        //*********************
            try {
                userDao = new UserDAO();
            } catch (SQLException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
            users = userDao.getAll();
            userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
            userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
            userActiveCol.setCellValueFactory(new PropertyValueFactory<>("active"));
            userTV.setItems(users);
    }
}