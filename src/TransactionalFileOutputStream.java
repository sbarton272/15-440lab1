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
	private String fileName;
	private boolean append;
	private long position;
	private FileOutputStream outFile;
	
	public TransactionalFileOutputStream(String fileName, boolean append) throws FileNotFoundException {
		// TODO what is boolean for?
		this.fileName = fileName;
		this.append = append;
		
		outFile = new FileOutputStream(fileName, append);
		
		// Get position
		try {
			this.position = outFile.getChannel().position();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
				
		// Close outFile so other processes can use it
		try {
			outFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void write(int b) throws IOException {
		// TODO optimize by leaving open unless migrated
		// TODO does process ever close?
        
		outFile = new FileOutputStream(fileName, true);
		
		// Update position
		outFile.getChannel().position(position);
		
		// Perform write
		outFile.write(b);
		
		// Save position
		this.position = outFile.getChannel().position();
				
		// Close outFile so other processes can use it
		outFile.close();
	}
	
}
