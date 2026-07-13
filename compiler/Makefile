JAVAC ?= javac
JAVA ?= java
SOURCE_DIR := src/main/java
BUILD_DIR := build/classes
SOURCES := $(wildcard $(SOURCE_DIR)/lexer/*.java) \
           $(wildcard $(SOURCE_DIR)/parser/*.java) \
           $(wildcard $(SOURCE_DIR)/ast/*.java) \
           $(wildcard $(SOURCE_DIR)/sem/*.java) \
           $(wildcard $(SOURCE_DIR)/sym/*.java) \
           $(wildcard $(SOURCE_DIR)/auxx/*.java) \
           $(wildcard $(SOURCE_DIR)/codegen/*.java) \
           $(wildcard $(SOURCE_DIR)/cli/*.java)

ifeq ($(OS),Windows_NT)
# Git Bash reports Windows_NT but sets MSYSTEM and executes POSIX commands.
# Native Windows Make may report SHELL=sh.exe while still using CMD recipes,
# so MSYSTEM is the reliable distinction here.
ifneq ($(strip $(MSYSTEM)),)
CREATE_BUILD_DIR = mkdir -p $(BUILD_DIR)
REMOVE_BUILD_DIR = rm -rf build
else
CREATE_BUILD_DIR = if not exist "$(subst /,\,$(BUILD_DIR))" mkdir "$(subst /,\,$(BUILD_DIR))"
REMOVE_BUILD_DIR = if exist "build" rmdir /s /q "build"
endif
else
CREATE_BUILD_DIR = mkdir -p $(BUILD_DIR)
REMOVE_BUILD_DIR = rm -rf build
endif

.PHONY: build clean

build:
	$(CREATE_BUILD_DIR)
	$(JAVAC) --release 11 -d $(BUILD_DIR) $(SOURCES)

clean:
	$(REMOVE_BUILD_DIR)
