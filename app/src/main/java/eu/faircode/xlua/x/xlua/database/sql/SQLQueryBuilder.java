package eu.faircode.xlua.x.xlua.database.sql;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ListUtils;
import eu.faircode.xlua.x.xlua.database.IDatabaseEntry;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;

public class SQLQueryBuilder {
    public static final int SQL_FLAG_ID = 0x0;


    public SQLSnake asSnake() { return (SQLSnake) this; }

    public static final String VALUE_BIND = "?";

    public static final String OP_OR = "OR";
    public static final String OP_AND = "AND";

    public static final String BITWISE_EQUALS = "=";
    public static final String BITWISE_EQUAL_EQUAL = "==";
    public static final String BITWISE_VALUE_GREATER = "<";             //Less than
    public static final String BITWISE_VALUE_LESSER = ">";              //Greater than
    public static final String BITWISE_VALUE_GREATER_EQUAL = "<=";      //Less than Equal
    public static final String BITWISE_VALUE_LESSER_EQUAL = ">=";       //Greater than Equal
    public static final String BITWISE_NOT_EQUAL = "!=";
    public static final String BITWISE_NOT_EQUAL_S = "<>";

    public static final List<String> COMPARE_SYMBOLS = Arrays.asList(
            BITWISE_EQUALS,
            BITWISE_EQUAL_EQUAL,
            BITWISE_VALUE_GREATER,
            BITWISE_VALUE_LESSER,
            BITWISE_VALUE_GREATER_EQUAL,
            BITWISE_VALUE_LESSER_EQUAL,
            BITWISE_NOT_EQUAL,
            BITWISE_NOT_EQUAL_S);

    public static final List<String> OP_SYMBOLS = Arrays.asList(OP_OR, OP_AND);

    protected StringBuilder whereClause = new StringBuilder();
    protected List<String> whereArgs = new ArrayList<>();
    protected List<String> onlyReturn = new ArrayList<>();

    private boolean mHasConsumedId = false;
    private int mColumnCount = 0;
    private String mOpSymbol  = OP_AND;
    private String mOpBitwise = BITWISE_EQUALS;

    public int flags = 0;

    public int getColumnCount() { return mColumnCount; }
    public String getOpSymbol() { return mOpSymbol; }
    public String getBitwise() { return mOpBitwise; }

    protected String tableName = null;
    public String getTableName() { return tableName; }

    private String mColumnOrder = null;
    public String getColumnOrder() { return mColumnOrder; }

    private boolean mPushColumnIfNull = true;
    public boolean getPushColumnValueIfNull() { return mPushColumnIfNull; }

    public String[] getOnlyReturn() { return ListUtils.toStringArray(onlyReturn); }

    public String getWhereClause() { return whereClause.toString(); }
    public String[] getWhereArgs() { return ListUtils.toStringArray(whereArgs); }   //Make this can be null ?

    public SQLQueryBuilder setWhereClause(String str) {
        whereClause = new StringBuilder();
        whereClause.append(str);
        return this;
    }

    public SQLQueryBuilder setWhereArgs(String[] whereArgs) {
        this.whereArgs.clear();
        ListUtil.addAllIfValid(this.whereArgs, List.of(whereArgs));
        return this;
    }

    public boolean hasConsumedId() { return mHasConsumedId; }

    public boolean hasWhereClause() { return whereClause.length() > 0; }
    public boolean hasWhereArgs() { return !whereArgs.isEmpty(); }

    public SQLQueryBuilder() { }
    public SQLQueryBuilder(String tableName) { this.tableName = tableName; }
    public SQLQueryBuilder(String tableName, boolean pushColumnIfNullValue) { this.tableName = tableName; this.mPushColumnIfNull = pushColumnIfNullValue; }

    @SuppressWarnings("unused")
    public SQLQueryBuilder or() {
        this.mOpSymbol = OP_OR;
        return this;
    }

    @SuppressWarnings("unused")
    public SQLQueryBuilder and() {
        this.mOpSymbol = OP_AND;
        return this;
    }

