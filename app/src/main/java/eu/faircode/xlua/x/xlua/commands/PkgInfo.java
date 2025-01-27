package eu.faircode.xlua.x.xlua.commands;

public class PkgInfo implements IPkgInfo {
    public XPackageInfo packageInfo = new XPackageInfo();

    public int getUid() { return packageInfo.uid; }
    public int getUserId() { return packageInfo.getUserId(); }

    public String getPackageOrCategory() { return packageInfo.packageName; }
    public boolean getKill() { return packageInfo.kill; }

    public boolean isGlobal() { return false; }

    public void setUid(int uid) {
        packageInfo.uid = uid;
    }

    public void setUser(int user) {
        packageInfo.userId = user;
    }

    public void setPackageOrCategory(String packageNameOrCategory) {
        packageInfo.packageName = packageNameOrCategory;
    }

    @Override
    public void consumePackageInfo(XPackageInfo packageInfo) {
        if(packageInfo != null) {
            this.packageInfo = packageInfo;
        }
    }
}
