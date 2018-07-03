package com.ym.materials.gson.internal.bind;

import com.ym.materials.gson.Gson;
import com.ym.materials.gson.TypeAdapter;
import com.ym.materials.gson.TypeAdapterFactory;
import com.ym.materials.gson.internal.ConstructorConstructor;
import com.ym.materials.gson.internal.ObjectConstructor;
import com.ym.materials.gson.internal.Types;
import com.ym.materials.gson.reflect.TypeToken;
import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonToken;
import com.ym.materials.gson.stream.JsonWriter;
import jdk.nashorn.internal.parser.Token;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Created by ym on 2018/7/3.
 */
public class CollectionTypeAdapterFactory implements TypeAdapterFactory {

    private final ConstructorConstructor constructorConstructor;

    public CollectionTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
        this.constructorConstructor = constructorConstructor;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Type type = typeToken.getType();
        Class<? extends T> rawType = typeToken.getRawType();
        if (!Collection.class.isAssignableFrom(rawType)) {
            return null;
        }

        Type elementType = Types.getCollectionElementType(type, rawType);
        TypeAdapter adapter = gson.getAdapter(typeToken.get(elementType));
        ObjectConstructor<T> constructor = constructorConstructor.get(typeToken);
        TypeAdapter<T> result = new Adapter(gson, elementType, adapter, constructor);
        return result;
    }

    private static final class Adapter<T> extends TypeAdapter<Collection<T>> {

        private final TypeAdapter<T> elementTypeAdapter;
        private final ObjectConstructor<? extends Collection<T>> constructor;

        public Adapter(Gson context, Type elementType, TypeAdapter<T> elementTypeAdapter,
                           ObjectConstructor<? extends Collection<T>> constructor) {
            this.elementTypeAdapter = new TypeAdapterRuntimeTypeWrapper<T>(context, elementTypeAdapter, elementType);
            this.constructor = constructor;
        }

        @Override
        public Collection<T> read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            Collection<T> collection = constructor.construct();
            in.beginArray();
            while (in.hasNext()) {
                T instance = elementTypeAdapter.read(in);
                collection.add(instance);
            }
            in.endArray();
            return collection;
        }

        @Override
        public void write(JsonWriter out, Collection<T> collection) throws IOException {
            if (collection == null) {
                out.nullValue();
                return;
            }

            out.beginArray();
            for (T element : collection) {
                elementTypeAdapter.write(out, element);
            }
            out.endArray();
        }
    }
}
