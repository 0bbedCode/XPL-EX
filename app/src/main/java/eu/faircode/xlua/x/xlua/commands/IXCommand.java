package eu.faircode.xlua.x.xlua.commands;

public interface IXCommand {
    boolean requiresPermissionCheck();
    boolean requiresSingleThread();
    boolean isMarshal();
    String getCommandName();
}
