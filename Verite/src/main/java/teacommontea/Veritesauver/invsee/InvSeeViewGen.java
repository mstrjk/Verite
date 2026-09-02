package teacommontea.veritesauver.invsee;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


final class InvSeeViewGen {

    private InvSeeViewGen() {}

    static byte[] generate(String internalName, InvSeeAccess access, Class<?> craftViewClass,
                           Constructor<?> superCtor, Method getHandle) throws InvSeeAccess.Unsupported {
        Class<?> menu = access.menuClass();
        String superName = Type.getInternalName(craftViewClass);
        String menuDesc = Type.getDescriptor(menu);
        String menuInternal = Type.getInternalName(menu);

        Class<?>[] sp = superCtor.getParameterTypes();
        if (sp.length != 3) {
            throw new InvSeeAccess.Unsupported("unexpected CraftInventoryView ctor arity " + sp.length);
        }
        String humanDesc = Type.getDescriptor(sp[0]);
        String invDesc = Type.getDescriptor(sp[1]);
        String menuParamDesc = Type.getDescriptor(sp[2]);
        String superCtorDesc = "(" + humanDesc + invDesc + menuParamDesc + ")V";

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, internalName, null, superName, null);
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "menu", menuDesc, null, null).visitEnd();

        String ctorDesc = "(" + humanDesc + invDesc + menuDesc + ")V";
        MethodVisitor ct = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", ctorDesc, null, null);
        ct.visitCode();
        ct.visitVarInsn(Opcodes.ALOAD, 0);
        ct.visitVarInsn(Opcodes.ALOAD, 1);
        ct.visitVarInsn(Opcodes.ALOAD, 2);
        ct.visitVarInsn(Opcodes.ALOAD, 3);
        ct.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", superCtorDesc, false);
        ct.visitVarInsn(Opcodes.ALOAD, 0);
        ct.visitVarInsn(Opcodes.ALOAD, 3);
        ct.visitFieldInsn(Opcodes.PUTFIELD, internalName, "menu", menuDesc);
        ct.visitInsn(Opcodes.RETURN);
        ct.visitMaxs(0, 0);
        ct.visitEnd();

        String getHandleDesc = Type.getMethodDescriptor(getHandle);
        MethodVisitor gh = cw.visitMethod(Opcodes.ACC_PUBLIC, getHandle.getName(), getHandleDesc, null, null);
        gh.visitCode();
        gh.visitVarInsn(Opcodes.ALOAD, 0);
        gh.visitFieldInsn(Opcodes.GETFIELD, internalName, "menu", menuDesc);
        Type ret = Type.getReturnType(getHandleDesc);
        if (!ret.getInternalName().equals(menuInternal)) {
            gh.visitTypeInsn(Opcodes.CHECKCAST, ret.getInternalName());
        }
        gh.visitInsn(Opcodes.ARETURN);
        gh.visitMaxs(0, 0);
        gh.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    static Constructor<?> resolveSuperCtor(Class<?> craftViewClass, InvSeeAccess access)
            throws InvSeeAccess.Unsupported {
        Class<?> menu = access.menuClass();
        for (Constructor<?> c : craftViewClass.getConstructors()) {
            Class<?>[] p = c.getParameterTypes();
            if (p.length == 3
                    && p[0].getName().equals("org.bukkit.entity.HumanEntity")
                    && org.bukkit.inventory.Inventory.class.isAssignableFrom(p[1])
                    && p[2].isAssignableFrom(menu)) {
                return c;
            }
        }
        throw new InvSeeAccess.Unsupported(
                "no (HumanEntity, Inventory, menu) CraftInventoryView constructor");
    }

    static Method resolveGetHandle(Class<?> craftViewClass, InvSeeAccess access)
            throws InvSeeAccess.Unsupported {
        Class<?> menu = access.menuClass();
        for (Method m : craftViewClass.getMethods()) {
            if (m.getName().equals("getHandle") && m.getParameterCount() == 0
                    && m.getReturnType().isAssignableFrom(menu)) {
                return m;
            }
        }
        throw new InvSeeAccess.Unsupported("no getHandle returning menu on CraftInventoryView");
    }
}
