### Examples
Simplest usage:

```java
Ngoy.renderString("hello {{ '{{ name }}' }}", Context.of("name", "world"), System.out);

// hello world
```

XML documents:
```java
public static class Person {
	public String name;
	public int age;

	public Person(String name, int age) {
		this.name = name;
		this.age = age;
	}
}

@Component(selector = "person", template = "<name>{{person.name}}</name><age>{{person.age}}</age>")
public static class PersonCmp {
	@Input
	public Person person;
}

@Component(selector = "test", template = "<doc [id]=\"docId\"><person *ngFor=\"let person of persons\" [person]=\"person\"></person></doc>")
@NgModule(declarations = { PersonCmp.class })
public static class Cmp {
	public List<Person> persons = asList(new Person("Peter", 22), new Person("Paul", 26), new Person("Mary", 24));

	public String docId = "28900";
}

```