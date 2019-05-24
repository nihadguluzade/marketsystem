package app.controller;

import app.Manager;
import app.tables.Order;
import app.tables.PeopleTable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/** Result page of the order.  */

public class OrderSuccessFailController {

    @FXML private AnchorPane orderStage;
    @FXML private Label hashLabel;
    @FXML private Button mainMenuButton;


    public void initialize() {}

    public void initPage(final Manager manager, final Order order, final PeopleTable user) {

        Stage stage = (Stage) orderStage.getScene().getWindow();
        System.out.println("stage = " + stage);
        stage.sizeToScene();
        stage.setTitle("Sipariş Mesajı");
        stage.setResizable(false);

        hashLabel.setText(order.getHash());
        if (mainMenuButton.getText().equals("Ana Sayfa")) {
            mainMenuButton.setOnAction(e -> manager.showShopMenu(user));
        } else {
            mainMenuButton.setOnAction(e -> manager.showLoginPage());
        }
    }

}
