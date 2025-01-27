package eu.faircode.xlua.x.ui.core.interfaces;

import eu.faircode.xlua.x.data.PrefManager;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.repos.IXLuaRepo;

public interface IRepositoryContainer<TElement> {
    void setUserContext(UserClientAppContext userContext);
    void setRepository(IXLuaRepo<TElement> repository);

    UserClientAppContext getUserContext();
    IXLuaRepo<TElement> getRepository();
    PrefManager getPreferences();
}
