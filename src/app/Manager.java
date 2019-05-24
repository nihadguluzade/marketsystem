package app;

import java.io.IOException;
import java.util.logging.*;
import app.controller.*;
import app.tables.Order;
import app.tables.PeopleTable;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;

/**
 * Page that controls transitions between pages.
 */

public class Manager extends Main {

    private Scene scene;
    private String[] cats = {"Oyun", "Müzik", "Kitap", "Film"}; // Categories

    public Scene getScene() {
        return scene;
    }

    /** Constructor */
    public Manager(Scene scene) {
        this.scene = scene;
    }

    /**
     * Pop ups alert in front of the program.
     */
    public void showAlert(Alert.AlertType alertType, Window window, String title, String message) {
        Alert alert = new Alert(alertType);
        // Set window on top of everyone
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(window);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    public void showLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/login.fxml"));
            scene.setRoot((Parent) loader.load());
            LoginController controller = loader.<LoginController>getController();
            controller.initManager(this);
        } catch (IOException e) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void showRegisterPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/register.fxml"));
            scene.setRoot((Parent) loader.load());
            RegisterController controller = loader.<RegisterController>getController();
            controller.initRegister(this);
        } catch (IOException e) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void showShopMenu(PeopleTable user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/shop.fxml"));
            scene.setRoot((Parent) loader.load());
            ShopController controller = loader.<ShopController>getController();
            controller.initShop(this, user);
        } catch (IOException e) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void viewBasketScreen(PeopleTable user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/basket.fxml"));
            scene.setRoot((Parent) loader.load());
            ViewBasketScreenController controller = loader.<ViewBasketScreenController>getController();
            controller.initBasket(this, user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showFinishOrderScreen(PeopleTable user, int[] productsIdList) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/finishOrder.fxml"));
            scene.setRoot((Parent) loader.load());
            FinishOrderController controller = loader.<FinishOrderController>getController();
            controller.initOrder(this, user, productsIdList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openAddProductView(PeopleTable user) {
        try {
            FXMLLoader loader = new FXMLLoader((
                    getClass().getResource("view/addProduct.fxml"))
            );
            Scene scene = new Scene(loader.load()); // create new scene, otherwise scene will not change after
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
            AddProductController controller =
                    loader.<AddProductController>getController();
            controller.initManager(this, cats, user);
        } catch (IOException ex) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void openDiscountView(PeopleTable user, ObservableList oblist) {
        try {
            FXMLLoader loader = new FXMLLoader((
                    getClass().getResource("view/makeDiscount.fxml"))
            );
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
            MakeDiscountController controller =
                    loader.<MakeDiscountController>getController();
            controller.initManager(this, user, oblist);
        } catch (IOException ex) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showOrderSuccess(Order order, PeopleTable user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/order_success.fxml"));
            scene.setRoot((Parent) loader.load());
            OrderSuccessFailController controller = loader.<OrderSuccessFailController>getController();
            controller.initPage(this, order, user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showOrderFail(Order order, PeopleTable user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/order_fail.fxml"));
            scene.setRoot((Parent) loader.load());
            OrderSuccessFailController controller = loader.<OrderSuccessFailController>getController();
            controller.initPage(this, order, user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
