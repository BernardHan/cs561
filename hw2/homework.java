import java.io.*;
import java.util.*;

public class homework{
  public static void main(String[] args){
    homework solution = new homework();
    Input input = null;
    try{
      input = solution.parseFile();
    }
    catch(IOException e){
      System.out.println("Input.txt cannot be parsed.");
      return;
    }


    solution.printInput(input);

    solution.minimax(input);
    /*
    try{
      solution.writeOutput(results, input);
    }
    catch(IOException e){
      System.out.println("output.txt cannot be generated.");
      return;
    }
    */
  }

  public void minimax(Input input) {
    boolean isMax = true;
    int row = 0;
    int col = 0;
    Node startNode = new Node(isMax, input);
    Node alpha = new Node(Integer.MIN_VALUE);
    Node beta = new Node(Integer.MAX_VALUE);
    int limit = 4;
    int depthCount = 0;
    Node chosenNode = maxExpand(input, startNode, alpha, beta, depthCount, limit);
    if(chosenNode == null) System.out.println("what");
    //boolean result = testGraph(input, startNode);
    System.out.println("Result:");
    printResult(input, chosenNode);

    //printGraph(startNode);
  }

  private Node maxExpand(Input input, Node root, Node alpha, Node beta, int depthCount, int limit) {
    // previous row is max nodes, run over all cells, and return the max
    if(limit != -1 && depthCount == limit) return null;
    int maxDiff = Integer.MIN_VALUE;
    Node maxNode = null;
    int successorVal = 0;
    for(int row = 0; row < input.size; row++) {
      for(int col = 0; col < input.size; col++) {
        if(!checkDuplicate(root, row, col)) continue;
        // take this fruit, calculate the score, apply gravity
        Node successor = makeMove(input, root, row, col, false, false);
        if(successor == null) continue;
        //printBox(successor.box);
        root.successors.add(successor);
        // then move to the next state for each operation
        Node minNodeOfSuccessor = minExpand(input, successor, alpha, beta, depthCount + 1, limit);
        maxNode = successor.score > maxDiff ? successor : maxNode;
        maxDiff = Math.max(maxDiff, successor.score);
        if(minNodeOfSuccessor == null) continue;
        //alpha = minNodeOfSuccessor.score > alpha.score ? minNodeOfSuccessor : alpha;
        alpha = minNodeOfSuccessor.score > alpha.score ? successor : alpha;

        if(alpha.score >= beta.score) {
          /*
          System.out.println("================================");
          System.out.println("Max Pruning is happening");
          System.out.println("Alpha:");
          //printResult(input, alpha);
          System.out.println("Beta:");
          //printResult(input, beta);
          System.out.println("================================");
          */
          root.box = null;
          return beta;
        }
      }
    }

    root.box = null;
    root.score = maxDiff == Integer.MIN_VALUE ? root.score : maxDiff;
    //root.decisionNode = maxNode;
    return maxNode;
  }

  private Node minExpand(Input input, Node root, Node alpha, Node beta, int depthCount, int limit) {
    if(limit != -1 && depthCount == limit) return null;
    int minDiff = Integer.MAX_VALUE;
    int successorVal = 0;
    Node minNode = null;

    for(int row = 0; row < input.size; row++) {
      for(int col = 0; col < input.size; col++) {
        // take this fruit, calculate the score, apply gravity
        // check if previous col already taken
        if(!checkDuplicate(root, row, col)) continue;
        Node successor = makeMove(input, root, row, col, true, false);
        if(successor == null) continue;
        //printBox(successor.box);
        root.successors.add(successor);
        // then move to the next state for each operation
        Node maxNodeOfSuccessor = maxExpand(input, successor, alpha, beta, depthCount + 1, limit);
        minNode = successor.score > minDiff ? successor : minNode;
        minDiff = Math.min(minDiff, successor.score);
        if(maxNodeOfSuccessor == null) continue;
        //beta = maxNodeOfSuccessor.score < beta.score ? maxNodeOfSuccessor : beta;
        beta = maxNodeOfSuccessor.score < beta.score ? successor : beta;

        if(beta.score <= alpha.score) {
          /*
          System.out.println("================================");
          System.out.println("Min Pruning is happening");
          System.out.println("Alpha:");
          //printResult(input, alpha);
          System.out.println("Beta:");
          //printResult(input, beta);
          System.out.println("================================");
          */
          root.box = null;
          return alpha;
        }
      }
    }
    root.box = null;
    root.score = minDiff == Integer.MAX_VALUE ? root.score : minDiff;
    //root.decisionNode = minNode;
    return minNode;
  }

