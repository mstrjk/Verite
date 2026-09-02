package teacommontea.veritesauver.invsee;

import java.util.List;


final class MenuHandlers {

    private MenuHandlers() {}

    abstract static class Base implements MenuHandler {
        final InvSeeClassgen gen;
        final InvSeeAccess access;
        final InvSeeMenuRoles roles;
        final ContainerHandler top;
        final SpectateLog log;
        final Object spectator;
        Object bukkitView;

        Base(InvSeeClassgen gen, InvSeeMenuRoles roles, ContainerHandler top,
             SpectateLog log, Object spectator) {
            this.gen = gen;
            this.access = gen.access();
            this.roles = roles;
            this.top = top;
            this.log = log;
            this.spectator = spectator;
            if (log != null) {
                log.onOpen();
            }
        }

        @Override
        public void clickedEntry(Object menu) {
        }

        @Override
        public boolean tracks() {
            return log != null;
        }

        @Override
        public void beforeClick(Object menu) {
            snapshotBefore = snapshot();
        }

        private List<org.bukkit.inventory.ItemStack> snapshotBefore;

        @Override
        public void afterClick(Object menu) {
            if (log != null) {
                log.onClick(snapshotBefore, snapshot());
            }
        }

        private List<org.bukkit.inventory.ItemStack> snapshot() {
            List<Object> nms = top.getContents();
            List<org.bukkit.inventory.ItemStack> out = new java.util.ArrayList<>(nms.size());
            for (Object o : nms) {
                try {
                    out.add(access.asBukkitCopy(o));
                } catch (Throwable t) {
                    out.add(null);
                }
            }
            return out;
        }

        @Override
        public void removed(Object menu, Object nmsPlayer) {
            if (log != null && java.util.Objects.equals(nmsPlayer, spectator)) {
                log.onClose();
            }
        }

        @Override
        public Object bukkitView(Object menu) {
            if (bukkitView == null) {
                bukkitView = buildView(menu);
            }
            return bukkitView;
        }

        abstract Object buildView(Object menu);

        Object filler(Palette palette) {
            try {
                return access.asNmsCopy(palette.get(Palette.Kind.INACCESSIBLE));
            } catch (Throwable t) {
                return null;
            }
        }

        Object emptyStack() {
            try {
                return access.emptyStack();
            } catch (Throwable t) {
                return null;
            }
        }

        Object quickMoveBetween(Object menu, int rawIndex, int topSize, boolean allowFromTop) {
            try {
                List<Object> slots = menuSlots(menu);
                if (rawIndex < 0 || rawIndex >= slots.size()) {
                    return emptyStack();
                }
                Object slot = slots.get(rawIndex);
                if (!(boolean) roles.slotHasItem.invoke(slot)) {
                    return emptyStack();
                }
                Object clicked = roles.slotGetItem.invoke(slot);
                Object result = copyOf(clicked);
                boolean moved;
                if (rawIndex < topSize) {
                    if (!allowFromTop) {
                        return emptyStack();
                    }
                    moved = (boolean) roles.moveItemStackTo.invoke(menu, clicked, topSize, slots.size(), true);
                } else {
                    moved = (boolean) roles.moveItemStackTo.invoke(menu, clicked, 0, topSize, false);
                }
                if (!moved) {
                    return emptyStack();
                }
                if (access.isEmptyStack(clicked)) {
                    roles.slotSet.invoke(slot, emptyStack());
                } else {
                    roles.slotSetChanged.invoke(slot);
                }
                return result;
            } catch (Throwable t) {
                return emptyStack();
            }
        }

        private Object copyOf(Object nmsStack) {
            try {
                return access.asNmsCopy(access.asBukkitCopy(nmsStack));
            } catch (Throwable t) {
                return nmsStack;
            }
        }

        @SuppressWarnings("unchecked")
        List<Object> menuSlots(Object menu) throws Throwable {
            java.lang.reflect.Field f = roles.slotsField;
            return (List<Object>) f.get(menu);
        }
    }

    static final class Main extends Base {
        private final Mirror mirror;
        private final Palette palette;
        private final Object playerInv;
        private final boolean own;
        private final MainViewContainer view;

