import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

public class GrepProcess implements MigratableProcess
{
	private static final long serialVersionUID = -2757242538506557482L;
	private FileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;

	private volatile boolean suspending;

	public GrepProcess(String args[]) throws Exception
	{
		if (args.length != 3) {
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		query = args[0];
		inFile = new FileInputStream(args[1]);//TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2], false);
	}

	public void run()
	{
		PrintStream out = new PrintStream(outFile);
		BufferedReader in = new BufferedReader(new InputStreamReader(inFile));

		try {
			while (!suspending) {
				String line = in.readLine();

				if (line == null) break;
				System.out.println(line);

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
		} catch (IOException e) {
			System.out.println ("GrepProcess: Error: " + e);
		}


		suspending = false;
	}

	public void suspend()
	{
		suspending = true;
		while (suspending);
	}

}