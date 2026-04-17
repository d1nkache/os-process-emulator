package emulation.multithreading.Memory.Core;

import lombok.Getter;
import java.util.BitSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import emulation.multithreading.Memory.TransferObjects.SegmentInfo;


@Getter
public class Segment implements Comparable<Integer> {
    private final int size;
    private final int startAddress;

    private final String name;
    private final BitSet data;

    public static final int INT8_MASK = 0xFF;
    public static final int INT8_SIZE = 8;
    public static final int INT32_SIZE = 32;

    public Segment(int size, int startAddress, String name) {
        this.size = size;
        this.startAddress = startAddress;
        this.name = name;
        this.data = new BitSet();
    }

    // ################################# COMPARATORS #################################
    @Override
    public int compareTo(@NotNull Integer inputAddress) {
        if (inputAddress < this.startAddress) {
            return 1;
        }

        if (inputAddress >= this.startAddress + this.size) {
            return -1;
        }

        return 0;
    }

    @Nullable
    public Segment compareAddress(int inputAddress) {
        if (this.startAddress <= inputAddress && inputAddress < this.startAddress + size ) {
            return this;
        }

        return null;
    }

    // ################################# Operations #################################
    public Boolean writeInt32(int offset, int value) {
        if (offset < 0) {
            throw new IllegalArgumentException();
        }

        if (offset + INT32_SIZE > this.size) {
            throw new RuntimeException("Segment maxSize is " + this.size);
        }

        for (int i = 0; i < INT32_SIZE; i ++) {
            int insertIndex = offset + i;
            boolean insertValue = (value >> (31 - i) & 1) == 1;

            this.data.set(insertIndex, insertValue);
        }

        return true;
    }

    public Boolean writeInt8(int offset, int value) {
        if (offset < 0) {
            throw new IllegalArgumentException();
        }

        if (offset + INT8_SIZE > this.size) {
            throw new RuntimeException("Segment maxSize is " + this.size);
        }

        value &= INT8_MASK;

        for (int i = 0; i < INT8_SIZE; i ++) {
            int insertIndex = offset + i;
            boolean insertValue = (value >> (7 - i) & 1) == 1;

            this.data.set(insertIndex, insertValue);
        }

        return true;
    }

    public int readInt8(int startIndex) {
        if (startIndex < 0) {
            throw new IllegalArgumentException();
        }

        if (startIndex + INT8_SIZE > this.size) {
            throw new RuntimeException("Segment maxSize is " + this.size);
        }
        int result = 0;

        for (int i = 0; i < INT8_SIZE; i ++) {
            if (this.data.get(startIndex + i)) {
                result |= (1 << (7 - i));
            }
        }

        return result;
    }

    public int readInt32(int startIndex) {
        if (startIndex < 0) {
            throw new IllegalArgumentException();
        }

        if (startIndex + INT32_SIZE > this.size) {
            throw new RuntimeException("Segment maxSize is " + this.size);
        }
        int result = 0;

        for (int i = 0; i < INT32_SIZE; i ++) {
            if (this.data.get(startIndex + i)) {
                result |= (1 << (31 - i));
            }
        }

        return result;
    }

    // ################################# GETTERS AND SETTERS #################################
    public SegmentInfo getSegmentInfo() {
        return new SegmentInfo(
                this.getSize(),
                this.getStartAddress(),
                this.getName()
        );
    }
}