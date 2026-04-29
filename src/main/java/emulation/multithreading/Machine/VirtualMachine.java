package emulation.multithreading.Machine;

import emulation.multithreading.Managment.Scheduler;
import emulation.multithreading.Memory.Core.Heap;
import emulation.multithreading.Tasks.Core.TaskStruct;

import java.util.List;


public class VirtualMachine {
    private int pidCounter;
    private final Interpretator interpretator;
    private final Scheduler scheduler;

    public VirtualMachine() {
        this.pidCounter = 0;
        this.scheduler = new Scheduler();
        this.interpretator = new Interpretator();
    }

    public int createProcess(List<String> code, int heapSize) {
        int pid = this.pidCounter++;
        int threadGroupId = pid;

        TaskStruct process = new TaskStruct(
                pid,
                threadGroupId,
                code,
                new Heap(heapSize)
        );

        process.allocateNewSegment(String.valueOf(process.getPid()));
        this.scheduler.addTask(process);

        return threadGroupId;
    }

    public TaskStruct createThread(TaskStruct process, List<String> code) {
        int pid = this.pidCounter++;

        TaskStruct thread = new TaskStruct(
                pid,
                process.getThreadGroupId(),
                code,
                process.getMemory()
        );

        this.scheduler.addTask(thread);
        return thread;
    }

    public void run() {
        while (scheduler.schedule()) {
            TaskStruct task = this.scheduler.getCurrentTask();
            String instruction = task.fetchNextInstruction();

            if (instruction == null) {
                this.scheduler.terminateCurrent();
                continue;
            }

            this.interpretator.execute(task, instruction);
            task.incrementVirtualRuntime();
        }
    }
}
