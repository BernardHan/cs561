import java.util.*;
import java.io.*;
import java.util.regex.*;

public class homework{
  public static void main(String[] args){
    homework solution = new homework();
    KB kb = new KB();
    Queries queries = new Queries();
    try{
      solution.parseFile(kb, queries);
    }
    catch(IOException e){
      System.out.println("Input.txt cannot be parsed.");
      return;
    }

    ArrayList<String> results = new ArrayList<String>(queries.num);
    solution.solveQueries(kb, queries, results);
    System.out.println(results);
    //solution.printInput(kb, queries);
    /*
    try{
      solution.writeOutput(results, input);
    }
    catch(IOException e){
      System.out.println("output.txt cannot be generated.");
      return;
    }*/
  }

  private void solveQueries(KB kb, Queries queries, ArrayList<String> results) {
    for(int i = 0; i < queries.num; i++) {
      results.add(solveQuery(kb, queries.queries.get(i)));
    }
    //results.add(solveQuery(kb, queries.queries.get(0)));
  }
  /*
  private String solveQuery(KB kb, String query) {
    boolean result = false;
    ArrayList<Clause> clauses = buildClauses(kb, query);


    //clauses.get(3).predicates.get(0).vars.get(0).value = "Bernard";
    //printClauses(clauses);

    // unwritten methods: resolve
    System.out.println();
    System.out.println();
    System.out.println("Query: " + query);
    ArrayList<Clause> allClauses = new ArrayList<Clause>(clauses);
    while(clauses.size() > 0) {
      ArrayList<Clause> nextSteps = new ArrayList<Clause>();
      System.out.println("=================================================================");
      System.out.println("Clauses: ");
      printClauses(clauses);
      for(int i = clauses.size() - 1; i >= 0; i--) {
        for(int j = i - 1; j >= 0; j--) {
          ArrayList<Clause> resolvents = resolve(clauses.get(i), clauses.get(j));
          // need to way to track unused clauses, those will be kept to the next round
          if(resolvents == null) return "TRUE";
          nextSteps.addAll(resolvents);
        }
      }
      //if(containsAll(allClauses, nextSteps)) return "FALSE";
      if(nextSteps.size() == 0) return "FALSE";
      if(containsAll(clauses, nextSteps)) return "FALSE";
      //updateClauses(clauses, nextSteps); // remove all clauses except unused, put new clauses
      //allClauses.addAll(nextSteps);
      clauses.addAll(nextSteps);
    }

    return "FALSE";
  }*/

  private String solveQuery(KB kb, String query) {
    boolean result = false;
    ArrayList<Clause> clauses = buildClauses(kb, query);
    //Clause contradiction = toClause(query);
    //contradiction.predicates.get(0).isNot = !contradiction.predicates.get(0).isNot;


    //clauses.get(3).predicates.get(0).vars.get(0).value = "Bernard";
    //printClauses(clauses);

    // unwritten methods: resolve
    System.out.println();
    System.out.println();
    System.out.println("Query: " + query);
    ArrayList<Clause> allClauses = new ArrayList<Clause>(clauses);
    while(clauses.size() > 0) {
      ArrayList<Clause> nextSteps = new ArrayList<Clause>();
      System.out.println("=================================================================");
      System.out.println("Clauses: ");
      printClauses(clauses);
      for(int i = clauses.size() - 1; i >= 0; i--) {
        for(int j = i - 1; j >= 0; j--) {
          ArrayList<Clause> resolvents = resolve(clauses.get(i), clauses.get(j));
          // need to way to track unused clauses, those will be kept to the next round
          if(resolvents == null) return "TRUE";
          nextSteps.addAll(resolvents);
        }
      }
      //if(containsAll(allClauses, nextSteps)) return "FALSE";
      if(nextSteps.size() == 0) return "FALSE";
      if(containsAll(clauses, nextSteps)) return "FALSE";
      //updateClauses(clauses, nextSteps); // remove all clauses except unused, put new clauses
      //allClauses.addAll(nextSteps);
      clauses.addAll(nextSteps);
    }

    return "FALSE";
  }

