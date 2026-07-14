# Luca Compiler Evolution Strategy

## North Star

The original consolidation is complete: Luca can compile source programs to
the Luca stack VM and execute them end to end. The next phase turns that
single-target compiler into a small multi-backend compiler built around a new,
typed intermediate representation.

```text
                              +-> Luca VM code -> switch VM runtime
.luc -> lexer -> parser -> semantic AST -> typed IR
                              +-> x86-64 assembly -> native runtime -> executable
```

The VM remains the supported reference implementation while the IR and native
backend are developed. Native work must not weaken the existing source-to-VM
pipeline or its regression coverage.

## Architecture Decision

Luca will use a target-independent, typed, three-address IR rather than:

- generating C and delegating all native code generation to a C compiler;
- translating the existing stack-VM bytecode to native instructions;
- maintaining independent AST walkers for every backend; or
- reviving the historical `.quad` format unchanged.

This route keeps one definition of language lowering while leaving storage
layout, calling conventions, instruction selection, and runtime integration to
the backends. It also makes the compiler's internal decisions inspectable and
testable without requiring either runtime.

The first native target will be x86-64 System V on Linux, using GNU assembler
syntax and the host C compiler driver for assembling and linking. This target
is reproducible in the existing Ubuntu CI environment. Windows x64 support is
a possible later target and must be implemented as a separate ABI layer rather
than hidden behind platform conditionals throughout the emitter.

## Current System

The active compiler currently has no general-purpose IR boundary:

```text
semantic AST -> CodeGenerator -> VmEmitter -> serialized VM instructions
```

`CodeGenerator` currently performs three jobs at once:

1. lays out globals, procedure frames, arrays, and records;
2. lowers AST expressions and control flow; and
3. selects stack-VM instructions.

The semantic symbol objects also carry mutable target sizes and offsets through
`Arch`, which makes compiling the same program for multiple targets fragile.
The IR migration will separate semantic types from backend data layout.

### Status of `.quad` and MIPS

The historical `.quad` representation is not produced or consumed by the
active Luca compiler. It is used only as input to the standalone part-8 MIPS
translator and its two golden regressions:

```text
historical .quad fixture -> experimental MIPS translator -> MIPS assembly
```

That format is a useful record of three-address-code coursework, but it is not
a suitable shared IR as written. It is textual, largely integer-oriented,
centered on static storage and `$MAIN`, and does not model the complete set of
procedures, aggregates, strings, floating-point operations, and runtime
behavior supported by the current compiler.

The new IR may borrow operation names and ideas from `.quad`, but it has no
compatibility requirement. The MIPS translator will move to the archive when
the repository is reorganized; it will not constrain the x86-64 design.

## Typed IR Contract

The first IR will be deliberately smaller than LLVM and will not require SSA,
phi nodes, register allocation, or an optimizer. Source variables remain in
explicit storage, while expression results use typed, single-assignment virtual
temporaries. A procedure contains labeled basic blocks, and every block ends in
an explicit control-flow instruction.

### Core model

- A module owns type declarations, string constants, globals, and procedures.
- A procedure owns formal parameters, local slots, virtual temporaries, and
  basic blocks.
- Every temporary and storage slot has an IR type.
- Scalar types initially include `i64`, `f64`, `bool`, `char`, `string`, and
  `addr<T>`.
- Aggregate types describe arrays and records without committing to byte
  offsets in the frontend.
- Instructions use at most one result and a small number of operands.
- Terminators are `jump`, conditional `branch`, and procedure `return`/end.
- Calls, reads, writes, and allocation are explicit operations rather than
  hidden emitter behavior.

A representative fragment should be readable in a deterministic debug form:

```text
proc @main() {
entry:
  %0:i64 = const 7
  %1:addr<i64> = global_addr @value
  store %1, %0
  %2:i64 = load %1
  write.i64 %2
  writeln
  return
}
```

The debug syntax is a serializer for tests and diagnostics, not the compiler's
in-memory API. Backends consume verified Java IR objects directly.

### Target-independent lowering

AST-to-IR lowering owns decisions that must be identical for every backend:

- expression evaluation order;
- short-circuit boolean control flow;
- constant materialization and numeric conversions;
- variable, formal, field, and index identity;
- procedure calls and reference/value argument intent; and
- the observable order of reads, writes, and other side effects.

The lowerer must not choose VM opcodes, machine registers, byte offsets, stack
frame offsets, assembly labels, or a host calling convention.

