package emulation.multithreading.Managment;

import lombok.Getter;
import emulation.multithreading.Tasks.Core.TaskState;
import emulation.multithreading.Tasks.Core.TaskStruct;

import java.util.Comparator;
import java.util.PriorityQueue;


@Getter
public class Scheduler {
    private TaskStruct currentTask;

    private final PriorityQueue<TaskStruct> runQueue =
            new PriorityQueue<>(Comparator.comparingLong(TaskStruct::getVirtualRuntime));

    // ################################# Operations #################################
    public boolean schedule() {
        if (this.currentTask != null && this.currentTask.getTaskState() == TaskState.RUNNABLE) {
            this.runQueue.add(this.currentTask);
        }

        this.currentTask = this.runQueue.poll();

        return this.currentTask != null;
    }

    public boolean addTask(TaskStruct task) {
        task.start();
        return this.runQueue.add(task);
    }

    // ################################### Task State #################################
    public void yieldCurrent() {
        if (this.currentTask == null) return;

        if (this.currentTask.getTaskState() == TaskState.RUNNABLE) {
            this.runQueue.add(this.currentTask);
        }

        this.currentTask = null;
    }

    public void blockCurrent() {
        if (this.currentTask != null) {
            this.currentTask.block();
            this.currentTask = null;
        }
    }

    public void terminateCurrent() {
        if (this.currentTask != null) {
            this.currentTask.terminate();
            this.currentTask = null;
        }
    }
}