  private boolean containsAll(ArrayList<Clause> clauses, ArrayList<Clause> nextSteps) {
    HashMap<String, Integer> superSet = new HashMap<String, Integer>();
    for(Clause clause : clauses) {
      superSet.put(clause.toString(), 0);
    }
    for(Clause clause : nextSteps) {
      String key = clause.toString();
      if(!superSet.containsKey(key)) return false;
    }

    return true;
  }


  private ArrayList<Clause> resolve(Clause c1, Clause c2) {
    ArrayList<Clause> result = new ArrayList<Clause>();
    // generate all posible resolution results here
    // need to do Unify somewhere
    printClause(c1);
    System.out.println(" + ");
    printClause(c2);
    for(Predicate p1 : c1.predicates) {
      for(Predicate p2 : c2.predicates) {
        if(p1.name.equals(p2.name) && (p1.isNot ^ p2.isNot)) {
          // opposite predicate found, check if they have the same constants
          if(checkCollide(p1, p2)) return null;
          //apply unify and resolution
          Clause clause = unifyClause(p1, p2, c1, c2); // predicate, var, clause should be new
          if(clause != null) {
            result.add(clause);
          }
        }
      }
    }

    System.out.println("==");
    System.out.println("-----------------------------------------------");
    if(result.size() != 0) {
      printClauses(result);
    }
    else {
      System.out.println("NULL");
    }

    System.out.println("-----------------------------------------------");

    return result;
  }

  private boolean checkCollide(Predicate p1, Predicate p2) {
    if(p1.vars.size() != p2.vars.size()) return false;
    for(int i = 0; i < p1.vars.size(); i++) {
      String val1 = p1.vars.get(i).value;
      String val2 = p2.vars.get(i).value;
      if(val1.equals("") || val2.equals("")) return false;
      if(!val1.equals(val2)) return false;
    }

    return true;
  }

  private Clause unifyClause(Predicate p1, Predicate p2, Clause c1, Clause c2) {
    // first see if p1 can unify with p2
    if(allEmpty(c1, c2)) return null;
    ArrayList<Variable> uniVars = unifyPredicate(p1, p2);
    if(uniVars == null) return null;
    Clause result = new Clause(c1.predicates.size() + c2.predicates.size(), c1.vars.size() + c2.vars.size());
    // replicate the two clauses, then append the vars, remove p1, p2, add the rest to result
    Predicate new_p1 = null;
    // TODO: missing the situation, multiple empty vars from two predicates may connect together
    /*
    -----------------------------------------------
    ~Parent: { x |  }, { y |  },
    ~Ancestor: { y |  }, { z |  },
    Ancestor: { x |  }, { z |  },
     +
    ~Parent: { x |  }, { y |  },
    Ancestor: { x |  }, { y |  },
    ==
    -----------------------------------------------
    -----------------------------
    ~Parent: { x |  }, { y |  },
    Ancestor: { x |  }, { z |  },
    ~Parent: { x |  }, { y |  },
    -----------------------------
    */
    ArrayList<Variable> part1 = new ArrayList<Variable>();
    for(Predicate p : c1.predicates) {
      Predicate new_p = new Predicate(p.name, p.isNot);
      for(Variable v : p.vars) {
        if(v.value.equals("")) {
          Variable new_v = getVar(part1, v.name);
          if(new_v == null) {
            new_v = new Variable(v.name);
            part1.add(new_v);
          }
          new_p.vars.add(new_v);
        }
        else {
          Variable new_v = new Variable(v.value, true);
          new_p.vars.add(new_v);
        }
      }
      if(p == p1) {
        new_p1 = new_p;
      }
      else {
        result.predicates.add(new_p);
      }
    }

    Predicate new_p2 = null;
    ArrayList<Variable> part2 = new ArrayList<Variable>();
    for(Predicate p : c2.predicates) {
      Predicate new_p = new Predicate(p.name, p.isNot);
      for(Variable v : p.vars) {
        if(v.value.equals("")) {
          Variable new_v = getVar(part2, v.name);
          if(new_v == null) {
            new_v = new Variable(v.name);
            part2.add(new_v);
          }
          new_p.vars.add(new_v);
        }
        else {
          Variable new_v = new Variable(v.value, true);
          new_p.vars.add(new_v);
        }
      }
      if(p == p2) {
        new_p2 = new_p;
      }
      else {
        result.predicates.add(new_p);
      }
    }

    for(int i = 0; i < uniVars.size(); i++) {
      Variable v1 = new_p1.vars.get(i);
      Variable v2 = new_p2.vars.get(i);
      for(Predicate p : result.predicates) {
        for(int i_v = 0; i_v < p.vars.size(); i_v++) {
          Variable v = p.vars.get(i_v);
          if(v == v2) {
            p.vars.set(i_v, v1);
          }
        }
      }
      v1.value = uniVars.get(i).value;
      //v2.value = uniVars.get(i).value;
    }

    // no vars in clause
    return result;
  }

