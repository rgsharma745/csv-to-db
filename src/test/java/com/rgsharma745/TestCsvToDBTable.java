package com.rgsharma745;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class TestCsvToDBTable {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");



  @Autowired
  JdbcTemplate jdbcTemplate;

  @Test
  void loadAllCsvToDB() {
    Assertions.assertTrue(postgres.isRunning());
    Integer count = jdbcTemplate.queryForObject("select count(*) from test", Integer.class);
    Assertions.assertEquals(2,count);
  }

}
