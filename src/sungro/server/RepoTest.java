package sungro.server;

import sungro.api.*;
import sungro.server.db.Database;
import sungro.server.db.Migrator;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;

public class RepoTest {
    private final Repo repo;

    public RepoTest(Repo repo) {
        this.repo = repo;
    }

    public static void main(String[] args) throws SQLException, IOException {
        Files.deleteIfExists(Path.of("test.db"));

        System.out.println("Connecting to database...");
        Database database = new Database("jdbc:sqlite:test.db");
        System.out.println("Database connected");

        System.out.println("Migrating database...");
        Migrator migrator = new Migrator(database);
        migrator.migrate();
        System.out.println("Database migrated");

        Repo repo = new Repo(database);
        RepoTest repoTest = new RepoTest(repo);

        repoTest.setUp();

        repoTest.testGetManyUsers();
        repoTest.testGetOneUser();
        repoTest.testAddUser();
        repoTest.testDeleteUser();
        repoTest.testSetUser();

        repoTest.testGetCurrentUser();
        repoTest.testLogin();
        repoTest.testLogout();

        repoTest.testGetManyProducts();
        repoTest.testGetOneProduct();
        repoTest.testAddProduct();
        repoTest.testDeleteProduct();
        repoTest.testSetProduct();

        repoTest.testGetManyStock();
        repoTest.testGetOneStock();
        repoTest.testGetManyStockTrx();
        repoTest.testAddStock();
        repoTest.testDeleteStock();
        repoTest.testSetStock();

        repoTest.testGetManySales();
        repoTest.testAddSale();
        repoTest.testDeleteSale();

        System.out.println("All tests in RepoTest passed");
    }

    private static void shouldBeTrue(boolean value) {
        if (!value) {
            throw new RuntimeException();
        }
    }

    public void setUp() throws IOException {
        String sessionId = "0123456789abcdef";

        ParamForAddUser param = new ParamForAddUser();
        param.setSessionId(sessionId);

        param.setFirstName("Jenine");
        param.setLastName("Lamberti");
        param.setEmail("jenine@example.com");
        param.setIdNumber("985710358912");
        param.setIdType("IC");
        param.setRole("Admin");
        param.setPassword("jenine123");
        param.setStatus("Active");
        repo.addUser(param);

        param.setFirstName("Josie");
        param.setLastName("Gartner");
        param.setEmail("josie@example.com");
        param.setIdNumber("58391537951");
        param.setIdType("IC");
        param.setRole("Sales Executive");
        param.setPassword("josie123");
        param.setStatus("Active");
        repo.addUser(param);

        param.setFirstName("Amina");
        param.setLastName("Yin");
        param.setEmail("amina@example.com");
        param.setIdNumber("98325798325");
        param.setIdType("IC");
        param.setRole("Sales Executive");
        param.setPassword("amina123");
        param.setStatus("Active");
        repo.addUser(param);

        param.setFirstName("Ward");
        param.setLastName("Bulow");
        param.setEmail("ward@example.com");
        param.setIdNumber("938751392857");
        param.setIdType("IC");
        param.setRole("Admin");
        param.setPassword("ward123");
        param.setStatus("Active");
        repo.addUser(param);

        param.setFirstName("Buck");
        param.setLastName("Richarson");
        param.setEmail("buck@example.com");
        param.setIdNumber("935793825123");
        param.setIdType("IC");
        param.setRole("Admin");
        param.setPassword("buck123");
        param.setStatus("Active");
        repo.addUser(param);

        param.setFirstName("Freeman");
        param.setLastName("Steinert");
        param.setEmail("freeman@example.com");
        param.setIdNumber("95382593815");
        param.setIdType("Passport");
        param.setRole("Admin");
        param.setPassword("freeman123");
        param.setStatus("Active");
        repo.addUser(param);

        param.setFirstName("Martha");
        param.setLastName("Sever");
        param.setEmail("martha@example.com");
        param.setIdNumber("93158395011");
        param.setIdType("IC");
        param.setRole("Admin");
        param.setPassword("martha123");
        param.setStatus("Active");
        repo.addUser(param);

        param.setFirstName("Estrella");
        param.setLastName("Borkowski");
        param.setEmail("estrella@example.com");
        param.setIdNumber("391587391501");
        param.setIdType("IC");
        param.setRole("Sales Executive");
        param.setPassword("estrella123");
        param.setStatus("Inactive");
        repo.addUser(param);

        param.setFirstName("Elenore");
        param.setLastName("Jessie");
        param.setEmail("elenore@example.com");
        param.setIdNumber("57315385913");
        param.setIdType("Passport");
        param.setRole("Sales Executive");
        param.setPassword("elenore123");
        param.setStatus("Inactive");
        repo.addUser(param);

        param.setFirstName("Vinita");
        param.setLastName("Fults");
        param.setEmail("vinita@example.com");
        param.setIdNumber("95837589723");
        param.setIdType("Passport");
        param.setRole("Sales Executive");
        param.setPassword("vinita123");
        param.setStatus("Active");
        repo.addUser(param);

        ParamForAddProduct param1 = new ParamForAddProduct();
        param1.setSessionId(sessionId);

        param1.setName("Cadbury");
        param1.setCategory("Chocolate");
        param1.setProductPrice(BigDecimal.valueOf(798, 2));
        param1.setStatus("Available");
        repo.addProduct(param1);

        param1.setName("Toblerone");
        param1.setCategory("Chocolate");
        param1.setProductPrice(BigDecimal.valueOf(898, 2));
        param1.setStatus("Available");
        repo.addProduct(param1);

        param1.setName("Kinder");
        param1.setCategory("Chocolate");
        param1.setProductPrice(BigDecimal.valueOf(898, 2));
        param1.setStatus("Available");
        repo.addProduct(param1);

        param1.setName("Knife");
        param1.setCategory("Cooking Oil");
        param1.setProductPrice(BigDecimal.valueOf(3145, 2));
        param1.setStatus("Available");
        repo.addProduct(param1);

        param1.setName("Natural Blend");
        param1.setCategory("Cooking Oil");
        param1.setProductPrice(BigDecimal.valueOf(2689, 2));
        param1.setStatus("Available");
        repo.addProduct(param1);

        param1.setName("Saji");
        param1.setCategory("Cooking Oil");
        param1.setProductPrice(BigDecimal.valueOf(2598, 2));
        param1.setStatus("Disabled");
        repo.addProduct(param1);

        ParamForAddStock param2 = new ParamForAddStock();
        param2.setSessionId(sessionId);

        param2.setProductId(1);
        param2.setQuantity(15);
        param2.setExpiryDate(LocalDate.of(2021, 3, 31));
        String sku1 = repo.addStock(param2).getNewSku();

        param2.setProductId(3);
        param2.setQuantity(30);
        param2.setExpiryDate(LocalDate.of(2021, 3, 31));
        repo.addStock(param2);

        param2.setProductId(4);
        param2.setQuantity(45);
        param2.setExpiryDate(LocalDate.of(2021, 3, 31));
        String sku4 = repo.addStock(param2).getNewSku();

        param2.setProductId(5);
        param2.setQuantity(55);
        param2.setExpiryDate(LocalDate.of(2021, 3, 31));
        String sku5 = repo.addStock(param2).getNewSku();

        param2.setProductId(6);
        param2.setQuantity(65);
        param2.setExpiryDate(LocalDate.of(2021, 3, 31));
        String sku6 = repo.addStock(param2).getNewSku();

        ParamForAddSale param3 = new ParamForAddSale();
        param3.setSessionId(sessionId);

        param3.setSku(sku1);
        param3.setSoldQuantity(5);
        repo.addSale(param3);

        param3.setSku(sku4);
        param3.setSoldQuantity(5);
        repo.addSale(param3);

        param3.setSku(sku5);
        param3.setSoldQuantity(5);
        repo.addSale(param3);

        param3.setSku(sku6);
        param3.setSoldQuantity(5);
        repo.addSale(param3);
    }

