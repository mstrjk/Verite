package teacommontea.veritesauver.invsee;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.Consumer;


final class SlotRef {

    private final Supplier<Object> getter;
    private final Consumer<Object> setter;

    private SlotRef(Supplier<Object> getter, Consumer<Object> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    static SlotRef of(Supplier<Object> getter, Consumer<Object> setter) {
        return new SlotRef(getter, setter);
    }

    static SlotRef ofList(List<Object> list, int index) {
        return new SlotRef(
            () -> list.get(index),
            item -> list.set(index, item)
        );
    }

    Object get() {
        return getter.get();
    }

    void set(Object item) {
        setter.accept(item);
    }
}
