package at.haha007.edennbt.parser;

import at.haha007.edennbt.element.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

/**
 * @author Haha007
 * Binary structure always: type,name,value
 * In list entries type and name are inferred
 * <br>
 * type byte
 * |  empty string as key
 * |  |     value
 * |  |     |
 * byte: 01 00 00 17
 */
public class IOStreamParser implements NbtParser<OutputStream, InputStream> {
    private static final Map<ElementType, Byte> TYPE_HEADERS = new EnumMap<>(ElementType.class);

    public static ElementType getType(byte header) {
        return TYPE_HEADERS.entrySet().stream()
                .filter(e -> e.getValue().equals(header))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalArgumentException("Invalid header: " + header));
    }

    static {
        TYPE_HEADERS.put(ElementType.END, (byte) 0);
        TYPE_HEADERS.put(ElementType.BYTE, (byte) 1);
        TYPE_HEADERS.put(ElementType.SHORT, (byte) 2);
        TYPE_HEADERS.put(ElementType.INT, (byte) 3);
        TYPE_HEADERS.put(ElementType.LONG, (byte) 4);
        TYPE_HEADERS.put(ElementType.FLOAT, (byte) 5);
        TYPE_HEADERS.put(ElementType.DOUBLE, (byte) 6);
        TYPE_HEADERS.put(ElementType.BYTE_ARRAY, (byte) 7);
        TYPE_HEADERS.put(ElementType.STRING, (byte) 8);
        TYPE_HEADERS.put(ElementType.LIST, (byte) 9);
        TYPE_HEADERS.put(ElementType.COMPOUND, (byte) 10);
        TYPE_HEADERS.put(ElementType.INT_ARRAY, (byte) 11);
        TYPE_HEADERS.put(ElementType.LONG_ARRAY, (byte) 12);
    }

    @SneakyThrows
    @Override
    public NbtElement read(InputStream input) {
        return new NbtReader(input).read();
    }

    @Override
    public OutputStream write(NbtElement input) {
        return new NbtWriter().write(input);
    }

    @RequiredArgsConstructor
    private static class NbtReader {
        private final InputStream is;

        @SneakyThrows
        public NbtElement read() {
            ElementType type = getType(is.readNBytes(1)[0]);
            readString(); //skip name, stupid part of the spec
            return read(type);
        }

        @SneakyThrows
        private String readString() {
            //is in reality an unsigned short
            int len = (int) readBytes(2);
            return new String(is.readNBytes(len), StandardCharsets.UTF_8);
        }

        @SneakyThrows
        private NbtElement read(ElementType type) {
            return switch (type) {
                case END -> throw new ParseException("Unexpected END tag", 0);
                case BYTE -> new NbtByte(is.readNBytes(1)[0]);
                case SHORT -> new NbtShort((short) readBytes(2));
                case INT -> new NbtInt((int) readBytes(4));
                case LONG -> new NbtLong(readBytes(8));
                case FLOAT -> new NbtFloat(Float.intBitsToFloat((int) readBytes(4)));
                case DOUBLE -> new NbtDouble(Double.longBitsToDouble(readBytes(8)));
                case BYTE_ARRAY -> {
                    int size = (int) readBytes(4);
                    byte[] bytes = is.readNBytes(size);
                    yield new NbtByteArray(bytes);

                }
                case STRING -> new NbtString(readString());
                case LIST -> {
                    ElementType listType = getType(is.readNBytes(1)[0]);
                    int size = (int) readBytes(4);
                    List<NbtElement> list = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        list.add(read(listType));
                    }
                    //noinspection RedundantCast,unchecked
                    NbtList<NbtElement> nbtList = (NbtList<NbtElement>) new NbtList<>(listType.getNbtClass());
                    nbtList.addAll(list);
                    yield nbtList;
                }
                case COMPOUND -> {
                    NbtCompound compound = new NbtCompound();
                    ElementType subType = getType(is.readNBytes(1)[0]);
                    while (subType != ElementType.END) {
                        compound.put(readString(), read(subType));
                        subType = getType(is.readNBytes(1)[0]);
                    }
                    yield compound;
                }
                case INT_ARRAY -> {
                    int size = (int) readBytes(4);
                    int[] ints = new int[size];
                    for (int i = 0; i < size; i++) {
                        ints[i] = (int) readBytes(4);
                    }
                    yield new NbtIntArray(ints);
                }
                case LONG_ARRAY -> {
                    int size = (int) readBytes(4);
                    long[] longs = new long[size];
                    for (int i = 0; i < size; i++) {
                        longs[i] = readBytes(8);
                    }
                    yield new NbtLongArray(longs);
                }
            };
        }

        private long readBytes(int size) throws IOException {
            long value = 0;
            for (int i = 0; i < size; i++) {
                value = (value << 8) | (is.readNBytes(1)[0] & 0xff);
            }
            return value;
        }
    }

    private static class NbtWriter {
        private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        public OutputStream write(NbtElement nbt) {
            ElementType type = nbt.getType();
            //write type and empty name
            bos.writeBytes(new byte[]{TYPE_HEADERS.get(type), 0, 0});
            writeElement(nbt);
            return bos;
        }

        @SneakyThrows
        private void writeElement(NbtElement nbt) {
            ElementType type = nbt.getType();
            switch (type) {
                case END -> throw new IllegalArgumentException("END tag is not allowed");
                case BYTE -> {
                    NbtByte nbtByte = (NbtByte) nbt;
                    bos.writeBytes(new byte[]{nbtByte.getValue()});
                }
                case SHORT -> {
                    NbtShort nbtShort = (NbtShort) nbt;
                    short value = nbtShort.getValue();
                    bos.writeBytes(new byte[]{(byte) (value >> 8), (byte) value});
                }
                case INT -> {
                    NbtInt nbtInt = (NbtInt) nbt;
                    int value = nbtInt.getValue();
                    bos.writeBytes(new byte[]{
                            (byte) (value >> 24),
                            (byte) (value >> 16),
                            (byte) (value >> 8),
                            (byte) value});
                }
                case LONG -> {
                    NbtLong nbtLong = (NbtLong) nbt;
                    long value = nbtLong.getValue();
                    bos.writeBytes(new byte[]{
                            (byte) (value >> 56),
                            (byte) (value >> 48),
                            (byte) (value >> 40),
                            (byte) (value >> 32),
                            (byte) (value >> 24),
                            (byte) (value >> 16),
                            (byte) (value >> 8),
                            (byte) value});
                }
                case FLOAT -> {
                    NbtFloat nbtFloat = (NbtFloat) nbt;
                    int value = Float.floatToIntBits(nbtFloat.getValue());
                    writeElement(new NbtInt(value));
                }
                case DOUBLE -> {
                    NbtDouble nbtDouble = (NbtDouble) nbt;
                    long value = Double.doubleToLongBits(nbtDouble.getValue());
                    writeElement(new NbtLong(value));
                }
                case BYTE_ARRAY -> {
                    NbtByteArray byteArray = (NbtByteArray) nbt;
                    int size = byteArray.getValue().length;
                    writeElement(new NbtInt(size));
                    bos.write(byteArray.getValue());
                }
                case STRING -> {
                    NbtString string = (NbtString) nbt;
                    int legth = string.getValue().length();
                    if (legth >= 65536) {
                        throw new IllegalArgumentException("String too long");
                    }
                    writeElement(new NbtShort((short) legth));
                    bos.write(string.getValue().getBytes(StandardCharsets.UTF_8));
                }
                case LIST -> {
                    NbtList<?> list = (NbtList<?>) nbt;
                    writeElement(new NbtByte(TYPE_HEADERS.get(list.getSubType())));
                    writeElement(new NbtInt(list.size()));
                    for (NbtElement element : list) {
                        writeElement(element);
                    }
                }
                case COMPOUND -> {
                    NbtCompound compound = (NbtCompound) nbt;
                    compound.getElements().forEach((key, value) -> {
                        //type, name, value
                        writeElement(new NbtByte(TYPE_HEADERS.get(value.getType())));
                        writeElement(new NbtString(key));
                        writeElement(value);
                    });
                    bos.write(new byte[]{0});
                }
                case INT_ARRAY -> {
                    NbtIntArray intArray = (NbtIntArray) nbt;
                    int size = intArray.getValue().length;
                    writeElement(new NbtInt(size));
                    for (int i = 0; i < size; i++) {
                        writeElement(new NbtInt(intArray.getValue()[i]));
                    }
                }
                case LONG_ARRAY -> {
                    NbtLongArray longArray = (NbtLongArray) nbt;
                    int size = longArray.getValue().length;
                    writeElement(new NbtInt(size));
                    for (int i = 0; i < size; i++) {
                        writeElement(new NbtLong(longArray.getValue()[i]));
                    }
                }
            }
        }
    }
}
