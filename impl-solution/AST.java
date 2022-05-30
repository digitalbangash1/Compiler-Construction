import java.util.Map;

public abstract class AST {
  public void error(String msg) {
    System.err.println(msg);
    System.exit(-1);
  }
}

abstract class Expr extends AST {
  abstract public Double eval(Environment env);

  abstract public void typecheck(Environment env);
  // simple convention:
  // 0.0 for Double
  // 1.0 for Array
}

class Addition extends Expr {
  Expr e1, e2;

  Addition(Expr e1, Expr e2) {
    this.e1 = e1;
    this.e2 = e2;
  }

  public Double eval(Environment env) {
    return e1.eval(env) + e2.eval(env);
  }

  public void typecheck(Environment env) {
    e1.typecheck(env);
    e2.typecheck(env);
  }
}

class Multiplication extends Expr {
  Expr e1, e2;

  Multiplication(Expr e1, Expr e2) {
    this.e1 = e1;
    this.e2 = e2;
  }

  public Double eval(Environment env) {
    return e1.eval(env) * e2.eval(env);
  }

  public void typecheck(Environment env) {
    e1.typecheck(env);
    e2.typecheck(env);
  }
}

class Subtraction extends Expr {
  Expr e1, e2;

  Subtraction(Expr e1, Expr e2) {
    this.e1 = e1;
    this.e2 = e2;
  }

  public Double eval(Environment env) {
    return e1.eval(env) - e2.eval(env);
  }

  public void typecheck(Environment env) {
    e1.typecheck(env);
    e2.typecheck(env);
  }
}

class Division extends Expr {
  Expr e1, e2;

  Division(Expr e1, Expr e2) {
    this.e1 = e1;
    this.e2 = e2;
  }

  public Double eval(Environment env) {
    return e1.eval(env) / e2.eval(env);
  }

  public void typecheck(Environment env) {
    e1.typecheck(env);
    e2.typecheck(env);
  }
}

class Constant extends Expr {
  Double d;

  Constant(Double d) {
    this.d = d;
  }

  public Double eval(Environment env) {
    return d;
  }

  public void typecheck(Environment env) {
  }
}

class Variable extends Expr {
  String varname;

  Variable(String varname) {
    this.varname = varname;
  }

  public Double eval(Environment env) {
    return env.getVariable(varname);
  }

  public void typecheck(Environment env) {
    if (env.getVariable(varname) != 0)
      error("Array used as value.");
	/* The environment of the type checker contains
	for each variable either 
	  0 if it was used in normal assignment or 
	  1 if it was used in an Array assignment
	So if we get 1 here, somebody has used an array
	as an expression (and this is a type error).
	*/
  }
}

// ### New ###
class Array extends Expr {
  String varname;
  Expr e;

  Array(String varname, Expr e) {
    this.varname = varname;
    this.e = e;
  }

  public Double eval(Environment env) {
    Double index = e.eval(env);
    return env.getVariable(varname + "[" + index + "]");
  }

  public void typecheck(Environment env) {
    e.typecheck(env);
    if (env.getVariable(varname) != 1)
      error("Using " + varname + " as an array.");
  }
}

abstract class Command extends AST {
  abstract public void eval(Environment env);

  abstract public void typecheck(Environment env);

  public void debug(Environment env, String evaluatedCondition, boolean result) {
    if (env.getTraceMode()) {
      if (!evaluatedCondition.isEmpty()) {
        new DebuggerTrace(evaluatedCondition + "==" + result).eval(env);
      }
    }
  }

  public void debugAssignment(Environment env, String evaluatedCondition, double value) {
    if (env.getTraceMode()) {
      new DebuggerTrace("Assignment: " + evaluatedCondition + "==" + value).eval(env);
    }
  }

  public void debugArrayAssignment(Environment env, String evaluatedCondition, double index, double value) {
    if (env.getTraceMode()) {
      new DebuggerTrace("Assignment: " + evaluatedCondition + "==" + "[" + index + "]" + "=" + value).eval(env);
    }
  }
}

// Do nothing command 
class NOP extends Command {
  public void eval(Environment env) {
  }

  public void typecheck(Environment env) {
  }

}

class Sequence extends Command {
  Command c1, c2;

  Sequence(Command c1, Command c2) {
    this.c1 = c1;
    this.c2 = c2;
  }

  public void eval(Environment env) {
    c1.eval(env);
    c2.eval(env);
  }

  public void typecheck(Environment env) {
    c1.typecheck(env);
    c2.typecheck(env);
  }

}


class Assignment extends Command {
  String v;
  Expr e;
  String assignment;

