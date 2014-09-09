import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {

	private static final String PATH = "tmp";
	private static final String FILE_EXT = ".ser";
	private String mFileName;

	public Serializer(String name) {
		mFileName = PATH + java.io.File.separator + name + FILE_EXT;
	}
	
	/**
	 * Serializes obj by saving it to a unique serialization file.
	 */
	public void serialize(Object obj) {
		try {
			// TODO add mutex?
			FileOutputStream serializedFile = new FileOutputStream(mFileName);
			ObjectOutputStream objStream = new ObjectOutputStream(serializedFile);
			
			// Serialize
			objStream.writeObject(obj);
			
	        // Close the serialization files
			objStream.close();
			serializedFile.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deserializes the obj by taking it from the serialized location
	 */
	public Object deserialize() {
		try {
			FileInputStream serializedFile = new FileInputStream(mFileName);
	        ObjectInputStream objStream = new ObjectInputStream(serializedFile);
        
			// Deserialize
			Object obj = objStream.readObject();

			// Close the serialization files
			objStream.close();
	        serializedFile.close();
	        
	        // TODO delete the file
	        
	        return obj;
		
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}

