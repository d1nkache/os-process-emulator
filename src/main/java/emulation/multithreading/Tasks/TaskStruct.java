package emulation.multithreading.Tasks;

import emulation.multithreading.Memory.Core.AddressSpace;
import emulation.multithreading.Tasks.TaskState;

import java.util.HashMap;


public class TaskStruct {
    private int pid;
    private int tgid;

    private int instructionPointer;
    /*
    В регистрах мы будем просто хранить переменные
    (например, A = 10, B = 20)
    */
    private HashMap<String, Integer> registers;
    private AddressSpace memory;
    private TaskState taskState;

}
