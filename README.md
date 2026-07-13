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

## Quick start

Build the compiler and runtime, then compile and execute merge sort directly
from Luca source:

```sh
make build
sh bin/lucac run examples/mergesort.luc
```

The example prints:

```text
Before:
7 2 6 3 8 1 5 4
After:
1 2 3 4 5 6 7 8
```

The individual compiler stages are also available:

```sh
bin/lucac lex tests/lexer/int.luc
bin/lucac parse tests/parser/prog1.luc
bin/lucac check tests/semantics/INT/OK1.luc
bin/lucac compile examples/hello.luc hello.vm
make test
```

`compile` writes VM text to standard output when its output argument is omitted.
`check` produces no output for a semantically valid program and writes
diagnostics to standard error for an invalid program. `run` uses a temporary
VM file and removes it after execution.

The regression suite is organized by compiler stage under `tests/`. The
code-generation suite compiles focused language fixtures and executes them on
the supported switch VM. The end-to-end suite exercises the public `lucac run`
command with complete programs and expected stdout.

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
