# Luca Compiler

Luca Compiler is a consolidation of eight incremental compiler-course
assignments into a maintainable compiler and virtual-machine project. The
active Java front end currently provides lexical analysis, parsing, AST
construction, and semantic analysis for the Luca language.

The intended end-to-end pipeline is:

```text
.luc source -> lexer -> parser -> AST -> semantic analysis -> VM code -> VM runtime
```

VM code generation is not implemented yet. The VM interpreters and the
experimental `.quad`-to-MIPS translator remain in their coursework directories
until their respective consolidation stages.

## Requirements

- JDK 11 or newer
- GNU Make
- A POSIX-compatible shell for `bin/lucac`

## Build and use

```sh
make build
bin/lucac lex part-5/tests/INT/OK1.luc
bin/lucac parse part-5/tests/INT/OK1.luc
bin/lucac check part-5/tests/INT/OK1.luc
make test
```

`check` produces no output for a semantically valid program and writes
diagnostics to standard error for an invalid program.

## Repository status

The repository is being migrated according to
[`docs/merge-strategy.md`](docs/merge-strategy.md). The `part-1` through
`part-8` directories preserve the original assignment snapshots during the
migration; active compiler code lives under `compiler/`.

This project originated as coursework at the University of Arizona. See the
source headers for attribution on inherited course framework code.
