package emulation.multithreading.Tasks;

import lombok.Getter;

import emulation.multithreading.Memory.Core.Segment;
import emulation.multithreading.Memory.Core.SegmentReader;
import emulation.multithreading.Memory.Core.SegmentBuilder;

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

    public Segment allocateNewSegment(int size, String name) {
        if (this.pid == this.tgid) {
            return this.memory.allocateSegment(size, this.pid, name);
        }

        throw new RuntimeException("Only process could allocate memory");
    }

    public Segment allocateNewSegment(String name) {
        if (this.pid == this.tgid) {
            return this.memory.allocateSegment(this.pid, name);
        }

        throw new RuntimeException("Only process could allocate memory");
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
    public SegmentReader beginParse(int fromAddress) {
        Segment segment = this.memory.getSegmentByAddress(fromAddress);

        if (segment == null) {
            throw new RuntimeException("No such segment");
        }

        return SegmentReader.beginParse(segment);
    }

    public SegmentBuilder begin(int fromAddress) {
        Segment segment = this.memory.getSegmentByAddress(fromAddress);

        if (segment == null) {
            throw new RuntimeException("No such segment");
        }

        return SegmentBuilder.begin(segment);
    }

    public boolean isProcess() {
        return this.tgid == this.pid;
    }

    @Nullable
    public Integer getRegisterValue(String name) {
        return this.registers.get(name);
    }

    public void setRegisterValue(String name, int value) { this.registers.put(name, value); }
}