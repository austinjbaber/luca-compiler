/*
 * Experimental Luca .quad-to-MIPS translator.
 *
 * This is a C port of the part-8 Python prototype. It consumes the historical
 * .quad interchange format; the supported Luca compiler emits VM code and does
 * not feed this backend.
 */

#include <ctype.h>
#include <errno.h>
#include <limits.h>
#include <stdarg.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    char *key;
    char *value;
} Attr;

typedef struct {
    char *op;
    Attr *attrs;
    size_t attr_count;
} Instruction;

typedef struct {
    Instruction *items;
    size_t count;
    size_t capacity;
} InstructionVec;

typedef struct {
    char **items;
    size_t count;
    size_t capacity;
} LineVec;

typedef struct {
    int id;
    int size;
} TypeInfo;

typedef struct {
    int id;
    int size;
    int element_type_id;
} ArrayInfo;

typedef struct {
    int id;
    int offset;
} FieldInfo;

typedef struct {
    int id;
    int size;
} VarInfo;

typedef struct {
    TypeInfo *types;
    size_t type_count;
    ArrayInfo *arrays;
    size_t array_count;
    TypeInfo *records;
    size_t record_count;
    FieldInfo *fields;
    size_t field_count;
    VarInfo *vars;
    size_t var_count;
} Tables;

typedef struct {
    int id;
    long long value;
} Constant;

typedef struct {
    Constant *items;
    size_t count;
    size_t capacity;
} ConstMap;

typedef struct {
    int from;
    int to;
} LabelAlias;

static void die(const char *format, ...)
{
    va_list args;
    va_start(args, format);
    fputs("luca_mips: ", stderr);
    vfprintf(stderr, format, args);
    fputc('\n', stderr);
    va_end(args);
    exit(EXIT_FAILURE);
}

static void *xrealloc(void *ptr, size_t size)
{
    void *result = realloc(ptr, size);
    if (result == NULL && size != 0) {
        die("out of memory");
    }
    return result;
}

static char *xstrdup(const char *text)
{
    size_t length = strlen(text) + 1;
    char *copy = xrealloc(NULL, length);
    memcpy(copy, text, length);
    return copy;
}

static char *format_string(const char *format, va_list args)
{
    va_list copy;
    va_copy(copy, args);
    int length = vsnprintf(NULL, 0, format, copy);
    va_end(copy);
    if (length < 0) {
        die("failed to format output");
    }
    char *text = xrealloc(NULL, (size_t)length + 1);
    vsnprintf(text, (size_t)length + 1, format, args);
    return text;
}

static void lines_add(LineVec *lines, const char *format, ...)
{
    if (lines->count == lines->capacity) {
        lines->capacity = lines->capacity == 0 ? 64 : lines->capacity * 2;
        lines->items = xrealloc(lines->items, lines->capacity * sizeof(*lines->items));
    }
    va_list args;
    va_start(args, format);
    lines->items[lines->count++] = format_string(format, args);
    va_end(args);
}

static void line_replace(LineVec *lines, size_t index, const char *format, ...)
{
    va_list args;
    va_start(args, format);
    char *replacement = format_string(format, args);
    va_end(args);
    free(lines->items[index]);
    lines->items[index] = replacement;
}

static void line_remove(LineVec *lines, size_t index)
{
    free(lines->items[index]);
    memmove(&lines->items[index], &lines->items[index + 1],
            (lines->count - index - 1) * sizeof(*lines->items));
    lines->count--;
}

static void instructions_add(InstructionVec *instructions, Instruction instruction)
{
    if (instructions->count == instructions->capacity) {
        instructions->capacity = instructions->capacity == 0 ? 64 : instructions->capacity * 2;
        instructions->items = xrealloc(instructions->items,
                                       instructions->capacity * sizeof(*instructions->items));
    }
    instructions->items[instructions->count++] = instruction;
}

static char *trim(char *text)
{
    while (isspace((unsigned char)*text)) {
        text++;
    }
    char *end = text + strlen(text);
    while (end > text && isspace((unsigned char)end[-1])) {
        end--;
    }
    *end = '\0';
    return text;
}

static int ascii_casecmp(const char *left, const char *right)
{
    while (*left != '\0' && *right != '\0') {
        int a = tolower((unsigned char)*left);
        int b = tolower((unsigned char)*right);
        if (a != b) {
            return a - b;
        }
        left++;
        right++;
    }
    return (unsigned char)*left - (unsigned char)*right;
}

