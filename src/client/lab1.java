package client;

import migratableprocess.FindReplaceProcess;
import migratableprocess.GrepProcess;
import migratableprocess.MigratableProcess;
import migratableprocess.WriteFibProcess;
import server.ProcessManager;

public class Lab1 {
	
	public static void main(String [ ] args) throws Exception {
		
		if (args.length != 2) {
			System.out.println("usage: Lab1 <host> <port>");
			throw new Exception("Invalid Arguments");
		}
		
		// Create client object to handle transactions
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		Client client = new Client(host, port);
		
		ProcessManager processManager = new ProcessManager();

		// Grep process
		String[] grepArgs = {"aa", "test/grep.txt", "test/grepOut2.txt"};
		MigratableProcess grepProcess = new GrepProcess(grepArgs);
		
		int grepPid = processManager.launch(grepProcess);
		Thread.sleep(500);
		processManager.migrate(grepPid);
		//Thread.sleep(1000);
		//processManager.remove(grepPid);
		
		// Try fib process
		String[] fibArgs = {"15", "test/fib1.txt"};
		MigratableProcess fibProcess = new WriteFibProcess(fibArgs);
		
		processManager.launch(fibProcess);

		// Try FindReplace
		String[] replaceArgs = {"aa", "AA", "test/grep.txt", "test/findReplaceOut1.txt"};
		MigratableProcess findReplaceProcess = new FindReplaceProcess(replaceArgs);
		processManager.launch(findReplaceProcess);
		
	}
}
