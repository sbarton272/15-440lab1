import server.ProcessManager;
import migratableprocess.FindReplaceProcess;
import migratableprocess.GrepProcess;
import migratableprocess.MigratableProcess;
import migratableprocess.WriteFibProcess;

public class Lab1 {
	
	public static void main(String [ ] args) throws Exception {
				
		// TODO provide cmd line UI: add worker, 3 cmds with process, poll pid state 
		
		System.out.println("Lab and process manager online");
		
		ProcessManager processManager = new ProcessManager();

		// Grep process
		String[] grepArgs = {"aa", "test/grep.txt", "test/grepOut2.txt"};
		MigratableProcess grepProcess = new GrepProcess(grepArgs);
		
		int grepPid = processManager.launch("localhost", 80, grepProcess);
//		Thread.sleep(500);
//		processManager.migrate(grepPid);
//		//Thread.sleep(1000);
//		//processManager.remove(grepPid);
//		
//		// Try fib process
//		String[] fibArgs = {"15", "test/fib1.txt"};
//		MigratableProcess fibProcess = new WriteFibProcess(fibArgs);
//		
//		processManager.launch(fibProcess);
//
//		// Try FindReplace
//		String[] replaceArgs = {"aa", "AA", "test/grep.txt", "test/findReplaceOut1.txt"};
//		MigratableProcess findReplaceProcess = new FindReplaceProcess(replaceArgs);
//		processManager.launch(findReplaceProcess);
		
	}
}
