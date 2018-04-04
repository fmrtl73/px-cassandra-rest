package px.rest.apis.cassandra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.SimpleUserTypeResolver;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.CassandraTemplate;
import com.datastax.driver.core.Session;

@Configuration
@EnableCassandraRepositories(basePackages = { "px.rest.apis.cassandra" })
public class CassandraConfig {
    private static final Log LOGGER = LogFactory.getLog(CassandraConfig.class);

    @Autowired
    private Environment environment;
    @Autowired Session session;
    protected String getKeyspaceName() {
        return environment.getProperty("cassandra.keyspace");
    }

    @Bean
    public CassandraClusterFactoryBean cluster() {
        final CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        cluster.setContactPoints(environment.getProperty("cassandra.contactpoints"));
        cluster.setPort(Integer.parseInt(environment.getProperty("cassandra.port")));
        LOGGER.info("Cluster created with contact points [" + environment.getProperty("cassandra.contactpoints") + "] " + "& port [" + Integer.parseInt(environment.getProperty("cassandra.port")) + "].");
        return cluster;
    }
    @Bean
    public CassandraMappingContext mappingContext() {

      BasicCassandraMappingContext mappingContext =  new BasicCassandraMappingContext();
      mappingContext.setUserTypeResolver(new SimpleUserTypeResolver(cluster().getObject(), getKeyspaceName()));

      return mappingContext;
    }
    @Bean
    public CassandraConverter converter() {
      return new MappingCassandraConverter(mappingContext());
    }
    @Bean
    public CassandraSessionFactoryBean session() throws Exception {

      CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
      session.setCluster(cluster().getObject());
      session.setKeyspaceName(getKeyspaceName());
      session.setConverter(converter());
      session.setSchemaAction(SchemaAction.CREATE_IF_NOT_EXISTS);

      return session;
    }
    @Bean
    public CassandraOperations cassandraTemplate() throws Exception {
      return new CassandraTemplate(session().getObject());
    }


}
