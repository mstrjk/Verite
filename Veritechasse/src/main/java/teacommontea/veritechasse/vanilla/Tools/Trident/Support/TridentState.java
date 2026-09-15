package teacommontea.veritechasse.vanilla.Tools.Trident.Support;

public final class TridentState {

    private float throwPower;
    private float spinAttackStrength;
    private int spinAttackTicks;
    private double returnAcceleration;
    private float bonusDamage;
    private boolean summonsLightning;

    public TridentState(float throwPower) {
        this.throwPower = throwPower;
        this.spinAttackStrength = 0.0F;
        this.spinAttackTicks = 0;
        this.returnAcceleration = 0.0D;
        this.bonusDamage = 0.0F;
        this.summonsLightning = false;
    }

    public float throwPower() {
        return this.throwPower;
    }

    public void throwPower(float value) {
        this.throwPower = value;
    }

    public void addThrowPower(float amount) {
        this.throwPower += amount;
    }

    public float spinAttackStrength() {
        return this.spinAttackStrength;
    }

    public void addSpinAttackStrength(float amount) {
        this.spinAttackStrength += amount;
    }

    public int spinAttackTicks() {
        return this.spinAttackTicks;
    }

    public void spinAttackTicks(int ticks) {
        this.spinAttackTicks = ticks;
    }

    public double returnAcceleration() {
        return this.returnAcceleration;
    }

    public void addReturnAcceleration(double amount) {
        this.returnAcceleration += amount;
    }

    public float bonusDamage() {
        return this.bonusDamage;
    }

    public void addBonusDamage(float amount) {
        this.bonusDamage += amount;
    }

    public boolean summonsLightning() {
        return this.summonsLightning;
    }

    public void summonsLightning(boolean value) {
        this.summonsLightning = value;
    }

    public boolean propelsHolder() {
        return this.spinAttackStrength > 0.0F;
    }
}
