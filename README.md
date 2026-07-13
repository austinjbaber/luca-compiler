# Luca Compiler

Luca Compiler is a consolidation of eight incremental compiler-course
assignments into a maintainable compiler and virtual-machine project. The
active Java compiler provides lexical analysis, parsing, AST construction,
semantic analysis, and code generation for the Luca stack VM.

The intended end-to-end pipeline is:

```text
.luc source -> lexer -> parser -> AST -> semantic analysis -> VM code -> VM runtime
```

The supported code generator covers scalar expressions, variables, I/O,
control flow, procedures, arrays, records, strings, and floating-point
operations. The experimental `.quad`-to-MIPS translator remains separate from
the source-to-VM pipeline.

## Implementation languages

The project uses each language at a deliberate architectural boundary:

- Java implements compiler analysis and transformation passes: lexing,
  parsing, AST construction, semantic analysis, and VM code generation.
- C implements the low-level VM runtime, including its operand stack, memory
  representation, procedure frames, and instruction dispatch.
- Bash provides thin build, launcher, and regression-test orchestration.

This keeps the compiler convenient to extend while preserving direct systems
programming experience in the component that executes generated code.

## Requirements

- JDK 11 or newer
- A C compiler with GNU C support (GCC or Clang)
- GNU Make
- Bash (Git for Windows provides it on Windows)

## Build and use

```sh
make build
bin/lucac lex tests/lexer/int.luc
bin/lucac parse tests/parser/prog1.luc
bin/lucac check tests/semantics/INT/OK1.luc
bin/lucac compile part-7/myprog.luc myprog.vm
runtime/build/luca_vm_switch myprog.vm
make test
```

`compile` writes VM text to standard output when its output argument is omitted.
`check` produces no output for a semantically valid program and writes
diagnostics to standard error for an invalid program.

The regression suite is organized by compiler stage under `tests/`. The
code-generation suite compiles focused language fixtures and executes them on
the supported switch VM.

## Repository status

The repository is being migrated according to
[`docs/merge-strategy.md`](docs/merge-strategy.md). The `part-1` through
`part-8` directories preserve the original assignment snapshots during the
migration; active compiler code lives under `compiler/`.

The supported VM uses portable switch dispatch. A GNU C computed-goto version
is retained as an experimental dispatch study, not as a feature-equivalent
runtime: it predates the supported runtime's procedure-frame implementation.

This project originated as coursework at the University of Arizona. See the
source headers for attribution on inherited course framework code.
