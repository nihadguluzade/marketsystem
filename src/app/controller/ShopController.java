package app.controller;

import app.DBUtils;
import app.Manager;
import app.tables.*;
import javafx.animation.PauseTransition;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.*;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.Optional;
import java.util.logging.*;

/**
 * Main page of the system. Controls the products view
 * and stores actions for admin users.
 */

public class ShopController {

    @FXML private AnchorPane shopStage;
    @FXML private TilePane tilePane;
    @FXML private ScrollPane scrollPane;
    @FXML private ChoiceBox productTypesBox;
    @FXML private Button exitButton;
    @FXML private Button addProductButton;
    @FXML private Button removeProductButton;
    @FXML private Button endRemovingButton;
    @FXML private Button makeDiscountButton;
    @FXML private Button refreshButton;
    @FXML private Button finishOrderButton;
    @FXML private Button searchSubmitButton;
    @FXML private Label basketCounterLabel;
    @FXML private Label addedLabel;
    @FXML private Label failedLabel;
    @FXML private TextField searchField;
    @FXML private Label usernameLabel;
    @FXML private Label saleLabel;
    private ObservableList<ProductTable> oblist = FXCollections.observableArrayList();
    private ObservableList<BasketTable> basketList = FXCollections.observableArrayList();
    private String[] cats = {"Hepsi", "Oyun", "Müzik", "Kitap", "Film"}; // Categories
    private Connection connection;

    private void openConnection() {
        try {
            connection = DBUtils.getConnection();
            PreparedStatement statement = connection.prepareStatement("use savt");
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkTableBasket() {
        String sql = "create table if not exists basket (" +
                "    username varchar(255) not null," +
                "    hash bigint," +
                "    product_id int," +
                "    img blob," +
                "    name varchar(255) not null," +
                "    price float," +
                "    discount float," +
                "    seller varchar(255) not null" +
                ");";
        try {
            PreparedStatement statement = connection.prepareStatement("use savt");
            statement.execute();
            statement = connection.prepareStatement(sql);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Table `basket` checked.");
    }

    private void checkTableProducts() {
        String sql = "create table if not exists products (" +
                "    id int," +
                "    img blob," +
                "    name varchar(255) not null," +
                "    price float," +
                "    discount float," +
                "    seller varchar(255)," +
                "    category varchar(255)" +
                ");";
        try {
            PreparedStatement statement = connection.prepareStatement("use savt");
            statement.execute();
            statement = connection.prepareStatement(sql);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Table `products` checked.");
    }

    public void initShop(final Manager manager, final PeopleTable user) {

        Stage stage = (Stage) shopStage.getScene().getWindow();
        stage.sizeToScene();
        stage.setResizable(false);

        openConnection();
        checkTableBasket();
        checkTableProducts();

        if (user.getType() == 0) {
            stage.setTitle("Alışveriş Sistemi");
            addProductButton.setVisible(false);
            removeProductButton.setVisible(false);
            makeDiscountButton.setVisible(false);
        } else {
            stage.setTitle("Alış - Veriş Uygulaması (Yönetici)");
        }

        if (countUserBasket(user) > 0) {
            finishOrderButton.setVisible(true);
        }

        scrollPane.setFitToWidth(true);
        basketCounterLabel.setText(Integer.toString(countUserBasket(user)));
        productTypesBox.getItems().addAll(cats);
        productTypesBox.getSelectionModel().selectFirst(); // sets default value of choicebox
        usernameLabel.setText(user.getUsername() + ".");

        // 'Filtrele'
        productTypesBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                sortProductsByCategory(productTypesBox.getSelectionModel().getSelectedIndex(), user);
            }
        });


        // 'Tamamla' butonu
        finishOrderButton.setOnAction(e -> {
            manager.viewBasketScreen(user);
        });


        // 'Fiyat/Indirim' butonu
        makeDiscountButton.setOnAction(e -> {
            update(0, user, null, null); // prevents bug
            manager.openDiscountView(user, oblist);
        });


        // 'Yenile' butonu
        refreshButton.setOnAction(e -> {
            // control for filter
            String f = cats[productTypesBox.getSelectionModel().getSelectedIndex()];
            update(0, user, f, null);
        });


        // 'Urun Ekle' butonu
        addProductButton.setOnAction(e -> {
            manager.openAddProductView(user);
        });


        // 'Cikis Yap' butonu
        exitButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Çıkış Onayla");
            alert.setHeaderText(null);
            alert.setContentText("Çıkış yapmak istediğinize emin misiniz?");
            Optional<ButtonType> action = alert.showAndWait();

            if (action.get() == ButtonType.OK) {
                manager.showLoginPage();
            }
        });


