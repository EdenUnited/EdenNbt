package at.haha007.edennbt.element;

import at.haha007.edennbt.parser.SnbtParser;

public abstract class NbtElement {

    public final ElementType getType() {
        return ElementType.getType(this.getClass());
    }

    @Override
    public String toString() {
        return new SnbtParser().write(this);
    }
}
