package emulation.multithreading.Memory.Core;

public class SegmentReader {
    private int currentBit;
    private final Segment segment;

    private SegmentReader(Segment segment) {
        this.currentBit = 0;
        this.segment = segment;
    }

    public int readInt8() {
        int result = this.segment.readInt8(this.currentBit);
        this.currentBit += Segment.INT8_MASK;

        return result;
    }

    public int readInt32() {
        int result = this.segment.readInt32(this.currentBit);
        this.currentBit += Segment.INT32_SIZE;

        return result;
    }

    public static SegmentReader beginParse(Segment segment) {
        return new SegmentReader(segment);
    }
}