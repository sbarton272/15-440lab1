package migratableprocess;
import java.io.PrintStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

import transactionalfilestream.TransactionalFileInputStream;
import transactionalfilestream.TransactionalFileOutputStream;

public class FindReplaceProcess implements MigratableProcess {

	private static final long serialVersionUID = -9013736246455305267L;
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;
	private String replacement; 

	private volatile boolean suspending;

	public FindReplaceProcess(String args[]) throws Exception
	{
		if (args.length != 4) {
			System.out.println("usage: FindReplaceProcess <queryString> " +
					"<replacementString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		query = args[0];
		replacement = args[1];
		inFile = new TransactionalFileInputStream(args[2]);
		outFile = new TransactionalFileOutputStream(args[3], false);
	}

	public void run()
	{
		
		PrintStream out = new PrintStream(outFile);
		TransactionalFileInputStream in = inFile;

		try {
			while (!suspending) {
				String line = in.readline(); 
				
				if (line == null) break;

				// Find and replace
				out.print(line.replace(query, replacement) + '\r'); // TODO loses newlines
				
				// Make take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			//End of File
			System.out.println("EOF");
		} catch (IOException e) {
			System.out.println("GrepProcess: Error: " + e);
		}

		System.out.println("DONE");
		suspending = false;
	}

	public void suspend()
	{
		suspending = true;
		
		System.out.println("SUSPENDED" + suspending);
		while (suspending);
		System.out.println("NOT SUSPENDED");
	}

}