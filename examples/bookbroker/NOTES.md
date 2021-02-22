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