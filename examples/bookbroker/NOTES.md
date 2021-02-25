## Documenting the creation and development of bookbroker example

Using coffeemaker example for guidance

The word Space refers to a JavaSpace service

---

Creating base directory for example:
```bash
$ mkdir examples/bookbroker
$ cd examples/bookbroker
```

Creating src directory structure. The example has three main components, the bookbroker, the bookbuyer, and the bookseller; making a separate directory for each:
```bash
$ mkdir -p src/main/java/sorcer/bookbroker
$ mkdir -p src/main/java/sorcer/bookseller
$ mkdir -p src/main/java/sorcer/bookbuyer
```

Also, going to need tests:
```bash
$ mkdir -p src/test/java/sorcer/bookbroker
```

Next, want to setup the minimal working example with build.gradle file and testing.

Starting by creating the bookbroker interface, `src/main/java/sorcer/bookbroker/BookBrokerService.java`. The bookbroker runs a server that takes requests from bookbuyers out of a Space and writes them into a Space for each registered bookseller. Additionally, the bookbroker takes bids from the Space for booksellers and writes them into the Space for bookbuyers.


Next, the bookbroker class, `src/main/java/sorcer/bookbroker/impl/BookBroker.java`.
```bash
mkdir -p src/main/java/sorcer/bookbroker/impl
```





TODO/BUGS:
* publishedInterfaces failing exception ambiguous, misleading... 
  was spelling error. Instead of null pointer should have been class 
  not found.
* method not declared in interface... no exception at all... should
  say that method not found in interface
* object needs to be serializable, message should be explicit in that 
  requirement... NotSerializableException
* local vs remote service execution, message should make it obvious
* hands on config file syntax erro

serial version uid... need to declare final long. java convention
```java
private static final long serialVersionUID = 1L;
```

-dl file should contain files only what client needs

impl backend only