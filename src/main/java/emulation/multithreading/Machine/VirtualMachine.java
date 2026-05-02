package emulation.multithreading.Machine;

import java.io.IOException;
import java.io.UncheckedIOException;

import java.nio.file.Path;
import java.nio.file.Files;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;

import emulation.multithreading.Memory.Core.Heap;
import emulation.multithreading.Managment.Scheduler;
import emulation.multithreading.Tasks.Core.TaskStruct;

import org.jetbrains.annotations.Nullable;


public class VirtualMachine {
    private static final Path RESOURCES_PROGRAMS_DIRECTORY = Path.of("src", "main", "resources", "programs");

    private int pidCounter;
    private final HashMap<Integer, TaskStruct> tasks;
    private final HashMap<Integer, List<String>> programs;
    private final HashMap<String, Integer> programIdsByName;

    private final Scheduler scheduler;
    private final Interpreter interpreter;

    public void init() {
        this.programs.clear();
        this.programIdsByName.clear();

        Path programsDirectory = resolveProgramsDirectory();
        if (programsDirectory == null) {
            return;
        }

        try (var files = Files.walk(programsDirectory)) {
            List<Path> programFiles = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".dinka"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();

            int programId = 0;
            for (Path programFile : programFiles) {
                List<String> instructions = Files.readAllLines(programFile).stream()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .filter(line -> !line.startsWith("#"))
                        .filter(line -> !line.startsWith("//"))
                        .toList();

                String fileName = programFile.getFileName().toString();
                String programName = fileName.substring(0, fileName.length() - ".dinka".length());

                this.programs.put(programId, instructions);
                this.programIdsByName.put(programName, programId);
                programId += 1;
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to load .dinka programs", exception);
        }
    }

    public VirtualMachine() {
        this.pidCounter = 0;
        this.scheduler = new Scheduler();

        this.tasks = new HashMap<>();
        this.programs = new HashMap<>();
        this.programIdsByName = new HashMap<>();
        this.interpreter = new Interpreter(this);
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
        this.tasks.put(process.getPid(), process);

        return threadGroupId;
    }

    public int createProcessFromProgram(int programId, int heapSize) {
        List<String> program = this.programs.get(programId);
        if (program == null) {
            throw new IllegalArgumentException("Program not found: " + programId);
        }

        return createProcess(program, heapSize);
    }

    public int createProcessFromProgram(String programName, int heapSize) {
        Integer programId = this.programIdsByName.get(programName);
        if (programId == null) {
            throw new IllegalArgumentException("Program not found: " + programName);
        }

        return createProcessFromProgram(programId, heapSize);
    }

    public TaskStruct createThread(TaskStruct process, List<String> code) {
        if (!process.isProcess()) {
            throw new IllegalArgumentException("Process is not the process");
        }

        int pid = this.pidCounter++;
        TaskStruct thread = new TaskStruct(
                pid,
                process.getThreadGroupId(),
                code,
                process.getMemory()
        );

        this.scheduler.addTask(thread);
        this.tasks.put(thread.getPid(), thread);

        return thread;
    }

    public TaskStruct createThreadFor(TaskStruct caller, List<String> code) {
        TaskStruct process = getProcessLeader(caller);
        return createThread(process, code);
    }

    @Nullable
    public TaskStruct getTaskById(int taskId) {
        return this.tasks.get(taskId);
    }

    @Nullable
    public List<String> getProgramById(int programId) {
        return this.programs.get(programId);
    }

    @Nullable
    public Integer getProgramIdByName(String programName) {
        return this.programIdsByName.get(programName);
    }

    public List<TaskStruct> getTasksByThreadGroupId(int threadGroupId) {
        List<TaskStruct> threadGroupTasks = new ArrayList<>();

        for (TaskStruct task : this.tasks.values()) {
            if (task.getThreadGroupId() == threadGroupId) {
                threadGroupTasks.add(task);
            }
        }

        threadGroupTasks.sort(Comparator.comparingInt(TaskStruct::getPid));
        return threadGroupTasks;
    }

    private TaskStruct getProcessLeader(TaskStruct task) {
        TaskStruct leader = this.tasks.get(task.getThreadGroupId());

        if (leader == null) {
            throw new IllegalStateException("Process leader not found: " + task.getThreadGroupId());
        }

        return leader;
    }

    public void run() {
        while (scheduler.schedule()) {
            TaskStruct task = this.scheduler.getCurrentTask();
            if (task == null) {
                continue;
            }

            String instruction = task.fetchNextInstruction();
            if (instruction == null) {
                this.scheduler.terminateCurrent();
                continue;
            }

            ExecutionResult result = this.interpreter.execute(task, instruction);
            switch (result) {
                case CONTINUE, YIELD -> this.scheduler.yieldCurrent();
                case BLOCK -> this.scheduler.blockCurrent();
                case TERMINATE -> this.scheduler.terminateCurrent();
            }
            task.incrementVirtualRuntime();
        }
    }

    @Nullable
    private Path resolveProgramsDirectory() {
        if (Files.exists(RESOURCES_PROGRAMS_DIRECTORY)) {
            return RESOURCES_PROGRAMS_DIRECTORY;
        }

        return null;
    }
}
