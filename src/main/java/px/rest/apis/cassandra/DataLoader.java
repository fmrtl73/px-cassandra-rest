package px.rest.apis.cassandra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.UUID;
import org.springframework.boot.*;
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.fluttercode.datafactory.impl.DataFactory;
import java.util.Random;

@Component
public class DataLoader implements CommandLineRunner {
  private static final Log LOGGER = LogFactory.getLog(DataLoader.class);
  @Autowired
  private Environment environment;
  @Autowired
  PersonRepository repository;

  private static String[] states = {"California,", "Alabama,", "Arkansas,", "Arizona,", "Alaska,", "Colorado,", "Connecticut,", "Delaware,", "Florida,", "Georgia,", "Hawaii,", "Idaho,", "Illinois,", "Indiana,", "Iowa,", "Kansas,", "Kentucky,", "Louisiana,", "Maine,", "Maryland,", "Massachusetts,", "Michigan,", "Minnesota,", "Mississippi,", "Missouri,", "Montana,", "Nebraska,", "Nevada,", "New Hampshire,", "New Jersey,", "New Mexico,", "New York,", "North Carolina,", "North Dakota,", "Ohio,", "Oklahoma,", "Oregon,", "Pennsylvania,", "Rhode Island,", "South Carolina,", "South Dakota,", "Tennessee,", "Texas,", "Utah,", "Vermont,", "Virginia,", "Washington,", "West Virginia,", "Wisconsin,", "Wyoming" };
  private Random random = new Random();

  public void run(String... s) {
		// Check for data loader config in environment
    String numberofrecords = environment.getProperty("dataloader.numberofrecords");
    if(numberofrecords != null){
      LOGGER.info("Dataloader creating " + numberofrecords + " records.");
      long t1 = System.currentTimeMillis();

      long actual = repository.count();
      Person p = new Person();
      Address a = new Address();
      DataFactory df = new DataFactory();
      while (actual < desired){
        p.setId(UUID.randomUUID().toString());
        p.setFirstName(df.getFirstName());
        p.setLastName(df.getLastName());
        a.setLine1(df.getAddress());
        a.setLine2(df.getAddressLine2());
        a.setCity(df.getCity());
        a.setState(states[random.nextInt(49)]);
        a.setZipcode(Integer.parseInt(df.getNumberText(5)));
        p.setAddress(a);
        repository.save(p);
        actual++;
      }
      long t2 = System.currentTimeMillis();
      LOGGER.info("Dataloader created " + (numberofrecords - actual) + " records in " + (t2 - t1)/1000 + " seconds.");
    }
	}

}
