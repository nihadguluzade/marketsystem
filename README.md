# Market System

Market System (Alışveriş sistemi), desktop application written in Java, using JavaFX library and database to store information where various types of users can purchase and order different products from this application. Project consists of several modules, one of the important one is the type of users who "will" use this app. In the process of designing the project, it was decided to have 2 types of users: Moderators and Normal Users. Normal users (the person who are using the system) have the ability to choose from various products with the benefit of searching or viewing them by their category, whereas Moderators have the much more control over the program.

## Moderators can:
  * Add products to market
  * Remove products from market
  * Change the price of the products
  * Add sale to the products
  * Order the product (like normal users)

All the information about **users**, **products**, **baskets**, **orders** are stored on the database.

To manually compile the program make sure to include javafx library into the project and be sure to import the databases to your localhost. The connection link to database is:

```java
public static Connection getConnection() throws SQLException {
        return DriverManager
                .getConnection("jdbc:mysql://localhost/savt?useUnicode=yes&characterEncoding=UTF-8",
                        "root", "");
    }
```

> jar extension of the whole program will be uploaded soon.
