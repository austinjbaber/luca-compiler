package codegen;

import ast.*;
import lexer.Token;
import sym.*;
import java.util.*;

/** Lowers a semantically annotated Luca AST to the textual stack-VM format */
public final class CodeGenerator {
    private static final int WORD_SIZE = 8;

    private enum Area { GLOBAL, LOCAL, FORMAL }

    private static final class Storage {
        final Area area;
        final int offset;

        Storage(Area area, int offset) {
            this.area = area;
            this.offset = offset;
        }
    }

    private VmEmitter out;
    private final Map<VariableSy, Storage> storage = new IdentityHashMap<VariableSy, Storage>();
    private final Map<ProcedureSy, VmEmitter.Label> procedures =
        new IdentityHashMap<ProcedureSy, VmEmitter.Label>();
    private final Deque<VmEmitter.Label> loopExits = new ArrayDeque<VmEmitter.Label>();
    private int globalSize;

    // generates a complete stack-VM program from annotated AST
    public String generate(PROGRAM program) {
        reset();
        layoutTypes(program.decls);
        layoutGlobals(program.decls);
        collectProcedures(program.decls);
        layoutProcedures(program.decls);

        out.setGlobalSize(globalSize);
        emitProcedures(program.decls);
        out.emit(Opcode.PROG_BEGIN);
        emitStatements(program.stats);
        out.emit(Opcode.PROG_END);
        return out.serialize();
    }

    private void reset() {
        out = new VmEmitter();
        storage.clear();
        procedures.clear();
        loopExits.clear();
        globalSize = 0;
    }

    // computes sizes and field offsets before variables that use these types are laid out
    private void layoutTypes(DECLS decls) {
        for (DECLS cursor = decls; !(cursor instanceof DECLNULL); cursor = cursor.right) {
            DECLARATION declaration = cursor.left;
            if (declaration instanceof ARRAYDECL) {
                ArrayType type = requireSymbol(declaration, ArrayType.class);
                type.SetSize(type.GetArrayCount() * sizeOf(type.GetArrayElementType()));
            } else if (declaration instanceof RECORDDECL) {
                layoutRecord((RECORDDECL) declaration);
            }
        }
    }

    // assigns each record field its byte offset and derives the record's total size
    private void layoutRecord(RECORDDECL declaration) {
        RecordType record = requireSymbol(declaration, RecordType.class);
        int offset = 0;
        for (DECLS cursor = declaration.fields; !(cursor instanceof DECLNULL); cursor = cursor.right) {
            FieldSy field = requireSymbol(cursor.left, FieldSy.class);
            int size = sizeOf(field.GetType());
            field.SetOffset(offset);
            field.SetSize(size);
            offset += size;
        }
        record.SetFieldSize(offset);
        record.SetSize(offset);
    }

    // reserves consecutive global-memory slots for variable declarations
    private void layoutGlobals(DECLS decls) {
        for (DECLS cursor = decls; !(cursor instanceof DECLNULL); cursor = cursor.right) {
            if (cursor.left instanceof VARDECL) {
                VariableSy variable = requireSymbol(cursor.left, VariableSy.class);
                int size = sizeOf(variable.GetType());
                variable.SetOffset(globalSize);
                variable.SetSize(size);
                storage.put(variable, new Storage(Area.GLOBAL, globalSize));
                globalSize += size;
            }
        }
    }

    // allocates labels for every procedure before any body is emitted, allowing calls to
    // procedures declared later or in nested declaration lists
    private void collectProcedures(DECLS decls) {
        for (DECLS cursor = decls; !(cursor instanceof DECLNULL); cursor = cursor.right) {
            if (cursor.left instanceof PROCDECL) {
                PROCDECL declaration = (PROCDECL) cursor.left;
                ProcedureSy procedure = requireSymbol(declaration, ProcedureSy.class);
                procedures.put(procedure, out.label());
                collectProcedures(declaration.decls);
            }
        }
    }

