package eu.faircode.xlua.api.xstandard.database;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.utilities.StringUtil;

public class SqlQueryBuilder {
    private static final String TAG = "XLua.DatabaseQueryBuilder";

    protected XDatabaseOld db;
    protected String tableName;

    protected List<String> compareValues = new ArrayList<>();
    protected List<String> onlyReturn = new ArrayList<>();

    private String symbolWithCompare = null;//" = ?";
    private boolean useOr = false;

    protected StringBuilder selectionArgsBuilder = new StringBuilder();
    protected String orderOrFieldName = null;
    protected int fCount = 0;

    public String getTableName() {
        return tableName;
    }
    public String getSelectionArgs() {
         return selectionArgsBuilder.toString();
    }

    public String[] getSelectionCompareValues() {
        if(compareValues.isEmpty())
            return new String[] { };

        return compareValues.toArray(new String[0]);
    }

    public SqlQueryBuilder() { }
    public SqlQueryBuilder(XDatabaseOld db, String tableName) {
        this.db = db;
        this.tableName = tableName;
    }

    protected void internal_setSymbol(String symbol) {
        internal_setSymbol(symbol.replace(" ", ""), "?");
    }

    protected void internal_setSymbol(String symbol, String compareValue) {
        if(!isOperator(symbol))
            return;

        this.symbolWithCompare = " " + symbol + " " + compareValue;
    }

    protected void internal_useOr(boolean useOr) {
        this.useOr = useOr;
    }

    protected void internal_whereColumnsEquals(String... columnNames) {
        for(String f : columnNames)
            internal_whereColumnBinds(f, null);
    }

    protected void internal_anchorValuesWithFields(String... values) {
        compareValues.addAll(Arrays.asList(values));
    }

    private static final String SYMBOL_EQUALS = "=";
    private static final String SYMBOL_WILD = "?";
    private static final String SYMBOL_SPACE = " ";
    private static final String SYMBOL_OR = "OR";
    private static final String SYMBOL_AND = "AND";

    private static final List<String> symbols = Arrays.asList("<", ">", "=", ">=", "<=", "=>", "=<", "==");

    protected void internal_whereColumnBinds(String columnName, String value) { internal_whereColumnBinds(columnName, value, null); }
    protected void internal_whereColumnBinds(String columnName, String value, String symbol) {
        if(!StringUtil.isValidString(columnName))
            return;

        //
        //
        //columnName    (column to perform check)
        //symbol        (symbol to use compare)
        //value         (value to perform comparison on)    if its (?) then we bind it from a compare values list else direct bind it
        //
        //<= >= =
        //
        //
        if(fCount != 0) selectionArgsBuilder.append(useOr ? " OR " : " AND ");
        selectionArgsBuilder.append(columnName);
        selectionArgsBuilder.append(" ");
        if(symbol == null) {
            if(symbolWithCompare == null) {
                selectionArgsBuilder.append(SYMBOL_EQUALS);
                selectionArgsBuilder.append(" ?");
            }else {
                String sc = symbolWithCompare.trim();
                selectionArgsBuilder.append(sc);
                if(!sc.endsWith("?") && symbols.contains(sc))
                    selectionArgsBuilder.append(" ?");
            }
        }else {
            symbol = symbol.trim();
            if(symbol.endsWith("*")) {
                if(symbol.length() > 1) selectionArgsBuilder.append(symbol.substring(0, symbol.length() - 1));
                else selectionArgsBuilder.append(SYMBOL_EQUALS);
                selectionArgsBuilder.append(" ");
                selectionArgsBuilder.append(value);
                fCount++;
                return;
            }else {
                selectionArgsBuilder.append(symbol);
                if(symbol.length() <= 2) {
                    if(!symbol.endsWith("?") && symbols.contains(symbol))
                        selectionArgsBuilder.append(" ?");
                }
            }
        }


        //compareValues.add(value);
        /*if(symbol == null)
            symbol = symbolWithCompare;
        if(symbol != null) {
        }
        if(fCount != 0)
            selectionArgsBuilder.append(useOr ? " OR " : " AND ");
        if(symbol != null) {
            if(symbol.equals("*")) {
                //symbol is going to be " value"
            }
            symbol = symbol.replace(" ", "");
            //String compEnd = value == null ? " ?" : " " + value.trim();
            symbol = " " + symbol; //+ compEnd;
            //eww this looks ugly fix later
        }*/

        //selectionArgsBuilder.append(columnName);
        //selectionArgsBuilder.append(symbol == null ? symbolWithCompare : symbol);
        if(value != null)
            compareValues.add(value);
        fCount++;
    }

    protected void internal_onlyReturnColumn(String columnName) {
        Log.i(TAG, "only setting return=" + columnName);
        if(!onlyReturn.contains(columnName))
            onlyReturn.add(columnName);
    }

    protected void internal_orderBy(String orderBy) {
        orderOrFieldName = orderBy;
    }

    protected void internal_onlyReturnColumns(String... fields) {
        for(String f : fields)
            if(!onlyReturn.contains(f))
                onlyReturn.add(f);
    }

    private static final String op_pattern = "^[=!<>]+$";
    private static boolean isOperator(String symbol) {
        return symbol.length() < 3 && symbol.matches(op_pattern);
    }
}
