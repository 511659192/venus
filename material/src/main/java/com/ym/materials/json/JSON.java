package com.ym.materials.json;


import com.ym.materials.json.serializer.*;

public class JSON {

    static final SerializeFilter[] emptyFilters = new SerializeFilter[0];

    public static int DEFAULT_GENERATE_FEATURE;

    static {

    }


    public static String toJSONString(Object object) {
        return toJSONString(object, emptyFilters);
    }

    private static String toJSONString(Object object,
                                       SerializeFilter[] filters,
                                       SerializerFeature... features) {
        return toJSONString(object, SerializeConfig.globalInstance, filters, null, DEFAULT_GENERATE_FEATURE, features);
    }

    private static String toJSONString(Object object,
                                       SerializeConfig config,
                                       SerializeFilter[] filters,
                                       String dateFormat,
                                       int defaultFeatures,
                                       SerializerFeature... features) {
        SerializeWriter out = new SerializeWriter(null, defaultFeatures, features);
        try {

            JSONSerializer serializer = new JSONSerializer(out, config);
            if (dateFormat != null && dateFormat.length() != 0) {
                serializer.setDateFormat(dateFormat);
                serializer.config(SerializerFeature.WriteDateUseDateFormat, true);
            }

            if (filters != null) {
                for (SerializeFilter filter : filters) {
                    serializer.addFilter(filter);
                }
            }


        } finally {
            out.close();
        }


        return null;
    }

}
