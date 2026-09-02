package teacommontea;

import java.lang.management.ManagementFactory;


public final class Secret {

    private Secret() {}

    public static int bind(int classKey, Class<?> caller) {
        int h = 0x811C9DC5;
        if (caller != null) {
            String n = caller.getName();
            for (int i = 0; i < n.length(); i++) {
                h = (h ^ n.charAt(i)) * 0x01000193;
            }
        }
        return classKey ^ h ^ g();
    }

    private static int cached = Integer.MIN_VALUE;

    public static int g() {
        int v = cached;
        if (v != Integer.MIN_VALUE) {
            return v;
        }
        v = probe();
        cached = v;
        return v;
    }

    private static int probe() {
        int flags = 0;
        try {
            for (String a : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                String s = a.toLowerCase(java.util.Locale.ROOT);
                if (s.startsWith("-javaagent") || s.startsWith("-agentlib") || s.startsWith("-agentpath")) {
                    flags |= 1;
                }
                if (s.contains("jdwp") || s.equals("-xdebug") || s.startsWith("-xrunjdwp")) {
                    flags |= 2;
                }
            }
        } catch (Throwable t) {
            flags |= 4;
        }
        try {
            for (Thread th : Thread.getAllStackTraces().keySet()) {
                String n = th.getName();
                if (n == null) {
                    continue;
                }
                String ln = n.toLowerCase(java.util.Locale.ROOT);
                if (ln.contains("jdwp") || ln.contains("java-agent")) {
                    flags |= 16;
                }
            }
        } catch (Throwable t) {
            flags |= 4;
        }
        try {
            if (attachClassLoaded()) {
                flags |= 32;
            }
        } catch (Throwable t) {
            flags |= 4;
        }
        if (flags == 0) {
            return 0;
        }
        return mix(flags);
    }

    private static boolean attachClassLoaded() {
        try {
            java.lang.reflect.Method m = ClassLoader.class.getDeclaredMethod(
                    "findLoadedClass", String.class);
            m.setAccessible(true);
            ClassLoader scl = ClassLoader.getSystemClassLoader();
            for (String n : new String[]{
                    "sun.tools.attach.HotSpotVirtualMachine",
                    "com.sun.tools.attach.VirtualMachine",
                    "sun.instrument.InstrumentationImpl"}) {
                for (ClassLoader cl = scl; cl != null; cl = cl.getParent()) {
                    if (m.invoke(cl, n) != null) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    private static int mix(int flags) {
        int h = 0x9E3779B9 ^ flags;
        h ^= (h << 13);
        h ^= (h >>> 17);
        h ^= (h << 5);
        return h == 0 ? 0x5A5A5A5A : h;
    }
}
