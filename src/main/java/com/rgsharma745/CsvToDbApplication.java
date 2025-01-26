package com.rgsharma745;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CsvToDbApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsvToDbApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(CsvToDbTable csvToDbTable, @Value("${csv.base.location}") String csvBaseLocation,
                                               @Value("${db.batch.size:1000}") int batchSize) {
        return _ -> csvToDbTable.loadAllCsvToDB(csvBaseLocation,batchSize);
    }

}
