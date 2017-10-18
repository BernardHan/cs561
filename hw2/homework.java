import java.io.*;
import java.util.*;

public class homework{
  public static void main(String[] args){
    homework solution = new homework();
    Input input = null;
    try{
      input = solution.parseFile("input.txt");
    }
    catch(IOException e){
      System.out.println("Input.txt cannot be parsed.");
      return;
    }


    //solution.printInput(input);

    Node answer = solution.minimax(input);


    try{
      solution.writeOutput(input, answer);
    }
    catch(IOException e){
      System.out.println("output.txt cannot be generated.");
      return;
    }
  }

  public void writeOutput(Input input, Node node)
  throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));

    Node nodeWithBox = node;
    node.box = input.box;

    char column = (char)((int)'A' + node.action.col);
    String row = Integer.toString(node.action.row + 1);
    writer.write(column+row);
    nodeWithBox = makeMove(input, nodeWithBox, node.action.row, node.action.col, true, true);
    for(String cells : nodeWithBox.box) {
      writer.write("\n" + cells);
    }
    writer.close();
  }

  public Node minimax(Input input) {
    boolean isMax = true;
    int row = 0;
    int col = 0;
    Node startNode = new Node(isMax, input);
    int alpha = Integer.MIN_VALUE;
    int beta = Integer.MAX_VALUE;
    int depth = 0;
    MaxDepth maxDepth = new MaxDepth(0);
    long start = System.currentTimeMillis();
    double allocatedTime = (double)input.remainTime * 0.4;
    int limit = getDepthLimit(input);
    Node chosenNode = maxExpand(input, startNode, alpha, beta, start, allocatedTime, depth, maxDepth, limit);
    return chosenNode;
    //if(chosenNode == null) System.out.println("what");
    //boolean result = testGraph(input, startNode);
    /*
    System.out.println(start + " - " + allocatedTime);
    System.out.println("Time used: " + (System.currentTimeMillis() - start));
    System.out.println("Depth: " + maxDepth.depth);
    System.out.println("Result:");
    printResult(input, chosenNode);
*/
    //printGraph(startNode);
  }

  private int getDepthLimit(Input input) {
    int star = countStar(input);
    double ratio = (double)star / (input.size * input.size);
    System.out.println("Star ratio: " + star + " / " + (input.size * input.size) + " = " + ratio);
    if(input.size > 24) {
      if(ratio < 0.15 || input.remainTime < 20000) return 2;
      return 3;
    }
    else if(input.size > 15) {
      if(ratio < 0.15 || input.remainTime < 20000) return 3;
      return 4;
    }
    else if(input.size > 9) {
      if(input.remainTime < 3000) return 3;
      return 4;
    }
    return 5;
  }

  private int countStar(Input input) {
    int result = 0;
    for(String cells : input.box) {
      for(int i = 0; i < cells.length(); i++) {
        char cell = cells.charAt(i);
        if(cell == '*') {
          result++;
        }
      }
    }

    return result;
  }

  private Node maxExpand(Input input, Node root, int alpha, int beta,
  long start, double allocatedTime, int depth, MaxDepth maxDepth, int limit) {
    // previous row is max nodes, run over all cells, and return the max
    if(depth == limit) {
      root.box = null;
      return null;
    }
    maxDepth.depth = Math.max(maxDepth.depth, depth);
    int maxDiff = Integer.MIN_VALUE;
    Node maxNode = null;
    int successorVal = 0;
    for(int row = 0; row < input.size; row++) {
      for(int col = 0; col < input.size; col++) {

        long now = System.currentTimeMillis();
        if(now - start >= allocatedTime) {
          // time up
          root.box = null;
          root.score = maxDiff == Integer.MIN_VALUE ? root.score : maxDiff; // means this node won't be considered
          return maxNode;
        }
        if(!checkDuplicate(root, row, col)) continue;
        // take this fruit, calculate the score, apply gravity
        Node successor = makeMove(input, root, row, col, false, false);
        if(successor == null) continue;
        //printBox(successor.box);
        //root.successors.add(successor);
        // then move to the next state for each operation
        Node minNodeOfSuccessor = minExpand(input, successor, alpha, beta, start, allocatedTime, depth + 1, maxDepth, limit);
        maxNode = successor.score > maxDiff ? successor : maxNode;
        maxDiff = Math.max(maxDiff, successor.score);
        //alpha = minNodeOfSuccessor.score > alpha.score ? minNodeOfSuccessor : alpha;
        alpha = successor.score > alpha ? successor.score : alpha;

        if(alpha >= beta) {
          root.box = null;
          root.score = Integer.MAX_VALUE; // means this node won't be considered
          return null;
        }
      }
    }

    root.box = null;
    root.score = maxDiff == Integer.MIN_VALUE ? root.score : maxDiff;
    //root.decisionNode = maxNode;
    return maxNode;
  }

  private Node minExpand(Input input, Node root, int alpha, int beta,
  long start, double allocatedTime, int depth, MaxDepth maxDepth, int limit) {
    if(depth == limit) {
      root.box = null;
      return null;
    }
    maxDepth.depth = Math.max(maxDepth.depth, depth);
    int minDiff = Integer.MAX_VALUE;
    int successorVal = 0;
    Node minNode = null;

    for(int row = 0; row < input.size; row++) {
      for(int col = 0; col < input.size; col++) {

        long now = System.currentTimeMillis();
        if(now - start >= allocatedTime) {
          // time up
          root.box = null;
          root.score = minDiff == Integer.MAX_VALUE ? root.score : minDiff;
          return minNode;
        }
        // take this fruit, calculate the score, apply gravity
        // check if previous col already taken
        if(!checkDuplicate(root, row, col)) continue;
        Node successor = makeMove(input, root, row, col, true, false);
        if(successor == null) continue;
        // then move to the next state for each operation
        Node maxNodeOfSuccessor = maxExpand(input, successor, alpha, beta, start, allocatedTime, depth + 1, maxDepth, limit);
        minNode = successor.score < minDiff ? successor : minNode;
        minDiff = Math.min(minDiff, successor.score);
        //beta = maxNodeOfSuccessor.score < beta.score ? maxNodeOfSuccessor : beta;
        beta = successor.score < beta ? successor.score : beta;

        if(beta <= alpha) {
          root.box = null;
          root.score = Integer.MIN_VALUE;
          return null;
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
    node.action.row = row;
    node.action.col = col;

    int score = takeFruit(input, node, row, col, fruit, row, col, forResult);
    if(score == -1) {
      // the resulting state already existed
      return null;
    }
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

  public Input parseFile(String filename) throws IOException{
    BufferedReader reader = new BufferedReader(new FileReader(filename));
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

  private void printResult(Input input, Node node) {
    // convert row, col to answer
    Node nodeWithBox = node;
    node.box = input.box;
    System.out.println("Node score: " + node.score);
    System.out.println("Node my score: " + node.me);
    System.out.println("Node enemy score: " + node.enemy);

    char column = (char)((int)'A' + node.action.col);
    String row = Integer.toString(node.action.row + 1);
    System.out.println(column+row);
    nodeWithBox = makeMove(input, nodeWithBox, node.action.row, node.action.col, true, true);
    System.out.println("Score: " + nodeWithBox.score);
    System.out.println("My Score: " + nodeWithBox.me);
    System.out.println("Enemy Score: " + nodeWithBox.enemy);
    printBox(nodeWithBox.box);

    node.box = null;
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

  static class MaxDepth{
    public int depth;

    public MaxDepth(int depth){
      this.depth = depth;
    }
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
    public FruitsTaken action;
    public int me; // the score I get
    public int enemy; // the score enemy get
    public boolean myTurn;

    public Node(boolean isMax) {
      this.isMax = isMax;
      score = -1;
      box = new ArrayList<String>();
      action = new FruitsTaken(-1, -1);
      me = 0;
      enemy = 0;
      myTurn = false;
    }

    public Node(boolean isMax, Node root) {
      this.isMax = isMax;
      score = root.score;
      this.box = new ArrayList<String>(root.box);
      action = new FruitsTaken(-1, -1);
      me = root.me;
      enemy = root.enemy;
      myTurn = !root.myTurn;
    }

    public Node(boolean isMax, Input input) {
      this.isMax = isMax;
      score = -1;
      this.box = new ArrayList<String>(input.box);
      action = new FruitsTaken(-1, -1);
      me = 0;
      enemy = 0;
      myTurn = false;
    }

    public Node(int score) {
      this.isMax = isMax;
      this.score = score;
      box = new ArrayList<String>();
      action = new FruitsTaken(-1, -1);
      me = 0;
      enemy = 0;
      myTurn = false;
    }
  }

  static class Input{
    public int size;
    public int numFruitType;
    public int remainTime;

    public ArrayList<String> box;

    public Input(int size, int numFruitType, double remainTime){
      this.size = size;
      this.numFruitType = numFruitType;
      this.remainTime = (int)(remainTime * 1000);

      box = new ArrayList<String>();
    }
  }
}
