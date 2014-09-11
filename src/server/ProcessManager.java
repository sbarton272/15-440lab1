package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import message.LaunchRequest;
import message.LaunchResponse;
import message.RemoveRequest;
import message.RemoveResponse;
import migratableprocess.MigratableProcess;

/**
 * 
 * @author Spencer
 * 
 */
public class ProcessManager {

	private HashMap<Integer, InetSocketAddress> mPidWorkerMap;

	public ProcessManager() {
		mPidWorkerMap = new HashMap<Integer, InetSocketAddress>();
	}

	// TODO split into chunks to make try/catch easier to see
	public int launch(String host, int port, MigratableProcess process) {
		System.out.println("LAUNCH: " + host + ":" + Integer.toString(port));

		// Generate pid
		int pid = process.hashCode();
		process.setPid(pid);

		try {

			// Open socket to given worker
			Socket soc = new Socket(host, port);

			// Send process to worker in serialized form
			ObjectOutputStream workerOutStream = new ObjectOutputStream(
					soc.getOutputStream());
			workerOutStream.writeObject(new LaunchRequest(process));

			// Wait on response from worker
			ObjectInputStream workerInStream = new ObjectInputStream(
					soc.getInputStream());
			try {

				// blocks here
				LaunchResponse response = (LaunchResponse) workerInStream
						.readObject();

				// If success store pid -> worker and set pid
				if (response.isSuccess()) {
					System.out.println("LAUNCH SUCCESS: " + host + ":"
							+ Integer.toString(port));

					// Store pid -> worker
					mPidWorkerMap.put(pid, new InetSocketAddress(host, port));
				} else {

					// Error case
					pid = -1;
				}

			} catch (ClassNotFoundException e) {
				e.printStackTrace();

				// Error case
				pid = -1;
			}

			workerOutStream.close();
			workerInStream.close();
			soc.close();

		} catch (IOException e) {
			System.out.println("Cannot connect/send: " + host + ":"
					+ Integer.toString(port));

			// Error case
			pid = -1;
		}
		// Error case
		return pid;
	}

	public MigratableProcess remove(int pid) {

		// Lookup worker, print error if not alive
		InetSocketAddress workerAddr = mPidWorkerMap.get(pid);
		if (workerAddr == null) {
			System.out.println("Process is dead: " + pid);
			return null;
		}

		// Open socket to worker, error if no socket available
		Socket workerSoc;
		try {
			workerSoc = new Socket(workerAddr.getHostString(),
					workerAddr.getPort());

			// Attempt communication over socket. Send request, receive response
			RemoveResponse response = sendRemove(workerSoc, pid);

			// If response is null we had an error
			if (response == null) {
				System.out
						.println("Unable to send request or recieve response");
				return null;
			}

			MigratableProcess process = null;
			if (response.isSuccess()) {

				if (response.isProcessAlive()) {

					// Success so extract process
					process = response.getProcess();
				} else {

					// If process is dead remove from mPidWorkerMap
					mPidWorkerMap.remove(pid);
					System.out.println("Process is dead: " + pid);
				}

			} else {
				// Print that error occurred
				System.out.println("Invalid response from worker");
			}

			workerSoc.close();

			// Potential success at this return
			return process;

		} catch (IOException e) {
			System.out.println("Unable to connect to worker: "
					+ workerAddr.getHostString() + ":" + workerAddr.getPort());
		}

		// Other error case
		return null;

	}

	private RemoveResponse sendRemove(Socket workerSoc, int pid) {
		// Attempt communication over socket. Send request, receive response
		try {

			// Send remove message with pid
			ObjectOutputStream workerOutStream = new ObjectOutputStream(
					workerSoc.getOutputStream());
			workerOutStream.writeObject(new RemoveRequest(pid));

			// Block on response
			ObjectInputStream workerInStream = new ObjectInputStream(
					workerSoc.getInputStream());
			RemoveResponse response = (RemoveResponse) workerInStream
					.readObject();

			// Close streams
			workerOutStream.close();
			workerInStream.close();
			return response;

		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * 
	 * @param host
	 *            host of new worker
	 * @param port
	 *            port of new worker
	 * @param pid
	 *            process id
	 */
	public void migrate(String host, int port, int pid) {

		// Remove process from first worker
		MigratableProcess process = remove(pid);

		if (process == null) {
			System.out.println("Unable to remove process from original worker "
					+ pid);
			return;
		}

		// Launch process on second worker
		launch(host, port, process);

	}

}
