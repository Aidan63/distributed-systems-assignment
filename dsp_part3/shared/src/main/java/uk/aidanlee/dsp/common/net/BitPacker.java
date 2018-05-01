package uk.aidanlee.dsp.common.net;

import java.util.BitSet;

/**
 * Bit packer utility class.
 * This bit packet allows us to specify exactly how many bits for values we want to send.
 */
public class BitPacker {
    /**
     * Max number of bits in the bit set.
     * (1400 * 8)
     */
    private final static int MAX_SIZE = 11200;

    /**
     * Bit set all data will be packed into.
     */
    private BitSet data;

    /**
     * The total number of bits in this set.
     */
    private int numBits;

    /**
     * The current read position.
     */
    private int readCursor;

    /**
     * Create an empty bit packer.
     */
    public BitPacker() {
        data       = new BitSet(MAX_SIZE);
        numBits    = 0;
        readCursor = 0;
    }

    // Write API

    /**
     * Writes an entire byte into the bit stream.
     * @param _data The byte to add.
     */
    public void writeByte(byte _data) {
        writeByte(_data, Byte.SIZE);
    }

    /**
     * Writes a specific number of bits from the provided byte into the bit stream.
     * @param _data   The byte to add.
     * @param _length The number of bits to write (max 8)
     */
    public void writeByte(byte _data, int _length) {
        if (_length > Byte.SIZE) {
            System.out.println("Invalid length (" + _length +") in writeByte()");
            System.exit(-1);
        }

        for (int i = 0; i < _length; i++) {
            if (((_data >> i) & 1) == 1) {
                data.set(numBits++, true);
            } else {
                data.set(numBits++, false);
            }
        }
    }

    /**
     * Writes an entire integer into the bit set.
     * @param _data The int to write.
     */
    public void writeInteger(int _data) {
        writeInteger(_data, Integer.SIZE);
    }

    /**
     * Writes a specific number of bits from the provided int into the bit stream.
     * @param _data   The int to write.
     * @param _length The number of bits to write (max 32)
     */
    public void writeInteger(int _data, int _length) {
        if (_length > Integer.SIZE) {
            System.out.println("Invalid length (" + _length +") in putInteger()");
            System.exit(-1);
        }

        for (int i = 0; i < _length; i++) {
            if (((_data >> i) & 1) == 1) {
                data.set(numBits++, true);
            } else {
                data.set(numBits++, false);
            }
        }
    }

    /**
     * Writes an entire float into the bit stream.
     * @param _data The float to write.
     */
    public void writeFloat(float _data) {
        int raw = Float.floatToRawIntBits(_data);

        for (int i = 0; i < Float.SIZE; i++) {
            if (((raw >> i) & 1) == 1) {
                data.set(numBits++, true);
            } else {
                data.set(numBits++, false);
            }
        }
    }

    /**
     * Writes a boolean into the bit stream.
     * @param _data The boolean to write.
     */
    public boolean writeBoolean(boolean _data) {
        data.set(numBits++, _data);
        return _data;
    }


    /**
     * Writes a string to the bit stream. Max length of 255 chars.
     * @param _data String value.
     */
    public void writeString(String _data) {
        byte[] payload = _data.getBytes();

        int length = payload.length;
        if (length > 255) {
            length = 255;
        }

        writeInteger(length, 8);
        writeBytes(payload);
    }

    /**
     * Writes a set of bytes into the bit stream.
     * @param _data Byte array to add.
     */
    public void writeBytes(byte[] _data) {
        writeBytes(_data, _data.length);
    }

    /**
     * Writes a specific number of bytes from a byte array into the bit stream.
     * @param _data  The bytes to add.
     * @param _bytes The number of bytes to add.
     */
    public void writeBytes(byte[] _data, int _bytes) {
        for (int i = 0; i < _bytes; i++) {
            writeByte(_data[i]);
        }
    }

    // Read API

    /**
     * Read an entire byte from the bit stream.
     * @return
     */
    public byte readByte() {
        return readByte(Byte.SIZE);
    }

    /**
     * Read a specific number of bits of a byte from the bit stream.
     * @param length Number of bits to read.
     * @return byte.
     */
    public byte readByte(int length) {

        byte value = 0;

        for (int i=0;i<length;i++) {
            value |= (data.get(readCursor++)?1:0) << (i%Byte.SIZE);
        }

        return value;
    }

    /**
     * Reads an entire 32bit integer from the bit stream.
     * @return integer.
     */
    public int readInteger() {
        return readInteger(Integer.SIZE);
    }

    /**
     * Reads a specific number of integer bits from the bit stream.
     * @param _length Number of bits to read.
     * @return integer.
     */
    public int readInteger(int _length) {
        int value = 0;
        for (int i = 0; i < _length; i++) {
            if (readCursor > numBits - 1) {
                System.out.println("Out of bound error, read: " + readCursor +", numBits: " +numBits);
                System.exit(-1);
            }

            value |= (data.get(readCursor++) ? 1 : 0) << (i % Integer.SIZE);
        }

        return value;
    }

    /**
     * Reads an entire 32bit float from the bit stream.
     * @return float.
     */
    public float readFloat() {
        return readFloat(Float.SIZE);
    }

    /**
     * Reads a specific number of bits of a float from a bit stream.
     * @param _length Number of bits to read.
     * @return float.
     */
    public float readFloat(int _length) {
        int value = 0;
        for (int i = 0; i < _length; i++) {
            if (readCursor > numBits - 1) {
                System.out.println("Out of bound error, read: " + readCursor +", numBits: " +numBits);
                System.exit(-1);
            }
            value |= (data.get(readCursor++) ? 1 : 0) << (i % Float.SIZE);
        }

        return Float.intBitsToFloat(value);
    }

    /**
     * Reads a boolean from the bit stream.
     * @return boolean.
     */
    public boolean readBoolean() {
        if (readCursor > numBits - 1) {
            System.out.println("Out of bound error, read: " + readCursor +", numBits: " +numBits);
            System.exit(-1);
        }

        return data.get(readCursor++);
    }

    /**
     * Reads a specific number of bytes from the bit stream.
     * @param _length Number of bytes to read.
     * @return byte array.
     */
    public byte[] readBytes(int _length) {
        byte[] output = new byte[_length];
        for (int i = 0;i < _length; i++) {
            output[i] = readByte();
        }

        return output;
    }


    /**
     * Reads a string from the bit stream.
     * @return
     */
    public String readString() {
        int length = readInteger(8);
        byte[] payload = readBytes(length);

        return new String(payload);

    }

    // Util API

    /**
     * Clears the bit set.
     */
    public void clear() {
        data.clear();
        numBits = 0;
    }

    /**
     * Gets the bytes of the bit set ceiled to the nearest byte.
     * @return byte array of the bit set.
     */
    public byte[] toBytes() {
        pad();
        byte[] output = new byte[numBits / 8];

        for (int i = 0; i < output.length; i++) {
            output[i] = readByte();
        }

        return output;
    }

    /**
     * Pads this bit set to the nearest byte. Fills any bits with 0.
     */
    public void pad() {
        int remaining = 8 - (numBits % 8);
        for (int i = 0; i < remaining;i++) {
            writeBoolean(false);
        }

    }
}
