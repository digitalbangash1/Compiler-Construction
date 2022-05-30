# 02332 Compiler Construction Exam Assignment

This is the Exam Assignment for Compiler Construction delivered due date 30-05-2022. The purpose of the assignment is to build a DEBUG functionality into the interpreter using ANTLR and JAVA. A more detailed explanation is attached in **Compiler task.pdf**.


# Language Debugger Extension

The language has been extended to recognize and parse debugging commands. Four commands have been added to the language:


- **Breakpoint: DEBUG: [Breakpoint] -> variable;**

- **Assert: DEBUG: [Assert] -> condition;**

- **Monitor: DEBUG: [Monitor] -> expression;**

- **Trace: DEBUG: [Trace];**

All four commands are expressed with similar syntax for language readability reasons across all debugging modes, the structure is based on the following expression:

**ACTION: <[ACTION_MODE]> -> <ACTION_CONSUMER>_**

    grammar impl;  
      
    /* A small imperative language */  
      
    start :  cs+=command* EOF ;  
      
    program : c=command # SingleCommand  
      | '{' cs+=command* '}' # MultipleCommands  
      ;  
      
    command : x=ID '=' e=expr ';' # Assignment  
      | a=ID '[' i=expr ']' '=' e=expr ';' # ArrayAssignment  
      | 'output' e=expr ';' # Output  
      | 'while' '('c=condition')' p=program # WhileLoop  
      | 'for' '(' x=ID '=' e1=expr '..' e2=expr ')' p=program # ForLoop  
      | 'if' '(' c=condition ')' p=program # If  
      | DEBUG LBRACKET DBREAK RBRACKET SARROW ID ';' # DebuggerBreak  
      | DEBUG LBRACKET DASSERT RBRACKET SARROW condition ';' # DebuggerAssert  
      | DEBUG LBRACKET DMONITOR RBRACKET SARROW expr ';' # DebuggerMonitor  
      | DEBUG LBRACKET DTRACE RBRACKET ';' # DebuggerTrace  
      ;  
      
    expr : e1=expr o=('*' | '/') e2=expr # Multiplication  
      | e1=expr o=('+' | '-') e2=expr # Addition  
      | c=FLOAT # Constant  
      | '-' c=FLOAT # NegativeConstant  
      | x=ID # Variable  
      | a=ID '[' e=expr ']' # Array  
      | '(' e=expr ')' # Parenthesis  
      ;  
      
    condition : e1=expr '!=' e2=expr # Unequal  
      | e1=expr '==' e2=expr # Equal  
      | e1=expr '<' e2=expr # Smaller  
      | c1=condition '||' c2=condition # Disjunction  
      | c1=condition '&&' c2=condition # Conjunction  
      | '!' c=condition # Negation  
      | '(' c=condition ')' # ParenthesisCondition  
      ;    
      
    LBRACKET : '[';  
    RBRACKET : ']';  
    SARROW : ' -> ';  
      
    DEBUG : 'DEBUG:';  
    DBREAK : 'Breakpoint';  
    DASSERT : 'Assert';  
    DMONITOR : 'Monitor';  
    DTRACE : 'Trace';  
      
    ID : ALPHA (ALPHA|NUM)* ;  
    FLOAT : NUM+ ('.' NUM+)? ;  
      
    ALPHA : [a-zA-Z_ÆØÅæøå] ;  
    NUM : [0-9] ;  
      
    WHITESPACE : [ \n\t\r]+ -> skip;  
    COMMENT : '//'~[\n]*  -> skip;  
    COMMENT2 : '/*' (~[*] | '*'~[/]  )*   '*/' -> skip;


## Breakpoint **(Test_case_1 in impl_additional.txt)**

A new instance of DebuggerBreak is created in the overridden implVisitor::DebuggerBreak method, once evaluated by calling DebuggerBreak::eval it will then print out the breakpoint variable and wait for user input before continuing execution of the program.

    **Output**
    
    _5.0 Breakpoint b (Press Enter key to continue...)_
    
    _20.0 Breakpoint b (Press Enter key to continue...)_
    
    _60.0 Breakpoint b (Press Enter key to continue...)_
    
    _120.0 Breakpoint b (Press Enter key to continue...)_
    
    _120.0 Breakpoint b_



## **Assert (Test_Case_2 in impl_additional.txt)**

Implemented by instantiating DebuggerAssert in the overridden implVisitor::DebuggerAssert method, once visited (reached line in the execution program) it will evaluate the given condition and system exist if the condition evaluates to false or continue if condition is true, it will also print the values used in the condition by calling Environment::getVariableValues during sys.out.

 
**Output**
 

    5.0 20.0 60.0 120.0 120.0
    
    DEBUG:[Assert] -> prev_result (violated) with following variables:
    
    {result=120.0, prev_result=60.0, n=1.0}

## **Three Monitors and a Breakpoint (Test_Case_5 & Test_Case_3 in impl_additional.txt)**

The debugger is able to monitor 0..* expressions, the implementation is based on a HashMap, the variables are added to the map during the debug monitor call, each monitor is updated in the Environment.class every time Environment::setVariable is called on a variable if the variable already exists as a key.

HashMap does not allow duplicate keys by default, which means no duplicate monitors in loops, since variables are only computed if absent from the map. Monitor gets called whenever DebuggerBreak and DebuggerAssert are evaluated.

**Output**

    Monitor: result=1.3983816E7
    
    Monitor: k=1.0
    
    Monitor: n=43.0
    
    Breakpoint bp

**_OR_**

    Monitor: result=120.0
    
    DEBUG:[Assert] -> prev_result<result (violated) with following variables: {result=120.0, prev_result=120.0, n=1.0}

## **Trace (Test_Case_6 in impl_additional.txt)**

Trace will look at the new variable “traceMode” in the Envrionment.class, if this variable is flipped to true it will trace each command (Loops, If, Assignment, ArrayAssignment) once eval is called. Clients can call the overridden eval method on DebuggerBreak.class, this is done via the debug methods on the abstract Command.class which each command inherits from. This way each command can call the inherited method with the option to override default implementation.

**Output**

    while(n!=k&&k!=0)==true (Press Enter key to continue...)
    
    Assignment: result=result*n/k==2.0 (Press Enter key to continue...)
    
    Assignment: n=n-1==3.0 (Press Enter key to continue...)
    
    Assignment: k=k-1==1.0 (Press Enter key to continue...)
    
    while(n!=k&&k!=0)==true (Press Enter key to continue...)
    
    Assignment: result=result*n/k==6.0 (Press Enter key to continue...)
    
    Assignment: n=n-1==2.0 (Press Enter key to continue...)
    
    Assignment: k=k-1==0.0 (Press Enter key to continue...)
    
    while(n!=k&&k!=0)==false (Press Enter key to continue...)

## Configuration
Open the project in your favourite IDE and in your IDE's project setting, add the **antlr.jar** file in modules.Download the latest antlr jar file from the [Antlr official website ](https://www.antlr.org/). Select is as a classpath variable in your computer enviroment settings. Download the **cygwin64** terminal and **cd** to the path of the project and run **make**. 


