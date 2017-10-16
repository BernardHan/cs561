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
    Node chosenNode = maxExpand(input, startNode);
    //startNode.decisionNode = chosenNode;
    if(chosenNode == null) System.out.println("what");
    boolean result = testGraph(input, startNode);
    printGraph(startNode);

    System.out.println("====================================");
    System.out.println("====================================");
    System.out.println("====================================");
    printDecision(startNode);
    System.out.println(result);

    if(result){
      printResult(chosenNode);
    }
  }

  private Node maxExpand(Input input, Node root) {
    // previous row is max nodes, run over all cells, and return the max
    int maxDiff = Integer.MIN_VALUE;
    Node maxNode = null;
    int successorVal = 0;
    for(int row = 0; row < input.size; row++) {
      for(int col = 0; col < input.size; col++) {
        if(!checkDuplicate(root, row, col)) continue;
        // take this fruit, calculate the score, apply gravity
        Node successor = makeMove(input, root, row, col, false);
        //printBox(successor.box);
        root.successors.add(successor);
        // then move to the next state for each operation
        Node minNodeOfSuccessor = minExpand(input, successor);
        //successor.decisionNode = minNodeOfSuccessor;
        maxNode = successor.score > maxDiff ? successor : maxNode;
        maxDiff = Math.max(maxDiff, successor.score);
      }
    }

    root.score = maxDiff == Integer.MIN_VALUE ? root.score : maxDiff;
    root.decisionNode = maxNode;
    return maxNode;
  }

  private Node minExpand(Input input, Node root) {
    int minDiff = Integer.MAX_VALUE;
    int successorVal = 0;
    Node minNode = null;

    for(int row = 0; row < input.size; row++) {
      for(int col = 0; col < input.size; col++) {
        // take this fruit, calculate the score, apply gravity
        // check if previous col already taken
        if(!checkDuplicate(root, row, col)) continue;
        Node successor = makeMove(input, root, row, col, true);
        //printBox(successor.box);
        root.successors.add(successor);
        // then move to the next state for each operation
        Node maxNodeOfSuccessor = maxExpand(input, successor);
        //successor.decisionNode = maxNodeOfSuccessor;
        minNode = successor.score > minDiff ? successor : minNode;
        minDiff = Math.min(minDiff, successor.score);
      }
    }

    root.score = minDiff == Integer.MAX_VALUE ? root.score : minDiff;
    root.decisionNode = minNode;
    return minNode;
  }

  private boolean checkDuplicate(Node root, int row, int col) {
    // check if this fruit link to previous fruit in the same row in any way
    char checkFruit = root.box.get(row).charAt(col);
    if(checkFruit == '*') return false;
    // when the previous col is the same fruit
    if(col - 1 >= 0 && checkFruit == root.box.get(row).charAt(col - 1)) return false;

    if(row == 0 && col == 0) return true;

    // check if the above row is all * when col is 0
    boolean allStar = true;
    if(col == 0 && row > 0) {
      for(int upCol = 0; upCol < root.box.size(); upCol++) {
        if(root.box.get(row - 1).charAt(upCol) != '*') {
          allStar = false;
          break;
        }
      }

      if(allStar) return true;
    }


    return checkDuplicateHelper(root, row, col, row, col, -1, -1);
  }

  private boolean checkDuplicateHelper(Node root, int checkRow, int checkCol, int fixRow, int fixCol,
  int lastRow, int lastCol) {
    System.out.println("CheckRow: " + checkRow + ", CheckCol: " + checkCol);
    if(checkRow == fixRow && checkCol < fixCol) return false;
    if(checkRow < fixRow) return false;
    // detect loop
    if(lastRow != -1 && checkRow == fixRow && checkCol == fixCol) return true;

    char checkFruit = root.box.get(fixRow).charAt(fixCol);
    if(checkRow + 1 < root.box.size() && !(checkRow + 1 == lastRow && checkCol == lastCol) && checkFruit == root.box.get(checkRow + 1).charAt(checkCol)) {
      if(!checkDuplicateHelper(root, checkRow + 1, checkCol, fixRow, fixCol, checkRow, checkCol)) return false;
    }
    if(checkRow - 1 >= 0 && !(checkRow - 1 == lastRow && checkCol == lastCol)  && checkFruit == root.box.get(checkRow - 1).charAt(checkCol)) {
      if(!checkDuplicateHelper(root, checkRow - 1, checkCol, fixRow, fixCol, checkRow, checkCol)) return false;
    }
    if(checkCol + 1 < root.box.size() && !(checkRow == lastRow && checkCol + 1 == lastCol)  && checkFruit == root.box.get(checkRow).charAt(checkCol + 1)) {
      if(!checkDuplicateHelper(root, checkRow, checkCol + 1, fixRow, fixCol, checkRow, checkCol)) return false;
    }
    if(checkCol - 1 >= 0 && !(checkRow == lastRow && checkCol - 1 == lastCol)  && checkFruit == root.box.get(checkRow).charAt(checkCol - 1)) {
      if(!checkDuplicateHelper(root, checkRow, checkCol - 1, fixRow, fixCol, checkRow, checkCol)) return false;
    }

    return true;
  }

  private Node makeMove(Input input, Node root, int row, int col, boolean isMax) {
    Node node = new Node(isMax, root);
    char fruit = node.box.get(row).charAt(col);
    node.rowTaken = row;
    node.colTaken = col;
    int score = takeFruit(input, node, row, col, fruit);
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

  private int takeFruit(Input input, Node node, int row, int col, char fruit) {
    if(fruit == '*') return 0;
    String cells = node.box.get(row);
    char currFruit = cells.charAt(col);
    if(currFruit != fruit) return 0;
    cells = cells.substring(0, col) + "*" + cells.substring(col + 1);
    node.box.set(row, cells);
    int result = 1;
    if(row + 1 < input.size) {
      result += takeFruit(input, node, row + 1, col, fruit);
    }
    if(col + 1 < input.size) {
      result += takeFruit(input, node, row, col + 1, fruit);
    }
    if(row - 1 >= 0) {
      result += takeFruit(input, node, row - 1, col, fruit);
    }
    if(col - 1 >= 0) {
      result += takeFruit(input, node, row, col - 1, fruit);
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

  private boolean testGraph(Input input, Node root) {
    if(root == null) return true;
    if(root.successors.size() == 0) return true;
    if(root.score == -1) return false;
    if(!testBox(input, root)) return false;

    int score = root.isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

    for(Node node : root.successors) {
      if(node.rowTaken == -1 || node.colTaken == -1) return false;
      if(!testBox(input, node)) return false;
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

    if(score != root.score) return false;

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
  }

  private void printResult(Node node) {
    // convert row, col to answer
    char column = (char)((int)'A' + node.colTaken);
    String row = Integer.toString(node.rowTaken + 1);
    System.out.println(column+row);
    printBox(node.box);
  }

  private void printDecision(Node node) {
    if(node == null) return;
    Node decision = node.decisionNode;
    if(decision == null) return;

    if(decision.myTurn) {
      System.out.println("My Turn");
    }
    else {
      System.out.println("Enemy Turn");
    }
    if(decision.isMax) {
      System.out.println("Node Type: MaxNode");
    }
    else {
      System.out.println("Node Type: MinNode");
    }
    System.out.println("Row: " + decision.rowTaken + ", Col: " + decision.colTaken);
    System.out.println("My Score: " + decision.me);
    System.out.println("Enemy Score: " + decision.enemy);
    printBox(decision.box);
    System.out.println("------------------");
    printDecision(decision);
  }

  private void printGraph(Node root) {
    List<List<Node>> trees = new ArrayList<>();
    List<Node> nodes = new ArrayList<Node>();
    DFS(root, trees, nodes);
    // now got the trees
    for(List<Node> branch : trees) {
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
        System.out.println("Row: " + node.rowTaken + ", Col: " + node.colTaken);
        System.out.println("My Score: " + node.me);
        System.out.println("Enemy Score: " + node.enemy);
        printBox(node.box);
        System.out.println("------------------");
      }
    }
  }

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

  static class Node{
    public boolean isMax;
    public int score;
    public ArrayList<String> box;
    public ArrayList<Node> successors;
    public int rowTaken; // the row, col this state being taken
    public int colTaken;
    public int me; // the score I get
    public int enemy; // the score enemy get
    public boolean myTurn;
    public Node decisionNode;

    public Node(boolean isMax) {
      this.isMax = isMax;
      score = -1;
      box = new ArrayList<String>();
      successors = new ArrayList<Node>();
      rowTaken = -1;
      colTaken = -1;
      me = 0;
      enemy = 0;
      myTurn = false;
      decisionNode = null;
    }

    public Node(boolean isMax, Node root) {
      this.isMax = isMax;
      score = root.score;
      this.box = new ArrayList<String>(root.box);
      successors = new ArrayList<Node>();
      rowTaken = -1;
      colTaken = -1;
      me = root.me;
      enemy = root.enemy;
      myTurn = !root.myTurn;
      decisionNode = null;
    }

    public Node(boolean isMax, Input input) {
      this.isMax = isMax;
      score = -1;
      this.box = new ArrayList<String>(input.box);
      successors = new ArrayList<Node>();
      rowTaken = -1;
      colTaken = -1;
      me = 0;
      enemy = 0;
      myTurn = false;
      decisionNode = null;
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
