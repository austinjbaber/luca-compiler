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
this repository.

## Source Of Truth

| Final component | Source assignment | Decision |
| --- | --- | --- |
| Lexer, parser, AST | `part-5` | Keep; it subsumes the earlier Java stages. |
| Semantic analysis and symbol table | `part-5` | Keep as the active front end. |
| VM interpreter | `part-7` | Keep as the most complete switch-dispatch runtime. |
| Indirect-dispatch interpreter | `part-6` | Retain as an optional runtime experiment. |
| MIPS translator | `part-8` | Retain as an experimental backend. |
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
    src/luca_vm_indirect.c
  backend/
    mips/luca_mips.py
  tests/
    lexer/
    parser/
    semantics/
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

1. Create repository hygiene.
   Add the root README, license, `.gitignore`, build entry points, and CI. Start
   a fresh Git history because the current workspace is not a Git repository.

2. Consolidate the Java front end.
   Move the part-5 Java packages under `compiler/src/main/java` without changing
   behavior. Replace ad-hoc shell launchers with one CLI that exposes `lex`,
   `parse`, and `check` subcommands.

3. Normalize the test suite.
   Migrate the strongest lexer fixtures from part 2, parser/AST fixtures from
   part 4, and semantic fixtures from part 5. Keep expected outputs only when
   they assert behavior; remove duplicate `my_*` result captures.

4. Consolidate the VM runtime.
   Start from part 7's switch interpreter, which adds real procedure-frame
   handling beyond the part-6 version. Extract opcode constants into a shared
   header and retain the part-6 indirect-dispatch runtime behind a separate
   build target.

5. Build the missing code-generation bridge.
   Add a small, well-tested AST-to-VM emitter in `compiler/.../codegen`. Begin
   with integer expressions, variables, writes, and control flow; then add
   procedures, arrays, records, strings, and floating-point operations. This is
   the work that converts the collection into a real end-to-end compiler.

6. Add end-to-end demonstrations.
   Each `tests/e2e` fixture contains a `.luc` program and expected stdout. The
   README's quick-start command must compile and run an example from source,
   rather than execute a checked-in `.vm` artifact.

7. Position the MIPS translator honestly.
   Keep it executable and documented as a `.quad` to MIPS prototype. Add a
   source-to-quad emitter only after the VM pipeline is stable; do not advertise
   MIPS compilation as an end-to-end feature before that bridge exists.

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

## Commit Plan

1. `chore: initialize portfolio repository and developer tooling`
2. `refactor: consolidate Java frontend from course milestones`
3. `test: migrate lexer parser and semantic regression fixtures`
4. `refactor: consolidate VM runtimes and opcode definitions`
5. `feat: add VM code generator and lucac CLI`
6. `test: add end-to-end compiler execution coverage`
7. `docs: publish architecture language reference and project demo`
