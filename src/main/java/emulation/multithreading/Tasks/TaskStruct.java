package emulation.multithreading.Tasks;

import emulation.multithreading.Memory.Core.Heap;
import emulation.multithreading.Memory.Core.Segment;
import emulation.multithreading.Memory.Core.SegmentBuilder;
import emulation.multithreading.Memory.Core.SegmentReader;
import emulation.multithreading.Tasks.TransferObjects.TaskContext;

import lombok.Getter;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

@Getter
public class TaskStruct {
    private final int pid;
    private final int threadGroupId;

    private final List<String> code;
    private int instructionPointer;

    private final Heap memory;
    private final HashMap<String, Integer> registers;

    private TaskState taskState;

    public TaskStruct(
            int pid,
            int threadGroupId,
            List<String> code,
            Heap memory
    ) {
        this.pid = pid;
        this.threadGroupId = threadGroupId;
        this.code = code;
        this.instructionPointer = 0;
        this.memory = memory;
        this.registers = new HashMap<>();
        this.taskState = TaskState.RUNNABLE;
    }

    // ################################# Instructions #################################
    @Nullable
    public String fetchNextInstruction() {
        if (this.taskState != TaskState.RUNNABLE) {
            return null;
        }

        if (this.instructionPointer >= this.code.size()) {
            this.terminate();
            return null;
        }

        String fetchedInstruction = this.code.get(this.instructionPointer);
        this.instructionPointer += 1;

        return fetchedInstruction;
    }

    // ################################# Memory Allocation #################################
    public Segment allocateNewSegment(int size, String name) {
        if (!this.isProcess()) {
            throw new IllegalStateException("Only process can allocate memory");
        }

        return this.memory.allocateSegment(size, this.pid, name);
    }

    public Segment allocateNewSegment(String name) {
        if (!this.isProcess()) {
            throw new IllegalStateException("Only process can allocate memory");
        }

        return this.memory.allocateSegment(this.pid, name);
    }

    // ################################# Memory Access #################################
    public SegmentReader beginParse(int fromAddress) {
        Segment segment = this.memory.getSegmentByAddress(fromAddress);

        if (segment == null) {
            throw new IllegalArgumentException("No segment at address: " + fromAddress);
        }

        return SegmentReader.beginParse(segment);
    }

    public SegmentBuilder beginBuild(int fromAddress) {
        Segment segment = this.memory.getSegmentByAddress(fromAddress);

        if (segment == null) {
            throw new IllegalArgumentException("No segment at address: " + fromAddress);
        }

        return SegmentBuilder.begin(segment);
    }

    // ################################# Task State #################################
    public void start() {
        this.taskState = TaskState.RUNNABLE;
    }

    public void block() {
        this.taskState = TaskState.WAITING;
    }

    public void unblock() {
        if (this.taskState == TaskState.WAITING) {
            this.taskState = TaskState.RUNNABLE;
        }
    }

    public void terminate() {
        this.taskState = TaskState.ZOMBIE;
    }

    // ################################# Context #################################
    public TaskContext saveContext() {
        return new TaskContext(
                this.pid,
                this.threadGroupId,
                this.instructionPointer,
                this.registers,
                this.taskState
        );
    }

    public void restoreContext(TaskContext context) {
        if (this.pid != context.pid()) {
            throw new IllegalArgumentException("Cannot restore context of another task");
        }

        if (this.threadGroupId != context.threadGroupId()) {
            throw new IllegalArgumentException("Cannot restore context from another thread group");
        }

        this.instructionPointer = context.instructionPointer();

        this.registers.clear();
        this.registers.putAll(context.registers());

        this.taskState = context.taskState();
    }

    // ################################# Registers #################################
    @Nullable
    public Integer getRegisterValue(String name) {
        return this.registers.get(name);
    }

    public void setRegisterValue(String name, int value) {
        this.registers.put(name, value);
    }

    // ################################# Helpers #################################
    public boolean isProcess() {
        return this.threadGroupId == this.pid;
    }
}