package migratableprocess;
import java.io.PrintStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

import transactionalfilestream.TransactionalFileInputStream;
import transactionalfilestream.TransactionalFileOutputStream;

public class GrepProcess extends MigratableProcess {
	private static final long serialVersionUID = -2757242538506557482L;
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;
	private String[] args;

	private volatile boolean suspending;

	public GrepProcess(String args[]) throws Exception
	{
		if (args.length != 3) {
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		this.args = args;
		query = args[0];
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2], false);
	}

	public void run()
	{
		
		PrintStream out = new PrintStream(outFile);
		TransactionalFileInputStream in = inFile;

		try {
			while (!suspending) {
				String line = in.readline(); 
				
				if (line == null) break;
				
				if (line.contains(query)) {
					out.println(line);
				}
				
				// Make grep take longer so that we don't require extremely large files for interesting results
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

	@Override
	public String toString() {
		return "GrepProcess " + args;
	}

}