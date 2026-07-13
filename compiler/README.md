# Luca compiler

This directory contains the active Luca lexer, parser, AST, semantic analyzer,
and AST-to-VM code generator.

Build it from a POSIX shell with:

```sh
make -C compiler build
```

The `bin/lucac` entry point exposes the front-end stages and VM compilation:

```sh
bin/lucac lex source.luc
bin/lucac parse source.luc
bin/lucac check source.luc
bin/lucac compile source.luc output.vm
```

`lex` and `parse` write their XML-like representations to standard output.
`check` is silent for a valid program and writes semantic diagnostics to
standard error. If `compile` is given no output path, it writes VM text to
standard output.
