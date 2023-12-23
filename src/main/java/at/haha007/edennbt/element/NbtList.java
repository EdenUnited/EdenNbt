package at.haha007.edennbt.element;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@ToString
@Getter
@NoArgsConstructor
public class NbtList<T extends NbtElement> extends NbtElement implements List<T> {
    private final List<T> elements = new ArrayList<>();
    private Class<T> clazz = null;

    public NbtList(Class<T> clazz) {
        this.clazz = clazz;
    }

    public NbtList(List<T> elements, Class<T> clazz) {
        this.clazz = clazz;
        addAll(elements);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NbtList<?> other)) {
            return false;
        }
        return elements.equals(other.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    public ElementType getSubType() {
        return ElementType.getType(clazz);
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
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    @Override
    @NotNull
    public Iterator<T> iterator() {
        return elements.iterator();
    }

    @Override
    public Object @NotNull [] toArray() {
        return elements.toArray();
    }

    @Override
    @NotNull
    public <T1> T1 @NotNull [] toArray(T1 @NotNull [] a) {
        return elements.toArray(a);
    }

    @Override
    public boolean add(T o) {
        checkType(o);
        return elements.add(o);
    }

    @Override
    public boolean remove(Object o) {
        return elements.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        //noinspection SlowListContainsAll
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T t : c) {
            add(t);
            changed = true;
        }
        return changed;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean changed = false;
        for (T t : c) {
            add(t);
            changed = true;
        }
        return changed;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return elements.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return elements.retainAll(c);
    }

    @Override
    public void clear() {
        elements.clear();
    }

    @Override
    public T get(int index) {
        return elements.get(index);
    }

    @Override
    public T set(int index, T element) {
        checkType(element);
        return elements.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        checkType(element);
        elements.add(index, element);
    }

    @Override
    public T remove(int index) {
        return elements.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return elements.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return elements.lastIndexOf(o);
    }

    @Override
    @NotNull
    public ListIterator<T> listIterator() {
        return elements.listIterator();
    }

    @Override
    @NotNull
    public ListIterator<T> listIterator(int index) {
        return elements.listIterator(index);
    }

    @Override
    @NotNull
    public List<T> subList(int fromIndex, int toIndex) {
        return elements.subList(fromIndex, toIndex);
    }

    private void checkType(Object o) {
        if (o == null)
            throw new NullPointerException("NbtList can only handle non-null elements.");
        if (!(o instanceof NbtElement))
            throw new IllegalArgumentException("NbtList can only handle NbtElements. Provided: "
                    + o.getClass().getCanonicalName());
        if (clazz == null) {
            //noinspection unchecked
            clazz = (Class<T>) o.getClass();
            return;
        }
        if (o.getClass() != clazz)
            throw new ClassCastException("NbtList can only handle elements of type " + clazz.getCanonicalName() +
                    ". Provided: " + o.getClass().getCanonicalName());

    }
}
