package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import message.LaunchMessage;
import message.ProccessDeadResponse;
import message.RemoveMessage;
import message.Response;
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
			workerOutStream.writeObject(new LaunchMessage(process));

			// Wait on response from worker
			ObjectInputStream workerInStream = new ObjectInputStream(
					soc.getInputStream());
			try {

				// blocks here
				Response response = (Response) workerInStream
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

	public void remove(int pid) {

		// Lookup worker, print error if not alive
		InetSocketAddress workerAddr = mPidWorkerMap.get(pid);
		if (workerAddr == null) {
			System.out.println("Process is dead: " + pid);
			return;
		}

		// Open socket to worker, error if no socket available
		Socket workerSoc;
		try {
			workerSoc = new Socket(workerAddr.getHostString(),
					workerAddr.getPort());

			// Attempt communication over socket. Send request, receive response
			Response response = sendRemove(workerSoc, pid);

			// If response is null we had an error
			if (response == null) {
				System.out
						.println("Unable to send request or recieve response");
				return;
			}

			// Success response do nothing but error print that error occurred
			if (response.isFailure()) {

				// If process is dead remove from mPidWorkerMap
				if (response instanceof ProccessDeadResponse) {
					int deadPid = ((ProccessDeadResponse) response)
							.getPid();
					if (deadPid == pid) {
						mPidWorkerMap.remove(deadPid);
						System.out.println("Process is dead: " + deadPid);
					} else {

						// Error case
						System.out.println("Invalid response from worker");
					}
				}
			}

			workerSoc.close();

		} catch (IOException e) {
			System.out.println("Unable to connect to worker: "
					+ workerAddr.getHostString() + ":" + workerAddr.getPort());
		}

	}

	private Response sendRemove(Socket workerSoc, int pid) {
		// Attempt communication over socket. Send request, receive response
		try {

			// Send remove message with pid
			ObjectOutputStream workerOutStream = new ObjectOutputStream(
					workerSoc.getOutputStream());
			workerOutStream.writeObject(new RemoveMessage(pid));

			// Block on response
			ObjectInputStream workerInStream = new ObjectInputStream(
					workerSoc.getInputStream());
			Response response = (Response) workerInStream
					.readObject();

			// Close streams
			workerOutStream.close();
			workerInStream.close();
			return response;

		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}

	public void migrate(int pid) {
		// TODO
	}

}
