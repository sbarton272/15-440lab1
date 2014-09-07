import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	private OutputStream inFile;
	
	public TransactionalFileOutputStream(String fileName, boolean b) throws FileNotFoundException {
		// TODO what is boolean for?
		inFile = new FileOutputStream(fileName);
		
		// Generate unique file name for serialization of file descriptor
		String uid = Integer.toString(inFile.hashCode());
		serializedFileName = new String(SERIAL_PATH + java.io.File.separator + uid);
	}
	
	@Override
	public void write(int arg0) throws IOException {
		// TODO Auto-generated method stub
		// TODO: write, serialize, close, return

	}

	/**
	 * Serializes inFile by saving it to a unique serialization file.
	 */
	private void serializeFileDescriptor() {
		try {
			// TODO add mutex?
			FileOutputStream serializedFile = new FileOutputStream(serializedFileName);
			ObjectOutputStream objStream = new ObjectOutputStream(serializedFile);
			objStream.writeObject(inFile);
			objStream.close();
			serializedFile.close();
			
			// TODO debug
	        System.out.printf("Serialized data is saved in " + serializedFileName);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
