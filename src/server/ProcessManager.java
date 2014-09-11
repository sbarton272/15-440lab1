package server;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import message.LaunchMessage;
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
	
	public int launch(String host, int port, MigratableProcess process) {
		System.out.println("LAUNCH: " + host + ":" + Integer.toString(port));

		// Generate pid
		int pid = process.hashCode();
		process.setPid(pid);
		
		try {
			
			// Open socket to given worker
			Socket soc = new Socket(host, port);
			
			// Send process to worker in serialized form
			ObjectOutputStream  workerOutStream = new ObjectOutputStream(soc.getOutputStream());
			workerOutStream.writeObject(new LaunchMessage(process));
			
			// Wait on response from worker
			ObjectInputStream workerInStream = new ObjectInputStream(soc.getInputStream());
			try {
				
				// blocks here
				ResponseMessage response = (ResponseMessage) workerInStream.readObject();
				
				// If success store pid -> worker and set pid
				if (response.isSuccess()) {
					System.out.println("LAUNCH SUCCESS: " + host + ":" + Integer.toString(port));

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
			System.out.println("Cannot connect/send: " + host + ":" + Integer.toString(port));
			
			// Error case
			pid = -1;
		}
		// Error case
		return pid;
	}
	
	public void remove(int pid) {
		// TODO
	}
	
	public void migrate(int pid) {
		// TODO
	}
	
}
