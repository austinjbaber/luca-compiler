# Regression tests

The consolidated regression suite is grouped by compiler stage:

- `lexer/` contains 20 source/output pairs migrated from coursework part 2.
- `parser/` contains 58 source/AST pairs migrated from coursework part 4.
- `semantics/` contains 78 source/diagnostic pairs migrated from coursework
  part 5.
- `runtime/` contains shared VM instruction regressions plus a switch-runtime
  procedure-frame regression.
- `codegen/` contains focused source/output pairs for AST-to-VM lowering.
- `e2e/` contains complete source programs and their expected stdout, exercised
  through the public `lucac run` command.

Run every group with `make test`, or run a single group after `make build`:

```sh
bash tests/run_frontend_regressions.sh lexer
bash tests/run_frontend_regressions.sh parser
bash tests/run_frontend_regressions.sh semantics
bash tests/run_codegen_regressions.sh
bash tests/run_e2e_regressions.sh
```

Expected files are hand-maintained assertions. The runner captures compiler
output in memory and does not create `my_*` files or test-result directories.
Line endings are normalized so the same expectations work on Windows and
Linux.

During migration, six stale expectations were aligned with the active part-5
front end: the lexer stops after its first error, parser output has consistent
blank-line formatting, and current semantic diagnostics reflect parser and
message-format fixes already present in part 5.
