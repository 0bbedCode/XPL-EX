package eu.faircode.xlua.x.xlua.commands.packet;

public interface IQueryPacketObject {
    void readSelectionFromQuery(String[] selection, int flags);
}