static void strip_quotes(char *text)
{
    size_t length = strlen(text);
    if (length >= 2 && text[0] == '"' && text[length - 1] == '"') {
        memmove(text, text + 1, length - 2);
        text[length - 2] = '\0';
    }
}

static const char *attr_optional(const Instruction *instruction, const char *key)
{
    for (size_t i = 0; i < instruction->attr_count; i++) {
        if (strcmp(instruction->attrs[i].key, key) == 0) {
            return instruction->attrs[i].value;
        }
    }
    return NULL;
}

static const char *attr_required(const Instruction *instruction, const char *key)
{
    const char *value = attr_optional(instruction, key);
    if (value == NULL) {
        die("%s instruction is missing %s", instruction->op, key);
    }
    return value;
}

static long long parse_integer(const char *text, const char *context)
{
    char *end = NULL;
    errno = 0;
    long long value = strtoll(text, &end, 10);
    if (errno == ERANGE || end == text || *end != '\0') {
        die("invalid integer '%s' for %s", text, context);
    }
    return value;
}

static int attr_int(const Instruction *instruction, const char *key)
{
    long long value = parse_integer(attr_required(instruction, key), key);
    if (value < INT_MIN || value > INT_MAX) {
        die("integer for %s is out of range", key);
    }
    return (int)value;
}

static InstructionVec parse_quad_file(const char *path)
{
    FILE *input = fopen(path, "r");
    if (input == NULL) {
        die("cannot open '%s': %s", path, strerror(errno));
    }

    InstructionVec instructions = {0};
    char buffer[8192];
    size_t line_number = 0;
    while (fgets(buffer, sizeof(buffer), input) != NULL) {
        line_number++;
        if (strchr(buffer, '\n') == NULL && !feof(input)) {
            die("%s:%zu: line is too long", path, line_number);
        }
        char *line = trim(buffer);
        if (*line == '\0') {
            continue;
        }

        char *token = strtok(line, " \t\r\n");
        Instruction instruction = {.op = xstrdup(token)};
        while ((token = strtok(NULL, " \t\r\n")) != NULL) {
            char *equals = strchr(token, '=');
            if (equals == NULL) {
                continue;
            }
            *equals = '\0';
            char *value = equals + 1;
            strip_quotes(value);
            instruction.attrs = xrealloc(instruction.attrs,
                                         (instruction.attr_count + 1) * sizeof(*instruction.attrs));
            instruction.attrs[instruction.attr_count].key = xstrdup(token);
            instruction.attrs[instruction.attr_count].value = xstrdup(value);
            instruction.attr_count++;
        }
        instructions_add(&instructions, instruction);
    }
    if (ferror(input)) {
        die("cannot read '%s': %s", path, strerror(errno));
    }
    fclose(input);
    return instructions;
}

static void append_type(TypeInfo **items, size_t *count, int id, int size)
{
    *items = xrealloc(*items, (*count + 1) * sizeof(**items));
    (*items)[*count] = (TypeInfo){.id = id, .size = size};
    (*count)++;
}

static int find_type_size(const TypeInfo *items, size_t count, int id)
{
    for (size_t i = 0; i < count; i++) {
        if (items[i].id == id) {
            return items[i].size;
        }
    }
    return -1;
}

static int get_type_size(int id, const Tables *tables)
{
    int size = find_type_size(tables->types, tables->type_count, id);
    if (size >= 0) {
        return size;
    }
    for (size_t i = 0; i < tables->array_count; i++) {
        if (tables->arrays[i].id == id) {
            return tables->arrays[i].size;
        }
    }
    size = find_type_size(tables->records, tables->record_count, id);
    return size >= 0 ? size : 4;
}

