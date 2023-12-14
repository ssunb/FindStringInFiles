/**  This program finds a string from all the files within a directory
 **  and its sub-directories.
 **  Author:  Subel Sunbeeb  
 **  Email:   subel.sunbeeb@gmail.com
>>>>>>> updatePreface
 **  Date  :  10/18/98  
 **/

import java.io.*;

public class Find {

   private static final int FILE_LIMIT = 100; 
   private static final int DIR_LIMIT = 20; 

   private static ThreadGroup limitFile = new ThreadGroup("LimitFile");
   private static ThreadGroup limitDir = new ThreadGroup("LimitDir");

   private static String string = null;
   private static String extension = null;
   private static int count = 0;
   private boolean caseSensitive = false;
  

   public Find(String directory, String string,  String extension) {
	SearchDir d;
	File dir = new File(directory);
        if (string.startsWith("-") && (string.length() > 1) ) {
	   this.caseSensitive = true;
           this.string = string.substring(1, string.length());
        }
        else          
	    this.string = string.toLowerCase();
	this.extension = extension;
	if (dir.isDirectory()){
 	   d = new SearchDir(limitDir, dir);
	   d.start();
	}
	else{
	   System.out.println("ERROR (Constructor1): Directory "+directory+" not found");
   	   error();
	}
   }


   public Find(String path, String directory, boolean caseSensitive) {
	SearchDir d;
	this.caseSensitive = caseSensitive;
	File dir = new File(path, directory);
	if (dir.isDirectory()){
	  /* if (limitDir.activeCount() > DIR_LIMIT) {
		System.out.println(limitDir.activeCount());
		try{ Thread.sleep(5000);}
	    	catch(InterruptedException e) {
				System.out.println("ERROR (Sleep):"+e.getMessage());
		}

	   }*/

	   d = new SearchDir(limitDir, dir);
	   d.start();
	}
	else
	   System.out.println("ERROR (Constructor2): Directory "+directory+" not found");
    }

   
     public class SearchDir extends Thread  {	
	String[] files;
	File dir;

	public SearchDir(ThreadGroup groupDir, File dir) {
		super(groupDir, "limitDir");	
		files = dir.list();
		this.dir = dir;
	}
	
         public void run() {
	  String path = dir.getAbsolutePath();
	  try{
	   for (int i=0; i<files.length; i++) {   
	    if (files[i] != null) {
		  int deadLockBreaker=0;
		  while ((limitFile.activeCount() > FILE_LIMIT) && (deadLockBreaker < 10)) {
			//System.out.print(limitFile.activeCount()+" ");
			try{ Thread.sleep(2000);}
	    	  	catch(InterruptedException e) {
				System.out.println("ERROR (Sleep):"+e.getMessage());
			}
			deadLockBreaker++;
		  }

		  SearchFile f =  new  SearchFile(limitFile, path, files[i].toLowerCase());
		  f.start();
 
	    	  try{ Thread.sleep(25);}
	    	  catch(InterruptedException e) {System.out.println("ERROR (Sleep):"+e.getMessage());}
  	       
	    }
	   }
	  }
	  catch(NullPointerException e) {}   
	 }	

      }


    public class SearchFile extends Thread {

	String path;
	String s; 
	Find find;

	public SearchFile(ThreadGroup groupFile, String path, String s) {
	 super(groupFile, "LimitFile"); 
	 this.path = path;
	 this.s    = s;
	}
	

	public void run() {	

     	 File file = new File(path, s);
	

	//System.out.println("---------------------------------" + file.getPath()); 
 
	 if ( file.isFile() && 
	     (s.endsWith(extension) ||
	      (extension.equals("*")  &&
	       !s.endsWith(".exe") &&
	       !s.endsWith(".dll") &&
	       !s.endsWith(".class") &&
	       !s.endsWith(".jar") &&
	       !s.endsWith(".zip") &&
	       !s.endsWith(".gif") &&
	       !s.endsWith(".doc") &&
	       !s.endsWith(".xls") &&
	       !s.endsWith(".hlp") &&
	       !s.endsWith(".bmp") &&
	       !s.endsWith(".ico") &&
	       !s.endsWith(".jpeg")  &&
	       !s.endsWith(".jpg") &&
	       !s.endsWith(".wav") &&
	       !s.endsWith(".ppt") &&
	       !s.endsWith(".ani") &&
	       !s.endsWith(".mdb") &&
	       !s.endsWith(".mde") &&
	       !s.endsWith(".pf") &&
	       !s.endsWith(".lib") &&
	       !s.endsWith(".mdf") &&
	       !s.endsWith(".ldf") &&
	       !s.endsWith(".lil") &&
	       !s.endsWith(".ttf")      // add more as necessary 
	      ))
	     ) {
	  try {
	   BufferedReader f = new BufferedReader(new FileReader(file));
	   int bytes_read;
	   String line, line2; 
	   int counter = 0;
	   while((line = f.readLine()) != null) {
		counter++;
		if (!caseSensitive) 
 			line2 = line.toLowerCase(); 
		else    line2 = line;
		if (line2.indexOf(string) > -1) {
		   count++;
		   System.out.println("-------"+file.getAbsolutePath()+ "   Line# "+counter+":  "+line);
		}
	     }
	    f = null;
	  }
	  catch(FileNotFoundException e)   {System.out.println("ERROR1: "+e.getMessage());}
	  catch(IOException e)             {System.out.println("ERROR2: "+e.getMessage());}
	  //System.out.println("Not found in: "+file.getPath() + "  "+limitFile.activeCount());
	 }
	 else if (file.isDirectory()) 
	        find = new Find(path, s, caseSensitive);
	 //System.out.println("End:  " + file.getPath());
        }

     }

    public static void error() {
	System.err.println("Usage1: java Find <string  OR  -string (match case)>");
	System.err.println("Usage2: java Find <string> <path AND directory name>");
	System.err.println("Usage3: java Find <string> </e filename OR extension>");
        System.err.println("Usage4: java Find <string> <path AND directory name> <filename OR extension>");
	System.exit(0);
    }	

   public static void finalLine(String string){
	do {
	     try{ Thread.sleep(3000); }
	     catch(InterruptedException e) {System.out.println("ERROR (Sleep1):"+e.getMessage());}
	}   while ((limitFile.activeCount() > 0) || (limitDir.activeCount() > 0));  
	
	if (string.charAt(0) == '-')
		string = string.substring(1,string.length());

	System.out.println("\n\n"+count+" lines that contain the string \""+string+"\" are found.");  
  }


    public static void main(String[] args) {

	if (args.length < 1) 		error();	
	
	String dir = null;	
	if  ((args.length > 1)       && 
	     (!args[1].equals("/e")) && 
	     (!args[1].equals("/E"))) 
			 		dir = args[1];
	
	if (dir == null)		dir = System.getProperty("user.dir");

	String ext = "*";
	if (args.length >2 )		ext = args[2];

	Find find = new Find(dir, args[0], ext.toLowerCase());   
	finalLine(args[0]);     
    }           
}