package com.rgsharma745;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class CsvToDbTable {

    private static final String CREATE_TABLE = "create table %s ( %s ) ";
    private static final String INSERT_TABLE = "insert into %s (%s) values ( %s ) ";
    private static final String DROP_TABLE = "drop table if exists %s ";
    private static final Logger log = LoggerFactory.getLogger(CsvToDbTable.class);

    private final JdbcTemplate jdbcTemplate;

    public CsvToDbTable(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    /**
     * Loads all CSV files in the specified directory to the database.
     *
     * @param filePath  The path to the directory containing the CSV files.
     * @param batchSize batch size for db insertion
     */
    public void loadAllCsvToDB(String filePath, int batchSize) throws IOException {
        try(Stream<Path> pathStream = Files.find(Paths.get(filePath), Integer.MAX_VALUE,
                (path, fileAttr) -> fileAttr.isRegularFile() && path.getFileName().toString().endsWith(".csv")))
        {
            Set<Path> paths = pathStream.collect(Collectors.toSet());
            for (Path path : paths) {
                try {
                    loadCsvToDB(path.toString(), batchSize);
                } catch (Exception e) {
                    log.error("Error ", e);
                }
            }
        }
    }

    public void loadCsvToDB(String filePath, int batchSize) throws IOException {
        long startTime = System.currentTimeMillis();
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toFile().getName();
        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .setAllowMissingColumnNames(true)
                .get();
        Reader reader = Files.newBufferedReader(path);
        CSVParser csvParser = CSVParser.builder().setReader(reader).setFormat(csvFormat).get();
        String tableName = Utils.normalize(fileName.replace(".csv", ""));
        String dropQuery = generateDropTable(tableName);
        List<String> columns = sortColumnBasedOnIndex(csvParser.getHeaderMap());
        log.debug("Drop Query :: {} ", dropQuery);
        jdbcTemplate.execute(dropQuery);
        String createQuery = generateCreateTable(tableName, columns);
        log.debug("Create Query :: {} ", createQuery);
        jdbcTemplate.execute(createQuery);
        String insertQuery = generateInsertQuery(tableName, columns);
        log.debug("Insert Query :: {} ", insertQuery);
        batchInsert(tableName, insertQuery, csvParser, columns, batchSize);
        log.info("File Name {} Total Time Required :: {} ms", tableName, System.currentTimeMillis() - startTime);
    }

    private String generateDropTable(String fileName) {
        return String.format(DROP_TABLE, fileName.toLowerCase());
    }

    public String generateCreateTable(String tableName, List<String> headers) {
        String columns = headers.stream().map(columnName -> Utils.normalize(columnName) + " text").collect(Collectors.joining(", "));
        return String.format(CREATE_TABLE, tableName.toLowerCase(), columns.toLowerCase());
    }

    private String generateInsertQuery(String tableName, List<String> headers) {
        String columns = headers.stream().map(Utils::normalize).collect(Collectors.joining(", "));
        String values = IntStream.range(0, headers.size()).mapToObj(_ -> "?").collect(Collectors.joining(", "));
        return String.format(INSERT_TABLE, tableName.toLowerCase(), columns.toLowerCase(), values);
    }

    private List<String> sortColumnBasedOnIndex(Map<String, Integer> headers) {
        return headers.entrySet().stream()
                .filter(entry -> StringUtils.hasText(entry.getKey()))
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
    }

    public void batchInsert(String fileName, String insertSql, CSVParser parser, List<String> headers, int batchSize) {
        List<CSVRecord> records = new ArrayList<>();
        for (CSVRecord csvRecord : parser) {
            records.add(csvRecord);
            if (records.size() == batchSize) {
                batchInsertInDb(fileName, insertSql, headers, batchSize, records);
                records.clear();
            }
        }
        if (!records.isEmpty()) {
            batchInsertInDb(fileName, insertSql, headers, batchSize, records);
        }
    }

    private void batchInsertInDb(String fileName, String insertSql, List<String> headers, int batchSize, List<CSVRecord> records) {
        jdbcTemplate.batchUpdate(insertSql, records, batchSize, (ps, csvRecord) -> {
            for (int i = 0; i < headers.size(); i++) {
                String value = null;
                try {
                    value = csvRecord.get(headers.get(i));
                } catch (IllegalArgumentException e) {
                    if (!e.getMessage().contains("Index for header")) {
                        log.warn("Error Reading Record from File {} Record Number {} , Exception :: {}", fileName, csvRecord.getRecordNumber(), e.getMessage());
                    }
                }
                ps.setString(i + 1, value);
            }
        });
        log.debug("Inserted {} records to table : {} ", records.size(), fileName);
    }
}
