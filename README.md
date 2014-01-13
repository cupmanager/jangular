Jangular
========

Java Templating engine with AngularJS-like syntax and concepts.

Current status: Really super-early experimental alpha

Send questions to jangular@cupmanager.net


Key points
-------------
* Uses XML syntax. Your template can be edited in any XML/HTML editor and you can easily keep it correctly balanced
* You can write directives that extend HTML with new tags. Directives are like isolated, reusable components!
* Dependency injection. We'll inject contextual variables into your directives/controllers for you!  
* Pretty fast. We use the [MVEL library](http://mvel.codehaus.org/) for expressions and generate Java bytecode for fast property accessing and scope creation.


Informal todo
-------------
* Repository for controllers? Or at least specify package names to look in.
* Directives as attributes and some transclude functionality?
* How to handle that template files change on disk?
* Directives/controllers should be able to only generate data for the fields that will actually be used in the template
* [FIXED] Caching system. Also including a GuavaCachingStrategy
* [FIXED] Specify where Jangular should look for template files
* [FIXED] Maybe compile() shouldn't return a node, but rather an object that holds both the node and the CompilerSession
* [FIXED] Translations. Can be implemented as directives / inline-directives 
* [FIXED] Will the generated classes ever disappear or will we eat memory until we die? 


Syntax
-------------
Like AngularJS, Jangular templates are always (almost) valid XML. Our syntax consists of special attributes and elements that you can add to your HTML/XML files. This enables you to use your preferred XML/HTML-editor and ensures that your elements are always correctly balanced and the resulting output is correct. 

This is a small example of a Jangular template:
``` html
<div>
  <h1 j-if="shouldShowTitle">Welcome, {{user.name}}</h1>
  You've bought the following items:
  <ul>
    <li j-repeat="item in items">
      {{item.title}}
    </li>
  </ul>
</div>
``` 


Jangular can also be used to generate plain text files by using the special attributes as elements instead:
```html
<j-if test="shouldShowTitle">Items for {{user.name}}:</j-if>
<j-repeat for="item in items">
  {{$index+1}}. {{item.title}}
</j-repeat>
```
Upon evaluation the ```j-``` tags will be removed and the output will be:
```
Items for John Doe:
  1. Apple
  2. Banana
  3. Orange
```


### Expressions
We use the [MVEL library](http://mvel.codehaus.org/) to parse and execute expressions. This includes expressions inside ```{{...}}```, ```j-if``` and the part of ```j-repeat``` after ```ìn```.


- - -
Usage in Java
-------------

This is a minimal example of how to invoke Jangular:
```java
String html = "<div>{{1+1}} == 2</div>";

StringBuilder sb = new StringBuilder();    
ConcreteTemplateCompiler.create()
    .compile(new ByteArrayInputStream(html.getBytes()))
    .eval(sb);

String result = sb.toString();
```
Now ```result``` will be equal to ```<div>2 == 2</div>```.

As you can see, the example above contains two steps. Compiling and evaluating.
Compiling turns the template into a series of very efficient steps ready for evaluation. We generate Java bytecode to improve performance.



You probably want to store your templates as files rather than strings in your code. If you move the actual template (```<div>{{1+1}} == 2</div>```) to a file called ```templates/template.html```, we can specify a TemplateLoader that tells Jangular how to find the template. The following code will produce output equivalent the to minimal example above:
```java
CompiledTemplate template = ConcreteTemplateCompiler.create(
	CompilerConfiguration.create()
		.withTemplateLoader(new FileTemplateLoader("templates")))
	.compile("template.html");

StringBuilder sb = new StringBuilder();
template.eval(sb);
String result = sb.toString();
```
Note that we also stored the compiled template to the ```template``` variable so that it can be reused later on.

You can of course also reuse the step just before ```compile()``` which is called a Compiler. Take a look at this code:
```java
TemplateCompiler compiler = ConcreteTemplateCompiler.create(
	CompilerConfiguration.create()
		.withTemplateLoader(new FileTemplateLoader("templates")));

CompiledTemplate template = compiler.compile("template.html");
CompiledTemplate other = compiler.compile("other.html");

StringBuilder sb = new StringBuilder();
template.eval(sb);
String result = sb.toString();
```

- - -


This example is quite boring as it does not contain any data, just a static expression. To add data we can go two ways. Either we create a ```Scope``` that contains the data, and we give that scope to Jangular when evaluating the template.
We can also let the template itself specify a controller. A controller is a Java class that knows how to fetch/produce data and expose it on a Scope so that the template can read and display it.

### Using a controller 

Controller class:
```java
public class ExampleCtrl extends AbstractController<ExampleCtrlScope> {
	public static class ExampleCtrlScope extends Scope {
		public String info;
	}
	
	@Override
	public void eval(ExampleCtrlScope scope) {
		scope.info = "Some data fetched from the database";
	}
}
```

Template (controller-example.html):
```html
Info:
<div j-controller="com.example.controllers.ExampleCtrl">
  {{info}}
</div>
```

Usage:
```java
TemplateCompiler compiler = ConcreteTemplateCompiler.create(
	CompilerConfiguration.create()
		.withTemplateLoader(new FileTemplateLoader("templates")));
    
CompiledTemplate template = compiler.compile("controller-example.html");

StringBuilder sb = new StringBuilder();
template.eval(sb);
String result = sb.toString();
```

Resulting output:
```html
Info:
<div>
  Some data fetched from the database
</div>

```


### Using a scope

Template (controller-example.html):
```html
Info:
<div>
  {{info}}
</div>
```

Usage:
```java
public class ExampleScope extends Scope {
  public String info;
}

....

TemplateCompiler compiler = ConcreteTemplateCompiler.create(
	CompilerConfiguration.create()
		.withTemplateLoader(new FileTemplateLoader("templates")));
    
CompiledTemplate template = compiler.compile("controller-example.html", ExampleScope.class);

StringBuilder sb = new StringBuilder();
ExampleScope scope = new ExampleScope();
scope.info = "Informational text";
template.eval(scope, sb);
String result = sb.toString();
```

Resulting output:
```html
Info:
<div>
  Informational text
</div>

```

- - - 

### Dependency injection

But how does the controller know what to fetch from the database? We provide you with the possibility to inject contextual variables into your controller. For example you might have an object that represents the current HTTP request or the currently logged in user. 
Code example!!

Controller class:
```java
public class UserCtrl extends AbstractController<UserCtrlScope> {
  public @Context User user;
  
	public static class UserCtrlScope extends Scope {
		public int loginCount;
	}
	
	@Override
	public void eval(UserCtrlScope scope) {
		scope.loginCount = Database.fetchLoginCount(user);
	}
}
```

Template (user-example.html):
```html
Info:
<div j-controller="com.example.controllers.UserCtrl">
  You've logged in {{loginCount}} times!
</div>
```

Usage:
```java
public class UserExampleContext extends EvaluationContext {
  public @Provides User user;
}

.....

TemplateCompiler compiler = ConcreteTemplateCompiler.create(
	CompilerConfiguration.create()
		.withTemplateLoader(new FileTemplateLoader("templates"))
		.withContextClass(UserExampleContext.class));
    
CompiledTemplate template = compiler.compile("controller-example.html");

UserExampleContext context = new UserExampleContext();
context.user = getCurrentlyLoggedInUser();

StringBuilder sb = new StringBuilder();
template.eval(sb, context);
String result = sb.toString();
```

Resulting output:
```html
Info:
<div j-controller="com.example.controllers.ExampleCtrl">
  You've logged in 13 times!
</div>
```

- - - 

### Directives
Just like in AngularJS you can create directives, which are isolated and reusable components. Directives come in two forms, regular and inline. 

#### Regular directives
Regular directives can be thought of as a special element that you can use in your template. For example:
```html
Choose an item: 
<example-dropdown options="myOptions"></example-dropdown>
```

You can write a Java class that specifies how to replace this kind of element like this:
```java
@Directive("example-dropdown")
@Template("exampleDropdown.html")
public class ExampleDropdownDirective extends AbstractDirective<ExampleDropdownDirectiveScope> {
	public @Context User user;
	
	public static class ExampleDropdownDirectiveScope extends Scope {
		@In public List<String> options;
	}
	
	@Override
	public void compile(Map<String,String> attrs, JangularNode templateNode, JangularNode contentNode) {
		
	}
	
	@Override
	public void eval(ExampleDropdownDirectiveScope scope) {
		scope.options = new ArrayList<String>(scope.options);
		scope.options.add(0, "Default option");
	}
}
```

And the exampleDropdown.html:
```html
<select>
	<option j-repeat="option in options">{{option}}</option>
</select>
```

A few things to note:
* You can @Inject things just like in a controller
* The directive template can only access the fields you specify as parameters, or expose on the directive scope. The directive scope is always isolated and does not inherit fields from the outside.
* Your scope class can contain fields annotated with @In. They will be filled with the value from the parent scope (in this case as specified by the attributes)

Running it: You have to register the directive class so Jangular knows about it using the withDirectives() method on the compiler:
```java
DirectiveRepository repo = new DirectiveRepository();
repo.register(ExampleDropdownDirective.class);

CompilerConfiguration conf = CompilerConfiguration.create()
		.withDirectives(repo)
    .withTemplateLoader(new FileTemplateLoader("templates"))
    .withContextClass(DirectiveExampleContext.class);

TemplateCompiler compiler = ConcreteTemplateCompiler.create(conf);

CompiledTemplate template = compiler.compile("controller-example.html");

DirectiveExampleContext context = new DirectiveExampleContext();
context.options = generateOptions();

StringBuilder sb = new StringBuilder();
template.eval(sb, context);
String result = sb.toString();
```

All of this will produce:
```html
Choose an item: 
<select>
	<option>Default option</option>
	<option>Apple</option>
	<option>Banana</option>
	<option>Orange</option>
</select>
```

#### Inline directives
Inline directives can be used to extend the syntax within elements. Jangular uses the syntax with double curly braces for expressions: {{....}}
You can use inline directives to come up with your own syntax for doing something else. For example, you could introduce a syntax with double brackets for translating text. So that ```[['Web.Page.Welcome']]``` would produce ```Välkommen``` in Swedish.

See the InlineTranslationTest class for an example of this (in src/test/java/net/cupmanager/jangular/util/InlineTranslationTest.java)


- - - 
### Caching
You can tell Jangular to cache the compilations of templates. We provide you with an implementation that uses Guava's Cache, but you can easily write your own adapter for another caching system.

Code example:
```java
	CompilerConfiguration conf = CompilerConfiguration.create()
		.withDirectives(repo)
		.withTemplateLoader(new FileTemplateLoader("templates/test", "templates/test/directives"));

	TemplateCompiler compiler = ConcreteTemplateCompiler.create(conf)
		.cached(new GuavaCachingStrategy(CacheBuilder.newBuilder().maximumSize(1000)));
  
	CompiledTemplate template = compiler.compile("test.html", AppScope.class);
	
	/* These will hit the cache immediately */
	template = compiler.compile("test.html", AppScope.class);
	template = compiler.compile("test.html", AppScope.class);
	template = compiler.compile("test.html", AppScope.class);
	template = compiler.compile("test.html", AppScope.class);
```

You can write your own caching strategy by extending the CachingStrategy interface, see the GuavaCachingStrategy for an example (it will be called by a CachingTemplateCompiler in Jangular)

