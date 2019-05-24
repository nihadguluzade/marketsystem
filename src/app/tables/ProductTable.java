package app.tables;

import javafx.scene.image.Image;

/**
 * Class for 'products' table in savt database
 */

public class ProductTable {

    private int id;
    private Image image;
    private String name;
    private double price;
    private double discountPrice;
    private String seller;
    private String category;

    public ProductTable(int id, Image image, String name, double price, double discountPrice, String seller, String category) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.price = price;
        this.discountPrice = discountPrice;
        this.seller = seller;
        this.category = category;
    }

    public ProductTable(int id, String name, double price, String seller) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.seller = seller;
    }

    public ProductTable() {}

    @Override
    public String toString() {
        return name + ", " + seller + ", " + price + "TL";
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
