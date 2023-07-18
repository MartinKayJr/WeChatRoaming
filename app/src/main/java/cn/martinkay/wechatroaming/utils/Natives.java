package cn.martinkay.wechatroaming.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Process;
import android.system.Os;
import android.system.StructUtsname;

import com.tencent.mmkv.MMKV;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import cn.martinkay.wechatroaming.BuildConfig;
import cn.martinkay.wechatroaming.settings.startup.HybridClassLoader;
import cn.martinkay.wechatroaming.xposed.HookLoader;

public class Natives {

    public static final int RTLD_LAZY = 0x00001;    /* Lazy function call binding.  */
    public static final int RTLD_NOW = 0x00002;    /* Immediate function call binding.  */
    public static final int RTLD_BINDING_MASK = 0x3;    /* Mask of binding time value.  */
    public static final int RTLD_NOLOAD = 0x00004;    /* Do not load the object.  */
    public static final int RTLD_DEEPBIND = 0x00008;    /* Use deep binding.  */
    /* If the following bit is set in the MODE argument to `dlopen',
       the symbols of the loaded object and its dependencies are made
       visible as if the object were linked directly into the program.  */
    public static final int RTLD_GLOBAL = 0x00100;
    /* Unix98 demands the following flag which is the inverse to RTLD_GLOBAL.
       The implementation does this by default and so we can define the
       value to zero.  */
    public static final int RTLD_LOCAL = 0;

    /* Do not delete object when closed.  */
    public static final int RTLD_NODELETE = 0x01000;

    public static final int PROT_READ = 0x1;        /* Page can be read.  */
    public static final int PROT_WRITE = 0x2;        /* Page can be written.  */
    public static final int PROT_EXEC = 0x4;        /* Page can be executed.  */
    public static final int PROT_NONE = 0x0;        /* Page can not be accessed.  */
    public static final int PROT_GROWSDOWN = 0x01000000;	/* Extend change to start of
					   growsdown vma (mprotect only).  */
    public static final int PROT_GROWSUP = 0x02000000;	/* Extend change to start of
					   growsup vma (mprotect only).  */

    public static final int SEEK_SET = 0;        /* Set file offset to OFFSET.  */
    public static final int SEEK_CUR = 1;        /* Set file offset to current plus OFFSET.  */
    public static final int SEEK_END = 2;        /* Set file offset to end plus OFFSET.  */

    public static final int O_ACCMODE = 0x3;
    public static final int O_RDONLY = 0x0;
    public static final int O_WRONLY = 0x1;
    public static final int O_RDWR = 0x2;
    public static final int O_CREAT = 0x40;
    public static final int O_EXCL = 0x80;
    public static final int O_NOCTTY = 0x100;
    public static final int O_TRUNC = 0x200;
    public static final int O_APPEND = 0x400;
    public static final int O_NONBLOCK = 0x800;
    public static final int O_DSYNC = 0x1000;
    public static final int O_DIRECT = 0x4000;
    public static final int O_LARGEFILE = 0x8000;
    public static final int O_DIRECTORY = 0x10000;
    public static final int O_NOFOLLOW = 0x20000;
    public static final int O_NOATIME = 0x40000;
    public static final int O_CLOEXEC = 0x80000;
    public static final int O_SYNC = (0x100000 | O_DSYNC);
    public static final int O_PATH = 0x200000;

    private Natives() {
        throw new AssertionError("No instance for you!");
    }

    public static native void mwrite(long ptr, int len, byte[] buf, int offset);

    public static void mwrite(long ptr, int len, byte[] buf) {
        mwrite(ptr, len, buf, 0);
    }

    public static native void mread(long ptr, int len, byte[] buf, int offset);

    public static void mread(long ptr, int len, byte[] buf) {
        mread(ptr, len, buf, 0);
    }

    public static native int write(int fd, byte[] buf, int offset, int len) throws IOException;

    public static int write(int fd, byte[] buf, int len) throws IOException {
        return write(fd, buf, 0, len);
    }

    public static native int read(int fd, byte[] buf, int offset, int len) throws IOException;

    public static int read(int fd, byte[] buf, int len) throws IOException {
        return read(fd, buf, 0, len);
    }

