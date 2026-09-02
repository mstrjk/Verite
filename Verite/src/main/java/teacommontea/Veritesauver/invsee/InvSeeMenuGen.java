package teacommontea.veritesauver.invsee;

import java.lang.reflect.Method;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


final class InvSeeMenuGen {

    private InvSeeMenuGen() {}

    private static final String HANDLER = Type.getInternalName(MenuHandler.class);
    private static final String HANDLER_DESC = Type.getDescriptor(MenuHandler.class);

    static byte[] generate(String internalName, InvSeeAccess access, InvSeeMenuRoles roles)
            throws InvSeeAccess.Unsupported {
        Class<?> menu = access.menuClass();
        Class<?> item = access.itemStackClass();
        Class<?> menuType = InvSeeAccess.firstExisting(
                "net.minecraft.world.inventory.MenuType",
                "net.minecraft.world.inventory.Containers");
        if (menuType == null) {
            throw new InvSeeAccess.Unsupported("no MenuType class on this server");
        }
        String menuName = Type.getInternalName(menu);
        String menuTypeDesc = Type.getDescriptor(menuType);
        String superCtorDesc = "(" + menuTypeDesc + "I)V";

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, internalName, null, menuName, null);
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "h", HANDLER_DESC, null, null).visitEnd();

        MethodVisitor ct = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                "(" + menuTypeDesc + "I" + HANDLER_DESC + ")V", null, null);
        ct.visitCode();
        ct.visitVarInsn(Opcodes.ALOAD, 0);
        ct.visitVarInsn(Opcodes.ALOAD, 1);
        ct.visitVarInsn(Opcodes.ILOAD, 2);
        ct.visitMethodInsn(Opcodes.INVOKESPECIAL, menuName, "<init>", superCtorDesc, false);
        ct.visitVarInsn(Opcodes.ALOAD, 0);
        ct.visitVarInsn(Opcodes.ALOAD, 3);
        ct.visitFieldInsn(Opcodes.PUTFIELD, internalName, "h", HANDLER_DESC);
        String checkReachable = access.checkReachableFieldName();
        if (checkReachable != null) {
            ct.visitVarInsn(Opcodes.ALOAD, 0);
            ct.visitInsn(Opcodes.ICONST_0);
            ct.visitFieldInsn(Opcodes.PUTFIELD, menuName, checkReachable, "Z");
        }
        ct.visitInsn(Opcodes.RETURN);
        ct.visitMaxs(0, 0);
        ct.visitEnd();

        emitClicked(cw, internalName, menuName, roles.clicked);
        emitQuickMove(cw, internalName, menuName, roles.quickMoveStack, item);
        emitStillValid(cw, internalName, roles.stillValid);
        emitRemoved(cw, internalName, menuName, roles.removed);
        if (roles.getBukkitView != null) {
            emitBukkitView(cw, internalName, roles.getBukkitView);
        }

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void emitClicked(ClassWriter cw, String owner, String superName, Method clicked) {
        String desc = Type.getMethodDescriptor(clicked);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, clicked.getName(), desc, null, null);
        mv.visitCode();

        callHandler1(mv, owner, "clickedEntry");

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, owner, "h", HANDLER_DESC);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, HANDLER, "tracks", "()Z", true);
        Label untracked = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, untracked);

        callHandler1(mv, owner, "beforeClick");
        superClicked(mv, superName, clicked, desc);
        callHandler1(mv, owner, "afterClick");
        mv.visitInsn(Opcodes.RETURN);

        mv.visitLabel(untracked);
        superClicked(mv, superName, clicked, desc);
        mv.visitInsn(Opcodes.RETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void callHandler1(MethodVisitor mv, String owner, String name) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, owner, "h", HANDLER_DESC);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, HANDLER, name, "(Ljava/lang/Object;)V", true);
    }

    private static void superClicked(MethodVisitor mv, String superName, Method clicked, String desc) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        Type[] at = Type.getArgumentTypes(desc);
        int local = 1;
        for (Type t : at) {
            mv.visitVarInsn(t.getOpcode(Opcodes.ILOAD), local);
            local += t.getSize();
        }
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, clicked.getName(), desc, false);
    }

    private static void emitQuickMove(ClassWriter cw, String owner, String superName, Method quick,
                                      Class<?> item) {
        String desc = Type.getMethodDescriptor(quick);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, quick.getName(), desc, null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, owner, "h", HANDLER_DESC);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, HANDLER, "quickMove",
                "(Ljava/lang/Object;Ljava/lang/Object;I)Ljava/lang/Object;", true);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(item));
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void emitStillValid(ClassWriter cw, String owner, Method still) {
        String desc = Type.getMethodDescriptor(still);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, still.getName(), desc, null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void emitRemoved(ClassWriter cw, String owner, String superName, Method removed) {
        String desc = Type.getMethodDescriptor(removed);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, removed.getName(), desc, null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, removed.getName(), desc, false);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, owner, "h", HANDLER_DESC);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, HANDLER, "removed",
                "(Ljava/lang/Object;Ljava/lang/Object;)V", true);
        mv.visitInsn(Opcodes.RETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void emitBukkitView(ClassWriter cw, String owner, Method view) {
        String desc = Type.getMethodDescriptor(view);
        Type ret = Type.getReturnType(desc);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, view.getName(), desc, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, owner, "h", HANDLER_DESC);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, HANDLER, "bukkitView",
                "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        mv.visitTypeInsn(Opcodes.CHECKCAST, ret.getInternalName());
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
