package eu.faircode.xlua.api.xstandard.interfaces;

import java.util.List;

import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.ShellInterception;

public interface ICommandIntercept {
    boolean interceptCommand(ShellInterception result);
    String getCommand();
    boolean containsCommand(String input);
    boolean containsCommand(List<String> commands);
    boolean keepGoing(UserContextMaps maps, String key);
}
