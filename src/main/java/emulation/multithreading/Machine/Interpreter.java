package emulation.multithreading.Machine;

import emulation.multithreading.Tasks.Core.TaskStruct;
import emulation.multithreading.Memory.Core.SegmentBuilder;
import emulation.multithreading.Memory.Core.SegmentReader;


/*
    INTERPRETER INSTRUCTIONS
    OPERATION NAME: save  ;      OPERANDS: n, v;     RESULT: TaskStruct.registers.put(a)
    OPERATION NAME: sum   ;      OPERANDS: c, a, b;  RESULT: save(c = a + b)
    OPERATION NAME: min   ;      OPERANDS: c, a, b;  RESULT: save(c = a - b)
    OPERATION NAME: get   ;      OPERANDS: n   ;     RESULT: TaskStruct.registers.get(n)
    OPERATION NAME: start ;      OPERANDS: a   ;     RESULT: TaskStruct.beginBuild(a)
    OPERATION NAME: parse ;      OPERANDS: a   ;     RESULT: TaskStruct.beginParse(a)
    OPERATION NAME: rvalue;      OPERANDS: r, s;     RESULT: parse.readInt + {s}
    OPERATION NAME: wvalue;      OPERANDS: r, s, v;  RESULT: v = parse.writeInt + {s}(v)
 */
public class Interpreter {
    private SegmentReader currentReader;
    private SegmentBuilder currentBuilder;

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
                requireArgs(tokens, 3);

                String target = tokens[1];
                int operandA = getRegisterOrLiteral(task, tokens[2]);
                int operandB = getRegisterOrLiteral(task, tokens[3]);

                task.setRegisterValue(target, operandA - operandB);
                yield ExecutionResult.CONTINUE;
            }
            case "start" -> {
                requireArgs(tokens, 2);
                yield ExecutionResult.CONTINUE;
            }
            case "parse" -> {
                requireArgs(tokens, 2);

                int address = getRegisterOrLiteral(task, tokens[1]);
                this.currentBuilder = task.beginBuild(address);

                yield ExecutionResult.CONTINUE;
            }
            case "rvalue" -> {
                requireArgs(tokens, 2);

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
                requireArgs(tokens, 2);

                if (this.currentReader == null) {
                    throw new IllegalStateException("Reader is not initialized. Use parse first.");
                }

                int size = Integer.parseInt(tokens[1]);
                int toInsert = Integer.parseInt(tokens[2]);

                switch (size) {
                    case 8  -> this.currentBuilder.writeInt8(toInsert);
                    case 32 -> this.currentBuilder.writeInt32(toInsert);
                    default -> throw new IllegalArgumentException("Unexpected value: " + size);
                };

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