    // lays out procedure formals and locals independently of global data area
    private void layoutProcedures(DECLS decls) {
        for (DECLS cursor = decls; !(cursor instanceof DECLNULL); cursor = cursor.right) {
            if (cursor.left instanceof PROCDECL) {
                PROCDECL declaration = (PROCDECL) cursor.left;
                ProcedureSy procedure = requireSymbol(declaration, ProcedureSy.class);
                layoutTypes(declaration.decls);

                int formalOffset = 0;
                // formals always occupy one stack word, aggregates passed by address
                for (DECLS formalCursor = declaration.formals;
                     !(formalCursor instanceof DECLNULL);
                     formalCursor = formalCursor.right) {
                    FormalSy formal = requireSymbol(formalCursor.left, FormalSy.class);
                    formal.SetOffset(formalOffset);
                    formal.SetSize(sizeOf(formal.GetType()));
                    storage.put(formal, new Storage(Area.FORMAL, formalOffset));
                    formalOffset += WORD_SIZE;
                }
                procedure.SetFormalSize(formalOffset);

                int localOffset = 0;
                for (DECLS localCursor = declaration.decls;
                     !(localCursor instanceof DECLNULL);
                     localCursor = localCursor.right) {
                    if (localCursor.left instanceof VARDECL) {
                        VariableSy local = requireSymbol(localCursor.left, VariableSy.class);
                        int size = sizeOf(local.GetType());
                        local.SetOffset(localOffset);
                        local.SetSize(size);
                        storage.put(local, new Storage(Area.LOCAL, localOffset));
                        localOffset += size;
                    }
                }
                procedure.SetLocalSize(localOffset);
                layoutProcedures(declaration.decls);
            }
        }
    }

    // returns the storage required by a value of {@code type}, updating derived type sizes
    private int sizeOf(TypeSy type) {
        if (type instanceof ArrayType) {
            ArrayType array = (ArrayType) type;
            int size = array.GetArrayCount() * sizeOf(array.GetArrayElementType());
            array.SetSize(size);
            return size;
        }
        if (type instanceof RecordType) {
            return type.GetSize();
        }
        if (type instanceof RefType) {
            type.SetSize(WORD_SIZE);
            return WORD_SIZE;
        }
        if (type == Standard.NoType) {
            throw new IllegalArgumentException("Cannot lay out an unresolved type");
        }
        type.SetSize(WORD_SIZE);
        return WORD_SIZE;
    }

    // emits procedure bodies before the main program body
    private void emitProcedures(DECLS decls) {
        for (DECLS cursor = decls; !(cursor instanceof DECLNULL); cursor = cursor.right) {
            if (cursor.left instanceof PROCDECL) {
                PROCDECL declaration = (PROCDECL) cursor.left;
                ProcedureSy procedure = requireSymbol(declaration, ProcedureSy.class);
                out.mark(procedures.get(procedure));
                out.emit(Opcode.PROC_BEGIN, procedure.GetFormalSize(), procedure.GetLocalSize());
                emitStatements(declaration.stats);
                out.emit(Opcode.PROC_END, procedure.GetFormalSize(), procedure.GetLocalSize());
                emitProcedures(declaration.decls);
            }
        }
    }

    private void emitStatements(STATS statements) {
        for (STATS cursor = statements; !(cursor instanceof STATNULL); cursor = cursor.right) {
            emitStatement(cursor.left);
        }
    }

