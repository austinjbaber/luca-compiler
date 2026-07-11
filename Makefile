.PHONY: build test clean

ifeq ($(OS),Windows_NT)
TEST_SHELL ?= C:/Program Files/Git/bin/bash.exe
else
TEST_SHELL ?= bash
endif

build:
	$(MAKE) -C compiler build
	$(MAKE) -C runtime build

test: build
	"$(TEST_SHELL)" tests/run_regressions.sh
	"$(TEST_SHELL)" tests/run_vm_regressions.sh

clean:
	$(MAKE) -C compiler clean
	$(MAKE) -C runtime clean
