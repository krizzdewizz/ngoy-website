
All ngoy features and notable differences to Angular are documented here.

# Components & Templates

A component is an ordinary Java class annotated with the `@Component` annotation:

```java
@Component(selector = "person", templateUrl = "person.component.html")
public class PersonComponent {
}
```

Whenever ngoy encounters an element that matches the CSS selector `selector`, the component 'takes over' the element. The component then controls the element itself (aka host element) and it's content (including attributes).

The component's template becomes the matching element's content.

Given this `person.component.html`:

```html
<h1>Person details</h1>
```

then this HTML:
```html
<html>
	<person></person>
</html>
```

will finally become:
```html
<html>
	<person>
		<h1>Person details</h1>
	</person>
</html>
```

A component can specify it's template either by a `templateUrl` or an inline `template`. The `templateUrl` is loaded with a call to `Class#getResourceAsStream()`.

Inline template:

```java
@Component(selector = "person", template = "<h1>Person details</h1>")
public class PersonComponent {
}
```

## Template Syntax & Data Binding 

A component has full control over the HTML by the use of data binding. There exists several possibilities:

### Interpolation

The text inside double curly braces is interpreted as an expression:

```html
<h1>Person details: {{ '{{ person.name }}' }}</h1>
``` 
At runtime, the expression is evaluated and it's return value is printed instead.

