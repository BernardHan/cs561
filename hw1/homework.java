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
      solution.printResults(results, true, input);
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
          // make sure [row, col] is not a tree
          if(noTree(input, row, col) && noEat(cols, row, col, input)){
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
          if(noTree(input, row, col) && noEat(cols, row, col, input)){
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

  private boolean noTree(Input input, int row, int col){
    for(Tree tree : input.trees){
      if(tree.row == row && tree.col == col) return false;
    }

    return true;
  }

  private boolean noEat(int[] cols, int row, int col, Input input){
    // now with trees, because of trees, multiple lizards can be in the same row, same col, same diagonal
    for(int checkRow = 0; checkRow < row; checkRow++){
      int checkCol = cols[checkRow];
      if(checkCol == -1) continue;
      if(checkCol == col){
        boolean treeBetween = false;
        // test if there's a tree between [checkRow, checkCol] and [row, col]
        for(Tree tree : input.trees){
          if(tree.row < row && tree.row > checkRow){
            // there's a tree between the rows, check col
            if(tree.col == checkCol){
              // there's a tree between even tho both lizards on the same col, no problem
              treeBetween = true;
              break;
            }
          }
        }
        // checked all trees, no trees in between, invalid placement
        if(!treeBetween) return false;
      }

      int colDiff = Math.abs(checkCol - col);
      int rowDiff = Math.abs(checkRow - row);
      if(colDiff == rowDiff){
        // now check if theres a tree between this diagonal
        // tree must in diagonal of [checkRow, checkCol] and also [row, col]
        // also need to be in between [checkRow, row] to avoid opposite diagonal
        boolean treeBetween = false;
        for(Tree tree : input.trees){
          // first search for tree that has row in between
          if(tree.row < row && tree.row > checkRow){
            int treeColDiff1 = Math.abs(tree.col - col);
            int treeColDiff2 = Math.abs(tree.col - checkCol);
            int treeRowDiff1 = row - tree.row;
            int treeRowDiff2 = tree.row - checkRow;
            if(treeRowDiff1 == treeColDiff1 && treeRowDiff2 == treeColDiff2){
              // found a tree in the diagonal
              treeBetween = true;
              break;
            }
          }
        }
        if(!treeBetween) return false;
      }
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
      input.lines.add(line);
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


  private boolean checkValid(int[] cols, Input input){
    int numLizard = 0;
    for(int row = 0; row < cols.length; row++){
      int col = cols[row];
      if(col != -1) numLizard++;
      if(!noTree(input, row, col)) return false;
      if(!checkValid(cols, row, col, input)) return false;
    }
    if(numLizard != input.numLizard) return false;
    return true;
  }

  private boolean checkValid(int[] cols, int row, int col, Input input){
    for(int checkRow = 0; checkRow < cols.length && col != -1; checkRow++){
      int checkCol = cols[checkRow];
      if(checkCol == -1 || (row == checkRow && col == checkCol)) continue;

      // when it's on same row, check if there's a tree between [row, col] and [row, checkCol]
      if(row == checkRow){
        // TODO
      }

      int largeRow = row > checkRow ? row : checkRow;
      int smallRow = row < checkRow ? row : checkRow; // row and checkRow should not be the same, already handled
      if(checkCol == col){
        boolean treeBetween = false;
        // test if there's a tree between [checkRow, checkCol] and [row, col]
        for(Tree tree : input.trees){
          if(tree.row < largeRow && tree.row > smallRow){
            // there's a tree between the rows, check col
            if(tree.col == checkCol){
              // there's a tree between even tho both lizards on the same col, no problem
              treeBetween = true;
              break;
            }
          }
        }
        // checked all trees, no trees in between, invalid placement
        if(!treeBetween) return false;
      }

      int colDiff = Math.abs(checkCol - col);
      int rowDiff = largeRow - smallRow;
      if(colDiff == rowDiff){
        // now check if theres a tree between this diagonal
        // tree must in diagonal of [checkRow, checkCol] and also [row, col]
        // also need to be in between [checkRow, row] to avoid opposite diagonal
        boolean treeBetween = false;
        for(Tree tree : input.trees){
          // first search for tree that has row in between
          if(tree.row < largeRow && tree.row > smallRow){
            int treeColDiff1 = Math.abs(tree.col - col);
            int treeColDiff2 = Math.abs(tree.col - checkCol);
            int treeRowDiff1 = Math.abs(row - tree.row);
            int treeRowDiff2 = Math.abs(tree.row - checkRow);
            if(treeRowDiff1 == treeColDiff1 && treeRowDiff2 == treeColDiff2){
              // found a tree in the diagonal
              treeBetween = true;
              break;
            }
          }
        }
        if(!treeBetween) return false;
      }
    }
    return true;
  }


  private void printResults(ArrayList<int[]> results, boolean printFalse, Input input){
    int count = 0;
    for(int[] result : results){
      if(printFalse && !checkValid(result, input)){
        count++;
        printResult(result, input);
        System.out.println();
        System.out.println();
      }
      else if(!printFalse){
        printResult(result, input);
        System.out.println();
        System.out.println();
      }
    }

    if(printFalse){
      System.out.println("Error placement: " + count);
    }
  }

  private void printResult(int[] result, Input input){
    for(int row = 0; row < result.length; row++){
      int col = result[row];
      String line = input.lines.get(row);
      if(col != -1){
        line = line.substring(0, col) + "1" + line.substring(col + 1);
        line = line.replace("", " ").trim();
        System.out.println(line);
      }
      else{
        line = line.replace("", " ").trim();
        System.out.println(line);
      }
    }
  }

  private void printInput(Input input){
    System.out.println();
    System.out.println("Method: " + input.method);
    System.out.println("Size: " + input.size);
    System.out.println("Number of lizard: " + input.numLizard);

    for(Tree tree : input.trees){
      System.out.println("[" + tree.row + ", " + tree.col + "]");
    }
    System.out.println();
    for(String line : input.lines){
      System.out.println(line);
    }

    System.out.println();
    System.out.println();
  }

  static class Input{
    public String method;
    public int size;
    public int numLizard;

    public ArrayList<Tree> trees;
    public ArrayList<String> lines;

    public Input(String method, int size, int numLizard){
      this.method = method;
      this.size = size;
      this.numLizard = numLizard;
      trees = new ArrayList<Tree>();
      lines = new ArrayList<String>();
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
        if(noEat(cols, row, col, input)){
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
}
