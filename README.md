# Stackifier

Deserializes a given stack trace to java.lang.Throwable and then groups together stack trace elements by library API calls.

Useful for programmatically analyzing stack traces.

## Install
**Repository**
```
git clone https://github.com/talal830/stackifier.git
cd stackifier
mvn clean install
```

**Dependency**
```xml
<dependency>
    <groupId>com.stackifier</groupId>
    <artifactId>stackifier</artifactId>
    <version>1.1</version>
</dependency>    
```

## Usage
```

// Create Stackifier using builder
Stackifier stackifier = new Stackifier.Builder()
        .add("org.apache.commons") // Add libraries you wish to stackup in stack trace
        .add("java", "sun", "junit")
        .use(new StackTraceDeserializer()) // Skip this to use default deserializer
        .get();

// Some stacktrace as string
String stacktrace = "java.lang.IndexOutOfBoundsException...." 

// Use stackifier to stackify stacktrace
Stackified stackified = stackifier.stackify(stacktrace);
   
// Use stackified object to get stacked groups and so on
List<Group> groups = stackified.getGroups();

```

## Example

#### Sample Stacktrace
```
java.lang.IndexOutOfBoundsException: Index: 2, Size: 2
	at java.util.ArrayList.rangeCheck(ArrayList.java:653)
	at java.util.ArrayList.get(ArrayList.java:429)
	at org.apache.commons.collections4.iterators.CollatingIterator.set(CollatingIterator.java:307)
	at org.apache.commons.collections4.iterators.CollatingIterator.least(CollatingIterator.java:350)
	at org.apache.commons.collections4.iterators.CollatingIterator.next(CollatingIterator.java:245)
	at org.apache.commons.collections4.iterators.CollatingIteratorTest.testIterateEvenEven(CollatingIteratorTest.java:135)
	at sun.reflect.GeneratedMethodAccessor2.invoke(Unknown Source)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at junit.framework.TestCase.runTest(TestCase.java:176)
	at junit.framework.TestCase.runBare(TestCase.java:141)
	at junit.framework.TestResult$1.protect(TestResult.java:122)
	at junit.framework.TestResult.runProtected(TestResult.java:142)
	at junit.framework.TestResult.run(TestResult.java:125)
	at junit.framework.TestCase.run(TestCase.java:129)
	at junit.framework.TestSuite.runTest(TestSuite.java:255)
	at junit.framework.TestSuite.run(TestSuite.java:250)
	at org.junit.internal.runners.JUnit38ClassRunner.run(JUnit38ClassRunner.java:84)
	at org.pitest.junit.adapter.CustomRunnerExecutor.run(CustomRunnerExecutor.java:42)
	at org.pitest.junit.adapter.AdaptedJUnitTestUnit.execute(AdaptedJUnitTestUnit.java:85)
	at org.pitest.mutationtest.execute.MutationTimeoutDecorator$1.run(MutationTimeoutDecorator.java:89)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at java.lang.Thread.run(Thread.java:748)

```

#### Pretty Printing
```java
Stackified stackified = stackifier.stackify(sample);
stackified.prettyPrint();
```
**Output**
```
(1) java
		java.util.ArrayList.rangeCheck(ArrayList.java:653)
		java.util.ArrayList.get(ArrayList.java:429)

(2) org.apache.commons
		org.apache.commons.collections4.iterators.CollatingIterator.set(CollatingIterator.java:307)
		org.apache.commons.collections4.iterators.CollatingIterator.least(CollatingIterator.java:350)
		org.apache.commons.collections4.iterators.CollatingIterator.next(CollatingIterator.java:245)
		org.apache.commons.collections4.iterators.CollatingIteratorTest.testIterateEvenEven(CollatingIteratorTest.java:135)

(3) sun
		sun.reflect.GeneratedMethodAccessor2.invoke(Native Method)
		sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)

(4) java
		java.lang.reflect.Method.invoke(Method.java:498)

(5) junit
		junit.framework.TestCase.runTest(TestCase.java:176)
		junit.framework.TestCase.runBare(TestCase.java:141)
		junit.framework.TestResult$1.protect(TestResult.java:122)
		junit.framework.TestResult.runProtected(TestResult.java:142)
		junit.framework.TestResult.run(TestResult.java:125)
		junit.framework.TestCase.run(TestCase.java:129)
		junit.framework.TestSuite.runTest(TestSuite.java:255)
		junit.framework.TestSuite.run(TestSuite.java:250)

(6) other
		org.junit.internal.runners.JUnit38ClassRunner.run(JUnit38ClassRunner.java:84)

(7) org.pitest
		org.pitest.junit.adapter.CustomRunnerExecutor.run(CustomRunnerExecutor.java:42)
		org.pitest.junit.adapter.AdaptedJUnitTestUnit.execute(AdaptedJUnitTestUnit.java:85)
		org.pitest.mutationtest.execute.MutationTimeoutDecorator$1.run(MutationTimeoutDecorator.java:89)

(8) java
		java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
		java.util.concurrent.FutureTask.run(FutureTask.java:266)
		java.lang.Thread.run(Thread.java:748)
```

#### Getting Groups ####
**By group name**
```
List<Group> javaGroups = stackified.getGroups("java"); // here group name is 'java'

for (Group group : groups) {
    // access group's stacktrace elements
    List<StackTraceElement> elements = group.getElements();
    
    // pretty print
    System.out.println(group.prettyString());
        
    ....    
}
```
**Output**
```
(1) java
		java.util.ArrayList.rangeCheck(ArrayList.java:653)
		java.util.ArrayList.get(ArrayList.java:429)


(4) java
		java.lang.reflect.Method.invoke(Method.java:498)


(8) java
		java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
		java.util.concurrent.FutureTask.run(FutureTask.java:266)
		java.lang.Thread.run(Thread.java:748)
```

**By group Id**
```
Optional<Group> javaGroups = stackified.getGroup(8); // Getting 'java' group at 8th index
System.out.println(group.get().prettyString());
```
**Output**
```
(8) java
		java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
		java.util.concurrent.FutureTask.run(FutureTask.java:266)
		java.lang.Thread.run(Thread.java:748)
```

**Getting elements directly by group id**
```
List<StackTraceElement> javaGroupElements = stackified.getElements(8); // All stack trace elements in 'java' group at 8th index

....
```
**Output**
```
    java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
    java.util.concurrent.FutureTask.run(FutureTask.java:266)
    java.lang.Thread.run(Thread.java:748)
```

## License
This project is licensed under the MIT License
