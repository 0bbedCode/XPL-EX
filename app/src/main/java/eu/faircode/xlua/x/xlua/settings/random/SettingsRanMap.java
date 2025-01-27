package eu.faircode.xlua.x.xlua.settings.random;

/*
    ToDO Make this a Repo
            This class can be useful to help identify all Lists of "items"
            Can keep track , Containers, Holders etc all from Here while the U.I Components grab / utilize this as some Soft Repo
            Handle Bindings etc
 */
/*public class SettingsRanMap {
    private static final String TAG = "XLua.RandomizerElementsMap";

    public final Map<String, IRandomizer> randomizers = RandomizersCache.getCopy();

    public final HashMap<SettingHolder, IRandomizer> settings = new HashMap<>();
    public final HashMap<SettingHolder, IRandomizer> onView = new HashMap<>();


    public final ViewStateRegistry viewRegistry = new ViewStateRegistry();

    public void parseContainers(List<SettingsContainer> containers) {
        synchronized (settings) {
            settings.clear();
            for(SettingsContainer container : containers) {
                for(SettingHolder holder : container.getSettings()) {
                    IRandomizer randomizer = randomizers.get(holder.getName());
                    settings.put(holder, randomizer);
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Settings Size=%s", settings.size()));
        }
    }

    public void bind(TextInputEditText textInputEdit, )
}  */
