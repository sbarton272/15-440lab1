import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;

import server.ProcessManager;
import migratableprocess.FindReplaceProcess;
import migratableprocess.GrepProcess;
import migratableprocess.MigratableProcess;
import migratableprocess.WriteFibProcess;

/**
 * TODO cleanup try/catch TODO debug remove TODO debug migrate TODO test with
 * remaining TODO build command line interface TODO get working on GHC machines
 * 
 * @author Spencer
 * 
 */
public class Lab1 {

	private static ProcessManager mProcessManager = new ProcessManager();
	private static final String PROMPT = "> ";
	private static final String HELP_MSG = "The following commands are available:\n"
			+ "launch <hostname> <port> <processName> <args>\n"
			+ "remove <pid>\n"
			+ "migrate <hostname> <port> <pid>\n"
			+ "isalive <pid>" + "test";
	private static final String LAUNCH = "launch";
	private static final String REMOVE = "remove";
	private static final String MIGRATE = "migrate";
	private static final String IS_ALIVE = "isalive";
	private static final String RUN_TEST = "test";

	public static void main(String[] args) throws Exception {

		// TODO provide cmd line UI: add worker, 3 cmds with process, poll pid
		// state

		// Startup
		System.out.println("Lab and process manager online");

		// Read user input, if invalid print help
		while (true) {
			BufferedReader promptIn = new BufferedReader(new InputStreamReader(
					System.in));

			// Read user input
			try {

				// Parse line, split on spaces
				String[] inputArgs = promptIn.readLine().split(" ");

				// Print help if just pressed enter
				if (inputArgs.length == 0) {
					System.out.println(HELP_MSG);
				}

				// Case on command type
				String command = inputArgs[0].toLowerCase();
				if (command.equals(LAUNCH)) {

					// Extract arguments
					if (inputArgs.length != 5) {
						System.out.println(HELP_MSG);
					}

					String hostname = inputArgs[1];
					int port = Integer.parseInt(inputArgs[2]);

					// Construct class given name of class in string
					Class<?> c = Class.forName(inputArgs[3]);
					Constructor<?> constructor = c
							.getConstructor(String[].class);
					MigratableProcess process = (MigratableProcess) constructor
							.newInstance(inputArgs[4]);

					// Launch process
					int pid = mProcessManager.launch(hostname, port, process);
					System.out.println("Started process with id (" + pid + ")");

				} else if (command.equals(REMOVE)) {

					// Extract arguments
					if (inputArgs.length != 2) {
						System.out.println(HELP_MSG);
					}

					int pid = Integer.parseInt(inputArgs[1]);

					// Launch process
					mProcessManager.remove(pid);

				} else if (command.equals(MIGRATE)) {

					// Extract arguments
					if (inputArgs.length != 4) {
						System.out.println(HELP_MSG);
					}

					String hostname = inputArgs[1];
					int port = Integer.parseInt(inputArgs[2]);
					int pid = Integer.parseInt(inputArgs[3]);

					// Launch process
					mProcessManager.migrate(hostname, port, pid);

				} else if (command.equals(IS_ALIVE)) {

					// Extract arguments
					if (inputArgs.length != 2) {
						System.out.println(HELP_MSG);
					}

					int pid = Integer.parseInt(inputArgs[1]);

					// Launch process
					boolean alive = mProcessManager.isAlive(pid);
					System.out.println("Is process alive? " + alive);

				} else if (command.equals(RUN_TEST)) {
					runTest1();
				} else {

					// Print help
					System.out.println(HELP_MSG);
				}

			} catch (IOException e) {
				System.out.println("System exiting on error");
				System.exit(1);
			}

		}

		// runTest1();
	}

	private static void runTest1() throws Exception {
		// Grep process
		String[] grepArgs = { "aa", "test/grep.txt", "test/grepOut2.txt" };
		MigratableProcess grepProcess = new GrepProcess(grepArgs);

		System.out.println("Starting with " + grepProcess);

		int grepPid = mProcessManager.launch("localhost", 80, grepProcess);
		Thread.sleep(500);
		mProcessManager.migrate("localhost", 81, grepPid);
		Thread.sleep(1000);
		System.out
				.println("Process alive? " + mProcessManager.isAlive(grepPid));
		mProcessManager.remove(grepPid);
		System.out
				.println("Process alive? " + mProcessManager.isAlive(grepPid));
		mProcessManager.remove(grepPid);
		mProcessManager.migrate("localhost", 80, grepPid);
		mProcessManager.launch("localhost", 80, grepProcess);

		// Try fib process
		String[] fibArgs = { "15", "test/fib1.txt" };
		MigratableProcess fibProcess = new WriteFibProcess(fibArgs);

		mProcessManager.launch("localhost", 80, fibProcess);
		mProcessManager.launch("localhost", 80, fibProcess);
		mProcessManager.launch("localhost", 80, fibProcess);
		mProcessManager.launch("localhost", 81, fibProcess);
		mProcessManager.launch("localhost", 81, fibProcess);
		mProcessManager.launch("localhost", 81, fibProcess);

		// Try FindReplace
		String[] replaceArgs = { "aa", "AA", "test/grep.txt",
				"test/findReplaceOut1.txt" };
		MigratableProcess findReplaceProcess = new FindReplaceProcess(
				replaceArgs);
		mProcessManager.launch("localhost", 80, findReplaceProcess);
	}
}