        // 'Urun Sil' butonu
        removeProductButton.setOnAction(e -> {
            String f = cats[productTypesBox.getSelectionModel().getSelectedIndex()];
            update(1, user, f, null);
            endRemovingButton.setVisible(true);
        });


        // 'Silmeyi Bitir' (Urun Sil) butonu
        endRemovingButton.setOnAction(e -> {
            endRemovingButton.setVisible(false);
            String f = cats[productTypesBox.getSelectionModel().getSelectedIndex()];
            update(0, user, f, null);
        });


        // 'Ara' butonu
        searchSubmitButton.setOnAction(e -> {
            String keyword = searchField.getText();
            update(0, user, null, keyword);
        });

        update(0, user, null, null);
    }


    /**
     * Method for filter property. Updates page according to selected option.
     * @param index selected option
     */
    private void sortProductsByCategory(int index, PeopleTable user) {
        update(0, user, cats[index], null);
    }

    /**
     * Counts current basket of the @param user from database.
     */
    private int countUserBasket(PeopleTable user) {
        try {
            PreparedStatement statement = connection.prepareStatement("use savt");
            statement.execute();
            ResultSet rs = connection.createStatement().executeQuery(
                    "SELECT * FROM basket WHERE username = '" + user.getUsername() + "'");

            int content = 0;
            while (rs.next()) {
                content++;
            }

            return content;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Action for 'sebete ekle' button. Finds id and tc of user from view,
     * calls addToBasket() method, counts user's basket and updates the
     * current scene.
     */
    @FXML
    private void addToBasketHandler(ActionEvent e) {
        System.out.println("e.getSource(): " + e.getSource());

        Button button = (Button) e.getSource();

        AnchorPane parent = (AnchorPane) button.getParent();
        Label idLabel = (Label) parent.lookup("#productIdLabel");
        Label tcLabel = (Label) parent.lookup("#tcLabel");

        String idString = idLabel.getText();
        int id = Integer.parseInt(idString);
        System.out.println("id: " + id);

        String tcString = tcLabel.getText();
        long tc = Long.parseLong(tcString);
        System.out.println("tc: " + tc);

        // gets the scene and finds Nodes to prevent NullPointerException
        Scene scene = button.getScene();
        basketCounterLabel = (Label) scene.lookup("#basketCounterLabel");
        addedLabel = (Label) scene.lookup("#addedLabel");
        failedLabel = (Label) scene.lookup("#failedLabel");
        finishOrderButton = (Button) scene.lookup("#finishOrderButton");

        if (addToBasket(id, tc)) {
            // show 'Eklendi!' label for 5 seconds
            finishOrderButton.setVisible(true);
            addedLabel.setVisible(true);
            PauseTransition visibility = new PauseTransition(
                    Duration.seconds(2)
            );
            visibility.setOnFinished(
                    event -> addedLabel.setVisible(false)
            );
            visibility.play();
        } else {
            // show fail label for 5 seconds
            failedLabel.setVisible(true);
            PauseTransition visibility = new PauseTransition(
                    Duration.seconds(2)
            );
            visibility.setOnFinished(
                    event -> failedLabel.setVisible(false)
            );
            visibility.play();
        }

        /* DYNAMICALLY UPDATES */
        // increases 'sebetim' label
        int basketCounter = Integer.parseInt(basketCounterLabel.getText());
        basketCounter++;
        basketCounterLabel.setText(Integer.toString(basketCounter));
    }

    /**
     * Action for 'delete' button. Finds id from view and
     * removes product by it is id.
     */
    @FXML
    private void deleteProductHandler(ActionEvent e) {
        //System.out.println("e.getSource(): " + e.getSource());

        Button button = (Button) e.getSource();
        AnchorPane parent = (AnchorPane) button.getParent();

        Label idLabel = (Label) parent.lookup("#productIdLabel");
        String idString = idLabel.getText();
        int id = Integer.parseInt(idString);
        System.out.println("deleteProductHandler(): id = " + id);

        Label tcLabel = (Label) parent.lookup("#tcLabel");
        long tc = Long.parseLong(tcLabel.getText());
        System.out.println("deleteProductHandler(): tc = " + tc);

        PeopleTable person = new PeopleTable();
        person.setTc(tc);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Ürün silme");
        alert.setHeaderText(null);
        alert.setContentText("Ürünü silmek istiyor musunuz?");
        Optional<ButtonType> action = alert.showAndWait();

        if (action.get() == ButtonType.OK) {
            removeProduct(id);
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            /* Prevents nodes to be null later in update() method,
             * so that page can update dynamically. */
            Scene scene = button.getScene();
            tilePane = (TilePane) scene.lookup("#tilePane");
            endRemovingButton = (Button) scene.lookup("#endRemovingButton");
            setTilePane(tilePane);
            setEndRemovingButton(endRemovingButton);

            endRemovingButton.setVisible(false);
            update(0, person, null, null);
        }

    }

    /**
     * Removes product from database by its id.
     */
    private void removeProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        PreparedStatement statement = null;
        try {
            openConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.execute();
            System.out.println("Product deleted.");
        } catch (Exception e) {
            System.out.println("Couldn't delete the product.");
        }
    }

    /**
     * Finds appropriate product by @param id and current logged user
     * by @param tc. Then adds username of logged person and some details
     * of product that user wants to add to basket to the basketList.
     * All the information stored in basketList then inserted into 'basket'
     * database.
     */
    private boolean addToBasket(int id, long tc) {
        try {
            String sql_product = "SELECT * FROM products WHERE id = " + id;
            String sql_people = "SELECT * FROM people WHERE tc = " + tc;
            String sql_basket =
                    "INSERT INTO basket (username, hash, id, img, name, price, discount, seller) VALUES (?,?,?,?,?,?,?,?)";
            BasketTable basketTable = new BasketTable();

            ResultSet rs_product = connection.createStatement().executeQuery(sql_product);
            ResultSet rs_people = connection.createStatement().executeQuery(sql_people);

            Blob blob = null;
            while (rs_product.next() && rs_people.next()) {

                // convert blob to byte array
                blob = rs_product.getBlob("img");
                int blobLength = (int) blob.length();
                byte[] blobAsBytes = blob.getBytes(1, blobLength);

                // convert byte array to image
                Image img = convertToJavaFXImage(blobAsBytes);

                basketTable.setUsername(rs_people.getString("username"));
                basketTable.setHash(generateHash());
                basketTable.setId(rs_product.getInt("id"));
                basketTable.setImage(img);
                basketTable.setName(rs_product.getString("name"));
                basketTable.setPrice(rs_product.getDouble("price"));
                basketTable.setDiscountPrice(rs_product.getDouble("discount"));
                basketTable.setSeller(rs_product.getString("seller"));

                basketList.add(basketTable);
            }
            
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = connection.prepareStatement(sql_basket);

                preparedStatement.setString(1, basketTable.getUsername());
                preparedStatement.setString(2, basketTable.getHash());
                preparedStatement.setInt(3, basketTable.getId());
                preparedStatement.setBlob(4, blob); // image
                preparedStatement.setString(5, basketTable.getName());
                preparedStatement.setDouble(6, basketTable.getPrice());
                preparedStatement.setDouble(7, basketTable.getDiscountPrice());
                preparedStatement.setString(8, basketTable.getSeller());
                preparedStatement.executeUpdate();

                System.out.println("Product inserted to basket table.\n");
                return true;

            } catch (Exception e) {
                Logger.getLogger(Manager.class.getName()).log(Level.FINE,
                        "Error in statement at addToBasket() method", e);
                return false;
            }

        } catch (SQLException e) {
            Logger.getLogger(Manager.class.getName()).log(Level.FINE, "Error at addToBasket() method", e);
            return false;
        }
    }

    /**
     * Stores data (in form of ProductTable objects) from database to Observable List oblist.
     * Then dynamically draws products and puts them to TilePane in shop.fxml file.
     * @param remove_flag if 1, 'sil' button will appear on screen
     * @param user writes user's tc on product (hidden label)
     * @param filter sorts products by category
     * @param keyword stores text written in search box
     */
    private void update(int remove_flag, final PeopleTable user, String filter, String keyword) {

        getTilePane().getChildren().clear(); // clear the tilepane view
        oblist.clear(); // clear the products list

        try {
            connection = DBUtils.getConnection();
            PreparedStatement statement = connection.prepareStatement("use savt");
            statement.execute();
            ResultSet rs;


            // send command to sql server according to condition
            if (keyword != null) {
                rs = connection.createStatement()
                        .executeQuery("SELECT * FROM products WHERE name LIKE '%" + keyword + "%' " +
                                "OR seller LIKE '%" + keyword + "%'");
            } else if (filter != null && !filter.equalsIgnoreCase("Hepsi")) {
                rs = connection.createStatement().executeQuery(
                        "SELECT * FROM products WHERE category = '" + filter + "'");
            } else {
                rs = connection.createStatement().executeQuery("SELECT * FROM products");
            }


            // store all data in ProductsTable Observable List
            while (rs.next()) {

                // convert blob to byte array
                Blob blob = rs.getBlob("img");
                int blobLength = (int) blob.length();
                byte[] blobAsBytes = blob.getBytes(1, blobLength);

                // convert byte array to image
                Image img = convertToJavaFXImage(blobAsBytes);

                // add data to Observable List
                oblist.add(new ProductTable(rs.getInt("id"), img,
                        rs.getString("name"), rs.getDouble("price"),
                        rs.getDouble("discount"), rs.getString("seller"),
                        rs.getString("category")));
            }

            // adds products to view
            for (ProductTable oblist: oblist) { // for (int i = 0; i < oblist.size(); i++)

                try {

                    // change decimal separator to ','
                    DecimalFormat decimalFormat = new DecimalFormat("#####0.00");
                    DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
                    symbols.setDecimalSeparator(',');
                    decimalFormat.setDecimalFormatSymbols(symbols);

                    // load product style
                    AnchorPane loader = FXMLLoader.load(getClass().getResource("/app/view/product.fxml"));

                    // set the hidden id of product
                    Label productIdLabel = (Label) loader.getChildren().get(6);
                    productIdLabel.setText(Integer.toString(oblist.getId()));

                    // set the hidden tc of user
                    Label userTcLabel = (Label) loader.getChildren().get(7);
                    userTcLabel.setText(Long.toString(user.getTc()));

                    // set image of product and center it
                    ImageView productImage = (ImageView) loader.getChildren().get(0);
                    productImage.setImage(oblist.getImage());
                    centerImage(productImage);

                    // set the name of product
                    Label productNameLabel = (Label) loader.getChildren().get(1);
                    productNameLabel.setText(oblist.getName());

                    Label sellerLabel = (Label) loader.getChildren().get(2);
                    sellerLabel.setText(oblist.getSeller());

                    // get the price label of product
                    Label productPriceLabel = (Label) loader.getChildren().get(3);

                    // set price based on discount
                    if (oblist.getDiscountPrice() > 0) {

                        // check if a double has no decimal part
                        if (oblist.getPrice() % 1 == 0 && oblist.getDiscountPrice() % 1 == 0) {

                            // if none of them has decimal part

                            int price = (int) oblist.getPrice();
                            int discountPrice = (int) oblist.getDiscountPrice();
                            productPriceLabel.setText(price + " ₺ → " +
                                    discountPrice + " ₺");

                        } else if (oblist.getPrice() % 1 == 0) {

                            // if discount has decimal part

                            int price = (int) oblist.getPrice();
                            productPriceLabel.setText(price + " ₺ → " +
                                    decimalFormat.format(oblist.getDiscountPrice()) + " ₺");

                        } else if (oblist.getDiscountPrice() % 1 == 0) {

                            // if price has decimal part

                            int discountPrice = (int) oblist.getDiscountPrice();
                            productPriceLabel.setText(decimalFormat.format(oblist.getPrice()) + " ₺ → " +
                                    discountPrice + " ₺");

                        } else {

                            // if both has decimal part

                            productPriceLabel.setText(decimalFormat.format(oblist.getPrice()) + " ₺ → " +
                                    decimalFormat.format(oblist.getDiscountPrice()) + " ₺");

                        }

                        // set the last 'indirimli' label
                        saleLabel = (Label) loader.getChildren().get(8);
                        saleLabel.setVisible(true);
                        productPriceLabel.setTextFill(Color.web("#ff7b00")); // orange, red #e4423f, green #09bb36
//                        productPriceLabel.setStyle("-fx-font-weight: bold");

                    } else {

                        // for products with 0 discount

                        if (oblist.getPrice() % 1 == 0) {
                            int price = (int) oblist.getPrice();
                            productPriceLabel.setText(price + " ₺");
                        } else {
                            productPriceLabel.setText(decimalFormat.format(oblist.getPrice()) + " ₺");
                            productPriceLabel.setTextFill(Color.web("#000000"));
                        }

                        productPriceLabel.setPadding(new Insets(15, 0, 0, 0));

                    }

                    if (remove_flag == 1) {
                        Button deleteButton = (Button) loader.getChildren().get(5);
                        deleteButton.setVisible(true);
                    }

                    // TODO: Test
                    /*productNameLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            productNameLabel.setTextFill(Color.web("#f42"));
                        }
                    });
                    productNameLabel.setOnMouseExited(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            productNameLabel.setTextFill(Color.web("#000000"));
                        }
                    });*/

                    // add elements into shop.fxml
                    tilePane.getChildren().add(loader);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE,
                    "Couldn't connect to database in ShopController", e);
        }
    }

    /**
     * Converts byte array into Image.
     */
    private static Image convertToJavaFXImage(byte[] raw) {
        WritableImage image = new WritableImage(100,100);
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(raw);
            BufferedImage read = ImageIO.read(bis);
            image = SwingFXUtils.toFXImage(read, null);
        } catch (IOException ex) {
            Logger.getLogger(ShopController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }

    /**
     * Centers image inside of ImageView in FXML.
     * @author https://stackoverflow.com/questions/32674393/centering-imageview-using-javafx
     */
    private static void centerImage(ImageView imgView) {

        Image img = imgView.getImage();

        if (img != null) {
            double w = 0;
            double h = 0;

            double ratioX = imgView.getFitWidth() / img.getWidth();
            double ratioY = imgView.getFitHeight() / img.getHeight();

            double reducCoeff = 0;
            if(ratioX >= ratioY) {
                reducCoeff = ratioY;
            } else {
                reducCoeff = ratioX;
            }

            w = img.getWidth() * reducCoeff;
            h = img.getHeight() * reducCoeff;

            imgView.setX((imgView.getFitWidth() - w) / 2);
            imgView.setY((imgView.getFitHeight() - h) / 2);

        }
    }

    /**
     * Generates random hash code for unique identification of user's products.
     */
    private static String generateHash() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    public TilePane getTilePane() {
        return tilePane;
    }

    public void setTilePane(TilePane tilePane) {
        this.tilePane = tilePane;
    }

    public Button getEndRemovingButton() {
        return endRemovingButton;
    }

    public void setEndRemovingButton(Button endRemovingButton) {
        this.endRemovingButton = endRemovingButton;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
