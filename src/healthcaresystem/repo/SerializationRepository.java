package healthcaresystem.repo;

import java.io.*;
import healthcaresystem.exception.PersistenceException;
import healthcaresystem.model.facility.CareHome;

// Handles saving and loading CareHome data to disk
public class SerializationRepository {

    // Saves the CareHome object to a file
    public void saveState(CareHome careHome, String file) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(careHome);
        } catch (IOException e) {
            throw new PersistenceException("Save failed: " + e.getMessage(), e);
        }
    }

    // Loads the CareHome object from file, or creates a new one if file not found
    public CareHome loadState(String file) {
        File f = new File(file);
        if (!f.exists()) return new CareHome();

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (CareHome) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenceException("Load failed: " + e.getMessage(), e);
        }
    }
}
