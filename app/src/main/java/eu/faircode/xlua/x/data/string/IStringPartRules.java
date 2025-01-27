package eu.faircode.xlua.x.data.string;

import java.util.List;

public interface IStringPartRules {
    boolean isDelimiterKind(char c);
    String cleanPart(String s);
}
