package teacommontea.skript.elements;

import teacommontea.api.VeriteCaptcha;
import teacommontea.skript.VeritePlayerCondition;

public class CondVeriteCaptcha extends VeritePlayerCondition {

    static {
        register(CondVeriteCaptcha.class, "in a captcha", "players");
    }

    public CondVeriteCaptcha() {
        super(VeriteCaptcha::isActive, "in a captcha");
    }
}
