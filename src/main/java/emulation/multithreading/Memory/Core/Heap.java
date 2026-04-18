package emulation.multithreading.Memory.Core;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;


@Getter
public class Heap {
    private final int maxSize;
    private final TreeMap<Integer, Segment> memoryMap;
    
    private int currentSize;

    public Heap(int maxSize) {
        this.maxSize = maxSize;
        this.memoryMap = new TreeMap<Integer, Segment>();
        this.currentSize = 0;
    }

    // ################################# Operations #################################
     public Segment allocateSegment(int size, String name) {
        if (isEnoughMemory(size)) {
            int address = findNextFreeAddress();
            Segment toInsert = new Segment(size, address, name);

            memoryMap.put(address, toInsert);
            this.currentSize += size;

            return toInsert;
        }

         throw new RuntimeException("Not enough memory");
     }

    public Segment allocateSegment(String name) {
        if (!isEnoughMemory(Segment.STANDARD_MAX_SIZE)) {
            throw new RuntimeException("Not enough memory");
        }

        int address = findNextFreeAddress();
        Segment toInsert = new Segment(Segment.STANDARD_MAX_SIZE, address, name);

        memoryMap.put(address, toInsert);
        this.currentSize += Segment.STANDARD_MAX_SIZE;

        return toInsert;
    }

    public boolean deallocateSegment(int startAddress) {
        Segment removed = this.memoryMap.remove(startAddress);

        if (removed != null) {
            this.currentSize -= removed.getSize();
            return true;
        }

        return false;
    }

    public boolean deallocateByAnyAddress(int address) {
        Segment toRemove = getSegmentByAddress(address);

        if (toRemove != null) {
            return deallocateSegment(toRemove.getStartAddress());
        }

        return false;
    }

    // ################################# HELPERS #################################
     private boolean isEnoughMemory(int toAllocate) {
        return maxSize >=  this.currentSize + toAllocate;
     }

     private int findNextFreeAddress() {
        if (memoryMap.isEmpty()) return 0;
        Segment last = this.memoryMap.lastEntry().getValue();

         return last.getStartAddress() + last.getSize();
     }

    // ################################# GETTERS #################################
    @Nullable
     public Segment getSegmentByAddress(int address) {
         Map.Entry<Integer, Segment> entry = memoryMap.floorEntry(address);

         if (entry != null && entry.getValue().compareTo(address) == 0) {
             return entry.getValue();
         }

         return null;
     }

    public Set<Map.Entry<Integer, Segment>> getAllSegments() {
        return this.memoryMap.entrySet();
    }
}
