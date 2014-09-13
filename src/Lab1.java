import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;

import server.ProcessManager;
import migratableprocess.FindReplaceProcess;
import migratableprocess.GrepProcess;
import migratableprocess.MigratableProcess;
import migratableprocess.WriteFibProcess;

public class Lab1 {

	private static ProcessManager mProcessManager = new ProcessManager();
	private static final String PROMPT = "> ";
	private static final String HELP_MSG = "The following commands are available:\n"
			+ "launch <hostname> <port> <processName> <args list>\n"
			+ "remove <pid>\n"
			+ "migrate <hostname> <port> <pid>\n"
			+ "isalive <pid>\n"
			+ "test <hostname1> <port1> <hostname2> <port2>\n"
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
					runTest(inputArgs);
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
				e.printStackTrace();
				System.exit(1);
			}

		}

	}

	// ----------------------------------------------------------------

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
		Constructor<?> constructor = c.getConstructor(String[].class);
		String[] processArgs = Arrays.copyOfRange(inputArgs, 4,
				inputArgs.length);
		MigratableProcess process = (MigratableProcess) constructor
				.newInstance((Object) processArgs);

		// Launch process
		int pid = mProcessManager.launch(hostname, port, process);
		if (pid == -1) {
			System.out.println("Unable to start process");
		} else {
			System.out.println("Started process with id (" + pid + ")");
		}
	}

	// ----------------------------------------------------------------

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

	private static void runTest(String[] args) throws Exception {

		// Extract arguments
		if (args.length != 5) {
			System.out.println(HELP_MSG);
			return;
		}

		// Extract worker locations
		String hostname1 = args[1];
		int port1 = Integer.parseInt(args[2]);
		String hostname2 = args[3];
		int port2 = Integer.parseInt(args[4]);

		// Grep process
		String[] grepArgs = { "aa", "test/grep.txt", "test/grepOut2.txt" };
		MigratableProcess grepProcess = new GrepProcess(grepArgs);

		// Fib process
		String[] fibArgs = { "15", "test/fib1.txt" };
		MigratableProcess fibProcess = new WriteFibProcess(fibArgs);

		// FindReplace process
		String[] replaceArgs = { "aa", "AA", "test/grep.txt",
				"test/findReplaceOut1.txt" };
		MigratableProcess findReplaceProcess = new FindReplaceProcess(
				replaceArgs);

		// Try all commands without a process started
		System.out.println("Starting with error cases");
		myAssert(null == mProcessManager.remove(0));
		myAssert(false == mProcessManager.migrate("localhost", 80, 0));
		myAssert(false == mProcessManager.migrate(hostname1, port1, 0));
		myAssert(false == mProcessManager.isAlive(0));
		myAssert(-1 == mProcessManager.launch("localhost", 1, grepProcess));

		System.out.println("Starting with " + grepProcess);

		// Launch, migrate and remove
		int grepPid = mProcessManager.launch(hostname1, port1, grepProcess);
		myAssert(grepPid > 0);
		myAssert(mProcessManager.migrate(hostname2, port2, grepPid));
		myAssert(mProcessManager.isAlive(grepPid));
		myAssert(null != mProcessManager.remove(grepPid));
		myAssert(false == mProcessManager.isAlive(grepPid));

		// Ensure removed
		myAssert(null == mProcessManager.remove(grepPid));
		myAssert(false == mProcessManager.migrate(hostname2, port2, grepPid));

		/**
		 * Start multiple processes and put in set checking each time for
		 * collision No pids should match even though these are the same
		 * processes with the same vars
		 */
		System.out
				.println("Start a bunch of fib, will all overwrite each other");
		HashSet<Integer> pids = new HashSet<Integer>();
		myAssert(pids.add(mProcessManager.launch("localhost", 80, fibProcess)));
		myAssert(pids.add(mProcessManager.launch("localhost", 80,
				new WriteFibProcess(fibArgs))));
		myAssert(pids.add(mProcessManager.launch("localhost", 80,
				new WriteFibProcess(fibArgs))));
		myAssert(pids.add(mProcessManager.launch("localhost", 81,
				new WriteFibProcess(fibArgs))));
		myAssert(pids.add(mProcessManager.launch("localhost", 81,
				new WriteFibProcess(fibArgs))));
		myAssert(pids.add(mProcessManager.launch("localhost", 81,
				new WriteFibProcess(fibArgs))));

		System.out.println("Run find replace just to see if it works");
		myAssert(0 < mProcessManager
				.launch("localhost", 80, findReplaceProcess));
		
	}

	private static void myAssert(boolean b) {
		if (!b) {
			throw new AssertionError("the test broke");
		}
	}
}
