package app.controller;

import app.DBUtils;
import app.Manager;
import app.tables.PeopleTable;
import app.tables.ProductTable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Page to change price and sale price of product.
 * 'Fiyat/Indirim butonuna tikladiktan sonra aciliyor. */

public class MakeDiscountController {

    @FXML private AnchorPane makeDiscountStage;
    @FXML private ChoiceBox productsBox;
    @FXML private TextField newPriceField;
    @FXML private TextField discountField;
    @FXML private GridPane dataContainer;
    @FXML private Button submitButton;

    public void initialize() {}

    public void initManager(final Manager manager, final PeopleTable user, ObservableList oblist) {

        Stage stage = (Stage) makeDiscountStage.getScene().getWindow();
        stage.sizeToScene();
        stage.setTitle("Fiyat Değiştir");
        stage.setResizable(false);

        ArrayList<String> productDataList = new ArrayList<String>(oblist);
        ArrayList<String> productIdList = new ArrayList<>();
        getProductList(productDataList, productIdList);

        for (int i = 0; i < oblist.size(); i++) {
            productsBox.getItems().add(productDataList.get(i));
        }

        // when option is selected
        productsBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                dataContainer.setVisible(true);
            }
        });

        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Window owner = submitButton.getScene().getWindow();

                if (newPriceField.getText().isEmpty()) {
                    manager.showAlert(Alert.AlertType.ERROR, owner, "Boş Alan",
                            "Lütfen, alanı boş bırakmayınız.");
                    return;
                }

                if (newPriceField.getText().matches(".*[a-z].*") ||
                        newPriceField.getText().matches(".*[,].*") ||
                        discountField.getText().matches(".*[a-z].*") ||
                        discountField.getText().matches(".*[,].*")) {
                    manager.showAlert(Alert.AlertType.ERROR, owner, "Yanlış Karakter",
                            "Lütfen, doğru bir fiyat giriniz.");
                    return;
                }

                int index = productsBox.getSelectionModel().getSelectedIndex();
                System.out.println("productIdList: " + productIdList.get(index));

                ProductTable product = new ProductTable();
                product.setId(Integer.parseInt(productIdList.get(index)));
                product.setPrice(Double.parseDouble(newPriceField.getText()));
                product.setDiscountPrice(Double.parseDouble(discountField.getText()));

                setValuesToDatabase(product);
                manager.showShopMenu(user);

                stage.close();
            }
        });
    }


    /**
     * Get all products from database, store them in ArrayList,
     * and @return the product
     */
    private void getProductList(ArrayList<String> dataList, ArrayList<String> idList) {
        try {
            Connection con = DBUtils.getConnection();

            // send command to sql server
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM products");
            ProductTable product = null;

            while (rs.next()) {

                product = new ProductTable(rs.getInt("id"),
                        rs.getString("name"), rs.getDouble("price"),
                        rs.getString("seller"));

                idList.add(Integer.toString(product.getId()));
                dataList.add(product.toString());

            }

        } catch (SQLException e) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, "Error in getProductList()", e);
        }
    }

    /**
     * Updates values in database.
     */
    private void setValuesToDatabase(ProductTable product) {
        try {
            Connection con = DBUtils.getConnection();
            PreparedStatement preparedStatement = null;
            String sql = "UPDATE products SET price = ?, discount = ? WHERE id = ?";

            try {
                preparedStatement = con.prepareStatement(sql);
                preparedStatement.setDouble(1, product.getPrice());
                preparedStatement.setDouble(2, product.getDiscountPrice());
                preparedStatement.setInt(3, product.getId());
                preparedStatement.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.setTitle("Başarılı");
                alert.setHeaderText(null);
                alert.setContentText("Değerler başarılı şekilde değiştirildi.");
                alert.show();

            } catch (Exception e) {
                Logger.getLogger(Manager.class.getName()).log(Level.FINE, "error while executing update setValuesToDatabase() method", e);
            }

        } catch (SQLException e) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, "error in setValuesToDatabase()", e);
        }
    }
}
