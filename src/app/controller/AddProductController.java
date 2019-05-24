package app.controller;

import app.DBUtils;
import app.Manager;
import app.tables.PeopleTable;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.stage.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.GridPane;
import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.logging.*;

/** View for 'Urun Ekle' button. */

public class AddProductController {

    @FXML private GridPane addProductStage;
    @FXML private TextField sellerField;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private ChoiceBox categoryBox;
    @FXML private ImageView uploadedImageView;
    @FXML private Button uploadButton;
    @FXML private Button submitButton;
    @FXML private Button backButton;
    private Image image;
    private File file;

    public void initManager(final Manager manager, final String[] cats, final PeopleTable user) {

        Stage stage = (Stage) addProductStage.getScene().getWindow();
        stage.sizeToScene();
        stage.setTitle("Ürün Ekleme");
        stage.setResizable(false);

        // used for opening image selection dialog
        FileChooser fileChooser = new FileChooser();

        // add category list to categoryBox in view
        categoryBox.getItems().addAll(cats);

        uploadButton.setOnAction(e -> {

            fileChooser.getExtensionFilters()
                    .addAll(new FileChooser
                            .ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
            // opens dialog to upload image
            File file = fileChooser.showOpenDialog(stage);
            if (file != null)
            {
                Image img = new Image(file.toURI().toString());
                uploadedImageView.setImage(img);
                uploadedImageView.setFitHeight(100);
                uploadedImageView.setFitWidth(100);
                uploadedImageView.setPreserveRatio(true);
                setImage(img);
            }
            setFile(file);
        });

        submitButton.setOnAction(e -> {

            Window owner = submitButton.getScene().getWindow();
            if (sellerField.getText().isEmpty() || nameField.getText().isEmpty() || priceField.getText().isEmpty()
                    || categoryBox.getSelectionModel().isEmpty() || getFile() == null) {
                manager.showAlert(Alert.AlertType.ERROR, owner, "Boş Alan",
                        "Lütfen, tüm alanları doldurunuz");
                return;
            }

            if (priceField.getText().matches(".*[a-z].*")) {
                   manager.showAlert(Alert.AlertType.ERROR, owner, "Yanlış fiyat",
                           "Fiyat yanlış girilmiş.");
                   return;
            }

            if (priceField.getText().matches(".*[,].*")) {
                manager.showAlert(Alert.AlertType.ERROR, owner, "Yanlış fiyat",
                        "Lütfen, fiyat değerini \".\" (nokta) ile giriniz.");
                return;
            }

            System.out.println("nameField: " + nameField.getText());
            System.out.println("priceField: " + Double.parseDouble(priceField.getText()));
            System.out.println("sellerField: " + sellerField.getText());
            System.out.println("categoryBox: " + categoryBox.getSelectionModel().getSelectedItem().toString());

            int productId = generateID();
            productId++;

            try {

                Image image = getImage();
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                String seller = sellerField.getText();
                String category = categoryBox.getSelectionModel().getSelectedItem().toString();

                /* update 1.01 */
                // Check the product in store if it is already been added,
                // if true then product with same name/seller/category exists
                boolean check = checkProductInStore(name, seller, category);
                System.out.println("check = " + check);
                if (!check) {
                    addProductToDatabase(productId, image, name, price, seller, category);
                    manager.showShopMenu(user);
                } else {
                    manager.showAlert(Alert.AlertType.ERROR, owner, "Hata Oluştu",
                            "Böyle bir ürün artık satışta var. Lütfen farklı" +
                                    " bir ürün ekleyiniz.");
                    return;
                }


            } catch (Exception ex) {
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, "Couldn't get access to addToDatabase() method", ex);
            }

            stage.close();
        });

        backButton.setOnAction(e -> {
            stage.close();
        });
    }

    /**
     * Converts image to byte array.
     * @author https://stackoverflow.com/questions/53365680/uploading-an-image-to-a-database-with-javafx
     */
    private byte[] imageToByteArray(Image image) {
        BufferedImage bufferimage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferimage, "png", output ); // works for jpg too somehow ?!
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toByteArray();
    }

    /**
     * Controls for the uniqueness of the product by
     * 3 parameters in the store.
     * @param name Name of the product.
     * @param seller Name of the developer/seller/publisher of the product.
     * @param category Category of the product.
     * @return The resulted boolean value of row.
     */
    private boolean checkProductInStore(String name, String seller, String category) {
        try {
            Connection connection = DBUtils.getConnection();
            String sql = "SELECT * FROM products WHERE name = \"" + name +
                    "\" && seller = \"" + seller + "\" && category = \"" + category + "\"";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            // If there is a match, will return true
            System.out.println("sql = " + sql);
            System.out.println("resultSet.next() = " + resultSet.next());
            boolean check = resultSet.next();
            return check;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Hata anlamında
        }
    }

    /**
     * Adds product to the database by sending all info through parameters.
     * In the process converts the Image into byte[] and then into Blob type.
     * @param id Unique id of the product generated by the generateID() method.
     * @param image Image of the product that will be converted into Blob type.
     * @param name Name of the product.
     * @param price Initial price of the product.
     * @param seller Name of the Developer/Seller/Publisher of the product.
     * @param category Category of the product.
     * @throws SQLException while adding to the database
     */
    private void addProductToDatabase(int id, Image image, String name, double price, String seller,
                                      String category) throws SQLException {
        PreparedStatement preparedStatement = null;
        String sql = "INSERT INTO products (id, img, name, price, discount, seller, category) VALUES (?,?,?,?,?,?,?)";
        Connection con = DBUtils.getConnection();

        // Convert image to byte array then to blob to store it in database
        byte[] buffer = imageToByteArray(image);
        try {
            preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.setBlob(2, new SerialBlob(buffer));
            preparedStatement.setString(3, name);
            preparedStatement.setDouble(4, price);
            preparedStatement.setDouble(5, 0);
            preparedStatement.setString(6, seller);
            preparedStatement.setString(7, category);
            preparedStatement.executeUpdate();
            System.out.println("Product inserted.");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Eklendi");
            alert.setHeaderText(null);
            alert.setContentText("Ürün başarılı şekilde eklendi.");
            alert.show();
            con.close();
        } catch (Exception e) {
            Logger.getLogger(Manager.class.getName()).log(Level.FINE,
                    "Error in addProductToDatabase(), in AddProductController", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Hata");
            alert.setHeaderText(null);
            alert.setContentText("Hata Oluşdu. Lütfen daha sonra deneyiniz.");
            alert.show();
        }
    }

    /**
     * Assigns unique id to the product to distinguish from other
     * products in database. Unique id is increased by 1 after the
     * latest added product in the database.
     * @return the unique id.
     */
    private int generateID() {
        try {
            Connection con = DBUtils.getConnection();
            ResultSet rs = con.createStatement().executeQuery("SELECT id FROM products ORDER BY id DESC LIMIT 1");

            int id = 0;

            while (rs.next()) {
                id = rs.getInt("id");
            }

//            int id = rs.getInt("id");
            System.out.println("id: " + id);
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Purpose of these functions is to extract variable from
     * uploadButton's handle() method and later use these variables in
     * submitbutton's handle() method.
     */
    private void setImage(Image img) {
        image = img;
    }

    private Image getImage() {
        return image;
    }

    private void setFile(File f) {
        file = f;
    }

    private File getFile() {
        return file;
    }

}
