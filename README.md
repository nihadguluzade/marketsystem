# Market System

Market System (Alışveriş sistemi), desktop application coded in Java, using JavaFX library and database to store information where various types of users can purchase and order different products from this application. The project consists of several modules, one of the important ones is the type of users who "will" use this app. In the process of designing the project, it was decided to have 2 types of users: Moderators and Normal Users. Normal users (the person who is using the system) have the ability to choose from various products with the benefit of searching or viewing them by their category, whereas Moderators have much more control over the program.

### Moderators can:
  * Add products to market
  * Remove products from market
  * Change the price of the products
  * Add sale to the products
  * Order the product (like normal users)

All the information about **users**, **products**, **baskets**, **orders** are stored on the database.

### Compile
To manually compile the program make sure to include javafx library into the project and be sure to import the databases to your localhost. The connection link to database is:

```java
public static Connection getConnection() throws SQLException {
        return DriverManager
                .getConnection("jdbc:mysql://localhost/savt?useUnicode=yes&characterEncoding=UTF-8",
                        "root", "");
    }
```

It is important to note that the content of the program is constructed in Turkish language. Jar extension of the whole program will be uploaded soon.

*Some of the screenshots from the program:*

![Login Screen](https://raw.githubusercontent.com/nihadguluzade/marketsystem/master/images/marketss1.png)
Login Screen

![Main Screen](https://raw.githubusercontent.com/nihadguluzade/marketsystem/master/images/marketss5.png)
Main Shop Screen

![Basket Screen](https://raw.githubusercontent.com/nihadguluzade/marketsystem/master/images/marketss3.png)
Basket View

![Order Screen](https://raw.githubusercontent.com/nihadguluzade/marketsystem/master/images/marketss4.png)
Order Page
