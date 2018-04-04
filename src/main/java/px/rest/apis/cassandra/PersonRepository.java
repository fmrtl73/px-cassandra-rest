package px.rest.apis.cassandra;


import java.util.List;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "people", path = "people")
public interface PersonRepository extends CrudRepository<Person, Long> {

		/**
		 * Sample method annotated with {@link Query}. This method executes the CQL from the {@link Query} value.
		 *
		 * @param id
		 * @return
		 */
		@Query("SELECT * from people where id in(?0)")
		Person findPersonByIdIn(String id);
		/**
		 * Derived query method. This query corresponds with {@code SELECT * FROM users WHERE uname = ?0}.
		 * {@link User#username} is not part of the primary so it requires a secondary index.
		 *
		 * @param lastName
		 * @return
		 */
		List<Person> findByLastName(String lastName);
}
