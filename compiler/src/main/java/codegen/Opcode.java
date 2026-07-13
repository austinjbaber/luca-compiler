package codegen;

/** Serialized opcode numbers and byte sizes shared with runtime/include/opcodes.h. */
final class Opcode {
    static final int DEF_STRING = 0;
    static final int PROG_BEGIN = 1;
    static final int PROG_END = 2;
    static final int PUSHADDR_LOCAL = 3;
    static final int PUSHADDR_FORMAL = 4;
    static final int PUSHADDR_GLOBAL = 5;
    static final int FIELD = 6;
    static final int INDEX = 7;
    static final int PLUS_I = 8;
    static final int MINUS_I = 9;
    static final int MULT_I = 10;
    static final int DIV_I = 11;
    static final int EQ_I = 12;
    static final int GE_I = 13;
    static final int GT_I = 14;
    static final int LT_I = 15;
    static final int LE_I = 16;
    static final int NE_I = 17;
    static final int PUSHCONST_I = 18;
    static final int LOAD_I = 19;
    static final int STORE_I = 20;
    static final int UMINUS_I = 21;
    static final int PLUS_F = 22;
    static final int MINUS_F = 23;
    static final int MULT_F = 24;
    static final int DIV_F = 25;
    static final int EQ_F = 26;
    static final int GE_F = 27;
    static final int GT_F = 28;
    static final int LT_F = 29;
    static final int LE_F = 30;
    static final int NE_F = 31;
    static final int PUSHCONST_F = 32;
    static final int LOAD_F = 33;
    static final int STORE_F = 34;
    static final int UMINUS_F = 35;
    static final int TRUNC = 36;
    static final int FLOAT = 37;
    static final int PROC_BEGIN = 38;
    static final int PROC_END = 39;
    static final int CALL = 40;
    static final int JUMP = 41;
    static final int ACTUAL_I = 42;
    static final int ACTUAL_F = 43;
    static final int WRITE_I = 44;
    static final int WRITE_F = 45;
    static final int WRITE_C = 46;
    static final int WRITE_S = 47;
    static final int READ_I = 48;
    static final int READ_F = 49;
    static final int READ_C = 50;
    static final int WRITELN = 51;
    static final int ISA = 52;
    static final int NARROW = 53;
    static final int NEW = 54;
    static final int DEF_GLOBALS = 55;
    static final int MOD_I = 56;

    static int size(int opcode) {
        switch (opcode) {
            case PROC_BEGIN:
            case PROC_END:
            case CALL:
            case INDEX:
                return 17;
            case PUSHADDR_LOCAL:
            case PUSHADDR_FORMAL:
            case PUSHADDR_GLOBAL:
            case FIELD:
            case EQ_I:
            case GE_I:
            case GT_I:
            case LT_I:
            case LE_I:
            case NE_I:
            case EQ_F:
            case GE_F:
            case GT_F:
            case LT_F:
            case LE_F:
            case NE_F:
            case PUSHCONST_I:
            case PUSHCONST_F:
            case JUMP:
                return 9;
            default:
                return 1;
        }
    }

    private Opcode() {}
}
