package migratableprocess;
import java.io.PrintStream;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.util.Arrays;

import transactionalfilestream.TransactionalFileOutputStream;

public class WriteFibProcess extends MigratableProcess {
	
	private static final long serialVersionUID = 5119275455400124063L;
	private TransactionalFileOutputStream outFile;
	private int maxDepth;
	private int depth;
	private int[] priorFib; 
	private String[] mArgs;
	private static final String PROCESS_NAME = "FindReplaceProcess";

	private volatile boolean suspending;

	public WriteFibProcess(String args[]) throws Exception
	{
		if (args.length != 2) {
			System.out.println("usage: WriteFibProcess <depth> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		mArgs = args;
		depth = 0;
		priorFib = new int[2];
		priorFib[0] = 0;
		priorFib[1] = 1;
		
		maxDepth = Integer.parseInt(args[0]);
		outFile = new TransactionalFileOutputStream(args[1], false);
	}

	public void run()
	{
		
		PrintStream out = new PrintStream(outFile);

		while (!suspending) {
			
			// Starts printing at 0
			out.println(priorFib[0]);
			
			// Calculate next and save
			int nextFib = priorFib[0] + priorFib[1];
			priorFib[0] = priorFib[1];
			priorFib[1] = nextFib;
			depth += 1;
			
			if (depth == maxDepth) {
				break;
			}
			
			// Make take longer so that we don't require extremely large files for interesting results
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore it
			}
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