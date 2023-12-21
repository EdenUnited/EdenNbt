package at.haha007.edennbt.parser;

import at.haha007.edennbt.element.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


class IOStreamParserTest {

    @Test
    void testWriteStringListCompound() throws IOException {
        //0A 00 00 09 00 04 74 65 73 74 08 00 00 00 01 00 04 74 65 73 74 00
        NbtCompound compound = new NbtCompound();
        compound.put("test", new NbtList<>(List.of(new NbtString("test")), NbtString.class));
        System.out.println(compound);
        IOStreamParser parser = new IOStreamParser();
        ByteArrayOutputStream bos = (ByteArrayOutputStream) parser.write(compound);
        byte[] bytes = bos.toByteArray();
        bos.close();
        Assertions.assertEquals("0A 00 00 09 00 04 74 65 73 74 08 00 00 00 01 00 04 74 65 73 74 00", toHexString(bytes));
    }

    @Test
    void testReadStringListCompound() {
        //0A 00 00 09 00 04 74 65 73 74 08 00 00 00 01 00 04 74 65 73 74 00
        String hexString = "0A 00 00 09 00 04 74 65 73 74 08 00 00 00 01 00 04 74 65 73 74 00";
        byte[] bytes = fromHexString(hexString);
        IOStreamParser parser = new IOStreamParser();
        NbtCompound compound = (NbtCompound) parser.read(new ByteArrayInputStream(bytes));
        System.out.println(compound);

        NbtCompound pure = new NbtCompound();
        pure.put("test", new NbtList<>(List.of(new NbtString("test")), NbtString.class));

        Assertions.assertEquals(pure, compound);
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

        IOStreamParser parser = new IOStreamParser();
        //noinspection resource
        byte[] bytes = ((ByteArrayOutputStream) parser.write(list)).toByteArray();
        Assertions.assertEquals(list, parser.read(new ByteArrayInputStream(bytes)));
    }

    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    private byte[] fromHexString(String hexString) {
        String[] hexChars = hexString.split(" ");
        byte[] bytes = new byte[hexChars.length];
        for (int i = 0; i < hexChars.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexChars[i], 16);
        }
        return bytes;
    }
}