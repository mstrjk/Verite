package teacommontea.veritevoiler;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public final class VanishTab {

    private VanishTab() {}

    private static volatile Object handler;

    private static final String GEN_NAME = "teacommontea.veritevoiler.gen.GenTabVanish";
    private static final String GEN_INTERNAL = "teacommontea/veritevoiler/gen/GenTabVanish";

    public static void installIfAvailable(Plugin plugin, Vanish vanish) {
        if (Bukkit.getPluginManager().getPlugin("TAB") == null) {
            return;
        }
        try {
            Class<?> integrationClass = Class.forName(
                    "me.neznamy.tab.api.integration.VanishIntegration", false, loaderParent());
            Class<?> tabPlayerClass = Class.forName(
                    "me.neznamy.tab.api.TabPlayer", false, loaderParent());
            Method getUniqueId = tabPlayerClass.getMethod("getUniqueId");

            TabVanishHandler bridge = new Bridge(vanish);

            byte[] bytes = generate(integrationClass, tabPlayerClass, getUniqueId);
            Loader loader = new Loader(loaderParent());
            Class<?> generated = loader.define(GEN_NAME, bytes);

            Object instance = generated.getConstructor(String.class, TabVanishHandler.class)
                    .newInstance(plugin.getName(), bridge);
            integrationClass.getMethod("registerHandler", integrationClass).invoke(null, instance);
            handler = instance;
            plugin.getLogger().info("TAB detected; vanish tablist integration registered.");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("TAB is installed but exposes no VanishIntegration; skipping tablist integration.");
        } catch (Throwable t) {
            plugin.getLogger().warning("TAB vanish integration failed to register: " + t.getMessage());
        }
    }

    public static void shutdown() {
        Object h = handler;
        if (h == null) {
            return;
        }
        handler = null;
        try {
            Class<?> integrationClass = h.getClass().getSuperclass();
            integrationClass.getMethod("unregisterHandler", integrationClass).invoke(null, h);
        } catch (Throwable ignored) {

        }
    }

    private static ClassLoader loaderParent() {
        return VanishTab.class.getClassLoader();
    }

    private static byte[] generate(Class<?> integrationClass, Class<?> tabPlayerClass, Method getUniqueId) {
        String superName = Type.getInternalName(integrationClass);
        String tabPlayerName = Type.getInternalName(tabPlayerClass);
        String tabPlayerDesc = Type.getDescriptor(tabPlayerClass);
        String handlerName = Type.getInternalName(TabVanishHandler.class);
        String handlerDesc = Type.getDescriptor(TabVanishHandler.class);
        String uuidName = "java/util/UUID";
        String uuidDesc = "Ljava/util/UUID;";

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, GEN_INTERNAL, null,
                superName, null);

        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "h", handlerDesc, null, null).visitEnd();

        MethodVisitor ctor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                "(Ljava/lang/String;" + handlerDesc + ")V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitVarInsn(Opcodes.ALOAD, 1);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", "(Ljava/lang/String;)V", false);
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitVarInsn(Opcodes.ALOAD, 2);
        ctor.visitFieldInsn(Opcodes.PUTFIELD, GEN_INTERNAL, "h", handlerDesc);
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();

        MethodVisitor iv = cw.visitMethod(Opcodes.ACC_PUBLIC, "isVanished",
                "(" + tabPlayerDesc + ")Z", null, null);
        iv.visitCode();
        iv.visitVarInsn(Opcodes.ALOAD, 0);
        iv.visitFieldInsn(Opcodes.GETFIELD, GEN_INTERNAL, "h", handlerDesc);
        iv.visitVarInsn(Opcodes.ALOAD, 1);
        iv.visitMethodInsn(Opcodes.INVOKEINTERFACE, tabPlayerName, getUniqueId.getName(),
                "()" + uuidDesc, true);
        iv.visitMethodInsn(Opcodes.INVOKEINTERFACE, handlerName, "isVanished",
                "(" + uuidDesc + ")Z", true);
        iv.visitInsn(Opcodes.IRETURN);
        iv.visitMaxs(0, 0);
        iv.visitEnd();

        MethodVisitor cv = cw.visitMethod(Opcodes.ACC_PUBLIC, "canSee",
                "(" + tabPlayerDesc + tabPlayerDesc + ")Z", null, null);
        cv.visitCode();
        cv.visitVarInsn(Opcodes.ALOAD, 0);
        cv.visitFieldInsn(Opcodes.GETFIELD, GEN_INTERNAL, "h", handlerDesc);
        cv.visitVarInsn(Opcodes.ALOAD, 1);
        cv.visitMethodInsn(Opcodes.INVOKEINTERFACE, tabPlayerName, getUniqueId.getName(),
                "()" + uuidDesc, true);
        cv.visitVarInsn(Opcodes.ALOAD, 2);
        cv.visitMethodInsn(Opcodes.INVOKEINTERFACE, tabPlayerName, getUniqueId.getName(),
                "()" + uuidDesc, true);
        cv.visitMethodInsn(Opcodes.INVOKEINTERFACE, handlerName, "canSee",
                "(" + uuidDesc + uuidDesc + ")Z", true);
        cv.visitInsn(Opcodes.IRETURN);
        cv.visitMaxs(0, 0);
        cv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static final class Bridge implements TabVanishHandler {
        private final Vanish vanish;
        Bridge(Vanish vanish) { this.vanish = vanish; }

        @Override
        public boolean isVanished(java.util.UUID player) {
            return vanish.isVanished(player);
        }

        @Override
        public boolean canSee(java.util.UUID viewer, java.util.UUID target) {
            if (!vanish.isVanished(target)) {
                return true;
            }
            Player v = viewer == null ? null : Bukkit.getPlayer(viewer);
            Player t = target == null ? null : Bukkit.getPlayer(target);
            if (t == null) {
                return true;
            }
            if (v == null) {
                return false;
            }
            return Vanish.canSee(v, t);
        }
    }

    private static final class Loader extends ClassLoader {
        Loader(ClassLoader parent) {
            super(parent);
        }

        Class<?> define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
