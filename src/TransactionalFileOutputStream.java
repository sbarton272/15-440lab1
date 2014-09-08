import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	//private static final String SERIAL_PATH = "tmp";
	private String fileName;
	//private Serializer serializer;
	private OutputStream outFile;
	
	public TransactionalFileOutputStream(String fileName, boolean b) throws FileNotFoundException {
		// TODO what is boolean for?
		this.fileName = fileName;
		outFile = new FileOutputStream(fileName);
		
		// Generate unique file name for serialization of file descriptor
		//String uid = Integer.toString(outFile.hashCode());
		//serializer = new Serializer(SERIAL_PATH + java.io.File.separator + uid);
		
		// Serialize outFile so ready to be used later
		//serializer.serialize(outFile);
		
		// Close outFile so other processes can use it
		try {
			outFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deserialize the outFile, write to it, serialize and close the connection.
	 */
	@Override
	public void write(int arg0) throws IOException {
		// TODO optimize by leaving open unless migrated
		// TODO does process ever close?
        
        // Write to current serialzed outFile and serialize
		//outFile = (OutputStream) serializer.deserialize();
		outFile = new FileOutputStream(fileName);
		outFile.write(arg0);
		//serializer.serialize(outFile);
		
		// Close file so it can be used by other processes
		outFile.close();
	}
	
}