static Tables build_tables(const InstructionVec *instructions)
{
    Tables tables = {0};
    for (size_t i = 0; i < instructions->count; i++) {
        const Instruction *instruction = &instructions->items[i];
        if (strcmp(instruction->op, "TypeDecl") == 0) {
            append_type(&tables.types, &tables.type_count,
                        attr_int(instruction, "id"), attr_int(instruction, "size"));
        } else if (strcmp(instruction->op, "ArrayDecl") == 0) {
            tables.arrays = xrealloc(tables.arrays,
                                     (tables.array_count + 1) * sizeof(*tables.arrays));
            tables.arrays[tables.array_count++] = (ArrayInfo){
                .id = attr_int(instruction, "id"),
                .size = attr_int(instruction, "size"),
                .element_type_id = attr_int(instruction, "elementTypeId")};
            (void)attr_required(instruction, "elementCount");
        } else if (strcmp(instruction->op, "RecordDecl") == 0) {
            append_type(&tables.records, &tables.record_count,
                        attr_int(instruction, "id"), attr_int(instruction, "size"));
        } else if (strcmp(instruction->op, "FieldDecl") == 0) {
            tables.fields = xrealloc(tables.fields,
                                     (tables.field_count + 1) * sizeof(*tables.fields));
            tables.fields[tables.field_count++] = (FieldInfo){
                .id = attr_int(instruction, "id"), .offset = attr_int(instruction, "offset")};
            (void)attr_required(instruction, "recordId");
        } else if (strcmp(instruction->op, "VarDecl") == 0) {
            int type_id = attr_int(instruction, "typeId");
            int size = get_type_size(type_id, &tables);
            tables.vars = xrealloc(tables.vars, (tables.var_count + 1) * sizeof(*tables.vars));
            tables.vars[tables.var_count++] = (VarInfo){
                .id = attr_int(instruction, "id"), .size = size == 0 ? 4 : size};
            (void)attr_required(instruction, "symbol");
            (void)attr_required(instruction, "kind");
        }
    }
    return tables;
}

static int compare_vars(const void *left, const void *right)
{
    const VarInfo *a = left;
    const VarInfo *b = right;
    return (a->id > b->id) - (a->id < b->id);
}

static const VarInfo *find_var(const Tables *tables, int id)
{
    for (size_t i = 0; i < tables->var_count; i++) {
        if (tables->vars[i].id == id) {
            return &tables->vars[i];
        }
    }
    die("unknown variable id %d", id);
    return NULL;
}

static const ArrayInfo *find_array(const Tables *tables, int id)
{
    for (size_t i = 0; i < tables->array_count; i++) {
        if (tables->arrays[i].id == id) {
            return &tables->arrays[i];
        }
    }
    die("unknown array id %d", id);
    return NULL;
}

static const FieldInfo *find_field(const Tables *tables, int id)
{
    for (size_t i = 0; i < tables->field_count; i++) {
        if (tables->fields[i].id == id) {
            return &tables->fields[i];
        }
    }
    die("unknown field id %d", id);
    return NULL;
}

static bool const_get(const ConstMap *map, int id, long long *value)
{
    for (size_t i = 0; i < map->count; i++) {
        if (map->items[i].id == id) {
            *value = map->items[i].value;
            return true;
        }
    }
    return false;
}

static void const_set(ConstMap *map, int id, long long value)
{
    for (size_t i = 0; i < map->count; i++) {
        if (map->items[i].id == id) {
            map->items[i].value = value;
            return;
        }
    }
    if (map->count == map->capacity) {
        map->capacity = map->capacity == 0 ? 16 : map->capacity * 2;
        map->items = xrealloc(map->items, map->capacity * sizeof(*map->items));
    }
    map->items[map->count++] = (Constant){.id = id, .value = value};
}

static void const_remove(ConstMap *map, int id)
{
    for (size_t i = 0; i < map->count; i++) {
        if (map->items[i].id == id) {
            map->items[i] = map->items[map->count - 1];
            map->count--;
            return;
        }
    }
}

static void gen_load_var(LineVec *lines, const Tables *tables, int id, const char *reg)
{
    const VarInfo *var = find_var(tables, id);
    lines_add(lines, "%s %s, var_%d", var->size == 1 ? "lb" : "lw", reg, id);
}

static void gen_store_var(LineVec *lines, const Tables *tables, int id, const char *reg)
{
    const VarInfo *var = find_var(tables, id);
    lines_add(lines, "%s %s, var_%d", var->size == 1 ? "sb" : "sw", reg, id);
}

static void gen_copy_value(LineVec *lines, const Tables *tables, ConstMap *constants,
                           int source_id, int result_id)
{
    long long value;
    if (const_get(constants, source_id, &value)) {
        lines_add(lines, "li $t0, %lld", value);
        gen_store_var(lines, tables, result_id, "$t0");
        const_set(constants, result_id, value);
        return;
    }
    gen_load_var(lines, tables, source_id, "$t0");
    gen_store_var(lines, tables, result_id, "$t0");
    const_remove(constants, result_id);
}

