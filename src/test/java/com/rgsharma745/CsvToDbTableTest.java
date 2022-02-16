package com.rgsharma745;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class CsvToDbTableTest {

    @Autowired
    private CsvToDbTable csvToDbTable;

    @Test
    void createTable() {
        csvToDbTable.loadAllCsvToDB("C:\\Test\\archetype_model\\csv\\importArchetypeEquipment");
        Assertions.assertTrue(true);
    }
}