  private boolean allEmpty(Clause c1, Clause c2) {
    for(Predicate p : c1.predicates) {
      for(Variable v : p.vars) {
        if(!v.value.equals("")) return false;
      }
    }
    for(Predicate p : c2.predicates) {
      for(Variable v : p.vars) {
        if(!v.value.equals("")) return false;
      }
    }
    return true;
  }

  private Variable getVar(ArrayList<Variable> vars, String name) {
    for(Variable var : vars) {
      if(var.name.equals(name)) return var;
    }

    return null;
  }

  private ArrayList<Variable> unifyPredicate(Predicate p1, Predicate p2) {
    if(p1.vars.size() != p2.vars.size()) return null;
    ArrayList<Variable> result = new ArrayList<Variable>();
    for(int i = 0; i < p1.vars.size(); i++) {
      if(!unifyVar(p1.vars.get(i), p2.vars.get(i), result)) return null;
    }

    return result;
  }

  private boolean unifyVar(Variable v1, Variable v2, ArrayList<Variable> result) {
    // when both are not empty, and values are different
    if(!v1.value.equals("") && !v2.value.equals("") && !v1.value.equals(v2.value)) return false;
    if(v1.value.equals("")) {
      Variable v_dup = new Variable(v1.name);
      v_dup.value = v2.value;
      result.add(v_dup);
    }
    else {
      Variable v_dup = new Variable(v2.name);
      v_dup.value = v1.value;
      result.add(v_dup);
    }

    return true;
  }

  private void updateClauses(ArrayList<Clause> clauses, ArrayList<Clause> nextSteps) {
    ArrayList<Clause> toDelete = new ArrayList<Clause>();
    for(Clause clause : clauses) {
      if(clause.used) {
        toDelete.add(clause);
      }
    }
    clauses.removeAll(toDelete);
    clauses.addAll(nextSteps);
  }

  private ArrayList<Clause> buildClauses(KB kb, String query) {

    Clause contradiction = toClause(query);
    contradiction.predicates.get(0).isNot = !contradiction.predicates.get(0).isNot;

    ArrayList<Clause> clauses = new ArrayList<Clause>();

    for(String fact : kb.facts) {
      clauses.add(toClause(fact));
    }
    clauses.add(contradiction);

    return clauses;
  }

  private Clause toClause(String clause) {
    String clauseFormat = "(?<predicate>~?\\w+)[(](?<args>[\\w,]+)[)]";
    Pattern pattern = Pattern.compile(clauseFormat);
    Matcher matcher = pattern.matcher(clause);

    Clause result = new Clause();

    while(matcher.find()) {
      String predicate_str = matcher.group("predicate");
      Predicate predicate = null;
      if(predicate_str.charAt(0) == '~') {
        predicate = new Predicate(predicate_str.substring(1), true);
      }
      else {
        predicate = new Predicate(predicate_str);
      }
      String argFormat = "\\w+";
      Pattern argPattern = Pattern.compile(argFormat);
      Matcher argMatcher = argPattern.matcher(matcher.group("args"));
      while(argMatcher.find()) {
        String arg = argMatcher.group();
        if(arg.matches("^[A-Z]{1}\\w*")) {
          // this arg is a constant
          Variable var = new Variable(arg, true);
          predicate.vars.add(var);
        }
        else {
          Variable var = null;
          if((var = result.getVar(arg)) == null) {
            var = new Variable(arg);
            result.vars.add(var);
          }
          predicate.vars.add(var);
        }
      }
      result.predicates.add(predicate);
    }

    return result;
  }

