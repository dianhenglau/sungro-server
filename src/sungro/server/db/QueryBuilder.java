package sungro.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class QueryBuilder {
    private final StringBuilder selectClause;
    private final ArrayList<Parameter> selectParams;
    private final StringBuilder fromClause;
    private final ArrayList<Parameter> fromParams;
    private final StringBuilder whereClause;
    private final ArrayList<Parameter> whereParams;
    private final StringBuilder remainingClause;
    private final ArrayList<Parameter> remainingParams;

    public QueryBuilder() {
        selectClause = new StringBuilder();
        selectParams = new ArrayList<>();
        fromClause = new StringBuilder();
        fromParams = new ArrayList<>();
        whereClause = new StringBuilder();
        whereParams = new ArrayList<>();
        remainingClause = new StringBuilder();
        remainingParams = new ArrayList<>();
    }

    public String getSelect() {
        return selectClause.toString();
    }

    public ArrayList<Parameter> getSelectParams() {
        return selectParams;
    }

    public void appendSelect(String sql) {
        selectClause.append(sql);
    }

    public void addIntToSelect(Integer x) {
        selectParams.add(new Parameter(DataType.INT, x));
    }

    public void addStringToSelect(String x) {
        selectParams.add(new Parameter(DataType.STRING, x));
    }

    public String getFrom() {
        return fromClause.toString();
    }

    public ArrayList<Parameter> getFromParams() {
        return fromParams;
    }

    public void appendFrom(String sql) {
        fromClause.append(sql);
    }

    public void addIntToFrom(Integer x) {
        fromParams.add(new Parameter(DataType.INT, x));
    }

    public void addStringToFrom(String x) {
        fromParams.add(new Parameter(DataType.STRING, x));
    }

    public String getWhere() {
        return whereClause.toString();
    }

    public ArrayList<Parameter> getWhereParams() {
        return whereParams;
    }

    public void appendWhere(String sql) {
        if (whereClause.length() > 0) {
            whereClause.append("and ");
        }
        whereClause.append(sql);
    }

    public void addIntToWhere(Integer x) {
        whereParams.add(new Parameter(DataType.INT, x));
    }

    public void addStringToWhere(String x) {
        whereParams.add(new Parameter(DataType.STRING, x));
    }

    public String getRemaining() {
        return remainingClause.toString();
    }

    public ArrayList<Parameter> getRemainingParams() {
        return remainingParams;
    }

    public void appendRemaining(String sql) {
        remainingClause.append(sql);
    }

    public void addIntToRemaining(Integer x) {
        remainingParams.add(new Parameter(DataType.INT, x));
    }

    public void addStringToRemaining(String x) {
        remainingParams.add(new Parameter(DataType.STRING, x));
    }

    public PreparedStatement prepare(Connection connection) throws SQLException {
        StringBuilder sql = new StringBuilder(selectClause);
        sql.append(fromClause);

        if (whereClause.length() != 0) {
            sql.append("where ");
        }

        sql.append(whereClause);
        sql.append(remainingClause);

        PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());

        ArrayList<Parameter> params = new ArrayList<>();
        params.addAll(selectParams);
        params.addAll(fromParams);
        params.addAll(whereParams);
        params.addAll(remainingParams);

        for (int i = 0; i < params.size(); i++) {
            Parameter p = params.get(i);

            switch (p.getDataType()) {
                case INT:
                    preparedStatement.setInt(i + 1, (Integer) p.getValue());
                    break;
                case STRING:
                    preparedStatement.setString(i + 1, (String) p.getValue());
                    break;
            }
        }

        return preparedStatement;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("SQL:\n");
        result.append(selectClause);
        result.append(fromClause);

        if (whereClause.length() != 0) {
            result.append("where ");
        }

        result.append(whereClause);
        result.append(remainingClause);
        result.append("\n\nParams:\n");

        ArrayList<Parameter> params = new ArrayList<>();
        params.addAll(selectParams);
        params.addAll(fromParams);
        params.addAll(whereParams);
        params.addAll(remainingParams);

        for (int i = 0; i < params.size(); i++) {
            Parameter p = params.get(i);
            result.append(String.format("%2d %10s %s\n", i + 1, p.getDataType(), p.getValue()));
        }

        return result.toString();
    }

    private enum DataType {INT, STRING}

    private static class Parameter {
        private final DataType dataType;
        private final Object value;

        public Parameter(DataType dataType, Object value) {
            this.dataType = dataType;
            this.value = value;
        }

        public DataType getDataType() {
            return dataType;
        }

        public Object getValue() {
            return value;
        }
    }
}
