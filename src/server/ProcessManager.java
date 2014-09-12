package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import message.AliveRequest;
import message.AliveResponse;
import message.LaunchRequest;
import message.LaunchResponse;
import message.RemoveRequest;
import message.RemoveResponse;
import message.RequestMessage;
import message.ResponseMessage;
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

	// ------------------------------------------------------------

	// TODO split into chunks to make try/catch easier to see
	public int launch(String host, int port, MigratableProcess process) {

		// Generate pid if it does not already have one
		int pid = process.hashCode();
		pid = process.setPid(pid);

		System.out.println("LAUNCH: " + host + ":" + Integer.toString(port)
				+ " (" + pid + ")");

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
							+ Integer.toString(port) + " (" + pid + ")");

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

	// ------------------------------------------------------------

	public MigratableProcess remove(int pid) {

		System.out.println("REMOVE: (" + pid + ")");

		// Lookup worker, print error if not alive
		InetSocketAddress workerAddr = mPidWorkerMap.get(pid);
		if (workerAddr == null) {
			System.out
					.println("REMOVE FAILURE: Process is dead or non-existant ("
							+ pid + ")");
			return null;
		}

		// Open socket to worker, error if no socket available
		Socket workerSoc;
		try {
			workerSoc = new Socket(workerAddr.getHostString(),
					workerAddr.getPort());

			// Attempt communication over socket. Send request, receive response
			RemoveResponse response = (RemoveResponse) sendRequest(workerSoc,
					new RemoveRequest(pid));

			// If response is null we had an error
			if (response == null) {
				System.out
						.println("Unable to send request or recieve response");
				return null;
			}

			MigratableProcess process = null;
			if (response.isSuccess()) {

				// Remove from mPidWorkerMap
				mPidWorkerMap.remove(pid);

				if (response.isProcessAlive()) {

					// Success so extract process
					process = response.getProcess();

					System.out.println("REMOVE SUCCESS: (" + pid + ")");
				} else {

					// If was dead then note this and do not return process
					System.out.println("Process was already dead: " + pid);
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

	// ------------------------------------------------------------

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

		String migrateDetails = host + ":" + port + " (" + pid + ")";
		System.out.println("MIGRATE: " + migrateDetails);

		// Remove process from first worker
		MigratableProcess process = remove(pid);

		if (process == null) {
			System.out
					.println("MIGRATE FAILURE: Unable to remove process from original worker "
							+ migrateDetails);
			return;
		}

		// Launch process on second worker
		int resultPid = launch(host, port, process);

		if (resultPid != -1) {
			System.out.println("MIGRATE SUCCESS: " + migrateDetails);
		} else {
			System.out.println("MIGRATE FAILURE: " + migrateDetails);
		}

	}

	// ------------------------------------------------------------

	/**
	 * Check if the given process is alive
	 * 
	 * @param pid
	 *            process id
	 * @return
	 */
	public boolean isAlive(int pid) {
		System.out.println("IS ALIVE: (" + pid + ")");

		// Lookup worker, return if know not alive
		InetSocketAddress workerAddr = mPidWorkerMap.get(pid);
		if (workerAddr == null) {
			System.out.println("IS ALIVE SUCCESS: (" + pid + ")");
			return false;
		}

		// Connect to worker and check if alive
		// Open socket to worker, error if no socket available
		Socket workerSoc;
		try {
			workerSoc = new Socket(workerAddr.getHostString(),
					workerAddr.getPort());

			// Attempt communication over socket. Send request, receive response
			AliveResponse response = (AliveResponse) sendRequest(workerSoc,
					new AliveRequest(pid));

			// If response is null we had an error
			if (response == null) {
				System.out
						.println("IS ALIVE: Unable to send request or recieve response");
				return false;
			}

			if (response.isSuccess()) {
				
				// Return state of process
				boolean isAlive = response.isAlive();
				
				// If not alive remove from map
				if (!isAlive) {
					mPidWorkerMap.remove(pid);
				}
				
				System.out.println("IS ALIVE SUCCESS: (" + pid + ")");
				return isAlive;
			} else {
				// Print that error occurred
				System.out.println("IS ALIVE: Invalid response from worker");
			}

			workerSoc.close();

		} catch (IOException e) {
			System.out.println("IS ALIVE: Unable to connect to worker: "
					+ workerAddr.getHostString() + ":" + workerAddr.getPort());
		}

		// Not alive or error
		return false;

	}

	// ------------------------------------------------------------

	private ResponseMessage sendRequest(Socket workerSoc, RequestMessage request) {
		// Attempt communication over socket. Send request, receive response
		try {

			// Send request
			ObjectOutputStream workerOutStream = new ObjectOutputStream(
					workerSoc.getOutputStream());
			workerOutStream.writeObject(request);

			// Block on response
			ObjectInputStream workerInStream = new ObjectInputStream(
					workerSoc.getInputStream());
			ResponseMessage response = (ResponseMessage) workerInStream
					.readObject();

			// Close streams
			workerOutStream.close();
			workerInStream.close();
			return response;

		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}

}
