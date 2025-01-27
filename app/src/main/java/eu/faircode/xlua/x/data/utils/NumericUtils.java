package eu.faircode.xlua.x.data.utils;

public class NumericUtils {
    public static final int FAIL_CODE_INT = -1;

    @FunctionalInterface
    public interface IntegerOperation {  int execute(); }

    public static int ensureIntSuccessOrOperation(int initial, IntegerOperation operation, int defaultOperationBad) {
        return initial == FAIL_CODE_INT ?
                valueGreaterThanZeroOrDefault(operation.execute(), defaultOperationBad) : initial;
    }

    public static int ensureIntSuccessOrOperation(int initial, int failCode, IntegerOperation operation, int defaultOperationBad) {
        return initial == failCode ?
                valueGreaterThanZeroOrDefault(operation.execute(), defaultOperationBad) : initial;
    }

    public static int valueGreaterThanZeroOrDefault(int a, int defaultValue) {
        return a > 0 ? a : defaultValue;
    }

}
