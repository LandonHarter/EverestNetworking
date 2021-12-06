package com.landonharter.everest.utility;

import at.favre.lib.bytes.Bytes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ByteUtility {

    public static byte[] GetBytes(int value) {
        return FromBytes(Bytes.from(value).toBoxedArray());
    }

    public static byte[] GetBytes(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static byte[] GetBytes(boolean value) {
        byte[] bytes = new byte[] { (byte)(value ? 1 : 0) };
        return bytes;
    }

    public static byte[] GetBytes(String value) {
        return value.getBytes();
    }

    public static int ToInt(byte[] value, int startIndex) {
        return Bytes.from(ToByteList(value).subList(startIndex, startIndex + 4)).toInt();
    }

    public static float ToFloat(byte[] value, int startIndex) {
        return ByteBuffer.wrap(value, startIndex, 4).getFloat();
    }

    public static boolean ToBoolean(byte[] value, int startIndex) {
        byte booleanValue = value[startIndex];

        return (booleanValue == 1) ? true : false;
    }

    public static String ToString(byte[] value, int startIndex, int length) {
        byte[] stringBytes = ToByteArray(ToByteList(value).subList(startIndex, startIndex + length));

        String stringValue = new String(stringBytes, StandardCharsets.UTF_8);
        return stringValue;
    }

    private static List<Byte> ToByteList(byte[] data) {
        List<Byte> returnBytes = new ArrayList<>();
        for (byte b : data) {
            returnBytes.add(b);
        }

        return returnBytes;
    }

    private static byte[] ToByteArray(List<Byte> bytes) {
        byte[] returnBytes = new byte[bytes.size()];

        for (int i = 0; i < returnBytes.length; i++) {
            returnBytes[i] = bytes.get(i);
        }

        return returnBytes;
    }

    private static byte[] FromBytes(Byte[] bytes) {
        byte[] returnBytes = new byte[bytes.length];

        for (int i = 0; i < returnBytes.length; i++) {
            returnBytes[i] = bytes[i];
        }

        return returnBytes;
    }

}
