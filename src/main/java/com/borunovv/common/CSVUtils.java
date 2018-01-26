package com.borunovv.common;

import au.com.bytecode.opencsv.CSVReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 * Хелпер по парсингу CSV-формата.
 * <p/>
 * Maven dependencies:
 * <pre>
 * {@code
 *
 * <dependency>
 * <groupId>net.sf.opencsv</groupId>
 * <artifactId>opencsv</artifactId>
 * <version>2.0</version>
 * </dependency>
 * }
 * </pre>
 * <p/>
 *
 * @author borunovv
 * @date 28-11-2013
 */
public final class CSVUtils {

    public static List<String[]> parse(String csvContent) throws IOException {
        return parse(new CSVReader(new StringReader(csvContent)));
    }

    public static List<String[]> parse(String csvContent, char separator, char quotechar) throws IOException {
        return parse(new CSVReader(new StringReader(csvContent), separator, quotechar));
    }

    public static List<String[]> parse(Reader reader) throws IOException {
        return parse(new CSVReader(reader));
    }

    public static List<String[]> parse(Reader reader, char separator, char quotechar) throws IOException {
        return parse(new CSVReader(reader, separator, quotechar));
    }

    public static String[] parseOneLine(Reader reader) throws IOException {
        return new CSVReader(reader).readNext();
    }

    public static List<Map<String, String>> parseToMaps(Reader reader) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();
        CSVReader csvReader = new CSVReader(reader);
        String[] headers = csvReader.readNext();
        if (headers != null) {
            String[] columns;
            while ((columns = csvReader.readNext()) != null) {
                Map<String, String> row = new TreeMap<>();
                for (int i = 0; i < columns.length; i++) {
                    row.put(headers[i], columns[i]);
                }
                result.add(row);
            }
        }
        return result;
    }

    private static List<String[]> parse(CSVReader csvReader) throws IOException {
        List<String[]> lines = new ArrayList<String[]>();
        String[] nextLine;
        while ((nextLine = csvReader.readNext()) != null) {
            lines.add(nextLine);
        }
        return lines;
    }
}