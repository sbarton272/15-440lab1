import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Arrays;

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
			+ "launch <hostname> <port> <processName> <args list>\n"
			+ "remove <pid>\n"
			+ "migrate <hostname> <port> <pid>\n"
			+ "isalive <pid>\n"
			+ "test\n"
			+ "v - toggles verbose mode\n"
			+ "exit";
	private static final String LAUNCH = "launch";
	private static final String REMOVE = "remove";
	private static final String MIGRATE = "migrate";
	private static final String IS_ALIVE = "isalive";
	private static final String RUN_TEST = "test";
	private static final String VERBOSE = "v";
	private static final String EXIT = "exit";
	private static boolean verbose = false;

	public static void main(String[] args) throws Exception {

		// Startup
		System.out.println("Lab and process manager online");
		System.out.println(HELP_MSG);

		// Set process manager verbosity
		mProcessManager.setVerbose(verbose);

		// Read user input, if invalid print help
		while (true) {

			// Print prompt and read input
			System.out.print(PROMPT);
			BufferedReader promptIn = new BufferedReader(new InputStreamReader(
					System.in));

			// Read user input
			try {

				// Parse line, split on spaces
				String line = promptIn.readLine();
				String[] inputArgs = line.split(" ");

				// Get command
				String command = "";
				if (inputArgs.length == 0) {
					if (line.isEmpty()) {

						// Print help if just pressed enter
						System.out.println(HELP_MSG);
					} else {
						command = line;
					}
				} else {
					command = inputArgs[0];
				}

				// Case on command type
				if (command.equals(LAUNCH)) {
					handleLaunch(inputArgs);
				} else if (command.equals(REMOVE)) {
					handleRemove(inputArgs);
				} else if (command.equals(MIGRATE)) {
					handleMigrate(inputArgs);
				} else if (command.equals(IS_ALIVE)) {
					handleIsAlive(inputArgs);
				} else if (command.equals(RUN_TEST)) {
					runTest1();
				} else if (command.equals(VERBOSE)) {

					// Toggle verbose
					verbose = !verbose;
					mProcessManager.setVerbose(verbose);
					System.out.println("Verbose " + verbose);

				} else if (command.equals(EXIT)) {
					System.exit(0);
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

	//----------------------------------------------------------------
	
	private static void handleIsAlive(String[] inputArgs) {

		// Extract arguments
		if (inputArgs.length != 2) {
			System.out.println(HELP_MSG);
			return;
		}

		int pid = Integer.parseInt(inputArgs[1]);

		// Launch process
		boolean alive = mProcessManager.isAlive(pid);
		System.out.println("Is process alive? " + alive);		
	}

	private static void handleMigrate(String[] inputArgs) {
		// Extract arguments
		if (inputArgs.length != 4) {
			System.out.println(HELP_MSG);
			return;
		}

		String hostname = inputArgs[1];
		int port = Integer.parseInt(inputArgs[2]);
		int pid = Integer.parseInt(inputArgs[3]);

		// Launch process
		boolean success = mProcessManager.migrate(hostname, port, pid);
		System.out.println("Migrated successfully? " + success);
	}

	private static void handleLaunch(String[] inputArgs) throws Exception {
		// Extract arguments
		if (inputArgs.length < 4) {
			System.out.println(HELP_MSG);
			return;
		}

		String hostname = inputArgs[1];
		int port = Integer.parseInt(inputArgs[2]);

		// Construct class given name of class in string
		Class<?> c = Class.forName(inputArgs[3]);
		Constructor<?> constructor = c
				.getConstructor(String[].class);
		String[] processArgs = Arrays.copyOfRange(inputArgs, 4,
				inputArgs.length);
		MigratableProcess process = (MigratableProcess) constructor
				.newInstance((Object) processArgs);

		// Launch process
		int pid = mProcessManager.launch(hostname, port, process);
		System.out.println("Started process with id (" + pid + ")");
	}
	
	//----------------------------------------------------------------
	
	private static void handleRemove(String[] inputArgs) {
		// Extract arguments
		if (inputArgs.length != 2) {
			System.out.println(HELP_MSG);
			return;
		}

		int pid = Integer.parseInt(inputArgs[1]);

		// Launch process
		MigratableProcess process = mProcessManager.remove(pid);
		if (process == null) {
			System.out.println("Unable to remove process " + pid);
		} else {
			System.out.println("Remove process " + pid);
		}
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