    public static native int dup(int fd) throws IOException;

    public static native int dup2(int oldfd, int newfd) throws IOException;

    public static native int dup3(int oldfd, int newfd, int flags) throws IOException;

    public static native int open(String path, int flags, int mode) throws IOException;

    public static native long lseek(int fd, long offset, int whence) throws IOException;

    public static native void close(int fd) throws IOException;

    public static native long malloc(int size);

    public static native void free(long ptr);

    public static native void memcpy(long dest, long src, int num);

    public static native void memset(long addr, int c, int num);

    public static native int mprotect(long addr, int len, int prot);

    public static native long dlsym(long ptr, String symbol);

    public static native long dlopen(String filename, int flag);

    public static native int dlclose(long ptr);

    public static native String dlerror();

    public static native int sizeofptr();

    public static native int getpagesize();

    public static native long call(long addr);

    public static native long call(long addr, long argv);

    public static native int getProcessDumpableState() throws IOException;

    public static native void setProcessDumpableState(int dumpable) throws IOException;

    /**
     * Allocate a object instance of the specified class without calling the constructor.
     * <p>
     * Do not use this directly, use {@link Reflex#allocateInstance(Class)} instead.
     *
     * @param clazz the class to allocate
     * @return the allocated object
     */
    public static native Object allocateInstanceImpl(Class<?> clazz);

    /**
     * Invoke an instance method non-virtually (i.e. without calling the overridden method).
     * <p>
     * Do not use this directly, use {@link Reflex#invokeNonVirtual(Object, Method, Object[])} instead.
     *
     * @param declaringClass the class of the method, e.g. "Ljava/lang/String;"
     * @param methodName     the method name
     * @param methodSig      the method signature, e.g. "(Ljava/lang/String;)Ljava/lang/String;"
     * @param obj            the object to invoke the method on, must not be null
     * @param args           the arguments to pass to the method, may be null if no arguments are passed
     * @return the return value of the method
     * @throws InvocationTargetException if the method threw an exception
     */
    public static native Object invokeNonVirtualImpl(Class<?> declaringClass, String methodName,
                                                     String methodSig, Object obj, Object[] args)
            throws InvocationTargetException;

