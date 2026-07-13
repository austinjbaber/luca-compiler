package codegen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builds VM instructions with symbolic labels, then serializes relative offsets. */
final class VmEmitter {
    static final class Label {
        private Integer position;
    }

    private static final class Instruction {
        final int opcode;
        final int position;
        final Object[] operands;

        Instruction(int opcode, int position, Object[] operands) {
            this.opcode = opcode;
            this.position = position;
            this.operands = operands;
        }
    }

    private final List<Instruction> instructions = new ArrayList<Instruction>();
    private final LinkedHashMap<String, Integer> strings = new LinkedHashMap<String, Integer>();
    private int position;
    private int globalSize;

    Label label() {
        return new Label();
    }

    void mark(Label label) {
        if (label.position != null) {
            throw new IllegalStateException("VM label marked twice");
        }
        label.position = Integer.valueOf(position);
    }

    void emit(int opcode, Object... operands) {
        instructions.add(new Instruction(opcode, position, operands));
        position += Opcode.size(opcode);
    }

    int internString(String value) {
        Integer existing = strings.get(value);
        if (existing != null) {
            return existing.intValue();
        }
        int index = strings.size();
        strings.put(value, Integer.valueOf(index));
        return index;
    }

    void setGlobalSize(int globalSize) {
        this.globalSize = globalSize;
    }

    String serialize() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Integer> entry : strings.entrySet()) {
            String value = entry.getKey();
            if (value.indexOf('"') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
                throw new IllegalArgumentException("String literal cannot be represented in VM text");
            }
            result.append(Opcode.DEF_STRING).append(' ')
                  .append(entry.getValue()).append(" \"").append(value).append("\"\n");
        }
        result.append(Opcode.DEF_GLOBALS).append(' ').append(globalSize).append('\n');

        for (Instruction instruction : instructions) {
            result.append(instruction.opcode);
            for (Object operand : instruction.operands) {
                result.append(' ');
                if (operand instanceof Label) {
                    Label label = (Label) operand;
                    if (label.position == null) {
                        throw new IllegalStateException("Unresolved VM label");
                    }
                    result.append(label.position.intValue() - instruction.position);
                } else {
                    result.append(operand);
                }
            }
            result.append('\n');
        }
        return result.toString();
    }
}