  public void parseFile(KB kb, Queries queries) throws IOException{
    BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
    String query = null;
    String KB = null;
    String numQ = null;
    String numKB = null;
    if((numQ = reader.readLine()) == null){
      return;
    }
    queries.num = Integer.parseInt(numQ);
    for(int i = 0; i < queries.num; i++) {
      if((query = reader.readLine()) == null){
        return;
      }
      queries.queries.add(query);
    }

    if((numKB = reader.readLine()) == null){
      return;
    }
    kb.num = Integer.parseInt(numKB);
    for(int i = 0; i < kb.num; i++) {
      if((KB = reader.readLine()) == null){
        return;
      }
      kb.facts.add(KB);
    }

    reader.close();
  }

  private void printInput(KB kb, Queries queries) {
    System.out.println("Query Num: " + queries.num);
    System.out.println(queries.queries);
    System.out.println("KB Num: " + kb.num);
    System.out.println(kb.facts);
  }

  private void printClauses(ArrayList<Clause> clauses) {
    for(Clause clause : clauses) {
      System.out.println("-----------------------------");
      printClause(clause);
      System.out.println("-----------------------------");
    }
  }

  private void printClause(Clause clause) {
    for(Predicate predicate : clause.predicates) {
      if(predicate.isNot) System.out.print("~");
      System.out.print(predicate.name + ": ");
      for(Variable var : predicate.vars) {
        System.out.print("{ " + var.name + " | " + var.value + " }, ");
      }
      System.out.println();
    }
  }

  static class Clause {
    public ArrayList<Predicate> predicates;
    public ArrayList<Variable> vars;
    public boolean used;


    public Clause() {
      predicates = new ArrayList<Predicate>();
      vars = new ArrayList<Variable>();
      used = false;
    }

    public Clause(int pSize, int vSize) {
      predicates = new ArrayList<Predicate>(pSize);
      vars = new ArrayList<Variable>(vSize);
      used = false;
    }

    public Variable getVar(String name) {
      for(Variable variable : vars) {
        if(variable.name.equals(name)) return variable;
      }

      return null;
    }

    public String toString(){
      StringBuilder result = new StringBuilder();
      for(Predicate predicate : predicates) {
        if(predicate.isNot) result.append("~");
        result.append(predicate.name);
        result.append("(");
        for(Variable var : predicate.vars) {
          if(!var.value.equals("")) {
            result.append(var.value + ",");
          }
          else {
            result.append(",");
          }
        }
        result.append(")|");
      }

      return result.toString();
    }
  }

  static class Predicate {
    public String name;
    public boolean isNot;
    public ArrayList<Variable> vars;

    public Predicate(String name, boolean isNot) {
      this.name = name;
      this.isNot = isNot;
      vars = new ArrayList<Variable>();
    }

    public Predicate(String name) {
      this.name = name;
      isNot = false;
      vars = new ArrayList<Variable>();
    }

    public Predicate(Predicate p) {
      name = p.name;
      isNot = p.isNot;
      vars = new ArrayList<Variable>(p.vars.size());
      for(Variable v : p.vars) {
        vars.add(new Variable(v));
      }
    }

    public boolean equals(Predicate p) {
      if(!p.name.equals(name)) return false;
      if(isNot != p.isNot) return false;
      if(vars.size() != p.vars.size()) return false;
      for(int i = 0; i < vars.size(); i++) {
        if(!vars.get(i).value.equals(p.vars.get(i).value)) return false;
      }

      return true;
    }
  }

  static class Variable {
    public String name;
    public String value;
    public boolean isConstant;

    public Variable(String name) {
      this.name = name;
      value = "";
      isConstant = false;
    }

    public Variable(String value, boolean isConstant) {
      name = "";
      this.value = value;
      this.isConstant = isConstant;
    }

    public Variable(Variable v) {
      name = v.name;
      value = v.value;
      isConstant = v.isConstant;
    }
  }

  static class KB {
    public int num;
    public ArrayList<String> facts;

    public KB() {
      facts = new ArrayList<String>();
      num = 0;
    }
  }

  static class Queries{
    public int num;
    public ArrayList<String> queries;

    public Queries() {
      queries = new ArrayList<String>();
      num = 0;
    }
  }
}