Note: ngoy uses the [Spring EL](https://docs.spring.io/spring/docs/4.3.10.RELEASE/spring-framework-reference/html/expressions.html) library for expression/evaluation. Please consult their docs for what's possible. It's syntax is almost conform to Angular's. 

The component designates the 'context' (aka `this`) of the evaluation. So `person` in `{{ '{{ person.name }}' }}` designates the `person` field of the `PersonComponent` instance:
```java
public class Person {
	public String name;
	public int age;
}

@Component(selector = "person", template = "<h1>Person details: {{ '{{ person.name }}' }}</h1>")
public class PersonComponent {
	public Person person;
}
```

Interpolation can occur also inside of attribute values:
```html
<h1 title="hello {{'{{person.name}}'}}"></h1>
```

### Attribute Binding

Beside interpolation, an attribute's value can also be evaluated at runtime with the use of attribute bindings; the preferred way.

Regular (`String`) attribute:

```html
<h1 title="hello"></h1>
```

The same with an attribute binding:
```html
<h1 [title]="person.name"></h1>
```

Just enclose the attribute's name with `[]` and it's value is interpreted as an expression.

Alternatively you can write also:
```html
<h1 [attr.title]="person.name"></h1>
```

The `[attr.]` syntax is relevant when using the `@HostBinding` annotation, see later in this document.

#### `class` attribute

The `class` attribute receives special treatment because it's value is effectively a class *list*.

A class can be added to the list by the use of the `[class.]` syntax:

```html
<h1 [class.vip]="isVip(person)"></h1>
```

Which means: add `vip` to the class list if the result of the expression `isVip(person)` evaluates to `true`.

Several class bindings can occur:
```html
<h1 [class.vip]="isVip(person)" [class.cool]="isCool(person)"></h1>
```

If both evaluate to `true`:
```html
<h1 class="vip cool"></h1>
```

An already existing class list will be merged into the final class list:
```html
<h1 class="person-title col-xs" [class.vip]="isVip(person)" [class.cool]="isCool(person)"></h1>
```

If both evaluate to `true`:
```html
<h1 class="person-title col-xs vip cool"></h1>
```

#### `ngClass` attribute

If several classes must be computed at once, it can be tedious to write them all with `[class.]` bindings. With the help of the `ngClass` attribute, you can add them all at once.

It expects a `java.util.Map<String, Boolean>` as the expression result:

```html
<h1 [ngClass]="personClasses"></h1>
```

```java
...
public class PersonComponent implements OnInit {
	...
	public Map<String, Boolean> personClasses = new HashMap<>();
	
	public void ngOnInit() {
		personClasses.put("vip", isVip(person));
		personClasses.put("cool", isCool(person));
	}
}
```

Or as an expression using Spring EL map literals:

```html
<h1 [ngClass]="{vip: isVip(person), cool: isCool(person)}"></h1>
```

#### `style` attribute

The `style` attribute receives special treatment because it's value is effectively a *map* from the style's name to it's value.

A style entry can be added by the use of the `[style.]` syntax:

```html
<h1 [style.background-color]="isVip(person) ? 'red' : 'inherit'"></h1>
```

Which means: add `background-color` to the styles attribute with the value being the result of the expression.

If `isVip(person)` evaluates to `true`:

```html
<h1 style="background-color:red"></h1>
```

You can append a unit to the style name, which is placed after the evaluated value:

```html
<h1 [style.width.px]="isVip(person) ? 240 : null"></h1>
```

If `isVip(person)` evaluates to `true`:

```html
<h1 style="width:240px"></h1>
```

Already existing styles will be merged into the final map.

As opposed to Angular, styles must not be camelCased.

Angular:
```
[style.backgroundColor]="'red'"
```

ngoy:
```
[style.background-color]="'red'"
```

#### `ngStyle` attribute

If several styles must be computed at once, it can be tedious to write them all with `[style.]` bindings. With the help of the `ngStyle` attribute, you can add them all at once.

It expects a `java.util.Map<String, String>` as the expression result:

```html
<h1 [ngStyle]="personStyles"></h1>
```

```java
...
public class PersonComponent implements OnInit {
	...
	public Map<String, Boolean> personStyles = new HashMap<>();
	
	public void ngOnInit() {
		personStyles.put("background-color", isVip(person) ? "red" : "inherit");
		...
	}
}
```

Or as an expression using Spring EL map literals:

```html
<h1 [ngStyle]="{'background-color': isVip(person) ? 'red' : 'inherit'}"></h1>
```

### @HostBinding

The attributes of a component's host element can be dynamically set using the `@HostBinding` annotation. It is set on a component's field (or getter).

```html
<html>
	<person></person> <!-- host element -->
</html>
```

```java
...
public class PersonComponent implements OnInit {
	@HostBinding("class.vip")
	public boolean isVip;
	
	public void ngOnInit() {
		isVip = isVip(person);
	}
}
```

The `@HostBinding`'s value is one of the bindings mentioned above (without the brackets): `attr.`, `class.`, `style.`. The type of the field  must correspond to the attribute kind:

- `"attr.title"` -> `String`. Value for the `title` attribute.
- `"class.vip"` -> `boolean`. `true` if class `vip` should be added to the class list.
- `"style.background-color"` -> `String`. Value for the style.
- `"ngText"` -> `String`. Value for the element's text content. This is an extension to Angular.

## Lifecycle hooks

Ngoy supports two lifecycle hooks. `OnInit` and `OnDestroy`:
```java
...
public class PersonComponent implements OnInit, OnDestroy {
	
	public void ngOnInit() {
	}

	public void ngOnDestroy() {
	}
}
```

`ngOnInit()` is called in the rendering phase each time the component is entered (when it's host element starts).

Typically, a component computes 'expensive' field values in `ngOnInit()`, which are used several times in the template.

Note: A new component instance is created each time it is entered. Initialization can happen in the constructor as long as no injected dependecies are used. At construction time, fields are not injected yet, but in `ngOnInit()`, they are.

`ngOnDestroy()` is called in the rendering phase each time the component is leaved (when it's host element ends).

## Component interaction

You can pass data from a parent component to a child component with an input binding:

```java
@Component(...)
public class PersonComponent {
	@Input
	public Person thePerson;
}

@Component(selector = "", template = "<person [thePerson]=\"peter\"></person>")
@NgModule(declarations = { PersonComponent.class })
public class AppComponent {
	public Person peter = new Person("Peter");
}
```

`PersonComponent` declares `thePerson` field as an `@Input` binding.

The input parameter is passed to the `PersonComponent` instance with the use of an attribute binding `[thePerson]`:

```html
<person [thePerson]="peter"></person>
``` 

`[]` designates an expression. its value, `peter`, is assigned to the component's input `thePerson`.

The `@Input` can optionally be renamed:

```java
@Component(...)
public class PersonComponent {
	@Input("personas")
	public Person thePerson;
}
```

```html
<person [personas]="peter"></person>
```

The `@Input` annotation can also be specified on a 'setter':

```java
@Component(...)
public class PersonComponent {
	private Person _person;
	
	@Input("personas")
	public void setThePerson(Person person) {
		_person = person;
	}
}
```

## Component styles

A component can specify inline `style`s or resources identified by `styleUrls`:

```java
@Component(selector = "person", styles = { "h1 { font-weight: normal; }" }, styleUrls = {"person.component.css"} )
public class PersonComponent {
}
```

Upon compilation of the template, all styles from all declared components are copied into a single `<style>` element in the app's HTML.

### Auto Prefixer

By default, styles are copied 'as-is'; they are global and not scoped to the component (no shadow DOM).

You can, however, opt-in to auto-prefixing:

```java
Ngoy.app(AppComponent.class)
	.prefixCss(true) // enable auto-prefixer 
	.build();
```

All style rules are prefixed with the component's selector.

In the above example, with prefix enabled, the style

```css
h1 { font-weight: normal; }
```

would be translated to:

```css
person h1 { font-weight: normal; }
``` 
## Attribute directives

Directives are annotated with the `@Directive` annotation:

```java
@Directive(selector = "[appHighlight]")
public class HighlightDirective {
	@HostBinding("style.background-color")
	public String getBgColor() {
	 	String color = ...;
		return color;
	}
}
```

A directive is aka a 'component without a template'. All rules of a component apply also to a directive, except that the host element's content is not replaced by any template. So a directive serves merely to change the attributes of the host element with the use of `@HostBinding`s, or as a compile-time hook (see below). 

## Control Structures

Control structures allows you to alter a host element's subtree by adding, removing or manipulating elements.

### *ngIf

`*ngIf` allows you to conditionally include an element in the subtree.

```html
<h1>Person details <span *ngIf="isVip(person)">VIP!</span></h1>
```

The value of the `*ngIf` attribute designates an expression. The element is printed only when the expression evaluates to `true`. 

You can optionally add an `else` clause:

```html
<h1>Person details <span *ngIf="isVip(person); else regularPerson">VIP!</span></h1>

<ng-template #regularPerson>
	<span>No hor d'oeuvres for this guy<span>
</ng-template>
```

### [ngSwitch]

`[ngSwitch]` allows you to switch on a once evaluated expression and 'jump' to one or more `*ngSwitchCase` labels:

```html
<h1 [ngSwitch]="emotion">
    <div *ngSwitchCase="'happy'">🙂</div>
    <div *ngSwitchCase="'sad'">🙁</div>
    <div *ngSwitchDefault>😐</div>
</h1>
```

The first `*ngSwitchCase` matching the expression `emotion` will be printed. If none of the cases match, `*ngSwitchDefault` is printed.

A `[ngSwitch]` must have at least one `*ngSwitchCase`.

### *ngFor

`*ngFor` allows you to repeat an element for every item in an `Iterator`.

```html
<div *ngFor="let person of persons">{{ '{{ person.name }}' }}</div>
```

`persons` may be an instance of `Iterable` or an array. `person` designates the current item of the iteration, 
which is available as a local variable inside the `<div>`.

This is the same as Java's enhanced for loop:
```java
for (Person person : persons) {
}
```

Optionally, you can declare local aliases for the built-in iteration variables, delimited with `;`

```html
<div *ngFor="let person of persons; index as i; first  as f">
	{{ '{{ person.name }}' }}, person index: {{ '{{ i }}' }}
</div>
```

The built-in variables are:
- `index`: The current iteration index starting at `0`
- `first`: `true` if this is the first iteration
- `last`: `true` if this is the last iteration
- `even`: `true` if `index` is even
- `odd`: `true` if `index` is odd

## &lt;ng-content&gt;

A component may include the host element's content into it's own template. This is known as 'content projection'.

The &lt;ng-content&gt; element inside the component's template is replaced by the contents of the host element:

Given this `PersonComponent` template:
```html
<h1>Person details: {{ '{{ person.name }}' }}
	<ng-content></ng-content> <!-- where the content is projected to -->
</h1>
```

then this HTML:
```html
<html>
	<person>
		<!-- everthing inside <person> is projected -->
		<div>More person details</div>
	</person>
</html>
```

will finally become:
```html
<html>
	<person>
		<h1>Person details: Peter
			<div>More person details</div> <!-- projection done -->
		</h1>
	</person>
</html>
```

A &lt;ng-content&gt; element may have a CSS `select` attribute. When this attribute is given, 
the first element inside the host element matching the selector will be projected:

Given this template:
```html
<h1>Person details: {{ '{{ person.name }}' }}
	<!-- project element with moreDetails attribute -->
	<ng-content select="[moreDetails]"></ng-content>
	<br>
	<!-- project element with personLinks attribute -->
	<ng-content select="[personLinks]"></ng-content>
</h1>
```

then this HTML:
```html
<html>
	<person>
		<div moreDetails>More person details</div>
		<div personLinks>Links</div>
	</person>
</html>
```

will finally become:
```html
<html>
	<person>
		<h1>Person details: Peter
			<div moreDetails>More person details</div>
			<br>
			<div personLinks>Links</div>
		</h1>
	</person>
</html>
```

## &lt;ng-container&gt;

&lt;ng-container&gt; behaves like any other element except that it is never printed but only it's content.

Using &lt;ng-container&gt;, you can often spare an element that would otherwise be used only for grouping:

Instead of:
```html
<div *ngFor="let person of persons; index as i">    <!-- we don't need that div -->  
	<span>name: {{ '{{ person.name }}' }}</span>
	<span>index: {{ '{{ i }}' }}</span>
</div>
```

You can write:
```html
<ng-container *ngFor="let person of persons; index as i">    <!-- will disappear -->  
	<span>name: {{ '{{ person.name }}' }}</span>
	<span>index: {{ '{{ i }}' }}</span>
</ng-container>
```

that will produce:
```html
<span>name: Peter</span>
<span>index: 0</span>
<span>name: Paul</span>
<span>index: 1</span>
<span>name: Mary</span>
<span>index: 2</span>
```

## Compile-time hook

When the template is compiled, a component/directive has the chance to alter the template's subtree before the compiler sees it. At this point, you can i.e. insert static content, re-write or expand a template based on some attributes etc.

A component/directive may implement the `OnCompile` interface:

```java
@Component(...)
public class PersonComponent implements OnCompile {
	public void ngOnCompile(Jerry el, String componentClass) {
		// changes to the DOM element 'el', it's attributes 
		// and content will be picked up by the compiler
	}
}
```

`ngOnCompile()` is called only *once* in the compile phase and not in the rendering phase.

See the [MarkdownComponent](https://github.com/krizzdewizz/ngoy-website/blob/master/src/main/java/ngoyweb/app/components/markdown/MarkdownComponent.java) for an example. It appends the HTML converted from CommonMark. You can write markdown inside the component or reference a `.md` resource.  

## Pipes

A pipe takes data as input and transforms it to a desired output. You can pipe the result of an expression to the desired pipe:

```html
<h1>hello: {{' {{ person.name' }} | {{'uppercase }}'}}</h1>
```

A pipe may have parameters delimited by colon `:`
```html
{{ '{{ T(LocalDateTime).of(2018, 10, 28, 12, 44) ' }} | {{ " date:'MMMM YYYY' }} "}}
```

Prints
```
October 2018
```

These are the built-in pipes:
- [uppercase](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/common/UpperCasePipe.java)
- [lowercase](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/common/LowerCasePipe.java)
- [capitalize](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/common/CapitalizePipe.java)
- [date](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/common/DatePipe.java)
- [translate](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/translate/TranslatePipe.java) (see TranslateModule later in this document)


Of course you can write your own:

Annotate a Java class with `@Pipe` and implement the `PipeTransform` interface.

The original [uppercase](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/common/UpperCasePipe.java) source:

```java
@Pipe("uppercase")
public class UpperCasePipe implements PipeTransform {

	@Inject
	public LocaleProvider localeProvider;

	@Override
	public Object transform(@Nullable Object obj, Object... params) {
		if (obj == null) {
			return null;
		}

		return obj.toString().toUpperCase(localeProvider.getLocale());
	}
}
```

Note: The pipe syntax `|` is not part of the Spring EL grammar. ngoy parses it 'manually' with regex. It should be fine in most cases, but in complex expressions (which should be avoided anyway) it could lead to a parse error. In such a situation, you can always resort to the function call syntax, prefixing the pipe with `$`:

```java
'hello' | uppercase
```

is the same as:
```java
$uppercase('hello')
```

# Modules

All components, directives, pipes (declarations) and providers/services must be registered within ngoy.

Given the person example from above:
```java
@Component(...)
public class PersonComponent {
}

@Component(selector = "", template = "<person></person>")
@NgModule(declarations = { PersonComponent.class }) // make PersonComponent known to ngoy
public class AppComponent {
	public Person peter = new Person("Peter");
}
```

Without the `NgModule` annotation, the `PersonComponent` would be unknown to ngoy and the `<person>` element would not match any selector and it would just stay there as it is. 

A class annotated with `NgModule` serves as a container for declarations and providers. A logical unit to group a feature together. May a component needs a provider to work correctly. So you would group the two into a module, so that client of your component can import just the module instead of all parts separately. And you could add more stuff to the module afterwards without breaking the clients. 

 A `NgModule` has three attributes:

- `declarations`: make components, directives and pipes known to ngoy
- `providers`: make providers/services known to ngoy
- `imports`: imports other `NgModule`s, making them known to ngoy recursively

Note: Unlike other module systems, there exists no enforced boundaries between modules. At the end, everything is stuffed into a single map. Everything can be reached from anywhere.

A runtime exception is thrown whenever more than one component matches an element.

A provider can also be specified on a `@Component`:
```java
@Component(selector = "person", providers = { PersonService.class }) 
public class PersonComponent {
}
```

Component providers are **never** local to a component but **always** global.

A `@Component` can also be a `NgModule`.

## Dynamic Modules

A dynamic module's declarations/providers are computed at runtime instead of 'declaration time' with annotations.

See [ModuleWithProviders.java](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/core/ModuleWithProviders.java) and 
[RouterModule.java](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/router/RouterModule.java) for an example usage.


## Package Modules

It can be tedious to add every single thing to a module. And in an isolated app, there's no risk that there would be collisions.

In addition to organize declarations with modules, ngoy can scan the class path for them:
  
```java
ngoy.app(AppComponent.class)

	// load all declarations/providers inside org.myapp package
	.modules("org.myapp")                     

	// load all declarations/providers inside the app's package
	.modules(AppComponent.class.getPackage())

	.build();
```

Note: Services must be annotated with `@Injectable` so that the class scanner picks it up as a provider:

```java
@Injectable
public class PersonService {
	public Person[] getPersons() {
		return ...;
	}
}
```

# Dependency Injection

Any class can be registered as a service/provider within ngoy and be injected into declarations and other services.

ngoy supports field injection and constructor injection.

```java
public class PersonService {
	public Person[] getPersons() {
		return ...;
	}
}

public class WeatherService {
}

@Component(selector = "person", providers = { PersonService.class }) 
public class PersonComponent implements OnInit {

	// constructor injection
	public PersonComponent(WeatherService weatherService) {
		// do things with weatherService
	}

	// field injection
	@Inject
	public PersonService personService;
	
	public void ngOnInit() {
		// do things with personService
	}
}
```

Fields must be annotated with `@Inject`. Constructor parameters must not be annotated.

A runtime exception is thrown when there's no provider for a service.

A dependency may be declared `@Optional` in which case no exception is thrown.

Note: a service must be annotated with `@Injectable` when it should be picked up by 'Package Modules', see above.

There are 3 kinds of providers:
- `class A` -> `class A`: Wherever `class A` is requested, inject an instance of `class A` (example above)
-  `class A` -> `useClass B`: Wherever `class A` is requested (could be an interface), inject an instance of `class B`, which must be assignable to `class A`. Used to override default behaviour.
-  `class A` -> `useValue V`: Wherever `class A` is requested (could be an interface), inject the instance `V`, which must be an instance of `class A`. Only available at runtime, not with annotations.

## Providers at runtime

Providers can be specified at runtime, especially the `useValue` variant, which is used to inject external dependencies into ngoy.  

```java
ngoy.app(AppComponent.class)
	.providers(Provider.useValue(MyService.class, service)
	.build();
```

See also [Provider.java](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/core/Provider.java)

## External DI Systems

By implementing the `Injector` interface, one can provide dependencies from other DI systems such as Spring Boot to ngoy.

See the [BeanInjector.java](https://github.com/krizzdewizz/ngoy-tour-of-heroes/blob/master/src/main/java/toh/app/BeanInjector.java) for an example.

```java
@Controller
@RequestMapping("/**")
public class Main implements InitializingBean {

	...

	@Autowired
	private BeanInjector beanInjector;

	private void createApp() {

		ngoy = Ngoy.app(AppComponent.class)
				...
				.injectors(beanInjector) // make Spring beans known to ngoy
				.build();
	}
}
```

# Routing

Basic routing functionality can be found in the `RouterModule`. See the [router](https://github.com/krizzdewizz/ngoy-examples/tree/master/src/main/java/ngoyexamples/routing) example in the [ngoy-examples](https://github.com/krizzdewizz/ngoy-examples) collection.


# Forms
 
to be done.

# CLI

ngoy has a built in CLI with which you can
- render templates and expressions from the command line
- quickly generate source code artifacts for new components, pipes etc. 

The CLI main class is `ngoy.Ngoy`. When you started with [ngoy-starter-web](https://github.com/krizzdewizz/ngoy-starter-web), 
the ngoy binaries and a shell script `ngoy` are ready to use:

```
$ ./ngoy

usage: ngoy [g|gen|generate] [options] template

If generate is given, the rest of the arguments are passed over to
ngoy-gen.

Options:
 -e,--expression              treat template as an expression
 -f,--file                    read template from file instead of command
                              line
 -h,--help                    display this help
 -in,--input                  run template for each line read from stdin
                              (use $ variable to access line within
                              template)
 -v,--variable <name=value>   add a variable to the execution context
    --version                 print version information
```

Evaluate an expression:

```
$ ./ngoy -e "T(LocalDateTime).now()"

2018-12-03T00:07:33.187
```

## Generate Source Artifactes

Using the `ngoy gen` command you can generate source code artifacts:

```
$ ./ngoy gen

usage: ngoy-gen [options] component|directive|pipe|module|service name

name should be lower-case-separated-with-dashes.

Examples:
  ngoy-gen component person-list
  ngoy-gen -p com.example pipe quantity-format

Shortcuts works as well:
  ngoy-gen p my-pipe

Options:
 -h,--help            display this help
 -p,--package <arg>   package prefix for the generated artifact. Default
                      is 'ngoygen'.
 -t,--target <arg>    target folder for the generated artifacts. A default
                      is searched in the following order:
                      [./src/main/java, ./src, .]
    --version         print version information

```

Generate a component:
```
$ ./ngoy g c person

generating artifact '.\src\main\java\ngoygen\person\PersonComponent.java'...
generating artifact '.\src\main\java\ngoygen\person\person.component.html'...
generating artifact '.\src\main\java\ngoygen\person\person.component.css'...
```

# Spring EL

ngoy uses the [Spring EL](https://docs.spring.io/spring/docs/4.3.10.RELEASE/spring-framework-reference/html/expressions.html) library for expression/evaluation.

Here are some notable differences to the Angular syntax:

Truthy/falsy values do not exists. Expressions like this won't work:

```html
<!-- runtime error: person is not a boolean -->
<h1 *ngIf="person">{{ '{{ person.name }}' }}</h1> 

<!-- runtime error: length is not a boolean -->
<h1 *ngIf="persons.length"></h1>
```

You have to write:
```html
<h1 *ngIf="person != null">{{ '{{ person.name }}' }}</h1>

<h1 *ngIf="persons.length > 0"></h1>
```

`===` does not exist. Use `==` instead.

List and Map literals are nice:

```
// list
{1, 2, 3}

// map
{a: 1, b: 2, c: 3}
```

I.e.
```html
<ng-container *ngFor="let x of {1, 2, 3}">{{ '{{ x }}' }}</ng-container>
```

will print:
```
123
```


