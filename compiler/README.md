# Luca front end

This directory contains the active Luca lexer, parser, AST, and semantic
analyzer consolidated from coursework part 5.

Build it from a POSIX shell with:

```sh
make -C compiler build
```

The `bin/lucac` entry point exposes the three front-end stages:

```sh
bin/lucac lex source.luc
bin/lucac parse source.luc
bin/lucac check source.luc
```

`lex` and `parse` write their XML-like representations to standard output.
`check` is silent for a valid program and writes semantic diagnostics to
standard error.
