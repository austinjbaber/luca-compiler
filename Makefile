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
	"$(TEST_SHELL)" tests/run_frontend_regressions.sh
	"$(TEST_SHELL)" tests/run_vm_regressions.sh
	"$(TEST_SHELL)" tests/run_codegen_regressions.sh
	"$(TEST_SHELL)" tests/run_e2e_regressions.sh

clean:
	$(MAKE) -C compiler clean
	$(MAKE) -C runtime clean
