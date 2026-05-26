package com.example.project.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple JDBC helper kept in utils to avoid mixing database setup logic into tests.
 * Leave it unused until the project needs backend data validation.
 */
public final class DatabaseUtils {

    private DatabaseUtils() {
    }

    public static List<Map<String, Object>> executeQuery(
            String connectionUrl,
            String username,
            String password,
            String sql
    ) {
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(connectionUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    row.put(metadata.getColumnLabel(columnIndex), resultSet.getObject(columnIndex));
                }
                rows.add(row);
            }

            return rows;
        } catch (SQLException exception) {
            throw new IllegalStateException("Database query execution failed.", exception);
        }
    }
}
