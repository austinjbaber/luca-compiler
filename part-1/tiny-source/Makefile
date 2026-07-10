all: .FORCE
	javac *.java

run: .FORCE
	java Compiler 1.tny
	java Compiler 4.tny > 4.as
	java Eval 4.tny
	java GenIR 4.tny > 4.ir
	java Interpreter 4.ir

.FORCE:
