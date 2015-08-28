PH Toolkit [![Build Status](http://ci.caseif.net/job/jTelemetry/badge/icon)](http://ci.caseif.net/job/jTelemetry/)
========

PH Toolkit is a lightweight Java toolkit for submitting arbitrary information to a web server (i.e. "phoning home").

Usage
-----

To use jTelemetry, first create an instance of the main class:

```
JTelemetry jt = new JTelemetry("http://example.com/post_page.php");
```

Then, create a `Payload` object and add data to it:

```
JTelemetry.Payload payload = jt.createPayload();
payload.addData("someString", "example");
payload.addData("someInt", 5);
payload.addData("someStringArray", new String[]{"something, something else"});
```

Finally, submit the data to the server:

```
payload.submit();
```

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