    public void testGetManyUsers() {
        System.out.print("Running testGetManyUsers... ");

        try {
            ParamForGetManyUsers param = new ParamForGetManyUsers();
            param.setSessionId("0123456789abcdef");

            ResultForGetManyUsers result = repo.getManyUsers(param);
            shouldBeTrue(result.getStatus() == ResultForGetManyUsers.Status.SUCCESS);
            shouldBeTrue(result.getUsers().size() == 11);
            shouldBeTrue(result.getUsers().get(0).getUserId() == 1);

            shouldBeTrue(result.getUsers().get(0).getEmail().equals("Administrator"));
            shouldBeTrue(result.getUsers().get(1).getEmail().equals("jenine@example.com"));
            shouldBeTrue(result.getUsers().get(2).getEmail().equals("josie@example.com"));
            shouldBeTrue(result.getUsers().get(3).getEmail().equals("amina@example.com"));
            shouldBeTrue(result.getUsers().get(4).getEmail().equals("ward@example.com"));
            shouldBeTrue(result.getUsers().get(5).getEmail().equals("buck@example.com"));
            shouldBeTrue(result.getUsers().get(6).getEmail().equals("freeman@example.com"));
            shouldBeTrue(result.getUsers().get(7).getEmail().equals("martha@example.com"));
            shouldBeTrue(result.getUsers().get(8).getEmail().equals("estrella@example.com"));
            shouldBeTrue(result.getUsers().get(9).getEmail().equals("elenore@example.com"));
            shouldBeTrue(result.getUsers().get(10).getEmail().equals("vinita@example.com"));

            for (User u : result.getUsers()) {
                shouldBeTrue(u.getCreatedByUserId() == 1);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    public void testGetOneUser() {
        System.out.print("Running testGetOneUser... ");

        try {
            ParamForGetOneUser param = new ParamForGetOneUser();
            param.setSessionId("0123456789abcdef");
            param.setUserId(11);

            ResultForGetOneUser result = repo.getOneUser(param);
            shouldBeTrue(result.getStatus() == ResultForGetOneUser.Status.SUCCESS);

            shouldBeTrue(result.getUser().getUserId() == 11);
            shouldBeTrue(result.getUser().getFirstName().equals("Vinita"));
            shouldBeTrue(result.getUser().getLastName().equals("Fults"));
            shouldBeTrue(result.getUser().getEmail().equals("vinita@example.com"));
            shouldBeTrue(result.getUser().getIdNumber().equals("95837589723"));
            shouldBeTrue(result.getUser().getIdType().equals("Passport"));
            shouldBeTrue(result.getUser().getRole().equals("Sales Executive"));
            shouldBeTrue(result.getUser().getStatus().equals("Active"));

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    public void testAddUser() {
        System.out.print("Running testAddUser... ");

        try {
            String sessionId = "0123456789abcdef";

            ParamForAddUser param = new ParamForAddUser();
            param.setSessionId(sessionId);
            param.setFirstName("Vratislav");
            param.setLastName("Zieliński");
            param.setEmail("vratislav@example.com");
            param.setIdNumber("98327592357");
            param.setIdType("IC");
            param.setRole("Sales Executive");
            param.setPassword("vratislav123");
            param.setStatus("Active");

            ResultForAddUser result = repo.addUser(param);
            shouldBeTrue(result.getStatus() == ResultForAddUser.Status.SUCCESS);
            int newUserId = result.getNewUserId();

            ParamForGetOneUser param1 = new ParamForGetOneUser();
            param1.setSessionId(sessionId);
            param1.setUserId(newUserId);

            ResultForGetOneUser result1 = repo.getOneUser(param1);
            shouldBeTrue(result1.getUser().getUserId() == 12);
            shouldBeTrue(result1.getUser().getFirstName().equals("Vratislav"));
            shouldBeTrue(result1.getUser().getLastName().equals("Zieliński"));
            shouldBeTrue(result1.getUser().getEmail().equals("vratislav@example.com"));
            shouldBeTrue(result1.getUser().getIdNumber().equals("98327592357"));
            shouldBeTrue(result1.getUser().getIdType().equals("IC"));
            shouldBeTrue(result1.getUser().getRole().equals("Sales Executive"));
            shouldBeTrue(result1.getUser().getStatus().equals("Active"));

            ParamForGetManyUsers param2 = new ParamForGetManyUsers();
            param2.setSessionId(sessionId);

            ResultForGetManyUsers result2 = repo.getManyUsers(param2);
            shouldBeTrue(result2.getUsers().size() == 12);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testDeleteUser() {
        System.out.print("Running testDeleteUser... ");

        try {
            String sessionId = "0123456789abcdef";

            ParamForDeleteUser param = new ParamForDeleteUser();
            param.setSessionId(sessionId);
            param.setUserId(2);

            ResultForDeleteUser result = repo.deleteUser(param);
            shouldBeTrue(result.getStatus() == ResultForDeleteUser.Status.SUCCESS);

            ParamForGetOneUser param1 = new ParamForGetOneUser();
            param1.setSessionId(sessionId);
            param1.setUserId(2);

            ResultForGetOneUser result1 = repo.getOneUser(param1);
            shouldBeTrue(result1.getStatus() == ResultForGetOneUser.Status.NOT_FOUND);

            ParamForGetManyUsers param2 = new ParamForGetManyUsers();
            param2.setSessionId(sessionId);

            ResultForGetManyUsers result2 = repo.getManyUsers(param2);
            shouldBeTrue(result2.getStatus() == ResultForGetManyUsers.Status.SUCCESS);
            shouldBeTrue(result2.getUsers().size() == 11);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testSetUser() {
        System.out.print("Running testSetUser... ");

        try {
            String sessionId = "0123456789abcdef";

            ParamForGetOneUser param = new ParamForGetOneUser();
            param.setSessionId(sessionId);
            param.setUserId(3);

            ResultForGetOneUser result = repo.getOneUser(param);
            shouldBeTrue(result.getStatus() == ResultForGetOneUser.Status.SUCCESS);
            shouldBeTrue(result.getUser().getStatus().equals("Active"));

            ParamForSetUser param1 = new ParamForSetUser();
            param1.setSessionId(sessionId);
            param1.setUserId(result.getUser().getUserId());
            param1.setFirstName(result.getUser().getFirstName());
            param1.setLastName(result.getUser().getLastName());
            param1.setEmail(result.getUser().getEmail());
            param1.setIdNumber(result.getUser().getIdNumber());
            param1.setIdType(result.getUser().getIdType());
            param1.setRole(result.getUser().getRole());
            param1.setStatus("Inactive");

            ResultForSetUser result1 = repo.setUser(param1);
            shouldBeTrue(result1.getStatus() == ResultForSetUser.Status.SUCCESS);

            ResultForGetOneUser result2 = repo.getOneUser(param);
            shouldBeTrue(result2.getStatus() == ResultForGetOneUser.Status.SUCCESS);
            shouldBeTrue(result2.getUser().getFirstName().equals(result.getUser().getFirstName()));
            shouldBeTrue(result2.getUser().getLastName().equals(result.getUser().getLastName()));
            shouldBeTrue(result2.getUser().getEmail().equals(result.getUser().getEmail()));
            shouldBeTrue(result2.getUser().getIdNumber().equals(result.getUser().getIdNumber()));
            shouldBeTrue(result2.getUser().getIdType().equals(result.getUser().getIdType()));
            shouldBeTrue(result2.getUser().getRole().equals(result.getUser().getRole()));
            shouldBeTrue(result2.getUser().getStatus().equals("Inactive"));

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testGetCurrentUser() {
        System.out.print("Running testGetCurrentUser... ");

        try {
            ParamForGetCurrentUser param = new ParamForGetCurrentUser();
            param.setSessionId("0123456789abcdef");

            ResultForGetCurrentUser result = repo.getCurrentUser(param);
            shouldBeTrue(result.getStatus() == ResultForGetCurrentUser.Status.SUCCESS);

            shouldBeTrue(result.getUser().getUserId() == 1);
            shouldBeTrue(result.getUser().getFirstName().equals("Administrator"));
            shouldBeTrue(result.getUser().getLastName().equals(""));
            shouldBeTrue(result.getUser().getEmail().equals("Administrator"));
            shouldBeTrue(result.getUser().getIdNumber().equals(""));
            shouldBeTrue(result.getUser().getIdType().equals("IC"));
            shouldBeTrue(result.getUser().getRole().equals("Admin"));
            shouldBeTrue(result.getUser().getStatus().equals("Active"));

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testLogin() {
        System.out.print("Running testLogin... ");

        try {
            ParamForLogin param = new ParamForLogin();
            param.setEmail("amina@example.com");
            param.setPassword("amina123");

            ResultForLogin result = repo.login(param);
            shouldBeTrue(result.getStatus() == ResultForLogin.Status.SUCCESS);

            shouldBeTrue(result.getUser().getUserId() == 4);
            shouldBeTrue(result.getUser().getFirstName().equals("Amina"));
            shouldBeTrue(result.getUser().getLastName().equals("Yin"));
            shouldBeTrue(result.getUser().getEmail().equals("amina@example.com"));
            shouldBeTrue(result.getUser().getIdNumber().equals("98325798325"));
            shouldBeTrue(result.getUser().getIdType().equals("IC"));
            shouldBeTrue(result.getUser().getRole().equals("Sales Executive"));
            shouldBeTrue(result.getUser().getStatus().equals("Active"));

            ParamForGetCurrentUser param1 = new ParamForGetCurrentUser();
            param1.setSessionId(result.getSessionId());

            ResultForGetCurrentUser result1 = repo.getCurrentUser(param1);
            shouldBeTrue(result1.getUser().getUserId() == 4);
            shouldBeTrue(result1.getUser().getFirstName().equals("Amina"));
            shouldBeTrue(result1.getUser().getLastName().equals("Yin"));
            shouldBeTrue(result1.getUser().getEmail().equals("amina@example.com"));
            shouldBeTrue(result1.getUser().getIdNumber().equals("98325798325"));
            shouldBeTrue(result1.getUser().getIdType().equals("IC"));
            shouldBeTrue(result1.getUser().getRole().equals("Sales Executive"));
            shouldBeTrue(result1.getUser().getStatus().equals("Active"));

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testLogout() {
        System.out.print("Running testLogout... ");

        try {
            ParamForLogin param = new ParamForLogin();
            param.setEmail("amina@example.com");
            param.setPassword("amina123");

            ResultForLogin result = repo.login(param);
            shouldBeTrue(result.getStatus() == ResultForLogin.Status.SUCCESS);

            ParamForLogout param1 = new ParamForLogout();
            param1.setSessionId(result.getSessionId());

            ResultForLogout result1 = repo.logout(param1);
            shouldBeTrue(result1.getStatus() == ResultForLogout.Status.SUCCESS);

            ParamForGetCurrentUser param2 = new ParamForGetCurrentUser();
            param2.setSessionId(result.getSessionId());

            ResultForGetCurrentUser result2 = repo.getCurrentUser(param2);
            shouldBeTrue(result2.getStatus() == ResultForGetCurrentUser.Status.INVALID_SESSION_ID);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testGetManyProducts() {
        System.out.print("Running testGetManyProducts... ");

        try {
            ParamForGetManyProducts param = new ParamForGetManyProducts();
            param.setSessionId("0123456789abcdef");

            ResultForGetManyProducts result = repo.getManyProducts(param);
            shouldBeTrue(result.getStatus() == ResultForGetManyProducts.Status.SUCCESS);
            shouldBeTrue(result.getProducts().size() == 6);
            shouldBeTrue(result.getProducts().get(5).getName().equals("Cadbury"));
            shouldBeTrue(result.getProducts().get(4).getName().equals("Toblerone"));
            shouldBeTrue(result.getProducts().get(3).getName().equals("Kinder"));
            shouldBeTrue(result.getProducts().get(2).getName().equals("Knife"));
            shouldBeTrue(result.getProducts().get(1).getName().equals("Natural Blend"));
            shouldBeTrue(result.getProducts().get(0).getName().equals("Saji"));

            for (Product p : result.getProducts()) {
                shouldBeTrue(p.getCreatedByUserId() == 1);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testGetOneProduct() {
        System.out.print("Running testGetOneProduct... ");

        try {
            ParamForGetOneProduct param = new ParamForGetOneProduct();
            param.setSessionId("0123456789abcdef");
            param.setProductId(1);

            ResultForGetOneProduct result = repo.getOneProduct(param);
            shouldBeTrue(result.getStatus() == ResultForGetOneProduct.Status.SUCCESS);

            shouldBeTrue(result.getProduct().getProductId() == 1);
            shouldBeTrue(result.getProduct().getName().equals("Cadbury"));
            shouldBeTrue(result.getProduct().getCategory().equals("Chocolate"));
            shouldBeTrue(result.getProduct().getProductPrice().compareTo(BigDecimal.valueOf(798, 2)) == 0);
            shouldBeTrue(result.getProduct().getStatus().equals("Available"));

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testAddProduct() {
        System.out.print("Running testAddProduct... ");

        try {
            String sessionId = "0123456789abcdef";

            ParamForAddProduct param = new ParamForAddProduct();
            param.setSessionId(sessionId);
            param.setName("Adabi Sardines");
            param.setCategory("Canned Food");
            param.setProductPrice(BigDecimal.valueOf(729, 2));
            param.setStatus("Available");

            ResultForAddProduct result = repo.addProduct(param);
            shouldBeTrue(result.getStatus() == ResultForAddProduct.Status.SUCCESS);
            int newProductId = result.getNewProductId();

            ParamForGetOneProduct param1 = new ParamForGetOneProduct();
            param1.setSessionId(sessionId);
            param1.setProductId(newProductId);

            ResultForGetOneProduct result1 = repo.getOneProduct(param1);
            shouldBeTrue(result1.getProduct().getProductId() == 7);
            shouldBeTrue(result1.getProduct().getName().equals("Adabi Sardines"));
            shouldBeTrue(result1.getProduct().getCategory().equals("Canned Food"));
            shouldBeTrue(result1.getProduct().getProductPrice().compareTo(BigDecimal.valueOf(729, 2)) == 0);
            shouldBeTrue(result1.getProduct().getStatus().equals("Available"));

            ParamForGetManyProducts param2 = new ParamForGetManyProducts();
            param2.setSessionId(sessionId);

            ResultForGetManyProducts result2 = repo.getManyProducts(param2);
            shouldBeTrue(result2.getProducts().size() == 7);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testDeleteProduct() {
        System.out.print("Running testDeleteProduct... ");

        try {
            String sessionId = "0123456789abcdef";

            ParamForDeleteProduct param = new ParamForDeleteProduct();
            param.setSessionId(sessionId);
            param.setProductId(2);

            ResultForDeleteProduct result = repo.deleteProduct(param);
            shouldBeTrue(result.getStatus() == ResultForDeleteProduct.Status.SUCCESS);

            ParamForGetOneProduct param1 = new ParamForGetOneProduct();
            param1.setSessionId(sessionId);
            param1.setProductId(2);

            ResultForGetOneProduct result1 = repo.getOneProduct(param1);
            shouldBeTrue(result1.getStatus() == ResultForGetOneProduct.Status.NOT_FOUND);

            ParamForGetManyProducts param2 = new ParamForGetManyProducts();
            param2.setSessionId(sessionId);

            ResultForGetManyProducts result2 = repo.getManyProducts(param2);
            shouldBeTrue(result2.getStatus() == ResultForGetManyProducts.Status.SUCCESS);
            shouldBeTrue(result2.getProducts().size() == 6);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testSetProduct() {
        System.out.print("Running testSetProduct... ");

        try {
            String sessionId = "0123456789abcdef";

            ParamForGetOneProduct param = new ParamForGetOneProduct();
            param.setSessionId(sessionId);
            param.setProductId(3);

            ResultForGetOneProduct result = repo.getOneProduct(param);
            shouldBeTrue(result.getStatus() == ResultForGetOneProduct.Status.SUCCESS);
            shouldBeTrue(result.getProduct().getStatus().equals("Available"));

            ParamForSetProduct param1 = new ParamForSetProduct();
            param1.setSessionId(sessionId);
            param1.setProductId(result.getProduct().getProductId());
            param1.setName(result.getProduct().getName());
            param1.setCategory(result.getProduct().getCategory());
            param1.setProductPrice(result.getProduct().getProductPrice());
            param1.setStatus("Disabled");

            ResultForSetProduct result1 = repo.setProduct(param1);
            shouldBeTrue(result1.getStatus() == ResultForSetProduct.Status.SUCCESS);

            ResultForGetOneProduct result2 = repo.getOneProduct(param);
            shouldBeTrue(result2.getStatus() == ResultForGetOneProduct.Status.SUCCESS);
            shouldBeTrue(result2.getProduct().getProductId() == result.getProduct().getProductId());
            shouldBeTrue(result2.getProduct().getName().equals(result.getProduct().getName()));
            shouldBeTrue(result2.getProduct().getCategory().equals(result.getProduct().getCategory()));
            shouldBeTrue(result2.getProduct().getProductPrice().compareTo(result.getProduct().getProductPrice()) == 0);
            shouldBeTrue(result2.getProduct().getStatus().equals("Disabled"));

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testGetManyStock() {
        System.out.print("Running testGetManyStock... ");

        try {
            ParamForGetManyStock param = new ParamForGetManyStock();
            param.setSessionId("0123456789abcdef");

            ResultForGetManyStock result = repo.getManyStock(param);
            shouldBeTrue(result.getStatus() == ResultForGetManyStock.Status.SUCCESS);
            shouldBeTrue(result.getStock().size() == 5);
            result.getStock().sort(Comparator.comparingInt(Stock::getProductId));
            shouldBeTrue(result.getStock().get(0).getProductId() == 1);
            shouldBeTrue(result.getStock().get(1).getProductId() == 3);
            shouldBeTrue(result.getStock().get(2).getProductId() == 4);
            shouldBeTrue(result.getStock().get(3).getProductId() == 5);
            shouldBeTrue(result.getStock().get(4).getProductId() == 6);

            for (Stock s : result.getStock()) {
                shouldBeTrue(s.getCreatedByUserId() == 1);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testGetOneStock() {
        System.out.print("Running testGetOneStock... ");

        try {
            ParamForGetManyStock param = new ParamForGetManyStock();
            param.setSessionId("0123456789abcdef");
            param.setProductId(4);

            ResultForGetManyStock result = repo.getManyStock(param);
            shouldBeTrue(result.getStatus() == ResultForGetManyStock.Status.SUCCESS);
            shouldBeTrue(result.getStock().size() == 1);

            ParamForGetOneStock param1 = new ParamForGetOneStock();
            param1.setSessionId("0123456789abcdef");
            param1.setSku(result.getStock().get(0).getSku());

            ResultForGetOneStock result1 = repo.getOneStock(param1);
            shouldBeTrue(result1.getStatus() == ResultForGetOneStock.Status.SUCCESS);

            shouldBeTrue(result1.getStock().getSku().equals(result.getStock().get(0).getSku()));
            shouldBeTrue(result1.getStock().getProductId() == 4);
            shouldBeTrue(result1.getStock().getProductName().equals("Knife"));
            shouldBeTrue(result1.getStock().getProductCategory().equals("Cooking Oil"));
            shouldBeTrue(result1.getStock().getProductPrice().compareTo(BigDecimal.valueOf(3145, 2)) == 0);
            shouldBeTrue(result1.getStock().getQuantity() == 40);
            shouldBeTrue(result1.getStock().getExpiryDate().isEqual(LocalDate.of(2021, 3, 31)));

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testGetManyStockTrx() {
        System.out.print("Running testGetManyStockTrx... ");

        try {
            ParamForGetManyStock param = new ParamForGetManyStock();
            param.setSessionId("0123456789abcdef");
            param.setProductId(4);

            ResultForGetManyStock result = repo.getManyStock(param);
            shouldBeTrue(result.getStatus() == ResultForGetManyStock.Status.SUCCESS);
            shouldBeTrue(result.getStock().size() == 1);

            ParamForGetManyStockTrx param1 = new ParamForGetManyStockTrx();
            param1.setSessionId("0123456789abcdef");
            param1.setSku(result.getStock().get(0).getSku());

            ResultForGetManyStockTrx result1 = repo.getManyStockTrx(param1);
            shouldBeTrue(result1.getStatus() == ResultForGetManyStockTrx.Status.SUCCESS);
            shouldBeTrue(result1.getStockTrx().size() == 2);

            shouldBeTrue(result1.getStockTrx().get(0).getSku().equals(result.getStock().get(0).getSku()));
            shouldBeTrue(result1.getStockTrx().get(0).getQuantityVaried() == -5);
            shouldBeTrue(result1.getStockTrx().get(1).getSku().equals(result.getStock().get(0).getSku()));
            shouldBeTrue(result1.getStockTrx().get(1).getQuantityVaried() == result.getStock().get(0).getQuantity() + 5);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testAddStock() {
        System.out.print("Running testAddStock... ");

        try {
            String sessionId = "0123456789abcdef";

            ParamForAddStock param = new ParamForAddStock();
            param.setSessionId(sessionId);
            param.setProductId(5);
            param.setQuantity(50);
            param.setExpiryDate(LocalDate.of(2021, 4, 30));

            ResultForAddStock result = repo.addStock(param);
            shouldBeTrue(result.getStatus() == ResultForAddStock.Status.SUCCESS);
            String newSku = result.getNewSku();

            ParamForGetOneStock param1 = new ParamForGetOneStock();
            param1.setSessionId(sessionId);
            param1.setSku(newSku);

            ResultForGetOneStock result1 = repo.getOneStock(param1);
            shouldBeTrue(result1.getStock().getSku().equals(newSku));
            shouldBeTrue(result1.getStock().getProductId() == 5);
            shouldBeTrue(result1.getStock().getProductName().equals("Natural Blend"));
            shouldBeTrue(result1.getStock().getProductCategory().equals("Cooking Oil"));
            shouldBeTrue(result1.getStock().getProductPrice().compareTo(BigDecimal.valueOf(2689, 2)) == 0);
            shouldBeTrue(result1.getStock().getQuantity() == 50);
            shouldBeTrue(result1.getStock().getExpiryDate().isEqual(LocalDate.of(2021, 4, 30)));

            ParamForGetManyStock param2 = new ParamForGetManyStock();
            param2.setSessionId(sessionId);

            ResultForGetManyStock result2 = repo.getManyStock(param2);
            shouldBeTrue(result2.getStock().size() == 6);

            ParamForGetManyStockTrx param3 = new ParamForGetManyStockTrx();
            param3.setSessionId(sessionId);
            param3.setSku(result.getNewSku());

            ResultForGetManyStockTrx result3 = repo.getManyStockTrx(param3);
            shouldBeTrue(result3.getStockTrx().size() == 1);
            shouldBeTrue(result3.getStockTrx().get(0).getSku().equals(result.getNewSku()));
            shouldBeTrue(result3.getStockTrx().get(0).getQuantityVaried() == 50);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testDeleteStock() {
        System.out.print("Running testDeleteStock... ");

        try {
            String sessionId = "0123456789abcdef";

            ParamForGetManyStock param = new ParamForGetManyStock();
            param.setSessionId(sessionId);
            param.setProductId(3);

            ResultForGetManyStock result = repo.getManyStock(param);
            shouldBeTrue(result.getStatus() == ResultForGetManyStock.Status.SUCCESS);
            shouldBeTrue(result.getStock().size() == 1);

            ParamForDeleteStock param1 = new ParamForDeleteStock();
            param1.setSessionId(sessionId);
            param1.setSku(result.getStock().get(0).getSku());

            ResultForDeleteStock result1 = repo.deleteStock(param1);
            shouldBeTrue(result1.getStatus() == ResultForDeleteStock.Status.SUCCESS);

            ParamForGetOneStock param2 = new ParamForGetOneStock();
            param2.setSessionId(sessionId);
            param2.setSku(result.getStock().get(0).getSku());

            ResultForGetOneStock result2 = repo.getOneStock(param2);
            shouldBeTrue(result2.getStatus() == ResultForGetOneStock.Status.NOT_FOUND);

            ParamForGetManyStock param3 = new ParamForGetManyStock();
            param3.setSessionId(sessionId);

            ResultForGetManyStock result3 = repo.getManyStock(param3);
            shouldBeTrue(result3.getStatus() == ResultForGetManyStock.Status.SUCCESS);
            shouldBeTrue(result3.getStock().size() == 5);

            ParamForGetManyStockTrx param4 = new ParamForGetManyStockTrx();
            param4.setSessionId(sessionId);
            param4.setSku(result.getStock().get(0).getSku());

            ResultForGetManyStockTrx result4 = repo.getManyStockTrx(param4);
            shouldBeTrue(result4.getStatus() == ResultForGetManyStockTrx.Status.NOT_FOUND);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testSetStock() {
        System.out.print("Running testSetStock... ");

        try {
            String sessionId = "0123456789abcdef";

            ParamForGetManyStock param = new ParamForGetManyStock();
            param.setSessionId(sessionId);
            param.setProductId(6);

            ResultForGetManyStock result = repo.getManyStock(param);
            shouldBeTrue(result.getStatus() == ResultForGetManyStock.Status.SUCCESS);
            shouldBeTrue(result.getStock().size() == 1);

            ParamForSetStock param1 = new ParamForSetStock();
            param1.setSessionId(sessionId);
            param1.setSku(result.getStock().get(0).getSku());
            param1.setQuantityVaried(6);
            param1.setRemark("Gift by vendor.");

            ResultForSetStock result1 = repo.setStock(param1);
            shouldBeTrue(result1.getStatus() == ResultForSetStock.Status.SUCCESS);

            ParamForGetOneStock param2 = new ParamForGetOneStock();
            param2.setSessionId(sessionId);
            param2.setSku(result.getStock().get(0).getSku());

            ResultForGetOneStock result2 = repo.getOneStock(param2);
            shouldBeTrue(result2.getStatus() == ResultForGetOneStock.Status.SUCCESS);
            shouldBeTrue(result2.getStock().getSku().equals(result.getStock().get(0).getSku()));
            shouldBeTrue(result2.getStock().getProductId() == 6);
            shouldBeTrue(result2.getStock().getProductName().equals("Saji"));
            shouldBeTrue(result2.getStock().getProductCategory().equals("Cooking Oil"));
            shouldBeTrue(result2.getStock().getProductPrice().compareTo(BigDecimal.valueOf(2598, 2)) == 0);
            shouldBeTrue(result2.getStock().getQuantity() == 66);
            shouldBeTrue(result2.getStock().getExpiryDate().isEqual(LocalDate.of(2021, 3, 31)));

            ParamForGetManyStockTrx param3 = new ParamForGetManyStockTrx();
            param3.setSessionId(sessionId);
            param3.setSku(result.getStock().get(0).getSku());

            ResultForGetManyStockTrx result3 = repo.getManyStockTrx(param3);
            shouldBeTrue(result3.getStatus() == ResultForGetManyStockTrx.Status.SUCCESS);
            shouldBeTrue(result3.getStockTrx().size() == 3);
            shouldBeTrue(result3.getStockTrx().get(0).getQuantityVaried() == 6);
            shouldBeTrue(result3.getStockTrx().get(1).getQuantityVaried() == -5);
            shouldBeTrue(result3.getStockTrx().get(2).getQuantityVaried() == 65);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testGetManySales() {
        System.out.print("Running testGetManySales... ");

        try {
            ParamForGetManySales param = new ParamForGetManySales();
            param.setSessionId("0123456789abcdef");

            ResultForGetManySales result = repo.getManySales(param);
            shouldBeTrue(result.getStatus() == ResultForGetManySales.Status.SUCCESS);
            shouldBeTrue(result.getSales().size() == 4);
            shouldBeTrue(result.getSales().get(3).getProductName().equals("Cadbury"));
            shouldBeTrue(result.getSales().get(2).getProductName().equals("Knife"));
            shouldBeTrue(result.getSales().get(1).getProductName().equals("Natural Blend"));
            shouldBeTrue(result.getSales().get(0).getProductName().equals("Saji"));

            for (Sale s : result.getSales()) {
                shouldBeTrue(s.getSoldByUserId() == 1);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testAddSale() {
        System.out.print("Running testAddSale... ");

        try {
            String sessionId = "0123456789abcdef";

            ParamForGetManyStock param = new ParamForGetManyStock();
            param.setSessionId(sessionId);
            param.setProductId(6);

            ResultForGetManyStock result = repo.getManyStock(param);
            shouldBeTrue(result.getStatus() == ResultForGetManyStock.Status.SUCCESS);
            shouldBeTrue(result.getStock().size() == 1);

            ParamForAddSale param1 = new ParamForAddSale();
            param1.setSessionId(sessionId);
            param1.setSku(result.getStock().get(0).getSku());
            param1.setSoldQuantity(10);

            ResultForAddSale result1 = repo.addSale(param1);
            shouldBeTrue(result1.getStatus() == ResultForAddSale.Status.SUCCESS);

            ParamForGetOneStock param2 = new ParamForGetOneStock();
            param2.setSessionId(sessionId);
            param2.setSku(result.getStock().get(0).getSku());

            ResultForGetOneStock result2 = repo.getOneStock(param2);
            shouldBeTrue(result2.getStatus() == ResultForGetOneStock.Status.SUCCESS);
            shouldBeTrue(result2.getStock().getSku().equals(result.getStock().get(0).getSku()));
            shouldBeTrue(result2.getStock().getProductId() == 6);
            shouldBeTrue(result2.getStock().getProductName().equals("Saji"));
            shouldBeTrue(result2.getStock().getProductCategory().equals("Cooking Oil"));
            shouldBeTrue(result2.getStock().getProductPrice().compareTo(BigDecimal.valueOf(2598, 2)) == 0);
            shouldBeTrue(result2.getStock().getQuantity() == 56);
            shouldBeTrue(result2.getStock().getExpiryDate().isEqual(LocalDate.of(2021, 3, 31)));

            ParamForGetManyStockTrx param3 = new ParamForGetManyStockTrx();
            param3.setSessionId(sessionId);
            param3.setSku(result.getStock().get(0).getSku());

            ResultForGetManyStockTrx result3 = repo.getManyStockTrx(param3);
            shouldBeTrue(result3.getStatus() == ResultForGetManyStockTrx.Status.SUCCESS);
            shouldBeTrue(result3.getStockTrx().size() == 4);
            shouldBeTrue(result3.getStockTrx().get(0).getQuantityVaried() == -10);
            shouldBeTrue(result3.getStockTrx().get(1).getQuantityVaried() == 6);
            shouldBeTrue(result3.getStockTrx().get(2).getQuantityVaried() == -5);
            shouldBeTrue(result3.getStockTrx().get(3).getQuantityVaried() == 65);

            ParamForGetManySales param4 = new ParamForGetManySales();
            param4.setSessionId("0123456789abcdef");

            ResultForGetManySales result4 = repo.getManySales(param4);
            shouldBeTrue(result4.getStatus() == ResultForGetManySales.Status.SUCCESS);
            shouldBeTrue(result4.getSales().size() == 5);
            shouldBeTrue(result4.getSales().get(0).getProductName().equals("Saji"));
            shouldBeTrue(result4.getSales().get(0).getSaleId() == 5);
            shouldBeTrue(result4.getSales().get(0).getSku().equals(result.getStock().get(0).getSku()));
            shouldBeTrue(result4.getSales().get(0).getProductId() == 6);
            shouldBeTrue(result4.getSales().get(0).getProductName().equals("Saji"));
            shouldBeTrue(result4.getSales().get(0).getProductCategory().equals("Cooking Oil"));
            shouldBeTrue(result4.getSales().get(0).getUnitPrice().compareTo(BigDecimal.valueOf(2598, 2)) == 0);
            shouldBeTrue(result4.getSales().get(0).getSoldQuantity() == 10);
            shouldBeTrue(result4.getSales().get(0).getSubTotalPrice().compareTo(BigDecimal.valueOf(25980, 2)) == 0);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }

    void testDeleteSale() {
        System.out.print("Running testDeleteSale... ");

        try {
            String sessionId = "0123456789abcdef";

            ParamForGetManyStock param = new ParamForGetManyStock();
            param.setSessionId(sessionId);
            param.setProductId(6);

            ResultForGetManyStock result = repo.getManyStock(param);
            shouldBeTrue(result.getStatus() == ResultForGetManyStock.Status.SUCCESS);
            shouldBeTrue(result.getStock().size() == 1);

            ParamForDeleteSale param1 = new ParamForDeleteSale();
            param1.setSessionId(sessionId);
            param1.setSaleId(5);

            ResultForDeleteSale result1 = repo.deleteSale(param1);
            shouldBeTrue(result1.getStatus() == ResultForDeleteSale.Status.SUCCESS);

            ParamForGetOneStock param2 = new ParamForGetOneStock();
            param2.setSessionId(sessionId);
            param2.setSku(result.getStock().get(0).getSku());

            ResultForGetOneStock result2 = repo.getOneStock(param2);
            shouldBeTrue(result2.getStatus() == ResultForGetOneStock.Status.SUCCESS);
            shouldBeTrue(result2.getStock().getSku().equals(result.getStock().get(0).getSku()));
            shouldBeTrue(result2.getStock().getProductId() == 6);
            shouldBeTrue(result2.getStock().getProductName().equals("Saji"));
            shouldBeTrue(result2.getStock().getProductCategory().equals("Cooking Oil"));
            shouldBeTrue(result2.getStock().getProductPrice().compareTo(BigDecimal.valueOf(2598, 2)) == 0);
            shouldBeTrue(result2.getStock().getQuantity() == 66);
            shouldBeTrue(result2.getStock().getExpiryDate().isEqual(LocalDate.of(2021, 3, 31)));

            ParamForGetManyStockTrx param3 = new ParamForGetManyStockTrx();
            param3.setSessionId(sessionId);
            param3.setSku(result.getStock().get(0).getSku());

            ResultForGetManyStockTrx result3 = repo.getManyStockTrx(param3);
            shouldBeTrue(result3.getStatus() == ResultForGetManyStockTrx.Status.SUCCESS);
            shouldBeTrue(result3.getStockTrx().size() == 3);
            shouldBeTrue(result3.getStockTrx().get(0).getQuantityVaried() == 6);
            shouldBeTrue(result3.getStockTrx().get(1).getQuantityVaried() == -5);
            shouldBeTrue(result3.getStockTrx().get(2).getQuantityVaried() == 65);

            ParamForGetManySales param4 = new ParamForGetManySales();
            param4.setSessionId("0123456789abcdef");

            ResultForGetManySales result4 = repo.getManySales(param4);
            shouldBeTrue(result4.getStatus() == ResultForGetManySales.Status.SUCCESS);
            shouldBeTrue(result4.getSales().size() == 4);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Passed");
    }
}
