CC ?= cc
CFLAGS ?= -std=c11 -Wall -Wextra -Wpedantic -O2
BUILD_DIR := build

ifeq ($(OS),Windows_NT)
CC := gcc
EXE := .exe
ifneq ($(strip $(MSYSTEM)),)
CREATE_BUILD_DIR = mkdir -p $(BUILD_DIR)
REMOVE_BUILD_DIR = rm -rf $(BUILD_DIR)
else
CREATE_BUILD_DIR = if not exist "$(BUILD_DIR)" mkdir "$(BUILD_DIR)"
REMOVE_BUILD_DIR = if exist "$(BUILD_DIR)" rmdir /s /q "$(BUILD_DIR)"
endif
else
EXE :=
CREATE_BUILD_DIR = mkdir -p $(BUILD_DIR)
REMOVE_BUILD_DIR = rm -rf $(BUILD_DIR)
endif

TRANSLATOR := $(BUILD_DIR)/luca_mips$(EXE)

.PHONY: build clean

build: $(TRANSLATOR)

$(TRANSLATOR): luca_mips.c
	$(CREATE_BUILD_DIR)
	$(CC) $(CFLAGS) -o $@ $<

clean:
	$(REMOVE_BUILD_DIR)
