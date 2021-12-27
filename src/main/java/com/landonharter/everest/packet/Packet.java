package com.landonharter.everest.packet;

import com.landonharter.everest.utility.Convert;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

public class Packet {

    private List<Byte> buffer;
    private byte[] readableBuffer;
    private int readPos;

    public Packet() {
        buffer = new ArrayList<>();
        readPos = 0;

        write(-1);
    }

    public Packet(int id) {
        buffer = new ArrayList<>();
        readPos = 0;

        write(id);
    }

    public Packet(ClientPackets packet) {
        buffer = new ArrayList<>();
        readPos = 0;

        write(packet.ordinal());
    }

    public Packet(ServerPackets packet) {
        buffer = new ArrayList<>();
        readPos = 0;

        write(packet.ordinal());
    }

    public Packet(byte[] data) {
        buffer = new ArrayList<>();
        readPos = 0;

        setBytes(data);
    }

    public void setBytes(byte[] bytes) {
        write(bytes);
        readableBuffer = toByteArray(buffer.toArray());
    }

    public void reset() {
        buffer.clear();
        readableBuffer = null;
        readPos = 0;
    }

    public void reset(boolean shouldReset) {
        if (shouldReset) {
            reset();
        } else {
            readPos -= 4;
        }
    }

    public void write(byte value) {
        if (!canWrite(1)) {
            notEnoughSpace();
            return;
        }

        buffer.add(value);
    }

    public void write(byte[] value) {
        if (!canWrite(value.length)) {
            notEnoughSpace();
            return;
        }

        addRange(value);
    }

    public void write(int value) {
        if (!canWrite(4)) {
            notEnoughSpace();
            return;
        }

        byte[] bytes = Convert.getBytes(value);
        addRange(bytes);
    }

    public void write(float value) {
        if (!canWrite(4)) {
            notEnoughSpace();
            return;
        }

        byte[] bytes = Convert.getBytes(value);
        addRange(bytes);
    }

    public void write(boolean value) {
        if (!canWrite(1)) {
            notEnoughSpace();
            return;
        }

        byte[] bytes = Convert.getBytes(value);
        addRange(bytes);
    }

    public void write(String value) {
        byte[] bytes = Convert.getBytes(value);
        
        write(value.length());
        write(bytes);
    }

    public void write(int[] value) {
        write(value.length);
        for (int integer : value) {
            write(integer);
        }
    }

    public void write(float[] value) {
        write(value.length);
        for (float dec : value) {
            write(dec);
        }
    }

    public void write(Color color) {
        write(color.getRGB());
    }

    public byte readByte() {
        if (buffer.size() > readPos) {
            byte value = readableBuffer[readPos];

            readPos++;
            return value;
        } else {
            System.err.println("Couldn't read type byte from packet");
            
            return -1;
        }
    }

    public byte[] readBytes(int length) {
        if (buffer.size() > readPos) {
            byte[] value = toByteArray(buffer.subList(readPos, readPos + length).toArray());
            readPos += length;

            return value;
        } else {
            System.err.println("Couldn't read type byte[] from packet");
            return null;
        }
    }

    public int readInt() {
        if (buffer.size() > readPos) {
            int value = Convert.toInt(readableBuffer, readPos);
            readPos += 4;

            return value;
        } else {
            System.err.println("Couldn't read type int from packet");
            return -1;
        }
    }

    public float readFloat() {
        if (buffer.size() > readPos) {
            float value = Convert.toFloat(readableBuffer, readPos);
            readPos += 4;

            return value;
        } else {
            System.err.println("Couldn't read type float from packet");
            return -1;
        }
    }

    public boolean readBoolean() {
        if (buffer.size() > readPos) {
            boolean value = Convert.toBoolean(readableBuffer, readPos);
            readPos += 1;

            return value;
        } else {
            System.err.println("Couldn't read type boolean from packet");
            return false;
        }
    }

    public String readString() {
        if (buffer.size() > readPos) {
            int length = readInt();
            String value = Convert.toString(readableBuffer, readPos, length);

            if (value.length() > 0) {
                readPos += length;
            }

            return value;
        } else {
            System.err.println("Couldn't read type String from packet");
            return null;
        }
    }

    public int[] readIntArray() {
        if (buffer.size() > readPos) {
            int length = readInt();
            int[] values = new int[length];

            for (int i = 0; i < values.length; i++) {
                values[i] = readInt();
            }

            return values;
        } else {
            System.err.println("Couldn't read type float[] from packet");
            return null;
        }
    }

    public float[] readFloatArray() {
        if (buffer.size() > readPos) {
            int length = readInt();
            float[] values = new float[length];

            for (int i = 0; i < values.length; i++) {
                values[i] = readFloat();
            }

            return values;
        } else {
            System.err.println("Couldn't read type float[] from packet");
            return null;
        }
    }

    public Color readColor() {
        return new Color(readInt());
    }

    public void writeLength() {
        insertRange(Convert.getBytes(buffer.size()), 0);
    }

    public int unreadLength() {
        return length() - readPos;
    }

    public int length() {
        return buffer.size();
    }

    public int unusedStorage() {
        int unused = 4096 - buffer.size();

        return unused;
    }

    public boolean hasStorage() {
        if (unusedStorage() > 0) {
            return true;
        }

        return false;
    }

    public byte[] toArray() {
        readableBuffer = toByteArray(buffer.toArray());
        return readableBuffer;
    }

    private void addRange(byte[] data) {
        for (byte b : data) {
            buffer.add(b);
        }
    }

    private void insertRange(byte[] data, int index) {
        for (int i = data.length - 1; i >= 0; i--) {
            buffer.add(index, data[i]);
        }
    }

    private byte[] toByteArray(Object[] array) {
        byte[] byteArray = new byte[array.length];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = (byte)array[i];
        }

        return byteArray;
    }

    private boolean canWrite(int size) {
        if (unusedStorage() >= size) {
            return true;
        } else {
            return false;
        }
    }

    private void notEnoughSpace() {
        System.err.println("Packet: There isn't enough space left to write the value");
    }

}