package at.haha007.edennbt.parser;

import at.haha007.edennbt.element.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class SnbtParserTest {
    @Test
    void testRead() {
        Assertions.assertEquals(new NbtCompound(Map.of("test", new NbtString("value"))),
                new SnbtParser().read("{test:value}"));
        Assertions.assertEquals(new NbtCompound(Map.of("test", new NbtString("value"))),
                new SnbtParser().read("{\"test\":\"value\"}"));
        Assertions.assertEquals(new NbtShort((short) 1), new SnbtParser().read("1s"));
        Assertions.assertEquals(new NbtCompound(Map.of("test", new NbtShort((short) 1))), new SnbtParser().read("{test:1s}"));
    }

    @Test
    void testWrite() {
        Assertions.assertEquals("{test:value}",
                new SnbtParser().write(new NbtCompound(Map.of("test", new NbtString("value")))));
    }

    @Test
    void testBigRead() {
        String snbt = "[{a:[B;1]},{b:2s},{c:3.0}]";
        NbtList<NbtCompound> list = new NbtList<>();
        NbtCompound compound = new NbtCompound(Map.of(
                "a", new NbtByteArray(new byte[]{1})
        ));
        list.add(compound);
        compound = new NbtCompound(Map.of(
                "b", new NbtShort((short) 2)
        ));
        list.add(compound);
        compound = new NbtCompound(Map.of(
                "c", new NbtDouble(3.0)
        ));
        list.add(compound);
        Assertions.assertEquals(list, new SnbtParser().read(snbt));
    }

    @Test
    void testBigWrite() {
        String snbt = "[{a:[B;1]},{b:2s}]";
        NbtList<NbtCompound> list = new NbtList<>();
        NbtCompound compound = new NbtCompound(Map.of(
                "a", new NbtByteArray(new byte[]{1})
        ));
        list.add(compound);
        compound = new NbtCompound(Map.of(
                "b", new NbtShort((short) 2)
        ));
        list.add(compound);
        Assertions.assertEquals(snbt, new SnbtParser().write(list));
    }

    @Test
    void testWriteReadAll() {
        NbtList<NbtCompound> list = new NbtList<>(NbtCompound.class);
        NbtCompound compound = new NbtCompound();
        compound.put("byte", new NbtByte((byte) 42));
        compound.put("string", new NbtString("test"));
        compound.put("compound", new NbtCompound());
        compound.put("short", new NbtShort((short) 42));
        compound.put("int", new NbtInt(42));
        compound.put("long", new NbtLong(42));
        compound.put("float", new NbtFloat(42));
        compound.put("double", new NbtDouble(42));
        compound.put("bytes", new NbtByteArray(new byte[]{1, 2, 3}));
        compound.put("intarray", new NbtIntArray(new int[]{1, 2, 3}));
        compound.put("longarray", new NbtLongArray(new long[]{1, 2, 3}));
        list.add(compound);

        SnbtParser parser = new SnbtParser();
        String snbt = parser.write(list);
        System.out.println(snbt);
        Assertions.assertEquals(list, parser.read(snbt));
    }
}