static bool is_power_of_two(int value)
{
    return value > 0 && (value & (value - 1)) == 0;
}

static long long trunc_div(long long left, long long right)
{
    if (right == 0) {
        return 0;
    }
    return left / right;
}

static long long trunc_mod(long long left, long long right)
{
    if (right == 0) {
        return 0;
    }
    return left % right;
}

static void gen_mul_by_const(LineVec *lines, const char *src, long long value,
                             const char *dst, const char *temp)
{
    if (value == 0) {
        lines_add(lines, "li %s, 0", dst);
        return;
    }
    if (value == 1) {
        lines_add(lines, "move %s, %s", dst, src);
        return;
    }
    if (value == -1) {
        lines_add(lines, "sub %s, $zero, %s", dst, src);
        return;
    }

    unsigned long long magnitude = value < 0 ? (unsigned long long)(-(value + 1)) + 1
                                             : (unsigned long long)value;
    bool first = true;
    int bit = 0;
    while (magnitude != 0) {
        if ((magnitude & 1U) != 0) {
            if (first) {
                lines_add(lines, bit == 0 ? "move %s, %s" : "sll %s, %s, %d",
                          dst, src, bit);
                first = false;
            } else if (bit == 0) {
                lines_add(lines, "add %s, %s, %s", dst, dst, src);
            } else {
                lines_add(lines, "sll %s, %s, %d", temp, src, bit);
                lines_add(lines, "add %s, %s, %s", dst, dst, temp);
            }
        }
        magnitude >>= 1;
        bit++;
    }
    if (value < 0) {
        lines_add(lines, "sub %s, $zero, %s", dst, dst);
    }
}

static size_t tokenize(const char *line, char tokens[][64], size_t max_tokens)
{
    char buffer[512];
    snprintf(buffer, sizeof(buffer), "%s", line);
    char *comment = strchr(buffer, '#');
    if (comment != NULL) {
        *comment = '\0';
    }
    size_t count = 0;
    for (char *token = strtok(buffer, " \t\r\n"); token != NULL && count < max_tokens;
         token = strtok(NULL, " \t\r\n")) {
        size_t length = strlen(token);
        if (length > 0 && token[length - 1] == ',') {
            token[length - 1] = '\0';
        }
        snprintf(tokens[count++], 64, "%s", token);
    }
    return count;
}

static bool label_name(const char *line, char *name, size_t name_size)
{
    char tokens[2][64];
    size_t count = tokenize(line, tokens, 2);
    if (count != 1) {
        return false;
    }
    size_t length = strlen(tokens[0]);
    if (length < 2 || tokens[0][length - 1] != ':') {
        return false;
    }
    tokens[0][length - 1] = '\0';
    if (!(isalpha((unsigned char)tokens[0][0]) || tokens[0][0] == '_')) {
        return false;
    }
    for (size_t i = 1; tokens[0][i] != '\0'; i++) {
        if (!(isalnum((unsigned char)tokens[0][i]) || tokens[0][i] == '_')) {
            return false;
        }
    }
    snprintf(name, name_size, "%s", tokens[0]);
    return true;
}

static bool simplify(LineVec *lines, size_t index)
{
    char tokens[4][64];
    size_t count = tokenize(lines->items[index], tokens, 4);
    if (count < 4) {
        return false;
    }
    if (strcmp(tokens[0], "add") == 0) {
        if (strcmp(tokens[3], "$zero") == 0) {
            line_replace(lines, index, "move %s, %s", tokens[1], tokens[2]);
            return true;
        }
        if (strcmp(tokens[2], "$zero") == 0) {
            line_replace(lines, index, "move %s, %s", tokens[1], tokens[3]);
            return true;
        }
    } else if (strcmp(tokens[0], "sub") == 0 && strcmp(tokens[3], "$zero") == 0) {
        line_replace(lines, index, "move %s, %s", tokens[1], tokens[2]);
        return true;
    } else if (strcmp(tokens[0], "addi") == 0 && strcmp(tokens[3], "0") == 0) {
        line_replace(lines, index, "move %s, %s", tokens[1], tokens[2]);
        return true;
    } else if (strcmp(tokens[0], "sll") == 0 && strcmp(tokens[3], "0") == 0) {
        line_replace(lines, index, "move %s, %s", tokens[1], tokens[2]);
        return true;
    }
    return false;
}

