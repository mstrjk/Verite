package teacommontea.veritedoux.intercept;

import org.bukkit.Bukkit;

import java.lang.reflect.Method;

final class ComponentDecoders {

    static NmsAccess.ComponentDecoder resolve() throws NmsAccess.Unsupported {
        NmsAccess.ComponentDecoder d;
        if ((d = craftChatMessage()) != null) return d;
        if ((d = componentGetString()) != null) return d;
        if ((d = paperAdventure()) != null) return d;
        throw new NmsAccess.Unsupported("no Component->text decoder found on this server");
    }

    private static NmsAccess.ComponentDecoder craftChatMessage() {
        try {
            String base = Bukkit.getServer().getClass().getPackage().getName();
            Class<?> ccm;
            try {
                ccm = Class.forName(base + ".util.CraftChatMessage");
            } catch (ClassNotFoundException e) {
                ccm = Class.forName("org.bukkit.craftbukkit.util.CraftChatMessage");
            }
            Class<?> comp = componentClass();
            if (comp == null) return null;
            Method from = ccm.getMethod("fromComponent", comp);
            return component -> (String) from.invoke(null, component);
        } catch (Throwable t) {
            return null;
        }
    }

    private static NmsAccess.ComponentDecoder componentGetString() {
        try {
            Class<?> comp = componentClass();
            if (comp == null) return null;
            Method getString = comp.getMethod("getString");
            return component -> (String) getString.invoke(component);
        } catch (Throwable t) {
            return null;
        }
    }

    private static NmsAccess.ComponentDecoder paperAdventure() {
        try {
            Class<?> pa = Class.forName("io.papermc.paper.adventure.PaperAdventure");
            Class<?> comp = componentClass();
            if (comp == null) return null;
            Method asAdv = pa.getMethod("asAdventure", comp);
            Class<?> plainSer = Class.forName(
                    "net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer");
            Object serializer = plainSer.getMethod("plainText").invoke(null);
            Class<?> advComp = Class.forName("net.kyori.adventure.text.Component");
            Method serialize = plainSer.getMethod("serialize", advComp);
            return component -> {
                Object adv = asAdv.invoke(null, component);
                return (String) serialize.invoke(serializer, adv);
            };
        } catch (Throwable t) {
            return null;
        }
    }

    private static Class<?> componentClass() {
        try { return Class.forName("net.minecraft.network.chat.Component"); }
        catch (Throwable a) {
            try { return Class.forName("net.minecraft.network.chat.IChatBaseComponent"); }
            catch (Throwable b) { return null; }
        }
    }

    private ComponentDecoders() {}
}
