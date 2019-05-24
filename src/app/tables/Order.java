package app.tables;

/**
 * Class to store the orders of users.
 */
public class Order {

    private String username;
    private String hash;
    private long tc;
    private String name;
    private String surname;
    private String email;
    private String city;
    private String zipcode;
    private String receiver;
    private String phone;
    private String address;
    private double price;
    private String productsId;

    public Order() {}

    public Order(String city, String zipcode, String receiver, String phone, String address, double price,
                 String productsId) {
        this.city = city;
        this.zipcode = zipcode;
        this.receiver = receiver;
        this.phone = phone;
        this.address = address;
        this.price = price;
        this.productsId = productsId;
    }

    public Order(String username, String hash, long tc, String name, String surname, String email, String city,
                 String zipcode, String receiver, String phone, String address, double price, String productsId) {
        this.username = username;
        this.hash = hash;
        this.tc = tc;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.city = city;
        this.zipcode = zipcode;
        this.receiver = receiver;
        this.phone = phone;
        this.address = address;
        this.price = price;
        this.productsId = productsId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getTc() {
        return tc;
    }

    public void setTc(long tc) {
        this.tc = tc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getProductsId() {
        return productsId;
    }

    public void setProductsId(String productsId) {
        this.productsId = productsId;
    }
}