### Backend-owned layout

Each backend owns a data-layout and frame-layout implementation. Logical IR
operations such as `field_addr` and `index_addr` retain enough type information
for the backend to calculate offsets safely.

- The VM layout preserves its current eight-byte scalar/address slots and
  serialized instruction format.
- The x86-64 layout defines native sizes, alignments, globals, stack frames,
  and ABI-visible arguments.
- Semantic analysis describes types and relationships; it no longer mutates
  shared symbol objects to install target-specific sizes or offsets.

An IR verifier will reject unresolved blocks, type-invalid operands, malformed
calls, use-before-definition, unterminated blocks, and invalid aggregate access
before either backend runs.

## Backend Strategy

### VM backend

The first consumer of the new IR is a replacement for the direct AST-to-VM
emitter. The VM is the migration oracle because its behavior is already covered
end to end.

During migration, the old `CodeGenerator` remains available behind an internal
legacy path. The IR-backed VM emitter becomes the default only after every
currently supported language feature passes the existing code-generation and
end-to-end suites. Once parity is established, the direct AST emitter is
removed rather than maintained as a third backend path.

### x86-64 backend

The initial x86-64 emitter favors correctness and readable assembly over
optimization:

- GNU assembler syntax for x86-64 System V;
- one assembly function per Luca procedure;
- straightforward stack slots for virtual temporaries before register
  allocation is considered;
- explicit prologues, epilogues, branches, and calls;
- a small C runtime shim for strings, reads, writes, allocation, and process
  startup; and
- the host C compiler driver for assembly and linking.

The native backend must implement Luca semantics, not expose accidental C or
machine behavior. Integer division, conversions, aggregate passing, bounds
checks, string representation, and reference parameters require explicit
contracts and focused regressions.

The intended command-line surface is:

```text
lucac compile --target vm program.luc program.vm
lucac emit-ir program.luc
lucac compile --target x86_64-linux program.luc program
```

Exact argument spelling may change during implementation, but targets must be
explicit and scriptable. The existing `lucac compile` behavior remains a
backward-compatible alias for the VM target.

## Repository Reorganization

The finished shape should distinguish active product code from preserved
coursework and retired experiments:

```text
luca-compiler/
  compiler/
    src/main/java/
      lexer/
      parser/
      ast/
      sem/
      sym/
      ir/                    # typed IR, verifier, deterministic printer
      lowering/              # semantic AST to IR
      codegen/
        vm/                  # IR to Luca VM
        x86/                 # IR to x86-64 assembly
      cli/
  runtime/
    include/
    src/                     # supported switch VM
    native/                  # x86-64 runtime shim
  tests/
    lexer/
    parser/
    semantics/
    ir/
    codegen/
    runtime/
    native/
    e2e/
  examples/
  docs/
  archive/
    coursework/
      part-1/
      part-2/
      part-3/
      part-4/
      part-5/
      part-6/
      part-7/
      part-8/
    experiments/
      mips-quad/
      indirect-vm/
```

The existing top-level `part-1` through `part-8` directories will move intact
under `archive/coursework/` so history and attribution remain visible without
presenting assignments as active components. The ported C MIPS translator,
original Python prototype, documentation, and golden fixtures will move under
`archive/experiments/mips-quad/`. The incomplete indirect-dispatch VM will also
leave the default build and move under `archive/experiments/indirect-vm/`.

Archived code is preserved for study, excluded from `make build` and
`make test`, and not imported by active source. A coursework-history document
will map each archived assignment to the active component or regression that
superseded it.

## Migration Sequence

Every step must leave the supported VM pipeline buildable and tested.

1. [ ] Archive coursework snapshots and retired experiments.
   Move `part-1` through `part-8` under `archive/coursework`, move the MIPS and
   indirect-VM experiments under `archive/experiments`, remove them from the
   default build, and record provenance. Do not delete historical source.

2. [ ] Establish the typed IR foundation.
   Add module, type, value, storage, procedure, block, instruction, and
   terminator classes. Add a verifier and deterministic text printer with
   focused unit/golden tests. No production backend changes in this step.

3. [ ] Lower scalar expressions and control flow to IR.
   Introduce a fresh `AstLowerer` covering integer/boolean constants,
   variables, assignment, arithmetic, comparisons, writes, conditionals, and
   loops. Expose `emit-ir` for inspection while the old VM path remains the
   default.

