package at.haha007.edennbt.element;

public abstract class NbtElement {

    public final ElementType getType() {
        return ElementType.getType(this.getClass());
    }
}
