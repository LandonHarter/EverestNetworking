package com.landonharter.everest.packet;

import com.landonharter.everest.utility.ByteUtility;

import java.util.List;
import java.util.ArrayList;

public class Packet {

    private List<Byte> buffer;
    private byte[] readableBuffer;
    private int readPos;

    public Packet() {
        buffer = new ArrayList<>();
        readPos = 0;
    }

    public Packet(int id) {
        buffer = new ArrayList<>();
        readPos = 0;

        Write(id);
    }

    public Packet(byte[] data) {
        buffer = new ArrayList<>();
        readPos = 0;

        SetBytes(data);
    }

    public void SetBytes(byte[] bytes) {
        Write(bytes);
        readableBuffer = ToByteArray(buffer.toArray());
    }

    public void Reset() {
        buffer.clear();
        readableBuffer = null;
        readPos = 0;
    }

    public void Reset(boolean shouldReset) {
        if (shouldReset) {
            Reset();
        } else {
            readPos -= 4;
        }
    }

    public void Write(byte value) {
        buffer.add(value);
    }

    public void Write(byte[] value) {
        AddRange(value);
    }

    public void Write(int value) {
        byte[] bytes = ByteUtility.GetBytes(value);
        AddRange(bytes);
    }

    public void Write(float value) {
        byte[] bytes = ByteUtility.GetBytes(value);
        AddRange(bytes);
    }

    public void Write(boolean value) {
        byte[] bytes = ByteUtility.GetBytes(value);
        AddRange(bytes);
    }

    public void Write(String value) {
        Write(value.length());

        byte[] bytes = ByteUtility.GetBytes(value);
        AddRange(bytes);
    }

    public void Write(int[] value) {
        Write(value.length);
        for (int integer : value) {
            Write(integer);
        }
    }

    public void Write(float[] value) {
        Write(value.length);
        for (float dec : value) {
            Write(dec);
        }
    }

    public byte ReadByte() {
        if (buffer.size() > readPos) {
            byte value = readableBuffer[readPos];

            readPos++;
            return value;
        } else {
            System.err.println("Couldn't read type byte from packet");
            
            return -1;
        }
    }

    public byte[] ReadBytes(int length) {
        if (buffer.size() > readPos) {
            byte[] value = ToByteArray(buffer.subList(readPos, readPos + length).toArray());
            readPos += length;

            return value;
        } else {
            System.err.println("Couldn't read type byte[] from packet");
            return null;
        }
    }

    public int ReadInt() {
        if (buffer.size() > readPos) {
            int value = ByteUtility.ToInt(readableBuffer, readPos);
            readPos += 4;

            return value;
        } else {
            System.err.println("Couldn't read type int from packet");
            return -1;
        }
    }

    public float ReadFloat() {
        if (buffer.size() > readPos) {
            float value = ByteUtility.ToFloat(readableBuffer, readPos);
            readPos += 4;

            return value;
        } else {
            System.err.println("Couldn't read type float from packet");
            return -1;
        }
    }

    public boolean ReadBoolean() {
        if (buffer.size() > readPos) {
            boolean value = ByteUtility.ToBoolean(readableBuffer, readPos);
            readPos += 1;

            return value;
        } else {
            System.err.println("Couldn't read type boolean from packet");
            return false;
        }
    }

    public String ReadString() {
        if (buffer.size() > readPos) {
            int length = ReadInt();
            String value = ByteUtility.ToString(readableBuffer, readPos, length);

            if (value.length() > 0) {
                readPos += length;
            }

            return value;
        } else {
            System.err.println("Couldn't read type String from packet");
            return null;
        }
    }

    public int[] ReadIntArray() {
        if (buffer.size() > readPos) {
            int length = ReadInt();
            int[] values = new int[length];

            for (int i = 0; i < values.length; i++) {
                values[i] = ReadInt();
            }

            return values;
        } else {
            System.err.println("Couldn't read type float[] from packet");
            return null;
        }
    }

    public float[] ReadFloatArray() {
        if (buffer.size() > readPos) {
            int length = ReadInt();
            float[] values = new float[length];

            for (int i = 0; i < values.length; i++) {
                values[i] = ReadFloat();
            }

            return values;
        } else {
            System.err.println("Couldn't read type float[] from packet");
            return null;
        }
    }

    public void WriteLength() {
        InsertRange(ByteUtility.GetBytes(buffer.size()), 0);
    }

    public int UnreadLength() {
        return Length() - readPos;
    }

    public int Length() {
        return buffer.size();
    }

    public byte[] ToArray() {
        readableBuffer = ToByteArray(buffer.toArray());
        return readableBuffer;
    }

    private void AddRange(byte[] data) {
        for (byte b : data) {
            buffer.add(b);
        }
    }

    private void InsertRange(byte[] data, int index) {
        for (int i = data.length - 1; i >= 0; i--) {
            buffer.add(index, data[i]);
        }
    }

    private byte[] ToByteArray(Object[] array) {
        byte[] byteArray = new byte[array.length];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = (byte)array[i];
        }

        return byteArray;
    }

}