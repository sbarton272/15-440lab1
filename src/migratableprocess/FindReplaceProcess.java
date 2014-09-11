package migratableprocess;
import java.io.PrintStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.util.Arrays;

import transactionalfilestream.TransactionalFileInputStream;
import transactionalfilestream.TransactionalFileOutputStream;

public class FindReplaceProcess extends MigratableProcess {

	private static final long serialVersionUID = -9013736246455305267L;
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;
	private String replacement;
	private String[] mArgs;
	private static final String PROCESS_NAME = "FindReplaceProcess";
	
	private volatile boolean suspending;

	public FindReplaceProcess(String args[]) throws Exception
	{
		if (args.length != 4) {
			System.out.println("usage: FindReplaceProcess <queryString> " +
					"<replacementString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		mArgs = args;
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
				out.println(line.replace(query, replacement));
				
				// Make take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(10);
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

		suspending = false;
	}

	public void suspend()
	{
		suspending = true;		
		while (suspending);
	}
	
	public String toString() {
		return PROCESS_NAME + " " + Arrays.toString(mArgs) + " (" + mPid +")";
	}

}