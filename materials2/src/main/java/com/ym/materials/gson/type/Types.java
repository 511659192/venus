package com.ym.materials.gson.type;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.ym.materials.gson.internal.Preconditions.checkArgument;
import static com.ym.materials.gson.internal.Preconditions.checkNotNull;

/**
 * Created by ym on 2018/7/8.
 */
public class Types {

    /**
     * 封装成自己的type类型
     *
     * @param type
     * @return
     */
    public static Type canonicalize(Type type) {
        checkNotNull(type);
        if (type instanceof Class) { // class 包含数组 需要特殊处理
            Class c = (Class) type;
            return c.isArray() ? new GenericArrayTypeImpl(c.getComponentType()) : c;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return new ParameterizedTypeImpl(p.getOwnerType(), p.getRawType(), p.getActualTypeArguments());
        } else if (type instanceof GenericArrayType) {
            GenericArrayType g = (GenericArrayType) type;
            return new GenericArrayTypeImpl(g.getGenericComponentType());
        } else if (type instanceof WildcardType) {
            WildcardType w = (WildcardType) type;
            return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());
        }

        // 变量类型 不进行处理 resoleve 会解析成实际的类型
        return type;
    }

    /**
     * 获取原始类型
     *
     * @param type
     * @return
     */
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof GenericArrayType) {
            return Array.newInstance(getRawType(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            Type rawType = p.getRawType();
            checkArgument(rawType instanceof Class);
            return (Class<?>) rawType;
        } else if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        } else if (type instanceof TypeVariable) {
            return Object.class;
        }
        return null;
    }

    public static Type resolve(Type context, Class<?> rawType, Type toResolve) {
        return resolve(context, rawType, toResolve, new HashSet<>());
    }

    public static Type resolve(Type context, Class<?> rawType, Type toResolve, Set<TypeVariable> visitedTypeVariable) {
        for (;;) {
            if (toResolve instanceof TypeVariable) {
                TypeVariable origin = (TypeVariable) toResolve;
                if (visitedTypeVariable.contains(origin)) {
                    return origin;
                }

                visitedTypeVariable.add(origin);
                Type type = resolveTypeVariable(context, rawType, origin);
                if (type == origin) {
                    return type;
                }
            } else if (toResolve instanceof Class && ((Class) toResolve).isArray()) {
                Class origin = (Class) toResolve;
                Type componentType = origin.getComponentType();
                Type newComponentType = resolve(context, rawType, componentType, visitedTypeVariable);
                return componentType == newComponentType ? origin : new GenericArrayTypeImpl(newComponentType);
            } else if (toResolve instanceof GenericArrayType) {
                GenericArrayType origin = (GenericArrayType) toResolve;
                Type componentType = origin.getGenericComponentType();
                Type newComponentType = resolve(context, rawType, componentType, visitedTypeVariable);
                return componentType == newComponentType ? origin : new GenericArrayTypeImpl(newComponentType);
            } else if (toResolve instanceof ParameterizedType) {
                ParameterizedType origin = (ParameterizedType) toResolve;
                Type ownerType = origin.getOwnerType();
                Type newOwnerType = resolve(context, rawType, ownerType, visitedTypeVariable);
                boolean changed = ownerType != newOwnerType;
                Type[] typeArguments = origin.getActualTypeArguments();
                for (int i = 0; i < typeArguments.length; i++) {
                    Type typeArgument = typeArguments[i];
                    Type newTypeArgument = resolve(context, rawType, typeArgument, visitedTypeVariable);
                    if (typeArgument != newTypeArgument) {
                        if (!changed) changed = true;
                        typeArguments[i] = newTypeArgument;
                    }
                }
                return changed ? new ParameterizedTypeImpl(newOwnerType, origin.getRawType(), typeArguments) : origin;
            } else if (toResolve instanceof WildcardType) {
                WildcardType origin = (WildcardType) toResolve;
                Type[] lowerBounds = origin.getLowerBounds();
                Type[] upperBounds = origin.getUpperBounds();
                if (lowerBounds.length == 1) {
                    Type lowerBound = resolve(context, rawType, lowerBounds[0], visitedTypeVariable);
                    return lowerBound == lowerBounds[0] ? origin : new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{lowerBound});
                }

                Type upperBound = resolve(context, rawType, upperBounds[0], visitedTypeVariable);
                return upperBound == upperBounds[0] ? origin : new WildcardTypeImpl(new Type[]{upperBound}, null);
            } else {
                return toResolve;
            }
        }
    }

    static Type resolveTypeVariable(Type context, Class<?> rawType, TypeVariable unknown) {
        Class<?> declaringClassOf = declaringClassOf(unknown);
        if (declaringClassOf == null) {
            return unknown;
        }

        Type declaredBy = getGenericSupertype(context, rawType, declaringClassOf);
        if (declaredBy instanceof ParameterizedType) {
            int index = indexOf(rawType.getTypeParameters(), unknown);
            return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
        }
        return unknown;
    }

    private static int indexOf(Object[] arr, Object toFind) {
        for (int i = 0; i < arr.length; i++) {
            if (toFind == arr[i]) {
                return i;
            }
        }
        throw new RuntimeException("not find");
    }

    private static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
        if (toResolve == rawType) {
            return context;
        }

        if (toResolve.isInterface()) {
            Class<?>[] interfaces = rawType.getInterfaces();
            Type[] genericInterfaces = rawType.getGenericInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                Class<?> anInterface = interfaces[i];
                if (toResolve == anInterface) {
                    return genericInterfaces[i];
                } else if (toResolve.isAssignableFrom(anInterface)) {
                    return getGenericSupertype(genericInterfaces[i], anInterface, toResolve);
                }
            }
        }

        if (!rawType.isInterface()) {
            Class<?> superclass = rawType.getSuperclass();
            if (toResolve == superclass) {
                return rawType.getGenericSuperclass();
            } else if (toResolve.isAssignableFrom(superclass)) {
                return getGenericSupertype(rawType.getGenericSuperclass(), superclass, toResolve);
            }
        }

        return toResolve;
    }

    private static Class<?> declaringClassOf(TypeVariable unknown) {
        GenericDeclaration genericDeclaration = unknown.getGenericDeclaration();
        return genericDeclaration instanceof Class ? (Class<?>) genericDeclaration : null;
    }


    private static final class WildcardTypeImpl implements WildcardType, Serializable {

        final Type upperBound;
        final Type lowerBound;

        public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            checkArgument(upperBounds.length == 1);
            checkArgument(lowerBounds.length <= 1);
            if (lowerBounds.length == 1) {
                Type lowerBound = lowerBounds[0];
                checkNotNull(lowerBound);
                checkNotPrimitive(lowerBound);
                checkArgument(upperBounds[0] == Object.class);
                this.upperBound = Object.class;
                this.lowerBound = canonicalize(lowerBound);
            } else {
                Type upperBound = upperBounds[0];
                checkNotNull(upperBound);
                checkNotPrimitive(upperBound);
                this.upperBound = canonicalize(upperBound);
                this.lowerBound = null;
            }
        }

        @Override
        public Type[] getUpperBounds() {
            return new Type[]{upperBound};
        }

        @Override
        public Type[] getLowerBounds() {
            return new Type[]{lowerBound};
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof WildcardType &&
                    Arrays.equals(this.getUpperBounds(), ((WildcardType) obj).getUpperBounds()) &&
                    Arrays.equals(this.getLowerBounds(), ((WildcardType) obj).getLowerBounds());
        }

        @Override
        public int hashCode() {
            return (lowerBound == null ? 1 : 31 + lowerBound.hashCode()) ^
                    (31 + upperBound.hashCode());
        }
    }


    private static final class ParameterizedTypeImpl implements ParameterizedType, Serializable {

        final Type ownerType;
        final Type rawType;
        final Type[] typeArguments;

        public ParameterizedTypeImpl(Type ownerType, Type rawType, Type[] actualTypeArguments) {
            if (rawType instanceof Class) {
                Class c = (Class) rawType;
                boolean isStaticOrTopLevel = Modifier.isStatic(c.getModifiers()) || c.getEnclosingClass() == null;
                checkArgument(ownerType != null || isStaticOrTopLevel);
            }
            this.ownerType = canonicalize(ownerType);
            this.rawType = canonicalize(rawType);
            this.typeArguments = new Type[actualTypeArguments.length];
            for (int i = 0, length = actualTypeArguments.length; i < length; i++) {
                Type argument = actualTypeArguments[i];
                checkNotNull(argument);
                checkNotPrimitive(argument);
                typeArguments[i] = canonicalize(argument);
            }
        }

        @Override
        public Type[] getActualTypeArguments() {
            return this.typeArguments;
        }

        @Override
        public Type getRawType() {
            return this.rawType;
        }

        @Override
        public Type getOwnerType() {
            return this.ownerType;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.typeArguments) ^
                    rawType.hashCode() ^
                    (ownerType == null ? 0 : ownerType.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ParameterizedType && Types.equal(this, obj);
        }
    }

    private static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    private static boolean checkNotPrimitive(Type argument) {
        return !(argument instanceof Class) || ((Class) argument).isPrimitive();
    }

    private static final class GenericArrayTypeImpl implements GenericArrayType, Serializable {
        final Type componentType;

        public GenericArrayTypeImpl(Type componentType) {
            this.componentType = canonicalize(componentType);
        }

        @Override
        public Type getGenericComponentType() {
            return this.componentType;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GenericArrayType && this.componentType.equals(((GenericArrayType) obj).getGenericComponentType());
        }

        @Override
        public int hashCode() {
            return componentType.hashCode();
        }
    }

}
