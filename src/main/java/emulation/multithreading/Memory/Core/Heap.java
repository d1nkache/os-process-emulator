package emulation.multithreading.Memory.Core;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;


public class Heap {
    private final Integer maxSize;
    private TreeMap<Integer, Segment> memoryMap;

    @Nullable
     public Segment getSegmentByAddress(Integer address) {
         Map.Entry<Integer, Segment> entry = memoryMap.floorEntry(address);

         if (entry != null && entry.getValue().compareTo(address) == 0) {
             return entry.getValue();
         }

         return null;
     }
}