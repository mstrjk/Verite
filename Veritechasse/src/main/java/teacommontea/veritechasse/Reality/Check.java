package teacommontea.veritechasse.Reality;

import java.util.List;

public interface Check {

    String key();

    void evaluate(CheckContext context, List<Observation> observations);
}
