= jyield =

Description
===========

This project aims to provide java continuations support similar to c# yield.

The methods annotated with @Continuable are transformed in runtime or optionally in compile time.

Please refer to the project site for futher information:
http://jyield.googlecode.com/


About the jars:
===============

  * jyield-VERSION-with-deps.jar
    Has the dependencies embedded. Useful for quick usage.
    Remember to use the jvm parameter -javaagent:jyield-VERSION-with-deps.jar

  * jyield-VERSION-runtime.jar 
    Should be used only if the classes are instrumented in compile time.
    It does not needs the dependencies and does not do runtime instrumentation.

  * jyield-VERSION.jar
    In order to work needs the dependencies present on the lib 
    directory of the jyield-VERSION-dist.zip file.

