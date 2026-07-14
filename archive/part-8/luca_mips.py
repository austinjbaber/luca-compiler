#!/usr/bin/env python3
import re
import sys

"""
Author: Austin Baber (austinjbaber@arizona.edu)
This script translates .quad intermediate code to MIPS assembly
    >> Usage: luca_mips <input.quad> <output.s> [-O]
-O optimizations: constant folding, algebraic simplifications, strength reduction, and jump-to-jump elimination
"""

class TypeInfo:
    def __init__(self, size, kind):
        self.size = size
        self.kind = kind

class ArrayInfo:
    def __init__(self, size, element_type_id, element_count):
        self.size = size
        self.element_type_id = element_type_id
        self.element_count = element_count

class RecordInfo:
    def __init__(self, size):
        self.size = size

class FieldInfo:
    def __init__(self, record_id, offset):
        self.record_id = record_id
        self.offset = offset

class VarInfo:
    def __init__(self, symbol, kind, type_id, size):
        self.symbol = symbol
        self.kind = kind
        self.type_id = type_id
        self.size = size

class QuadInstruction:
    def __init__(self, op, attrs):
        self.op = op
        self.attrs = attrs

###############################################################################
# helpers
###############################################################################
def parse_value(text):
    if text == "":
        return ""
    if re.match(r"^-?\d+$", text): # integer literal
        return int(text)
    if len(text) >= 2 and text[0] == '"' and text[-1] == '"':
        return text[1:-1]
    return text

def parse_quad_file(path):
    instructions = []
    with open(path, "r", encoding="utf-8") as handle: # parse .quad file into a list of QuadInstruction
        for raw in handle:
            line = raw.strip()
            if not line:
                continue
            parts = line.split()
            op = parts[0]
            attrs = {}
            for part in parts[1:]:
                if "=" not in part:
                    continue
                key, value = part.split("=", 1)
                attrs[key] = parse_value(value)
            instructions.append(QuadInstruction(op, attrs))
    return instructions

def get_type_size(type_id, type_table, array_table, record_table):
    if type_id in type_table:
        return type_table[type_id].size
    if type_id in array_table:
        return array_table[type_id].size
    if type_id in record_table:
        return record_table[type_id].size
    return 4

def build_tables(instructions):
    type_table = {}
    array_table = {}
    record_table = {}
    field_table = {}
    var_table = {}

    for inst in instructions:
        op = inst.op
        a = inst.attrs
        if op == "TypeDecl":
            type_table[a["id"]] = TypeInfo(a["size"], a.get("symbol", ""))
        elif op == "ArrayDecl":
            array_table[a["id"]] = ArrayInfo(a["size"], a["elementTypeId"], a["elementCount"])
        elif op == "RecordDecl":
            record_table[a["id"]] = RecordInfo(a["size"])
        elif op == "FieldDecl":
            field_table[a["id"]] = FieldInfo(a["recordId"], a["offset"])
        elif op == "VarDecl":
            size = get_type_size(a["typeId"], type_table, array_table, record_table)
            if size == 0:
                size = 4
            var_table[a["id"]] = VarInfo(a["symbol"], a["kind"], a["typeId"], size)

    return type_table, array_table, record_table, field_table, var_table

def extract_main_instructions(instructions):
    main = []
    in_main = False
    for inst in instructions:
        if inst.op == "ProcBegin" and inst.attrs.get("symbol") == "$MAIN":
            in_main = True
            continue
        if inst.op == "ProcEnd" and in_main:
            break
        if in_main:
            main.append(inst)
    return main

def build_label_aliases(instructions):
    label_alias = {}
    for idx, inst in enumerate(instructions):
        if inst.op != "Label":
            continue
        j = idx + 1
        while j < len(instructions) and instructions[j].op == "Label":
            j += 1
        if j < len(instructions) and instructions[j].op == "Goto":
            # jump-to-jump elimination (x goto y => x alias to y)
            label_alias[inst.attrs["label"]] = instructions[j].attrs["label"]
    return label_alias

def resolve_label(label, label_alias):
    current = label
    seen = set()
    while current in label_alias and current not in seen:
        seen.add(current)
        current = label_alias[current]
    return current

def is_power_of_two(value):
    return value > 0 and (value & (value - 1)) == 0 # check if only one bit set 

