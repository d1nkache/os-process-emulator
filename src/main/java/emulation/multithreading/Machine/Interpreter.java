package emulation.multithreading.Machine;

import emulation.multithreading.Tasks.Core.TaskStruct;
import emulation.multithreading.Memory.Core.SegmentReader;
import emulation.multithreading.Memory.Core.SegmentBuilder;

import java.util.ArrayList;
import java.util.List;


/*
    INTERPRETER INSTRUCTIONS
    OPERATION NAME: save        OPERANDS: n, v         RESULT: TaskStruct.registers.put(n, v)
    OPERATION NAME: sum         OPERANDS: c, a, b      RESULT: save(c, a + b)
    OPERATION NAME: min         OPERANDS: c, a, b      RESULT: save(c, a - b)
    OPERATION NAME: get         OPERANDS: n            RESULT: TaskStruct.registers.get(n)
    OPERATION NAME: start       OPERANDS: a            RESULT: TaskStruct.beginBuild(a)
    OPERATION NAME: parse       OPERANDS: a            RESULT: TaskStruct.beginParse(a)
    OPERATION NAME: rvalue      OPERANDS: r, s         RESULT: reader.readInt + {s}
    OPERATION NAME: wvalue      OPERANDS: r, s, v      RESULT: builder.writeInt + {s}(v)
    OPERATION NAME: nthread     OPERANDS: c, p, l      RESULT: virtualMachine.createThread(p, l)
 */
public class Interpreter {
    private SegmentReader currentReader;
    private SegmentBuilder currentBuilder;

    private final VirtualMachine virtualMachine;

    public Interpreter(VirtualMachine virtualMachine) {
        this.virtualMachine = virtualMachine;
    }

    public ExecutionResult execute(TaskStruct task, String instruction) {
        if (instruction.isEmpty()) return ExecutionResult.CONTINUE;;

        String[] tokens = instruction.split("\\s+");
        String command = tokens[0];

        return switch (command) {
            case "save" -> {
                requireArgs(tokens, 3);

                String name = tokens[1];
                int value = Integer.parseInt(tokens[2]);

                task.setRegisterValue(name, value);
                yield ExecutionResult.CONTINUE;
            }
            case "sum" -> {
                requireArgs(tokens, 4);

                String target = tokens[1];
                int operandA = getRegisterOrLiteral(task, tokens[2]);
                int operandB = getRegisterOrLiteral(task, tokens[3]);

                task.setRegisterValue(target, operandA + operandB);

                yield ExecutionResult.CONTINUE;
            }
            case "minus" -> {
                requireArgs(tokens, 4);

                String target = tokens[1];
                int operandA = getRegisterOrLiteral(task, tokens[2]);
                int operandB = getRegisterOrLiteral(task, tokens[3]);

                task.setRegisterValue(target, operandA - operandB);
                yield ExecutionResult.CONTINUE;
            }
            case "start" -> {
                requireArgs(tokens, 2);

                int address = getRegisterOrLiteral(task, tokens[1]);
                this.currentBuilder = task.beginBuild(address);

                yield ExecutionResult.CONTINUE;
            }
            case "parse" -> {
                requireArgs(tokens, 2);

                int address = getRegisterOrLiteral(task, tokens[1]);
                this.currentReader = task.beginParse(address);

                yield ExecutionResult.CONTINUE;
            }
            case "rvalue" -> {
                requireArgs(tokens, 3);

                if (this.currentReader == null) {
                    throw new IllegalStateException("Reader is not initialized. Use parse first.");
                }

                String target = tokens[1];
                int size = Integer.parseInt(tokens[2]);

                int value = switch (size) {
                    case 8  -> this.currentReader.readInt8();
                    case 32 -> this.currentReader.readInt32();
                    default -> throw new IllegalArgumentException("Unexpected value: " + size);
                };

                task.setRegisterValue(target, value);
                yield ExecutionResult.CONTINUE;
            }
            case "wvalue" -> {
                requireArgs(tokens, 3);

                if (this.currentBuilder == null) {
                    throw new IllegalStateException("Builder is not initialized. Use start first.");
                }

                int size = Integer.parseInt(tokens[1]);
                int toInsert = getRegisterOrLiteral(task, tokens[2]);

                switch (size) {
                    case 8  -> this.currentBuilder.writeInt8(toInsert);
                    case 32 -> this.currentBuilder.writeInt32(toInsert);
                    default -> throw new IllegalArgumentException("Unexpected value: " + size);
                };

                yield ExecutionResult.CONTINUE;
            }
            case "nthread" -> {
                requireArgs(tokens, 4);

                String targetRegister = tokens[1];
                int line = getRegisterOrLiteral(task, tokens[3]);
                int programId = getRegisterOrLiteral(task, tokens[2]);

                List<String> program = this.virtualMachine.getProgramById(programId);
                if (program == null) {
                    throw new NullPointerException();
                }

                if (line < 0 || line >= program.size()) {
                    throw new IllegalArgumentException("Invalid start line: " + line);
                }

                List<String> threadCode = new ArrayList<>(program.subList(line, program.size()));
                TaskStruct thread = this.virtualMachine.createThreadFor(task, threadCode);

                task.setRegisterValue(targetRegister, thread.getPid());
                yield ExecutionResult.CONTINUE;
            }

            case "yield"     -> { yield ExecutionResult.YIELD; }
            case "block"     -> { yield ExecutionResult.BLOCK; }
            case "terminate" -> { yield ExecutionResult.TERMINATE; }
            default -> {
                throw new IllegalArgumentException("Unknown operation: " + command);
            }
        };
    }

    private int getRegisterOrLiteral(TaskStruct task, String token) {
        Integer registerValue = task.getRegisterValue(token);
        if (registerValue != null) {
            return registerValue;
        }

        return Integer.parseInt(token);
    }

    private void requireArgs(String[] tokens, int expected) {
        if (tokens.length != expected) {
            throw new IllegalArgumentException(
                    "Invalid arguments count for operation '"
                            + tokens[0]
                            + "'. Expected: "
                            + (expected - 1)
                            + ", got: " + (tokens.length - 1)
            );
        }
    }
}
