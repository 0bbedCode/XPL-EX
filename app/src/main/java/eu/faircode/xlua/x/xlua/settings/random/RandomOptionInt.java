package eu.faircode.xlua.x.xlua.settings.random;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;

public class RandomOptionInt extends RandomElement {
    public static IRandomizer create(int value) { return new RandomOptionInt(value); }
    public static IRandomizer create(String displayName, int value) { return new RandomOptionInt(displayName, value); }

    public int value;

    @Override
    public String getRawValue() { return String.valueOf(value); }

    public RandomOptionInt(int value) {
        super(String.valueOf(value));
        this.value = value;
    }

    public RandomOptionInt(String displayName, int value) {
        super(displayName);
        this.value = value;
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), String.valueOf(value));
    }

    public static IRandomizer[] generateOptions(int... values) { return generateOptions(true, values); }
    public static IRandomizer[] generateOptions(boolean generateNullFirstItem, int... values) {
        List<IRandomizer> items = new ArrayList<>();
        if(generateNullFirstItem)
            items.add(RandomOptionNullElement.create());

        if(ArrayUtils.isValid(values)) {
            List<Integer> added = new ArrayList<>();
            for(int v : values) {
                if(!added.contains(v)) {
                    items.add(RandomOptionInt.create(v));
                    added.add(v);
                }
            }
        }

        return ArrayUtils.toArray(items, IRandomizer.class);
    }

    public static IRandomizer[] generateRange(int start, int last, int... skipValues) { return generateRange(start, last, true, skipValues); }
    public static IRandomizer[] generateRange(int start, int last, boolean generateNullFirstItem, int... skipValues) {
        List<IRandomizer> items = new ArrayList<>();
        if(generateNullFirstItem)
            items.add(RandomOptionNullElement.create());

        if(start != last) {
            if(start > last) {
                int o = start;
                start = last;
                last = o;
            }

            for(int i = start; i < last + 1; i++) {
                boolean add = true;
                if(ArrayUtils.isValid(skipValues)) {
                    for(int skip : skipValues) {
                        if(skip == i) {
                            add = false;
                            break;
                        }
                    }
                }

                if(add)
                    items.add(create(i));
            }
        } else {
            items.add(RandomOptionInt.create(start));
        }

        return ArrayUtils.toArray(items, IRandomizer.class);
    }
}
