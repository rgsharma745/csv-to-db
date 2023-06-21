package com.rgsharma745;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CsvToDbTable {

    private static final String CREATE_TABLE = "create table %s ( %s ) ";
    private static final String INSERT_TABLE = "insert into %s  (%s)  values ( %s ) ";
    private static final String DROP_TABLE = "drop table if exists %s   ";

    private final JdbcTemplate jdbcTemplate;

    public void loadAllCsvToDB(String filePath) throws IOException {
        try (Stream<Path> pathStream = Files.find(Paths.get(filePath), Integer.MAX_VALUE, (path, fileAttr) -> fileAttr.isRegularFile() && path.getFileName().toString().endsWith(".csv"))) {
            Set<Path> paths = pathStream.collect(Collectors.toSet());
            for (Path path : paths) {
                try {
                    loadCsvToDB(path.toString());
                } catch (Exception e) {
                    log.error("Error ", e);
                }
            }
        }
    }

    public void loadCsvToDB(String filePath) throws IOException {
        long startTime = System.currentTimeMillis();
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toFile().getName();
        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .withAllowMissingColumnNames(true);
        try (Reader reader = Files.newBufferedReader(path); CSVParser csvParser = new CSVParser(reader, csvFormat)) {
            String tableName = fileName.replace(".csv", "");
            String dropQuery = generateDropTable(tableName);
            List<String> columns = sortColumnBasedOnIndex(csvParser.getHeaderMap());
            log.debug("Drop Query :: {} ", dropQuery);
            jdbcTemplate.execute(dropQuery);
            String createQuery = generateCreateTable(tableName, columns);
            log.debug("Create Query :: {} ", createQuery);
            jdbcTemplate.execute(createQuery);
            String insertQuery = generateInsertQuery(tableName, columns);
            log.debug("Insert Query :: {} ", insertQuery);
            int[][] batchInsert = batchInsert(tableName, insertQuery, csvParser.getRecords(), columns, 500);
            log.debug("Batch Insert Details :: {} ", (Object) batchInsert);
            log.info("File Name {} Total Time Required :: {} ms", tableName, System.currentTimeMillis() - startTime);
        }
    }

    private String generateDropTable(String fileName) {
        return String.format(DROP_TABLE, fileName.toLowerCase());
    }

    public String generateCreateTable(String tableName, List<String> headers) {
        String columns = headers.stream().map(integer -> integer + " text").collect(Collectors.joining(", "));
        return String.format(CREATE_TABLE, tableName.toLowerCase(), columns.toLowerCase());
    }

    private String generateInsertQuery(String tableName, List<String> headers) {
        Supplier<String> questionMarkSupplier = () -> "?";
        String columns = String.join(", ", headers);
        String values = Stream.generate(questionMarkSupplier).limit(headers.size()).collect(Collectors.joining(", "));

        return String.format(INSERT_TABLE, tableName.toLowerCase(), columns.toLowerCase(), values.toLowerCase());
    }

    private List<String> sortColumnBasedOnIndex(Map<String, Integer> headers) {
        List<String> columns = new LinkedList<>();
        LinkedHashMap<String, Integer> sortedMap = headers.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        sortedMap.forEach((key, value) -> {
            if (StringUtils.hasText(key)) {
                columns.add(key);
            }
        });
        return columns;
    }

    public int[][] batchInsert(String fileName, String insertSql, List<CSVRecord> records, List<String> headers, int batchSize) {
        return jdbcTemplate.batchUpdate(insertSql, records, batchSize, (ps, csvRecord) -> {
            for (int i = 1; i <= headers.size(); i++) {
                String value = null;
                try {
                    value = csvRecord.get(headers.get(i - 1));
                } catch (IllegalArgumentException e) {
                    if (!e.getMessage().contains("Index for header")) {
                        log.warn("Error Reading Record from File {} Record Number {} , Exception :: {}", fileName, csvRecord.getRecordNumber(), e.getMessage());
                    }
                }
                ps.setString(i, value);
            }
        });
    }
}
