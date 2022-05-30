antlr4 = java org.antlr.v4.Tool
grun   = java org.antlr.v4.gui.TestRig

SRCFILES = main.java Environment.java AST.java
GENERATED = implParser.java implBaseVisitor.java implVisitor.java implLexer.java

all:
	make main.class

main.class:	$(SRCFILES) $(GENERATED) impl.g4
	javac  $(SRCFILES) $(GENERATED)

implParser.java:	impl.g4
	$(antlr4) -visitor impl.g4

test:	main.class

	java main impl_input.txt
	java main impl_additional.txt
	

tree:	$(GENERATED) impl.g4
	javac $(GENERATED)
	$(grun) impl start -tree -gui < impl_input.txt
