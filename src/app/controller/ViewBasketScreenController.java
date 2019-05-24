package app.controller;

import app.DBUtils;
import app.Manager;
import app.tables.*;
import javafx.collections.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.logging.*;

/**
 * Sepetin son halini gosteren sayfa.
 */

public class ViewBasketScreenController {

    @FXML private AnchorPane basketViewStage;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox vBoxPanel;
    @FXML private Label productCounterLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Button continueButton;
    @FXML private Button goBackButton;

    private Double totalPrice;
    private int totalCounter;
    private ObservableList<BasketTable> basketList = FXCollections.observableArrayList();
    private int[] productsIdList;

    public void initBasket(final Manager manager, final PeopleTable user) {

        Stage stage = (Stage) basketViewStage.getScene().getWindow();
        stage.sizeToScene();
        stage.setTitle("Sepet");
        stage.setResizable(false);

        updateBasketList(user);

        // hide horizontal scroller
        scrollPane.setFitToWidth(true);

        // 'Geri' butonu
        goBackButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                manager.showShopMenu(user);
            }
        });

        // 'Devam et' butonu
        continueButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                productsIdList = getProductsIdList();
                manager.showFinishOrderScreen(user, productsIdList);
            }
        });

    }

    @FXML
    private void removeFromBasketHandler(ActionEvent e) {
        System.out.println("e.getSource() = " + e.getSource());

        Button button = (Button) e.getSource();

        AnchorPane parent = (AnchorPane) button.getParent();

        Label hashLabel = (Label) parent.lookup("#productHashLabel");
        String hashString = hashLabel.getText();
        System.out.println("hashString = " + hashString);

        Label usernameLabel = (Label) parent.lookup("#usernameLabel");
        String username = usernameLabel.getText();
        System.out.println("username = " + username);

        Label idLabel = (Label) parent.lookup("#idLabel");
        int id = Integer.parseInt(idLabel.getText());

        PeopleTable person = new PeopleTable();
        person.setUsername(username);

        try {
            Thread.sleep(100);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }

        if (!removeProductFromBasket(hashString)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Ürün Silme");
            alert.setHeaderText(null);
            alert.setContentText("Hata Oluşdu. Lütfen daha sonra deneyiniz.");
            alert.show();
        }
        else {

            Button removeButton = (Button) parent.lookup("#removeButton");
            Scene scene = removeButton.getScene();

            vBoxPanel = (VBox) scene.lookup("#vBoxPanel");
            productCounterLabel = (Label) scene.lookup("#productCounterLabel");
            totalPriceLabel = (Label) scene.lookup("#totalPriceLabel");
            continueButton = (Button) scene.lookup("#continueButton");
            setvBoxPanel(vBoxPanel);
            setTotalPrice(totalPrice);
            setContinueButton(continueButton);

            updateBasketList(person);
        }
    }

    /**
     * Removes product from database by its id.
     */
    private boolean removeProductFromBasket(String hash) {
        try {
            Connection con = DBUtils.getConnection();
            String sql = "DELETE FROM basket WHERE hash = ?";

            PreparedStatement preparedStatement = null;

            try {
                preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, hash);
                preparedStatement.executeUpdate();
                System.out.println("Product deleted.");
                return true;
            } catch (Exception e) {
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE,
                        "Error while removing at removeProductFromBasket(), at ViewBasketScreenController", e);
                return false;
            }


        } catch (SQLException e) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE,
                    "Error at removeProductFromBasket(), at ViewBasketScreenController", e);
            return false;
        }
    }

    // Updates the basket view
    private void updateBasketList(PeopleTable user) {

        // change decimal separator to ','
        DecimalFormat decimalFormat = new DecimalFormat("#####0.00");
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        decimalFormat.setDecimalFormatSymbols(symbols);

        getvBoxPanel().getChildren().clear(); // clear the view
        basketList.clear(); // clear the list

        getPriceAndCounter(user); // get the counter and total price values

        productCounterLabel.setText(getTotalCounter() + " Ürün");
        totalPriceLabel.setText(decimalFormat.format(getTotalPrice()) + " ₺");

        if (getTotalPrice() == 0) {
            continueButton.setVisible(false);
        }

        try {
            Connection connection = DBUtils.getConnection();
            String sql = "SELECT * FROM basket WHERE username = '" + user.getUsername() + "'";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            PeopleTable person = new PeopleTable();
            person.setUsername(user.getUsername());

            // store all data in basketList
            while (rs.next()) {

                // convert blob to byte array
                Blob blob = rs.getBlob("img");
                int blobLength = (int) blob.length();
                byte[] blobAsBytes = blob.getBytes(1, blobLength);

                // convert byte array to image
                Image img = convertToJavaFXImage(blobAsBytes);

                // add data to basketList
                basketList.add(new BasketTable(rs.getString("username"),
                        rs.getString("hash"), rs.getInt("id"),
                        img, rs.getString("name"), rs.getDouble("price"),
                        rs.getDouble("discount"), rs.getString("seller")));

            }

            // store purchased products on array
            productsIdList = new int[basketList.size()];

            // add products to view
            for (int i = 0; i < basketList.size(); i++) {

                // get elements from fxml file by index value and fill them with values from basketList
                try {

                    // load product style
                    AnchorPane loader = FXMLLoader.load(getClass().getResource("/app/view/basketproduct.fxml"));
                    
                    // set imageView
                    ImageView imageView = (ImageView) loader.getChildren().get(0);
                    imageView.setImage(basketList.get(i).getImage());
                    centerImage(imageView);

                    // set product name
                    Label productName = (Label) loader.getChildren().get(1);
                    productName.setText(basketList.get(i).getName());

                    // set seller name
                    Label seller = (Label) loader.getChildren().get(2);
                    seller.setText(basketList.get(i).getSeller());

                    // get product price
                    Label price = (Label) loader.getChildren().get(3);
                    // get discount percentage
                    Label discount = (Label) loader.getChildren().get(4);

                    // check for discount
                    if (basketList.get(i).getDiscountPrice() > 0) {

                        price.setText(decimalFormat.format(basketList.get(i).getDiscountPrice()) + " ₺");

                        // calculate the discount
                        double percent = (basketList.get(i).getPrice() - basketList.get(i).getDiscountPrice())
                                / basketList.get(i).getPrice() * 100;
                        int p = (int) percent;

                        discount.setVisible(true);
                        discount.setText("% " + Integer.toString(p) + " indirim");
                        discount.setTextFill(Color.web("#ff7b00"));
                    } else {
                        price.setText(decimalFormat.format(basketList.get(i).getPrice()) + " ₺");
                    }

                    // set the hash label (hidden)
                    Label hashLabel = (Label) loader.getChildren().get(6); // 5 - is 'Sil" button
                    hashLabel.setText((basketList.get(i).getHash()));

                    // set the username label (hidden)
                    Label usernameLabel = (Label) loader.getChildren().get(7);
                    usernameLabel.setText((basketList.get(i).getUsername()));

                    // set the id label (hidden)
                    Label idLabel = (Label) loader.getChildren().get(8);
                    idLabel.setText(String.valueOf(basketList.get(i).getId()));

                    getvBoxPanel().getChildren().add(loader);

                    productsIdList[i] = basketList.get(i).getId();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            // Return array from this function
            setProductsIdList(productsIdList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates total price and total piece of products
     * of the current @param user's basket.
     */
    private void getPriceAndCounter(PeopleTable user) {
        try {
            Connection connection = DBUtils.getConnection();
            String sql = "SELECT * FROM basket WHERE username = '" + user.getUsername() + "'";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            double total = 0;
            int counter = 0;
            while (rs.next()) {
                if (rs.getDouble("discount") > 0) {
                    total += rs.getDouble("discount");
                } else {
                    total += rs.getDouble("price");
                }
                counter++;
            }
            total = (double) Math.round(total * 100000d) / 100000d;

            setTotalPrice(total);
            setTotalCounter(counter);

        } catch (SQLException e) {
            e.printStackTrace();
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
            ex.printStackTrace();
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

    public Label getProductCounterLabel() {
        return productCounterLabel;
    }

    public void setProductCounterLabel(Label productCounterLabel) {
        this.productCounterLabel = productCounterLabel;
    }

    public Label getTotalPriceLabel() {
        return totalPriceLabel;
    }

    public void setTotalPriceLabel(Label totalPriceLabel) {
        this.totalPriceLabel = totalPriceLabel;
    }

    public VBox getvBoxPanel() {
        return vBoxPanel;
    }

    public void setvBoxPanel(VBox vBoxPanel) {
        this.vBoxPanel = vBoxPanel;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalCounter() {
        return totalCounter;
    }

    public void setTotalCounter(int totalCounter) {
        this.totalCounter = totalCounter;
    }

    public Button getContinueButton() {
        return continueButton;
    }

    public void setContinueButton(Button continueButton) {
        this.continueButton = continueButton;
    }

    public int[] getProductsIdList() {
        return productsIdList;
    }

    public void setProductsIdList(int[] productsIdList) {
        this.productsIdList = productsIdList;
    }
}
