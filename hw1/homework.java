import java.util.*;
import java.io.*;

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

    ArrayList<int[]> results = solution.placeLizard(input);

    if(results == null){
      System.out.println("No Solution.");
    }
    else{
      solution.printResults(results, true);
      System.out.println(results.size());
    }

    solution.printInput(input);
  }

  public ArrayList<int[]> placeLizard(Input input){
    int[] cols = new int[input.size];
    Arrays.fill(cols, -1);
    ArrayList<int[]> results = new ArrayList<int[]>();
    boolean found = false;

    if(input.method.equals("DFS")){
      //found = placeLizard(0, cols, results, input, input.numLizard);
      found = placeLizardDFS(results, input);
    }
    else if(input.method.equals("BFS")){
      found = placeLizardBFS(results, input);
    }


    if(found){
      return results;
    }
    else{
      return null;
    }
  }

  private boolean placeLizardBFS(ArrayList<int[]> results, Input input){
    Queue<Node> queue = new LinkedList<Node>();

    int[] init_cols = new int[input.size];
    Arrays.fill(init_cols, -1);
    queue.add(new Node(-1, init_cols, input.numLizard));

    while(!queue.isEmpty()){
      Node node = queue.remove();
      int row = node.lastRow + 1;
      int[] cols = node.cols;

      if(row < input.size && node.lizardLeft > 0){
        int[] tmp = cols.clone();
        for(int col = 0; col < input.size; col++){
          if(noEat(cols, row, col)){
            cols[row] = col;
            queue.add(new Node(row, cols.clone(), node.lizardLeft - 1));
          }
        }
        // skip row without placing lizard
        queue.add(new Node(row, tmp, node.lizardLeft));
      }
      else{
        if(node.lizardLeft == 0){
          results.add(cols.clone());
          //return true;
        }
      }
    }
    // return false;
    return true;
  }

  private boolean placeLizardDFS(ArrayList<int[]> results, Input input){
    Stack<Node> stack = new Stack<Node>();

    int[] init_cols = new int[input.size];
    Arrays.fill(init_cols, -1);
    stack.push(new Node(-1, init_cols, input.numLizard));

    while(!stack.isEmpty()){
      Node node = stack.pop();
      int row = node.lastRow + 1;
      int[] cols = node.cols;

      if(row < input.size && node.lizardLeft > 0){
        int[] tmp = cols.clone();
        for(int col = 0; col < input.size; col++){
          if(noEat(cols, row, col)){
            cols[row] = col;
            stack.push(new Node(row, cols.clone(), node.lizardLeft - 1));
          }
        }
        // skip row without placing lizard
        stack.push(new Node(row, tmp, node.lizardLeft));
      }
      else{
        if(node.lizardLeft == 0){
          results.add(cols.clone());
          //return true;
        }
      }
    }
    // return false;
    return true;
  }

  private int[] copyArr(int[] cols, int newVal){
    int[] result = new int[cols.length + 1];
    int i = 0;
    for(i = 0; i < cols.length; i++){
      result[i] = cols[i];
    }
    result[i] = newVal;

    return result;
  }

  private boolean placeLizard(int row, int[] cols, ArrayList<int[]> results, Input input, int lizardLeft){
    if(lizardLeft == 0){
      results.add(cols.clone());
      return true;
    }
    else if(row == input.size){
      return false;
    }
    else{
      int[] tmp = cols.clone();
      for(int col = 0; col < input.size; col++){
        if(noEat(cols, row, col)){
          cols[row] = col;
          boolean found = placeLizard(row + 1, cols, results, input, lizardLeft - 1);
          if(found){
            //return true;
          }
        }
      }
      boolean found = placeLizard(row + 1, tmp, results, input, lizardLeft);
      //if(found){
        //return true;
      //}
    }

    //return false;
    return true;
  }

  private boolean noEat(int[] cols, int row, int col){
    for(int checkRow = 0; checkRow < row; checkRow++){
      int checkCol = cols[checkRow];
      if(checkCol == -1) continue;
      if(checkCol == col) return false;

      int colDiff = Math.abs(checkCol - col);
      int rowDiff = Math.abs(checkRow - row);
      if(colDiff == rowDiff) return false;
    }
    return true;
  }

  public Input parseFile() throws IOException{
    BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
    String method = null;
    String size = null;
    String numLizard = null;

    if((method = reader.readLine()) == null){
      return null;
    }

    if((size = reader.readLine()) == null){
      return null;
    }

    if((numLizard = reader.readLine()) == null){
      return null;
    }

    Input input = new Input(method, Integer.parseInt(size), Integer.parseInt(numLizard));

    String line = null;
    int row = 0;
    while((line = reader.readLine()) != null){
      for(int col = 0; col < line.length(); col++){
        if(line.charAt(col) == '2'){
          input.trees.add(new Tree(row, col));
        }
      }
      row++;
    }

    reader.close();

    return input;
  }

  private void printResults(ArrayList<int[]> results, boolean printFalse){
    int count = 0;
    for(int[] result : results){
      if(printFalse && !checkValid(result)){
        count++;
        for(int col : result){
          if(col != -1){
            printZeros(col);
            System.out.print(" 1 ");
            printZeros(result.length - col - 1);
          }
          else{
            printZeros(result.length);
          }
          System.out.println();
        }
        System.out.println();
        System.out.println();
      }
      else if(!printFalse){
        for(int col : result){
          if(col != -1){
            printZeros(col);
            System.out.print(" 1 ");
            printZeros(result.length - col - 1);
          }
          else{
            printZeros(result.length);
          }
          System.out.println();
        }
        System.out.println();
        System.out.println();
      }
    }

    if(printFalse){
      System.out.println("Error placement: " + count);
    }
  }

  private boolean checkValid(int[] cols){
    for(int row = 0; row < cols.length; row++){
      int col = cols[row];
      if(!checkValid(cols, row, col)) return false;
    }
    return true;
  }

  private boolean checkValid(int[] cols, int row, int col){
    for(int checkRow = 0; checkRow < cols.length && col != -1; checkRow++){
      if(checkRow != row){
        int checkCol = cols[checkRow];
        if(checkCol == -1) continue;
        if(checkCol == col) return false;

        int colDiff = Math.abs(checkCol - col);
        int rowDiff = Math.abs(checkRow - row);
        if(colDiff == rowDiff) return false;
      }
    }
    return true;
  }

  private void printZeros(int num){
    for(int i = 0 ; i < num; i++){
      System.out.print(" 0 ");
    }
  }

  private void printInput(Input input){
    System.out.println("Method: " + input.method);
    System.out.println("Size: " + input.size);
    System.out.println("Number of lizard: " + input.numLizard);

    for(Tree tree : input.trees){
      System.out.println("[" + tree.row + ", " + tree.col + "]");
    }

    System.out.println();
    System.out.println();
  }

  static class Input{
    public String method;
    public int size;
    public int numLizard;

    public ArrayList<Tree> trees;

    public Input(String method, int size, int numLizard){
      this.method = method;
      this.size = size;
      this.numLizard = numLizard;
      trees = new ArrayList<Tree>();
    }
  }

  static class Tree{
    public int row;
    public int col;

    public Tree(int row, int col){
      this.row = row;
      this.col = col;
    }
  }

  static class Node{
    public int lastRow;
    public int[] cols;
    public int lizardLeft;

    public Node(int row, int[] cols, int lizard){
      lastRow = row;
      this.cols = cols;
      lizardLeft = lizard;
    }
  }
}
