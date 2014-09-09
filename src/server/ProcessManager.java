package server;
import helper.Serializer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import worker.Worker.ThreadRunnablePair;
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
	
	public int launch(String host, int port, MigratableProcess process) {
		System.out.println("LAUNCH: " + host + ":" + Integer.toString(port));
		
		try {
			// Open socket to given worker
			Socket soc = new Socket(host, port);

			// Generate process id
			int pid = process.hashCode();
			
			// Send process to worker in serialized form
			ObjectOutputStream  workerOutStream = new ObjectOutputStream(soc.getOutputStream());
			workerOutStream.writeObject(process);
			workerOutStream.close();
			
			// Wait on response from worker
			ObjectInputStream workerInStream = new ObjectInputStream(soc.getInputStream());
			ResponseMessage response = (ResponseMessage) workerInStream.readObject(); // blocks here
			workerInStream.close();
			
			// If success store pid -> worker and return pid
			if (response.isSuccess()) {
				mPidWorkerMap.put(pid, new InetSocketAddress(host, port));
				return pid;
			}
			
			// If error return -1 to signal error
			return -1;
			
		} catch (IOException e) {
			System.out.println("Cannot connect/send: " + host + ":" + Integer.toString(port));
		}
		// Error case
		return -1;
	}
	
	public void remove(int pid) {

	}
	
	public void migrate(int pid) {

	}
	
}
