package example.repositories;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetProvider;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import com.github.database.rider.junit5.api.DBRider;
import example.domain.Pet;
import io.micronaut.test.annotation.MicronautTest;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
@DBRider
public class PetRepositoryTest {

    @Inject
    PetRepository petRepository;

    @Test
    @DataSet("pets.yml")
    void testRetrievePetAndOwner() {
        Pet lassie = petRepository.findByName("Lassie").orElse(null);
        assertNotNull(lassie);
        assertEquals("Lassie", lassie.getName());
        assertEquals("Fred", lassie.getOwner().getName());
    }

    @Test
    @DataSet(provider = PetsDataSetProvider.class)
    void testRetrievePetAndOwnerUsingDataSetProvider() {
        Pet lassie = petRepository.findByName("Lassie").orElse(null);
        assertNotNull(lassie);
        assertEquals("Lassie", lassie.getName());
        assertEquals("Fred", lassie.getOwner().getName());
    }



    public static class PetsDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
            DataSetBuilder builder = new DataSetBuilder();
            IDataSet dataSet = builder
                    .table("OWNER")
                    .row()
                        .column("ID", 1)
                        .column("AGE", 45)
                        .column("NAME", "Fred")
                    .row()
                        .column("ID", 2)
                        .column("AGE", 40)
                        .column("NAME", "Barney")
                    .table("PET")
                    .row()
                        .column("ID", 3)
                        .column("NAME", "Lassie")
                        .column("TYPE", 0)
                        .column("OWNER_ID", 1)
                    .row()
                        .column("ID", 4)
                        .column("NAME", "Baby Puss")
                        .column("TYPE", 1)
                        .column("OWNER_ID", 1)
                    .row()
                        .column("ID", 5)
                        .column("NAME", "Hoppy")
                        .column("TYPE", 0)
                        .column("OWNER_ID", 2).build();
            return dataSet;
        }
    }
}
