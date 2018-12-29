package de.riegraf.juson.converter;

import de.riegraf.juson.database.PostgreSQL;
import de.riegraf.juson.exception.JusonException;
import de.riegraf.juson.utils.table.Record;
import de.riegraf.juson.utils.table.Table;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.JSONObject;
import org.json.JSONTokener;


public class Juson {

  public Juson(String rootName, String json, PostgreSQL postgre, String schema) throws JusonException, SQLException, ClassNotFoundException {

    JusonConverter jusonConverter = new JusonConverter(schema);
    JusonConverter.Database db = jusonConverter.convert(rootName, json);

    List<Table> tables = db.getTables();
    List<Record> records = db.getRecords();

    printTablesAndRecords(tables, records);

    tables.forEach(x -> {
      try {
        postgre.executeSQL(x.getCreateTableQuery());
      } catch (SQLException e) {
        e.printStackTrace();
      }
    });

    tables.forEach(table -> {
      try {
        PreparedStatement p = postgre
            .createPreparedStatement(table.getInsertSqlForPrepStatement());

        List<Record> recordOfTable = records.stream()
            .filter(r -> r.getTable().equals(table))
            .collect(Collectors.toList());

        insertRecords(recordOfTable, p);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    });
  }

  private static void insertRecords(List<Record> records, PreparedStatement p) {
    try {
      for (Record r : records) {
        for (int i = 0; i < r.getTable().getColumns().size(); i++) {
          p.setString(i + 1, r.getData(i).orElse(null));
        }
        p.addBatch();
      }
      p.executeBatch();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void printTablesAndRecords(List<Table> tables, List<Record> records) {
    System.out.println(
        "--- Tables ---\n" + tables.stream()
            .map(Table::getCreateTableQuery)
            .collect(Collectors.joining("\n"))
    );

    Map<Table, List<Record>> collect = records.stream()
        .collect(Collectors.groupingBy(Record::getTable));

    collect.entrySet().stream().map(entry -> recordsToString(entry)).forEach(System.out::println);

  }


  public static String recordsToString(Map.Entry<Table, List<Record>> entry) {
    int columnCount = entry.getKey().getColumns().size();
    return entry.getValue().stream().map(r ->
        "INSERT INTO " + r.getTable().getName() + " VALUES ("
            + IntStream.range(0, columnCount)
            .mapToObj(index -> r.getData(index).orElse("null"))
            .collect(Collectors.joining(", "))
            + ")").collect(Collectors.joining("\n"));
  }

  public static void main(final String[] args){
    final String filename = "world.json";
    final String path = "/home/julian/IdeaProjects/juson/src/test/resources/" + filename;
    final String schema = "fromJson";

    try {
      final String json = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
      PostgreSQL postgreSQL = new PostgreSQL("localhost:5432", "postgres", "docker");
      postgreSQL.executeSQL("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
      postgreSQL.executeSQL("CREATE SCHEMA " + schema);
      new Juson(filename, json, postgreSQL, schema);

    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (JusonException e) {
      e.printStackTrace();
    }

  }

}