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

            if (migrations.size() < 2 || !migrations.get(1).equals("m01_create_sessions")) {
                System.out.println("Running m01_create_sessions...");
                m01_create_sessions(connection);
                System.out.println("Done");
            }

            if (migrations.size() < 3 || !migrations.get(2).equals("m02_create_product_stock_trx_sales")) {
                System.out.println("Running m02_create_product_stock_trx_sales...");
                m02_create_product_stock_trx_sales(connection);
                System.out.println("Done");
            }
        }
    }

    private ArrayList<String> getMigrations(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet row = statement.executeQuery(
                    "select 1 from sqlite_master where type = 'table' and name = 'Migrations'"
            )) {
                // If table "Migrations" doesn't exist
                if (!row.next()) {
                    statement.executeUpdate("create table Migrations (Name text primary key not null)");

                    return new ArrayList<>();
                }
            }

            try (ResultSet row = statement.executeQuery("select Name from Migrations")) {
                ArrayList<String> migrations = new ArrayList<>();
                while (row.next()) {
                    migrations.add(row.getString(1));
                }

                return migrations;
            }
        }
    }

    private void m00_create_db(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
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

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into Users (" +
                            "FirstName, LastName, Email, IDNumber, IDType, Role, PwHash, ProfilePic, Status, " +
                            "CreatedBy, CreatedOn" +
                            ")" +
                            "values (" +
                            "'Administrator', '', 'Administrator', '', 'IC', 'Admin', ?, '', 'Active', " +
                            "1, ?" +
                            ")"
            )) {
                // PwHash
                preparedStatement.setString(1, PasswordEncoder.encode("admin"));
                // CreatedOn
                preparedStatement.setString(2, LocalDateTime.now().toString());

                preparedStatement.executeUpdate();
            }

            statement.executeUpdate("insert into Migrations (Name) values ('m00_create_db')");
        }
    }

    private void m01_create_sessions(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate("drop table if exists Sessions");

            statement.executeUpdate(
                    "create table Sessions (" +
                            "SessionID text primary key not null, " +
                            "UserID integer references Users (UserID) on delete restrict on update restrict" +
                            ")"
            );

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into Sessions (SessionID, UserID) values (?, ?)"
            )) {
                preparedStatement.setString(1, "0123456789abcdef");
                preparedStatement.setInt(2, 1);

                preparedStatement.executeUpdate();
            }

            statement.executeUpdate("insert into Migrations (Name) values ('m01_create_sessions')");
        }
    }

    private void m02_create_product_stock_trx_sales(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("drop table if exists Products");

            statement.executeUpdate(
                    "create table Products (" +
                            "ProductID integer primary key not null, " +
                            "Name text unique not null, " +
                            "Category text not null, " +
                            "ProductPrice integer not null, " +
                            "ProductPic blob not null, " +
                            "Status text not null check (Status in ('Available', 'Disabled')), " +
                            "CreatedBy integer references Users (UserID) on delete restrict on update restrict, " +
                            "CreatedOn text not null" +
                            ")"
            );

            statement.executeUpdate("drop table if exists Stock");

            statement.executeUpdate(
                    "create table Stock (" +
                            "SKU text primary key not null, " +
                            "ProductID integer references Products (ProductID) on delete restrict on update restrict, " +
                            "ExpDate text not null, " +
                            "Quantity integer not null, " +
                            "CreatedBy integer references Users (UserID) on delete restrict on update restrict, " +
                            "CreatedOn text not null" +
                            ")"
            );

            statement.executeUpdate("drop table if exists StockTrx");

            statement.executeUpdate(
                    "create table StockTrx (" +
                            "StockTrxID integer primary key not null, " +
                            "SKU text references Stock (SKU) on delete restrict on update restrict, " +
                            "QuantityVaried integer not null, " +
                            "Remark text not null, " +
                            "CreatedBy integer references Users (UserID) on delete restrict on update restrict, " +
                            "CreatedOn text not null" +
                            ")"
            );

            statement.executeUpdate("drop table if exists Sales");

            statement.executeUpdate(
                    "create table Sales (" +
                            "SaleID integer primary key not null, " +
                            "StockTrxID integer references StockTrx (StockTrxID) on delete restrict on update restrict, " +
                            "UnitPrice integer not null, " +
                            "SoldQuantity integer not null, " +
                            "SoldBy integer references Users (UserID) on delete restrict on update restrict, " +
                            "SoldOn text not null" +
                            ")"
            );

            statement.executeUpdate("insert into Migrations (Name) values ('m02_create_product_stock_trx_sales')");
        }
    }
}
