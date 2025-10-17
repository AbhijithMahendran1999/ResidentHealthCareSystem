package healthcaresystem.repo;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.people.Manager;

public class SerializationRepositoryTests {

    @Test
    public void saveThenLoad_restoresData() {
        SerializationRepository repo = new SerializationRepository();

        CareHome original = new CareHome();
        original.getStaff().add(new Manager("M1","Mgr","mgr"));

        String path = "test-carehome.dat";
        repo.saveState(original, path);

        CareHome loaded = repo.loadState(path);
        assertNotNull(loaded);
        assertEquals(1, loaded.getStaff().size());
        assertEquals("M1", loaded.getStaff().get(0).getId());

        new File(path).delete();
    }
}
