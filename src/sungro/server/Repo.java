package sungro.server;

import sungro.api.*;
import sungro.server.db.Database;
import sungro.server.db.QueryBuilder;
import sungro.server.db.UpdateBuilder;

import java.rmi.RemoteException;
import java.sql.*;
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
                            ")" +
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
