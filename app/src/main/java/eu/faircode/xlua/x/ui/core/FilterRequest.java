package eu.faircode.xlua.x.ui.core;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;

public class FilterRequest {
    public static FilterRequest create() { return new FilterRequest(); }
    public static FilterRequest create(String query) { return new FilterRequest().setQuery(query); }
    public static FilterRequest create(String query, String order) { return new FilterRequest().setQuery(query).setOrder(order); }
    public static FilterRequest create(String query, String order, boolean isReversed) { return new FilterRequest().setQuery(query).setOrder(order).setIsReversed(isReversed); }

    public String query;
    public String order;
    public final List<String> filterTags = new ArrayList<>();
    public boolean isReversed = false;

    public boolean hasQuery() { return query != null; }
    public boolean hasOrder() { return order != null; }
    public boolean hasFilterTags() { return !filterTags.isEmpty(); }

    public String getQueryOrDefault() { return getQueryOrDefault(""); }
    public String getQueryOrDefault(String defaultValue) { return query == null ? defaultValue : query; }
    public String getOrderOrDefault(String defaultValue) { return order == null ? defaultValue : order; }


    public boolean isEmptyOrClearQuery() {
        return TextUtils.isEmpty(this.query);
    }

    public FilterRequest setQuery(String query) {
        this.query = query;
        return this;
    }

    public FilterRequest setOrder(String order) {
        this.order = order;
        return this;
    }

    public FilterRequest setIsReversed(boolean isReversed) {
        this.isReversed = isReversed;
        return this;
    }

    public FilterRequest setFilterTags(String... tags) {
        filterTags.clear();
        filterTags.addAll(Arrays.asList(tags));
        return this;
    }

    public FilterRequest setFilterTags(List<String> tags) {
        filterTags.clear();
        filterTags.addAll(tags);
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Query", this.query)
                .appendFieldLine("Order", this.order)
                .appendFieldLine("Is Reversed", this.isReversed)
                .appendFieldLine("Filter Tags", Str.joinList(filterTags))
                .toString(true);
    }
}
