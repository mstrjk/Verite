package teacommontea.veritesauver.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ClientProfile {

    private final UUID player;
    private final List<Signal> signals = new CopyOnWriteArrayList<>();
    private final Map<String, String> facts = new ConcurrentHashMap<>();

    private volatile String reportedBrand;
    private volatile String identity;
    private volatile int confidence;

    ClientProfile(UUID player) {
        this.player = player;
    }

    public UUID player() {
        return player;
    }

    public String reportedBrand() {
        return reportedBrand;
    }

    void reportedBrand(String brand) {
        this.reportedBrand = brand;
    }

    public String identity() {
        return identity;
    }

    public int confidence() {
        return confidence;
    }

    void conclude(String identity, int confidence) {
        this.identity = identity;
        this.confidence = confidence;
    }

    public void add(Signal signal) {
        if (signal == null) {
            return;
        }
        signals.add(signal);
    }

    public void addAll(Collection<Signal> found) {
        for (Signal s : found) {
            add(s);
        }
    }

    public void fact(String key, String value) {
        if (key == null || value == null || value.isBlank()) {
            return;
        }
        facts.put(key, value);
    }

    public Map<String, String> facts() {
        return new LinkedHashMap<>(facts);
    }

    public List<Signal> signals() {
        return new ArrayList<>(signals);
    }

    public boolean has(Signal.Source source) {
        for (Signal s : signals) {
            if (s.source() == source) {
                return true;
            }
        }
        return false;
    }
}
