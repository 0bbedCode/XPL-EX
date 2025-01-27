package eu.faircode.xlua.x.xlua.settings.interfaces;

import java.util.HashMap;

public interface INameResolver {
    boolean ensureNamed(HashMap<String, String> map);
}
