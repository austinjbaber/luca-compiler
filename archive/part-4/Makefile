all: .FORCE
	javac --release 11 ast/*.java
	javac --release 11 lexer/*.java
	javac --release 11 parser/Parse.java
	chmod +x luca_parse

clean:
	find . -name '*.class' -delete

rebuild: clean all

.FORCE:
