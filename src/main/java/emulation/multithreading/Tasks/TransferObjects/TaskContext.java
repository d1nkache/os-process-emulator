package emulation.multithreading.Tasks.TransferObjects;

import emulation.multithreading.Tasks.TaskState;

import java.util.Map;

public record TaskContext(
        int pid,
        int threadGroupId,
        int instructionPointer,
        Map<String, Integer> registers,
        TaskState taskState
) {
    public TaskContext {
        registers = Map.copyOf(registers);
    }
}