  private boolean checkDuplicate(Node root, int row, int col) {
    // simple check if this fruit link to previous fruit in the same row in any way
    char checkFruit = root.box.get(row).charAt(col);
    if(checkFruit == '*') return false;
    // when the previous col is the same fruit
    if(col - 1 >= 0 && checkFruit == root.box.get(row).charAt(col - 1)) return false;
    // when the previous row is the same fruit
    if(row - 1 >= 0 && checkFruit == root.box.get(row - 1).charAt(col)) return false;


    return true;
  }

  private Node makeMove(Input input, Node root, int row, int col, boolean isMax, boolean forResult) {
    Node node = new Node(isMax, root);
    char fruit = node.box.get(row).charAt(col);
    node.actions.add(new FruitsTaken(row, col));

    int score = takeFruit(input, node, row, col, fruit, row, col, forResult);
    if(score == -1) {
      // the resulting state already existed
      return null;
    }
    //System.out.println("================================");
    //printBox(node.box);
    if(node.myTurn) {
      node.me += (int)Math.pow(score, 2);
    }
    else {
      node.enemy += (int)Math.pow(score, 2);
    }
    node.score = node.me - node.enemy;
    applyGravity(input, node);

    return node;
  }

  private void applyGravity(Input input, Node node) {
    for(int col = 0; col < input.size; col++) {
      for(int row = input.size - 1; row > 0; row--) {
        String cells = node.box.get(row);
        // find the cell with *
        if(cells.charAt(col) == '*') {
          // look upward to find the first non *, and swap
          findNSwap(node, row, col);
          break;
        }
      }
    }
  }

  private void findNSwap(Node node, int row, int col) {
    if(row == 0) return;

    for(int upRow = row - 1; upRow >= 0; upRow--) {
      char fruit = node.box.get(upRow).charAt(col);
      if(fruit != '*') {
        // found the first fruit on stack, now swap
        String bottom = node.box.get(row);
        bottom = bottom.substring(0, col) + fruit + bottom.substring(col + 1);
        node.box.set(row, bottom);
        row--;
        String curr = node.box.get(upRow);
        curr = curr.substring(0, col) + '*' + curr.substring(col + 1);
        node.box.set(upRow, curr);
      }
    }
  }

  private int takeFruit(Input input, Node node, int row, int col, char fruit,
  int fixRow, int fixCol, boolean forResult) {
    if(fruit == '*') return 0;
    String cells = node.box.get(row);
    char currFruit = cells.charAt(col);
    if(currFruit != fruit) return 0;
    // if this cell is at the same row, but previous col, then should skip it
    if(!forResult && row == fixRow && col < fixCol) return -1;
    // if it connects to a cell in upper row, skip it, such state already explored
    if(!forResult && row < fixRow) return -1;

    cells = cells.substring(0, col) + "*" + cells.substring(col + 1);
    node.box.set(row, cells);
    int result = 1;
    int subResult = 0;
    if(row + 1 < input.size) {
      subResult = takeFruit(input, node, row + 1, col, fruit, fixRow, fixCol, forResult);
      if(subResult == -1) {
        // detected a connection to previous cols or rows
        return -1;
      }
      result += subResult;
    }
    if(col + 1 < input.size) {
      subResult = takeFruit(input, node, row, col + 1, fruit, fixRow, fixCol, forResult);
      if(subResult == -1) {
        // detected a connection to previous cols or rows
        return -1;
      }
      result += subResult;
    }
    if(row - 1 >= 0) {
      subResult = takeFruit(input, node, row - 1, col, fruit, fixRow, fixCol, forResult);
      if(subResult == -1) {
        // detected a connection to previous cols or rows
        return -1;
      }
      result += subResult;
    }
    if(col - 1 >= 0) {
      subResult = takeFruit(input, node, row, col - 1, fruit, fixRow, fixCol, forResult);
      if(subResult == -1) {
        // detected a connection to previous cols or rows
        return -1;
      }
      result += subResult;
    }

    return result;
  }

