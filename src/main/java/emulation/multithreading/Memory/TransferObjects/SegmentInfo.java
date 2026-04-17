package emulation.multithreading.Memory.TransferObjects;

import lombok.Data;
import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor
public class SegmentInfo {
    int size;
    int startAddress;
    String name;
}