    @SuppressWarnings("unused")
    public SQLQueryBuilder consumedId(boolean hasConsumedId) {
        this.mHasConsumedId = hasConsumedId;
        return this;
    }

    public SQLQueryBuilder whereColumns(String ... columns) {
        for(String c : columns) whereColumn(c, null);
        return this;
    }

    public SQLQueryBuilder whereValues(String ... values) {
        whereArgs.addAll(Arrays.asList(values));
        return this;
    }

    public SQLQueryBuilder whereIdentity(int userId, String category) {
        if(!mHasConsumedId) {
            mHasConsumedId = true;
            whereColumn(UserIdentityIO.FIELD_USER, userId, BITWISE_EQUALS, OP_AND);
            whereColumn(UserIdentityIO.FIELD_CATEGORY, category, BITWISE_EQUALS, OP_AND);
        }

        return this;
    }

    public SQLQueryBuilder whereObjectId(IDatabaseEntry entry) {
        entry.populateSnake(this);
        return this;
    }

    /**
     * Adds a condition to the WHERE clause with a column name, a value, a comparison operator,
     * and an optional logical operator. Ensures the value is converted to a String.
     *
     * @param <T>               The type of the value to be converted to String.
     * @param columnName        The name of the column.
     * @param value             The value to compare against.
     * @param compareSymbol     The comparison operator (e.g., "=", ">", "<"). If null, the default operator is used.
     * @param logicalOp         The logical operator (e.g., "AND", "OR"). If null, the default operator is used.
     * @param parameterizedQuery If true, the query uses parameterized values; otherwise, values are hardcoded.
     * @return The current instance of SQLQueryBuilder for chaining.
     */
    public <T> SQLQueryBuilder whereColumn(String columnName, T value, String compareSymbol, String logicalOp, boolean parameterizedQuery) {
        String valueStr = Str.toString(value);          //Ensure Value is in String Format for the Query
        if (!TextUtils.isEmpty(columnName) && (mPushColumnIfNull || valueStr != null)) {
            appendLogicalOperator(logicalOp);               // [1] Append "AND" or "OR" if it already had previous arguments
            appendColumnName(columnName);                   // [2] Append a space if there are previous arguments, then append the column name
            appendComparisonOperator(compareSymbol);        // [3] Append the comparison operator after the column name, e.g., ">" "<" "="
            appendValue(valueStr, parameterizedQuery);      // [4] Append the value to the WHERE clause
            mColumnCount++;
        }

        return this;
    }

    /**
     * Overloaded method for adding a condition with a default comparison operator and logical operator.
     *
     * @param <T>       The type of the value to be converted to String.
     * @param columnName The name of the column.
     * @param value      The value to compare against.
     * @return The current instance of SQLQueryBuilder for chaining.
     */
    public <T> SQLQueryBuilder whereColumn(String columnName, T value) {
        return whereColumn(columnName, value, null, null, true);
    }

    /**
     * Overloaded method for adding a condition with a default logical operator.
     *
     * @param <T>           The type of the value to be converted to String.
     * @param columnName    The name of the column.
     * @param value         The value to compare against.
     * @param compareSymbol The comparison operator (e.g., "=").
     * @return The current instance of SQLQueryBuilder for chaining.
     */
    public <T> SQLQueryBuilder whereColumn(String columnName, T value, String compareSymbol) {
        return whereColumn(columnName, value, compareSymbol, null, true);
    }

    /**
     * Overloaded method for adding a condition with a specified logical operator.
     *
     * @param <T>           The type of the value to be converted to String.
     * @param columnName    The name of the column.
     * @param value         The value to compare against.
     * @param compareSymbol The comparison operator (e.g., "=").
     * @param logicalOp     The logical operator (e.g., "AND").
     * @return The current instance of SQLQueryBuilder for chaining.
     */
    public <T> SQLQueryBuilder whereColumn(String columnName, T value, String compareSymbol, String logicalOp) {
        return whereColumn(columnName, value, compareSymbol, logicalOp, true);
    }


