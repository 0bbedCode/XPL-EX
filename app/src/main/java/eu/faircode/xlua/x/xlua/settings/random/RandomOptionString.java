package eu.faircode.xlua.x.xlua.settings.random;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;

public class RandomOptionString extends RandomElement {
    public static IRandomizer create(String value) { return new RandomOptionString(value); }
    public static IRandomizer create(String displayName, String value) {  return new RandomOptionString(displayName, value); }

    public String value;

    @Override
    public String getRawValue() { return value; }

    public RandomOptionString(String value) {
        super(value);
        this.value = value;
    }

    public RandomOptionString(String displayName, String value) {
        super(displayName);
        this.value = value;
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), value);
    }

    public static IRandomizer[] generate(String... elements) { return generate(true, elements); }
    public static IRandomizer[] generate(boolean generateNullFirstItem, String... elements) {
        List<IRandomizer> items = new ArrayList<>();
        if(generateNullFirstItem)
            items.add(RandomOptionNullElement.create());

        if(ArrayUtils.isValid(elements)) {
            List<String> added = new ArrayList<>();
            for(String element : elements) {
                if(!added.contains(element)) {
                    items.add(RandomOptionString.create(element));
                    added.add(element);
                }
            }
        }

        IRandomizer[] array = new IRandomizer[items.size()];
        for(int i = 0; i < items.size(); i++) {
            array[i] = items.get(i);
        }

        return array;
    }
}
