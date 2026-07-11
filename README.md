# Luca Compiler

Luca Compiler is a consolidation of eight incremental compiler-course
assignments into a maintainable compiler and virtual-machine project. The
active Java front end currently provides lexical analysis, parsing, AST
construction, and semantic analysis for the Luca language.

The intended end-to-end pipeline is:

```text
.luc source -> lexer -> parser -> AST -> semantic analysis -> VM code -> VM runtime
```

VM code generation is not implemented yet. The consolidated VM runtimes and
the experimental `.quad`-to-MIPS translator are currently separate from the
Java front end; the missing code-generation bridge is the next major stage.

## Implementation languages

The project uses each language at a deliberate architectural boundary:

- Java implements compiler analysis and transformation passes: lexing,
  parsing, AST construction, semantic analysis, and eventually VM code
  generation.
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
make test
```

`check` produces no output for a semantically valid program and writes
diagnostics to standard error for an invalid program.

The regression suite is organized by compiler stage under `tests/`. Run one
stage directly with `bash tests/run_regressions.sh lexer`, `parser`, or
`semantics`.

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
