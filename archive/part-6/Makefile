.PHONY: all clean

all: luca_run_switch.exe luca_run_indirect.exe

luca_run_switch.exe: luca_run_switch.c
	gcc -o $@ $<

luca_run_indirect.exe: luca_run_indirect.c
	gcc -o $@ $<

clean:
	rm -f luca_run_switch.exe luca_run_indirect.exe
