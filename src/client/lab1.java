package client;
import server.ProcessManager;
import migratableprocess.FindReplaceProcess;
import migratableprocess.GrepProcess;
import migratableprocess.MigratableProcess;
import migratableprocess.WriteFibProcess;


public class lab1 {

	public static void main(String [ ] args) throws Exception {
		
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