        Main(InvSeeClassgen gen, InvSeeMenuRoles roles, MainViewContainer view, SpectateLog log,
             Object spectator, Object playerInv, Mirror mirror, Palette palette, boolean own) {
            super(gen, roles, view, log, spectator);
            this.view = view;
            this.mirror = mirror;
            this.palette = palette;
            this.playerInv = playerInv;
            this.own = own;
        }

        void layout(Object menu, Object genContainer) throws Throwable {
            for (int i = 0; i < 36; i++) {
                addTop(menu, genContainer, i, i, SlotHandlers.Plain.INSTANCE);
            }
            addFiller(menu, genContainer, 36, nms(InvSeeIcons.placeholder()));
            addFiller(menu, genContainer, 37, nms(InvSeeIcons.placeholder()));
            addEquip(menu, genContainer, 38, nms(InvSeeIcons.heldLabel()));
            addEquip(menu, genContainer, 39, nms(InvSeeIcons.offhandLabel()));
            addVehicle(menu, genContainer, 40);
            addEquip(menu, genContainer, 41, nms(InvSeeIcons.armourLabel("Helmet")));
            addEquip(menu, genContainer, 42, nms(InvSeeIcons.armourLabel("Chestplate")));
            addEquip(menu, genContainer, 43, nms(InvSeeIcons.armourLabel("Pants")));
            addEquip(menu, genContainer, 44, nms(InvSeeIcons.armourLabel("Boots")));
            addPlayerRows(menu);
        }

        private void addEquip(Object menu, Object container, int position, Object filler) throws Throwable {
            int x = position % 9;
            int y = position / 9;
            int px = 8 + x * 18;
            int py = 18 + y * 18;
            access.menuAddSlot(menu, gen.newSlot(container, position, px, py,
                    new SlotHandlers.Equip(filler, access, view, position)));
        }

        private void addTop(Object menu, Object container, int position, int containerIndex,
                            SlotHandler handler) throws Throwable {
            int x = position % 9;
            int y = position / 9;
            int px = 8 + x * 18;
            int py = 18 + y * 18;
            access.menuAddSlot(menu, gen.newSlot(container, containerIndex, px, py, handler));
        }

        private void addFiller(Object menu, Object container, int position, Object filler) throws Throwable {
            int x = position % 9;
            int y = position / 9;
            int px = 8 + x * 18;
            int py = 18 + y * 18;
            access.menuAddSlot(menu, gen.newSlot(container, position, px, py,
                    new SlotHandlers.Inaccessible(filler == null ? emptyStack() : filler, emptyStack())));
        }

        private void addVehicle(Object menu, Object container, int position) throws Throwable {
            org.bukkit.inventory.ItemStack vehicle = vehicleItem();
            Object icon = vehicle != null ? nms(vehicle) : nms(InvSeeIcons.vehicleLabel());
            addFiller(menu, container, position, icon);
        }

        private org.bukkit.inventory.ItemStack vehicleItem() {
            try {
                Object target = view.nmsTarget();
                java.lang.reflect.Method getBukkit = target.getClass().getMethod("getBukkitEntity");
                Object bukkit = getBukkit.invoke(target);
                if (bukkit instanceof org.bukkit.entity.Entity entity) {
                    return InvSeeVehicle.displayFor(entity.getVehicle());
                }
            } catch (Throwable ignored) {
            }
            return null;
        }

        private Object nms(org.bukkit.inventory.ItemStack bukkit) {
            if (bukkit == null) {
                return emptyStack();
            }
            try {
                return access.asNmsCopy(bukkit);
            } catch (Throwable t) {
                return emptyStack();
            }
        }

        private void addPlayerRows(Object menu) throws Throwable {
            int addY = 18;
            for (int y = 1; y < 4; y++) {
                for (int x = 0; x < 9; x++) {
                    access.menuAddSlot(menu, gen.newSlot(playerInv, x + y * 9, 8 + x * 18,
                            103 + y * 18 + addY, SlotHandlers.Plain.INSTANCE));
                }
            }
            for (int x = 0; x < 9; x++) {
                access.menuAddSlot(menu, gen.newSlot(playerInv, x, 8 + x * 18, 161 + addY,
                        SlotHandlers.Plain.INSTANCE));
            }
        }

