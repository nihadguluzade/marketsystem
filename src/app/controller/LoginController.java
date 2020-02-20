package app.controller;

import app.DBUtils;
import app.Manager;
import app.tables.PeopleTable;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.*;
import javafx.scene.control.*;
import java.sql.*;

/** Login page, first page, of the app. */

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button submitButton;
    @FXML private Button registerButton;
    private Connection connection;

    public void initDatabase() {
        DBUtils.createDatabase();
    }

    private void openConnection() {
        try {
            connection = DBUtils.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkTablePeople() {
        String db = "use savt;";
        String sql = "create table if not exists people (" +
                "    tc bigint," +
                "    email varchar(255)," +
                "    username varchar(255)," +
                "    first_name varchar(255)," +
                "    last_name varchar(255)," +
                "    password varchar(255)," +
                "    type int" +
                ");";
        try {
            PreparedStatement statement = connection.prepareStatement(db);
            statement.execute();
            statement = connection.prepareStatement(sql);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Table `people` checked.");
    }

    public void initManager(final Manager manager) {

        Scene scene = submitButton.getScene();

        // Fix scene size whenever mouse moves
        // Bug fix, otherwise gets NullPointerException
        scene.setOnMouseMoved(e -> {
            Stage stage = (Stage) scene.getWindow();
            stage.sizeToScene();
            stage.setResizable(false);
            stage.setTitle("Alışveriş Sistemi");
        });

        openConnection();
        checkTablePeople();

        submitButton.setOnAction(e -> {

            Window owner = submitButton.getScene().getWindow();
            if (usernameField.getText().isEmpty()) {
                manager.showAlert(Alert.AlertType.ERROR, owner, "Boş Alan",
                        "HATA: Kullanıcı Adı kısmı boş");
                return;
            }
            if (passwordField.getText().isEmpty()) {
                manager.showAlert(Alert.AlertType.ERROR, owner, "Boş Alan",
                        "HATA: Şifre girilmemiş");
                return;
            }

            PeopleTable user = null;
            user = authorize(usernameField.getText(), passwordField.getText());

            if (user != null) {
                manager.showShopMenu(user);
            } else {
                manager.showAlert(Alert.AlertType.ERROR, owner, "Yanlış Bilgi",
                        "Kayıt bulunamadı. Girdiğiniz bilgileri bir daha kontrol ediniz.");
            }
        });

        registerButton.setOnAction(e -> manager.showRegisterPage());

    }

    /**
     * Sends the values to the database
     * @param username entered username
     * @param password entered password
     * @return all user information
     * @throws SQLException
     */
    private PeopleTable authorize(String username, String password) {

        // Open connection to database
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "SELECT * FROM people WHERE username = ? and password = ?";

        try{
            // changing ? marks with real values in String sql
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery(); // sends sql code to database

            System.out.printf("username: %s\npassword: %s\n", username, password);
            if(!resultSet.next()){
                return null;
            } else {
                // store resulted value in PeopleTable loggedUser
                PeopleTable loggedUser;
                loggedUser = new PeopleTable(resultSet.getLong("tc"),
                        resultSet.getString("email"),
                        resultSet.getString("username"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("password"),
                        resultSet.getInt("type"));
                return loggedUser;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
