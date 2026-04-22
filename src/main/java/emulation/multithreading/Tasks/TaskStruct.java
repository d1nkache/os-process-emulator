package emulation.multithreading.Tasks;

import emulation.multithreading.Memory.Core.Segment;
import lombok.Getter;

import org.jetbrains.annotations.Nullable;

import emulation.multithreading.Memory.Core.Heap;

import java.util.List;
import java.util.HashMap;


@Getter
public class TaskStruct {
    private final int pid;
    private final int tgid;

    private final List<String> code;
    private int instructionPointer;

    private final Heap memory;
    private final HashMap<String, Integer> registers;

    private TaskState taskState;


    public TaskStruct(
            int pid,
            int tgid,
            List<String> code,
            Heap memory,
            int heapMaxSize
    ) {
        this.pid = pid;
        this.tgid = tgid;
        this.code = code;
        this.instructionPointer = 0;
        this.memory = memory;
        this.registers = new HashMap<String, Integer>();
        this.taskState = TaskState.RUNNABLE;
    }

    // ################################# Operations #################################
    @Nullable
    public String fetchNextInstruction() {
        if (this.instructionPointer >= this.code.size()) {
            this.taskState = TaskState.ZOMBIE;

            return null;
        }

        if (this.taskState != TaskState.RUNNABLE) {
            return null;
        }

        String fetchedInstruction = this.code.get(this.instructionPointer);
        this.instructionPointer += 1;

        return fetchedInstruction;
    }

    @Nullable
    public Integer readValueInt32(int fromAddress, int startIndex) {
        Segment segment = this.memory.getSegmentByAddress(fromAddress);

        if (segment == null) {
            return null;
        }

        return segment.readInt32(startIndex);
    }

    @Nullable
    public Integer readValueInt8(int fromAddress, int startIndex) {
        Segment segment = this.memory.getSegmentByAddress(fromAddress);

        if (segment == null) {
            return null;
        }

        return segment.readInt8(startIndex);
    }

    public Boolean writeValueInt32(int fromAddress, int startIndex, int value) {
        Segment segment = this.memory.getSegmentByAddress(fromAddress);

        if (segment == null) {
            return null;
        }

        return segment.writeInt32(startIndex, value);
    }

    public Boolean writeValueInt8(int fromAddress, int startIndex, int value) {
        Segment segment = this.memory.getSegmentByAddress(fromAddress);

        if (segment == null) {
            return null;
        }

        return segment.writeInt8(startIndex, value);
    }

    public void start() {
        this.taskState = TaskState.RUNNABLE;
    }

    public void block() {
        this.taskState = TaskState.WAITING;
    }

    public void unblock() {
        if (taskState == TaskState.WAITING) {
            this.taskState = TaskState.RUNNABLE;
        }
    }

    public void terminate() {
        this.taskState = TaskState.ZOMBIE;
    }

    // ################################# GETTERS AND SETTERS #################################
    public boolean isProcess() {
        return this.tgid == this.pid;
    }

    @Nullable
    public Integer getRegisterValue(String name) {
        return this.registers.get(name);
    }

    public void setRegisterValue(String name, int value) { this.registers.put(name, value); }
}