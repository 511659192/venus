package com.ym.materials.json.serializer;

import com.ym.materials.json.serializer.filters.BeforeFilter;
import com.ym.materials.json.serializer.filters.PropertyPreFilter;

import java.util.ArrayList;
import java.util.List;

public class SerializeFilterable {

    protected List<BeforeFilter>       beforeFilters       = null;
    protected List<PropertyPreFilter>  propertyPreFilters  = null;

    protected boolean                  writeDirect         = true;

    public List<BeforeFilter> getBeforeFilters() {
        if (beforeFilters == null) {
            beforeFilters = new ArrayList<BeforeFilter>();
            writeDirect = false;
        }

        return beforeFilters;
    }

    public void addFilter(SerializeFilter filter) {
        if (filter == null) {
            return;
        }

        if (filter instanceof PropertyPreFilter) {

        }


    }

    public List<PropertyPreFilter> getPropertyPreFilters() {
        if (propertyPreFilters == null) {
            propertyPreFilters = new ArrayList<PropertyPreFilter>();
            writeDirect = false;
        }

        return propertyPreFilters;
    }
}
