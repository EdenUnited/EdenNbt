package at.haha007.edennbt.parser;

import at.haha007.edennbt.element.NbtElement;

public interface NbtParser<T, U> {
    NbtElement read(U input);

    T write(NbtElement input);
}
