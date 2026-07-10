.PHONY: build test clean

build:
	$(MAKE) -C compiler build

test: build
	java -cp compiler/build/classes cli.Lucac lex part-2/tests/int.luc
	java -cp compiler/build/classes cli.Lucac parse part-5/tests/INT/OK1.luc
	java -cp compiler/build/classes cli.Lucac check part-5/tests/INT/OK1.luc

clean:
	$(MAKE) -C compiler clean
