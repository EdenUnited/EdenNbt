package at.haha007.edennbt.element;

import java.util.Arrays;

public enum ElementType {
    END(NbtEnd.class),
    BYTE(NbtByte.class),
    SHORT(NbtShort.class),
    INT(NbtInt.class),
    LONG(NbtLong.class),
    FLOAT(NbtFloat.class),
    DOUBLE(NbtDouble.class),
    BYTE_ARRAY(NbtByteArray.class),
    STRING(NbtString.class),
    LIST(NbtList.class),
    COMPOUND(NbtCompound.class),
    INT_ARRAY(NbtIntArray.class),
    LONG_ARRAY(NbtLongArray.class);
    private final Class<? extends NbtElement> clazz;

    ElementType(Class<? extends NbtElement> clazz) {
        this.clazz = clazz;
    }

    public static ElementType getType(Class<? extends NbtElement> clazz) {
        return Arrays.stream(values()).filter(e -> e.getNbtClass() == clazz).findFirst().orElse(null);
    }

    public Class<? extends NbtElement> getNbtClass() {
        return clazz;
    }


}
