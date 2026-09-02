package teacommontea.veritedoux.postprocess;


public final class SelfHarmMessages {

    private SelfHarmMessages() {}

    private static final String ENGLISH =
            "Your message wasn't sent. It may contain content related to suicide or self-harm. If this is a mistake, you can rephrase your message and try again.\\n\\nIf you're talking about yourself or someone else and this concern is real, you don't have to go through it alone. Consider reaching out to someone you trust or contacting a local crisis service for support. If you believe there is an immediate risk of harm, contact your local emergency services.\\n\\nYou can find crisis resources for your country here:\\nhttps://findahelpline.com/";

    public static String message() {
        return ENGLISH;
    }
}
