package at.haha007.edennbt.parser;

import at.haha007.edennbt.element.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class SnbtParser implements NbtParser<String, String> {
    @Override
    public NbtElement read(String input) {
        return new NbtReader(input).read();
    }

    @Override
    public String write(NbtElement input) {
        ElementType type = input.getType();
        return switch (type) {
            case END -> throw new IllegalArgumentException("END tag is not allowed");
            case BYTE -> ((NbtByte) input).getValue() + "b";
            case SHORT -> ((NbtShort) input).getValue() + "s";
            case INT -> Integer.toString(((NbtInt) input).getValue());
            case LONG -> ((NbtLong) input).getValue() + "L";
            case FLOAT -> ((NbtFloat) input).getValue() + "f";
            case DOUBLE -> ((NbtDouble) input).getValue() + "d";
            case BYTE_ARRAY -> {
                StringBuilder sb = new StringBuilder();
                sb.append("[B;");
                NbtByteArray array = (NbtByteArray) input;
                StringJoiner stringJoiner = new StringJoiner(",", "", "");
                for (byte b : array.getValue()) {
                    stringJoiner.add(Byte.toString(b));
                }
                sb.append(stringJoiner);
                sb.append("]");
                yield sb.toString();
            }
            case STRING -> {
                String string = ((NbtString) input).getValue();
                if (string.matches("[0-9a-zA-Z_\\-.+]+"))
                    yield string;
                yield "\"" + escape(string) + "\"";
            }
            case LIST -> {
                NbtList<?> list = (NbtList<?>) input;
                String joined = list.stream().map(this::write).collect(Collectors.joining(","));
                yield "[" + joined + "]";
            }
            case COMPOUND -> {
                NbtCompound compound = (NbtCompound) input;
                StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
                for (Map.Entry<String, NbtElement> element : compound.entrySet()) {
                    stringJoiner.add(write(new NbtString(element.getKey())) + ":" + write(element.getValue()));
                }
                yield stringJoiner.toString();
            }
            case INT_ARRAY -> {
                StringBuilder sb = new StringBuilder();
                sb.append("[I;");
                NbtByteArray array = (NbtByteArray) input;
                StringJoiner stringJoiner = new StringJoiner(",", "", "");
                for (byte b : array.getValue()) {
                    stringJoiner.add(Byte.toString(b));
                }
                sb.append(stringJoiner);
                sb.append("]");
                yield sb.toString();
            }
            case LONG_ARRAY -> {
                StringBuilder sb = new StringBuilder();
                sb.append("[L;");
                NbtByteArray array = (NbtByteArray) input;
                StringJoiner stringJoiner = new StringJoiner(",", "", "");
                for (byte b : array.getValue()) {
                    stringJoiner.add(Byte.toString(b));
                }
                sb.append(stringJoiner);
                sb.append("]");
                yield sb.toString();
            }
        };
    }

    private static String escape(String originalString) {
        StringBuilder result = new StringBuilder();
        int length = originalString.length();

        for (int i = 0; i < length; i++) {
            char currentChar = originalString.charAt(i);

            switch (currentChar) {
                case '\n':
                    result.append("\\n");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                case '\"':
                    result.append("\\\"");
                    break;
                case '\'':
                    result.append("\\'");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                // Add more cases for other characters as needed
                default:
                    result.append(currentChar);
                    break;
            }
        }

        return result.toString();
    }

    @RequiredArgsConstructor
    private static class NbtReader {
        private int position = 0;
        private final String input;

        @SneakyThrows
        public NbtElement read() {
            checkEndOfInput();
            char currentChar = input.charAt(position);
            position++;
            return switch (currentChar) {
                case '{' -> readCompound();
                case '[' -> readListLike();
                default -> {
                    position--;
                    try {
                        yield readNumber();
                    } catch (NumberFormatException | ParseException e) {
                        yield readString();
                    }
                }
            };
        }

        private void checkEndOfInput() throws ParseException {
            if (position >= input.length())
                throw new ParseException("Unexpected end of input", position);
        }

        private NbtElement readNumber() throws ParseException {
            String num = readNum();
            if (num.isBlank()) {
                throw new ParseException("Not a number", position);
            }
            char currentChar = input.charAt(position);
            position++;
            if (currentChar == 'B' || currentChar == 'b') {
                return new NbtByte(Byte.parseByte(num));
            }
            if (currentChar == 'S' || currentChar == 's') {
                return new NbtShort(Short.parseShort(num));
            }
            if (currentChar == 'L' || currentChar == 'l') {
                return new NbtLong(Long.parseLong(num));
            }
            if (currentChar == 'F' || currentChar == 'f') {
                return new NbtFloat(Float.parseFloat(num));
            }
            if (currentChar == 'D' || currentChar == 'd') {
                return new NbtDouble(Double.parseDouble(num));
            }
            position--;
            return new NbtInt(Integer.parseInt(num));
        }

        private NbtElement readCompound() throws ParseException {
            checkEndOfInput();
            char currentChar = input.charAt(position);
            if (currentChar == '}') {
                position++;
                return new NbtCompound();
            }
            NbtCompound compund = new NbtCompound();
            while (true) {
                checkEndOfInput();
                currentChar = input.charAt(position);
                if (currentChar == '}') {
                    position++;
                    return compund;
                }
                String key = readString().getValue();
                currentChar = input.charAt(position);
                if (currentChar != ':') {
                    throw new ParseException("missing ':' after key " + key, position);
                }
                position++;
                NbtElement value = read();
                compund.put(key, value);
            }
        }

        private NbtString readString() throws ParseException {
            checkEndOfInput();
            char currentChar = input.charAt(position);
            String s;
            if (currentChar == '"' || currentChar == '\'') {
                s = unescapeJavaString(input, position);
                position += s.length() + 2;
            } else {
                //read not escaped string
                StringBuilder builder = new StringBuilder();
                while ((currentChar >= 'a' && currentChar <= 'z') ||
                        (currentChar >= 'A' && currentChar <= 'Z') ||
                        (currentChar >= '0' && currentChar <= '9') ||
                        currentChar == '_' ||
                        currentChar == '-' ||
                        currentChar == '.' ||
                        currentChar == '+'
                ) {
                    builder.append(currentChar);
                    position++;
                    checkEndOfInput();
                    currentChar = input.charAt(position);
                }
                s = builder.toString();
            }
            return new NbtString(s);
        }

        private static String unescapeJavaString(String escapedString, int startIndex) {
            StringBuilder result = new StringBuilder();
            int length = escapedString.length();
            char escapeChar = escapedString.charAt(startIndex);
            if (escapedString.charAt(startIndex) != '"' && escapedString.charAt(startIndex) != '\'') {
                throw new IllegalArgumentException("Invalid escaped string: " + escapedString
                        + " (start index: " + startIndex + ")");
            }
            for (int i = startIndex + 1; i < length; i++) {
                char currentChar = escapedString.charAt(i);

                if (currentChar == '\\' && i < length - 1) {
                    // Handle escape sequences
                    char nextChar = escapedString.charAt(i + 1);
                    switch (nextChar) {
                        case 'n':
                            result.append('\n');
                            break;
                        case 't':
                            result.append('\t');
                            break;
                        // Add more cases for other escape sequences as needed
                        default:
                            // If it's not a recognized escape sequence, append the characters as is
                            result.append(currentChar);
                            break;
                    }
                    // Skip the next character since it has been processed as part of the escape sequence
                    i++;
                } else {
                    //a non-escaped " signals the end of the string
                    if (currentChar == escapeChar) {
                        return result.toString();
                    }
                    // If it's not the start of an escape sequence, append the character as is
                    result.append(currentChar);
                }
            }
            throw new IllegalArgumentException("Invalid escaped string: " + escapedString);
        }

        /**
         * May read List, ByteArray, IntArray or LongArray
         *
         * @return the corresponding NbtElement
         */
        private NbtElement readListLike() throws ParseException {
            checkEndOfInput();
            char currentChar = input.charAt(position);
            //handle empty list
            if (currentChar == ']') {
                position++;
                return new NbtList<>();
            }

            if (position + 1 >= input.length())
                throw new ParseException("Unexpected end of input", position);

            //handle byte array
            if (currentChar == 'B') {
                return getNbtByteArray();
            }

            //handle int array
            if (currentChar == 'I') {
                return getNbtIntArray();
            }

            //handle long array
            if (currentChar == 'L') {
                return getNbtLongArray();
            }

            //handle list
            NbtList<NbtElement> list = new NbtList<>();
            while (true) {
                checkEndOfInput();
                list.add(read());
                currentChar = input.charAt(position);
                if (currentChar == ']') {
                    position++;
                    break;
                }
                if (currentChar != ',') {
                    throw new ParseException("Invalid list declaration: " + currentChar, position);
                }
                position++;
            }
            return list;
        }

        private NbtElement getNbtLongArray() throws ParseException {
            char currentChar;
            position++;
            currentChar = input.charAt(position);
            if (currentChar != ';') {
                throw new ParseException("Invalid LongArray declaration: " + currentChar, position);
            }
            position++;
            List<Long> longs = new ArrayList<>();
            while (true) {
                checkEndOfInput();
                currentChar = input.charAt(position);
                if (currentChar == ']') {
                    position++;
                    break;
                }
                String num = readNum();
                longs.add(Long.parseLong(num));
            }
            long[] array = new long[longs.size()];
            for (int i = 0; i < longs.size(); i++) {
                array[i] = longs.get(i);
            }
            return new NbtLongArray(array);
        }

        private NbtElement getNbtIntArray() throws ParseException {
            char currentChar;
            position++;
            currentChar = input.charAt(position);
            if (currentChar != ';') {
                throw new ParseException("Invalid IntArray declaration: " + currentChar, position);
            }
            position++;
            List<Integer> ints = new ArrayList<>();
            while (true) {
                checkEndOfInput();
                currentChar = input.charAt(position);
                if (currentChar == ']') {
                    position++;
                    break;
                }
                String num = readNum();
                ints.add(Integer.parseInt(num));
            }
            int[] array = new int[ints.size()];
            for (int i = 0; i < ints.size(); i++) {
                array[i] = ints.get(i);
            }
            return new NbtIntArray(array);
        }

        @NotNull
        private NbtByteArray getNbtByteArray() throws ParseException {
            char currentChar;
            position++;
            currentChar = input.charAt(position);
            if (currentChar != ';') {
                throw new ParseException("Invalid ByteArray declaration: " + currentChar, position);
            }
            position++;
            List<Byte> bytes = new ArrayList<>();
            while (true) {
                checkEndOfInput();
                currentChar = input.charAt(position);
                if (currentChar == ']') {
                    position++;
                    break;
                }
                String num = readNum();
                bytes.add(Byte.parseByte(num));
            }
            byte[] array = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                array[i] = bytes.get(i);
            }
            return new NbtByteArray(array);
        }

        private String readNum() {
            StringBuilder sb = new StringBuilder();
            while (position < input.length() &&
                    (Character.isDigit(input.charAt(position)) || input.charAt(position) == '.')) {
                sb.append(input.charAt(position));
                position++;
            }
            return sb.toString();
        }
    }
}