        @Override
        public Object quickMove(Object menu, Object nmsPlayer, int rawIndex) {
            if (own) {
                return emptyStack();
            }
            return quickMoveBetween(menu, rawIndex, 54, true);
        }

        @Override
        Object buildView(Object menu) {
            try {
                org.bukkit.entity.HumanEntity human = spectatorBukkit();
                org.bukkit.inventory.Inventory topInv = topBukkitInventory();
                return gen.newView(human, topInv, menu);
            } catch (Throwable t) {
                return null;
            }
        }

        private org.bukkit.entity.HumanEntity spectatorBukkit() throws Throwable {
            java.lang.reflect.Method getBukkit = spectator.getClass().getMethod("getBukkitEntity");
            return (org.bukkit.entity.HumanEntity) getBukkit.invoke(spectator);
        }

        private org.bukkit.inventory.Inventory topBukkitInventory() throws Throwable {
            Class<?> craftInv = InvSeeAccess.obc("inventory.CraftInventory");
            java.lang.reflect.Constructor<?> c =
                    craftInv.getConstructor(access.containerClass());
            return (org.bukkit.inventory.Inventory) c.newInstance(genContainerRef);
        }

        Object genContainerRef;
    }

    static final class Ender extends Base {
        private final Mirror mirror;
        private final Palette palette;
        private final Object playerInv;
        private final int topRows;
        private final EnderViewContainer view;
        Object genContainerRef;

        Ender(InvSeeClassgen gen, InvSeeMenuRoles roles, EnderViewContainer view, SpectateLog log,
              Object spectator, Object playerInv, Mirror mirror, Palette palette, int topRows) {
            super(gen, roles, view, log, spectator);
            this.view = view;
            this.mirror = mirror;
            this.palette = palette;
            this.playerInv = playerInv;
            this.topRows = topRows;
        }

        void layout(Object menu, Object genContainer) throws Throwable {
            Object filler = filler(palette);
            int size = view.getContainerSize();
            for (int y = 0; y < topRows; y++) {
                for (int x = 0; x < 9; x++) {
                    int position = x + y * 9;
                    int px = 8 + x * 18;
                    int py = 18 + y * 18;
                    int enderSlot = mirror.getEnderSlot(position);
                    if (enderSlot < 0 || enderSlot >= size) {
                        access.menuAddSlot(menu, gen.newSlot(genContainer, position, px, py,
                                new SlotHandlers.Inaccessible(filler, emptyStack())));
                    } else {
                        access.menuAddSlot(menu, gen.newSlot(genContainer, enderSlot, px, py,
                                SlotHandlers.Plain.INSTANCE));
                    }
                }
            }
            int addY = (topRows - 4) * 18;
            for (int y = 1; y < 4; y++) {
                for (int x = 0; x < 9; x++) {
                    access.menuAddSlot(menu, gen.newSlot(playerInv, x + y * 9, 8 + x * 18,
                            103 + y * 18 + addY, SlotHandlers.Plain.INSTANCE));
                }
            }
            for (int x = 0; x < 9; x++) {
                access.menuAddSlot(menu, gen.newSlot(playerInv, x, 8 + x * 18, 161 + addY,
                        SlotHandlers.Plain.INSTANCE));
            }
        }

        @Override
        public Object quickMove(Object menu, Object nmsPlayer, int rawIndex) {
            return quickMoveBetween(menu, rawIndex, topRows * 9, true);
        }

        @Override
        Object buildView(Object menu) {
            try {
                java.lang.reflect.Method getBukkit = spectator.getClass().getMethod("getBukkitEntity");
                org.bukkit.entity.HumanEntity human = (org.bukkit.entity.HumanEntity) getBukkit.invoke(spectator);
                Class<?> craftInv = InvSeeAccess.obc("inventory.CraftInventory");
                java.lang.reflect.Constructor<?> c = craftInv.getConstructor(access.containerClass());
                org.bukkit.inventory.Inventory topInv =
                        (org.bukkit.inventory.Inventory) c.newInstance(genContainerRef);
                return gen.newView(human, topInv, menu);
            } catch (Throwable t) {
                return null;
            }
        }
    }
}
