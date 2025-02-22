package eu.faircode.xlua.x.xlua.database.sql;

public class SQLTableColumn {
    public static SQLTableColumn create(String name, String type) { return new SQLTableColumn(name, type); }
    public static SQLTableColumn create(String name, String type, boolean isPartOfPrimaryKey) { return new SQLTableColumn(name, type, isPartOfPrimaryKey); }

    public final String name;
    public final String type;
    public final boolean isPartOfPrimaryKey;

    public SQLTableColumn(String name, String type) { this(name, type, false); }
    public SQLTableColumn(String name, String type, boolean isPartOfPrimaryKey) {
        this.name = name;
        this.type = type;
        this.isPartOfPrimaryKey = isPartOfPrimaryKey;
    }
}
