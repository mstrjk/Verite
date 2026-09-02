package teacommontea.veritesauver.invsee;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


final class InvSeeMenuRoles {

    final Method clicked;
    final Class<?> clickModeType;
    final Method quickMoveStack;
    final Method stillValid;
    final Method removed;
    final Method getSlot;
    final Method moveItemStackTo;
    final Method getBukkitView;
    final java.lang.reflect.Field slotsField;
    final Method slotHasItem;
    final Method slotGetItem;
    final Method slotSet;
    final Method slotSetChanged;

    private InvSeeMenuRoles(Method clicked, Class<?> clickModeType, Method quickMoveStack,
                           Method stillValid, Method removed, Method getSlot,
                           Method moveItemStackTo, Method getBukkitView,
                           java.lang.reflect.Field slotsField, Method slotHasItem, Method slotGetItem,
                           Method slotSet, Method slotSetChanged) {
        this.clicked = clicked;
        this.clickModeType = clickModeType;
        this.quickMoveStack = quickMoveStack;
        this.stillValid = stillValid;
        this.removed = removed;
        this.getSlot = getSlot;
        this.moveItemStackTo = moveItemStackTo;
        this.getBukkitView = getBukkitView;
        this.slotsField = slotsField;
        this.slotHasItem = slotHasItem;
        this.slotGetItem = slotGetItem;
        this.slotSet = slotSet;
        this.slotSetChanged = slotSetChanged;
    }

    static InvSeeMenuRoles resolve(InvSeeAccess access) throws InvSeeAccess.Unsupported {
        Class<?> menu = access.menuClass();
        Class<?> item = access.itemStackClass();
        Class<?> player = access.playerClass();
        Class<?> slot = access.slotClass();

        Method clicked = null;
        Method quick = null;
        Method still = null;
        Method removed = null;
        Method getSlot = null;
        Method move = null;
        Method bukkitView = null;

        for (Method m : menu.getMethods()) {
            Class<?>[] p = m.getParameterTypes();
            Class<?> ret = m.getReturnType();
            if (ret == void.class && p.length == 4 && p[0] == int.class && p[1] == int.class
                    && !p[2].isPrimitive() && player.isAssignableFrom(p[3])) {
                clicked = m;
            } else if (ret == item && p.length == 2 && player.isAssignableFrom(p[0]) && p[1] == int.class) {
                quick = m;
            } else if (ret == boolean.class && p.length == 1 && player.isAssignableFrom(p[0])) {
                still = m;
            } else if (ret == void.class && p.length == 1 && player.isAssignableFrom(p[0])) {
                removed = m;
            } else if (ret == slot && p.length == 1 && p[0] == int.class) {
                getSlot = m;
            } else if (ret.getName().equals("org.bukkit.inventory.InventoryView") && p.length == 0) {
                bukkitView = m;
            }
        }

        move = findMove(menu, item);

        req(clicked, "clicked");
        req(quick, "quickMoveStack");
        req(still, "stillValid");
        req(removed, "removed");
        req(getSlot, "getSlot");
        req(move, "moveItemStackTo");

        java.lang.reflect.Field slotsField = findSlotsField(menu, slot);
        if (slotsField == null) {
            throw new InvSeeAccess.Unsupported("no slots list field on " + menu.getName());
        }
        slotsField.setAccessible(true);

        InvSeeSlotRoles slotRoles = InvSeeSlotRoles.resolve(access);
        Method slotHasItem = slotRoles.hasItem;
        Method slotGetItem = slotRoles.getItem;
        Method slotSet = slotRoles.set;
        Method slotSetChanged = findSlotSetChanged(slot);
        req(slotHasItem, "slotHasItem");
        req(slotGetItem, "slotGetItem");
        req(slotSet, "slotSet");
        req(slotSetChanged, "slotSetChanged");

        return new InvSeeMenuRoles(clicked, clicked.getParameterTypes()[2], quick, still, removed,
                getSlot, move, bukkitView, slotsField, slotHasItem, slotGetItem, slotSet, slotSetChanged);
    }

    private static java.lang.reflect.Field findSlotsField(Class<?> menu, Class<?> slot) {
        for (java.lang.reflect.Field f : menu.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            if (!java.util.List.class.isAssignableFrom(f.getType())) {
                continue;
            }
            java.lang.reflect.Type gt = f.getGenericType();
            if (gt instanceof java.lang.reflect.ParameterizedType pt) {
                java.lang.reflect.Type[] args = pt.getActualTypeArguments();
                if (args.length == 1 && args[0] == slot) {
                    return f;
                }
            }
        }
        for (java.lang.reflect.Field f : menu.getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())
                    && f.getType().getSimpleName().equals("NonNullList")) {
                return f;
            }
        }
        return null;
    }

    private static Method findSlotSetChanged(Class<?> slot) {
        Method found = null;
        for (Method m : slot.getMethods()) {
            if (m.getParameterCount() == 0 && m.getReturnType() == void.class
                    && m.getDeclaringClass() != Object.class
                    && java.lang.reflect.Modifier.isPublic(m.getModifiers())) {
                if (found != null) {
                    return null;
                }
                found = m;
            }
        }
        if (found != null) {
            found.setAccessible(true);
        }
        return found;
    }

    private static Method findMove(Class<?> menu, Class<?> item) {
        // The call sites invoke moveItemStackTo with four arguments
        // (ItemStack, startIndex, endIndex, reverseDirection). That four-argument overload exists on
        // both mappings; the Mojang class additionally carries a five-argument overload that plain
        // CraftBukkit does not, so match the four-argument form by structure to stay mapping-agnostic.
        // The method is inherited from the menu base, so walk the hierarchy rather than only the
        // declared methods of the resolved class.
        for (Class<?> c = menu; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                Class<?>[] p = m.getParameterTypes();
                if (m.getReturnType() == boolean.class && p.length == 4 && p[0] == item
                        && p[1] == int.class && p[2] == int.class && p[3] == boolean.class) {
                    m.setAccessible(true);
                    return m;
                }
            }
        }
        return null;
    }

    private static void req(Method m, String role) throws InvSeeAccess.Unsupported {
        if (m == null) {
            throw new InvSeeAccess.Unsupported("could not resolve menu role " + role);
        }
        m.setAccessible(true);
    }
}
