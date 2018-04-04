package px.rest.apis.cassandra;

import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.datastax.driver.core.DataType.Name;
import com.datastax.driver.core.UDTValue;


@Table(value = "people")
public class Person {

	@PrimaryKey
  private String id = UUID.randomUUID().toString();

	@Column("firstName") private String firstName;
	@Column("lastName") private String lastName;
	Address address;

	@CassandraType(type = Name.UDT, userTypeName = "address")

	protected Person() {}

	public Person(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
  }

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id=id;
	}

  public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

  @Override
  public String toString() {
      return String.format(
              "Person[id=%d, firstName='%s', lastName='%s']",
              id, firstName, lastName);
  }

}
