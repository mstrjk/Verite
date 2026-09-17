package teacommontea.veritechasse.Reality;

import java.util.List;

public interface Check {

    double DEFAULT_MAX_BUFFER = 6.0D;

    double DEFAULT_DECAY = 0.5D;

    String key();

    String title();

    String description();

    String engine(CheckContext context);

    String handwritten(CheckContext context);

    default double maxBuffer() {
        return DEFAULT_MAX_BUFFER;
    }

    default double decay() {
        return DEFAULT_DECAY;
    }

    default String engineBufferKey() {
        return key() + ".engine";
    }

    default String handwrittenBufferKey() {
        return key() + ".handwritten";
    }

    default void evaluate(CheckContext context, List<Violation> violations) {
        String engineDetail = engine(context);
        String handwrittenDetail = handwritten(context);

        boolean engineCrossed = accrue(context, engineBufferKey(), engineDetail);
        boolean handwrittenCrossed = accrue(
            context, handwrittenBufferKey(), handwrittenDetail);

        if (engineCrossed && handwrittenCrossed) {
            violations.add(Violation.handwritten(this, handwrittenDetail)
                .agreedWith(Violation.engine(this, engineDetail)));
            return;
        }
        if (engineCrossed) {
            violations.add(Violation.engine(this, engineDetail));
        }
        if (handwrittenCrossed) {
            violations.add(Violation.handwritten(this, handwrittenDetail));
        }
    }

    private boolean accrue(CheckContext context, String bufferKey, String detail) {
        PlayerState state = context.state();
        if (detail == null) {
            state.decayBuffer(bufferKey, decay());
            return false;
        }
        if (state.increaseBuffer(bufferKey, 1.0D) <= maxBuffer()) {
            return false;
        }
        state.clearBuffer(bufferKey);
        return true;
    }
}
