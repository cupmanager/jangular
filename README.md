Jangular
========

Java Templating engine with AngularJS-like syntax and concepts.

Current status: Really super-early experimental alpha

Send questions to jangular@cupmanager.net


Todo
-------------
* [FIXED] Caching system. Also including a GuavaCachingStrategy
* Repository for controllers? Or at least specify package names to look in.
* [FIXED] Specify where Jangular should look for template files
* Tests & Documentation
* [FIXED] Maybe compile() shouldn't return a node, but rather an object that holds both the node and the CompilerSession
* [FIXED] Translations. Can be implemented as directives / inline-directives 
* [FIXED] Will the generated classes ever disappear or will we eat memory until we die? 
* Directives as attributes and some transclude functionality?


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
CompiledTemplate template = ConcreteTemplateCompiler.create()
    .withTemplateLoader(new FileTemplateLoader("templates"))
    .compile("template.html");

StringBuilder sb = new StringBuilder();
template.eval(sb);
String result = sb.toString();
```
Note that we also stored the compiled template to the ```template``` variable so that it can be reused later on.

You can of course also reuse the step just before ```compile()``` which is called a Compiler. Take a look at this code:
```java
TemplateCompiler compiler = ConcreteTemplateCompiler.create()
    .withTemplateLoader(new FileTemplateLoader("templates"));
    
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
TemplateCompiler compiler = ConcreteTemplateCompiler.create()
    .withTemplateLoader(new FileTemplateLoader("templates"));
    
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

TemplateCompiler compiler = ConcreteTemplateCompiler.create()
    .withTemplateLoader(new FileTemplateLoader("templates"));
    
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

TemplateCompiler compiler = ConcreteTemplateCompiler.create()
    .withTemplateLoader(new FileTemplateLoader("templates"))
    .withContextClass(UserExampleContext.class);
    
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



- - - 
### Caching





