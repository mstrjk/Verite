package teacommontea.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


import teacommontea.util.Scope;

public final class VeriteScope {

    private final Scope scope;

    public VeriteScope(Scope scope) {
        this.scope = scope;
    }

    public void set(String key, Object value) {
        scope.set(key, value);
    }

    public void delete(String key) {
        scope.delete(key);
    }

    public boolean has(String key) {
        return scope.has(key);
    }

    public Object get(String key) {
        return scope.get(key);
    }

    public String getString(String key, String def) {
        return scope.getString(key, def);
    }

    public long getLong(String key, long def) {
        return scope.getLong(key, def);
    }

    public int getInt(String key, int def) {
        return scope.getInt(key, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return scope.getBoolean(key, def);
    }

    public BigDecimal getBigDecimal(String key) {
        return scope.getBigDecimal(key);
    }

    public List<ScopeEntry> entries() {
        List<Scope.Entry> raw = scope.entries();
        List<ScopeEntry> out = new ArrayList<>(raw.size());
        for (Scope.Entry e : raw) {
            out.add(new ScopeEntry(e.key(), e.value()));
        }
        return out;
    }
}
