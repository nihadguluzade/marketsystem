package app.tables;

/**
 * Class for 'people' table in savt database.
 */

public class PeopleTable {

    private long tc;
    private String email;
    private String username;
    private String name;
    private String surname;
    private String password;
    private int type; // 1 - staff, 0 - guest

    public PeopleTable(long tc, String email, String username, String name, String surname, String password, int type) {
        this.tc = tc;
        this.email = email;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.type = type;
    }

    public PeopleTable() {}

    public long getTc() {
        return tc;
    }

    public void setTc(long tc) {
        this.tc = tc;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