    // dispatches a statement to its VM control-flow or data-movement sequence
    private void emitStatement(STATEMENT statement) {
        if (statement instanceof ASSIGN) {
            ASSIGN assign = (ASSIGN) statement;
            emitAddress(assign.left);
            emitValue(assign.right);
            out.emit(isReal(assign.left.type) ? Opcode.STORE_F : Opcode.STORE_I);
        } else if (statement instanceof WRITE) {
            WRITE write = (WRITE) statement;
            emitValue(write.expr);
            if (isReal(write.expr.type)) {
                out.emit(Opcode.WRITE_F);
            } else if (write.expr.type == Standard.CharType) {
                out.emit(Opcode.WRITE_C);
            } else if (write.expr.type == Standard.StringType) {
                out.emit(Opcode.WRITE_S);
            } else {
                out.emit(Opcode.WRITE_I);
            }
        } else if (statement instanceof WRITELN) {
            out.emit(Opcode.WRITELN);
        } else if (statement instanceof READ) {
            READ read = (READ) statement;
            emitAddress(read.des);
            if (isReal(read.des.type)) {
                out.emit(Opcode.READ_F);
            } else if (read.des.type == Standard.CharType) {
                out.emit(Opcode.READ_C);
            } else {
                out.emit(Opcode.READ_I);
            }
        } else if (statement instanceof PROCCALL) {
            emitCall((PROCCALL) statement);
        } else if (statement instanceof IF1) {
            IF1 conditional = (IF1) statement;
            VmEmitter.Label thenLabel = out.label();
            VmEmitter.Label endLabel = out.label();
            emitCondition(conditional.expr, thenLabel, endLabel);
            out.mark(thenLabel);
            emitStatements(conditional.then_);
            out.mark(endLabel);
        } else if (statement instanceof IF2) {
            IF2 conditional = (IF2) statement;
            VmEmitter.Label thenLabel = out.label();
            VmEmitter.Label elseLabel = out.label();
            VmEmitter.Label endLabel = out.label();
            emitCondition(conditional.expr, thenLabel, elseLabel);
            out.mark(thenLabel);
            emitStatements(conditional.then_);
            out.emit(Opcode.JUMP, endLabel);
            out.mark(elseLabel);
            emitStatements(conditional.else_);
            out.mark(endLabel);
        } else if (statement instanceof WHILE) {
            WHILE loop = (WHILE) statement;
            VmEmitter.Label testLabel = out.label();
            VmEmitter.Label bodyLabel = out.label();
            VmEmitter.Label endLabel = out.label();
            out.mark(testLabel);
            emitCondition(loop.expr, bodyLabel, endLabel);
            out.mark(bodyLabel);
            emitStatements(loop.stats);
            out.emit(Opcode.JUMP, testLabel);
            out.mark(endLabel);
        } else if (statement instanceof REPEAT) {
            REPEAT loop = (REPEAT) statement;
            VmEmitter.Label bodyLabel = out.label();
            VmEmitter.Label endLabel = out.label();
            out.mark(bodyLabel);
            emitStatements(loop.stats);
            emitCondition(loop.expr, endLabel, bodyLabel);
            out.mark(endLabel);
        } else if (statement instanceof LOOP) {
            LOOP loop = (LOOP) statement;
            VmEmitter.Label bodyLabel = out.label();
            VmEmitter.Label endLabel = out.label();
            out.mark(bodyLabel);
            loopExits.push(endLabel);
            emitStatements(loop.stats);
            loopExits.pop();
            out.emit(Opcode.JUMP, bodyLabel);
            out.mark(endLabel);
        } else if (statement instanceof EXIT) {
            if (loopExits.isEmpty()) {
                throw new IllegalArgumentException("EXIT emitted outside LOOP");
            }
            out.emit(Opcode.JUMP, loopExits.peek());
        } else if (statement instanceof BLOCK) {
            emitStatement(((BLOCK) statement).stats);
        } else {
            throw unsupported(statement);
        }
    }

    // emits actual arguments followed by a call to the preallocated procedure label
    private void emitCall(PROCCALL call) {
        if (!(call.des instanceof VARREF)) {
            throw new IllegalArgumentException("Procedure call requires a simple name");
        }
        VARREF reference = (VARREF) call.des;
        Symbol symbol = call.inEnv.locateByName(reference.ident);
        if (!(symbol instanceof ProcedureSy)) {
            throw new IllegalArgumentException("Unresolved procedure " + reference.ident);
        }
        ProcedureSy procedure = (ProcedureSy) symbol;
        ACTUAL actual = call.actuals;
        int number = 1;
        while (!(actual instanceof ACTUALNULL)) {
            FormalSy formal = ProcedureSy.GetFormalParam(procedure.GetProcFormals(), number);
            if (formal == null) {
                throw new IllegalArgumentException("Missing formal metadata for " + procedure.GetName());
            }
            if (formalByReference(formal)) {
                // reference arguments leave an address on the stack, value arguments leave a value
                if (!(actual.expr instanceof DESIGNATOR)) {
                    throw new IllegalArgumentException("Reference parameter requires a designator");
                }
                emitAddress((DESIGNATOR) actual.expr);
            } else {
                emitValue(actual.expr);
            }
            out.emit(isReal(formal.GetType()) ? Opcode.ACTUAL_F : Opcode.ACTUAL_I);
            actual = actual.nextActual;
            number++;
        }
        VmEmitter.Label target = procedures.get(procedure);
        if (target == null) {
            throw new IllegalArgumentException("Procedure has no VM body: " + procedure.GetName());
        }
        out.emit(Opcode.CALL, target, procedure.GetFormalSize());
    }

    // emits code that leaves the expression's value on the operand stack
    private void emitValue(EXPRESSION expression) {
        if (expression.isConstant && expression.value != null) {
            emitConstant(expression.value);
            return;
        }
        if (expression instanceof DESIGNATOR) {
            emitAddress((DESIGNATOR) expression);
            out.emit(isReal(expression.type) ? Opcode.LOAD_F : Opcode.LOAD_I);
        } else if (expression instanceof UNARY) {
            UNARY unary = (UNARY) expression;
            if (unary.op == Token.NOT) {
                emitBooleanValue(unary);
            } else {
                emitValue(unary.left);
                if (unary.op == Token.MINUS) {
                    out.emit(isReal(unary.type) ? Opcode.UMINUS_F : Opcode.UMINUS_I);
                } else if (unary.op == Token.TRUNC) {
                    out.emit(Opcode.TRUNC);
                } else if (unary.op == Token.FLOAT) {
                    out.emit(Opcode.FLOAT);
                } else {
                    throw unsupported(unary);
                }
            }
        } else if (expression instanceof BINARY) {
            BINARY binary = (BINARY) expression;
            if (isBooleanOperator(binary.op)) {
                emitBooleanValue(binary);
            } else {
                emitValue(binary.left);
                emitValue(binary.right);
                out.emit(arithmeticOpcode(binary.op, binary.type));
            }
        } else if (expression instanceof ast.ISA) {
            throw unsupported(expression);
        } else {
            throw unsupported(expression);
        }
    }

