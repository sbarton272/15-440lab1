import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


public class TransactionalFileInputStream extends InputStream implements Serializable {

	private static final long serialVersionUID = -9067377820514406998L;
	//private static final String SERIAL_PATH = "tmp";
	private String fileName;
	//private Serializer serializer;
	private InputStream inFile;

	public TransactionalFileInputStream(String fileName) throws FileNotFoundException {
		this.fileName = fileName;
		inFile = new FileInputStream(fileName);
		
		// Generate unique file name for serialization of file descriptor
		//String uid = Integer.toString(inFile.hashCode());
		//serializer = new Serializer(SERIAL_PATH + java.io.File.separator + uid);
		
		// Serialize outFile so ready to be used later
		//serializer.serialize(inFile);
		
		// Close inFile so other processes can use it
		try {
			inFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deserialize the input file descriptor, read, serialize and close the connection.
	 */
	@Override
	public int read() throws IOException {
		
		//inFile = (InputStream) serializer.deserialize();
		inFile = new FileInputStream(fileName);
		int readInt = inFile.read();
		//serializer.serialize(inFile);
		
		// Close inFile so other processes can use it
		try {
			inFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return readInt;
	}

}
