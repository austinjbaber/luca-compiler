LUCA = java -cp ../code/lexer.jar:../code/ast.jar:../code/sym:jar:../code/parser.jar parser.Parse 

SRCS = \
   array1.luc \
   array2.luc \
   assign1.luc \
   assign2.luc \
   call1.luc \
   call2.luc \
   call3.luc \
   call4.luc \
   call5.luc \
   call6.luc \
   call7.luc \
   const1.luc \
   des1.luc \
   des2.luc \
   des3.luc \
   des4.luc \
   des5.luc \
   empty.luc \
   exit.luc \
   expr1.luc \
   expr2.luc \
   expr3.luc \
   expr4.luc \
   expr5.luc \
   expr6.luc \
   expr7.luc \
   expr8.luc \
   expr9.luc \
   exprA.luc \
   exprB.luc \
   formal1.luc \
   formal2.luc \
   formal3.luc \
   formal4.luc \
   if1.luc \
   if2.luc \
   if3.luc \
   if4.luc \
   if5.luc \
   loop1.luc \
   proc1.luc \
   proc2.luc \
   prog1.luc \
   prog2.luc \
   read1.luc \
   read2.luc \
   record1.luc \
   record2.luc \
   record3.luc \
   repeat1.luc \
   var1.luc \
   while_repeat_if.luc \
   while1.luc \
   while2.luc \
   write1.luc \
   writeln.luc \
   writeln1.luc \
   writeln2.luc \

OBJS	= ${SRCS:.luc=.xml}

.SUFFIXES:

.SUFFIXES: .luc .xml

.luc.xml: 
	-${LUCA} $< > $@

all: ${OBJS}

clean:
	/bin/rm *.xml *~

.FORCE:
