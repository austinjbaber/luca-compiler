all: .FORCE
	javac --release 11 lexer/*.java
	javac --release 11 parser/*.java
	javac --release 11 ast/*.java
	javac --release 11 sem/*.java
	javac --release 11 auxx/*.java
	javac --release 11 sym/*.java
	chmod +x luca_sem

clean:
	find . -name '*.class' -delete

rebuild: clean all

.FORCE:
