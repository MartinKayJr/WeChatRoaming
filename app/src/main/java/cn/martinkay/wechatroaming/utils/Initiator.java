package cn.martinkay.wechatroaming.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashMap;

public class Initiator {

    private static ClassLoader sHostClassLoader;
    private static ClassLoader sPluginParentClassLoader;
    private static final HashMap<String, Class<?>> sClassCache = new HashMap<>(16);

    private Initiator() {
        throw new AssertionError("No instance for you!");
    }

    public static void init(ClassLoader classLoader) {
        sHostClassLoader = classLoader;
        sPluginParentClassLoader = Initiator.class.getClassLoader();
    }

    public static ClassLoader getPluginClassLoader() {
        return Initiator.class.getClassLoader();
    }

    public static ClassLoader getHostClassLoader() {
        return sHostClassLoader;
    }

    /**
     * Load a class, if the class is not found, null will be returned.
     *
     * @param className The class name.
     * @return The class, or null if not found.
     */
    @Nullable
    public static Class<?> load(String className) {
        if (sPluginParentClassLoader == null || className == null || className.isEmpty()) {
            return null;
        }
        if (className.endsWith(";") || className.contains("/")) {
            className = className.replace('/', '.');
            if (className.endsWith(";")) {
                if (className.charAt(0) == 'L') {
                    className = className.substring(1, className.length() - 1);
                } else {
                    className = className.substring(0, className.length() - 1);
                }
            }
        }
        try {
            return sHostClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Load a class, if the class is not found, a ClassNotFoundException will be thrown.
     *
     * @param className The class name.
     * @return The class.
     * @throws ClassNotFoundException If the class is not found.
     */
    @NonNull
    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        Class<?> ret = load(className);
        if (ret == null) {
            throw new ClassNotFoundException(className);
        }
        return ret;
    }

    @NonNull
    public static Class<?> loadClassEither(@NonNull String... classNames) throws ClassNotFoundException {
        for (String className : classNames) {
            Class<?> ret = load(className);
            if (ret != null) {
                return ret;
            }
        }
        throw new ClassNotFoundException("Class not found for names: " + Arrays.toString(classNames));
    }

    /**
     * Load a class, if the class is not found, an unsafe ClassNotFoundException will be thrown.
     *
     * @param className The class name.
     * @return The class.
     */
    @NonNull
    public static Class<?> requireClass(@NonNull String className) {
        try {
            return loadClass(className);
        } catch (ClassNotFoundException e) {
            IoUtils.unsafeThrow(e);
            throw new AssertionError("Unreachable code");
        }
    }

    @Nullable
    private static Class<?> findClassWithSyntheticsImpl(@NonNull String className, int... index) {
        Class<?> clazz = load(className);
        if (clazz != null) {
            return clazz;
        }
        if (index != null && index.length > 0) {
            for (int i : index) {
                Class<?> cref = load(className + "$" + i);
                if (cref != null) {
                    try {
                        return cref.getDeclaredField("this$0").getType();
                    } catch (ReflectiveOperationException ignored) {
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private static Class<?> findClassWithSynthetics(@NonNull String className, int... index) {
        Class<?> cache = sClassCache.get(className);
        if (cache != null) {
            return cache;
        }
        Class<?> clazz = load(className);
        if (clazz != null) {
            sClassCache.put(className, clazz);
            return clazz;
        }
        clazz = findClassWithSyntheticsImpl(className, index);
        if (clazz != null) {
            sClassCache.put(className, clazz);
            return clazz;
        }
        Log.e("Initiator/E class " + className + " not found");
        return null;
    }

    @Nullable
    public static Class<?> findClassWithSynthetics(@NonNull String className1, @NonNull String className2) {
        Class<?> clazz = load(className1);
        if (clazz != null) {
            return clazz;
        }
        return load(className2);
    }

    @Nullable
    public static Class<?> findClassWithSynthetics0(@NonNull String className1, @NonNull String className2, int... index) {
        String cacheKey = className1;
        Class<?> cache = sClassCache.get(cacheKey);
        if (cache != null) {
            return cache;
        }
        Class<?> clazz = findClassWithSyntheticsImpl(className1, index);
        if (clazz != null) {
            sClassCache.put(cacheKey, clazz);
            return clazz;
        }
        clazz = findClassWithSyntheticsImpl(className2, index);
        if (clazz != null) {
            sClassCache.put(cacheKey, clazz);
            return clazz;
        }
        return null;
    }

    @Nullable
    public static Class<?> findClassWithSynthetics(@NonNull String className1, @NonNull String className2, int... index) {
        Class<?> ret = findClassWithSynthetics0(className1, className2, index);
        logErrorIfNotFound(ret, className1);
        return ret;
    }

    private static void logErrorIfNotFound(@Nullable Class<?> c, @NonNull String name) {
        if (c == null) {
            Log.e("Initiator/E class " + name + " not found");
        }
    }

    @Nullable
    public static Class<?> findClassWithSynthetics(@NonNull String className1, @NonNull String className2,
            @NonNull String className3, int... index) {
        String cacheKey = className1;
        Class<?> cache = sClassCache.get(cacheKey);
        if (cache != null) {
            return cache;
        }
        Class<?> clazz = findClassWithSyntheticsImpl(className1, index);
        if (clazz != null) {
            sClassCache.put(cacheKey, clazz);
            return clazz;
        }
        clazz = findClassWithSyntheticsImpl(className2, index);
        if (clazz != null) {
            sClassCache.put(cacheKey, clazz);
            return clazz;
        }
        clazz = findClassWithSyntheticsImpl(className3, index);
        if (clazz != null) {
            sClassCache.put(cacheKey, clazz);
            return clazz;
        }
        Log.e("Initiator/E class " + className1 + " not found");
        return null;
    }

}
