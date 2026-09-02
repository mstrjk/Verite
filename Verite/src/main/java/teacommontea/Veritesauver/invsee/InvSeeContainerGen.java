package teacommontea.veritesauver.invsee;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


final class InvSeeContainerGen {

    private InvSeeContainerGen() {}

    private static final String HANDLER = Type.getInternalName(ContainerHandler.class);
    private static final String HANDLER_DESC = Type.getDescriptor(ContainerHandler.class);

    private enum Role {
        SIZE("()I", "getContainerSize", "()I"),
        IS_EMPTY("()Z", "isEmpty", "()Z"),
        GET_ITEM("(I)L", "getItem", "(I)Ljava/lang/Object;"),
        REMOVE_ITEM("(II)L", "removeItem", "(II)Ljava/lang/Object;"),
        REMOVE_NO_UPDATE("(I)L", "removeItemNoUpdate", "(I)Ljava/lang/Object;"),
        SET_ITEM("(IL", "setItem", "(ILjava/lang/Object;)V"),
        MAX_STACK("()I", "getMaxStackSize", "()I"),
        SET_CHANGED("()V", "setChanged", "()V"),
        STILL_VALID("(L1)Z", "stillValid", "(Ljava/lang/Object;)Z"),
        SET_MAX_STACK("(I)V", "setMaxStackSize", "(I)V"),
        GET_CONTENTS("()Ljava/util/List;", "getContents", "()Ljava/util/List;"),
        GET_VIEWERS("()Ljava/util/List;", "getViewers", "()Ljava/util/List;"),
        ON_OPEN("(Lcb)V", "onOpen", "(Lorg/bukkit/entity/HumanEntity;)V"),
        ON_CLOSE("(Lcb)V", "onClose", "(Lorg/bukkit/entity/HumanEntity;)V"),
        GET_OWNER("()Lorg/bukkit/inventory/InventoryHolder;", "getOwner",
                "()Lorg/bukkit/inventory/InventoryHolder;"),
        GET_LOCATION("()Lorg/bukkit/Location;", "getLocation", "()Lorg/bukkit/Location;");

        final String handlerName;
        final String handlerDesc;
        Role(String shapeHint, String handlerName, String handlerDesc) {
            this.handlerName = handlerName;
            this.handlerDesc = handlerDesc;
        }
    }

    static byte[] generate(String internalName, InvSeeAccess access) throws InvSeeAccess.Unsupported {
        return generate(internalName, access.containerClass(), access.itemStackClass(),
                access.playerClass());
    }

