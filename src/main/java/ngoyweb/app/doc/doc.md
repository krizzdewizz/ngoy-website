
All ngoy features and notable differences to Angular are documented here.

# Running ngoy

The simplest way to use ngoy's template engine is via the static `renderString()` or `renderTemplate()` methods:

```java
Ngoy.renderString("hello {{ '{{ name }}' }}", Context.of("name", "world"), System.out);

// hello world
```

The first parameter is the template. Text inside the double curly braces is treated as a Java expression (see <a href="#template-syntax--data-binding">Template Syntax & Data Binding</a>).

The second parameter is the evaluation 'context' or `this` inside the template. Context can be an object and/or a bunch of variables.
In the example the variable `"name"` is assigned the value `"world"`.

The third parameter is the `OutputStream` to write to.

`renderTemplate()` works exactly the same, except that the template is read from a resource via `Class#getResourceAsStream()`.

For a simple template this might be already enough.

If you want more like reusable components, custom functions (pipes), services... maybe a router... you'd start from ngoy's static `app()` method.

It expects the root component class as the first parameter:

```java
@Component(selector = "", template = "hello {{ '{{ name '}} | {{ 'uppercase }}' }}")
public class AppComponent {
    public String name = "world";
}

public static void main(String[] args) {
	// build once
    Ngoy<AppComponent> ngoy = Ngoy.app(AppComponent.class)
            .build();

	// render many times
	ngoy.render(System.out);
	// ngoy.render(System.out);
	// ngoy.render(System.out);
	// ngoy.render(System.out);
}

// hello WORLD
```

`build()` compiles the template to Java byte code on the fly and returns an Ngoy instance which then renders the content to the given `OutputStream`.

It's supposed to build once, render many times.

Note: In Angular, you normally have an `index.html` which 'bootstraps' the root app. In ngoy, the root app *is* the page.

# Components & Templates

A component is an ordinary Java class annotated with the `@Component` annotation:

```java
@Component(selector = "person", templateUrl = "person.component.html")
public class PersonComponent {
}
```

Whenever ngoy encounters an HTML element that matches the CSS selector `selector`, the component 'takes over' the element. The component then controls the element itself (aka host element) and it's content (including attributes).

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

The text inside double curly braces is interpreted as a Java expression:

```html
<h1>Person details: {{ '{{ person.name }}' }}</h1>
```
At runtime, the expression's return value is rendered.

Note: Expressions are Java code with some script-friendly extensions. See <a href="#java-expressions">Java Expressions</a>.

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
<h1 title="hello {{'{{ person.name }}'}}"></h1>
```

### Attribute Binding

Beside interpolation, an attribute's value can also be evaluated at runtime with the use of attribute bindings; the preferred way.

Regular attribute:

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

The `[attr.]` syntax is relevant when using the <a href="#hostbinding">`@HostBinding`</a> annotation.

In Angular, there exists property bindings and attribute bindings. In ngoy, there exists only attribute bindings.

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

	public void onInit() {
		personClasses.put("vip", isVip(person));
		personClasses.put("cool", isCool(person));
	}
}
```

Or as an expression using the `map` pipe:

```html
<h1 [ngClass]="$map('vip', isVip(person), 'cool', isCool(person))"></h1>
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

	public void onInit() {
		personStyles.put("background-color", isVip(person) ? "red" : "inherit");
		...
	}
}
```

Or as an expression using the <a href="#built-in-functions">built-in</a> Map function:

```html
<h1 [ngStyle]="$map('background-color', isVip(person) ? 'red' : 'inherit')"></h1>
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

	public void onInit() {
		isVip = calculateVipStatus(person);
	}
}
```

The `@HostBinding`'s value is one of the bindings mentioned above (without the brackets): `attr.`, `class.`, `style.`. The type of the field  must correspond to the attribute kind:

- `"attr.title"` -> `String`. Value for the `title` attribute.
- `"class.vip"` -> `boolean`. `true` if class `vip` should be added to the class list.
- `"style.background-color"` -> `String`. Value for the style.
- `"ngText"` -> `String`. Value for the element's text content. This is an extension to Angular.

