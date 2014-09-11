package migratableprocess;
import java.io.PrintStream;
import java.lang.Thread;
import java.lang.InterruptedException;

import transactionalfilestream.TransactionalFileOutputStream;

public class WriteFibProcess implements MigratableProcess {
	
	private static final long serialVersionUID = 5119275455400124063L;
	private TransactionalFileOutputStream outFile;
	private int maxDepth;
	private int depth;
	private int[] priorFib; 
	private int mPid;

	private volatile boolean suspending;

	public WriteFibProcess(String args[]) throws Exception
	{
		if (args.length != 2) {
			System.out.println("usage: WriteFibProcess <depth> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
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
	public void setPid(int pid) {
		mPid = pid;
	}

	@Override
	public int getPid() {
		return mPid;
	}

}