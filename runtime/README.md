# Luca VM runtimes

The active runtime is `luca_vm_switch`, consolidated from coursework part 7.
It uses portable switch dispatch and implements procedure call frames.

`luca_vm_indirect`, retained from part 6, is a dispatch experiment
using GNU C computed gotos. It implements the earlier instruction behavior and
does not support part 7's procedure-frame semantics, so it is not a supported
drop-in alternative to `luca_vm_switch`.

Both interpreters consume Luca's textual `.vm` format and share the serialized
opcode definitions in `include/opcodes.h`.

```sh
make -C runtime build
runtime/build/luca_vm_switch program.vm
runtime/build/luca_vm_indirect program.vm
```