## Lifecycle hooks

Ngoy supports these lifecycle hooks: `OnInit` and `OnDestroy`:
```java
...
public class PersonComponent implements OnInit, OnDestroy {

	public void onInit() {
	}

	public void onDestroy() {
	}
}
```

`onInit()` is called in the rendering phase each time the component, resp. it's host element starts.

Typically, a component computes field values in `onInit()`, may using an injected service.

Note: A new component instance is created each time it is entered.
Initialization can happen in the constructor as long as no injected dependecies are used.
At construction time, fields are not injected yet, but in `onInit()`, they are.

`onDestroy()` is called in the rendering phase each time the component, resp. it's host element ends.

## Component Interaction

You can pass data from a parent component to a child component with an input binding:

```java
@Component(selector="person", ...)
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
@Component(selector="person", ...)
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
@Component(selector="person", ...)
public class PersonComponent {
	private Person _person;

	@Input("personas")
	public void setThePerson(Person person) {
		_person = person;
	}
}
```

An `@Input` field must be public, non-static and non-final.

## Component Styles

A component can specify inline `style`s or resources identified by `styleUrls`:

```java
@Component(
	selector = "person",
	styleUrls = { "person.component.css" },
	styles = { "h1 { font-weight: normal; }" })
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
## Attribute Directives

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

A directive is aka a 'component without a template'. All rules of a component apply also to a directive, except that the host element's content is not replaced by any template. So a directive serves merely to change the attributes of the host element with the use of `@HostBinding`s, or as a <a href="#compile-time-hook">compile-time hook</a>.

## Control Structures

Control structures allows you to alter a host element's subtree by adding, removing or manipulating elements.

### *ngIf

`*ngIf` allows you to conditionally include an element in the subtree.

```html
<h1>Person details <span *ngIf="isVip(person)">VIP!</span></h1>
```

The value of the `*ngIf` attribute designates an expression. The element is rendered only when the expression evaluates to `true`.

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

The first `*ngSwitchCase` matching the expression `emotion` will be rendered. If none of the cases match, `*ngSwitchDefault` is rendered.

A `[ngSwitch]` must have at least one `*ngSwitchCase`.

`[ngSwitch]` can switch on any type of value.

### *ngFor

`*ngFor` allows you to repeat an element for every item in an `Iterable`, `Stream` or array.

```html
<div *ngFor="let person of persons">{{ '{{ person.name }}' }}</div>
```
`persons` may be an instance of `Iterable`, `Stream` or array. `person` designates the current item of the iteration,
which is available as a local variable inside the `<div>`.

Several flavors are supported to bring Angular and Java developers together. The general syntax is:
```
let|var|Type item :|of iterable-or-array
```

So these are all the same. The type of the item is inferred at compile time:
```java
let person of persons
var person of persons
let person : persons
var person : persons
```

ngoy's type inference algorithm should cover most cases. If the type cannot be determined, `java.lang.Object` is used.
If this is insufficent, you can specify the item's type. The type must be full qualified if not in `java.lang`.
```html
<div *ngFor="com.example.Person person of persons">{{'{{ person.name }}'}}</div>
```

Optionally, you can declare local aliases for the built-in iteration variables, delimited with `;`

```html
<div *ngFor="let person of persons; index as i; first as f">
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

&lt;ng-container&gt; behaves like any other element except that it is never rendered but only it's content.

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

## Compile-time Hook

When the template is compiled, a component/directive has the chance to alter the template's subtree before the compiler sees it. At this point, you can i.e. insert static content, re-write or expand a template based on some attributes etc.

A component/directive may implement the `OnCompile` interface:

```java
@Component(...)
public class PersonComponent implements OnCompile {
	public void onCompile(Jerry el, String componentClass) {
		// changes to the DOM element 'el', it's attributes
		// and content will be picked up by the compiler
	}
}
```

`onCompile()` is called only *once* in the compile phase and not in the rendering phase.

