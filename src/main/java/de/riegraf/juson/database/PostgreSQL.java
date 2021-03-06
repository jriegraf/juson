package de.riegraf.juson.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A wrapper for the PostgreSQL connection.
 */
public class PostgreSQL implements DatabaseConnection{

  private Connection connection;

  public PostgreSQL(String url, String user, String password)
      throws SQLException {
    connection = DriverManager.getConnection("jdbc:postgresql://" + url + "/", user, password);
  }

  public PostgreSQL() {
  }

  /**
   * Executes an SQL statement on the de.riegraf.juson.database. Use this if you are not interested in a ResultSet.
   *
   * @param sql the sql statement
   * @return true if the result is a ResultSet object
   */
  public boolean executeSQL(String sql) throws SQLException {
    try (var statement = connection.createStatement()){
      return statement.execute(sql);
    }
  }

  /**
   * Executes a sql statement.
   *
   * @param sql the sql statement
   * @return the response from the de.riegraf.juson.database in a ResultSet
   */
  public ResultSet querySQL(String sql) throws SQLException {
    try (var statement = connection.createStatement()){
      return statement.executeQuery(sql);
    }
  }

  /**
   * Create a prepared statement.
   *
   * @param sql the sql statement that contain one or more '?' placeholder
   * @return the precompiled PreparedStatement
   */
  public PreparedStatement createPreparedStatement(String sql) throws SQLException {
    return connection.prepareStatement(sql);
  }

  /**
   * Closes the de.riegraf.juson.database connection.
   */
  public void close() throws SQLException {
    connection.close();
  }
}
