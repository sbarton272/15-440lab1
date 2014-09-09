import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


public class TransactionalFileInputStream extends InputStream implements Serializable {

	private static final long serialVersionUID = -9067377820514406998L;
	private String mFileName;
	private long mPosition;

	public TransactionalFileInputStream(String fileName) throws FileNotFoundException {
		this.mFileName = fileName;
		
		FileInputStream inFile = new FileInputStream(mFileName);
		
		// Get start position
		try {
			this.mPosition = inFile.getChannel().position();
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
		
		FileInputStream inFile = new FileInputStream(mFileName);
		
		// Update position
		inFile.getChannel().position(mPosition);
		
		// Read
		int readInt = inFile.read();
		
		// Save position
		this.mPosition = inFile.getChannel().position();
		
		// Close inFile so other processes can use it
		try {
			inFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return readInt;
	}

}
