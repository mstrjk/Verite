package teacommontea.veritesauver.invsee;

import java.lang.reflect.Constructor;


public final class InvSeeClassgen {

    private final InvSeeAccess access;
    private final Constructor<?> containerCtor;
    private final Constructor<?> slotCtor;
    private final Constructor<?> menuCtor;
    private final Constructor<?> viewCtor;

    private InvSeeClassgen(InvSeeAccess access, Constructor<?> containerCtor,
                          Constructor<?> slotCtor, Constructor<?> menuCtor, Constructor<?> viewCtor) {
        this.access = access;
        this.containerCtor = containerCtor;
        this.slotCtor = slotCtor;
        this.menuCtor = menuCtor;
        this.viewCtor = viewCtor;
    }

    public InvSeeAccess access() {
        return access;
    }

    public Object newContainer(ContainerHandler handler) throws Throwable {
        return containerCtor.newInstance(handler);
    }

    public Object newSlot(Object container, int index, int x, int y, SlotHandler handler)
            throws Throwable {
        return slotCtor.newInstance(container, index, x, y, handler);
    }

    public Object newMenu(Object menuType, int id, MenuHandler handler) throws Throwable {
        return menuCtor.newInstance(menuType, id, handler);
    }

    public boolean hasView() {
        return viewCtor != null;
    }

    public Object newView(org.bukkit.entity.HumanEntity human, org.bukkit.inventory.Inventory top,
                          Object menu) throws Throwable {
        return viewCtor == null ? null : viewCtor.newInstance(human, top, menu);
    }

    public static InvSeeClassgen build() throws InvSeeAccess.Unsupported {
        try {
            InvSeeAccess access = InvSeeAccess.resolve();
            InvSeeSlotRoles slotRoles = InvSeeSlotRoles.resolve(access);
            InvSeeMenuRoles menuRoles = InvSeeMenuRoles.resolve(access);

            Loader loader = new Loader(InvSeeClassgen.class.getClassLoader());
            String pkg = "teacommontea/veritesauver/invsee/gen/";

            byte[] cBytes = InvSeeContainerGen.generate(pkg + "GenContainer", access);
            Class<?> cClass = loader.define("teacommontea.veritesauver.invsee.gen.GenContainer", cBytes);

            byte[] sBytes = InvSeeSlotGen.generate(pkg + "GenSlot", access, slotRoles);
            Class<?> sClass = loader.define("teacommontea.veritesauver.invsee.gen.GenSlot", sBytes);

            byte[] mBytes = InvSeeMenuGen.generate(pkg + "GenMenu", access, menuRoles);
            Class<?> mClass = loader.define("teacommontea.veritesauver.invsee.gen.GenMenu", mBytes);

            Constructor<?> cCtor = cClass.getConstructor(ContainerHandler.class);
            Constructor<?> sCtor = sClass.getConstructor(access.containerClass(), int.class, int.class,
                    int.class, SlotHandler.class);
            Class<?> menuType = InvSeeAccess.firstExisting(
                    "net.minecraft.world.inventory.MenuType",
                    "net.minecraft.world.inventory.Containers");
            Constructor<?> mCtor = mClass.getConstructor(menuType, int.class, MenuHandler.class);

            Constructor<?> vCtor = buildView(access, loader, pkg);

            return new InvSeeClassgen(access, cCtor, sCtor, mCtor, vCtor);
        } catch (InvSeeAccess.Unsupported u) {
            throw u;
        } catch (Throwable t) {
            throw new InvSeeAccess.Unsupported("class generation failed: " + t, t);
        }
    }

    private static Constructor<?> buildView(InvSeeAccess access, Loader loader, String pkg) {
        try {
            Class<?> craftView = InvSeeAccess.obc("inventory.CraftInventoryView");
            java.lang.reflect.Constructor<?> superCtor =
                    InvSeeViewGen.resolveSuperCtor(craftView, access);
            java.lang.reflect.Method getHandle = InvSeeViewGen.resolveGetHandle(craftView, access);
            byte[] bytes = InvSeeViewGen.generate(pkg + "GenView", access, craftView, superCtor, getHandle);
            Class<?> vClass = loader.define("teacommontea.veritesauver.invsee.gen.GenView", bytes);
            return vClass.getConstructor(org.bukkit.entity.HumanEntity.class,
                    org.bukkit.inventory.Inventory.class, access.menuClass());
        } catch (Throwable t) {
            return null;
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
