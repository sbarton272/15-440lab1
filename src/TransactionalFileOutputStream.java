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
	private String mFileName;
	private long mPosition;
	
	public TransactionalFileOutputStream(String fileName, boolean append) throws FileNotFoundException {
		this.mFileName = fileName;
		
		FileOutputStream outFile = new FileOutputStream(mFileName, append);
		
		// Get start position
		try {
			this.mPosition = outFile.getChannel().position();
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
        
		// Always append on these writes
		FileOutputStream outFile = new FileOutputStream(mFileName, true);
		
		// Update position
		outFile.getChannel().position(mPosition);
		
		// Perform write
		outFile.write(b);
		
		// Save position
		this.mPosition = outFile.getChannel().position();
				
		// Close outFile so other processes can use it
		outFile.close();
	}
	
}
