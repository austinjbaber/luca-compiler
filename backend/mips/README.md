# Experimental MIPS translator

`luca_mips` translates the historical textual `.quad` representation from
coursework part 8 into MIPS assembly. It is a standalone experiment and is not
an alternative backend for the supported `lucac` command:

```text
.quad file -> luca_mips -> MIPS assembly
```

The active compiler follows a separate pipeline:

```text
.luc source -> lucac -> Luca VM code -> Luca VM runtime
```

There is no `.luc`-to-`.quad` pass in this repository. Consequently, the MIPS
translator cannot compile the Luca examples and should not be described as an
end-to-end compiler backend.

## Build and use

Build the translator with the rest of the project or by itself:

```sh
make build
make mips
```

Then translate an existing `.quad` file:

```sh
backend/mips/build/luca_mips input.quad output.s
backend/mips/build/luca_mips input.quad output.s -O
```

On Windows the executable has an `.exe` suffix. `-O` enables the prototype's
constant folding, algebraic simplifications, constant-multiply strength
reduction, and jump cleanup.

## Scope and limitations

The translator handles the `$MAIN` procedure, static variable storage,
integer and byte loads/stores, integer arithmetic, branches, array indexing,
record-field addresses, and integer writes. It emits the syscall conventions
used by educational MIPS simulators. It does not lower other procedures,
strings, floating-point operations, or the full Luca language, and the test
suite validates emitted assembly text rather than executing it in a simulator.

The implementation in `luca_mips.c` is a C port of the original Python
prototype preserved under `part-8/`. The golden regressions cover both normal
and optimized translation without requiring Python.
