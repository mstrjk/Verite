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


final class InvSeeSlotRoles {

    final Method mayPlace;
    final Method getItem;
    final Method hasItem;
    final Method isActive;
    final Method set;
    final Method getMaxStackSize;
    final Method remove;
    final Method mayPickup;
    final Method allowModification;
    final Method getNoItemIcon;

    private InvSeeSlotRoles(Method[] r) {
        this.mayPlace = r[0];
        this.getItem = r[1];
        this.hasItem = r[2];
        this.isActive = r[3];
        this.set = r[4];
        this.getMaxStackSize = r[5];
        this.remove = r[6];
        this.mayPickup = r[7];
        this.allowModification = r[8];
        this.getNoItemIcon = r[9];
    }

    private record Entry(String name, String desc) {}

    static InvSeeSlotRoles resolve(InvSeeAccess access) throws InvSeeAccess.Unsupported {
        Class<?> slot = access.slotClass();
        Class<?> item = access.itemStackClass();
        Class<?> player = access.playerClass();

        List<Entry> ordered = readOrder(slot);

        String itemDesc = Type.getDescriptor(item);
        String playerDesc = Type.getDescriptor(player);

        Entry mayPlace = nth(ordered, "(" + itemDesc + ")Z", 0);
        Entry getItem = nth(ordered, "()" + itemDesc, 0);
        Entry hasItem = nth(ordered, "()Z", 0);
        Entry isActive = nth(ordered, "()Z", 1);
        Entry set = lastOf(ordered, "(" + itemDesc + ")V");
        Entry maxStack = nth(ordered, "()I", 0);
        Entry remove = nth(ordered, "(I)" + itemDesc, 0);
        Entry mayPickup = nth(ordered, "(" + playerDesc + ")Z", 0);
        Entry allowMod = nth(ordered, "(" + playerDesc + ")Z", 1);
        Entry noIcon = noItemIcon(ordered);

        Method[] r = new Method[10];
        r[0] = find(slot, mayPlace, "mayPlace");
        r[1] = find(slot, getItem, "getItem");
        r[2] = find(slot, hasItem, "hasItem");
        r[3] = find(slot, isActive, "isActive");
        r[4] = find(slot, set, "set");
        r[5] = find(slot, maxStack, "getMaxStackSize");
        r[6] = find(slot, remove, "remove");
        r[7] = find(slot, mayPickup, "mayPickup");
        r[8] = find(slot, allowMod, "allowModification");
        r[9] = noIcon == null ? null : findOptional(slot, noIcon);

        return new InvSeeSlotRoles(r);
    }

    private static List<Entry> readOrder(Class<?> slot) throws InvSeeAccess.Unsupported {
        String res = slot.getName().replace('.', '/') + ".class";
        List<Entry> out = new ArrayList<>();
        try (InputStream in = slot.getClassLoader().getResourceAsStream(res)) {
            if (in == null) {
                throw new InvSeeAccess.Unsupported("cannot read Slot class bytes: " + res);
            }
            ClassReader cr = new ClassReader(in);
            cr.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int accessFlags, String name, String descriptor,
                                                 String signature, String[] exceptions) {
                    out.add(new Entry(name, descriptor));
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (InvSeeAccess.Unsupported u) {
            throw u;
        } catch (Exception e) {
            throw new InvSeeAccess.Unsupported("failed reading Slot bytes: " + e);
        }
        return out;
    }

    private static Entry nth(List<Entry> ordered, String desc, int ordinal) {
        int seen = 0;
        for (Entry e : ordered) {
            if (e.desc().equals(desc)) {
                if (seen == ordinal) return e;
                seen++;
            }
        }
        return null;
    }

    private static Entry lastOf(List<Entry> ordered, String desc) {
        Entry found = null;
        for (Entry e : ordered) {
            if (e.desc().equals(desc)) found = e;
        }
        return found;
    }

    private static Entry noItemIcon(List<Entry> ordered) {
        for (Entry e : ordered) {
            if (!e.desc().startsWith("()")) continue;
            String ret = e.desc().substring(2);
            if (ret.equals("Lnet/minecraft/resources/Identifier;")
                    || ret.equals("Lnet/minecraft/resources/ResourceLocation;")
                    || ret.equals("Lnet/minecraft/resources/MinecraftKey;")
                    || ret.equals("Lcom/mojang/datafixers/util/Pair;")) {
                return e;
            }
        }
        return null;
    }

    private static Method find(Class<?> slot, Entry e, String role) throws InvSeeAccess.Unsupported {
        if (e == null) {
            throw new InvSeeAccess.Unsupported("could not locate Slot role " + role);
        }
        Method m = findOptional(slot, e);
        if (m == null) {
            throw new InvSeeAccess.Unsupported("resolved Slot role " + role
                    + " to " + e.name() + e.desc() + " but no matching Method");
        }
        return m;
    }

    private static Method findOptional(Class<?> slot, Entry e) {
        for (Method m : slot.getMethods()) {
            if (m.getName().equals(e.name()) && Type.getMethodDescriptor(m).equals(e.desc())) {
                m.setAccessible(true);
                return m;
            }
        }
        for (Method m : slot.getDeclaredMethods()) {
            if (m.getName().equals(e.name()) && Type.getMethodDescriptor(m).equals(e.desc())) {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }
}
