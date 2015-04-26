# jyield

-----
**Beware:** This project is not currently being actively maintened.

-----

jyield aims to provide java continuations support similar to [c# yield](http://msdn.microsoft.com/en-us/library/9k7k7cf0.aspx) coroutines. 

The methods annotated with @Continuable are transformed in runtime or optionally in compile time.

Features
-------

  * Generators 
  * Continuations
  * Chaining continuations
  * Execution state serialization support
  * Try catch blocks support
  * Synchronized blocks support

Code sample
-----------

```java
import jyield.Continuable;
import jyield.Yield;

public class Sample {
        
        @Continuable
        public static Iterable<Integer> power(int number, int exponent) {
                int counter = 0;
                int result = 1;
                while (counter++ < exponent) {
                        result = result * number;
                        System.out.print("[" + result+ "]");
                        Yield.ret(result);
                }
                return Yield.done();
        }

        public static void main(String[] args) {
                // Display powers of 2 up to the exponent 8:
                for (int i : power(2, 8)) {
                        System.out.print(" "+ i + " ");
                }
        }
}
// Output: [2] 2 [4] 4 [8] 8 [16] 16 [32] 32 [64] 64 [128] 128 [256] 256
```

Check out [Samples other examples], including continuation example.

Quick Start
-----------

  * Download the distribution package and unzip it.
  * With the command prompt go to the subdirectory `jyield-0.0.6/examples/basic/`.
  * Inside this directory you should find the file Sample.java (equal to the sample in this page).
  * Execute the following commands
```
javac -cp ../../jyield-0.0.6-jar-with-dependencies.jar Sample.java
java -javaagent:../../jyield-0.0.6-jar-with-dependencies.jar Sample
```
### Advanced usage, offline instrumentation

  * Same steps as above, but with these commands:
  
```
javac -cp ../../jyield-0.0.6-jar-with-dependencies.jar Sample.java
java -jar ../../jyield-0.0.6-jar-with-dependencies.jar Sample.class --overwrite --verbose
java -cp .;../../jyield-0.0.6-runtime.jar Sample
```

  * With offline instrumentation you can deploy your project without the -javaagent jvm parameter, also the file jyield-0.0.6-runtime.jar (6KB) is much smaller than jyield-0.0.6-jar-with-dependencies.jar (172KB).

Dependencies
------------

[Asm](http://asm.ow2.org/) is used during instrumentation. 

For simplicity all dependencies are included in the jyield-0.0.6-jar-with-dependencies.jar binary.
If this binary is used no extra dependencies are needed, however it implies in adding a specific version of [http://asm.ow2.org/ Asm] to your classpath, be aware of conflicts.

If you use offline instrumentation no Asm version will be required by your application in runtime, just jyield-0.0.6-runtime.jar, also other apps linking against your libraries won't need neither Asm nor runtime instrumentation.

How it works
------------

The jyield agent changes all methods that return Iterable, Iterator or Enumeration and have the @Continuable annotation.

The methods are replaced so when they are called instead of executing they return an instance of Iterable, Iterator or Enumeration that wraps a context for all the local variables of the method. 

Each call to `.next()` triggers an invocation of a modified version of the method. This new method has a switch statement that jumps to the correct line at each iteration.

Also at every call the local variables are restored on enter and saved on exit the method. This is done without any object wrapping so heavy processing may be done inside the continuations. The only time jyield instantiates any object (actually 3) is on the first call to the continuable method.

Using with maven
----------------

```xml
	<repositories>
		<repository>
			<id>maven2-repository.dev.java.net</id>
			<name>Java.net Repository for Maven</name>
			<url>http://download.java.net/maven/2/</url>
		</repository>
	</repositories>
...

	<dependencies>
		<dependency>
			<groupId>com.googlecode</groupId>
			<artifactId>jyield</artifactId>
			<version>0.0.6</version>
		</dependency>
	</dependencies>
```

Documentation
-------------

  * [Javadocs](http://jyield.googlecode.com/svn/wiki/apidocs/index.html ).
  * [Samples](Samples)


Acknowledgements
----------------

![http://asm.ow2.org/](http://asm.ow2.org/images/poweredbyasm.gif)