static void peephole_optimize(LineVec *lines)
{
    size_t text_start = 0;
    for (size_t i = 0; i < lines->count; i++) {
        if (strcmp(lines->items[i], ".text") == 0) {
            text_start = i + 1;
            break;
        }
    }

    bool changed = true;
    while (changed) {
        changed = false;
        for (size_t i = text_start; i < lines->count;) {
            char tokens[4][64];
            size_t count = tokenize(lines->items[i], tokens, 4);
            if (count == 0) {
                i++;
                continue;
            }
            if (i + 3 < lines->count && strcmp(tokens[0], "j") == 0 && count == 2) {
                char label[64];
                if (label_name(lines->items[i + 1], label, sizeof(label)) &&
                    strcmp(label, tokens[1]) == 0) {
                    line_remove(lines, i);
                    changed = true;
                    continue;
                }
            }
            if (i + 3 < lines->count && strcmp(tokens[0], "j") == 0 && count == 2) {
                char label[64];
                char next[2][64];
                size_t next_count = tokenize(lines->items[i + 2], next, 2);
                if (label_name(lines->items[i + 1], label, sizeof(label)) &&
                    strcmp(label, tokens[1]) == 0 && next_count == 2 &&
                    strcmp(next[0], "j") == 0) {
                    line_replace(lines, i, "j %s", next[1]);
                    changed = true;
                    i++;
                    continue;
                }
            }
            if (i + 3 < lines->count &&
                (strcmp(tokens[0], "beq") == 0 || strcmp(tokens[0], "bne") == 0) &&
                count == 4) {
                char label[64];
                char next[2][64];
                size_t next_count = tokenize(lines->items[i + 2], next, 2);
                if (label_name(lines->items[i + 1], label, sizeof(label)) &&
                    strcmp(label, tokens[3]) == 0 && next_count == 2 &&
                    strcmp(next[0], "j") == 0) {
                    line_replace(lines, i, "%s %s, %s, %s", tokens[0], tokens[1],
                                 tokens[2], next[1]);
                    changed = true;
                    i++;
                    continue;
                }
            }
            if (simplify(lines, i)) {
                changed = true;
            }
            i++;
        }
    }
}

static int resolve_label(int label, const LabelAlias *aliases, size_t alias_count)
{
    int current = label;
    for (size_t steps = 0; steps <= alias_count; steps++) {
        bool found = false;
        for (size_t i = 0; i < alias_count; i++) {
            if (aliases[i].from == current) {
                current = aliases[i].to;
                found = true;
                break;
            }
        }
        if (!found) {
            break;
        }
    }
    return current;
}

static LabelAlias *build_label_aliases(const Instruction *items, size_t count,
                                       size_t *alias_count)
{
    LabelAlias *aliases = NULL;
    *alias_count = 0;
    for (size_t i = 0; i < count; i++) {
        if (strcmp(items[i].op, "Label") != 0) {
            continue;
        }
        size_t j = i + 1;
        while (j < count && strcmp(items[j].op, "Label") == 0) {
            j++;
        }
        if (j < count && strcmp(items[j].op, "Goto") == 0) {
            aliases = xrealloc(aliases, (*alias_count + 1) * sizeof(*aliases));
            aliases[*alias_count] = (LabelAlias){
                .from = attr_int(&items[i], "label"), .to = attr_int(&items[j], "label")};
            (*alias_count)++;
        }
    }
    return aliases;
}