See the [MarkdownComponent](https://github.com/krizzdewizz/ngoy-modules/blob/master/ngoy-module-markdown/src/main/java/ngoy/markdown/component/MarkdownComponent.java) for an example. It appends the HTML converted from CommonMark. You can write markdown inside the component or reference a `.md` resource.

## Custom rendering

A component may implement the `OnRender` interface to render any content. Such a component does not need a template. Rendering can be done in code only:

```java
@Component(selector = "person") // no template
public class PersonComponent implements OnRender {
	public void onRender(Output output) {
		output.write("Person works!");
	}
}
```

Some prefer to have the logic/view separated, some prefer to have it all in code. ngoy has you both covered.

For code only components, we heavily suggest our other product `hyperml`, which is integrated in ngoy. Like ngoy, it provides fast, near zero-copy rendering. Please visit the [hyperml site](https://github.com/krizzdewizz/hyperml) for documentation.

Make your component a subclass of `HtmlComponent` and write the `hyperml` markup in the overridden `content()` method:

```java
@Component(selector = "person")
public class PersonComponent extends HtmlComponent {

	protected void content() {
		div(classs, "title");
		{
			h1("Person works!", $);
		}
		$(); // div
	}

	// optionally render styles - corresponds to styles/styleUrls
	protected void styles() {
		css(".title", color, "red");
		css("body");
		{
			$(height, "100%");
			$(backgroundColor, "blue");
		}
		$();
	}
}
```

You can render components. Attributes, as usual, are used to pass inputs to the component.

```java
div();
{
	$("person", "thePerson", new Person("peter"), $);

	// or using the component's class instead of it's selector
	$(PersonComponent.class, "thePerson", new Person("paul"), $);

	// instead of untyped input initialization using attributes, you can pass an init function:
	$(PersonComponent.class, personCmp -> personCmp.thePerson = new Person("mary"), $);

	// the selector variant needs a type parameter or a cast inside the lambda
	this.<PersonComponent>$("person", personCmp -> personCmp.thePerson = new Person("gandalf"), $);
}
$(); // div

/*
<div>
  <person>hello: peter</person>
  <person>hello: paul</person>
  <person>hello: mary</person>
  <person>hello: gandalf</person>
</div>
*/
```

This allows you to freely mix template- and code-only components.

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
- [raw](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/common/RawPipe.java)
- [translate](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/translate/TranslatePipe.java) (see <a href="#translate">Translate Module</a>)


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

Note: The pipe syntax `|` is not part of the Java grammar. ngoy parses it 'manually' with regex. It should be fine in most cases, but in complex expressions (which should be avoided anyway) it could lead to a parse error. In such a situation, you can always resort to the function call syntax, prefixing the pipe with `$`:

```java
'hello' | uppercase | greet
```

is the same as:
```java
$greet($uppercase('hello'))
```

## Plain Text Templates

ngoy renders plain text formats (no markup) and you can nevertheless use components the same way as with regular templates.

Just set the root component's `contentType` to `"text/plain"`.

A complete example:

```java
@Component(selector = "header", template = "Welcome, {{'{{ name }}'}}\n")
public class HeaderCmp {
	@Input
	public String name;
}

@Component(selector = "footer", template = "sincerely")
public class FooterCmp {
}

// set contentType to text/plain
@Component(selector = "", contentType = "text/plain", templateUrl = "mail.txt")
@NgModule(declarations = { HeaderCmp.class, FooterCmp.class })
public class AppComponent {
	public Person person = new Person("peter", 22);
	public String[] hobbies = new String[] { "music", "surfing", "dancing" };
}
```

`mail.txt`:
```
<header [name]="person.name"></header>
age: {{'{{ person.age }}'}}
hobbies:
<span *ngFor="let h of hobbies">	{{'{{ h }}'}}
</span>
<footer></footer>
```

Produces:

```
Welcome, peter

age: 22
hobbies:
	music
	surfing
	dancing

sincerely
```

`text/plain` characteristics:
- no output escaping takes place
- all components/elements are inlined, just like <a href="#ng-container">&lt;ng-container&gt;</a>

## XML Templates

ngoy makes no distinction between XML and HTML. It's totally the same.

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

A class annotated with `NgModule` serves as a container for declarations and providers. A logical unit to group a feature together.
May a component needs a provider or other components to work correctly. So you would group them into a module, so that clients of your component can import just the module instead of all parts separately. And you could add more stuff to the module afterwards without breaking the clients.

 A `NgModule` has three attributes:

- `declarations`: make components, directives and pipes known to ngoy
- `providers`: make providers/services known to ngoy
- `imports`: imports other `NgModule`s, making them known to ngoy recursively

Note: Unlike other module systems, there exists no boundaries between modules. At the end, everything is stuffed into a single map. Everything can be reached from anywhere.

A runtime exception is thrown whenever more than one component matches an element.

A provider can also be specified on a `@Component` to spare a `NgModule`:
```java
@Component(selector = "person", providers = { PersonService.class })
public class PersonComponent {
}
```

Component providers are **never** local to a component but **always** global.

A `@Component` can also be a `@NgModule`.

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

## Built-in Modules

The following modules are contained in the ngoy binaries.
See the module's documentation below on how they must be imported into your app.

### Router

A 'traditional' server-side web setup is page oriented. A page's url, such as /index.jsp, is bound to a template engine which renders the page's content. When your site will get more pages, you start extracting out common parts to separate 'fragments' and #include them in the different pages.

You could do that with ngoy as well, but ngoy favors the 'single page application' design pattern.

All your site/app's urls are served by this single page. A `Router` maps from a url/path to a component, which is rendered inside that single page when the user navigates to that url.

We'll use the code of this website as an example to configure the router. See [Main.java](https://github.com/krizzdewizz/ngoy-website/blob/master/src/main/java/ngoyweb/app/Main.java).

This website has 4 urls: `/index`, `/get-started`, `/doc` and `/motivation`

In order to serve all these, add a `/*` mapping to the `@Controller`:

```java
@Controller
@RequestMapping("/*")
public class Main implements InitializingBean {
	...
}
```

Configure the router using a `RouterConfig`:

```java
RouterConfig routerConfig = RouterConfig
	.baseHref("/")
	.location(useValue(Location.class, () -> request.getRequestURI()))
	.route("index", HomeComponent.class)
	.route("get-started", GetStartedComponent.class)
	.route("doc", DocComponent.class)
	.route("motivation", MotivationComponent.class)
	.build();
```

`baseHref()` should be given the same path prefix as in `@RequestMapping()` above. So if your app serves `/example/*`, the `baseHref()` would be `/example`.

`location()` specifies a `Location` provider, which uses the current `request.getRequestURI()` value for the location path.

`route()` takes a path for the first parameter and a component to render when `Location` points to that path.

`build()` finally builds the router config to be passed to the router module:

```java
Ngoy<AppComponent> ngoy = Ngoy.app(AppComponent.class)

	// add router module
	.modules(RouterModule.forRoot(routerConfig))

	...
	.build();
```

Inside the app's template, add a `<router-outlet>` element to the place where you want the components to be rendered:

```html
<!-- Page Content -->
<div class="container app-container">
	<div class="row">
		<div class="col">
			<!-- HomeComponent, GetStartedComponent etc will be rendered here -->
			<router-outlet></router-outlet>
		</div>
	</div>
</div>
```

Note: In Angular, the component is rendered as a sibling to the `<router-outlet>` element. In ngoy, the `<router-outlet>` element is not rendered.

So if the user navigates to `/get-started`, the `GetStartedComponent` is rendered where `<router-outlet>` is etc.

One route path segment, and only one, can be parametrized:

```java
.route("person/detail/:id", PersonDetailComponent.class)
```

The actual parameter `id` can be retrived by injecting the `RouteParams` object:

```java
@Component(...)
public class PersonDetailComponent implements OnInit {
	@Inject
	public RouteParams routeParams;

	@Override
	public void onInit() {
		String id = routeParams.get("id");

		// load person with id
	}
}
```

So when the user navigates to `/person/detail/27`, the route parameter `id` would be `27`.

The `RouteParams` object is valid only during the rendering phase and is cleared afterwards.

The routing possibilities are very basic at the moment. You can provide your own sophisticated routing by a `ActiveRouteProvider`:

```java
public interface ActiveRouteProvider {
	/**
	 * Returns the active route based on the path returned by
	 * {@link Location#getPath()}.
	 *
	 * @param path Path
	 * @return active route or null if none
	 */
	@Nullable
	ActiveRoute getActiveRoute(String path);
}
```

So you could handle query parameters, maybe sub routing etc. Contributions are welcome!

For more information see also [Static site](#static-site).

### Translate

Using the `TranslateModule`, you can easily translate text in your templates using the standard Java way with message bundles.

The easiest way to include translation support is to build the ngoy instance with a call to `translateBundle()`:

```java
Ngoy<AppComponent> ngoy = Ngoy.app(AppComponent.class)
	.translateBundle("messages") // PropertyResourceBundle 'baseName'
	.build();
```

`translateBundle()` adds the `TranslateModule` to ngoy and configures the `TranslateService` to load the `PropertyResourceBundle` named `"messages"`.

Given these message bundles:

`messages_en.properties`:
```
MSG_GREETING=hello world
```
and `messages_de.properties`:
```
MSG_GREETING=hallo welt
```

then in your template, you can obtain the translation/message for a given key using either the `translate` pipe:
```html
<h1>{{"{{ 'MSG_GREETING'"}} | {{'translate'}}  }}</h1>
```

or the `translate` directive:
```html
<h1 translate="MSG_GREETING"></h1>
```

The current locale for the translation is held by the `LocaleProvider`. Default is `Locale#getDefault()`.

Given a german locale, both will render:
```html
<h1>hallo welt</h1>
```

You can override the locale by providing another `LocaleProvider`:

```java
Ngoy<AppComponent> ngoy = Ngoy.app(AppComponent.class)
	.translateBundle("messages")
	.providers(
		Provider.useValue(LocaleProvider.class, new LocaleProvider.Default(Locale.ENGLISH)))
	.build();
```

or use the session's locale (Spring Boot):
```java
Ngoy<AppComponent> ngoy = Ngoy.app(AppComponent.class)
	.translateBundle("messages")
	.providers(
		Provider.useValue(LocaleProvider.class, LocaleContextHolder::getLocale))
	.build();
```

Parameters are supported by the `translate` pipe:
```
MSG_GREET_WITH_PARAM=hello {0}
```

```html
<h1>{{"{{ 'MSG_GREET_WITH_PARAM'"}} | {{"translate: 'world'"}}  }}</h1>
```

You can inject `TranslateService` anywhere for translations in code:

```java
@Component(...)
public class PersonComponent implements OnInit {

	@Inject
	public TranslateService translateService;

	public void onInit() {
		String greeting = translateService.translate("MSG_GREET_WITH_PARAM", "world");
		...
	}
}
```

## Additional Modules

More ready-to-use modules can be found in the [ngoy-modules](https://github.com/krizzdewizz/ngoy-modules) collection.

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

@Component(selector = "person", providers = { PersonService.class, WeatherService.class })
public class PersonComponent implements OnInit {

	// constructor injection
	public PersonComponent(WeatherService weatherService) {
		// do things with weatherService
	}

	// field injection
	@Inject
	public PersonService personService;

	public void onInit() {
		// do things with personService
	}
}
```

Fields must be public, non-static, non-final and annotated with `@Inject`. Constructor parameters must not be annotated.

A runtime exception is thrown if there's no provider for a service. A dependency may be declared `@Optional` in which case no exception is thrown.

Note: a service must be annotated with `@Injectable` when it should be picked up by <a href="#package-modules">Package Modules</a>.

There are 3 kinds of providers:
- `class A` -> `class A`: Wherever `class A` is requested, inject an instance of `class A` (example above)
-  `class A` -> `useClass B`: Wherever `class A` is requested (could be an interface), inject an instance of `class B`, which must be assignable to `class A`. Used to override default behaviour.
-  `class A` -> `useValue V`: Wherever `class A` is requested (could be an interface), inject the instance `V`, which must be an instance of `class A`. Only available at runtime, not with annotations.

## Providers at Runtime

Providers can be specified at runtime, especially the `useValue` variant, which is used to inject external dependencies into ngoy.

```java

// 'service' may be an Spring repository/service or an instance created by you

ngoy.app(AppComponent.class)
	.providers(Provider.useValue(MyService.class, service)
	.build();
```

See also [Provider.java](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/main/java/ngoy/core/Provider.java)

## External DI Systems

By implementing the `Injector` interface, one can provide dependencies from other DI systems such as Spring Boot to ngoy.

See [BeanInjector.java](https://github.com/krizzdewizz/ngoy-tour-of-heroes/blob/master/src/main/java/toh/app/BeanInjector.java) for an example.

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

# Unit Testing

Unit testing your components, directives, pipes etc. is a piece of cake. ngoy can render any component, not just the 'app'.
You can provide mock services or quickly have a container that uses your component.

See [`ANgoyTest.java`](https://github.com/krizzdewizz/ngoy/blob/master/ngoy/src/test/java/ngoy/ANgoyTest.java) to get started or any of the other [~290 tests](https://github.com/krizzdewizz/ngoy/tree/master/ngoy/src/test/java/ngoy).

Example:
```java
public class ContainerTest extends ANgoyTest {

	@Component(
		selector = "test",
		template = "a<ng-container *ngFor=\"let s of strings\">{{'{{s}}'}}</ng-container>b")
	public static class CmpRepeated {
		public String[] strings = new String[] { "w", "x", "q" };
	}

	@Test
	public void testRepeated() {
		assertThat(render(CmpRepeated.class)).isEqualTo("awxqb");
	}
}
```

# Debugging

The most common source of errors are probably the Java expressions inside the template. Developers are used to have smart editors that indicate syntax errors, unresolved/misspelled fields etc. Currently there is no such thing for ngoy templates.

ngoy tries hard to give you precise information about an error inside your template.

There are two type of errors:

- Compile errors are thrown as part of the template -> Java -> byte code process when there is a syntax error, unresolved field etc.

Example of a compile error:
```
ngoy.core.CompileException: Compile error in expression "persn.getName()": "persn" is neither a method, a field, nor a member class of "ngoy.demo.PersonComponent"
source: templateUrl: person.component.html, position: [7:3 @256]
```

- Runtime errors are thrown when an exception occurs during the evaluation of the expression, such as `NullPointerException`.

Example of a runtime error:
```
ngoy.core.NgoyException: Runtime error in expression "person.getName()": java.lang.NullPointerException
source: templateUrl: person.component.html, position: [7:3 @256]
```

Both errors give you the errorneous expression, the template url/component and line information.

# Applications

ngoy is a standalone library with no dependencies to any web server/framework. All it needs is an Writer/OutputStream to write the contents to.

You can simply render it to `System.out` in the `main` method:

```java
public static void main(String[] args) {
	Ngoy.app(AppComponent.class)
		.build()
		.render(System.out);
}
```

## Web

When rendering HTML, it's practical to spin off a web server to view the contents in the browser. See the [Getting started](get-started) guide for an example. May the app stays/is designed for a web app or, after successful testing, you can render it to a static site (see below).

ngoy is designed to 'build once, render many times'. So there should be a single instance of your ngoy app serving all the clients.

If you need session state, separate it from any other and provide it with `Provider#useValue()`. A complete example is available in the [ngoy-e2e-test](https://github.com/krizzdewizz/ngoy-e2e-test) project.

## Static site

Using the `renderSite()` method, you can render the app to a folder on your computer:

```java
public static void main(String[] args) {
	Ngoy.app(AppComponent.class)
		.build()
		.renderSite(Paths.get("docs"));
}
```

If your site consists of only one component, an `index.html` file is written.

If your app contains a `Router`, a file for every configured route is written. You can render individual/parametrized routes:

```java
.renderSite(Paths.get("docs"), "/index", "/person/detail/23", "/person/detail/56");
```

See also [this file](https://github.com/krizzdewizz/blog/blob/master/src/main/java/blog/app/Main.java) for an example.

# CLI

ngoy has a built in CLI with which you can
- render templates and expressions from the command line
- quickly generate source code artifacts for new projects, components, pipes etc.

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
$ ./ngoy -e "java.time.LocalDateTime.now()"

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
$ ./ngoy g c com.example.person.Person

generating artifact './src/main/java/ngoygen/person/PersonComponent.java'...
generating artifact './src/main/java/ngoygen/person/person.component.html'...
generating artifact './src/main/java/ngoygen/person/person.component.css'...
```

Note: the element, resp. the `selector` of `PersonComponent` is prefixed with `app`. It's common practice to have a prefix in order to
avoid collisions with existing HTML elements or components from other libraries.

You can override the prefix. Create a `ngoy.properties` file and add this property:

```
app.prefix=myprefix
```

The generator looks up the properties file in the current directory.

# Java Expressions

ngoy expressions are Java code with some template friendly extensions.

## Access fields, call methods

Expressions are bound to the template's component instance. You can access the component's (static) fields and call it's (static) methods.

Except for the `java` package, all other static calls are prohibited. So inside template expressions, you may use a fully qualified call to `java.*`:

```html
<div *ngFor="let nbr of java.util.Arrays.asList(1, 2, 3)">{{'{{nbr}}'}}</div>
```

The above example can also be written using the `list` pipe:
```html
<div *ngFor="let nbr of $list(1, 2, 3)">{{'{{nbr}}'}}</div>
```

### Lambdas

Lambdas can be used in template expressions. Method references are not supported.

```html
<div *ngFor="let entry : java.util.stream.Stream.of('a ', ' b').map(c -> c.trim())">{{'{{entry}}'}}</div>
```

Note: Template expressions should be kept simple and basically free from business logic. Complex logic should reside in the component class.

Static interface methods like `java.util.stream.Stream#of` are currently not supported in Java 11.

## Prohibited Syntax

These Java syntax elements are prohibited and will lead to a compile error:

- assignments such as `name = 'x'`
- increment/decrement such as `x++`, `--i`, `x += 1`
- `this` reference
- Anonymous class

**For Angular users, here are some notable differences to the Angular syntax:**

Truthy/falsy values do not exist. Expressions like this won't work:

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

## Java Extensions

ngoy adds some template friendly extensions to the Java language. They are transformed to standard Java and compiled directly into the template class to be statically verified.

### Smart Strings

A string's delimiter may be the single or double quote; just like JavaScript.

Using Java's double quoted strings inside template attribute bindings enforces you to escape or use single quoted strings
 for the attribute value:
```html
<div [title]=' "-" + person.name + "-" '></div>
```

With ngoy's Smart Strings, you can have it also the regular way:
```html
<div [title]=" '-' + person.name + '-' "></div>
```

As a consequence, the char literal does not exist anymore, since `'a'` is transformed to `"a"`. If you really need a char, you can write:
```java
'a'.charAt(0)
```

### Field access to getter

A field access is replaced by it's getter if available:

```java
public class Person {
	private String name;
	public String getName() {
		return name;
	}

	public int age;

	public boolean isTeenager() {
		return age < 20;
	}
}

person.name     // -> person.getName()
person.teenager // -> person.isTeenager()
```

### List/Map index access

The array index syntax `array[index]` can be used on `java.util.List` and `java.util.Map`:

```java
List<Person> persons = ...
person[0]           // -> person.get(0)

Map<String, Person> personMap = ...
personMap['Peter']  // -> personMap.get("Peter")
```

### Pipes

A pipe is transformed to a function prefixed with `$`.

```java
person.birthdate | date:'dd.MM.yyyy' // -> $date(person.birthdate, 'dd.MM.yyyy')
```

# Javadoc

Javadoc can be found [here](javadoc/index.html).
