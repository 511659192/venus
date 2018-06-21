package com.ym.materials.json.serializer;

public enum SerializerFeature {
    WriteDateUseDateFormat,
    WriteEnumUsingToString,
    WriteEnumUsingName,
    QuoteFieldNames,
    UseSingleQuotes,
    SortField,
    DisableCircularReferenceDetect,
    BeanToArray,
    WriteNonStringValueAsString,
    NotWriteDefaultValue,
    BrowserCompatible,
    PrettyFormat,
    WriteSlashAsSpecial,
    IgnoreErrorGetter,
    WriteClassName,
    BrowserSecure,
    WriteNullListAsEmpty,
    WriteNullStringAsEmpty,
    WriteNullBooleanAsFalse,
    WriteNullNumberAsZero;

    SerializerFeature() {
        mask = (1 << ordinal());
    }

    public final int mask;

    public final int getMask(){
        return mask;
    }
}
