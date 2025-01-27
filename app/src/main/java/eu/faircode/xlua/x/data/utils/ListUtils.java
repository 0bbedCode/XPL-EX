package eu.faircode.xlua.x.data.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListUtils {


    public static String[] toStringArray(List<String> lst) {
        if(lst == null || lst.isEmpty()) return new String[] { };
        return lst.toArray(new String[0]);
    }

    public static <T> List<T> combineElements(T[] arrayOne, T[] arrayTwo, boolean checkIfContains) {
        List<T> elements = new ArrayList<>();
        if(ArrayUtils.isValid(arrayOne))
            elements.addAll(Arrays.asList(arrayOne));

        if(ArrayUtils.isValid(arrayTwo)) {
            if(checkIfContains) {
                for(T e : arrayTwo) {
                    if(e != null && !elements.contains(e))
                        elements.add(e);
                }
            } else {
                elements.addAll(Arrays.asList(arrayTwo));
            }
        }

        return elements;
    }
}