static void generate_mips(const Instruction *items, size_t count, Tables *tables,
                          bool optimize, LineVec *lines)
{
    qsort(tables->vars, tables->var_count, sizeof(*tables->vars), compare_vars);
    lines_add(lines, ".data");
    lines_add(lines, "newline: .asciiz \"\\n\"");
    for (size_t i = 0; i < tables->var_count; i++) {
        if (tables->vars[i].size >= 4) {
            lines_add(lines, ".align 2");
        }
        lines_add(lines, "var_%d: .space %d", tables->vars[i].id, tables->vars[i].size);
    }
    lines_add(lines, ".text");
    lines_add(lines, ".globl main");
    lines_add(lines, "main:");

    ConstMap constants = {0};
    size_t alias_count = 0;
    LabelAlias *aliases = optimize ? build_label_aliases(items, count, &alias_count) : NULL;

    for (size_t i = 0; i < count; i++) {
        const Instruction *instruction = &items[i];
        const char *op = instruction->op;
        if (strcmp(op, "Label") == 0) {
            lines_add(lines, "L%d:", attr_int(instruction, "label"));
        } else if (strcmp(op, "Goto") == 0) {
            int label = attr_int(instruction, "label");
            lines_add(lines, "j L%d", optimize ? resolve_label(label, aliases, alias_count) : label);
        } else if (strcmp(op, "Branch") == 0) {
            int label = attr_int(instruction, "label");
            if (optimize) {
                label = resolve_label(label, aliases, alias_count);
            }
            gen_load_var(lines, tables, attr_int(instruction, "leftId"), "$t0");
            gen_load_var(lines, tables, attr_int(instruction, "rightId"), "$t1");
            const char *symbol = attr_required(instruction, "op");
            if (strcmp(symbol, "=") == 0) {
                lines_add(lines, "beq $t0, $t1, L%d", label);
            } else if (strcmp(symbol, "#") == 0) {
                lines_add(lines, "bne $t0, $t1, L%d", label);
            } else if (strcmp(symbol, "<") == 0) {
                lines_add(lines, "slt $t2, $t0, $t1");
                lines_add(lines, "bne $t2, $zero, L%d", label);
            } else if (strcmp(symbol, ">") == 0) {
                lines_add(lines, "slt $t2, $t1, $t0");
                lines_add(lines, "bne $t2, $zero, L%d", label);
            } else if (strcmp(symbol, "<=") == 0) {
                lines_add(lines, "slt $t2, $t1, $t0");
                lines_add(lines, "beq $t2, $zero, L%d", label);
            } else if (strcmp(symbol, ">=") == 0) {
                lines_add(lines, "slt $t2, $t0, $t1");
                lines_add(lines, "beq $t2, $zero, L%d", label);
            } else {
                lines_add(lines, "# Unsupported op %s", symbol);
            }
        } else if (strcmp(op, "AddressOf") == 0) {
            int result = attr_int(instruction, "resultId");
            lines_add(lines, "la $t0, var_%d", attr_int(instruction, "id"));
            gen_store_var(lines, tables, result, "$t0");
            const_remove(&constants, result);
        } else if (strcmp(op, "LoadLit") == 0) {
            int result = attr_int(instruction, "resultId");
            long long value = parse_integer(attr_required(instruction, "value"), "value");
            lines_add(lines, "li $t0, %lld", value);
            gen_store_var(lines, tables, result, "$t0");
            const_set(&constants, result, value);
        } else if (strcmp(op, "Load") == 0) {
            int result = attr_int(instruction, "resultId");
            int size = get_type_size(attr_int(instruction, "typeId"), tables);
            gen_load_var(lines, tables, attr_int(instruction, "desId"), "$t0");
            lines_add(lines, "%s $t1, 0($t0)", size == 1 ? "lb" : "lw");
            gen_store_var(lines, tables, result, "$t1");
            const_remove(&constants, result);
        } else if (strcmp(op, "Store") == 0) {
            int size = get_type_size(attr_int(instruction, "typeId"), tables);
            gen_load_var(lines, tables, attr_int(instruction, "leftId"), "$t0");
            gen_load_var(lines, tables, attr_int(instruction, "rightId"), "$t1");
            lines_add(lines, "%s $t1, 0($t0)", size == 1 ? "sb" : "sw");
        } else if (strcmp(op, "UnaryExpr") == 0) {
            int expr = attr_int(instruction, "exprId");
            int result = attr_int(instruction, "resultId");
            const char *symbol = attr_required(instruction, "op");
            long long value;
            if (optimize && const_get(&constants, expr, &value)) {
                if (strcmp(symbol, "-") == 0) {
                    value = -value;
                } else if (ascii_casecmp(symbol, "not") == 0) {
                    value = value == 0 ? 1 : 0;
                } else {
                    lines_add(lines, "# Unsupported op %s", symbol);
                    const_remove(&constants, result);
                    continue;
                }
                lines_add(lines, "li $t0, %lld", value);
                gen_store_var(lines, tables, result, "$t0");
                const_set(&constants, result, value);
            } else {
                gen_load_var(lines, tables, expr, "$t0");
                if (strcmp(symbol, "-") == 0) {
                    lines_add(lines, "sub $t1, $zero, $t0");
                } else if (ascii_casecmp(symbol, "not") == 0) {
                    lines_add(lines, "seq $t1, $t0, $zero");
                } else {
                    lines_add(lines, "# Unsupported op %s", symbol);
                    lines_add(lines, "move $t1, $t0");
                }
                gen_store_var(lines, tables, result, "$t1");
                const_remove(&constants, result);
            }
        } else if (strcmp(op, "BinExpr") == 0) {
            int left = attr_int(instruction, "leftId");
            int right = attr_int(instruction, "rightId");
            int result = attr_int(instruction, "resultId");
            const char *symbol = attr_required(instruction, "op");
            long long left_value = 0, right_value = 0, value = 0;
            bool left_const = const_get(&constants, left, &left_value);
            bool right_const = const_get(&constants, right, &right_value);
            bool folded = false;
            if (optimize && left_const && right_const) {
                if (strcmp(symbol, "+") == 0) value = left_value + right_value, folded = true;
                else if (strcmp(symbol, "-") == 0) value = left_value - right_value, folded = true;
                else if (strcmp(symbol, "*") == 0) value = left_value * right_value, folded = true;
                else if (strcmp(symbol, "/") == 0) value = trunc_div(left_value, right_value), folded = true;
                else if (strcmp(symbol, "%") == 0) value = trunc_mod(left_value, right_value), folded = true;
            }
            if (folded) {
                lines_add(lines, "li $t0, %lld", value);
                gen_store_var(lines, tables, result, "$t0");
                const_set(&constants, result, value);
                continue;
            }
            if (optimize && ((strcmp(symbol, "+") == 0 && right_const && right_value == 0) ||
                             (strcmp(symbol, "-") == 0 && right_const && right_value == 0) ||
                             (strcmp(symbol, "*") == 0 && right_const && right_value == 1) ||
                             (strcmp(symbol, "/") == 0 && right_const && right_value == 1))) {
                gen_copy_value(lines, tables, &constants, left, result);
                continue;
            }
            if (optimize && ((strcmp(symbol, "+") == 0 && left_const && left_value == 0) ||
                             (strcmp(symbol, "*") == 0 && left_const && left_value == 1))) {
                gen_copy_value(lines, tables, &constants, right, result);
                continue;
            }
            if (optimize && ((strcmp(symbol, "*") == 0 &&
                              ((left_const && left_value == 0) || (right_const && right_value == 0))) ||
                             ((strcmp(symbol, "/") == 0 || strcmp(symbol, "%") == 0) &&
                              left_const && left_value == 0))) {
                lines_add(lines, "li $t0, 0");
                gen_store_var(lines, tables, result, "$t0");
                const_set(&constants, result, 0);
                continue;
            }
            if (optimize && strcmp(symbol, "*") == 0 && (left_const || right_const)) {
                long long constant = left_const ? left_value : right_value;
                gen_load_var(lines, tables, left_const ? right : left, "$t0");
                gen_mul_by_const(lines, "$t0", constant, "$t1", "$t2");
                gen_store_var(lines, tables, result, "$t1");
                const_remove(&constants, result);
                continue;
            }
            gen_load_var(lines, tables, left, "$t0");
            gen_load_var(lines, tables, right, "$t1");
            if (strcmp(symbol, "+") == 0) lines_add(lines, "add $t2, $t0, $t1");
            else if (strcmp(symbol, "-") == 0) lines_add(lines, "sub $t2, $t0, $t1");
            else if (strcmp(symbol, "*") == 0) lines_add(lines, "mul $t2, $t0, $t1");
            else if (strcmp(symbol, "/") == 0) {
                lines_add(lines, "div $t0, $t1");
                lines_add(lines, "mflo $t2");
            } else if (strcmp(symbol, "%") == 0) {
                lines_add(lines, "div $t0, $t1");
                lines_add(lines, "mfhi $t2");
            } else {
                lines_add(lines, "# Unsupported op %s", symbol);
                lines_add(lines, "move $t2, $t0");
            }
            gen_store_var(lines, tables, result, "$t2");
            const_remove(&constants, result);
        } else if (strcmp(op, "IndexOf") == 0) {
            int result = attr_int(instruction, "resultId");
            const ArrayInfo *array = find_array(tables, attr_int(instruction, "arrayId"));
            int element_size = get_type_size(array->element_type_id, tables);
            gen_load_var(lines, tables, attr_int(instruction, "baseId"), "$t0");
            gen_load_var(lines, tables, attr_int(instruction, "indexId"), "$t1");
            if (optimize && is_power_of_two(element_size)) {
                int shift = 0;
                while ((1 << shift) < element_size) shift++;
                lines_add(lines, "sll $t2, $t1, %d", shift);
            } else {
                lines_add(lines, "li $t2, %d", element_size);
                lines_add(lines, "mul $t2, $t1, $t2");
            }
            lines_add(lines, "add $t3, $t0, $t2");
            gen_store_var(lines, tables, result, "$t3");
            const_remove(&constants, result);
        } else if (strcmp(op, "FieldOf") == 0) {
            int result = attr_int(instruction, "resultId");
            int offset = find_field(tables, attr_int(instruction, "fieldId"))->offset;
            gen_load_var(lines, tables, attr_int(instruction, "baseId"), "$t0");
            if (offset >= -32768 && offset <= 32767) {
                lines_add(lines, "addi $t1, $t0, %d", offset);
            } else {
                lines_add(lines, "li $t2, %d", offset);
                lines_add(lines, "add $t1, $t0, $t2");
            }
            gen_store_var(lines, tables, result, "$t1");
            const_remove(&constants, result);
        } else if (strcmp(op, "Write") == 0) {
            gen_load_var(lines, tables, attr_int(instruction, "exprId"), "$a0");
            lines_add(lines, "li $v0, 1");
            lines_add(lines, "syscall");
        } else if (strcmp(op, "WriteLn") == 0) {
            lines_add(lines, "li $v0, 4");
            lines_add(lines, "la $a0, newline");
            lines_add(lines, "syscall");
        }
    }
    lines_add(lines, "li $v0, 10");
    lines_add(lines, "syscall");
    if (optimize) {
        peephole_optimize(lines);
    }
    free(constants.items);
    free(aliases);
}

