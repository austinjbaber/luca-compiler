CPPFLAGS ?= -Iinclude
CFLAGS ?= -std=c11 -Wall -Wextra -Wpedantic -O2
INDIRECT_CFLAGS ?= -std=gnu11 -Wall -Wextra -O2
BUILD_DIR := build

ifeq ($(OS),Windows_NT)
CC := gcc
EXE := .exe
CREATE_BUILD_DIR = if not exist "$(BUILD_DIR)" mkdir "$(BUILD_DIR)"
REMOVE_BUILD_DIR = if exist "$(BUILD_DIR)" rmdir /s /q "$(BUILD_DIR)"
else
CC ?= cc
EXE :=
CREATE_BUILD_DIR = mkdir -p $(BUILD_DIR)
REMOVE_BUILD_DIR = rm -rf $(BUILD_DIR)
endif

SWITCH_VM := $(BUILD_DIR)/luca_vm_switch$(EXE)
INDIRECT_VM := $(BUILD_DIR)/luca_vm_indirect$(EXE)

.PHONY: build switch indirect clean

build: switch indirect

switch: $(SWITCH_VM)

indirect: $(INDIRECT_VM)

$(SWITCH_VM): src/luca_vm_switch.c include/opcodes.h
	$(CREATE_BUILD_DIR)
	$(CC) $(CPPFLAGS) $(CFLAGS) -o $@ $<

$(INDIRECT_VM): src/luca_vm_indirect.c include/opcodes.h
	$(CREATE_BUILD_DIR)
	$(CC) $(CPPFLAGS) $(INDIRECT_CFLAGS) -o $@ $<

clean:
	$(REMOVE_BUILD_DIR)
