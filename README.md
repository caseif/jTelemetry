jTelemetry [![Build Status](http://ci.caseif.net/job/jTelemetry/badge/icon)](http://ci.caseif.net/job/jTelemetry/)
========

jTelemetry is a lightweight Java toolkit for submitting arbitrary information to a web server.

Usage
-----

To use jTelemetry, first create an instance of the main class:

```java
JTelemetry jt = new JTelemetry("http://example.com/post_page.php");
```

Then, create a `Payload` object and add data to it:

```java
JTelemetry.Payload payload = jt.createPayload();
payload.addData("someString", "example");
payload.addData("someInt", 5);
payload.addData("someStringArray", new String[]{"something, something else"});
```

Finally, submit the data to the server:

```java
HttpResponse response = payload.submit();
```

You can then analyze the response:

```java
int statusCode = response.getStatusCode();
String responseMessage = response.getMessage();
```

The server implementation may provide more information for non-success responses in the message depending on the status
code.

jTelemetry can be shaded as a Maven dependency. Add `http://repo.caseif.net/content/groups/public/` as a repository, and
`net.caseif.jtelemetry:jtelemetry:1.0.0` as a dependency.

Contributing
------------

Pull requests are always welcome. Contributions should first and foremost compile, and should follow
[Google Java Style](https://google.github.io/styleguide/javaguide.html).

Building
--------

jTelemetry uses Gradle to handle compilation and dependencies.

1. Clone the repo: `git clone https://github.com/caseif/jTelemetry.git`
2. Build the project: `./gradlew` (`gradlew` on Windows)

License
-------

jTelemetry is made available under the BSD 3-Clause license. Its source and binaries may be used and distributed within
the license's bounds.
