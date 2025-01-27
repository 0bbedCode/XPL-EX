package eu.faircode.xlua.utilities;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.lang.reflect.Field;
import java.util.List;

import eu.faircode.xlua.ActivityBase;
import eu.faircode.xlua.AdapterApp;
import eu.faircode.xlua.R;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.DataNullElement;
import eu.faircode.xlua.random.elements.IManagedSpinnerElement;
import eu.faircode.xlua.random.elements.ISpinnerElement;

public class UiUtil {
    private static final String TAG = "XLua.UiUtil";
    public static final int CIRCLE_DIAMETER = 64;

    public static Intent createSaveFileIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return intent;
    }

    public static Intent createOpenFileIntent() { return createOpenFileIntent("*/*"); }
    public static Intent createOpenFileIntent(String extension) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(extension); // Use "image/*" for images, "application/pdf" for PDF, etc.
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    public static void initTheme(FragmentActivity activity, String theme) {
        try { initTheme((ActivityBase) activity, theme);
        }catch (Exception e) { XLog.e("Failed to Cast Fragment Activity to Activity Base: is null=" + (activity == null) + " theme=" + theme, e, true); }
    }

    public static void initTheme(ActivityBase activity, String theme) {
        if(activity == null) { XLog.e("ActivityBase argument is NULL: theme=" + theme, new Throwable(), true); return; }
        if(theme == null) theme = "dark";
        if (!theme.equals(activity.getThemeName())) activity.recreate();
    }

    public static AdapterApp.enumShow getShow(Context context) {
        String show = XLuaCall.getSettingValue(context, "show");
        if (show != null && show.equals("user")) return AdapterApp.enumShow.user;
        else if (show != null && show.equals("all")) return AdapterApp.enumShow.all;
        else return AdapterApp.enumShow.icon;
    }

    public static boolean initRandomizer(ArrayAdapter<IRandomizerOld> adapterRandomizer, Spinner spRandomSelector, LuaSettingExtended setting, List<IRandomizerOld> randomizers) {
        adapterRandomizer.clear();
        IRandomizerOld randomizer = setting.getRandomizer();
        boolean enable = false;
        if(randomizer != null) {
            List<ISpinnerElement> elements = randomizer.getOptions();
            if(randomizer.isSetting(setting.getName()) && elements != null && !elements.isEmpty()) {
                adapterRandomizer.addAll(elements);//here we can compare values
                boolean found = false;
                if(setting.isModified()) {
                    String setModValue = setting.getModifiedValue();
                    if(setModValue != null && !TextUtils.isEmpty(setModValue)) {
                        for(int i = 0; i < adapterRandomizer.getCount(); i++) {
                            IRandomizerOld r = adapterRandomizer.getItem(i);
                            if(r == null) continue;
                            if(r instanceof ISpinnerElement) {
                                ISpinnerElement spe = (ISpinnerElement) r;
                                if(spe.getName().equals(DataNullElement.EMPTY_ELEMENT.getName())) continue;
                                if(spe.getValue().equalsIgnoreCase(setModValue)) {
                                    spRandomSelector.setSelection(i);
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if(!found) {
                    for(int i = 0; i < adapterRandomizer.getCount(); i++) {
                        IRandomizerOld r = adapterRandomizer.getItem(i);
                        if(r == null) continue;
                        if(r.getName().equals(DataNullElement.EMPTY_ELEMENT.getName())) {
                            spRandomSelector.setSelection(i);
                            break;
                        }
                    }
                }
            }
            else {
                adapterRandomizer.addAll(randomizers);
                boolean found = false;
                for(int i = 0; i < adapterRandomizer.getCount(); i++) {
                    IRandomizerOld r = adapterRandomizer.getItem(i);
                    if(r == null) continue;
                    //if(r.isSetting(randomizer))
                    if(r.getName().equalsIgnoreCase(randomizer.getName())) {
                        spRandomSelector.setSelection(i);
                        break;
                    }
                }
            } enable = true;
        }

        return enable;
    }

    public static boolean handleSpinnerSelection(Spinner spRandomizer, LuaSettingExtended setting) {
        IRandomizerOld selected = (IRandomizerOld) spRandomizer.getSelectedItem();
        String name = selected.getName();
        try {
            if (name == null ? spRandomizer.getTag() != null : !name.equals(spRandomizer.getTag())) {
                XLog.i("Selected Randomizer Drop Down spinner Modified. randomizer=" + name);
                spRandomizer.setTag(name);
            }

            if(setting == null)
                return false;

            IRandomizerOld randomizer = setting.getRandomizer();
            if(randomizer != null) {
                List<ISpinnerElement> options = randomizer.getOptions();
                if(options != null && !options.isEmpty() && (randomizer.isSetting(setting.getName()))) {
                    if(selected instanceof ISpinnerElement) {
                        ISpinnerElement element = (ISpinnerElement) selected;
                        if(!element.getName().equals(DataNullElement.EMPTY_ELEMENT.getName())) {
                            if(selected instanceof IManagedSpinnerElement) {
                                IManagedSpinnerElement managedElement = (IManagedSpinnerElement)element;
                                setting.setModifiedValue(managedElement.generateString(spRandomizer.getContext()), true);
                            }else setting.setModifiedValue(element.getValue(), true);
                            //SettingUtil.initCardViewColor(spRandomizer.getContext(), tvSettingName, cvSetting, setting);
                            return true;
                        }
                    } return false;
                }
            } setting.bindRandomizer(selected);
        }catch (Exception e) { XLog.e("Failed to Init Randomizer Drop Down Spinner.", e); }
        return false;
    }

    public static boolean handleSpinnerSelection(Spinner spRandomizer, List<LuaSettingExtended> filtered, int position) {
        IRandomizerOld selected = (IRandomizerOld) spRandomizer.getSelectedItem();
        String name = selected.getName();
        try {
            if (name == null ? spRandomizer.getTag() != null : !name.equals(spRandomizer.getTag())) {
                XLog.i("Selected Randomizer Drop Down spinner Modified. randomizer=" + name);
                spRandomizer.setTag(name);
            }

            LuaSettingExtended setting = filtered.get(position);
            if(setting == null)
                return false;

            IRandomizerOld randomizer = setting.getRandomizer();
            if(randomizer != null) {
                List<ISpinnerElement> options = randomizer.getOptions();
                if(options != null && !options.isEmpty() && (randomizer.isSetting(setting.getName()))) {
                    if(selected instanceof ISpinnerElement) {
                        ISpinnerElement element = (ISpinnerElement) selected;
                        if(!element.getName().equals(DataNullElement.EMPTY_ELEMENT.getName())) {
                            if(selected instanceof IManagedSpinnerElement) {
                                IManagedSpinnerElement managedElement = (IManagedSpinnerElement)element;
                                setting.setModifiedValue(managedElement.generateString(spRandomizer.getContext()), true);
                            }else setting.setModifiedValue(element.getValue(), true);
                            //SettingUtil.initCardViewColor(spRandomizer.getContext(), tvSettingName, cvSetting, setting);
                            return true;
                        }
                    } return false;
                }
            } setting.bindRandomizer(selected);
        }catch (Exception e) { XLog.e("Failed to Init Randomizer Drop Down Spinner.", e); }
        return false;
    }

    public static void initRv(Context context, RecyclerView rv, RecyclerView.Adapter<?> adapter) {
        try {
            rv.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(context);
            llm.setAutoMeasureEnabled(true);
            rv.setLayoutManager(llm);
            rv.setAdapter(adapter);
        }catch (Exception e) {
            XLog.e("Failed to init Recycler View. ", e, true);
        }
    }

    public static void initFloatingActionButtonAnimations(Context context, Animation fabOpen, Animation fabClose, Animation fromBottom, Animation toBottom) {
        try {
            fabOpen = AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim_one);
            fabClose = AnimationUtils.loadAnimation(context,R.anim.rotate_close_anim_one);
            fromBottom = AnimationUtils.loadAnimation(context,R.anim.from_bottom_anim_one);
            toBottom = AnimationUtils.loadAnimation(context,R.anim.to_bottom_anim_one);
        }catch (Exception e) {
            Log.e(TAG, "Failed to init Floating Action Buttons... e=" + e);
        }
    }

    public static void setSwipeRefreshLayoutEndOffset(Context context, SwipeRefreshLayout srl, int startOffset) {
        if(srl == null || context == null) {
            Log.e(TAG, "[setSwipeRefreshLayoutEndOffset] Context or SwipeRefreshLayout is null...");
            return;
        }

        int offsetRaw = getSwipeRefreshEndOffset(context);
        if(offsetRaw == -1) {
            try {
                Field mSpinnerOffsetEndField = SwipeRefreshLayout.class.getDeclaredField("mSpinnerOffsetEnd");
                mSpinnerOffsetEndField.setAccessible(true);
                int mSpinnerOffsetEnd = (int) mSpinnerOffsetEndField.get(srl);
                if(mSpinnerOffsetEnd > 0)
                    offsetRaw = mSpinnerOffsetEnd;
                else throw new Exception("Field Error");
            }catch (Exception e) {
                Log.e(TAG, "Failed to use Reflection to get Current SwipeRefreshLayout [mSpinnerOffsetEnd] Field... hardcoding it too (130) " + e);
                offsetRaw = 130;
            }
        }

        srl.setProgressViewOffset(false, startOffset, startOffset + offsetRaw);
    }

    public static int getSwipeRefreshEndOffset(Context context) {
        try {
            final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return (int) (CIRCLE_DIAMETER * metrics.density);
        }catch (Exception e) {
            Log.e(TAG, "Failed to calculate offset for Circle Refresh Swipe thingy: " + e);
            return -1;
        }
    }
}
