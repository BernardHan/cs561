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

    ArrayList<ArrayList<ArrayList<Integer>>> results = solution.placeLizard(input);

    if(results == null){
      System.out.println("No Solution.");
    }
    else{
      solution.printResults(results, false, input);
      System.out.println("Total number of placements: " + results.size());
    }

    solution.printInput(input);

    try{
      solution.writeOutput(results, input);
    }
    catch(IOException e){
      System.out.println("output.txt cannot be generated.");
      return;
    }
  }

  public ArrayList<ArrayList<ArrayList<Integer>>> placeLizard(Input input){
    ArrayList<ArrayList<ArrayList<Integer>>> results = new ArrayList<ArrayList<ArrayList<Integer>>>();
    boolean found = false;
    long start = System.currentTimeMillis();
    if(input.method.equals("DFS")){
      found = placeLizardDFS(results, input);
    }
    else if(input.method.equals("BFS")){
      found = placeLizardBFS(results, input);
    }
    else if(input.method.equals("SA")){

      long end = start + 290 * 1000;
      while(!found && System.currentTimeMillis() < end){
        found = placeLizardSA(results, input);
      }
    }
    System.out.println("Time used: " + (System.currentTimeMillis() - start));
    if(found){
      return results;
    }
    else{
      return null;
    }
  }

  private boolean placeLizardSA(ArrayList<ArrayList<ArrayList<Integer>>> results, Input input){
    ArrayList<ArrayList<Integer>> cols = new ArrayList<ArrayList<Integer>>(input.size);
    initCols(cols, input.size);
    Random rand = new Random();
    initRandomPosition(cols, input, rand);

    int current_energy = calculateE(cols, input);
    double temp = 10 * input.numLizard;
    double cooling = 0.9;
    double stable = input.numLizard / 5;
    double stableFactor = 1.08;

    long start = System.currentTimeMillis();
    long end = start + 289 * 1000;
    while(temp > 0 && current_energy != 0 && System.currentTimeMillis() < end){
      for(int i = 0; i < stable; i++){
        ArrayList<ArrayList<Integer>> new_cols = randomNeighbor(cols, input, rand);
        int new_energy = calculateE(new_cols, input);
        int deltaE = new_energy - current_energy;
        if(acceptProbability(temp, deltaE)){
          // accept the new cols
          cols = new_cols;
          current_energy = new_energy;
        }
      }
      temp *= cooling;
      stable *= stableFactor;
    }

    if(current_energy != 0) return false;

    results.add(cols);
    return true;
  }

  private int calculateE(ArrayList<ArrayList<Integer>> cols, Input input){
    int numConflicts = 0;
    for(int row = 0; row < input.size; row++){
      ArrayList<Integer> rowList = cols.get(row);
      if(rowList.size() == 0) continue;

      for(int col : rowList){
        numConflicts += numEat(cols, row, col, input);
      }
    }

    return numConflicts;
  }

  private int numEat(ArrayList<ArrayList<Integer>> cols, int row, int col, Input input){
    int result = 0;
    // now with trees, because of trees, multiple lizards can be in the same row, same col, same diagonal
    for(int checkRow = row; checkRow < input.size; checkRow++){
      ArrayList<Integer> rowLink = cols.get(checkRow);
      if(rowLink.size() == 0) continue;
      int largeRow = row > checkRow ? row : checkRow;
      int smallRow = row < checkRow ? row : checkRow; // row and checkRow should not be the same, already handled
      for(int checkCol : rowLink){
        //int checkCol = rowLink.get(0);
        if(row == checkRow && col == checkCol) continue;
        // when it's on same row, check if there's a tree between [row, col] and [row, checkCol]
        if(row == checkRow){
          boolean treeBetween = false;
          for(Tree tree : input.trees){
            if(tree.row == row){
              int largeCol = col > checkCol ? col : checkCol;
              int smallCol = col < checkCol ? col : checkCol;
              if(tree.col < largeCol && tree.col > smallCol){
                treeBetween = true;
                break;
              }
            }
          }
          if(!treeBetween){
            // one conflict
            result++;
          }
        }

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
          if(!treeBetween){
            result++;
          }
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
          if(!treeBetween){
            result++;
          }
        }
      }
    }
    return result;
  }

  private ArrayList<ArrayList<Integer>> randomNeighbor(ArrayList<ArrayList<Integer>> cols, Input input, Random rand){
    ArrayList<ArrayList<Integer>> new_cols = new ArrayList<ArrayList<Integer>>();
    for(ArrayList<Integer> list : cols){
      new_cols.add(new ArrayList<Integer>(list));
    }

    int lizardToMove = rand.nextInt(input.numLizard);
    int count = -1;
    int changingRow = 0;
    int changingCol = -1;
    for(ArrayList<Integer> list : new_cols){
      for(int col : list){
        count++;
        if(count == lizardToMove){
          changingCol = col;
          break;
        }
      }
      if(changingCol != -1) break;
      changingRow++;
    }

    // now delete row and col, and generate a new random position
    new_cols.get(changingRow).remove((Integer)changingCol);
    if(!randomPosition(new_cols, input, rand)) return null;
    return new_cols;
  }

  private int isRepeated(ArrayList<ArrayList<Integer>> cols, int row, int col, int size){
    int result = -1; // 0 is not repeated, 1 is repeated, -1 is all full
    for(ArrayList<Integer> list : cols){
      if(list.size() != size){
        result = 0;
        break;
      }
    }
    if(result == -1) return result;

    result = cols.get(row).contains(col) ? 1 : 0;
    return result;
  }

  private boolean randomPosition(ArrayList<ArrayList<Integer>> cols, Input input, Random rand){
    int row = -1;
    int col = -1;
    int repeated = 1;

    do {
      row = rand.nextInt(input.size);
      col = rand.nextInt(input.size);

      repeated = isRepeated(cols, row, col, input.size);
      if(repeated == -1) break; // if -1, means all position in board are occupied
    } while (!noTree(input, row, col) || repeated == 1);
    cols.get(row).add(col);

    if(repeated == -1) return false;
    return true;
  }

  private boolean initRandomPosition(ArrayList<ArrayList<Integer>> cols, Input input, Random rand){
    for(int lizard = 0; lizard < input.numLizard; lizard++){
      if(!randomPosition(cols, input, rand)) return false;
    }
    return true;
  }

  private boolean acceptProbability(double temp, int deltaE){
    if(deltaE < 0) return true;
    double p = Math.exp(-deltaE / temp);
    double random = Math.random();

    return random < p ? true : false;
  }

  private boolean placeLizardBFS(ArrayList<ArrayList<ArrayList<Integer>>> results, Input input){
    Queue<Node> queue = new LinkedList<Node>();

    ArrayList<ArrayList<Integer>> init_cols = new ArrayList<ArrayList<Integer>>(input.size);
    initCols(init_cols, input.size);
    queue.add(new Node(-1, init_cols, input.numLizard));

    while(!queue.isEmpty()){
      Node node = queue.remove();
      int row = node.lastRow;
      ArrayList<ArrayList<Integer>> cols = node.cols;


      // queue the node with cols of new row before the first tree in the row
      row++;
      if(row < input.size && node.lizardLeft > 0){
        ArrayList<ArrayList<Integer>> tmp = cloneCols(cols);
        boolean foundTree = false;
        for(int col = 0; col < input.size && !foundTree; col++){
          // make sure [row, col] is not a tree
          foundTree = !noTree(input, row, col);
          if(!foundTree && noEat(cols, row, col, input)){
            ArrayList<Integer> rowList = cols.get(row);
            rowList.add(col);
            queue.add(new Node(row, cloneCols(cols), node.lizardLeft - 1));
            rowList.remove(rowList.size() - 1); // remove the last
          }
        }

        // skip row without placing lizard
        queue.add(new Node(row, tmp, node.lizardLeft));
      }
      else{
        if(node.lizardLeft == 0){
          results.add(cloneCols(cols));
          return true;
        }
      }
      row--;
      // detect if current row has tree, if yes, queue the nodes with cols after the tree
      if(row > -1 && row < input.size && node.lizardLeft > 0){
        for(Tree tree : input.trees){
          if(tree.row == row){
            // detect if any lizard is placed after this tree
            int until = noLizardAfterTree(tree.row, tree.col, cols, input); // this gives the col that you can put lizard util, -1 if such node already in queue
            for(int col = tree.col + 1; col < until && col < input.size; col++){
              if(noEat(cols, row, col, input)){
                ArrayList<Integer> rowList = cols.get(row);
                if(!rowList.contains(col)){
                  rowList.add(col);
                  queue.add(new Node(row, cloneCols(cols), node.lizardLeft - 1));
                  rowList.remove(rowList.size() - 1); // remove the last
                }
              }
            }
          }
        }
      }
    }
    return false;
    //return true;
  }

  private boolean placeLizardDFS(ArrayList<ArrayList<ArrayList<Integer>>> results, Input input){
    Stack<Node> stack = new Stack<Node>();

    ArrayList<ArrayList<Integer>> init_cols = new ArrayList<ArrayList<Integer>>(input.size);
    initCols(init_cols, input.size);
    stack.push(new Node(-1, init_cols, input.numLizard));

    while(!stack.isEmpty()){
      Node node = stack.pop();
      int row = node.lastRow;
      ArrayList<ArrayList<Integer>> cols = node.cols;
      // detect if current row has tree, if yes, queue the nodes with cols after the tree
      if(row > -1 && row < input.size && node.lizardLeft > 0){
        for(Tree tree : input.trees){
          if(tree.row == row){
            // detect if any lizard is placed after this tree
            int until = noLizardAfterTree(tree.row, tree.col, cols, input); // this gives the col that you can put lizard util, -1 if such node already in queue
            for(int col = tree.col + 1; col < until && col < input.size; col++){
              if(noEat(cols, row, col, input)){
                ArrayList<Integer> rowList = cols.get(row);
                if(!rowList.contains(col)){
                  rowList.add(col);
                  stack.push(new Node(row, cloneCols(cols), node.lizardLeft - 1));
                  rowList.remove(rowList.size() - 1); // remove the last
                }
              }
            }
          }
        }
      }

      // queue the node with cols of new row before the first tree in the row
      row++;
      if(row < input.size && node.lizardLeft > 0){
        ArrayList<ArrayList<Integer>> tmp = cloneCols(cols);
        // skip row without placing lizard
        stack.push(new Node(row, tmp, node.lizardLeft));
        boolean foundTree = false;
        for(int col = 0; col < input.size && !foundTree; col++){
          // make sure [row, col] is not a tree
          foundTree = !noTree(input, row, col);
          if(!foundTree && noEat(cols, row, col, input)){
            ArrayList<Integer> rowList = cols.get(row);
            rowList.add(col);
            stack.push(new Node(row, cloneCols(cols), node.lizardLeft - 1));
            rowList.remove(rowList.size() - 1); // remove the last
          }
        }
      }
      else{
        if(node.lizardLeft == 0){
          results.add(cloneCols(cols));
          return true;
        }
      }
    }
    return false;
    //return true;
  }

  private int noLizardAfterTree(int treeRow, int treeCol, ArrayList<ArrayList<Integer>> cols, Input input){
    // check if any lizard is placed between this tree and the next tree (if any) in the same row
    // if a lizard is already in between, return -1
    // if no lizard, return the col before the next tree or the end of col
    ArrayList<Integer> rowList = cols.get(treeRow);
    int result = input.size;

    // first determine if any other tree is after current tree
    for(Tree tree : input.trees){
      if(tree.row == treeRow && tree.col > treeCol){
        result = tree.col;
        break;
      }
    }

    for(int col : rowList){
      if(col > treeCol && col < result){
        result = -1;
        break;
      }
    }

    return result;
  }

  private ArrayList<ArrayList<Integer>> cloneCols(ArrayList<ArrayList<Integer>> cols){
    ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>(cols.size());
    for(ArrayList<Integer> link : cols){
      result.add(new ArrayList<Integer>(link));
    }

    return result;
  }

  private void initCols(ArrayList<ArrayList<Integer>> init_cols, int len){
    for(int i = 0; i < len; i++) {
        init_cols.add(new ArrayList<Integer>(len));
    }
  }

  private boolean noTree(Input input, int row, int col){
    for(Tree tree : input.trees){
      if(tree.row == row && tree.col == col) return false;
    }

    return true;
  }

  private boolean noEat(ArrayList<ArrayList<Integer>> cols, int row, int col, Input input){
    // now with trees, because of trees, multiple lizards can be in the same row, same col, same diagonal
    for(int checkRow = 0; checkRow < input.size; checkRow++){
      ArrayList<Integer> rowLink = cols.get(checkRow);
      if(rowLink.size() == 0) continue;
      int largeRow = row > checkRow ? row : checkRow;
      int smallRow = row < checkRow ? row : checkRow; // row and checkRow should not be the same, already handled
      for(int checkCol : rowLink){
        //int checkCol = rowLink.get(0);
        if(row == checkRow && col == checkCol) continue;
        // when it's on same row, check if there's a tree between [row, col] and [row, checkCol]
        if(row == checkRow){
          boolean treeBetween = false;
          for(Tree tree : input.trees){
            if(tree.row == row){
              int largeCol = col > checkCol ? col : checkCol;
              int smallCol = col < checkCol ? col : checkCol;
              if(tree.col < largeCol && tree.col > smallCol){
                treeBetween = true;
                break;
              }
            }
          }
          if(!treeBetween) return false;
        }

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


  private boolean checkValid(ArrayList<ArrayList<Integer>> cols, Input input){
    int numLizard = 0;
    for(int row = 0; row < input.size; row++){
      ArrayList<Integer> rowList = cols.get(row);
      if(rowList.size() == 0) continue;

      for(int col : rowList){
        numLizard++;
        if(!noTree(input, row, col)) return false;
        if(!noEat(cols, row, col, input)) return false;
      }
    }
    if(numLizard != input.numLizard) return false;
    return true;
  }


  private void printResults(ArrayList<ArrayList<ArrayList<Integer>>> results, boolean printFalse, Input input){
    int count = 0;
    for(ArrayList<ArrayList<Integer>> result : results){
      if(printFalse && !checkValid(result, input)){
        count++;
        printResult(result, input);
        System.out.println();
        System.out.println();
      }
      else if(!printFalse){
        if(!checkValid(result, input)){
          count++;
        }
        printResult(result, input);
        System.out.println();
        System.out.println();
      }
    }

    System.out.println("Error placement: " + count);
  }

  private void printResult(ArrayList<ArrayList<Integer>> result, Input input){
    for(int row = 0; row < result.size(); row++){
      ArrayList<Integer> rowLink = result.get(row);
      String line = input.lines.get(row);
      if(rowLink.size() != 0){
        Collections.sort(rowLink);
        StringBuilder builder = new StringBuilder();
        int startPoint = 0;
        for(int col : rowLink){
          builder.append(line.substring(startPoint, col));
          builder.append("1");
          startPoint = col + 1;
        }
        builder.append(line.substring(startPoint));
        System.out.println(builder.toString().replace("", " ").trim());
      }
      else{
        line = line.replace("", " ").trim();
        System.out.println(line);
      }
    }
  }

  public void writeOutput(ArrayList<ArrayList<ArrayList<Integer>>> results, Input input)
  throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
    if(results == null || results.size() == 0){
      writer.write("FAIL");
    }
    else{
      writer.write("OK\n");
      ArrayList<ArrayList<Integer>> result = results.get(0);
      for(int row = 0; row < result.size(); row++){
        ArrayList<Integer> rowLink = result.get(row);
        String line = input.lines.get(row);
        if(rowLink.size() != 0){
          Collections.sort(rowLink);
          StringBuilder builder = new StringBuilder();
          int startPoint = 0;
          for(int col : rowLink){
            builder.append(line.substring(startPoint, col));
            builder.append("1");
            startPoint = col + 1;
          }
          builder.append(line.substring(startPoint));
          writer.write(builder.toString());
        }
        else{
          writer.write(line);
        }
        if(row < result.size() - 1){
          writer.write("\n");
        }
      }
    }
    writer.close();
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
    public ArrayList<ArrayList<Integer>> cols;
    public int lizardLeft;

    public Node(int row, ArrayList<ArrayList<Integer>> cols, int lizard){
      lastRow = row;
      this.cols = cols;
      lizardLeft = lizard;
    }
  }
}
