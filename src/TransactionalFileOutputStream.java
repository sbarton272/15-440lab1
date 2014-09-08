import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;


/**
 * This defines a thread-safe serializable file output stream.
 * 
 * @author Spencer
 *
 */
public class TransactionalFileOutputStream extends OutputStream implements Serializable {

	private static final long serialVersionUID = -3557579291611635226L;
	private static final String SERIAL_PATH = "tmp";
	private String serializedFileName;
	private OutputStream outFile;
	
	public TransactionalFileOutputStream(String fileName, boolean b) throws FileNotFoundException {
		// TODO what is boolean for?
		outFile = new FileOutputStream(fileName);
		
		// Generate unique file name for serialization of file descriptor
		String uid = Integer.toString(outFile.hashCode());
		serializedFileName = new String(SERIAL_PATH + java.io.File.separator + uid);
		
		// Serialize outFile
		serializeOutFile();
		
		// Close outFile so other processes can use it
		try {
			outFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deserialize the outFile, write to it, reserialize and close the connection.
	 */
	@Override
	public void write(int arg0) throws IOException {
		// TODO optimize by leaving open unless migrated
		// TODO does process ever close?
        
        // Write to current serialzed outFile and serialize
        deserializeOutFile();
		outFile.write(arg0);
		serializeOutFile();
		
		// Close file so it can be used by other processes
		outFile.close();
	}

	/**
	 * Serializes outFile by saving it to a unique serialization file.
	 */
	private void serializeOutFile() {
		try {
			// TODO add mutex?
			FileOutputStream serializedFile = new FileOutputStream(serializedFileName);
			ObjectOutputStream objStream = new ObjectOutputStream(serializedFile);
			
			// Serialize
			objStream.writeObject(outFile);
			
	        // Close the serialization files
			objStream.close();
			serializedFile.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deserializes the outFile by taking it from the serialized location
	 */
	private void deserializeOutFile() {
		try {
			FileInputStream serializedFile = new FileInputStream(serializedFileName);
	        ObjectInputStream objStream = new ObjectInputStream(serializedFile);
        
			// Deserialize
			outFile = (OutputStream) objStream.readObject();

			// Close the serialization files
			objStream.close();
	        serializedFile.close();
		
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
