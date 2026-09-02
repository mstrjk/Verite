package teacommontea.veritesauver.punish;

import teacommontea.util.Colours;
import teacommontea.veritesauver.util.SauverMessages;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.core.SauverEngine;
import teacommontea.veritesauver.util.SauverDuration;

public final class PunishDialog {

    private final Sauver sauver;

    public PunishDialog(Sauver sauver) {
        this.sauver = sauver;
    }

    public boolean open(Player staff, UUID target, String targetName, String category) {
        try {
            ClassLoader cl = staff.getClass().getClassLoader();

            Class<?> dialogInput = load(cl, "io.papermc.paper.registry.data.dialog.input.DialogInput");
            Class<?> optionEntryCls = load(cl, "io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput$OptionEntry");
            Class<?> dialogBody = load(cl, "io.papermc.paper.registry.data.dialog.body.DialogBody");
            Class<?> dialogBase = load(cl, "io.papermc.paper.registry.data.dialog.DialogBase");
            Class<?> actionButton = load(cl, "io.papermc.paper.registry.data.dialog.ActionButton");
            Class<?> dialogAction = load(cl, "io.papermc.paper.registry.data.dialog.action.DialogAction");
            Class<?> dialogActionCallback = load(cl, "io.papermc.paper.registry.data.dialog.action.DialogActionCallback");
            Class<?> dialogType = load(cl, "io.papermc.paper.registry.data.dialog.type.DialogType");
            Class<?> dialogCls = load(cl, "io.papermc.paper.dialog.Dialog");
            Class<?> clickOptions = load(cl, "net.kyori.adventure.text.event.ClickCallback$Options");
            Class<?> componentCls = load(cl, "net.kyori.adventure.text.Component");
            Class<?> factoryCls = load(cl, "io.papermc.paper.registry.RegistryBuilderFactory");
            Class<?> entryBuilderCls = load(cl, "io.papermc.paper.registry.data.dialog.DialogRegistryEntry$Builder");

            List<Object> bodies = new ArrayList<>();
            bodies.add(plainMessage(dialogBody, componentCls,
                    comp(Colours.BRAND + "Actor: " + Colours.BRAND_ACCENT_SECONDARY + staff.getName())));
            bodies.add(plainMessage(dialogBody, componentCls,
                    comp(Colours.BRAND + "Target: " + Colours.BRAND_ACCENT_SECONDARY + targetName)));
            if (category != null && !category.isBlank()) {
                bodies.add(plainMessage(dialogBody, componentCls,
                        comp(Colours.BRAND + "Issue: " + Colours.BRAND_ACCENT_SECONDARY + pretty(category))));
            }

            List<Object> inputs = new ArrayList<>();
            inputs.add(singleOption(dialogInput, optionEntryCls, componentCls, "punishment",
                    comp(Colours.BRAND_ACCENT_SECONDARY + "Punishment"), List.of(
                            new String[]{"warn", "Warn"},
                            new String[]{"mute", "Mute"},
                            new String[]{"permmute", "Perma-mute"},
                            new String[]{"kick", "Kick"},
                            new String[]{"ban", "Ban"},
                            new String[]{"permban", "Perma-ban"})));
            inputs.add(text(dialogInput, componentCls, "duration",
                    comp(Colours.BRAND_ACCENT_SECONDARY + "Duration " + Colours.BRAND_ACCENT + "(blank or Perma = permanent)")));
            inputs.add(text(dialogInput, componentCls, "reason", comp(Colours.BRAND_ACCENT_SECONDARY + "Reason")));

            Class<?> afterActionCls = load(cl, "io.papermc.paper.registry.data.dialog.DialogBase$DialogAfterAction");
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object afterClose = Enum.valueOf((Class<? extends Enum>) afterActionCls, "CLOSE");
            Object base = dialogBase.getMethod("create",
                            componentCls, componentCls, boolean.class, boolean.class,
                            afterActionCls, List.class, List.class)
                    .invoke(null, comp(Colours.BRAND_ACCENT_SECONDARY + "Punish " + targetName), null, true, true, afterClose, bodies, inputs);

            Object callback = Proxy.newProxyInstance(cl, new Class<?>[]{dialogActionCallback},
                    (proxy, method, args) -> {
                        if (method.getName().equals("accept") && args != null && args.length >= 1) {
                            handle(staff, target, targetName, category, args[0]);
                        }
                        return null;
                    });

            Class<?> optionsBuilder = load(cl, "net.kyori.adventure.text.event.ClickCallback$Options$Builder");
            Object optsBuilder = clickOptions.getMethod("builder").invoke(null);
            Object options = optionsBuilder.getMethod("build").invoke(optsBuilder);
            Object action = dialogAction.getMethod("customClick", dialogActionCallback, clickOptions)
                    .invoke(null, callback, options);

            Object confirm = actionButton.getMethod("create", componentCls, componentCls, int.class, dialogAction)
                    .invoke(null, comp(Colours.SUCCESS + "Confirm"), null, 150, action);
            Object cancel = actionButton.getMethod("create", componentCls, componentCls, int.class, dialogAction)
                    .invoke(null, comp(Colours.WARNING + "Cancel"), null, 150, null);

            Object type = dialogType.getMethod("confirmation", actionButton, actionButton)
                    .invoke(null, confirm, cancel);

            java.util.function.Consumer<Object> configure = factory -> {
                try {
                    Object builder = factoryCls.getMethod("empty").invoke(factory);
                    entryBuilderCls.getMethod("base", dialogBase).invoke(builder, base);
                    entryBuilderCls.getMethod("type", dialogType).invoke(builder, type);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            };
            Object dialog = dialogCls.getMethod("create", java.util.function.Consumer.class)
                    .invoke(null, configure);

            Method showDialog = staff.getClass().getMethod("showDialog", load(cl, "net.kyori.adventure.dialog.DialogLike"));
            showDialog.invoke(staff, dialog);
            return true;
        } catch (Throwable t) {
            Throwable cause = t instanceof java.lang.reflect.InvocationTargetException ite && ite.getCause() != null
                    ? ite.getCause() : t;
            sauver.plugin().getLogger().log(java.util.logging.Level.WARNING,
                    "[Veritesauver] punishment dialog failed to open; falling back to chat prompt", cause);
            return false;
        }
    }

    private void handle(Player staff, UUID target, String targetName, String category, Object responseView) {
        try {
            Method getText = responseView.getClass().getMethod("getText", String.class);
            String punishment = str(getText.invoke(responseView, "punishment"));
            String duration = str(getText.invoke(responseView, "duration"));
            String reason = str(getText.invoke(responseView, "reason"));
            if (punishment == null || punishment.isEmpty()) {
                return;
            }
            String finalReason = (reason == null || reason.isBlank())
                    ? (category == null || category.isBlank() ? "Manual punishment" : pretty(category))
                    : reason;
            String verb = punishment.toLowerCase(java.util.Locale.ROOT);
            boolean forcePermanent = verb.startsWith("perm");
            long millis = Entry.PERMANENT;
            if (!forcePermanent && duration != null && !duration.isBlank()) {
                String d = duration.trim();
                if (!d.equalsIgnoreCase("perma") && !d.equalsIgnoreCase("permanent")) {
                    long parsed = SauverDuration.parse(d);
                    if (parsed > 0) {
                        millis = parsed;
                    }
                }
            }
            UUID execUuid = staff.getUniqueId();
            String execName = staff.getName();
            Entry.Type checkType = switch (verb) {
                case "warn" -> Entry.Type.WARNING;
                case "kick" -> Entry.Type.KICK;
                case "mute", "permmute" -> Entry.Type.MUTE;
                case "ban", "permban" -> Entry.Type.BAN;
                default -> null;
            };
            if (checkType != null) {
                String block = SauverExempt.blockReason(staff, target, checkType);
                if (block != null) {
                    sauver.messages().err(staff, Colours.WARNING + block);
                    return;
                }
            }
            SauverEngine.Result r = switch (verb) {
                case "warn" -> SauverEngine.warn(target, targetName, finalReason, execUuid, execName, false);
                case "kick" -> SauverEngine.kick(target, targetName, finalReason, execUuid, execName, false);
                case "mute", "permmute" -> SauverEngine.issue(Entry.Type.MUTE, target, null, targetName, finalReason,
                        execUuid, execName, millis, false, false);
                case "ban", "permban" -> SauverEngine.issue(Entry.Type.BAN, target, null, targetName, finalReason,
                        execUuid, execName, millis, false, false);
                default -> null;
            };
            if (r != null && !r.ok()) {
                sauver.messages().err(staff, Colours.WARNING + r.error());
            } else if (r != null) {
                sauver.messages().send(staff, Colours.BRAND + "Punished " + Colours.BRAND_ACCENT_SECONDARY + targetName
                        + " " + Colours.BRAND_ACCENT + "(" + Colours.WARNING + punishment + Colours.BRAND_ACCENT + ")");
            }
        } catch (Throwable t) {
            Throwable cause = t instanceof java.lang.reflect.InvocationTargetException ite && ite.getCause() != null
                    ? ite.getCause() : t;
            sauver.plugin().getLogger().log(java.util.logging.Level.WARNING,
                    "[Veritesauver] punishment dialog response handling failed", cause);
            sauver.messages().err(staff, Colours.WARNING + "That punishment could not be applied.");
        }
    }

    private Object plainMessage(Class<?> dialogBody, Class<?> componentCls, Object message) throws Exception {
        return dialogBody.getMethod("plainMessage", componentCls).invoke(null, message);
    }

    private Object text(Class<?> dialogInput, Class<?> componentCls, String key, Object label) throws Exception {
        Object builder = dialogInput.getMethod("text", String.class, componentCls).invoke(null, key, label);
        callVia(builder, "width", new Class<?>[]{int.class}, 200);
        callVia(builder, "maxLength", new Class<?>[]{int.class}, 128);
        return callVia(builder, "build", new Class<?>[]{});
    }

    private Object singleOption(Class<?> dialogInput, Class<?> optionEntryCls, Class<?> componentCls,
                               String key, Object label, List<String[]> entries) throws Exception {
        List<Object> opts = new ArrayList<>();
        boolean first = true;
        for (String[] e : entries) {
            Object entry = optionEntryCls.getMethod("create", String.class, componentCls, boolean.class)
                    .invoke(null, e[0], comp(Colours.BRAND_ACCENT_SECONDARY + e[1]), first);
            opts.add(entry);
            first = false;
        }
        Object builder = dialogInput.getMethod("singleOption", String.class, componentCls, List.class)
                .invoke(null, key, label, opts);
        return callVia(builder, "build", new Class<?>[]{});
    }

    private static Object callVia(Object target, String name, Class<?>[] paramTypes, Object... args) throws Exception {
        Method m = accessibleMethod(target.getClass(), name, paramTypes);
        if (m == null) {
            throw new NoSuchMethodException(name + " on " + target.getClass().getName());
        }
        return m.invoke(target, args);
    }

    private static Method accessibleMethod(Class<?> impl, String name, Class<?>[] paramTypes) {
        for (Class<?> iface : allInterfaces(impl)) {
            try {
                return iface.getMethod(name, paramTypes);
            } catch (NoSuchMethodException ignored) {
            }
        }
        try {
            return impl.getMethod(name, paramTypes);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static List<Class<?>> allInterfaces(Class<?> start) {
        List<Class<?>> out = new ArrayList<>();
        for (Class<?> c = start; c != null && c != Object.class; c = c.getSuperclass()) {
            collectInterfaces(c, out);
        }
        return out;
    }

    private static void collectInterfaces(Class<?> c, List<Class<?>> out) {
        for (Class<?> i : c.getInterfaces()) {
            if (!out.contains(i)) {
                out.add(i);
                collectInterfaces(i, out);
            }
        }
    }

    private static Object comp(String mini) {
        return SauverMessages.screen(mini);
    }

    private static Class<?> load(ClassLoader cl, String name) throws ClassNotFoundException {
        return Class.forName(name, true, cl);
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    private static String pretty(String category) {
        String base = category.replace('.', ' ').replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (String w : base.split(" ")) {
            if (w.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }
}
