package eu.faircode.xlua.x.ui.core.interfaces;

import eu.faircode.xlua.x.ui.core.UserClientAppContext;

public interface IUserContext {
    int getIcon();
    int getUserId();

    int getAppUid();
    String getAppName();
    String getAppPackageName();

    boolean isGlobal();

    UserClientAppContext getUserContext();
    void setUserContext(UserClientAppContext context);
    boolean hasContext();

    void ensureHasUserContext(boolean useGlobalIfNull);
}
