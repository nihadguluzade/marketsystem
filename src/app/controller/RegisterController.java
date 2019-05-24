package app.controller;

import app.DBUtils;
import app.Manager;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.stage.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;
import java.util.logging.*;

public class RegisterController {

    @FXML private GridPane registerStage;
    @FXML private TextField tcField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private PasswordField passwordField;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    private boolean blankTc = true;
    private boolean blankUsername = true;

    public boolean isBlankTc() {
        return blankTc;
    }

    public void setBlankTc(boolean blankTc) {
        this.blankTc = blankTc;
    }

    public boolean isBlankUsername() {
        return blankUsername;
    }

    public void setBlankUsername(boolean blankUsername) {
        this.blankUsername = blankUsername;
    }

    public void initialize() {}

    public void initRegister(final Manager manager) {

        Stage stage = (Stage) registerStage.getScene().getWindow();
        stage.sizeToScene();
        stage.setTitle("Kayıt Başvurusu");
        stage.setResizable(false);

        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                Window owner = registerButton.getScene().getWindow();

                if (tcField.getText().isEmpty() || emailField.getText().isEmpty() ||
                    usernameField.getText().isEmpty() || nameField.getText().isEmpty() ||
                    surnameField.getText().isEmpty() || passwordField.getText().isEmpty()) {

                    manager.showAlert(Alert.AlertType.ERROR, owner, "Boş Alan",
                            "Lütfen tüm alanları doldurunuz");
                    return;
                }

                if (!tcField.getText().matches("[0-9]*")) {
                    manager.showAlert(Alert.AlertType.ERROR, owner, "Yanlış T.C. No.",
                            "Lütfen doğru T.C. Kimlik numarası giriniz");
                    return;
                }

                // check for unique tc and username
                controlCheck(Long.parseLong(tcField.getText()), usernameField.getText());

                if (!isBlankTc()) {
                    manager.showAlert(Alert.AlertType.ERROR, owner, "Kullanılan Bilgi",
                            "Bu T.C. Kimlik numarası artık kullanılmaktadır.");
                    tcField.setStyle("-fx-border-color:#f42;-fx-border-radius:5px");
                    return;
                } else {
                    tcField.setStyle("");
                }

                if (!isBlankUsername()) {
                    manager.showAlert(Alert.AlertType.ERROR, owner, "Kullanılan Bilgi",
                            "Bu kullanıcı adı artık kullanılmaktadır.");
                    usernameField.setStyle("-fx-border-color:#f42;-fx-border-radius:5px");
                    return;
                } else {
                    usernameField.setStyle("");
                }

                try {
                    if (addToDatabase(Long.parseLong(tcField.getText()), emailField.getText(), usernameField.getText(),
                            nameField.getText(), surnameField.getText(), passwordField.getText())) {
                        manager.showAlert(Alert.AlertType.INFORMATION, owner, "",
                                "Kayıt Başarılı.");
                    } else {
                        manager.showAlert(Alert.AlertType.ERROR, owner, "Kayıt BAŞARISIZ",
                                "Hata Oluşdu. Lütfen daha sonra deneyiniz.");
                    }
                } catch (SQLException e) {
                    Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, "Couldn't get access to addToDatabase() method", e);
                }


                manager.showLoginPage();

            }
        });

        // Return to the login page when clicking Back button
        backButton.setOnAction(e -> manager.showLoginPage());
    }

    // Controls if there are same tc and/or username
    private void controlCheck(long tc, String username) {

        try {

            Connection connection = DBUtils.getConnection();
            String sql = "SELECT * FROM people WHERE tc = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, tc);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                setBlankTc(false);
            else
                setBlankTc(true);

            sql = "SELECT * FROM people WHERE username = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                setBlankUsername(false);
            else
                setBlankUsername(true);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends all data of user from input fields to database.
     * @param tc user's unique TC number
     * @param username user's unique username
     */
    private boolean addToDatabase(long tc, String email, String username, String name, String surname, String password)
            throws SQLException {

        PreparedStatement preparedStatement = null;
        String sql = "INSERT INTO people (tc, email, username, name, surname, password, type) VALUES (?,?,?,?,?,?,?)";
        Connection con = DBUtils.getConnection();

        try {
            preparedStatement = con.prepareStatement(sql);
            preparedStatement.setLong(1, tc);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, name);
            preparedStatement.setString(5, surname);
            preparedStatement.setString(6, password);
            preparedStatement.setInt(7, 0);
            preparedStatement.executeUpdate();
            System.out.println("New user inserted successfully.");
            return true;
        } catch (Exception e) {
            Logger.getLogger(Manager.class.getName()).log(Level.FINE, "Something wrong happened in addToDatabase() method", e);
            return false;
        }
    }

}