4. [ ] Add an IR-to-VM backend and reach scalar parity.
   Emit the existing serialized VM format from IR and run both VM compilation
   paths against the focused code-generation fixtures. Preserve public CLI and
   runtime behavior.

5. [ ] Complete IR and VM feature parity.
   Add procedures, formals, reference arguments, arrays, records, characters,
   strings, reads, floating-point operations, and conversions in vertical
   slices. Each slice requires IR and VM regression coverage before the next.

6. [ ] Make IR the only production lowering path.
   Switch normal VM compilation to `AST -> IR -> VM`, remove the legacy direct
   AST-to-VM emitter, and replace mutable global `Arch` layout state with
   backend-owned layout objects.

7. [ ] Build the x86-64 integer/control-flow slice.
   Emit assembly for globals, locals, integer expressions, assignment,
   comparisons, branches, loops, and integer output. Assemble, link, and run
   native fixtures in CI through the C runtime shim.

8. [ ] Complete native feature parity.
   Add procedures and reference parameters, arrays and records, characters and
   strings, reads, floating-point operations, conversions, and runtime checks.
   Run every portable end-to-end Luca program against both VM and native
   targets and compare observable output.

9. [ ] Publish the multi-backend project.
   Update architecture and language-reference documents, document both target
   contracts and limitations, add native quick-start examples, and retain the
   VM as the portable reference target.

## Testing Strategy

- IR unit tests construct valid and invalid modules without parsing Luca.
- IR golden tests compile small Luca programs and compare deterministic IR.
- VM backend tests preserve current instruction-level and stdout regressions.
- Native backend tests inspect focused assembly only where instruction shape is
  part of the contract; most tests assemble, link, and execute programs.
- Cross-backend tests compile the same source for VM and x86-64 and compare
  stdout, exit status, and defined runtime failures.
- CI always tests the frontend and VM; Linux CI additionally builds and runs
  the x86-64 target.

## Phase-Two Definition Of Done

- No `part-*` coursework directories remain at repository root.
- Archived coursework and experiments are documented and excluded from normal
  builds and tests.
- One AST-to-IR lowerer supplies both production backends.
- The IR is typed, verified, deterministically printable, and free of target
  byte offsets and opcodes.
- Existing VM programs and regressions continue to pass through the IR-backed
  VM emitter.
- `lucac` can produce and run an x86-64 Linux executable for every language
  feature supported by the VM target.
- Portable end-to-end fixtures produce matching observable results on VM and
  native targets.
- Generated binaries, assembly, object files, VM files, and reports remain
  untracked.
- GitHub Actions builds and tests the same supported paths documented for
  developers.

## Out Of Scope For This Phase

- Optimizing IR passes, SSA conversion, phi nodes, and register allocation.
- JIT compilation or translating VM bytecode to native code.
- Reconnecting the historical `.quad` or MIPS backend to production.
- Windows x64, macOS, ARM64, object-file emission without an assembler, and
  cross-compilation.
- Language redesign unrelated to backend correctness.

## Completed Consolidation Record

The first consolidation phase remains part of the project history:

1. [x] Added repository hygiene, portable build entry points, and CI.
2. [x] Consolidated the part-5 Java frontend under `compiler/src/main/java`.
3. [x] Migrated 156 lexer, parser, and semantic regressions.
4. [x] Established part 7's switch VM as the supported runtime and retained the
   indirect VM as an explicitly incomplete experiment.
5. [x] Built the direct AST-to-VM bridge for scalar values, control flow,
   procedures, arrays, records, strings, and floating-point operations.
6. [x] Added focused code-generation tests and four source-to-runtime examples.
7. [x] Ported the isolated `.quad`-to-MIPS experiment from Python to C and
   documented the missing source-to-quad bridge.

This completed phase is the behavioral baseline for the IR migration, not an
architecture that must remain frozen internally.

## Suggested Commit Sequence

1. `docs: plan typed IR and x86-64 backend migration`
2. `chore: archive coursework snapshots and retired experiments`
3. `feat: add typed IR model verifier and printer`
4. `feat: lower scalar Luca programs to typed IR`
5. `feat: emit scalar VM programs from typed IR`
6. `feat: complete typed IR and VM feature parity`
7. `refactor: make typed IR the production lowering boundary`
8. `feat: add x86-64 integer backend and native runtime`
9. `feat: complete x86-64 backend feature parity`
10. `docs: publish multi-backend architecture and native demos`
