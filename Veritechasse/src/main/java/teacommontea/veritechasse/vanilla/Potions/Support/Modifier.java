package teacommontea.veritechasse.vanilla.Potions.Support;

public final class Modifier {

    private final double amount;
    private final Operation operation;

    public Modifier(double amount, Operation operation) {
        this.amount = amount;
        this.operation = operation;
    }

    public double amount() {
        return this.amount;
    }

    public Operation operation() {
        return this.operation;
    }

    public double scaledFor(int amplifier) {
        return this.amount * (double) (amplifier + 1);
    }

    public Modifier at(int amplifier) {
        return new Modifier(this.scaledFor(amplifier), this.operation);
    }
}