def trunc_div(left, right):
    if right == 0:
        return 0
    neg = (left < 0) ^ (right < 0) # XOR to check if negative
    quotient = abs(left) // abs(right)
    return -quotient if neg else quotient

def trunc_mod(left, right):
    if right == 0:
        return 0
    return left - trunc_div(left, right) * right # mod = dividend - (dividend / divisor) * divisor

def gen_mul_by_const(lines, src_reg, const_val, dst_reg, temp_reg):
    # strength reduction (shifts/adds for constant multiplication)
    if const_val == 0:
        lines.append(f"li {dst_reg}, 0")
        return
    if const_val == 1:
        lines.append(f"move {dst_reg}, {src_reg}")
        return
    if const_val == -1:
        lines.append(f"sub {dst_reg}, $zero, {src_reg}")
        return

    abs_val = abs(const_val)
    first = True
    bit = 0
    while abs_val: # iterate through bits
        if abs_val & 1: # if bit is set, add shifted value
            if first: # first add can be move instead of add
                if bit == 0:
                    lines.append(f"move {dst_reg}, {src_reg}")
                else:
                    lines.append(f"sll {dst_reg}, {src_reg}, {bit}")
                first = False
            else: # remaining adds need to use temp register
                if bit == 0:
                    lines.append(f"add {dst_reg}, {dst_reg}, {src_reg}")
                else:
                    lines.append(f"sll {temp_reg}, {src_reg}, {bit}")
                    lines.append(f"add {dst_reg}, {dst_reg}, {temp_reg}")
        abs_val >>= 1 # shift to next bit
        bit += 1 # increment bit position

    if const_val < 0:
        lines.append(f"sub {dst_reg}, $zero, {dst_reg}")

def _strip_comment(line):
    return line.split("#", 1)[0].rstrip()

def _tokenize_inst(line):
    return [token.strip(",") for token in _strip_comment(line).split()]

def _is_label_line(line):
    text = _strip_comment(line).strip()
    return bool(re.match(r"^[A-Za-z_][A-Za-z0-9_]*:$", text)) # matches lines that are just a label (like L1:)

def _label_name(line):
    text = _strip_comment(line).strip()
    return text[:-1] if _is_label_line(text) else None # extract label

def _simplify(line):
    tokens = _tokenize_inst(line)
    if not tokens:
        return line
    op = tokens[0]
    if op == "add" and len(tokens) >= 4: # x + 0 => x
        dst, left, right = tokens[1], tokens[2], tokens[3]
        if right == "$zero":
            return f"move {dst}, {left}"
        if left == "$zero":
            return f"move {dst}, {right}"
    if op == "sub" and len(tokens) >= 4: # x - 0 => x
        dst, left, right = tokens[1], tokens[2], tokens[3]
        if right == "$zero":
            return f"move {dst}, {left}"
    if op == "addi" and len(tokens) >= 4: # x + 0 => x
        dst, src, imm = tokens[1], tokens[2], tokens[3]
        if imm == "0":
            return f"move {dst}, {src}"
    if op == "sll" and len(tokens) >= 4: # x << 0 => x
        dst, src, shamt = tokens[1], tokens[2], tokens[3]
        if shamt == "0":
            return f"move {dst}, {src}"
    return line

