package app.controller;

import app.DBUtils;
import app.Manager;
import app.tables.Order;
import app.tables.PeopleTable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.*;
import java.sql.SQLException;
import java.text.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.*;

/** Last order confirmation page. 'Devam Et' butonuna
 * tikladiktan sonra aciliyor. */

public class FinishOrderController {

    @FXML private AnchorPane finishOrderStage;
    @FXML private Label usernameLabel;
    @FXML private Label tcLabel;
    @FXML private Label nameLabel;
    @FXML private Label surnameLabel;
    @FXML private Label emailLabel;
    @FXML private Label totalPriceLabel;
    @FXML private TextField cityField;
    @FXML private TextField zipCodeField;
    @FXML private TextField receiverField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private Button submitButton;
    @FXML private Button backButton;
    private Connection connection;

    private void openConnection() {
        try {
            connection = DBUtils.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {}

    private void checkTableOrders() {
        String sql = "create table if not exists orders (" +
                "    username varchar(255) not null," +
                "    hash varchar(255)," +
                "    tc bigint," +
                "    first_name varchar(255)," +
                "    last_name varchar(255)," +
                "    email varchar(255)," +
                "    city varchar(255)," +
                "    zipcode varchar(255)," +
                "    buyer varchar(255)," +
                "    phone bigint," +
                "    address varchar(255)," +
                "    price float," +
                "    products_ids varchar(255)" +
                ");";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Table `orders` checked.");
    }

    public void initOrder(final Manager manager, final PeopleTable user, final int[] productsIdList) {

        Stage stage = (Stage) finishOrderStage.getScene().getWindow();
        stage.sizeToScene();
        stage.setTitle("Sipariş Onayı");
        stage.setResizable(false);

        openConnection();
        checkTableOrders();

        DecimalFormat decimalFormat = new DecimalFormat("#####0.00");
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        decimalFormat.setDecimalFormatSymbols(symbols);


        usernameLabel.setText(user.getUsername());
        tcLabel.setText(Long.toString(user.getTc()));
        nameLabel.setText(user.getName());
        surnameLabel.setText(user.getSurname());
        emailLabel.setText(user.getEmail());
        totalPriceLabel.setText(decimalFormat.format(getTotalPrice(user)) + " ₺");


        backButton.setOnAction(e -> manager.viewBasketScreen(user));
        System.out.println("Arrays.toString(productsIdList) = " + Arrays.toString(productsIdList));

        submitButton.setOnAction(e -> {

            if (cityField.getText().isEmpty() || zipCodeField.getText().isEmpty() ||
                receiverField.getText().isEmpty() || phoneField.getText().isEmpty() ||
            addressField.getText().isEmpty()) {
                manager.showAlert(Alert.AlertType.ERROR, submitButton.getScene().getWindow(),
                        "Boş Alan", "Lütfen, tüm alanları doldurunuz.");
                return;
            }

            if (phoneField.getText().matches(".*[a-z].*") || phoneField.getText().matches(".*[,].*")) {
                manager.showAlert(Alert.AlertType.ERROR, submitButton.getScene().getWindow(),
                        "Yanlış Bilgi", "Lütfen, irtibat numarasını doğru giriniz.");
                return;
            }

            // Arrays.toString(int[] a) converts int[] array into String
            Order order = new Order(cityField.getText(), zipCodeField.getText(), receiverField.getText(),
                    phoneField.getText(), addressField.getText(), getTotalPrice(user), Arrays.toString(productsIdList));

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Siparişi Onayla");
            alert.setHeaderText(null);
            alert.setContentText("Siparişi Onaylıyor musunuz?");
            Optional<ButtonType> action = alert.showAndWait();

            if (action.get() == ButtonType.OK) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                    // Check if product is still available in the store
                    if (checkForProductAvailability(productsIdList)) {

                    if (sendToDatabase(user, order)) { // if order info successfully sent to database
                        clearUserBasket(user);
                        manager.showOrderSuccess(order, user);
                    } else {
                        manager.showOrderFail(order, user);
                    }

                } else {
                    manager.showAlert(Alert.AlertType.ERROR, submitButton.getScene().getWindow(),
                            "Başarısız", "Üzgünüz, istediğiniz ürün(ler) artık stokta kalmadı.");
                    manager.showOrderFail(order, user);
                }
            }
        });

    }

    /**
     * Checks if product available in the store. If not then it's
     * out of stock.
     * @param productsIdList id's of the all products
     */
    private boolean checkForProductAvailability(int[] productsIdList) {
        try {
            for (int p: productsIdList) {
                ResultSet rs = connection.createStatement().executeQuery(
                        "SELECT * FROM products WHERE id = " + p);
                if (!rs.next()) {
                    // if false then there's no such product in store
                    System.out.println("Product not available anymore.");
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends all order information into `orders` table in the database.
     * @param user gets the all information about user.
     * @param order stores all order information in object.
     */
    private boolean sendToDatabase(PeopleTable user, Order order) {
        String sql = "INSERT INTO orders (username, hash, tc, first_name," +
                " last_name, email, city, zipcode, buyer, phone, address, price, productsId) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        order.setUsername(user.getUsername());
        order.setHash(generateHash());
        order.setTc(user.getTc());
        order.setName(user.getName());
        order.setSurname(user.getSurname());
        order.setEmail(user.getEmail());

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, order.getUsername());
            preparedStatement.setString(2, order.getHash());
            preparedStatement.setLong(3, order.getTc());
            preparedStatement.setString(4, order.getName());
            preparedStatement.setString(5, order.getSurname());
            preparedStatement.setString(6, order.getEmail());
            preparedStatement.setString(7, order.getCity());
            preparedStatement.setString(8, order.getZipcode());
            preparedStatement.setString(9, order.getReceiver());
            preparedStatement.setString(10, order.getPhone());
            preparedStatement.setString(11, order.getAddress());
            preparedStatement.setDouble(12, order.getPrice());
            preparedStatement.setString(13, order.getProductsId());
            preparedStatement.executeUpdate();
            System.out.println("Order sent.\n");
            return true;

        } catch (Exception e) {
            Logger.getLogger(Manager.class.getName()).log(Level.FINE,
                    "Error in statement at sendToDatabase(), in FinishOrderController", e);
            return false;
        }
    }

    /**
     * Clear the user's basket after successful order.
     */
    private void clearUserBasket(PeopleTable user) {
        String sql = "DELETE FROM basket WHERE username = ?";
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.executeUpdate();
            System.out.println("Basket cleared.");
        } catch (Exception e) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE,
                    "Error while clearing at clearUserBasket(), at FinishOrderController", e);
        }
    }

    /**
     * Calculates total price of current @param user's basket.
     */
    private double getTotalPrice(PeopleTable user) {
        try {
            String sql = "SELECT * FROM basket WHERE username = '" + user.getUsername() + "'";
            ResultSet rs = connection.createStatement().executeQuery(sql);

            double total = 0;

            while (rs.next()) {
                if (rs.getDouble("discount") > 0) {
                    total += rs.getDouble("discount");
                } else {
                    total += rs.getDouble("price");
                }
            }
            total = (double) Math.round(total * 100000d) / 100000d;

            return total;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Generates random hash code for unique identification of user's products.
     */
    private static String generateHash() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

}