static void free_instructions(InstructionVec *instructions)
{
    for (size_t i = 0; i < instructions->count; i++) {
        free(instructions->items[i].op);
        for (size_t j = 0; j < instructions->items[i].attr_count; j++) {
            free(instructions->items[i].attrs[j].key);
            free(instructions->items[i].attrs[j].value);
        }
        free(instructions->items[i].attrs);
    }
    free(instructions->items);
}

static void usage(void)
{
    fputs("Usage: luca_mips <input.quad> <output.s> [-O]\n", stderr);
}

int main(int argc, char **argv)
{
    if (argc < 3 || argc > 4 || (argc == 4 && strcmp(argv[3], "-O") != 0)) {
        usage();
        return EXIT_FAILURE;
    }
    InstructionVec instructions = parse_quad_file(argv[1]);
    Tables tables = build_tables(&instructions);

    const Instruction *main_items = NULL;
    size_t main_count = 0;
    for (size_t i = 0; i < instructions.count; i++) {
        if (strcmp(instructions.items[i].op, "ProcBegin") == 0 &&
            strcmp(attr_optional(&instructions.items[i], "symbol") != NULL
                       ? attr_optional(&instructions.items[i], "symbol") : "",
                   "$MAIN") == 0) {
            main_items = &instructions.items[i + 1];
            for (size_t j = i + 1; j < instructions.count; j++) {
                if (strcmp(instructions.items[j].op, "ProcEnd") == 0) {
                    main_count = j - i - 1;
                    break;
                }
            }
            break;
        }
    }

    LineVec lines = {0};
    generate_mips(main_items, main_count, &tables, argc == 4, &lines);
    FILE *output = fopen(argv[2], "w");
    if (output == NULL) {
        die("cannot open '%s': %s", argv[2], strerror(errno));
    }
    for (size_t i = 0; i < lines.count; i++) {
        if (fprintf(output, "%s\n", lines.items[i]) < 0) {
            die("cannot write '%s': %s", argv[2], strerror(errno));
        }
        free(lines.items[i]);
    }
    if (fclose(output) != 0) {
        die("cannot close '%s': %s", argv[2], strerror(errno));
    }

    free(lines.items);
    free(tables.types);
    free(tables.arrays);
    free(tables.records);
    free(tables.fields);
    free(tables.vars);
    free_instructions(&instructions);
    return EXIT_SUCCESS;
}
