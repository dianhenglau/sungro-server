package sungro.server;

import sungro.api.*;
import sungro.server.db.Database;
import sungro.server.db.QueryBuilder;
import sungro.server.db.UpdateBuilder;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Repo implements sungro.api.Repo {
    private final Database database;

    public Repo(Database database) {
        this.database = database;
    }

    @Override
    public ResultForGetManyUsers getManyUsers(ParamForGetManyUsers param) throws RemoteException {
        ResultForGetManyUsers result = new ResultForGetManyUsers();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForGetManyUsers.Status.INVALID_SESSION_ID);
                return result;
            }

            if (!currentUser.getRole().equals("Admin")) {
                result.setStatus(ResultForGetManyUsers.Status.PERMISSION_DENIED);
                return result;
            }

            QueryBuilder queryData = new QueryBuilder();

            queryData.appendSelect(
                    "select " +
                            "U.UserID, " +
                            "U.FirstName, " +
                            "U.LastName, " +
                            "U.Email, " +
                            "U.IDNumber, " +
                            "U.IDType, " +
                            "U.Role, " +
                            "U.ProfilePic, " +
                            "U.Status, " +
                            "C.UserID as CreatedByUserID, " +
                            "C.FirstName || ' ' || C.LastName as CreatedByUserName, " +
                            "U.CreatedOn "
            );

            queryData.appendFrom(
                    "from Users as U " +
                            "inner join Users as C " +
                            "on C.UserID = U.CreatedBy "
            );

            if (!param.getName().isBlank()) {
                queryData.appendWhere("(U.FirstName like ? or U.LastName like ?) ");
                queryData.addStringToWhere(param.getName() + "%");
                queryData.addStringToWhere(param.getName() + "%");
            }

            if (!param.getEmail().isBlank()) {
                queryData.appendWhere("U.Email like ? ");
                queryData.addStringToWhere(param.getEmail() + "%");
            }

            if (!param.getIdNumber().isBlank()) {
                queryData.appendWhere("U.IDNumber = ? ");
                queryData.addStringToWhere(param.getIdNumber());
            }

            if (!param.getRole().isBlank()) {
                queryData.appendWhere("U.Role = ? ");
                queryData.addStringToWhere(param.getRole());
            }

            queryData.appendRemaining("order by U.UserID limit 20 offset ? ");
            queryData.addIntToRemaining((param.getPage() - 1) * 20);

            try (
                    PreparedStatement preparedStatement = queryData.prepare(connection);
                    ResultSet row = preparedStatement.executeQuery()
            ) {
                while (row.next()) {
                    User user = new User();
                    user.setUserId(row.getInt(1));
                    user.setFirstName(row.getString(2));
                    user.setLastName(row.getString(3));
                    user.setEmail(row.getString(4));
                    user.setIdNumber(row.getString(5));
                    user.setIdType(row.getString(6));
                    user.setRole(row.getString(7));
                    user.setProfilePic(row.getBytes(8));
                    user.setStatus(row.getString(9));
                    user.setCreatedByUserId(row.getInt(10));
                    user.setCreatedByUserName(row.getString(11));
                    user.setCreatedOn(LocalDateTime.parse(row.getString(12)));

                    result.getUsers().add(user);
                }
            }

            QueryBuilder queryCount = new QueryBuilder();

            queryCount.appendSelect("select count(*) ");
            queryCount.appendFrom(queryData.getFrom());
            queryCount.appendWhere(queryData.getWhere());
            queryCount.getWhereParams().addAll(queryData.getWhereParams());

            try (
                    PreparedStatement preparedStatement = queryCount.prepare(connection);
                    ResultSet row = preparedStatement.executeQuery()
            ) {
                row.next();
                int rowCount = row.getInt(1);
                result.setMaxPage((rowCount - 1) / 20 + 1);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForGetManyUsers.Status.SERVER_ERROR);
        }

        result.setCurrentPage(param.getPage());

        return result;
    }

    @Override
    public ResultForGetOneUser getOneUser(ParamForGetOneUser param) throws RemoteException {
        ResultForGetOneUser result = new ResultForGetOneUser();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForGetOneUser.Status.INVALID_SESSION_ID);
                return result;
            }

            if (!currentUser.getRole().equals("Admin")) {
                result.setStatus(ResultForGetOneUser.Status.PERMISSION_DENIED);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select " +
                            "U.UserID, " +
                            "U.FirstName, " +
                            "U.LastName, " +
                            "U.Email, " +
                            "U.IDNumber, " +
                            "U.IDType, " +
                            "U.Role, " +
                            "U.ProfilePic, " +
                            "U.Status, " +
                            "C.UserID as CreatedByUserID, " +
                            "C.FirstName || ' ' || C.LastName as CreatedByUserName, " +
                            "U.CreatedOn " +
                            "from Users as U " +
                            "inner join Users as C " +
                            "on C.UserID = U.CreatedBy " +
                            "where U.UserID = ?"
            )) {
                preparedStatement.setInt(1, param.getUserId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    if (row.next()) {
                        User user = result.getUser();
                        user.setUserId(row.getInt(1));
                        user.setFirstName(row.getString(2));
                        user.setLastName(row.getString(3));
                        user.setEmail(row.getString(4));
                        user.setIdNumber(row.getString(5));
                        user.setIdType(row.getString(6));
                        user.setRole(row.getString(7));
                        user.setProfilePic(row.getBytes(8));
                        user.setStatus(row.getString(9));
                        user.setCreatedByUserId(row.getInt(10));
                        user.setCreatedByUserName(row.getString(11));
                        user.setCreatedOn(LocalDateTime.parse(row.getString(12)));
                    } else {
                        result.setStatus(ResultForGetOneUser.Status.NOT_FOUND);
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForGetOneUser.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForAddUser addUser(ParamForAddUser param) throws RemoteException {
        ResultForAddUser result = new ResultForAddUser();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForAddUser.Status.INVALID_SESSION_ID);
                return result;
            }

            if (!currentUser.getRole().equals("Admin")) {
                result.setStatus(ResultForAddUser.Status.PERMISSION_DENIED);
                return result;
            }

            if (param.getFirstName().isBlank()) {
                result.setStatus(ResultForAddUser.Status.MISSING_FIRST_NAME);
                return result;
            }

            if (param.getLastName().isBlank()) {
                result.setStatus(ResultForAddUser.Status.MISSING_LAST_NAME);
                return result;
            }

            if (param.getEmail().isBlank()) {
                result.setStatus(ResultForAddUser.Status.MISSING_EMAIL);
                return result;
            }

            String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
            if (!param.getEmail().matches(regex)) {
                result.setStatus(ResultForAddUser.Status.INVALID_EMAIL);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Users where Email = ?)"
            )) {
                preparedStatement.setString(1, param.getEmail());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 1) {
                        result.setStatus(ResultForAddUser.Status.REPEATED_EMAIL);
                        return result;
                    }
                }
            }

            if (param.getIdNumber().isBlank()) {
                result.setStatus(ResultForAddUser.Status.MISSING_ID_NUMBER);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Users where IDNumber = ?)"
            )) {
                preparedStatement.setString(1, param.getIdNumber());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 1) {
                        result.setStatus(ResultForAddUser.Status.REPEATED_ID_NUMBER);
                        return result;
                    }
                }
            }

            if (param.getIdType().isBlank()) {
                result.setStatus(ResultForAddUser.Status.MISSING_ID_TYPE);
                return result;
            }

            if (!param.getIdType().equals("IC") && !param.getIdType().equals("Passport")) {
                result.setStatus(ResultForAddUser.Status.INVALID_ID_TYPE);
            }

            if (param.getRole().isBlank()) {
                result.setStatus(ResultForAddUser.Status.MISSING_ROLE);
                return result;
            }

            if (!param.getRole().equals("Admin") && !param.getRole().equals("Sales Executive")) {
                result.setStatus(ResultForAddUser.Status.INVALID_ROLE);
                return result;
            }

            if (param.getPassword().isBlank()) {
                result.setStatus(ResultForAddUser.Status.MISSING_PASSWORD);
                return result;
            }

            if (param.getStatus().isBlank()) {
                result.setStatus(ResultForAddUser.Status.MISSING_STATUS);
                return result;
            }

            if (!param.getStatus().equals("Active") && !param.getStatus().equals("Inactive")) {
                result.setStatus(ResultForAddUser.Status.INVALID_STATUS);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into Users ( " +
                            "FirstName, LastName, Email, IDNumber, IDType, " +
                            "Role, PwHash, ProfilePic, Status, CreatedBy, " +
                            "CreatedOn " +
                            ") " +
                            "values ( " +
                            "?, ?, ?, ?, ?, " +
                            "?, ?, ?, ?, ?, " +
                            "? " +
                            ")"
            )) {
                preparedStatement.setString(1, param.getFirstName());
                preparedStatement.setString(2, param.getLastName());
                preparedStatement.setString(3, param.getEmail());
                preparedStatement.setString(4, param.getIdNumber());
                preparedStatement.setString(5, param.getIdType());
                preparedStatement.setString(6, param.getRole());
                preparedStatement.setString(7, PasswordEncoder.encode(param.getPassword()));
                preparedStatement.setBytes(8, param.getProfilePic());
                preparedStatement.setString(9, param.getStatus());
                preparedStatement.setInt(10, currentUser.getUserId());
                preparedStatement.setString(11, LocalDateTime.now().toString());

                preparedStatement.executeUpdate();
            }

            try (Statement statement = connection.createStatement()) {
                try (ResultSet row = statement.executeQuery("select last_insert_rowid()")) {
                    row.next();
                    result.setNewUserId(row.getInt(1));
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForAddUser.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForDeleteUser deleteUser(ParamForDeleteUser param) throws RemoteException {
        ResultForDeleteUser result = new ResultForDeleteUser();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForDeleteUser.Status.INVALID_SESSION_ID);
                return result;
            }

            if (!currentUser.getRole().equals("Admin")) {
                result.setStatus(ResultForDeleteUser.Status.PERMISSION_DENIED);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Users where UserID = ?)"
            )) {
                preparedStatement.setInt(1, param.getUserId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 0) {
                        result.setStatus(ResultForDeleteUser.Status.NOT_FOUND);
                        return result;
                    }
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Users where CreatedBy = ?)"
            )) {
                preparedStatement.setInt(1, param.getUserId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 1) {
                        result.setStatus(ResultForDeleteUser.Status.DEPENDED);
                        return result;
                    }
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "delete from Users where UserID = ?"
            )) {
                preparedStatement.setInt(1, param.getUserId());

                preparedStatement.executeUpdate();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForDeleteUser.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForSetUser setUser(ParamForSetUser param) throws RemoteException {
        ResultForSetUser result = new ResultForSetUser();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForSetUser.Status.INVALID_SESSION_ID);
                return result;
            }

            if (!currentUser.getRole().equals("Admin") && currentUser.getUserId() != param.getUserId()) {
                result.setStatus(ResultForSetUser.Status.PERMISSION_DENIED);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Users where UserID = ?)"
            )) {
                preparedStatement.setInt(1, param.getUserId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 0) {
                        result.setStatus(ResultForSetUser.Status.NOT_FOUND);
                        return result;
                    }
                }
            }

            if (param.getFirstName().isBlank()) {
                result.setStatus(ResultForSetUser.Status.MISSING_FIRST_NAME);
                return result;
            }

            if (param.getLastName().isBlank()) {
                result.setStatus(ResultForSetUser.Status.MISSING_LAST_NAME);
                return result;
            }

            if (param.getEmail().isBlank()) {
                result.setStatus(ResultForSetUser.Status.MISSING_EMAIL);
                return result;
            }

            String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
            if (!param.getEmail().matches(regex)) {
                result.setStatus(ResultForSetUser.Status.INVALID_EMAIL);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Users where Email = ? and UserID <> ?)"
            )) {
                preparedStatement.setString(1, param.getEmail());
                preparedStatement.setInt(2, param.getUserId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 1) {
                        result.setStatus(ResultForSetUser.Status.REPEATED_EMAIL);
                        return result;
                    }
                }
            }

            if (param.getIdNumber().isBlank()) {
                result.setStatus(ResultForSetUser.Status.MISSING_ID_NUMBER);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Users where IDNumber = ? and UserID <> ?)"
            )) {
                preparedStatement.setString(1, param.getIdNumber());
                preparedStatement.setInt(2, param.getUserId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 1) {
                        result.setStatus(ResultForSetUser.Status.REPEATED_ID_NUMBER);
                        return result;
                    }
                }
            }

            if (param.getIdType().isBlank()) {
                result.setStatus(ResultForSetUser.Status.MISSING_ID_TYPE);
                return result;
            }

            if (!param.getIdType().equals("IC") && !param.getIdType().equals("Passport")) {
                result.setStatus(ResultForSetUser.Status.INVALID_ID_TYPE);
            }

            if (param.getRole().isBlank()) {
                result.setStatus(ResultForSetUser.Status.MISSING_ROLE);
                return result;
            }

            if (!param.getRole().equals("Admin") && !param.getRole().equals("Sales Executive")) {
                result.setStatus(ResultForSetUser.Status.INVALID_ROLE);
                return result;
            }

            if (param.getStatus().isBlank()) {
                result.setStatus(ResultForSetUser.Status.MISSING_STATUS);
                return result;
            }

            if (!param.getStatus().equals("Active") && !param.getStatus().equals("Inactive")) {
                result.setStatus(ResultForSetUser.Status.INVALID_STATUS);
                return result;
            }

            UpdateBuilder updateBuilder = new UpdateBuilder();
            updateBuilder.appendUpdate("update Users ");

            updateBuilder.appendSet("FirstName = ? ");
            updateBuilder.addStringToSet(param.getFirstName());

            updateBuilder.appendSet("LastName = ? ");
            updateBuilder.addStringToSet(param.getLastName());

            updateBuilder.appendSet("Email = ? ");
            updateBuilder.addStringToSet(param.getEmail());

            updateBuilder.appendSet("IDNumber = ? ");
            updateBuilder.addStringToSet(param.getIdNumber());

            updateBuilder.appendSet("IDType = ? ");
            updateBuilder.addStringToSet(param.getIdType());

            updateBuilder.appendSet("Role = ? ");
            updateBuilder.addStringToSet(param.getRole());

            updateBuilder.appendSet("Status = ? ");
            updateBuilder.addStringToSet(param.getStatus());

            if (!param.getPassword().isBlank()) {
                updateBuilder.appendSet("PwHash = ? ");
                updateBuilder.addStringToSet(PasswordEncoder.encode(param.getPassword()));
            }

            if (param.getProfilePic().length != 0) {
                updateBuilder.appendSet("ProfilePic = ? ");
                updateBuilder.addBytesToSet(param.getProfilePic());
            }

            updateBuilder.appendWhere("UserID = ? ");
            updateBuilder.addIntToWhere(param.getUserId());

            try (PreparedStatement preparedStatement = updateBuilder.prepare(connection)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForSetUser.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForGetCurrentUser getCurrentUser(ParamForGetCurrentUser param) throws RemoteException {
        ResultForGetCurrentUser result = new ResultForGetCurrentUser();

        try (Connection connection = database.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select " +
                            "U.UserID, " +
                            "U.FirstName, " +
                            "U.LastName, " +
                            "U.Email, " +
                            "U.IDNumber, " +
                            "U.IDType, " +
                            "U.Role, " +
                            "U.ProfilePic, " +
                            "U.Status, " +
                            "C.UserID as CreatedByUserID, " +
                            "C.FirstName || ' ' || C.LastName as CreatedByUserName, " +
                            "U.CreatedOn " +
                            "from Sessions as S" +
                            "inner join Users as U on U.UserID = S.UserID " +
                            "inner join Users as C on C.UserID = U.CreatedBy " +
                            "where S.SessionID = ?"
            )) {
                preparedStatement.setString(1, param.getSessionId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    if (row.next()) {
                        User user = result.getUser();
                        user.setUserId(row.getInt(1));
                        user.setFirstName(row.getString(2));
                        user.setLastName(row.getString(3));
                        user.setEmail(row.getString(4));
                        user.setIdNumber(row.getString(5));
                        user.setIdType(row.getString(6));
                        user.setRole(row.getString(7));
                        user.setProfilePic(row.getBytes(8));
                        user.setStatus(row.getString(9));
                        user.setCreatedByUserId(row.getInt(10));
                        user.setCreatedByUserName(row.getString(11));
                        user.setCreatedOn(LocalDateTime.parse(row.getString(12)));
                    } else {
                        result.setStatus(ResultForGetCurrentUser.Status.INVALID_SESSION_ID);
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForGetCurrentUser.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForLogin login(ParamForLogin param) throws RemoteException {
        ResultForLogin result = new ResultForLogin();

        try (Connection connection = database.getConnection()) {
            if (param.getEmail().isBlank()) {
                result.setStatus(ResultForLogin.Status.MISSING_EMAIL);
                return result;
            }

            if (param.getPassword().isEmpty()) {
                result.setStatus(ResultForLogin.Status.MISSING_PASSWORD);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select " +
                            "U.UserID, " +
                            "U.FirstName, " +
                            "U.LastName, " +
                            "U.Email, " +
                            "U.IDNumber, " +
                            "U.IDType, " +
                            "U.Role, " +
                            "U.ProfilePic, " +
                            "U.Status, " +
                            "C.UserID as CreatedByUserID, " +
                            "C.FirstName || ' ' || C.LastName as CreatedByUserName, " +
                            "U.CreatedOn, " +
                            "U.PwHash " +
                            "from Users as U" +
                            "inner join Users as C on C.UserID = U.CreatedBy " +
                            "where U.Email = ?"
            )) {
                preparedStatement.setString(1, param.getEmail());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    if (!row.next()) {
                        result.setStatus(ResultForLogin.Status.INVALID_CREDENTIAL);
                        return result;
                    }

                    if (!PasswordEncoder.verify(param.getPassword(), row.getString(13))) {
                        result.setStatus(ResultForLogin.Status.INVALID_CREDENTIAL);
                        return result;
                    }

                    User user = result.getUser();
                    user.setUserId(row.getInt(1));
                    user.setFirstName(row.getString(2));
                    user.setLastName(row.getString(3));
                    user.setEmail(row.getString(4));
                    user.setIdNumber(row.getString(5));
                    user.setIdType(row.getString(6));
                    user.setRole(row.getString(7));
                    user.setProfilePic(row.getBytes(8));
                    user.setStatus(row.getString(9));
                    user.setCreatedByUserId(row.getInt(10));
                    user.setCreatedByUserName(row.getString(11));
                    user.setCreatedOn(LocalDateTime.parse(row.getString(12)));
                }
            }

            result.setSessionId(RandomStrGenerator.generateSessionId());

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into Sessions (SessionID, UserID) values (?, ?)"
            )) {
                preparedStatement.setString(1, result.getSessionId());
                preparedStatement.setInt(2, result.getUser().getUserId());

                preparedStatement.executeUpdate();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForLogin.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForLogout logout(ParamForLogout param) throws RemoteException {
        ResultForLogout result = new ResultForLogout();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForLogout.Status.INVALID_SESSION_ID);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "delete from Sessions where SessionID = ?"
            )) {
                preparedStatement.setString(1, param.getSessionId());

                preparedStatement.executeUpdate();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForLogout.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForGetManyProducts getManyProducts(ParamForGetManyProducts param) throws RemoteException {
        ResultForGetManyProducts result = new ResultForGetManyProducts();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForGetManyProducts.Status.INVALID_SESSION_ID);
                return result;
            }

            QueryBuilder queryData = new QueryBuilder();

            queryData.appendSelect(
                    "select " +
                            "P.ProductID, " +
                            "P.Name, " +
                            "P.Category, " +
                            "P.ProductPrice, " +
                            "P.ProductPic, " +
                            "P.Status, " +
                            "C.UserID as CreatedByUserID, " +
                            "C.FirstName || ' ' || C.LastName as CreatedByUserName, " +
                            "P.CreatedOn "
            );

            queryData.appendFrom(
                    "from Products as P " +
                            "inner join Users as C " +
                            "on C.UserID = P.CreatedBy "
            );

            if (!param.getName().isBlank()) {
                queryData.appendWhere("P.Name like ? ");
                queryData.addStringToWhere(param.getName() + "%");
            }

            if (!param.getCategory().isBlank()) {
                queryData.appendWhere("P.Category = ? ");
                queryData.addStringToWhere(param.getCategory());
            }

            if (!param.getStatus().isBlank()) {
                queryData.appendWhere("P.Status = ? ");
                queryData.addStringToWhere(param.getStatus());
            }

            queryData.appendRemaining("order by P.ProductID desc limit 20 offset ? ");
            queryData.addIntToRemaining((param.getPage() - 1) * 20);

            try (
                    PreparedStatement preparedStatement = queryData.prepare(connection);
                    ResultSet row = preparedStatement.executeQuery()
            ) {
                while (row.next()) {
                    Product product = new Product();
                    product.setProductId(row.getInt(1));
                    product.setName(row.getString(2));
                    product.setCategory(row.getString(3));
                    product.setProductPrice(BigDecimal.valueOf(row.getInt(4), 2));
                    product.setProductPic(row.getBytes(5));
                    product.setStatus(row.getString(6));
                    product.setCreatedByUserId(row.getInt(7));
                    product.setCreatedByUserName(row.getString(8));
                    product.setCreatedOn(LocalDateTime.parse(row.getString(9)));

                    result.getProducts().add(product);
                }
            }

            QueryBuilder queryCount = new QueryBuilder();

            queryCount.appendSelect("select count(*) ");
            queryCount.appendFrom(queryData.getFrom());
            queryCount.appendWhere(queryData.getWhere());
            queryCount.getWhereParams().addAll(queryData.getWhereParams());

            try (
                    PreparedStatement preparedStatement = queryCount.prepare(connection);
                    ResultSet row = preparedStatement.executeQuery()
            ) {
                row.next();
                int rowCount = row.getInt(1);
                result.setMaxPage((rowCount - 1) / 20 + 1);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForGetManyProducts.Status.SERVER_ERROR);
        }

        result.setCurrentPage(param.getPage());

        return result;
    }

    @Override
    public ResultForGetOneProduct getOneProduct(ParamForGetOneProduct param) throws RemoteException {
        ResultForGetOneProduct result = new ResultForGetOneProduct();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForGetOneProduct.Status.INVALID_SESSION_ID);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select " +
                            "P.ProductID, " +
                            "P.Name, " +
                            "P.Category, " +
                            "P.ProductPrice, " +
                            "P.ProductPic, " +
                            "P.Status, " +
                            "C.UserID as CreatedByUserID, " +
                            "C.FirstName || ' ' || C.LastName as CreatedByUserName, " +
                            "P.CreatedOn " +
                            "from Products as P " +
                            "inner join Users as C " +
                            "on C.UserID = P.CreatedBy " +
                            "where P.ProductID = ?"
            )) {
                preparedStatement.setInt(1, param.getProductId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    if (row.next()) {
                        Product product = result.getProduct();
                        product.setProductId(row.getInt(1));
                        product.setName(row.getString(2));
                        product.setCategory(row.getString(3));
                        product.setProductPrice(BigDecimal.valueOf(row.getInt(4), 2));
                        product.setProductPic(row.getBytes(5));
                        product.setStatus(row.getString(6));
                        product.setCreatedByUserId(row.getInt(7));
                        product.setCreatedByUserName(row.getString(8));
                        product.setCreatedOn(LocalDateTime.parse(row.getString(9)));
                    } else {
                        result.setStatus(ResultForGetOneProduct.Status.NOT_FOUND);
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForGetOneProduct.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForAddProduct addProduct(ParamForAddProduct param) throws RemoteException {
        ResultForAddProduct result = new ResultForAddProduct();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForAddProduct.Status.INVALID_SESSION_ID);
                return result;
            }

            if (param.getName().isBlank()) {
                result.setStatus(ResultForAddProduct.Status.MISSING_NAME);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Products where Name = ?)"
            )) {
                preparedStatement.setString(1, param.getName());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 1) {
                        result.setStatus(ResultForAddProduct.Status.REPEATED_NAME);
                        return result;
                    }
                }
            }

            if (param.getCategory().isBlank()) {
                result.setStatus(ResultForAddProduct.Status.MISSING_CATEGORY);
                return result;
            }

            if (param.getProductPrice().compareTo(BigDecimal.valueOf(0)) == 0) {
                result.setStatus(ResultForAddProduct.Status.MISSING_PRODUCT_PRICE);
                return result;
            }

            if (param.getProductPrice().compareTo(BigDecimal.valueOf(0)) < 0) {
                result.setStatus(ResultForAddProduct.Status.INVALID_PRODUCT_PRICE);
                return result;
            }

            if (param.getStatus().isBlank()) {
                result.setStatus(ResultForAddProduct.Status.MISSING_STATUS);
                return result;
            }

            if (!param.getStatus().equals("Available") && !param.getStatus().equals("Disabled")) {
                result.setStatus(ResultForAddProduct.Status.INVALID_STATUS);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into Products ( " +
                            "Name, Category, ProductPrice, ProductPic, Status, " +
                            "CreatedBy, CreatedOn " +
                            ") " +
                            "values ( " +
                            "?, ?, ?, ?, ?, " +
                            "?, ? " +
                            ")"
            )) {
                preparedStatement.setString(1, param.getName());
                preparedStatement.setString(2, param.getCategory());
                preparedStatement.setInt(3, param.getProductPrice().movePointRight(2).intValue());
                preparedStatement.setBytes(4, param.getProductPic());
                preparedStatement.setString(5, param.getStatus());
                preparedStatement.setInt(6, currentUser.getUserId());
                preparedStatement.setString(7, LocalDateTime.now().toString());

                preparedStatement.executeUpdate();
            }

            try (Statement statement = connection.createStatement()) {
                try (ResultSet row = statement.executeQuery("select last_insert_rowid()")) {
                    row.next();
                    result.setNewProductId(row.getInt(1));
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForAddProduct.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForDeleteProduct deleteProduct(ParamForDeleteProduct param) throws RemoteException {
        ResultForDeleteProduct result = new ResultForDeleteProduct();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForDeleteProduct.Status.INVALID_SESSION_ID);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Products where ProductID = ?)"
            )) {
                preparedStatement.setInt(1, param.getProductId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 0) {
                        result.setStatus(ResultForDeleteProduct.Status.NOT_FOUND);
                        return result;
                    }
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Stock where ProductID = ?)"
            )) {
                preparedStatement.setInt(1, param.getProductId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 1) {
                        result.setStatus(ResultForDeleteProduct.Status.DEPENDED);
                        return result;
                    }
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "delete from Products where ProductID = ?"
            )) {
                preparedStatement.setInt(1, param.getProductId());

                preparedStatement.executeUpdate();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForDeleteProduct.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForSetProduct setProduct(ParamForSetProduct param) throws RemoteException {
        ResultForSetProduct result = new ResultForSetProduct();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForSetProduct.Status.INVALID_SESSION_ID);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Products where ProductID = ?)"
            )) {
                preparedStatement.setInt(1, param.getProductId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 0) {
                        result.setStatus(ResultForSetProduct.Status.NOT_FOUND);
                        return result;
                    }
                }
            }

            if (param.getName().isBlank()) {
                result.setStatus(ResultForSetProduct.Status.MISSING_NAME);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Products where Name = ? and ProductID <> ?)"
            )) {
                preparedStatement.setString(1, param.getName());
                preparedStatement.setInt(2, param.getProductId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 1) {
                        result.setStatus(ResultForSetProduct.Status.REPEATED_NAME);
                        return result;
                    }
                }
            }

            if (param.getCategory().isBlank()) {
                result.setStatus(ResultForSetProduct.Status.MISSING_CATEGORY);
                return result;
            }

            if (param.getProductPrice().compareTo(BigDecimal.valueOf(0)) == 0) {
                result.setStatus(ResultForSetProduct.Status.MISSING_PRODUCT_PRICE);
                return result;
            }

            if (param.getProductPrice().compareTo(BigDecimal.valueOf(0)) < 0) {
                result.setStatus(ResultForSetProduct.Status.INVALID_PRODUCT_PRICE);
                return result;
            }

            if (param.getStatus().isBlank()) {
                result.setStatus(ResultForSetProduct.Status.MISSING_STATUS);
                return result;
            }

            if (!param.getStatus().equals("Available") && !param.getStatus().equals("Disabled")) {
                result.setStatus(ResultForSetProduct.Status.INVALID_STATUS);
                return result;
            }

            UpdateBuilder updateBuilder = new UpdateBuilder();
            updateBuilder.appendUpdate("update Products ");

            updateBuilder.appendSet("Name = ? ");
            updateBuilder.addStringToSet(param.getName());

            updateBuilder.appendSet("Category = ? ");
            updateBuilder.addStringToSet(param.getCategory());

            updateBuilder.appendSet("ProductPrice = ? ");
            updateBuilder.addIntToSet(param.getProductPrice().movePointRight(2).intValue());

            if (param.getProductPic().length != 0) {
                updateBuilder.appendSet("ProductPic = ? ");
                updateBuilder.addBytesToSet(param.getProductPic());
            }

            updateBuilder.appendSet("Status = ? ");
            updateBuilder.addStringToSet(param.getStatus());

            updateBuilder.appendWhere("ProductID = ? ");
            updateBuilder.addIntToWhere(param.getProductId());

            try (PreparedStatement preparedStatement = updateBuilder.prepare(connection)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForSetProduct.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForGetManyStock getManyStock(ParamForGetManyStock param) throws RemoteException {
        ResultForGetManyStock result = new ResultForGetManyStock();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForGetManyStock.Status.INVALID_SESSION_ID);
                return result;
            }

            QueryBuilder queryData = new QueryBuilder();

            queryData.appendSelect(
                    "select " +
                            "S.SKU, " +
                            "P.ProductID, " +
                            "P.Name, " +
                            "P.Category, " +
                            "P.ProductPrice, " +
                            "P.ProductPic, " +
                            "S.Quantity, " +
                            "S.ExpDate, " +
                            "C.UserID as CreatedByUserID, " +
                            "C.FirstName || ' ' || C.LastName as CreatedByUserName, " +
                            "S.CreatedOn "
            );

            queryData.appendFrom(
                    "from Stock as S " +
                            "inner join Users as C on C.UserID = S.CreatedBy " +
                            "inner join Products as P on P.ProductID = S.ProductID"
            );

            if (!param.getSku().isBlank()) {
                queryData.appendWhere("S.SKU = ? ");
                queryData.addStringToWhere(param.getSku());
            }

            if (!param.getName().isBlank()) {
                queryData.appendWhere("P.Name like ? ");
                queryData.addStringToWhere(param.getName() + "%");
            }

            if (!param.getCategory().isBlank()) {
                queryData.appendWhere("P.Category = ? ");
                queryData.addStringToWhere(param.getCategory());
            }

            if (!param.getExpiryDateFrom().isEqual(LocalDate.of(1970, 1, 1))) {
                queryData.appendWhere("S.ExpDate >= ? ");
                queryData.addStringToWhere(param.getExpiryDateFrom().toString());
            }

            if (!param.getExpiryDateTo().isEqual(LocalDate.of(1970, 1, 1))) {
                queryData.appendWhere("S.ExpDate <= ? ");
                queryData.addStringToWhere(param.getExpiryDateTo().toString());
            }

            queryData.appendRemaining("order by S.SKU desc limit 20 offset ? ");
            queryData.addIntToRemaining((param.getPage() - 1) * 20);

            try (
                    PreparedStatement preparedStatement = queryData.prepare(connection);
                    ResultSet row = preparedStatement.executeQuery()
            ) {
                while (row.next()) {
                    Stock stock = new Stock();
                    stock.setSku(row.getString(1));
                    stock.setProductId(row.getInt(2));
                    stock.setProductName(row.getString(3));
                    stock.setProductCategory(row.getString(4));
                    stock.setProductPrice(BigDecimal.valueOf(row.getInt(5), 2));
                    stock.setProductPic(row.getBytes(6));
                    stock.setQuantity(row.getInt(7));
                    stock.setExpiryDate(LocalDate.parse(row.getString(8)));
                    stock.setCreatedByUserId(row.getInt(9));
                    stock.setCreatedByUserName(row.getString(10));
                    stock.setCreatedOn(LocalDateTime.parse(row.getString(11)));

                    result.getStock().add(stock);
                }
            }

            QueryBuilder queryCount = new QueryBuilder();

            queryCount.appendSelect("select count(*) ");
            queryCount.appendFrom(queryData.getFrom());
            queryCount.appendWhere(queryData.getWhere());
            queryCount.getWhereParams().addAll(queryData.getWhereParams());

            try (
                    PreparedStatement preparedStatement = queryCount.prepare(connection);
                    ResultSet row = preparedStatement.executeQuery()
            ) {
                row.next();
                int rowCount = row.getInt(1);
                result.setMaxPage((rowCount - 1) / 20 + 1);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForGetManyStock.Status.SERVER_ERROR);
        }

        result.setCurrentPage(param.getPage());

        return result;
    }

    @Override
    public ResultForGetOneStock getOneStock(ParamForGetOneStock param) throws RemoteException {
        ResultForGetOneStock result = new ResultForGetOneStock();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForGetOneStock.Status.INVALID_SESSION_ID);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select " +
                            "S.SKU, " +
                            "P.ProductID, " +
                            "P.Name, " +
                            "P.Category, " +
                            "P.ProductPrice, " +
                            "P.ProductPic, " +
                            "S.Quantity, " +
                            "S.ExpDate, " +
                            "C.UserID as CreatedByUserID, " +
                            "C.FirstName || ' ' || C.LastName as CreatedByUserName, " +
                            "S.CreatedOn " +
                            "from Stock as S " +
                            "inner join Users as C on C.UserID = S.CreatedBy " +
                            "inner join Products as P on P.ProductID = S.ProductID" +
                            "where S.SKU = ?"
            )) {
                preparedStatement.setString(1, param.getSku());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    if (row.next()) {
                        Stock stock = result.getStock();
                        stock.setSku(row.getString(1));
                        stock.setProductId(row.getInt(2));
                        stock.setProductName(row.getString(3));
                        stock.setProductCategory(row.getString(4));
                        stock.setProductPrice(BigDecimal.valueOf(row.getInt(5), 2));
                        stock.setProductPic(row.getBytes(6));
                        stock.setQuantity(row.getInt(7));
                        stock.setExpiryDate(LocalDate.parse(row.getString(8)));
                        stock.setCreatedByUserId(row.getInt(9));
                        stock.setCreatedByUserName(row.getString(10));
                        stock.setCreatedOn(LocalDateTime.parse(row.getString(11)));
                    } else {
                        result.setStatus(ResultForGetOneStock.Status.NOT_FOUND);
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForGetOneStock.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForGetManyStockTrx getManyStockTrx(ParamForGetManyStockTrx param) throws RemoteException {
        ResultForGetManyStockTrx result = new ResultForGetManyStockTrx();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForGetManyStockTrx.Status.INVALID_SESSION_ID);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Stock where SKU = ?)"
            )) {
                preparedStatement.setString(1, param.getSku());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 0) {
                        result.setStatus(ResultForGetManyStockTrx.Status.NOT_FOUND);
                        return result;
                    }
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select " +
                            "T.StockTrxID, " +
                            "T.SKU, " +
                            "T.QuantityVaried, " +
                            "T.Remark, " +
                            "C.UserID as CreatedByUserID, " +
                            "C.FirstName || ' ' || C.LastName as CreatedByUserName, " +
                            "T.CreatedOn " +
                            "from StockTrx as T " +
                            "left join Users as C on C.UserID = T.CreatedBy " +
                            "where T.SKU = ? " +
                            "order by T.StockTrxID desc limit 20 offset ?"
            )) {
                preparedStatement.setString(1, param.getSku());
                preparedStatement.setInt(2, (param.getPage() - 1) * 20);

                try (ResultSet row = preparedStatement.executeQuery()) {
                    while (row.next()) {
                        StockTrx stockTrx = new StockTrx();
                        stockTrx.setStockTrxId(row.getInt(1));
                        stockTrx.setSku(row.getString(2));
                        stockTrx.setQuantityVaried(row.getInt(3));
                        stockTrx.setRemark(row.getString(4));
                        stockTrx.setCreatedByUserId(row.getInt(5));
                        stockTrx.setCreatedByUserName(row.getString(6));
                        stockTrx.setCreatedOn(LocalDateTime.parse(row.getString(7)));

                        result.getStockTrx().add(stockTrx);
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForGetManyStockTrx.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForAddStock addStock(ParamForAddStock param) throws RemoteException {
        ResultForAddStock result = new ResultForAddStock();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForAddStock.Status.INVALID_SESSION_ID);
                return result;
            }

            if (param.getProductId() == 0) {
                result.setStatus(ResultForAddStock.Status.MISSING_PRODUCT_ID);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Products where ProductID = ?)"
            )) {
                preparedStatement.setInt(1, param.getProductId());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 1) {
                        result.setStatus(ResultForAddStock.Status.INVALID_PRODUCT_ID);
                        return result;
                    }
                }
            }

            if (param.getQuantity() == 0) {
                result.setStatus(ResultForAddStock.Status.MISSING_QUANTITY);
                return result;
            }

            if (param.getQuantity() < 0) {
                result.setStatus(ResultForAddStock.Status.INVALID_QUANTITY);
                return result;
            }

            if (param.getExpiryDate().compareTo(LocalDate.of(1970, 1, 1)) == 0) {
                result.setStatus(ResultForAddStock.Status.MISSING_EXPIRY_DATE);
                return result;
            }

            result.setNewSku(RandomStrGenerator.generateSku());
            String now = LocalDateTime.now().toString();

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into Stock ( " +
                            "SKU, ProductID, ExpDate, Quantity, CreatedBy, " +
                            "CreatedOn " +
                            ") " +
                            "values ( " +
                            "?, ?, ?, ?, ?, " +
                            "? " +
                            ")"
            )) {
                preparedStatement.setString(1, result.getNewSku());
                preparedStatement.setInt(2, param.getProductId());
                preparedStatement.setString(3, param.getExpiryDate().toString());
                preparedStatement.setInt(4, param.getQuantity());
                preparedStatement.setInt(5, currentUser.getUserId());
                preparedStatement.setString(6, now);

                preparedStatement.executeUpdate();
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into StockTrx ( " +
                            "SKU, QuantityVaried, Remark, CreatedBy, CreatedOn " +
                            ") " +
                            "values ( " +
                            "?, ?, ?, ?, ? " +
                            ")"
            )) {
                preparedStatement.setString(1, result.getNewSku());
                preparedStatement.setInt(2, param.getQuantity());
                preparedStatement.setString(3, param.getRemark());
                preparedStatement.setInt(4, currentUser.getUserId());
                preparedStatement.setString(5, now);

                preparedStatement.executeUpdate();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForAddStock.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForDeleteStock deleteStock(ParamForDeleteStock param) throws RemoteException {
        ResultForDeleteStock result = new ResultForDeleteStock();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForDeleteStock.Status.INVALID_SESSION_ID);
                return result;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select exists(select 1 from Stock where SKU = ?)"
            )) {
                preparedStatement.setString(1, param.getSku());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) == 0) {
                        result.setStatus(ResultForDeleteStock.Status.NOT_FOUND);
                        return result;
                    }
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select count(*) from StockTrx where SKU = ?"
            )) {
                preparedStatement.setString(1, param.getSku());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    row.next();

                    if (row.getInt(1) > 1) {
                        result.setStatus(ResultForDeleteStock.Status.DEPENDED);
                        return result;
                    }
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "delete from Stock where SKU = ?"
            )) {
                preparedStatement.setString(1, param.getSku());

                preparedStatement.executeUpdate();
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "delete from StockTrx where SKU = ?"
            )) {
                preparedStatement.setString(1, param.getSku());

                preparedStatement.executeUpdate();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForDeleteStock.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForSetStock setStock(ParamForSetStock param) throws RemoteException {
        ResultForSetStock result = new ResultForSetStock();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForSetStock.Status.INVALID_SESSION_ID);
                return result;
            }

            int remainingQuantity;

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select Quantity from Stock where SKU = ?"
            )) {
                preparedStatement.setString(1, param.getSku());

                try (ResultSet row = preparedStatement.executeQuery()) {
                    if (row.next()) {
                        remainingQuantity = row.getInt(1);
                    } else {
                        result.setStatus(ResultForSetStock.Status.NOT_FOUND);
                        return result;
                    }
                }
            }

            if (param.getQuantityVaried() == 0 || -param.getQuantityVaried() > remainingQuantity) {
                result.setStatus(ResultForSetStock.Status.INVALID_QUANTITY_VARIED);
                return result;
            }
            
            if (param.getRemark().isBlank()) {
                result.setStatus(ResultForSetStock.Status.MISSING_REMARK);
                return result;
            }

            UpdateBuilder updateBuilder = new UpdateBuilder();
            updateBuilder.appendUpdate("update Stock ");

            updateBuilder.appendSet("Quantity = ? ");
            updateBuilder.addIntToSet(remainingQuantity + param.getQuantityVaried());

            updateBuilder.appendWhere("SKU = ? ");
            updateBuilder.addStringToWhere(param.getSku());

            try (PreparedStatement preparedStatement = updateBuilder.prepare(connection)) {
                preparedStatement.executeUpdate();
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into StockTrx ( " +
                            "SKU, QuantityVaried, Remark, CreatedBy, CreatedOn " +
                            ") " +
                            "values ( " +
                            "?, ?, ?, ?, ? " +
                            ")"
            )) {
                preparedStatement.setString(1, param.getSku());
                preparedStatement.setInt(2, param.getQuantityVaried());
                preparedStatement.setString(3, param.getRemark());
                preparedStatement.setInt(4, currentUser.getUserId());
                preparedStatement.setString(5, LocalDateTime.now().toString());

                preparedStatement.executeUpdate();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForSetStock.Status.SERVER_ERROR);
        }

        return result;
    }

    @Override
    public ResultForGetManySales getManySales(ParamForGetManySales param) throws RemoteException {
        /*
        ResultForGetManySales result = new ResultForGetManySales();

        try (Connection connection = database.getConnection()) {
            User currentUser = getCurrentUser(connection, param.getSessionId());
            if (currentUser == null) {
                result.setStatus(ResultForGetManySales.Status.INVALID_SESSION_ID);
                return result;
            }

            QueryBuilder queryData = new QueryBuilder();

            queryData.appendSelect(
                    "select " +
                            "A.SaleID, " +
                            "A.SKU, " +
                            "P.ProductID, " +
                            "P.Name, " +
                            "P.Category, " +
                            "A.UnitPrice, " +
                            "A.SoldQuantity, " +
                            "C.UserID as SoldByUserID, " +
                            "C.FirstName || ' ' || C.LastName as SoldByUserName, " +
                            "A.SoldOn "
            );

            queryData.appendFrom(
                    "from Sales as A " +
                            "inner join Stock as O on O.SKU = A.SKU " +
                            "inner join Products as P on P.ProductID = O.ProductID " + 
                            "inner join Users as C on C.UserID = S.SoldBy "
            );

            if (param.getProductId() != 0) {
                queryData.appendWhere("P.ProductID = ? ");
                queryData.addIntToWhere(param.getProductId());
            }

            if (!param.getCategory().isBlank()) {
                queryData.appendWhere("P.Category = ? ");
                queryData.addStringToWhere(param.getCategory());
            }

            if (!param.getStatus().isBlank()) {
                queryData.appendWhere("P.Status = ? ");
                queryData.addStringToWhere(param.getStatus());
            }

            queryData.appendRemaining("order by P.ProductID desc limit 20 offset ? ");
            queryData.addIntToRemaining((param.getPage() - 1) * 20);

            try (
                    PreparedStatement preparedStatement = queryData.prepare(connection);
                    ResultSet row = preparedStatement.executeQuery()
            ) {
                while (row.next()) {
                    Product product = new Product();
                    product.setProductId(row.getInt(1));
                    product.setName(row.getString(2));
                    product.setCategory(row.getString(3));
                    product.setProductPrice(BigDecimal.valueOf(row.getInt(4), 2));
                    product.setProductPic(row.getBytes(5));
                    product.setStatus(row.getString(6));
                    product.setCreatedByUserId(row.getInt(7));
                    product.setCreatedByUserName(row.getString(8));
                    product.setCreatedOn(LocalDateTime.parse(row.getString(9)));

                    result.getProducts().add(product);
                }
            }

            QueryBuilder queryCount = new QueryBuilder();

            queryCount.appendSelect("select count(*) ");
            queryCount.appendFrom(queryData.getFrom());
            queryCount.appendWhere(queryData.getWhere());
            queryCount.getWhereParams().addAll(queryData.getWhereParams());

            try (
                    PreparedStatement preparedStatement = queryCount.prepare(connection);
                    ResultSet row = preparedStatement.executeQuery()
            ) {
                row.next();
                int rowCount = row.getInt(1);
                result.setMaxPage((rowCount - 1) / 20 + 1);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.setStatus(ResultForGetManyProducts.Status.SERVER_ERROR);
        }

        result.setCurrentPage(param.getPage());

        return result;
        */
        return null;
    }

    @Override
    public ResultForAddSale addSale(ParamForAddSale param) throws RemoteException {
        return null;
    }

    @Override
    public ResultForDeleteSale deleteSale(ParamForDeleteSale param) throws RemoteException {
        return null;
    }

    private User getCurrentUser(Connection connection, String sessionId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "select U.UserID, U.Role " +
                        "from Sessions as S inner join Users as U on U.UserId = S.UserId " +
                        "where S.SessionID = ?"
        )) {
            preparedStatement.setString(1, sessionId);

            try (ResultSet row = preparedStatement.executeQuery()) {
                if (!row.next()) {
                    return null;
                } else {
                    User user = new User();
                    user.setUserId(row.getInt(1));
                    user.setRole(row.getString(2));
                    return user;
                }
            }
        }
    }
}