  // x = x+1;
  Assignment(String v, Expr e, String assignment) {
    this.v = v;
    this.e = e;
    this.assignment = assignment;
  }

  public void eval(Environment env) {
    Double d = e.eval(env);
    env.setVariable(v, d);
    debugAssignment(env, assignment, d);
  }

  public void typecheck(Environment env) {
    e.typecheck(env);
    env.checkVariable(v, Double.valueOf(0));
    // variant of setVariable: if already defined, it must be the same now, otherwise it will stop with an error.
  }

}

// ### New ###
class ArrayAssignment extends Command {
  String v;
  Expr i;
  Expr e;
  String assignment;

  // x = x+1;
  ArrayAssignment(String v, Expr i, Expr e, String assignment) {
    this.v = v;
    this.i = i;
    this.e = e;
    this.assignment = assignment;
  }

  public void eval(Environment env) {
    Double index = i.eval(env);
    Double value = e.eval(env);
    env.setVariable(v + "[" + index + "]", value);
    debugArrayAssignment(env, assignment, index, value);
  }

  public void typecheck(Environment env) {
    i.typecheck(env);
    e.typecheck(env);
    env.checkVariable(v, Double.valueOf(1));
  }

}


class Output extends Command {
  Expr e;

  Output(Expr e) {
    this.e = e;
  }

  public void eval(Environment env) {
    Double d = e.eval(env);
    System.out.println(d);
  }

  public void typecheck(Environment env) {
    e.typecheck(env);
  }

}

class While extends Command {
  Condition c;
  Command body;
  String evaluatedCondition;

  While(Condition c, Command body, String evaluatedCondition) {
    this.c = c;
    this.body = body;
    this.evaluatedCondition = evaluatedCondition;
  }

  public void eval(Environment env) {
    while (c.eval(env)) {
      debug(env, evaluatedCondition, true);
      body.eval(env);
    }
    debug(env, evaluatedCondition, false);
  }

  public void typecheck(Environment env) {
    c.typecheck(env);
    body.typecheck(env);
  }

}

// ### New ###
class For extends Command {
  String v;
  Expr e1;
  Expr e2;
  Command body;
  String evaluatedCondition;

  For(String v, Expr e1, Expr e2, Command body, String evaluatedCondition) {
    this.v = v;
    this.e1 = e1;
    this.e2 = e2;
    this.body = body;
    this.evaluatedCondition = evaluatedCondition;
  }

  public void eval(Environment env) {
    Double d1 = e1.eval(env);
    Double d2 = e2.eval(env);

    for (Double i = d1; i <= d2; i++) {
      debug(env, evaluatedCondition, true);
      env.setVariable(v, i);
      body.eval(env);
    }
    debug(env, evaluatedCondition, false);
  }

  public void typecheck(Environment env) {
    e1.typecheck(env);
    e2.typecheck(env);
    env.checkVariable(v, Double.valueOf(0));
    body.typecheck(env);
  }

}

// ### New ###
class If extends Command {
  Condition c;
  Command body;
  String evaluatedCondition;

  If(Condition c, Command body, String evaluatedCondition) {
    this.c = c;
    this.body = body;
    this.evaluatedCondition = evaluatedCondition;
  }

  public void eval(Environment env) {
    if (c.eval(env)) {
      debug(env, evaluatedCondition, true);
      body.eval(env);
    } else {
      debug(env, evaluatedCondition, false);
    }
  }

  public void typecheck(Environment env) {
    c.typecheck(env);
    body.typecheck(env);
  }

}

// IMPLEMENTATION START
class DebuggerBreak extends Command {
  String debugMode;
  String argument;

  DebuggerBreak(String debugMode, String argument) {
    this.debugMode = debugMode;
    this.argument = argument;
  }

  @Override
  public void eval(Environment env) {
    logDebugMonitor(env);
    logDebugWithArgument();
    pressAnyKeyToContinue();
  }

  @Override
  public void typecheck(Environment env) { }

  private void logDebugWithArgument() {
    System.out.println("Breakpoint" + " " + argument);
  }

  private void logDebugMonitor(Environment env) {
    for (Map.Entry<String, Double> entry : env.getMonitors().entrySet()) {
      System.out.println("Monitor: " + entry.getKey() + "=" + entry.getValue());
    }
  }

  private void pressAnyKeyToContinue() {
    System.out.println("Press Enter key to continue...");
    try {
      System.in.read();
    } catch(Exception ignored) {}
  }
}

