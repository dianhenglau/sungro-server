package sungro.server.db;

import sungro.server.PasswordEncoder;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Migrator {
    private final Database database;

    public Migrator(Database database) {
        this.database = database;
    }

    public void migrate() throws SQLException {
        try (Connection connection = database.getConnection()) {
            ArrayList<String> migrations = getMigrations(connection);

            if (migrations.size() < 1 || !migrations.get(0).equals("m00_create_db")) {
                System.out.println("Running m00_create_db...");
                m00_create_db(connection);
                System.out.println("Done");
            }
        }
    }

    private ArrayList<String> getMigrations(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet row = statement.executeQuery(
                "select 1 from sqlite_master where type = 'table' and name = 'Migrations'"
        );

        // If table "Migrations" doesn't exist
        if (!row.next()) {
            statement.executeUpdate("create table Migrations (Name text primary key not null)");

            return new ArrayList<>();
        }

        row = statement.executeQuery("select Name from Migrations");

        ArrayList<String> migrations = new ArrayList<>();
        while (row.next()) {
            migrations.add(row.getString(1));
        }

        return migrations;
    }

    private void m00_create_db(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        statement.executeUpdate("drop table if exists Users");

        statement.executeUpdate(
                "create table Users (" +
                        "UserID integer primary key not null, " +
                        "FirstName text not null, " +
                        "LastName text not null, " +
                        "Email text unique not null, " +
                        "IDNumber text unique not null, " +
                        "IDType text not null check (IDType in ('IC', 'Passport')), " +
                        "Role text not null check (Role in ('Admin', 'Sales Executive')), " +
                        "PwHash text not null, " +
                        "ProfilePic blob not null, " +
                        "Status text not null check (Status in ('Active', 'Inactive')), " +
                        "CreatedBy integer references Users (UserID) on delete restrict on update restrict, " +
                        "CreatedOn text not null" +
                        ")"
        );

        PreparedStatement preparedStatement = connection.prepareStatement(
                "insert into Users (" +
                        "FirstName, LastName, Email, IDNumber, IDType, Role, PwHash, ProfilePic, Status, " +
                        "CreatedBy, CreatedOn" +
                        ")" +
                        "values (" +
                        "'Administrator', '', 'Administrator', '', 'IC', 'Admin', ?, '', 'Active', " +
                        "null, ?" +
                        ")"
        );

        // PwHash
        preparedStatement.setString(1, PasswordEncoder.encode("admin"));
        // CreatedOn
        preparedStatement.setString(2, LocalDateTime.now().toString());

        preparedStatement.executeUpdate();

        statement.executeUpdate("insert into Migrations (Name) values ('m00_create_db')");
    }
}
