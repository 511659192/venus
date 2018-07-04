package com.ym.materials.gson.internal;

import org.junit.Assert;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by ym on 2018/7/2.
 */
public class Types<E> {

    private List<E> list;

    public static void main(String[] args) throws NoSuchFieldException {
        Field field = Types.class.getDeclaredField("list");
        Type genericType = field.getGenericType();
        Class<?> rawType = getRawType(genericType);
        System.out.println(getCollectionElementType(genericType, rawType));
    }

    static final Type[] EMPTY_TYPE_ARRAY = new Type[]{};

    /**
     * 获取原始类型
     * @param type
     * @return
     */
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            assertTrue(rawType instanceof Class);
            return (Class<?>) parameterizedType.getRawType();
        }

        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            Type genericComponentType = genericArrayType.getGenericComponentType();
            return Array.newInstance(getRawType(genericComponentType), 0).getClass(); // 转换成数组class
        }

        if (type instanceof TypeVariable) {
            return Object.class;
        }

        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            Type[] upperBounds = wildcardType.getUpperBounds(); // 上下界限定 从继承树来看
            return getRawType(upperBounds[0]);
        }

        String className = type == null ? "null" : type.getClass().getName();
        throw new IllegalArgumentException("expected class, parameterizedType, genericArrayType, but type <" + type + "> is of type" + className);
    }

    /**
     * 封装成自己的type类型
     * 不涉及父类
     * 递归
     * @param type
     * @return
     */
    public static Type canonicalize(Type type) {
        Assert.assertNotNull(type);
        if (type instanceof Class) {
            Class c = (Class) type;
            return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c; // 判定是不是数组
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return new ParameterizedTypeImpl(parameterizedType.getOwnerType(), parameterizedType.getRawType(), parameterizedType.getActualTypeArguments());
        }

        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return new GenericArrayTypeImpl(genericArrayType.getGenericComponentType());
        }

        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            return new WildcardTypeImpl(wildcardType.getUpperBounds(), wildcardType.getLowerBounds());
        }
        return type;
    }

    /**
     * 获取集合元素类型
     * @param context
     * @param contextRawType
     * @return
     */
    public static Type getCollectionElementType(Type context, Class<?> contextRawType) {
        Type type = getSupertype(context, contextRawType, Collection.class);
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            type = wildcardType.getUpperBounds()[0];
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return parameterizedType.getActualTypeArguments()[0];
        }
        return Object.class;
    }

    /**
     * 封装指定父类型
     * @param context
     * @param contextRawType
     * @param supertype
     * @return
     */
    private static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
        if (context instanceof WildcardType) {
            context = ((WildcardType) context).getUpperBounds()[0];
        }

        assertTrue(supertype.isAssignableFrom(contextRawType));
        return resolve(context, contextRawType, getGeniricSuppertype(context, contextRawType, supertype));
    }

    public static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {
        return resolve(context, contextRawType, toResolve, new HashSet<TypeVariable>());
    }

    private static Type resolve(Type context, Class<?> contextRawType, Type toResolve, HashSet<TypeVariable> visitedTypeVariables) {
        for (;;) {
            if (toResolve instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable) toResolve;
                if (visitedTypeVariables.contains(typeVariable)) {
                    return toResolve;
                }

                visitedTypeVariables.add(typeVariable);
                toResolve = resolveTypeVariable(context, contextRawType, typeVariable); // 变量类型 可能有多层声明 需要找到最终声明它的那个
                if (toResolve == typeVariable) {
                    return toResolve;
                }
            } else if (toResolve instanceof Class && ((Class) toResolve).isArray()) {
                Class<?> original = ((Class) toResolve);
                Type componentType = original.getComponentType();
                Type newComponentType = resolve(context, contextRawType, componentType, visitedTypeVariables);
                return componentType == newComponentType ? original : arrayOf(newComponentType);
            } else if (toResolve instanceof GenericArrayType) {
                GenericArrayType origin = (GenericArrayType) toResolve;
                Type componentType = origin.getGenericComponentType();
                Type newComponentType = resolve(context, contextRawType, componentType, visitedTypeVariables);
                return componentType == newComponentType ? origin : arrayOf(newComponentType);
            } else if (toResolve instanceof ParameterizedType) {
                ParameterizedType origin = (ParameterizedType) toResolve;
                Type ownerType = origin.getOwnerType();
                Type newOwnerType = resolve(context, contextRawType, ownerType, visitedTypeVariables);
                boolean changed = newOwnerType != ownerType;
                Type[] args = origin.getActualTypeArguments();
                for (int i = 0, length = args.length; i < length; i++) {
                    Type resolvedTypeArgument = resolve(context, contextRawType, args[i], visitedTypeVariables);
                    if (resolvedTypeArgument != args[i]) {
                        if (!changed) {
                            args = args.clone();
                            changed = true;
                        }
                        args[i] = resolvedTypeArgument;
                    }
                }
                return changed ? newParameterizedTypeWithOwner(newOwnerType, origin.getRawType(), args) : origin;
            } else if (toResolve instanceof WildcardType) {
                WildcardType origin = (WildcardType) toResolve;
                Type[] originLowerBounds = origin.getLowerBounds();
                Type[] originUpperBounds = origin.getUpperBounds();

                if (originLowerBounds.length == 1) {
                    Type lowerBount = resolve(context, contextRawType, originLowerBounds[0], visitedTypeVariables);
                    if (lowerBount != originLowerBounds[0]) {
                        return supertypeOf(lowerBount);
                    }
                } else if (originUpperBounds.length == 1) {
                    Type upperBound = resolve(context, contextRawType, originUpperBounds[0], visitedTypeVariables);
                    if (upperBound != originUpperBounds[0]) {
                        return subtypeOf(upperBound);
                    }
                }
                return origin;
            } else {
                return toResolve;
            }
        }
    }

    private static Type supertypeOf(Type bound) {
        Type[] lowerBounds;
        if (bound instanceof WildcardType) {
            lowerBounds = ((WildcardType) bound).getLowerBounds();
        } else {
            lowerBounds = new Type[]{bound};
        }
        return new WildcardTypeImpl(new Type[]{Object.class}, lowerBounds);
    }

    private static Type subtypeOf(Type bound) {
        Type[] upperBounds;
        if (bound instanceof WildcardType) {
            upperBounds = ((WildcardType) bound).getUpperBounds();
        } else {
            upperBounds = new Type[] {bound};
        }
        return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
    }

    private static Type newParameterizedTypeWithOwner(Type ownerType, Type rawType, Type... typeArguments) {
        return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
    }

    private static GenericArrayType arrayOf(Type componentType) {
        return new GenericArrayTypeImpl(componentType);
    }

    private static Type resolveTypeVariable(Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
        Class<?> declaredByRaw = declaringClassOf(unknown);
        if (declaredByRaw == null) {
            return unknown;
        }

        Type declaredBy = getGeniricSuppertype(context, contextRawType, declaredByRaw);
        if (declaredBy instanceof ParameterizedType) {
            int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
            return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
        }
        return unknown;
    }

    private static int indexOf(Object[] array, Object toFind) {
        for (int i = 0, length = array.length; i < length; i++) {
            if (toFind.equals(array[i])) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }

    private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
        GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
        return genericDeclaration instanceof Class ? (Class<?>) genericDeclaration : null;
    }

    /**
     * 类型转换成泛型类型
     * @param context
     * @param rawType
     * @param toResolve
     * @return
     */
    private static Type getGeniricSuppertype(Type context, Class<?> rawType, Class<?> toResolve) {
        if (toResolve == rawType) {
            return context;
        }

        if (toResolve.isInterface()) {
            Class<?>[] interfaces = rawType.getInterfaces();
            Type[] genericInterfaces = rawType.getGenericInterfaces();
            for (int i = 0, length = interfaces.length; i < length; i++) {
                if (toResolve == interfaces[i]) {
                    return genericInterfaces[i];
                } else if (toResolve.isAssignableFrom(interfaces[i])) { // 递归查找
                    return getGeniricSuppertype(genericInterfaces[i], interfaces[i], toResolve);
                }
            }
        }

        if (!rawType.isInterface()) {
            for (Class<?> supperClass; (supperClass = rawType.getSuperclass()) != Object.class; rawType = supperClass) {
                if (toResolve == supperClass) {
                    return rawType.getGenericSuperclass();
                } else if (toResolve.isAssignableFrom(supperClass)) {
                    return getGeniricSuppertype(rawType.getGenericSuperclass(), supperClass, toResolve);
                }
            }
        }
        return toResolve;
    }

    private static final class GenericArrayTypeImpl implements GenericArrayType, Serializable {
        private final Type componentType;

        public GenericArrayTypeImpl(Type componentType) {
            this.componentType = canonicalize(componentType);
        }

        @Override
        public Type getGenericComponentType() {
            return componentType;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GenericArrayType && Types.equals(this, ((GenericArrayType) obj));
        }

        @Override
        public int hashCode() {
            return componentType.hashCode();
        }

        @Override
        public String toString() {
            return "GenericArrayTypeImpl{" +
                    "componentType=" + componentType +
                    '}';
        }
    }

    private static final class ParameterizedTypeImpl implements ParameterizedType, Serializable {
        private final Type ownerType;
        private final Type rawType;
        private final Type[] typeArguments;

        public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
            if (rawType instanceof Class) {
                Class rawTypeAsClass = (Class) rawType;
                boolean isStaticOrTopLevel = Modifier.isStatic(rawTypeAsClass.getModifiers()) || ((Class) rawType).getEnclosingClass() == null;
                assertTrue(ownerType != null || isStaticOrTopLevel);
            }

            this.ownerType = ownerType == null ? null : canonicalize(ownerType);
            this.rawType = canonicalize(rawType);
            this.typeArguments = typeArguments;
            for (int i = 0; i < typeArguments.length; i++) {
                assertNotNull(typeArguments[i]);
                checkNotPrimitive(typeArguments[i]);
                typeArguments[i] = canonicalize(typeArguments[i]);
            }
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ParameterizedType && Types.equals(this, ((ParameterizedType) obj));
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(typeArguments) ^ rawType.hashCode() ^ hashCodeOrZero(ownerType);
        }

        @Override
        public String toString() {
            return "ParameterizedTypeImpl{" +
                    "ownerType=" + ownerType +
                    ", rawType=" + rawType +
                    ", typeArguments=" + Arrays.toString(typeArguments) +
                    '}';
        }
    }

    private static final class WildcardTypeImpl implements WildcardType, Serializable {
        private final Type upperBound;
        private final Type lowerBound;

        public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            assertTrue(upperBounds.length == 1);
            assertTrue(lowerBounds.length <= 1);
            if (lowerBounds.length == 1) {
                assertNotNull(lowerBounds[0]);
                checkNotPrimitive(lowerBounds[0]);
                assertTrue(upperBounds[0] == Object.class);
                this.lowerBound = canonicalize(lowerBounds[0]);
                this.upperBound = Object.class;
                return;
            }

            assertNotNull(upperBounds[0]);
            checkNotPrimitive(upperBounds[0]);
            this.lowerBound = null;
            this.upperBound = canonicalize(upperBounds[0]);
        }

        @Override
        public Type[] getLowerBounds() {
            return lowerBound == null ? EMPTY_TYPE_ARRAY : new Type[]{lowerBound};
        }

        @Override
        public Type[] getUpperBounds() {
            return new Type[]{upperBound};
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof WildcardType && Types.equals(this, ((WildcardType) obj));
        }

        @Override
        public int hashCode() {
            return (lowerBound != null ? 31 + lowerBound.hashCode() : 1) ^ (31 + upperBound.hashCode());
        }

        @Override
        public String toString() {
            return "WildcardTypeImpl{" +
                    "upperBound=" + upperBound +
                    ", lowerBound=" + lowerBound +
                    '}';
        }
    }

    static void checkNotPrimitive(Type type) {
        assertTrue(!(type instanceof Class<?>) || !((Class<?>) type).isPrimitive());
    }

    static int hashCodeOrZero(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    public static boolean equals(Type a, Type b) {
        if (a == b) {
            return true;
        }

        if (a instanceof Class) {
            return a.equals(b);
        }

        if (a instanceof ParameterizedType) {
            if (!(b instanceof ParameterizedType)) {
                return false;
            }

            ParameterizedType pa = (ParameterizedType) a;
            ParameterizedType pb = (ParameterizedType) b;
            return equal(pa.getOwnerType(), pb.getOwnerType()) &&  // ownerType 可能为空
                    pa.getRawType().equals(pb.getRawType()) &&
                    Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());
        }

        if (a instanceof GenericArrayType) {
            if (!(b instanceof GenericArrayType)) {
                return false;
            }

            GenericArrayType ga = (GenericArrayType) a;
            GenericArrayType gb = (GenericArrayType) b;
            return equals(ga.getGenericComponentType(), gb.getGenericComponentType());
        }

        if (a instanceof WildcardType) {
            if (!(b instanceof WildcardType)) {
                return false;
            }

            WildcardType wa = (WildcardType) a;
            WildcardType wb = (WildcardType) b;
            return Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds()) &&
                    Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds());
        }

        if (a instanceof TypeVariable) {
            if (!(b instanceof TypeVariable)) {
                return false;
            }

            TypeVariable ta = (TypeVariable) a;
            TypeVariable tb = (TypeVariable) b;
            return ta.getGenericDeclaration() == tb.getGenericDeclaration() &&
                    ta.getName().equals(tb.getName());
        }

        return false;
    }

    static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }
}
