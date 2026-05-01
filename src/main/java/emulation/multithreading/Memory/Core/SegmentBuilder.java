package emulation.multithreading.Memory.Core;

public class SegmentBuilder {
    private int currentBit;
    private final Segment segment;

    private SegmentBuilder(Segment segment) {
        this.currentBit = 0;
        this.segment = segment;
    }

    public SegmentBuilder writeInt8(int value) {
        boolean result = this.segment.writeInt8(currentBit, value);
        if (result) {
            this.currentBit += 8;
            return this;
        }

        throw new RuntimeException("Error in writing process");
    }

    public SegmentBuilder writeInt32(int value) {
        boolean result = this.segment.writeInt32(currentBit, value);
        if (result) {
            this.currentBit += 32;
            return this;
        }

        throw new RuntimeException("Error in writing process");
    }

    public static SegmentBuilder begin(Segment segment) {
        return new SegmentBuilder(segment);
    }


}