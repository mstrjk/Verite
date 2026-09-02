package teacommontea.veritesauver.invsee;


public enum MainSlot {

    STORAGE_00(0), STORAGE_01(1), STORAGE_02(2), STORAGE_03(3), STORAGE_04(4),
    STORAGE_05(5), STORAGE_06(6), STORAGE_07(7), STORAGE_08(8), STORAGE_09(9),
    STORAGE_10(10), STORAGE_11(11), STORAGE_12(12), STORAGE_13(13), STORAGE_14(14),
    STORAGE_15(15), STORAGE_16(16), STORAGE_17(17), STORAGE_18(18), STORAGE_19(19),
    STORAGE_20(20), STORAGE_21(21), STORAGE_22(22), STORAGE_23(23), STORAGE_24(24),
    STORAGE_25(25), STORAGE_26(26), STORAGE_27(27), STORAGE_28(28), STORAGE_29(29),
    STORAGE_30(30), STORAGE_31(31), STORAGE_32(32), STORAGE_33(33), STORAGE_34(34),
    STORAGE_35(35),

    ARMOUR_BOOTS(36), ARMOUR_LEGGINGS(37), ARMOUR_CHESTPLATE(38), ARMOUR_HELMET(39),
    OFFHAND(40), BODY(41), SADDLE(42), CURSOR(43),

    PERSONAL_00(45), PERSONAL_01(46), PERSONAL_02(47), PERSONAL_03(48), PERSONAL_04(49),
    PERSONAL_05(50), PERSONAL_06(51), PERSONAL_07(52), PERSONAL_08(53);

    public static final int GRID_SIZE = 54;

    private final int defaultIndex;

    MainSlot(int defaultIndex) {
        this.defaultIndex = defaultIndex;
    }

    public int defaultIndex() {
        return defaultIndex;
    }

    public boolean isStorage() {
        return ordinal() <= STORAGE_35.ordinal();
    }

    public boolean isPersonal() {
        return ordinal() >= PERSONAL_00.ordinal();
    }

    public int personalIndex() {
        return isPersonal() ? ordinal() - PERSONAL_00.ordinal() : -1;
    }

    public int storageIndex() {
        return isStorage() ? ordinal() : -1;
    }

    public static MainSlot byDefaultIndex(int index) {
        for (MainSlot slot : values()) {
            if (slot.defaultIndex == index) {
                return slot;
            }
        }
        return null;
    }
}