  public Input parseFile() throws IOException{
    BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
    String size = null;
    String numFruitType = null;
    String remainTime = null;

    if((size = reader.readLine()) == null){
      return null;
    }

    if((numFruitType = reader.readLine()) == null){
      return null;
    }

    if((remainTime = reader.readLine()) == null){
      return null;
    }

    Input input = new Input(Integer.parseInt(size), Integer.parseInt(numFruitType), Double.parseDouble(remainTime));

    String line = null;
    while((line = reader.readLine()) != null){
      input.box.add(line);
    }

    reader.close();

    return input;
  }
  /*
  private boolean testGraph(Input input, Node root) {
    if(root == null) return true;
    if(root.successors.size() == 0) return true;
    if(root.score == -1) {
      System.out.println("Node score has -1");
      return false;
    }
    if(!testBox(input, root)) {
      System.out.println("Root Gravity not applied");
      return false;
    }

    int score = root.isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

    for(Node node : root.successors) {
      if(node.rowTaken == -1 || node.colTaken == -1) {
        System.out.println("Node has no taken data");
        return false;
      }
      if(!testBox(input, node)) {
        System.out.println("Loop gravity not applied");
        return false;
      }
      if(!testGraph(input, node)) {
        return false;
      }
      if(root.isMax) {
        score = node.score > score ? node.score : score;
      }
      else {
        score = node.score < score ? node.score : score;
      }
    }

    if(score != root.score) {
      System.out.println("Score does not match");
      return false;
    }

    return true;
  }

  private boolean testBox(Input input, Node node) {
    for(int col = 0; col < input.size; col++) {
      for(int row = input.size - 1; row > 0; row--) {
        String cells = node.box.get(row);
        // find the cell with *
        if(cells.charAt(col) == '*') {
          // look if its above has any fruit, if yes, return false;
          for(int upRow = row - 1; upRow >= 0; upRow--) {
            char fruit = node.box.get(upRow).charAt(col);
            if(fruit != '*') {
              return false;
            }
          }
          break;
        }
      }
    }

    return true;
  }*/

  private void printResult(Input input, Node node) {
    // convert row, col to answer
    Node nodeWithBox = node;
    node.box = input.box;
    System.out.println("Node score: " + node.score);
    System.out.println("Node my score: " + node.me);
    System.out.println("Node enemy score: " + node.enemy);
    //node.me = 0;
    //node.enemy = 0;
    //node.score = 0;
    for(FruitsTaken action : node.actions) {
      char column = (char)((int)'A' + action.col);
      String row = Integer.toString(action.row + 1);
      System.out.println(column+row);
      nodeWithBox = makeMove(input, nodeWithBox, action.row, action.col, true, true);
      System.out.println("Score: " + nodeWithBox.score);
      System.out.println("My Score: " + nodeWithBox.me);
      System.out.println("Enemy Score: " + nodeWithBox.enemy);
      printBox(nodeWithBox.box);
    }
    node.box = null;
  }
  /*
  private void printGraph(Node root) {
    List<List<Node>> trees = new ArrayList<>();
    List<Node> nodes = new ArrayList<Node>();
    DFS(root, trees, nodes);
    // now got the trees
    int forOne = Integer.MAX_VALUE;
    int forZero = Integer.MAX_VALUE;
    boolean one = false;
    boolean zero = false;
    for(List<Node> branch : trees) {
      one = false;
      zero = false;
      Node test = branch.get(0);
      if(test.rowTaken == 0 && test.colTaken == 8) {
        Node test2 = branch.get(1);
        if(!(test2.rowTaken == 3 && test2.colTaken == 7)) continue;
        one = true;
      }
      else if(!(test.rowTaken == 4 && test.colTaken == 7)) {
        continue;
      }

      if(!one) {
        zero = true;
      }

      System.out.println("========================================");
      for(Node node : branch) {
        if(node.myTurn) {
          System.out.println("My Turn");
        }
        else {
          System.out.println("Enemy Turn");
        }
        if(node.isMax) {
          System.out.println("Node Type: MaxNode");
        }
        else {
          System.out.println("Node Type: MinNode");
        }
        char column = (char)((int)'A' + node.colTaken);
        String row = Integer.toString(node.rowTaken + 1);
        System.out.println("Col: " + column + ", Row: " + row);
        System.out.println("My Score: " + node.me);
        System.out.println("Enemy Score: " + node.enemy);
        //printBox(node.box);
        System.out.println("------------------");
        if(one) {
          forOne = Math.min(forOne, node.score);
        }
        else {
          forZero = Math.min(forZero, node.score);
        }
      }
    }

    System.out.println("Max Diff by choosing 1: " + forOne);
    System.out.println("Max Diff by choosing 0: " + forZero);
  }*/

