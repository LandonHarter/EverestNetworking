package com.landonharter.everest.utility;

import at.favre.lib.bytes.Bytes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class Convert {

    protected Convert() {}

    public static byte[] getBytes(int value) {
        return fromBytes(Bytes.from(value).toBoxedArray());
    }

    public static byte[] getBytes(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static byte[] getBytes(boolean value) {
        byte[] bytes = new byte[] { (byte)(value ? 1 : 0) };
        return bytes;
    }

    public static byte[] getBytes(String value) {
        return value.getBytes();
    }

    public static int toInt(byte[] value, int startIndex) {
        return Bytes.from(toByteList(value).subList(startIndex, startIndex + 4)).toInt();
    }

    public static float toFloat(byte[] value, int startIndex) {
        return ByteBuffer.wrap(value, startIndex, 4).getFloat();
    }

    public static boolean toBoolean(byte[] value, int startIndex) {
        byte booleanValue = value[startIndex];

        return (booleanValue == 1) ? true : false;
    }

    public static String toString(byte[] value, int startIndex, int length) {
        byte[] stringBytes = toByteArray(toByteList(value).subList(startIndex, startIndex + length));

        String stringValue = new String(stringBytes, StandardCharsets.UTF_8);
        return stringValue;
    }

    private static List<Byte> toByteList(byte[] data) {
        List<Byte> returnBytes = new ArrayList<>();
        for (byte b : data) {
            returnBytes.add(b);
        }

        return returnBytes;
    }

    private static byte[] toByteArray(List<Byte> bytes) {
        byte[] returnBytes = new byte[bytes.size()];

        for (int i = 0; i < returnBytes.length; i++) {
            returnBytes[i] = bytes.get(i);
        }

        return returnBytes;
    }

    private static byte[] fromBytes(Byte[] bytes) {
        byte[] returnBytes = new byte[bytes.length];

        for (int i = 0; i < returnBytes.length; i++) {
            returnBytes[i] = bytes[i];
        }

        return returnBytes;
    }

}