    private void emitConstant(Value value) {
        if (value instanceof RealValue) {
            out.emit(Opcode.PUSHCONST_F, Double.valueOf(((RealValue) value).getValue()));
        } else if (value instanceof StringValue) {
            out.emit(Opcode.PUSHCONST_I, out.internString(((StringValue) value).getValue()));
        } else if (value instanceof CharValue) {
            out.emit(Opcode.PUSHCONST_I, (int) ((CharValue) value).getValue());
        } else if (value instanceof BoolValue) {
            out.emit(Opcode.PUSHCONST_I, ((BoolValue) value).getValue() ? 1 : 0);
        } else if (value instanceof IntValue) {
            out.emit(Opcode.PUSHCONST_I, ((IntValue) value).getValue());
        } else {
            throw new IllegalArgumentException("Unsupported constant value " + value.getClass().getName());
        }
    }

    // materializes branch boolean expression as the integer values 1 or 0
    private void emitBooleanValue(EXPRESSION expression) {
        VmEmitter.Label trueLabel = out.label();
        VmEmitter.Label falseLabel = out.label();
        VmEmitter.Label endLabel = out.label();
        emitCondition(expression, trueLabel, falseLabel);
        out.mark(trueLabel);
        out.emit(Opcode.PUSHCONST_I, 1);
        out.emit(Opcode.JUMP, endLabel);
        out.mark(falseLabel);
        out.emit(Opcode.PUSHCONST_I, 0);
        out.mark(endLabel);
    }

    // emits short-circuiting control flow for {@code expression}
    // jumps to exactly one supplied label rather than leaving a boolean on the stack
    private void emitCondition(EXPRESSION expression, VmEmitter.Label whenTrue, VmEmitter.Label whenFalse) {
        if (expression.isConstant && expression.value instanceof BoolValue) {
            boolean value = ((BoolValue) expression.value).getValue();
            out.emit(Opcode.JUMP, value ? whenTrue : whenFalse);
            return;
        }
        if (expression instanceof UNARY && ((UNARY) expression).op == Token.NOT) {
            emitCondition(((UNARY) expression).left, whenFalse, whenTrue);
            return;
        }
        if (expression instanceof BINARY) {
            BINARY binary = (BINARY) expression;
            if (binary.op == Token.AND) {
                VmEmitter.Label right = out.label();
                emitCondition(binary.left, right, whenFalse);
                out.mark(right);
                emitCondition(binary.right, whenTrue, whenFalse);
                return;
            }
            if (binary.op == Token.OR) {
                VmEmitter.Label right = out.label();
                emitCondition(binary.left, whenTrue, right);
                out.mark(right);
                emitCondition(binary.right, whenTrue, whenFalse);
                return;
            }
            if (isComparison(binary.op)) {
                emitValue(binary.left);
                emitValue(binary.right);
                out.emit(comparisonOpcode(binary.op, binary.left.type), whenTrue);
                out.emit(Opcode.JUMP, whenFalse);
                return;
            }
        }
        emitValue(expression);
        out.emit(Opcode.PUSHCONST_I, 0);
        out.emit(Opcode.NE_I, whenTrue);
        out.emit(Opcode.JUMP, whenFalse);
    }

    // emits the address denoted by a variable, array element, or record field
    private void emitAddress(DESIGNATOR designator) {
        if (!(designator instanceof VARREF)) {
            throw new IllegalArgumentException("Designator does not start with a variable");
        }
        VARREF reference = (VARREF) designator;
        Symbol symbol = reference.inEnv.locateByName(reference.ident);
        if (!(symbol instanceof VariableSy) || symbol instanceof ConstSy) {
            throw new IllegalArgumentException("Variable expected: " + reference.ident);
        }
        VariableSy variable = (VariableSy) symbol;
        Storage location = storage.get(variable);
        if (location == null) {
            throw new IllegalArgumentException("Variable has no VM storage: " + reference.ident);
        }
        switch (location.area) {
            case GLOBAL:
                out.emit(Opcode.PUSHADDR_GLOBAL, location.offset);
                break;
            case LOCAL:
                out.emit(Opcode.PUSHADDR_LOCAL, location.offset);
                break;
            case FORMAL:
                out.emit(Opcode.PUSHADDR_FORMAL, location.offset);
                if (formalByReference((FormalSy) variable)) {
                    out.emit(Opcode.LOAD_I);
                }
                break;
            default:
                throw new AssertionError(location.area);
        }
        emitDesignatorTail(reference.next, variable.GetType());
    }

