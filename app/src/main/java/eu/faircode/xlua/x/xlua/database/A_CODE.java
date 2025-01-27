package eu.faircode.xlua.x.xlua.database;

import android.os.Bundle;

public enum A_CODE {
    NONE(9999),
    SUCCESS(0),
    FAILED(1),
    PERMISSION_DENIED_FILE(2),
    PERMISSION_DENIED_DB_X(3),
    PERMISSION_DENIED_DB_M(4),
    INVALID_UID(5),
    INVALID_USER(6),
    INVALID_CATEGORY(7),
    INVALID_CALLER(8),
    MISSING_DB_ENTRY_X(9),
    MISSING_DB_ENTRY_M(10),
    GENERIC_DB_ERROR_X(11),
    GENERIC_DB_ERROR_M(12),
    GENERIC_INVALID_ARGS(13),
    GENERIC_INVALID_INPUT(14),

    ERROR_TABLE_PREPARE_X(14),
    ERROR_TABLE_PREPARE_M(15),
    ERROR_TABLE_BEGIN_TRANSACTION_X(16),
    ERROR_TABLE_BEGIN_TRANSACTION_M(17),


    FILE_NOT_EXIST(999);

    public static final String FIELD_RESULT = "result_code";

    private final int value;
    A_CODE(int value) { this.value = value; }
    public int getValue() { return value; }

    public Bundle toBundle() { Bundle b = new Bundle(); b.putInt(FIELD_RESULT, value); return b; }

    public static A_CODE fromBundle(Bundle b) {
        if(b == null || !b.containsKey(FIELD_RESULT)) return NONE;
        return fromInt(b.getInt(FIELD_RESULT));
    }

    public static Bundle toBundle(A_CODE result) {
        Bundle b = new Bundle();
        b.putInt(FIELD_RESULT, result.value);
        return b;
    }

    public static A_CODE result(boolean result) { return result ? A_CODE.SUCCESS: FAILED; }

    public static A_CODE resultToCode_x(boolean result) { return result ? A_CODE.SUCCESS : A_CODE.GENERIC_DB_ERROR_X; }
    public static A_CODE resultToCode_m(boolean result) { return result ? A_CODE.SUCCESS : A_CODE.GENERIC_DB_ERROR_M; }


    public static boolean isSuccessful(A_CODE code) { return code == SUCCESS; }

    public static A_CODE fromInt(int flags) {
        switch (flags) {
            case 0: return SUCCESS;
            case 1: return FAILED;
            case 2: return PERMISSION_DENIED_FILE;
            case 3: return PERMISSION_DENIED_DB_X;
            case 4: return PERMISSION_DENIED_DB_M;
            case 5: return INVALID_UID;
            case 6: return INVALID_USER;
            case 7: return INVALID_CATEGORY;
            case 8: return INVALID_CALLER;
            case 9: return MISSING_DB_ENTRY_X;
            case 10: return MISSING_DB_ENTRY_M;
            case 11: return GENERIC_DB_ERROR_X;
            case 12: return GENERIC_DB_ERROR_M;
            case 13: return GENERIC_INVALID_ARGS;
            case 14: return GENERIC_INVALID_INPUT;
            case 15: return FILE_NOT_EXIST;
            default: return NONE;
        }
    }
}
