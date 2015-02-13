package demo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

import demo.SpringDataTest.SomeConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SomeConfig.class)
public class SpringDataTest {
 
	@Configuration
	@EnableMongoRepositories(considerNestedRepositories = true)
	public static class SomeConfig extends AbstractMongoConfiguration {
 
		@Override
		protected String getDatabaseName() {
			return "db-ref-test";
		}
 
		@Override
		public Mongo mongo() throws Exception {
			return new MongoClient();
		}
 
	}
 
	@Autowired MongoTemplate template;
	@Autowired SomePersonRepository personRepo;
 
	@Before
	public void setUp() {
		template.dropCollection(Person.class);
		template.dropCollection(Tag.class);
	}
	
	@Test
	public void so28504457() {
 
		Tag t1 = new Tag();
		t1.id = "t1";
		t1.tagName = "tag-1";
 
		Tag t2 = new Tag();
		t2.id = "t2";
		t2.tagName = "tag-2";
 
		template.save(t1);
		template.save(t2);
 
		Person p1 = new Person();
		p1.id = "p1";
		p1.tag = t1;
 
		Person p2 = new Person();
		p2.id = "p2";
		p2.tag = t2;
 
		Person p3 = new Person();
		p3.id = "p3";
		p3.tag = t2;
 
		template.save(p1);
		template.save(p2);
		template.save(p3);
 
		Page<Person> page_t1 = personRepo.findByTagId(t1.id, new PageRequest(0, 1));
		assertThat(page_t1.getTotalElements(), equalTo(1L));
		assertThat(page_t1.getNumberOfElements(), equalTo(1));
		assertThat(page_t1.getTotalPages(), equalTo(1));
 
		Page<Person> page_t2 = personRepo.findByTagId(t2.id, new PageRequest(0, 1));
		assertThat(page_t2.getTotalElements(), equalTo(2L));
		assertThat(page_t2.getNumberOfElements(), equalTo(1));
		assertThat(page_t2.getTotalPages(), equalTo(2));
	}
 
	static interface SomePersonRepository extends CrudRepository<Person, String> {
		Page<Person> findByTagId(String id, Pageable page);
	}
 
	@Document
	static class Person {
		@Id String id;
		String name;
		@org.springframework.data.mongodb.core.mapping.DBRef Tag tag;
	}
 
	@Document
	static class Tag {
		@Id String id;
		String tagName;
	}
 
}