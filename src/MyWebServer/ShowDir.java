package MyWebServer;

import java.io.*;

public class ShowDir {
  public static void main(String[] args) {
    File f = new File(".");
    try{
      String directoryRoot = f.getCanonicalPath();
      System.out.println("Directory root is: " + directoryRoot);
    }catch (Throwable e){e.printStackTrace();}
  }
}