    private static void registerNativeLibEntry(String soTailingName) {
        if (soTailingName == null || soTailingName.length() == 0) {
            return;
        }
        try {
            Class<?> xp = Class.forName(HybridClassLoader.getXposedBridgeClassName());
            try {
                xp.getClassLoader()
                        .loadClass(HybridClassLoader.getObfuscatedLsposedNativeApiClassName())
                        .getMethod("recordNativeEntrypoint", String.class)
                        .invoke(null, soTailingName);
            } catch (ClassNotFoundException ignored) {
                // not LSPosed, ignore
            } catch (NoSuchMethodException | IllegalArgumentException
                     | InvocationTargetException | IllegalAccessException e) {
                Log.e(e);
            }
        } catch (ClassNotFoundException e) {
            // not in host process, ignore
        }
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    public static void load(Context ctx) throws LinkageError {
        try {
            getpagesize();
            return;
        } catch (UnsatisfiedLinkError ignored) {
        }
        String abi = getAbiForLibrary();
        try {
            Class.forName(HybridClassLoader.getXposedBridgeClassName());
            // in host process
            try {
//                String modulePath = HookEntry.getModulePath();
                String modulePath = HookLoader.getModulePath();
                if (modulePath != null) {
                    // try direct memory map
                    System.load(modulePath + "!/lib/" + abi + "/libblack_spider.so");
                    Log.d("dlopen by mmap success");
                }
            } catch (UnsatisfiedLinkError e1) {
                throwIfJniError(e1);
                // direct memory map load failed, extract and dlopen
                File libname = extractNativeLibrary(ctx, "black_spider", abi);
                // registerNativeLibEntry(libname.getName());
                try {
                    System.load(libname.getAbsolutePath());
                    Log.d("dlopen by extract success");
                } catch (UnsatisfiedLinkError e3) {
                    throwIfJniError(e3);
                    // give enough information to help debug
                    // Is this CPU_ABI bad?
                    Log.e("Build.SDK_INT=" + VERSION.SDK_INT);
                    Log.e("Build.CPU_ABI is: " + Build.CPU_ABI);
                    Log.e("Build.CPU_ABI2 is: " + Build.CPU_ABI2);
                    Log.e("Build.SUPPORTED_ABIS is: " + Arrays.toString(Build.SUPPORTED_ABIS));
                    Log.e("Build.SUPPORTED_32_BIT_ABIS is: " + Arrays.toString(Build.SUPPORTED_32_BIT_ABIS));
                    Log.e("Build.SUPPORTED_64_BIT_ABIS is: " + Arrays.toString(Build.SUPPORTED_64_BIT_ABIS));
                    // check whether this is a 64-bit ART runtime
                    Log.e("Process.is64bit is: " + Process.is64Bit());
                    StructUtsname uts = Os.uname();
                    Log.e("uts.machine is: " + uts.machine);
                    Log.e("uts.version is: " + uts.version);
                    Log.e("uts.sysname is: " + uts.sysname);
                    // panic, this is a bug
                    throw e3;
                }
            }
        } catch (ClassNotFoundException e) {
            // not in host process, ignore
            System.loadLibrary("black_spider");
        }
        getpagesize();
        File mmkvDir = new File(ctx.getFilesDir(), "bs_mmkv");
        if (!mmkvDir.exists()) {
            mmkvDir.mkdirs();
        }
        // MMKV requires a ".tmp" cache directory, we have to create it manually
        File cacheDir = new File(mmkvDir, ".tmp");
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        MMKV.initialize(ctx, mmkvDir.getAbsolutePath(), s -> {
            // nop, mmkv is attached with libblack_spider.so already
        });
        MMKV.mmkvWithID("global_config", MMKV.MULTI_PROCESS_MODE);
        MMKV.mmkvWithID("global_cache", MMKV.MULTI_PROCESS_MODE);
    }

    private static void throwIfJniError(UnsatisfiedLinkError error) {
        if (error.getMessage() != null && error.getMessage().contains("JNI_ERR")) {
            throw error;
        }
    }

    /**
     * Extract or update native library into "bs_dyn_lib" dir
     *
     * @param libraryName library name without "lib" or ".so", eg. "black_spider", "mmkv"
     */
    static File extractNativeLibrary(Context ctx, String libraryName, String abi) throws IOError {
        String soName = "lib" + libraryName + ".so." + BuildConfig.VERSION_CODE + "." + abi;
        File dir = new File(ctx.getFilesDir(), "bs_dyn_lib");
        if (!dir.isDirectory()) {
            if (dir.isFile()) {
                dir.delete();
            }
            dir.mkdir();
        }
        File soFile = new File(dir, soName);
        if (!soFile.exists()) {
            InputStream in = Natives.class.getClassLoader()
                    .getResourceAsStream("lib/" + abi + "/lib" + libraryName + ".so");
            if (in == null) {
                throw new UnsatisfiedLinkError("Unsupported ABI: " + abi);
            }
            //clean up old files
            for (String name : dir.list()) {
                if (name.startsWith("lib" + libraryName + "_")
                        || name.startsWith("lib" + libraryName + ".so")) {
                    new File(dir, name).delete();
                }
            }
            try {
                // extract so file
                soFile.createNewFile();
                FileOutputStream fout = new FileOutputStream(soFile);
                byte[] buf = new byte[1024];
                int i;
                while ((i = in.read(buf)) > 0) {
                    fout.write(buf, 0, i);
                }
                in.close();
                fout.flush();
                fout.close();
            } catch (IOException ioe) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
                // rethrow as error
                throw new IOError(ioe);
            }
        }
        return soFile;
    }

    public static String getAbiForLibrary() {
        String[] supported = Process.is64Bit() ? Build.SUPPORTED_64_BIT_ABIS : Build.SUPPORTED_32_BIT_ABIS;
        if (supported == null || supported.length == 0) {
            throw new IllegalStateException("No supported ABI in this device");
        }
        List<String> abis = Arrays.asList("armeabi-v7a", "arm64-v8a", "x86", "x86_64");
        for (String abi : supported) {
            if (abis.contains(abi)) {
                return abi;
            }
        }
        throw new IllegalStateException("No supported ABI in " + Arrays.toString(supported));
    }
}