    static byte[] generate(String internalName, Class<?> iface, Class<?> itemStack,
                           Class<?> playerClass) throws InvSeeAccess.Unsupported {
        Map<Method, Role> bound = bindRoles(iface, itemStack, playerClass);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, internalName, null,
                "java/lang/Object", new String[]{ Type.getInternalName(iface) });

        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "h", HANDLER_DESC, null, null).visitEnd();

        MethodVisitor ctor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                "(" + HANDLER_DESC + ")V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitVarInsn(Opcodes.ALOAD, 1);
        ctor.visitFieldInsn(Opcodes.PUTFIELD, internalName, "h", HANDLER_DESC);
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();

        for (Map.Entry<Method, Role> e : bound.entrySet()) {
            emit(cw, internalName, e.getKey(), e.getValue(), itemStack);
        }

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static Map<Method, Role> bindRoles(Class<?> iface, Class<?> itemStack, Class<?> playerClass)
            throws InvSeeAccess.Unsupported {
        List<Method> abstracts = new ArrayList<>();
        for (Method m : iface.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers()) && !m.isDefault()) {
                abstracts.add(m);
            }
        }
        sortByDeclarationOrder(iface, abstracts);

        Map<Method, Role> out = new LinkedHashMap<>();
        int intNoArg = 0;
        int itemFromInt = 0;
        int listNoArg = 0;
        for (Method m : abstracts) {
            Role r = classify(m, itemStack, playerClass, intNoArg, itemFromInt, listNoArg);
            if (r == null) {
                throw new InvSeeAccess.Unsupported(
                        "unrecognised abstract container method " + m + " on " + iface.getName());
            }
            if (r == Role.SIZE || r == Role.MAX_STACK) intNoArg++;
            if (r == Role.GET_ITEM || r == Role.REMOVE_NO_UPDATE) itemFromInt++;
            if (r == Role.GET_CONTENTS || r == Role.GET_VIEWERS) listNoArg++;
            out.put(m, r);
        }
        return out;
    }

    private static void sortByDeclarationOrder(Class<?> iface, List<Method> methods) {
        List<String> order = new ArrayList<>();
        try (java.io.InputStream in = iface.getClassLoader()
                .getResourceAsStream(iface.getName().replace('.', '/') + ".class")) {
            if (in == null) {
                return;
            }
            org.objectweb.asm.ClassReader cr = new org.objectweb.asm.ClassReader(in);
            cr.accept(new org.objectweb.asm.ClassVisitor(Opcodes.ASM9) {
                @Override
                public org.objectweb.asm.MethodVisitor visitMethod(int a, String name, String desc,
                        String sig, String[] ex) {
                    order.add(name + desc);
                    return null;
                }
            }, org.objectweb.asm.ClassReader.SKIP_CODE | org.objectweb.asm.ClassReader.SKIP_DEBUG
                    | org.objectweb.asm.ClassReader.SKIP_FRAMES);
        } catch (Exception e) {
            return;
        }
        if (order.isEmpty()) {
            return;
        }
        methods.sort((x, y) -> {
            int ix = order.indexOf(x.getName() + Type.getMethodDescriptor(x));
            int iy = order.indexOf(y.getName() + Type.getMethodDescriptor(y));
            if (ix < 0) ix = Integer.MAX_VALUE;
            if (iy < 0) iy = Integer.MAX_VALUE;
            return Integer.compare(ix, iy);
        });
    }

    private static Role classify(Method m, Class<?> itemStack, Class<?> playerClass,
                                 int intNoArgSeen, int itemFromIntSeen, int listNoArgSeen) {
        Class<?>[] p = m.getParameterTypes();
        Class<?> ret = m.getReturnType();

        if (p.length == 0 && ret == int.class) {
            return intNoArgSeen == 0 ? Role.SIZE : Role.MAX_STACK;
        }
        if (p.length == 0 && ret == boolean.class) return Role.IS_EMPTY;
        if (p.length == 1 && p[0] == int.class && ret == itemStack) {
            return itemFromIntSeen == 0 ? Role.GET_ITEM : Role.REMOVE_NO_UPDATE;
        }
        if (p.length == 2 && p[0] == int.class && p[1] == int.class && ret == itemStack) {
            return Role.REMOVE_ITEM;
        }
        if (p.length == 2 && p[0] == int.class && p[1] == itemStack && ret == void.class) {
            return Role.SET_ITEM;
        }
        if (p.length == 0 && ret == void.class) return Role.SET_CHANGED;
        if (p.length == 1 && playerClass.isAssignableFrom(p[0]) && ret == boolean.class) {
            return Role.STILL_VALID;
        }
        if (p.length == 1 && p[0] == int.class && ret == void.class) return Role.SET_MAX_STACK;
        if (p.length == 0 && ret == List.class) {
            return listNoArgSeen == 0 ? Role.GET_CONTENTS : Role.GET_VIEWERS;
        }
        if (p.length == 1 && ret == void.class && isCraftHuman(p[0])) {
            return craftOpenOrClose(m);
        }
        if (p.length == 0 && ret.getName().equals("org.bukkit.inventory.InventoryHolder")) {
            return Role.GET_OWNER;
        }
        if (p.length == 0 && ret.getName().equals("org.bukkit.Location")) return Role.GET_LOCATION;
        return null;
    }

    private static boolean isCraftHuman(Class<?> c) {
        return c.getName().endsWith(".entity.CraftHumanEntity");
    }

    private static Role craftOpenOrClose(Method m) {
        String n = m.getName().toLowerCase();
        if (n.contains("close")) return Role.ON_CLOSE;
        return Role.ON_OPEN;
    }

    private static void emit(ClassWriter cw, String owner, Method m, Role role, Class<?> itemStack) {
        String desc = Type.getMethodDescriptor(m);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), desc, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, owner, "h", HANDLER_DESC);

        Type[] args = Type.getArgumentTypes(desc);
        int local = 1;
        for (Type at : args) {
            mv.visitVarInsn(at.getOpcode(Opcodes.ILOAD), local);
            local += at.getSize();
        }

        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, HANDLER, role.handlerName, role.handlerDesc, true);

        Type ret = Type.getReturnType(desc);
        Type handlerRet = Type.getReturnType(role.handlerDesc);
        if (handlerRet.getSort() == Type.OBJECT && ret.getSort() == Type.OBJECT
                && !handlerRet.getInternalName().equals(ret.getInternalName())) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, ret.getInternalName());
        }
        mv.visitInsn(ret.getOpcode(Opcodes.IRETURN));
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
