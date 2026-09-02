package teacommontea.veritesauver.invsee;

import java.lang.reflect.Method;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


final class InvSeeSlotGen {

    private InvSeeSlotGen() {}

    private static final String HANDLER = Type.getInternalName(SlotHandler.class);

    static byte[] generate(String internalName, InvSeeAccess access, InvSeeSlotRoles roles)
            throws InvSeeAccess.Unsupported {
        Class<?> slot = access.slotClass();
        Class<?> container = access.containerClass();
        Class<?> item = access.itemStackClass();
        String slotName = Type.getInternalName(slot);
        String handlerDesc = Type.getDescriptor(SlotHandler.class);
        String ctorDesc = "(" + Type.getDescriptor(container) + "III)V";

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, internalName, null, slotName, null);

        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "h", handlerDesc, null, null).visitEnd();

        MethodVisitor ct = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                "(" + Type.getDescriptor(container) + "III" + handlerDesc + ")V", null, null);
        ct.visitCode();
        ct.visitVarInsn(Opcodes.ALOAD, 0);
        ct.visitVarInsn(Opcodes.ALOAD, 1);
        ct.visitVarInsn(Opcodes.ILOAD, 2);
        ct.visitVarInsn(Opcodes.ILOAD, 3);
        ct.visitVarInsn(Opcodes.ILOAD, 4);
        ct.visitMethodInsn(Opcodes.INVOKESPECIAL, slotName, "<init>", ctorDesc, false);
        ct.visitVarInsn(Opcodes.ALOAD, 0);
        ct.visitVarInsn(Opcodes.ALOAD, 5);
        ct.visitFieldInsn(Opcodes.PUTFIELD, internalName, "h", handlerDesc);
        ct.visitInsn(Opcodes.RETURN);
        ct.visitMaxs(0, 0);
        ct.visitEnd();

        String OBJ = "Ljava/lang/Object;";

        String BOOL = "Ljava/lang/Boolean;";
        emitBoolGuard(cw, internalName, slotName, roles.mayPlace,
                "mayPlace", "(" + OBJ + OBJ + ")" + BOOL, true);
        emitBoolGuard(cw, internalName, slotName, roles.hasItem,
                "hasItem", "(" + OBJ + ")" + BOOL, false);
        emitBoolGuard(cw, internalName, slotName, roles.isActive,
                "isActive", "(" + OBJ + ")" + BOOL, false);
        emitBoolGuard(cw, internalName, slotName, roles.mayPickup,
                "mayPickup", "(" + OBJ + OBJ + ")" + BOOL, true);
        emitBoolGuard(cw, internalName, slotName, roles.allowModification,
                "allowModification", "(" + OBJ + OBJ + ")" + BOOL, true);

        emitItemReturn(cw, internalName, slotName, roles.getItem,
                "getItem", "(" + OBJ + ")" + OBJ, item);
        emitItemReturn(cw, internalName, slotName, roles.remove,
                "remove", "(" + OBJ + "I)" + OBJ, item);

        emitSet(cw, internalName, slotName, roles.set, item);
        emitMaxStack(cw, internalName, slotName, roles.getMaxStackSize);

        if (roles.getNoItemIcon != null) {
            emitNoIcon(cw, internalName, slotName, roles.getNoItemIcon);
        }

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void emitBoolGuard(ClassWriter cw, String owner, String superName, Method target,
                                      String handlerName, String handlerDesc, boolean hasPlayerArg) {
        String desc = Type.getMethodDescriptor(target);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, target.getName(), desc, null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, owner, "h", Type.getDescriptor(SlotHandler.class));
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        if (target.getParameterCount() == 1) {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
        }
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, HANDLER, handlerName, handlerDesc, true);

        int box = 1 + argSlots(target);
        mv.visitVarInsn(Opcodes.ASTORE, box);
        mv.visitVarInsn(Opcodes.ALOAD, box);
        Label deferLbl = new Label();
        mv.visitJumpInsn(Opcodes.IFNULL, deferLbl);
        mv.visitVarInsn(Opcodes.ALOAD, box);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
        mv.visitInsn(Opcodes.IRETURN);

        mv.visitLabel(deferLbl);
        superCall(mv, superName, target, desc);
        mv.visitInsn(Opcodes.IRETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void emitItemReturn(ClassWriter cw, String owner, String superName, Method target,
                                       String handlerName, String handlerDesc, Class<?> item) {
        String desc = Type.getMethodDescriptor(target);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, target.getName(), desc, null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, owner, "h", Type.getDescriptor(SlotHandler.class));
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        Type[] at = Type.getArgumentTypes(desc);
        int local = 1;
        for (Type t : at) {
            mv.visitVarInsn(t.getOpcode(Opcodes.ILOAD), local);
            local += t.getSize();
        }
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, HANDLER, handlerName, handlerDesc, true);

        int store = 1 + argSlots(target);
        mv.visitVarInsn(Opcodes.ASTORE, store);
        mv.visitVarInsn(Opcodes.ALOAD, store);
        Label deferLbl = new Label();
        mv.visitJumpInsn(Opcodes.IFNULL, deferLbl);
        mv.visitVarInsn(Opcodes.ALOAD, store);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(item));
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitLabel(deferLbl);
        superCall(mv, superName, target, desc);
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void emitSet(ClassWriter cw, String owner, String superName, Method target,
                                Class<?> item) {
        String desc = Type.getMethodDescriptor(target);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, target.getName(), desc, null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, owner, "h", Type.getDescriptor(SlotHandler.class));
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, HANDLER, "handleSet",
                "(Ljava/lang/Object;Ljava/lang/Object;)Z", true);

        Label deferLbl = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, deferLbl);
        mv.visitInsn(Opcodes.RETURN);

        mv.visitLabel(deferLbl);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, target.getName(), desc, false);
        mv.visitInsn(Opcodes.RETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void emitMaxStack(ClassWriter cw, String owner, String superName, Method target) {
        String desc = Type.getMethodDescriptor(target);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, target.getName(), desc, null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, owner, "h", Type.getDescriptor(SlotHandler.class));
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, HANDLER, "maxStackSize",
                "(Ljava/lang/Object;)I", true);

        mv.visitInsn(Opcodes.DUP);
        Label deferLbl = new Label();
        mv.visitJumpInsn(Opcodes.IFLT, deferLbl);
        mv.visitInsn(Opcodes.IRETURN);

        mv.visitLabel(deferLbl);
        mv.visitInsn(Opcodes.POP);
        superCall(mv, superName, target, desc);
        mv.visitInsn(Opcodes.IRETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void emitNoIcon(ClassWriter cw, String owner, String superName, Method target) {
        String desc = Type.getMethodDescriptor(target);
        Type ret = Type.getReturnType(desc);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, target.getName(), desc, null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, owner, "h", Type.getDescriptor(SlotHandler.class));
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, HANDLER, "noItemIcon",
                "(Ljava/lang/Object;)Ljava/lang/Object;", true);

        mv.visitInsn(Opcodes.DUP);
        Label deferLbl = new Label();
        mv.visitJumpInsn(Opcodes.IFNULL, deferLbl);
        mv.visitTypeInsn(Opcodes.CHECKCAST, ret.getInternalName());
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitLabel(deferLbl);
        mv.visitInsn(Opcodes.POP);
        superCall(mv, superName, target, desc);
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void superCall(MethodVisitor mv, String superName, Method target, String desc) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        Type[] at = Type.getArgumentTypes(desc);
        int local = 1;
        for (Type t : at) {
            mv.visitVarInsn(t.getOpcode(Opcodes.ILOAD), local);
            local += t.getSize();
        }
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, target.getName(), desc, false);
    }

    private static int argSlots(Method m) {
        int n = 0;
        for (Class<?> p : m.getParameterTypes()) {
            n += (p == long.class || p == double.class) ? 2 : 1;
        }
        return n;
    }
}
