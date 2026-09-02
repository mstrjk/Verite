package teacommontea.api;


public final class FilterOutcome {

    private final FilterResult category;
    private final boolean repeat;

    FilterOutcome(FilterResult category, boolean repeat) {
        this.category = category == null ? FilterResult.CLEAN : category;
        this.repeat = repeat;
    }

    public FilterResult category() {
        return category;
    }

    public boolean repeat() {
        return repeat;
    }

    public boolean blocks() {
        return category.blocks();
    }
}
