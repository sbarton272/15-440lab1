import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


public class TransactionalFileInputStream extends InputStream implements Serializable {

	private static final long serialVersionUID = -9067377820514406998L;
	private String fileName;
	private long position;
	private FileInputStream inFile;

	public TransactionalFileInputStream(String fileName) throws FileNotFoundException {
		this.fileName = fileName;
		
		inFile = new FileInputStream(fileName);
		
		// Get start position
		try {
			this.position = inFile.getChannel().position();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
				
		// Close file so other processes can use it
		try {
			inFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int read() throws IOException {
		
		inFile = new FileInputStream(fileName);
		
		// Update position
		inFile.getChannel().position(position);
		
		// Read
		int readInt = inFile.read();
		
		// Save position
		this.position = inFile.getChannel().position();
		
		// Close inFile so other processes can use it
		try {
			inFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return readInt;
	}

}
