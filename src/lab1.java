
public class lab1 {

	public static void main(String [ ] args) throws Exception {
		
		ProcessManager processManager = new ProcessManager();

		String[] grepArgs = {"aa", "test/grep.txt", "test/grepOut.txt"};
		MigratableProcess grepProcess = new GrepProcess(grepArgs);
		
		int grepPid = processManager.launch(grepProcess);
		
	}
	
}
