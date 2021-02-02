package sungro.server;

import sungro.server.db.Database;
import sungro.server.db.Migrator;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws RemoteException, SQLException {
        System.out.println("Connecting to database...");
        Database database = new Database("jdbc:sqlite:sample.db");
        System.out.println("Database connected");

        System.out.println("Migrating database...");
        Migrator migrator = new Migrator(database);
        migrator.migrate();
        System.out.println("Database migrated");

        System.out.println("Creating and binding Repo...");
        sungro.api.Repo repo = new Repo(database);
        sungro.api.Repo stub = (sungro.api.Repo) UnicastRemoteObject.exportObject(repo, 0);

        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("Repo", stub);
        System.out.println("Repo bound");
    }
}
