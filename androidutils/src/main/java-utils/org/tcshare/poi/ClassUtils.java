package org.tcshare.poi;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.tcshare.poi.config.Configure;

import java.util.*;

/**
 * 类操作工具类，全静态方法、空安全，无需反射即可处理类名、继承体系、装箱拆箱、类加载
 * 禁止实例化，所有方法 static
 */
public class ClassUtils {

    // ===================== 常量 =====================
    public static final char PACKAGE_SEPARATOR_CHAR = '.';
    public static final String PACKAGE_SEPARATOR = ".";
    public static final char INNER_CLASS_SEPARATOR_CHAR = '$';
    public static final String INNER_CLASS_SEPARATOR = "$";

    /**
     * 基本类型 -> 包装类 映射
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER_MAP;
    /**
     * 包装类 -> 基本类型 映射
     */
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_MAP;

    static {
        PRIMITIVE_TO_WRAPPER_MAP = new HashMap<>();
        WRAPPER_TO_PRIMITIVE_MAP = new HashMap<>();

        PRIMITIVE_TO_WRAPPER_MAP.put(Boolean.TYPE, Boolean.class);
        PRIMITIVE_TO_WRAPPER_MAP.put(Byte.TYPE, Byte.class);
        PRIMITIVE_TO_WRAPPER_MAP.put(Character.TYPE, Character.class);
        PRIMITIVE_TO_WRAPPER_MAP.put(Short.TYPE, Short.class);
        PRIMITIVE_TO_WRAPPER_MAP.put(Integer.TYPE, Integer.class);
        PRIMITIVE_TO_WRAPPER_MAP.put(Long.TYPE, Long.class);
        PRIMITIVE_TO_WRAPPER_MAP.put(Float.TYPE, Float.class);
        PRIMITIVE_TO_WRAPPER_MAP.put(Double.TYPE, Double.class);
        PRIMITIVE_TO_WRAPPER_MAP.put(Void.TYPE, Void.class);

        // 反向映射
        for (final Map.Entry<Class<?>, Class<?>> entry : PRIMITIVE_TO_WRAPPER_MAP.entrySet()) {
            WRAPPER_TO_PRIMITIVE_MAP.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * 私有构造：工具类禁止实例化
     */
    private ClassUtils() {
        throw new AssertionError("Cannot instantiate utility class.");
    }

    // ===================== 类名获取（高频） =====================
    /**
     * 获取不带包名的简单类名（处理内部类、数组）
     * ClassUtils.getShortClassName(StringUtils.class) → "StringUtils"
     */
    public static String getShortClassName(final Class<?> cls) {
        if (cls == null) {
            return null;
        }
        return getShortClassName(cls.getName());
    }

    public static String getShortClassName(String className) {
        if (className == null) {
            return null;
        }
        if (className.isEmpty()) {
            return className;
        }
        final int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        if (lastDotIndex == -1) {
            return className;
        }
        className = className.substring(lastDotIndex + 1);
        // 内部类 $ 替换为 .
        return className.replace(INNER_CLASS_SEPARATOR_CHAR, '.');
    }

    /**
     * 获取包名
     */
    public static String getPackageName(final Class<?> cls) {
        if (cls == null) {
            return null;
        }
        return getPackageName(cls.getName());
    }

    public static String getPackageName(String className) {
        if (className == null || className.isEmpty()) {
            return EMPTY;
        }
        final int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        return lastDotIndex <= 0 ? EMPTY : className.substring(0, lastDotIndex);
    }

    // ===================== 基本类型 & 包装类互转 =====================
    /**
     * 判断是否为基本数据类型
     */
    public static boolean isPrimitive(final Class<?> cls) {
        return cls != null && cls.isPrimitive();
    }

    /**
     * 基本类型 → 包装类
     */
    public static Class<?> primitiveToWrapper(final Class<?> cls) {
        Class<?> convertedClass = cls;
        if (isPrimitive(cls)) {
            convertedClass = PRIMITIVE_TO_WRAPPER_MAP.get(cls);
        }
        return convertedClass;
    }

    /**
     * 包装类 → 基本类型
     */
    public static Class<?> wrapperToPrimitive(final Class<?> cls) {
        Class<?> convertedClass = cls;
        if (!isPrimitive(cls)) {
            convertedClass = WRAPPER_TO_PRIMITIVE_MAP.get(cls);
        }
        return convertedClass;
    }

    /**
     * 批量数组转换：基本数组 → 包装数组
     */
    public static Class<?>[] primitivesToWrappers(final Class<?>[] classes) {
        if (classes == null) {
            return null;
        }
        final Class<?>[] wrappedClasses = new Class<?>[classes.length];
        for (int i = 0; i < classes.length; i++) {
            wrappedClasses[i] = primitiveToWrapper(classes[i]);
        }
        return wrappedClasses;
    }

    /**
     * 批量数组转换：包装数组 → 基本数组
     */
    public static Class<?>[] wrappersToPrimitives(final Class<?>[] classes) {
        if (classes == null) {
            return null;
        }
        final Class<?>[] primitiveClasses = new Class<?>[classes.length];
        for (int i = 0; i < classes.length; i++) {
            primitiveClasses[i] = wrapperToPrimitive(classes[i]);
        }
        return primitiveClasses;
    }

    // ===================== 类型赋值兼容 isAssignable（核心反射常用） =====================
    /**
     * 兼容自动装箱的类型赋值判断（比原生 Class.isAssignableFrom 更强）
     * @param cls 源类型
     * @param toClass 目标类型
     * @param autoboxing 是否开启装箱拆箱兼容
     */
    public static boolean isAssignable(Class<?> cls, final Class<?> toClass, final boolean autoboxing) {
        if (cls == null || toClass == null) {
            return false;
        }
        // 原生兼容
        if (toClass.isAssignableFrom(cls)) {
            return true;
        }
        // 开启装箱兼容
        if (autoboxing) {
            return primitiveToWrapper(cls).isAssignableFrom(primitiveToWrapper(toClass));
        }
        return false;
    }

    /**
     * 默认开启自动装箱的重载方法
     */
    public static boolean isAssignable(final Class<?> cls, final Class<?> toClass) {
        return isAssignable(cls, toClass, true);
    }

    // ===================== 获取继承链、父类、接口 =====================
    /**
     * 获取所有父类（不含Object），链式遍历
     */
    public static List<Class<?>> getAllSuperclasses(final Class<?> cls) {
        final List<Class<?>> list = new ArrayList<>();
        Class<?> superclass = cls == null ? null : cls.getSuperclass();
        while (superclass != null) {
            list.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return list;
    }

    /**
     * 获取所有实现接口（含父类接口）
     */
    public static Set<Class<?>> getAllInterfaces(final Class<?> cls) {
        final Set<Class<?>> interfaceSet = new LinkedHashSet<>();
        if (cls == null) {
            return interfaceSet;
        }
        Class<?> clazz = cls;
        while (clazz != null) {
            final Class<?>[] interfaces = clazz.getInterfaces();
            Collections.addAll(interfaceSet, interfaces);
            clazz = clazz.getSuperclass();
        }
        return interfaceSet;
    }

    // ===================== 类加载、全类名加载 =====================
    /**
     * 根据全类名加载类，空安全、兼容上下文类加载器
     */
    public static Class<?> getClass(final String className) throws ClassNotFoundException {
        return getClass(className, false);
    }

    public static Class<?> getClass(final String className, final boolean initialize) throws ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassUtils.class.getClassLoader();
        }
        return Class.forName(className, initialize, loader);
    }

    // ===================== 数组相关类工具 =====================
    /**
     * 判断是否为数组类型
     */
    public static boolean isArray(final Class<?> cls) {
        return cls != null && cls.isArray();
    }

    /**
     * 获取数组元素类型
     */
    public static Class<?> getComponentType(final Class<?> arrayClass) {
        return arrayClass == null ? null : arrayClass.getComponentType();
    }

    private static final String EMPTY = "";

    public static String getSimpleName(Class<?> aClass) {
        return aClass.getSimpleName();
    }
}