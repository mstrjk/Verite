package teacommontea.util.sched;


public interface TaskHandle {

    void cancel();

    TaskHandle NONE = () -> { };
}
