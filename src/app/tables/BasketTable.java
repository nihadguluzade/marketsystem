package app.tables;

import javafx.scene.image.Image;

/**
 * Class for 'basket' table in savt database
 */

public class BasketTable {

    private String username;
    private String hash;
    private int id;
    private Image image;
    private String name;
    private double price;
    private double discountPrice;
    private String seller;

    public BasketTable() {}

    public BasketTable(String username, String hash, int id, Image image, String name, double price, double discountPrice, String seller) {
        this.username = username;
        this.hash = hash;
        this.id = id;
        this.image = image;
        this.name = name;
        this.price = price;
        this.discountPrice = discountPrice;
        this.seller = seller;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }
}
