package teacommontea.api;


public enum FilterResult {

    CLEAN,
    BLOCK,
    SELF_HARM,
    ABUSE,
    PROFANITY;

    public boolean blocks() {
        return this != CLEAN;
    }
}