###############################################################################
# peephole optimization to apply 4-wide window of instruction simplifications and jump-to-jump elimination
###############################################################################
def peephole_optimize(lines):
    text_start = 0
    for idx, line in enumerate(lines): # find start of .text
        if line.strip() == ".text":
            text_start = idx + 1
            break

    window = 4
    lookahead = window - 1
    changed = True
    while changed: # optimize until no more changes
        changed = False
        i = text_start
        while i < len(lines):
            line = lines[i]
            if not line.strip() or line.strip().startswith("#"):
                i += 1
                continue

            # j L1 ; L1: => drop the jump (fall-through)
            tokens = _tokenize_inst(line)
            if i + lookahead < len(lines):
                if tokens and tokens[0] == "j" and len(tokens) == 2:
                    target = tokens[1]
                    if _label_name(lines[i + 1]) == target:
                        lines.pop(i)
                        changed = True
                        continue

                # j L1 ; L1: ; j L2 => j L2 (jump-to-jump)
                if tokens and tokens[0] == "j" and len(tokens) == 2:
                    label = _label_name(lines[i + 1])
                    next_tokens = _tokenize_inst(lines[i + 2])
                    if label == tokens[1] and next_tokens[:1] == ["j"] and len(next_tokens) == 2:
                        lines[i] = f"j {next_tokens[1]}"
                        changed = True
                        i += 1
                        continue

                # beq/bne ...; L1 ; L1: ; j L2 => branch to L2 (jump-to-jump)
                if tokens and tokens[0] in {"beq", "bne"} and len(tokens) == 4:
                    target = tokens[3]
                    if _label_name(lines[i + 1]) == target:
                        next_tokens = _tokenize_inst(lines[i + 2])
                        if next_tokens[:1] == ["j"] and len(next_tokens) == 2:
                            lines[i] = f"{tokens[0]} {tokens[1]}, {tokens[2]}, {next_tokens[1]}"
                            changed = True
                            i += 1
                            continue

            simplified = _simplify(line)
            if simplified != line:
                lines[i] = simplified
                changed = True
            i += 1

    return lines

