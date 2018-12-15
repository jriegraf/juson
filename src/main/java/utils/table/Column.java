package utils.table;

/**
 * Represents a column in a database table. Does NOT hold any data.
 */
public class Column {

  public String name;
  public String datatype;
  public Long precision;
  public String defaultValue;

  public Column(String name, String datatype, Long precision, String defaultValue) {
    this.name = name;
    this.datatype = datatype;
    this.precision = precision;
    this.defaultValue = defaultValue;
  }

  private String getDefaultString() {
    return defaultValue == null ? "" : " DEFAULT '" + defaultValue + "'";
  }

  private String getPreisionString() {
    return precision == null ? "" : " (" + precision + ")";
  }

  @Override
  public String toString() {
    return name + " " + datatype + getPreisionString() + getDefaultString();
  }
}
