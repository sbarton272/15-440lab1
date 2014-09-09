
public class lab1 {

	public static void main(String [ ] args) throws Exception {
		
		ProcessManager processManager = new ProcessManager();

		String[] grepArgs = {"aa", "test/grep.txt", "test/grepOut2.txt"};
		MigratableProcess grepProcess = new GrepProcess(grepArgs);
		
		int grepPid = processManager.launch(grepProcess);
		Thread.sleep(10);
		//processManager.migrate(grepPid);
		
	}
	
}
