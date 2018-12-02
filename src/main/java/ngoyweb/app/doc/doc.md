
All ngoy features and notable differences to Angular are documented here.

## Components and Templates

A component is an ordinary Java class annotated with the `@Component` annotation:

```java
@Component(selector = "person", templateUrl = "person.component.html")
public class PersonComponent {
}
```

Whenever ngoy encounters an element that matches the CSS selector `selector`, the component 'takes over' the element. The component then controls the element itself (aka host element) and it's content (including attributes).

The components template becomes the matching element's content.

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

### Template Syntax & Data Binding 

A component has full control over the HTML by the use of data binding. There exists several possibilities:

#### Interpolation

The text inside double curly braces is interpreted as an expression:

```html
<h1>Person details: {{ '{{ person.name }}' }}</h1>
``` 
At runtime, the expression is evaluated and it's return value is inserted instead.

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

#### Attribute Binding

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

##### `class` attribute

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

##### `ngClass` attribute

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

##### `style` attribute

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

##### `ngStyle` attribute

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

#### @HostBinding

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
- `"text"` -> `String`. Value for the element's text content. This is an extension to Angular.

### Lifecycle hooks

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

Note: A new component instance is created each time it is entered. Initialization can happen in the constructor, as long as no injected dependecies are used. At construction time, fields are not injected yet, but in `ngOnInit()`, they are.

`ngOnDestroy()` is called in the rendering phase each time the component is leaved (when it's host element ends).

### Component interaction

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

### Component styles

A component can specify inline `style`s or resources identified by `styleUrls`:

```java
@Component(selector = "person", styles = { "h1 { font-weight: normal; }" }, styleUrls = {"person.component.css"} )
public class PersonComponent {
}
```

Upon compilation of the template, all styles from all declared components are copied into a single `<style>` element in the app's HTML.

#### Auto Prefixer

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
### Attribute directives

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

A directive is aka a 'components without a template'. All rules of a component apply to a directive, except that the host element's content is not replaced by any template. So a directive serves merely to change the attributes of the host element with the use of `@HostBinding`s, or as a compile-time hook (see below). 

### Components/directives compile-time hook

When the template is compiled, a component/directive has the chance to alter the template's subtree before the compiler sees it. At this point, you can i.e. insert static content, re-write or expand a template based on some attributes etc.

A component/directive may implement the `OnCompile` interface:

```java
@Component(...)
public class PersonComponent implements OnCompile {
	public void ngOnCompile(Jerry el, String componentClass) {
		// changes to the DOM element 'el', it's attributes and content will be picked up by the compiler
	}
}
```

`ngOnCompile()` is called only *once* in the compile phase and not in the rendering phase.

See the [MarkdownComponent](https://github.com/krizzdewizz/ngoy-website/blob/master/src/main/java/ngoyweb/app/components/MarkdownComponent.java) for an example. It appends the HTML converted from CommonMark. You can write markdown inside the component or reference a `.md` resource.  

### Pipes

A pipe takes in data as input and transforms it to a desired output. You can pipe the result of an expression to the desired pipe:

```html
<h1>hello: {{' {{ person.name' }} | {{'uppercase }}'}}</h1>
```

These are the built-in pipes:
- [uppercase](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/common/UpperCasePipe.java)
- [lowercase](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/common/LowerCasePipe.java)
- [capitalize](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/common/CapitalizePipe.java)
- [date](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/common/DatePipe.java)
- [translate](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/translate/TranslatePipe.java) (see TranslateModule later in this document)

You can of course write your own:

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

Note: The pipe syntax `|` is not part of the Spring EL grammar. ngoy parses it manually with regex. It should be fine in most cases, but in complex expressions (which should be avoided anyway) it could lead to a parse error. In such a situation, you can always ressort to the function call syntax, prefixing the pipe with `$`:

```java
'hello' | uppercase
```

is the same as:
```java
$uppercase('hello')
```

## Modules

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

### Dynamic Modules

A dynamic module's declarations/providers are computed at runtime instead of 'declaration time' with annotations.

See [ModuleWithProviders.java](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/core/ModuleWithProviders.java) and 
[RouterModule.java](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/router/RouterModule.java) for an example usage.


### Package Modules

It can be tedious to add every single thing to a module. And in an isolated app, there's no risk that there would be collisions.

In addition to organize declarations with modules, ngoy can scan the class path for them:
  
```java
ngoy.app(AppComponent.class)
	.modules("org.myapp")                     // load all declarations/providers inside org.myapp package
	.modules(AppComponent.class.getPackage()) // load all declarations/providers inside the app's package
	.build();
```

## Dependency Injection

Any class can be registered as a service/provider within ngoy and be injected into declarations and other services.

```java
public class PersonService {
	public Person[] getPersons() {
		return ...;
	}
}

@Component(selector = "person", providers = { PersonService.class }) 
public class PersonComponent implements OnInit {
	@Inject
	public PersonService personService;
	
	public void ngOnInit() {
		// do things with personService
	}
}
```

There are 3 kinds of providers:
- `class A` -> `class A`: Wherever `class A` is requested, inject an instance of `class A` (example above)
-  `class A` -> `useClass B`: Wherever `class A` is requested, inject an instance of `class B`. Used to override default behaviour, see below.
-  `class A` -> `useValue B`: Wherever `class A` is requested, inject the instance `B`. Only available at runtime, not with annotations.

### Providers at runtime

Providers can be specified at runtime, especially the `useValue` variant, which is used to inject external dependencies into ngoy.  

```java
ngoy.app(AppComponent.class)
	.providers(Provider.useValue(MyService.class, service)
	.build();
```

See also [Provider.java](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/core/Provider.java)

### External DI Systems

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

## Routing

Basic routing functionality can be found in the `RouterModule`. See the [router](https://github.com/krizzdewizz/ngoy-examples/tree/master/src/main/java/ngoyexamples/routing) example in the [ngoy-examples](https://github.com/krizzdewizz/ngoy-examples) collection.


## Forms

to be done