  private void DFS(Node root, List<List<Node>> trees, List<Node> nodes) {
    if(root.successors.size() == 0) {
      trees.add(new ArrayList<Node>(nodes));
      return;
    }
    for(Node node : root.successors) {
      nodes.add(node);
      DFS(node, trees, nodes);
      nodes.remove(node);
    }
  }

  private void printBox(ArrayList<String> box) {
    System.out.println();
    for(String cells : box) {
      System.out.print("[ ");
      for(int i = 0; i < cells.length(); i++) {
        System.out.print(cells.charAt(i) + " ");
      }
      System.out.print("]");
      System.out.println();
    }
    System.out.println();
  }

  private void printInput(Input input){
    System.out.println();
    System.out.println("Box Size: " + input.size);
    System.out.println("Number of Fruit: " + input.numFruitType);
    System.out.println("Time Remaining: " + input.remainTime);

    System.out.println();
    for(String line : input.box){
      System.out.println(line);
    }

    System.out.println();
    System.out.println();
  }

  static class FruitsTaken{
    public int row;
    public int col;

    public FruitsTaken(int row, int col) {
      this.row = row;
      this.col = col;
    }
  }

  static class Node{
    public boolean isMax;
    public int score;
    public ArrayList<String> box;
    public ArrayList<Node> successors;
    public ArrayList<FruitsTaken> actions;
    public int me; // the score I get
    public int enemy; // the score enemy get
    public boolean myTurn;

    public Node(boolean isMax) {
      this.isMax = isMax;
      score = -1;
      box = new ArrayList<String>();
      successors = new ArrayList<Node>();
      actions = new ArrayList<FruitsTaken>();
      me = 0;
      enemy = 0;
      myTurn = false;
    }

    public Node(boolean isMax, Node root) {
      this.isMax = isMax;
      score = root.score;
      this.box = new ArrayList<String>(root.box);
      successors = new ArrayList<Node>();
      actions = new ArrayList<FruitsTaken>(root.actions);
      me = root.me;
      enemy = root.enemy;
      myTurn = !root.myTurn;
    }

    public Node(boolean isMax, Input input) {
      this.isMax = isMax;
      score = -1;
      this.box = new ArrayList<String>(input.box);
      successors = new ArrayList<Node>();
      actions = new ArrayList<FruitsTaken>();
      me = 0;
      enemy = 0;
      myTurn = false;
    }

    public Node(int score) {
      this.isMax = isMax;
      this.score = score;
      box = new ArrayList<String>();
      successors = new ArrayList<Node>();
      actions = new ArrayList<FruitsTaken>();
      me = 0;
      enemy = 0;
      myTurn = false;
    }
  }

  static class Input{
    public int size;
    public int numFruitType;
    public double remainTime;

    public ArrayList<String> box;

    public Input(int size, int numFruitType, double remainTime){
      this.size = size;
      this.numFruitType = numFruitType;
      this.remainTime = remainTime;

      box = new ArrayList<String>();
    }
  }
}
