package com.example.project.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight CSV reader for small test datasets kept in src/test/resources.
 * For complex CSV quoting rules, this can be replaced later by Apache Commons CSV.
 */
public final class CsvDataReader {

    public List<Map<String, String>> read(String resourceName) {
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("CSV resource not found: " + resourceName);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return parseRows(reader);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read CSV resource: " + resourceName, exception);
        }
    }

    private List<Map<String, String>> parseRows(BufferedReader reader) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        String headerLine = reader.readLine();

        if (headerLine == null || headerLine.isBlank()) {
            return rows;
        }

        String[] headers = splitCsvLine(headerLine);
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            if (currentLine.isBlank()) {
                continue;
            }

            String[] values = splitCsvLine(currentLine);
            if (values.length != headers.length) {
                throw new IllegalArgumentException("Malformed CSV row. Expected "
                        + headers.length + " columns but found " + values.length);
            }

            Map<String, String> row = new LinkedHashMap<>();
            for (int index = 0; index < headers.length; index++) {
                row.put(headers[index].trim(), values[index].trim());
            }
            rows.add(row);
        }

        return rows;
    }

    private String[] splitCsvLine(String line) {
        return line.split("\\s*,\\s*");
    }
}
