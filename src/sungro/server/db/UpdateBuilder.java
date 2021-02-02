package sungro.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class UpdateBuilder {
    private final StringBuilder updateClause;

    private final StringBuilder setClause;
    private final ArrayList<Parameter> setParams;
    private final StringBuilder fromClause;
    private final ArrayList<Parameter> fromParams;
    private final StringBuilder whereClause;
    private final ArrayList<Parameter> whereParams;

    public UpdateBuilder() {
        updateClause = new StringBuilder();

        setClause = new StringBuilder();
        setParams = new ArrayList<>();
        fromClause = new StringBuilder();
        fromParams = new ArrayList<>();
        whereClause = new StringBuilder();
        whereParams = new ArrayList<>();
    }

    public String getUpdate() {
        return updateClause.toString();
    }

    public void appendUpdate(String sql) {
        updateClause.append(sql);
    }

    public String getSet() {
        return setClause.toString();
    }

    public ArrayList<Parameter> getSetParams() {
        return setParams;
    }

    public void appendSet(String sql) {
        if (setClause.length() > 0) {
            setClause.append(", ");
        }
        setClause.append(sql);
    }

    public void addIntToSet(Integer x) {
        setParams.add(new Parameter(DataType.INT, x));
    }

    public void addStringToSet(String x) {
        setParams.add(new Parameter(DataType.STRING, x));
    }

    public void addBytesToSet(byte[] x) {
        setParams.add(new Parameter(DataType.BYTES, x));
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

    public void addBytesToFrom(byte[] x) {
        fromParams.add(new Parameter(DataType.BYTES, x));
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

    public PreparedStatement prepare(Connection connection) throws SQLException {
        StringBuilder sql = new StringBuilder(updateClause);
        sql.append("set ");
        sql.append(setClause);
        sql.append(fromClause);

        if (whereClause.length() != 0) {
            sql.append("where ");
        }

        sql.append(whereClause);

        PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());

        ArrayList<Parameter> params = new ArrayList<>();
        params.addAll(setParams);
        params.addAll(fromParams);
        params.addAll(whereParams);

        for (int i = 0; i < params.size(); i++) {
            Parameter p = params.get(i);

            switch (p.getDataType()) {
                case INT:
                    preparedStatement.setInt(i + 1, (Integer) p.getValue());
                    break;
                case STRING:
                    preparedStatement.setString(i + 1, (String) p.getValue());
                    break;
                case BYTES:
                    preparedStatement.setBytes(i + 1, (byte[]) p.getValue());
            }
        }

        return preparedStatement;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("SQL:\n");
        result.append(updateClause);
        result.append("set ");
        result.append(setClause);
        result.append(fromClause);

        if (whereClause.length() != 0) {
            result.append("where ");
        }

        result.append(whereClause);
        result.append("\n\nParams:\n");

        ArrayList<Parameter> params = new ArrayList<>();
        params.addAll(setParams);
        params.addAll(fromParams);
        params.addAll(whereParams);

        for (int i = 0; i < params.size(); i++) {
            Parameter p = params.get(i);
            result.append(String.format("%2d %10s %s\n", i + 1, p.getDataType(), p.getValue()));
        }

        return result.toString();
    }

    private enum DataType {INT, STRING, BYTES}

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
