import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class Serializer implements Serializable {

	private static final long serialVersionUID = -5213253933056101103L;
	private String fileName;

	public Serializer(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * Serializes obj by saving it to a unique serialization file.
	 */
	public void serialize(Object obj) {
		try {
			// TODO add mutex?
			FileOutputStream serializedFile = new FileOutputStream(fileName);
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
			FileInputStream serializedFile = new FileInputStream(fileName);
	        ObjectInputStream objStream = new ObjectInputStream(serializedFile);
        
			// Deserialize
			Object obj = objStream.readObject();

			// Close the serialization files
			objStream.close();
	        serializedFile.close();
	        
	        return obj;
		
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}

