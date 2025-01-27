package eu.faircode.xlua.ui;

import android.content.Context;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.TextDividerItemDecoration;

public class GroupHelper {
    public static TextDividerItemDecoration createGroupDivider(Context context) {
        TextDividerItemDecoration textDividerItemDecoration = new TextDividerItemDecoration(context);
        textDividerItemDecoration.setUseIndependentDividers(false);
        textDividerItemDecoration.setBarCornerRadius(20.0f);
        textDividerItemDecoration.setLeftBarToStartParentPadding(50);
        textDividerItemDecoration.setRightBarToEndParentPadding(50);
        textDividerItemDecoration.enableLineDivider(true, 85);
        textDividerItemDecoration.setDividerPosition(false);
        textDividerItemDecoration.setLinkDividersToGroupIDs(true);
        textDividerItemDecoration.setTextPaddingLeft(50);
        textDividerItemDecoration.initColors(context);
        textDividerItemDecoration.setDividerTopPadding(50);
        textDividerItemDecoration.setDividerBottomPadding(50);
        textDividerItemDecoration.setTextPaddingBottom(0);
        textDividerItemDecoration.setTextPaddingTop(0);
        textDividerItemDecoration.setTextVerticalAlignment(TextDividerItemDecoration.TextVerticalAlignment.CENTER);
        textDividerItemDecoration.setTextSize(65);
        textDividerItemDecoration.setTextAlignment(3);
        textDividerItemDecoration.setLeftBarToStartParentPadding(80);
        return textDividerItemDecoration;
    }

    public static String getGroupId(String str) {
        String firstString = Str.getFirstString(str, ".", "Unknown");
        return (firstString.equalsIgnoreCase("record") || firstString.equalsIgnoreCase("send") || firstString.equalsIgnoreCase("use")) ? "Usage" : firstString.equalsIgnoreCase("id") ? "Identification" : firstString.equalsIgnoreCase("ad") ? "Advertisement & Analytics" : firstString;
    }

}
