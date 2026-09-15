package teacommontea.veritechasse.vanilla.Potions.Support;

import java.util.List;

public final class AttributePipeline {

    private AttributePipeline() {
    }

    public static double resolve(Ranged attribute, List<Modifier> modifiers) {
        double base = attribute.base();

        for (Modifier modifier : modifiers) {
            if (modifier.operation() == Operation.ADD_VALUE) {
                base = base + modifier.amount();
            }
        }

        double total = base;

        for (Modifier modifier : modifiers) {
            if (modifier.operation() == Operation.ADD_MULTIPLIED_BASE) {
                total = total + base * modifier.amount();
            }
        }

        for (Modifier modifier : modifiers) {
            if (modifier.operation() == Operation.ADD_MULTIPLIED_TOTAL) {
                total = total * (1.0D + modifier.amount());
            }
        }

        return attribute.sanitize(total);
    }

    public static double resolve(Ranged attribute, Modifier modifier) {
        return resolve(attribute, List.of(modifier));
    }
}
