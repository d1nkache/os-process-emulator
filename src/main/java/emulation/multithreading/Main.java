package emulation.multithreading;

import emulation.multithreading.Machine.VirtualMachine;
import emulation.multithreading.Tasks.Core.TaskStruct;

public class Main {
    public static void main(String[] args) {
        VirtualMachine virtualMachine = new VirtualMachine();
        virtualMachine.init();

        int processId = virtualMachine.createProcessFromProgram("demo-math", 256);

        virtualMachine.run();

        printThreadGroupResult(virtualMachine, processId, "demo-math");
    }

    private static void printThreadGroupResult(VirtualMachine virtualMachine, int threadGroupId, String programName) {
        System.out.println(programName + " -> threadGroupId=" + threadGroupId);

        for (TaskStruct task : virtualMachine.getTasksByThreadGroupId(threadGroupId)) {
            String taskKind = task.isProcess() ? "process" : "thread";
            System.out.println(
                    taskKind
                            + " pid="
                            + task.getPid()
                            + ", state="
                            + task.getTaskState()
                            + ", registers="
                            + task.getRegisters()
            );
        }

        if (virtualMachine.getTasksByThreadGroupId(threadGroupId).isEmpty()) {
            System.out.println("No tasks found for thread group: " + threadGroupId);
            return;
        }
    }
}