class
DebuggerAssert extends Command {
  String debugMode;
  Condition condition;

  DebuggerAssert(String debugMode, Condition condition) {
    this.debugMode = debugMode;
    this.condition = condition;
  }

  @Override
  public void eval(Environment env) {
    if (debugMode.contains("Assert")) {
      if (!condition.eval(env)) {
        logDebugMonitor(env);
        logDebugWithAssertionError(env.getVariableValues());
        System.exit(1);
      }
    }
  }

  @Override
  public void typecheck(Environment env) {
    condition.typecheck(env);
  }

  private void logDebugWithAssertionError(Map<String, Double> results) {
    System.out.println(debugMode.replaceAll(";", "")
        + " " + "(violated) " + "with following variables: " + results.toString());
  }

  private void logDebugMonitor(Environment env) {
    for (Map.Entry<String, Double> entry : env.getMonitors().entrySet()) {
      System.out.println("Monitor: " + entry.getKey() + "=" + entry.getValue());
    }
  }
}

class DebuggerMonitor extends Command {
  Expr expr;

  public DebuggerMonitor(Expr expr) {
    this.expr = expr;
  }

  @Override
  public void eval(Environment env) {
    if (expr instanceof Variable) {
      env.addMonitorIfAbsent(((Variable) expr).varname, expr.eval(env));
    }
  }

  @Override
  public void typecheck(Environment env) {
    expr.typecheck(env);
  }
}

class DebuggerTrace extends Command {
  String debugMode;

  DebuggerTrace(String debugMode) {
    this.debugMode = debugMode;
  }

  @Override
  public void eval(Environment env) {
    if (!env.getTraceMode()) {
      env.flipTraceMode();
    } else {
      logDebugWithTrace();
      pressAnyKeyToContinue();
    }
  }

  @Override
  public void typecheck(Environment env) { }

  private void logDebugWithTrace() {
    System.out.println(debugMode);
  }

  private void pressAnyKeyToContinue() {
    System.out.println("Press Enter key to continue...");
    try {
      System.in.read();
    } catch(Exception ignored) {}
  }
}
// IMPLEMENTATION END

abstract class Condition extends AST {
  abstract public Boolean eval(Environment env);

  abstract public void typecheck(Environment env);

  public void debug(Environment env) {
    if (env.getTraceMode()) {
      //new DebuggerTrace(env.toString()).eval(env);
    }
  }
}

class Unequal extends Condition {
  Expr e1, e2;

  Unequal(Expr e1, Expr e2) {
    this.e1 = e1;
    this.e2 = e2;
  }

  public Boolean eval(Environment env) {
    debug(env);
    return !e1.eval(env).equals(e2.eval(env));
  }

  public void typecheck(Environment env) {
    e1.typecheck(env);
    e2.typecheck(env);
  }

}

// ### New ###
class Equal extends Condition {
  Expr e1, e2;

  Equal(Expr e1, Expr e2) {
    this.e1 = e1;
    this.e2 = e2;
  }

  public Boolean eval(Environment env) {
    debug(env);
    return e1.eval(env).equals(e2.eval(env));
  }

  public void typecheck(Environment env) {
    e1.typecheck(env);
    e2.typecheck(env);
  }

}

// ### New ###
class Smaller extends Condition {
  Expr e1, e2;

  Smaller(Expr e1, Expr e2) {
    this.e1 = e1;
    this.e2 = e2;
  }

  public Boolean eval(Environment env) {
    debug(env);
    return e1.eval(env) < (e2.eval(env));
  }

  public void typecheck(Environment env) {
    e1.typecheck(env);
    e2.typecheck(env);
  }

}

// ### New ###
class Conjunction extends Condition {
  Condition e1, e2;

  Conjunction(Condition e1, Condition e2) {
    this.e1 = e1;
    this.e2 = e2;
  }

  public Boolean eval(Environment env) {
    debug(env);
    return e1.eval(env) && (e2.eval(env));
  }

  public void typecheck(Environment env) {
    e1.typecheck(env);
    e2.typecheck(env);
  }

}

// ### New ###
class Disjunction extends Condition {
  Condition e1, e2;

  Disjunction(Condition e1, Condition e2) {
    this.e1 = e1;
    this.e2 = e2;
  }

  public Boolean eval(Environment env) {
    debug(env);
    return e1.eval(env) || (e2.eval(env));
  }

  public void typecheck(Environment env) {
    e1.typecheck(env);
    e2.typecheck(env);
  }

}

// ### New ###
class Negation extends Condition {
  Condition e1;

  Negation(Condition e1) {
    this.e1 = e1;
  }

  public Boolean eval(Environment env) {
    debug(env);
    return !e1.eval(env);
  }

  public void typecheck(Environment env) {
    e1.typecheck(env);
  }

}