###############################################################################
# main code generation function that translates list of QuadInstructions into MIPS assembly
###############################################################################
def generate_mips(main_instructions, type_table, array_table, record_table, field_table, var_table, optimize):
    lines = []
    var_labels = {var_id: f"var_{var_id}" for var_id in var_table}

    lines.append(".data")
    lines.append('newline: .asciiz "\\n"')
    for var_id in sorted(var_table.keys()):
        size = var_table[var_id].size
        if size >= 4:
            lines.append(".align 2")
        lines.append(f"{var_labels[var_id]}: .space {size}")

    lines.append(".text")
    lines.append(".globl main")
    lines.append("main:")

    const_map = {}
    # alias labels that immediately jump to another label to enable (jump-to-jump)
    label_alias = build_label_aliases(main_instructions) if optimize else {}

    # helpers for load/store variables, and to copy values for optimizations
    def gen_load_var(var_id, reg):
        size = var_table[var_id].size
        label = var_labels[var_id]
        if size == 1:
            lines.append(f"lb {reg}, {label}")
        else:
            lines.append(f"lw {reg}, {label}")

    def gen_store_var(var_id, reg):
        size = var_table[var_id].size
        label = var_labels[var_id]
        if size == 1:
            lines.append(f"sb {reg}, {label}")
        else:
            lines.append(f"sw {reg}, {label}")

    def gen_copy_value(src_id, dst_id):
        if src_id in const_map:
            lines.append(f"li $t0, {const_map[src_id]}")
            gen_store_var(dst_id, "$t0")
            const_map[dst_id] = const_map[src_id]
            return
        gen_load_var(src_id, "$t0")
        gen_store_var(dst_id, "$t0")
        const_map.pop(dst_id, None)

    # main instruction loop
    for inst in main_instructions:
        op = inst.op
        a = inst.attrs

        if op == "Label":
            lines.append(f"L{a['label']}:")
            continue

        if op == "Goto":
            label = resolve_label(a["label"], label_alias) if optimize else a["label"] # jump-to-jump
            lines.append(f"j L{label}")
            continue

        if op == "Branch":
            label = resolve_label(a["label"], label_alias) if optimize else a["label"] # jump-to-jump
            left_id = a["leftId"]
            right_id = a["rightId"]
            gen_load_var(left_id, "$t0")
            gen_load_var(right_id, "$t1")
            op_sym = a["op"]
            if op_sym == "=":
                lines.append(f"beq $t0, $t1, L{label}")
            elif op_sym == "#":
                lines.append(f"bne $t0, $t1, L{label}")
            elif op_sym == "<":
                lines.append("slt $t2, $t0, $t1")
                lines.append(f"bne $t2, $zero, L{label}")
            elif op_sym == ">":
                lines.append("slt $t2, $t1, $t0")
                lines.append(f"bne $t2, $zero, L{label}")
            elif op_sym == "<=":
                lines.append("slt $t2, $t1, $t0")
                lines.append(f"beq $t2, $zero, L{label}")
            elif op_sym == ">=":
                lines.append("slt $t2, $t0, $t1")
                lines.append(f"beq $t2, $zero, L{label}")
            else:
                lines.append(f"# Unsupported op {op_sym}")
            continue

        if op == "AddressOf":
            target_id = a["id"]
            result_id = a["resultId"]
            lines.append(f"la $t0, {var_labels[target_id]}")
            gen_store_var(result_id, "$t0")
            const_map.pop(result_id, None)
            continue

        if op == "LoadLit":
            result_id = a["resultId"]
            value = a["value"]
            lines.append(f"li $t0, {value}")
            gen_store_var(result_id, "$t0")
            const_map[result_id] = value
            continue

        if op == "Load":
            result_id = a["resultId"]
            addr_id = a["desId"]
            size = get_type_size(a["typeId"], type_table, array_table, record_table)
            gen_load_var(addr_id, "$t0")
            if size == 1:
                lines.append("lb $t1, 0($t0)")
            else:
                lines.append("lw $t1, 0($t0)")
            gen_store_var(result_id, "$t1")
            const_map.pop(result_id, None)
            continue

        if op == "Store":
            addr_id = a["leftId"]
            value_id = a["rightId"]
            size = get_type_size(a["typeId"], type_table, array_table, record_table)
            gen_load_var(addr_id, "$t0")
            gen_load_var(value_id, "$t1")
            if size == 1:
                lines.append("sb $t1, 0($t0)")
            else:
                lines.append("sw $t1, 0($t0)")
            continue

        if op == "UnaryExpr":
            expr_id = a["exprId"]
            result_id = a["resultId"]
            op_sym = a["op"]
            if optimize and expr_id in const_map: # constant folding
                value = const_map[expr_id]
                if op_sym == "-":
                    lines.append(f"li $t0, {-value}")
                    gen_store_var(result_id, "$t0")
                    const_map[result_id] = -value
                elif op_sym.lower() == "not":
                    folded = 0 if value else 1
                    lines.append(f"li $t0, {folded}")
                    gen_store_var(result_id, "$t0")
                    const_map[result_id] = folded
                else:
                    lines.append(f"# Unsupported op {op_sym}")
                    const_map.pop(result_id, None)
                continue

            gen_load_var(expr_id, "$t0")
            if op_sym == "-":
                lines.append("sub $t1, $zero, $t0")
            elif op_sym.lower() == "not":
                lines.append("seq $t1, $t0, $zero")
            else:
                lines.append(f"# Unsupported op {op_sym}")
                lines.append("move $t1, $t0")
            gen_store_var(result_id, "$t1")
            const_map.pop(result_id, None)
            continue

        if op == "BinExpr":
            left_id = a["leftId"]
            right_id = a["rightId"]
            result_id = a["resultId"]
            op_sym = a["op"]

            left_const = const_map.get(left_id)
            right_const = const_map.get(right_id)

            if optimize and left_const is not None and right_const is not None:
                # constant folding for constant expressions
                if op_sym == "+":
                    value = left_const + right_const
                elif op_sym == "-":
                    value = left_const - right_const
                elif op_sym == "*":
                    value = left_const * right_const
                elif op_sym == "/":
                    if right_const == 0:
                        value = 0
                    else:
                        value = trunc_div(left_const, right_const)
                elif op_sym == "%":
                    if right_const == 0:
                        value = 0
                    else:
                        value = trunc_mod(left_const, right_const)
                else:
                    value = None

                if value is not None:
                    lines.append(f"li $t0, {value}")
                    gen_store_var(result_id, "$t0")
                    const_map[result_id] = value
                    continue

            if optimize:
                # algebraic simplifications for zeros and ones
                if op_sym == "+" and right_const == 0:
                    gen_copy_value(left_id, result_id)
                    continue
                if op_sym == "+" and left_const == 0:
                    gen_copy_value(right_id, result_id)
                    continue
                if op_sym == "-" and right_const == 0:
                    gen_copy_value(left_id, result_id)
                    continue
                if op_sym == "*" and right_const == 1:
                    gen_copy_value(left_id, result_id)
                    continue
                if op_sym == "*" and left_const == 1:
                    gen_copy_value(right_id, result_id)
                    continue
                if op_sym == "*" and (right_const == 0 or left_const == 0):
                    lines.append("li $t0, 0")
                    gen_store_var(result_id, "$t0")
                    const_map[result_id] = 0
                    continue
                if op_sym == "/" and right_const == 1:
                    gen_copy_value(left_id, result_id)
                    continue
                if op_sym == "/" and left_const == 0:
                    lines.append("li $t0, 0")
                    gen_store_var(result_id, "$t0")
                    const_map[result_id] = 0
                    continue
                if op_sym == "%" and left_const == 0:
                    lines.append("li $t0, 0")
                    gen_store_var(result_id, "$t0")
                    const_map[result_id] = 0
                    continue

            if optimize and op_sym == "*" and (left_const is not None or right_const is not None):
                # strength reduction for multiply by constant (shift/add instead of mul)
                if left_const is not None:
                    const_val = left_const
                    gen_load_var(right_id, "$t0")
                else:
                    const_val = right_const
                    gen_load_var(left_id, "$t0")
                gen_mul_by_const(lines, "$t0", const_val, "$t1", "$t2")
                gen_store_var(result_id, "$t1")
                const_map.pop(result_id, None)
                continue

            gen_load_var(left_id, "$t0")
            gen_load_var(right_id, "$t1")
            if op_sym == "+":
                lines.append("add $t2, $t0, $t1")
            elif op_sym == "-":
                lines.append("sub $t2, $t0, $t1")
            elif op_sym == "*":
                lines.append("mul $t2, $t0, $t1")
            elif op_sym == "/":
                lines.append("div $t0, $t1")
                lines.append("mflo $t2")
            elif op_sym == "%":
                lines.append("div $t0, $t1")
                lines.append("mfhi $t2")
            else:
                lines.append(f"# Unsupported op {op_sym}")
                lines.append("move $t2, $t0")
            gen_store_var(result_id, "$t2")
            const_map.pop(result_id, None)
            continue

        if op == "IndexOf":
            base_id = a["baseId"]
            index_id = a["indexId"]
            result_id = a["resultId"]
            array_id = a["arrayId"]
            element_size = get_type_size(array_table[array_id].element_type_id, type_table, array_table, record_table)
            gen_load_var(base_id, "$t0")
            gen_load_var(index_id, "$t1")
            if optimize and is_power_of_two(element_size): # strength reduction
                shift = int(element_size).bit_length() - 1 # log2 of size for shift amount
                lines.append(f"sll $t2, $t1, {shift}")
            else:
                lines.append(f"li $t2, {element_size}")
                lines.append("mul $t2, $t1, $t2")
            lines.append("add $t3, $t0, $t2")
            gen_store_var(result_id, "$t3")
            const_map.pop(result_id, None)
            continue

        if op == "FieldOf":
            base_id = a["baseId"]
            result_id = a["resultId"]
            field_id = a["fieldId"]
            offset = field_table[field_id].offset
            gen_load_var(base_id, "$t0")
            if -32768 <= offset <= 32767: # check if offset is 16 bits for addi
                lines.append(f"addi $t1, $t0, {offset}")
            else:
                lines.append(f"li $t2, {offset}")
                lines.append("add $t1, $t0, $t2")
            gen_store_var(result_id, "$t1")
            const_map.pop(result_id, None)
            continue

        if op == "Write":
            expr_id = a["exprId"]
            gen_load_var(expr_id, "$a0")
            lines.append("li $v0, 1")
            lines.append("syscall")
            continue

        if op == "WriteLn":
            lines.append("li $v0, 4")
            lines.append("la $a0, newline")
            lines.append("syscall")
            continue

    lines.append("li $v0, 10")
    lines.append("syscall")

    if optimize:
        lines = peephole_optimize(lines)

    return lines

def main():
    args = sys.argv[1:]
    if len(args) < 2:
        print("Usage: luca_mips <input.quad> <output.s> [-O]", file=sys.stderr)
        sys.exit(1)

    input_path = args[0]
    output_path = args[1]
    optimize = False
    if len(args) >= 3 and args[2] == "-O":
        optimize = True

    instructions = parse_quad_file(input_path)
    type_table, array_table, record_table, field_table, var_table = build_tables(instructions)
    main_instructions = extract_main_instructions(instructions)

    lines = generate_mips(main_instructions, type_table, array_table, record_table, field_table, var_table, optimize)
    with open(output_path, "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines))
        handle.write("\n")

if __name__ == "__main__":
    main()