    public SQLQueryBuilder pushColumnValueIfNull(boolean pushIfNull) {
        this.mPushColumnIfNull = pushIfNull;
        return this;
    }

    public SQLQueryBuilder clearOnlyReturns() {
        this.onlyReturn.clear();
        return this;
    }

    public SQLQueryBuilder onlyReturn(String... columnNames) {
        if(columnNames != null) {
            for(String s : columnNames) {
                if(s == null) continue;
                s = s.trim();
                if(!TextUtils.isEmpty(s))
                    if(!onlyReturn.contains(s))
                        this.onlyReturn.add(s);
            }
        } return this;
    }

    public SQLQueryBuilder orderColumns(String orderFieldNameOrder) {
        this.mColumnOrder = orderFieldNameOrder;
        return this;
    }

    public SQLQueryBuilder bitwise(String bitwise) {
        if(COMPARE_SYMBOLS.contains(bitwise)) this.mOpBitwise = bitwise;
        return this;
    }

    public SQLQueryBuilder opSymbol(String symbol) {
        String up = symbol.toUpperCase();
        if(OP_SYMBOLS.contains(up)) this.mOpSymbol = up;
        return this;
    }

    public SQLQueryBuilder table(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * Ensures the WHERE clause is ready for appending additional conditions by adding a logical operator
     * like "AND" or "OR" if there are already existing conditions.
     *
     * @param logicalOperator The logical operator to append, such as "AND" or "OR". If null, the default operator is used.
     */
    private void appendLogicalOperator(String logicalOperator) {
        // Ensure the logical operator is valid and convert it to uppercase for consistency.
        String opUpper = logicalOperator != null ? logicalOperator.toUpperCase() : null;
        String opSym = TextUtils.isEmpty(opUpper) || !OP_SYMBOLS.contains(opUpper) ? mOpSymbol : opUpper;

        // If there are previous arguments, append the logical operator.
        if (mColumnCount != 0)
            whereClause.append(" ").append(opSym);
    }

    /**
     * Appends a column name to the WHERE clause, ensuring proper spacing if previous conditions exist.
     *
     * @param columnName The name of the column to append to the WHERE clause.
     */
    private void appendColumnName(String columnName) {
        // Add a space if the WHERE clause already has conditions, then append the column name.
        whereClause.append(" ")
                .append(columnName);
    }

    /**
     * Appends a comparison operator (e.g., "=", ">", "<") to the WHERE clause.
     *
     * @param compareSymbol The comparison operator to append. If null or invalid, the default operator is used.
     */
    private void appendComparisonOperator(String compareSymbol) {
        // Ensure the comparison operator is valid. Use the default operator if invalid or null.
        String sym = compareSymbol != null && COMPARE_SYMBOLS.contains(compareSymbol)
                ? compareSymbol
                : mOpBitwise;

        // Append the comparison operator to the WHERE clause.
        whereClause.append(" ").append(sym);
    }

    /**
     * Appends a value to the WHERE clause, either as a parameterized query using "?" or as a hardcoded value.
     *
     * @param value               The value to append to the WHERE clause.
     * @param parameterizedQuery  If true, the value is parameterized (using "?"); otherwise, it is hardcoded.
     */
    private void appendValue(String value, boolean parameterizedQuery) {
        if (!parameterizedQuery) {
            // Hardcode the value directly in the query.
            whereClause.append(" ").append(value);
            return;
        }

        // Use parameterized query by appending "?" and adding the value to the argument list.
        whereClause.append(" ").append(VALUE_BIND);
        if (value != null)
            whereArgs.add(value);
    }


    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .appendFieldLine("Table Name", this.tableName)
                .appendFieldLine("Where Clause", this.whereClause.toString())
                .appendFieldLine("Where Args", Str.joinList(this.whereArgs, ","))
                .appendFieldLine("Only Return", Str.joinList(this.onlyReturn, ","))
                .appendFieldLine("Order By", mColumnOrder)
                .toString();
    }
}
