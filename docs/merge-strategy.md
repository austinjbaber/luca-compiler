# Luca Compiler Consolidation Strategy

## Goal

Turn eight incremental course assignments into one maintainable, demonstrable
compiler project. The published repository should make the active compiler easy
to build, run, test, and understand without exposing generated files or course
submission scaffolding as product code.

The primary deliverable is an end-to-end `lucac` command:

```text
.luc source -> lexer -> parser -> AST -> semantic analysis -> VM code -> VM runtime
```

The MIPS translator is retained as an experimental backend because it consumes
`.quad` input, not VM code, and the source-to-quad generator is not present in
this repository. Its active implementation will be ported from Python to C so
the finished project has an intentional Java compiler, C runtime/backend, and
Bash tooling boundary.

## Source Of Truth

| Final component | Source assignment | Decision |
| --- | --- | --- |
| Lexer, parser, AST | `part-5` | Keep; it subsumes the earlier Java stages. |
| Semantic analysis and symbol table | `part-5` | Keep as the active front end. |
| VM interpreter | `part-7` | Keep as the most complete switch-dispatch runtime. |
| Indirect-dispatch interpreter | `part-6` | Retain as an experimental GNU C dispatch study, not a feature-equivalent runtime. |
| MIPS translator | `part-8` | Port the `.quad` translator to C and retain it as an experimental backend. |
| Tiny language prototype | `part-1` | Preserve only as a historical artifact. |
| Earlier lexer/parser/AST milestones | `part-2` through `part-4` | Replace with focused regression fixtures and a migration note. |

`part-4` and `part-5` already share 51 identical Java source files. The
remaining three shared AST files differ only because part 5 extends the
semantic-analysis stage. Moving the part-5 source avoids a manual merge of
those stages.

## Final Repository Shape

```text
luca-compiler/
  README.md
  LICENSE
  .gitignore
  Makefile
  docs/
    architecture.md
    language-reference.md
    coursework-history.md
    merge-strategy.md
  examples/
    hello.luc
    mergesort.luc
  compiler/
    src/main/java/
      lexer/
      parser/
      ast/
      sem/
      sym/
      auxx/
      codegen/               # new: AST to VM code
      cli/                   # new: lucac command-line interface
  runtime/
    include/opcodes.h        # new: shared VM instruction definitions
    src/luca_vm_switch.c
    experimental/
      luca_vm_indirect.c
  backend/
    mips/luca_mips.c
  tests/
    lexer/
    parser/
    semantics/
    runtime/
    e2e/
    fixtures/
  archive/
    tiny-source/
  .github/workflows/ci.yml
```

Only active source, hand-written tests, small examples, and useful architecture
artifacts belong in the top-level project. Java `.class` files, Windows `.exe`
files, test-result directories, generated XML/Graphviz/assembly output, and
course-platform JSON metadata are not versioned.

## Migration Sequence

1. [x] Create repository hygiene.
   Added the root README, license, `.gitignore`, portable build entry points,
   and GitHub Actions CI. Removed course-platform JSON metadata and tracked
   generated binaries/results.

2. [x] Consolidate the Java front end.
   Moved the part-5 Java packages under `compiler/src/main/java` without
   changing behavior. Replaced the active ad-hoc launchers with one CLI that
   exposes `lex`, `parse`, and `check` subcommands.

3. [x] Normalize the test suite.
   Migrated 20 lexer fixtures from part 2, 58 parser/AST fixtures from part 4,
   and 78 semantic fixtures from part 5. The Bash regression runner performs
   comparisons in temporary storage; legacy `test_results` directories and
   duplicate `my_*` captures were removed.

4. [x] Consolidate the VM runtime.
   Established part 7's switch interpreter as the supported runtime, extracted
   all 57 serialized opcodes into a shared header, and added arithmetic and
   procedure-frame regressions. Retained part 6's incomplete indirect-dispatch
   implementation as a clearly labeled experiment.

5. [x] Build the missing code-generation bridge.
   Add a small, well-tested AST-to-VM emitter in `compiler/.../codegen`. Begin
   with integer expressions, variables, writes, and control flow; then add
   procedures, arrays, records, strings, and floating-point operations. This is
   the work that converts the collection into a real end-to-end compiler.

6. Add end-to-end demonstrations.
   Each `tests/e2e` fixture contains a `.luc` program and expected stdout. The
   README's quick-start command must compile and run an example from source,
   rather than execute a checked-in `.vm` artifact.

7. Position the MIPS translator honestly.
   Port the Python prototype to C and document it as a `.quad` to MIPS
   experiment. Add a source-to-quad emitter only after the VM pipeline is
   stable; do not advertise MIPS compilation as an end-to-end feature before
   that bridge exists.

## Current Status

Steps 1 through 4 are complete. A clean build
compiles the Java front end and both C runtime implementations. The regression
suite contains 156 front-end cases and three VM cases. The next task is step 5:
the AST-to-VM code-generation bridge. GL me.

## Definition Of Done

- `make build` builds the Java compiler and C VM runtime from a clean checkout.
- `make test` runs unit/regression and end-to-end tests with no generated files
  required from the course environment.
- `./bin/lucac run examples/mergesort.luc` produces the documented output.
- GitHub Actions runs the same build and test commands on Ubuntu.
- The README includes a short language sample, architecture diagram, supported
  features, limitations, and attribution to the University of Arizona course.
- The repository contains no compiled binaries, generated reports, or private
  course-platform metadata.

## Remaining Plan

1. `refactor: move indirect VM behind experimental build target`
2. `test: add end-to-end compiler execution coverage`
3. `refactor: port experimental MIPS translator to C`
4. `docs: publish architecture language reference and project demo`