# KtSpoon
Experimental Kotlin support for Spoon. 

KtSpoon is a prototype investigates to what extent [Spoon](https://github.com/INRIA/spoon)'s metamodel of Java can be used to model Kotlin code. The metamodel used by the prototype is almost the original Spoon metamodel for Java, with additional Kotlin-specific metadata in some of the nodes. The biggest deviation from the basic Java metamodel is the addition of a KtStatementExpression node, used to overcome the fact that statements and expressions are treated differently in Java and Kotlin. 

See [KtSpoon : Modelling Kotlin by extending Spoon’s Java Metamodel (Jesper Lundholm, 2021)](http://urn.kb.se/resolve?urn=urn:nbn:se:kth:diva-304429)


The IR module of the official Kotlin compiler is used to parse the code and gain access to an IR graph, which is then transformed into the metamodel.

## Current state and usage
The current state is _very_ rudimentary. It is possible to build a model from error-free Kotlin (1.3.72) code and print it back to source code. Running processors on the resulting model has not been tested. 

There is no CLI or facade class. To use KtSpoon one will have to download the code and create a runner. The class ExampleMain can be used as a template for a runner. It contains the necessary steps to build and print a model from code, using the IrTreeBuilder and DefaultPrettyPrinter classes. 

## Future work
In order to add true Kotlin-support to Spoon, the immediate first steps required would likely be:
  * Define and implement a metamodel. Either create a completely new one with the same philosophy of the original Spoon metamodel, or modify the existing one to allow it to model Kotlin code.
  * Implement a tree builder that parses source code and generates an instance of the metamodel. Preferably utilizing the FIR or IR module of the Kotlin compiler as is already demonstrated in this prototype. 
