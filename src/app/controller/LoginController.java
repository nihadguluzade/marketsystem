package app.controller;

import app.DBUtils;
import app.Manager;
import app.tables.PeopleTable;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.*;
import javafx.scene.control.*;
import java.sql.*;
import java.util.logging.*;

/** Login page, first page, of the app. */

public class LoginController {

    /* @FXML - view'lerdeki nesneleri class'lara aktarmak icin kullanilir */
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button submitButton;
    @FXML private Button registerButton;

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
            try {
                user = authorize(usernameField.getText(), passwordField.getText());
            } catch (SQLException ex) {
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, "Couldn't get access to authorize() method", e);
            }
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
    private PeopleTable authorize(String username, String password) throws SQLException {

        // Open connection to database
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "SELECT * FROM people WHERE username = ? and password = ?";
        Connection con = DBUtils.getConnection();

        try{
            // changing ? marks with real values in String sql
            preparedStatement = con.prepareStatement(sql);
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
                        resultSet.getString("name"),
                        resultSet.getString("surname"),
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