    // extends an already-emitted base address through array indices and record fields
    private void emitDesignatorTail(DESIGNATOR tail, TypeSy baseType) {
        if (tail instanceof DESNULL) {
            return;
        }
        if (tail instanceof INDEX) {
            if (!(baseType instanceof ArrayType)) {
                throw new IllegalArgumentException("INDEX applied to non-array type");
            }
            ArrayType array = (ArrayType) baseType;
            INDEX index = (INDEX) tail;
            emitValue(index.index);
            out.emit(Opcode.INDEX, sizeOf(array.GetArrayElementType()), array.GetArrayCount());
            emitDesignatorTail(index.next, array.GetArrayElementType());
            return;
        }
        if (tail instanceof FIELDREF) {
            if (!(baseType instanceof RecordType)) {
                throw new IllegalArgumentException("FIELD applied to non-record type");
            }
            FIELDREF reference = (FIELDREF) tail;
            Symbol fieldSymbol = ((RecordType) baseType).GetFields().locateByName(reference.ident);
            if (!(fieldSymbol instanceof FieldSy)) {
                throw new IllegalArgumentException("Unknown record field " + reference.ident);
            }
            FieldSy field = (FieldSy) fieldSymbol;
            out.emit(Opcode.FIELD, field.GetOffset());
            emitDesignatorTail(reference.next, field.GetType());
            return;
        }
        throw unsupported(tail);
    }

    private int arithmeticOpcode(int operator, TypeSy type) {
        boolean real = isReal(type);
        switch (operator) {
            case Token.PLUS: return real ? Opcode.PLUS_F : Opcode.PLUS_I;
            case Token.MINUS: return real ? Opcode.MINUS_F : Opcode.MINUS_I;
            case Token.STAR: return real ? Opcode.MULT_F : Opcode.MULT_I;
            case Token.SLASH: return real ? Opcode.DIV_F : Opcode.DIV_I;
            case Token.PERCENT: return Opcode.MOD_I;
            default: throw new IllegalArgumentException("Unsupported arithmetic operator " + operator);
        }
    }

    private int comparisonOpcode(int operator, TypeSy operandType) {
        boolean real = isReal(operandType);
        switch (operator) {
            case Token.EQ: return real ? Opcode.EQ_F : Opcode.EQ_I;
            case Token.GE: return real ? Opcode.GE_F : Opcode.GE_I;
            case Token.GT: return real ? Opcode.GT_F : Opcode.GT_I;
            case Token.LT: return real ? Opcode.LT_F : Opcode.LT_I;
            case Token.LE: return real ? Opcode.LE_F : Opcode.LE_I;
            case Token.NE: return real ? Opcode.NE_F : Opcode.NE_I;
            default: throw new IllegalArgumentException("Unsupported comparison operator " + operator);
        }
    }

    private boolean isBooleanOperator(int operator) {
        return operator == Token.AND || operator == Token.OR || isComparison(operator);
    }

    private boolean isComparison(int operator) {
        return operator == Token.EQ || operator == Token.GE || operator == Token.GT ||
               operator == Token.LT || operator == Token.LE || operator == Token.NE;
    }

    private boolean isReal(TypeSy type) {
        return type == Standard.RealType;
    }

    private boolean formalByReference(FormalSy formal) {
        return "VAR".equals(formal.GetFormalMode()) ||
               formal.GetType() instanceof ArrayType || formal.GetType() instanceof RecordType;
    }

    private <T extends Symbol> T requireSymbol(DECLARATION declaration, Class<T> kind) {
        if (!kind.isInstance(declaration.symbol)) {
            throw new IllegalArgumentException(
                "Missing " + kind.getSimpleName() + " for declaration " + declaration.ident);
        }
        return kind.cast(declaration.symbol);
    }

    private IllegalArgumentException unsupported(Object node) {
        return new IllegalArgumentException("Code generation is not implemented for " +
                                            node.getClass().getSimpleName());
    }
}
