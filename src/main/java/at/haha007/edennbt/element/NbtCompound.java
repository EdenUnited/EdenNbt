package at.haha007.edennbt.element;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@Getter
@ToString
public class NbtCompound extends NbtElement implements Map<String, NbtElement> {
    private final Map<String, NbtElement> elements = new LinkedHashMap<>();

    public NbtCompound(Map<String, NbtElement> elements) {
        this.elements.putAll(elements);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NbtCompound other)) {
            return false;
        }
        if (other.elements.size() != elements.size()) {
            return false;
        }
        for (Map.Entry<String, NbtElement> entry : elements.entrySet()) {
            if (!entry.getValue().equals(other.elements.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return elements.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return elements.containsValue(value);
    }

    @Override
    public NbtElement get(Object key) {
        return elements.get(key);
    }

    @Nullable
    @Override
    public NbtElement put(String key, NbtElement value) {
        return elements.put(key, value);
    }

    @Override
    public NbtElement remove(Object key) {
        return elements.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends NbtElement> m) {
        elements.putAll(m);
    }

    @Override
    public void clear() {
        elements.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return elements.keySet();
    }

    @NotNull
    @Override
    public Collection<NbtElement> values() {
        return elements.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, NbtElement>> entrySet() {
        return elements.entrySet();
    }
}
