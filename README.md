# Designing a Game of Dices (Kata)

  * Author: [Sébastien Mosser](mosser@i3s.unice.fr)
  * Version: 1.0 (11.2015)

The intent of this kata is to put all the elementary bricks shown in the _Software Engineering_ together. We'll use Maven and JUnit as a support to build a simple game of dices in Java, using a vertical (feature-driven) approach. We'll also introduce _mock_ objects (using the Mockito framework) to support testing when classical unit tests do not fit.

## Functional Specifications [Tasks]

The product backlog is defined as the following (order matters, representing the prioritization defined by the _Product Owner_). We explicitly do not use the _user story_ paradigm, as it is possible to follow a feature-driven approach even without stories.

  1. Being able to throw a dice 
    * _Acceptance criteria_: The dice has 6 faces, and returns a random number in [1,6].
  2. Associate a dice roll result to a given player:
    * _Acceptance criteria_: A player has a name, and exposes the value obtained from her very own dice
  3. The player throws two dices and keeps the max
    * _Acceptance criteria_: the dice is only thrown twice, and the max value is kept.  
  4. A _Game of Dice_ is a two player game, and the max value win (ex-aequo implies to restart the game, no winner after 5 ex-aequo matches)
    * _Acceptance criteria_: the game exposes a winner, following the game rules

## Project Architecture [Maven]

We start by creating a new directory, _e.g._, `GoD`. In this directory, we create one directory for the Java source (`src/main/java`) and another one for the tests (`src/test/java`). 

    mosser@azrael GoD$ mkdir -p src/main/java src/test/java 


We create a `pom.xml` file, containing the minimal information needed by Maven to model the project. The 
project has the identifier `game-of-dices` in the `fr.unice.polytech.katas.3a` group, and we rely on Junit 4.12 for testing purpose.

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>fr.unice.polytech.katas.3a</groupId>
    <artifactId>game-of-dices</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
</project>
```

You can now import this empty project in your favorite IDE, as a Maven project, and generate a `.gitignore` file for version control purpose.


## Task #1: Trowing a dice

In a package `god` we create a class `Dice` used to model this concept. The responsibility of this class is to support dice throwing, through a `roll` method. In a minimal approach, we only consider dices with 6 faces. Using a `RuntimeException` is part of our debt, but there is no added value for now to introduce an exception hierarchy in our code. We construct a Dice using a given `Random` object to support game reproducibility.

```java
package god;

import java.util.Random;

public class Dice {

	private final static int FACES = 6;
	private Random rand;

	public Dice(Random rand) { this.rand = rand; }

	public int roll() {
		int result = rand.nextInt(FACES) + 1;
		if (result < 1 || result > FACES)
			throw new RuntimeException("Dice returns an incompatible value");
		return result;
	}
}
```

### Tests [JUnit]

We use unit tests to model our acceptance criteria and automate the verification of our dice. Operationalizing the _definition of done_ is one of the good side effect of testing your code.

We need to test that: _(i)_ throwing a dice returns a value, and that _(ii)_ a value not in [1,6] will throw an exception. The first test is easy: we'll throw the dice a _good enough_ number of time, and assess that the returned values are in range. But how to implement the second one, as it is not possible with this implementation ?

The _naive_ answer is to extend the `Random` concept into a `NoRandom` one. This extension will be configurable considering that one can configure which value will be returned when the `nextInt(int): int` method is called. We'll use it in our test suite to check that the exception is thrown when necessary. 

```java
package god;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DiceTest {

	Dice theDice;

	@Test
	public void rollReturnsAValue() {
		theDice = new Dice(new Random());
		for(int i = 0; i < 100; i++) {
			int result = theDice.roll();
			assertTrue(result >= 1);
			assertTrue(result <= 6);
		}
	}

	@Test(expected = RuntimeException.class)
	public void identifyBadValuesGreaterThanNumberOfFaces() {
		theDice = new Dice(new NoRandom(7));
		theDice.roll();
	}

	@Test(expected = RuntimeException.class)
	public void identifyBadValuesLesserThanOne() {
		theDice = new Dice(new NoRandom(-1));
		theDice.roll();
	}

	class NoRandom extends Random {
		int value;
		public NoRandom(int v) { this.value = v; }
		@Override
		public int nextInt(int m) { return value; }
	}
}
```

## Introducing mock objects

It does not make any sense to manually override classes for test purpose. Actually, what we only need is to consider a `Dice` but we need to change its behavior into something that fit our test context. The concept of _Mock Object_ is exactly defined to support this intention.

### Introducing the Mock Framework [Maven]

Thanks to the Maven dependency engine, we automatically introduce _Mockito_ (a state of the art mock framework for the Java ecosystem) by declaring the dependency in the POM file:

```xml
<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-core</artifactId>
  <version>1.10.19</version>
  <scope>test</scope>
</dependency>
```

### Creating a mock

The role of a _mock_ is to overload the behavior of an object, in a controllable way. As a consequence, instead of manually creating a class extending `Random`, we create a _mocked_ random, and overload its behavior when the `nextInt(int): int` method is called, according to our needs.

```java
// ...
import static org.mockito.Mockito.*;

public class DiceTest {

   // ...
   
	@Test(expected = RuntimeException.class)
	public void identifyBadValuesGreaterThanNumberOfFaces() {
		Random tooMuch = mock(Random.class);
		when(tooMuch.nextInt(anyInt())).thenReturn(7);
		theDice = new Dice(tooMuch);
		theDice.roll();
	}

	@Test(expected = RuntimeException.class)
	public void identifyBadValuesLesserThanOne() {
		Random notEnough = mock(Random.class);
		when(notEnough.nextInt(anyInt())).thenReturn(-1);
		theDice = new Dice(notEnough);
		theDice.roll();
	}
}
```

## Task #2: Associating a dice roll to a player

A minimal implementation of the `Player` concept is the following: the constructor takes the player's name and her dice, and will store in a `lastValue` field the last value obtained from the dice. We'll return `-1` if the dice has not been thrown for now. A `play` method throws the dice and stores the value.

```java
package god;

public class Player {

	private String name;
	private Dice dice;
	private int lastValue = -1;

	public Player(String name, Dice dice) {
		this.name = name;
		this.dice = dice;
	}
	
	public void play() {
		this.lastValue = dice.roll();
	}
	
	public int getLastValue() {
		return lastValue;
	}

}
```

### Test [JUnit]

The test suite is easy to define for this implementation.

```java
package god;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Random;

public class PlayerTest {

	Player p;

	@Test
	public void lastValueNotInitialized() {
		p = new Player("John Doe", new Dice(new Random()));
		assertEquals(p.getLastValue(), -1);
	}

	@Test
	public void lastValueInitialized() {
		p = new Player("John Doe", new Dice(new Random()));
		p.play();
		assertNotEquals(p.getLastValue(), -1);
	}

}
```

### Using Java 8 Optionals [Maven]

Returning `-1` when the dice has not been thrown is ugly _by design_, and thus part of our technical debt. Java 8 defines the notion of `Optional` that fits this very purpose: a value can be defined, or not. To support this feature, we must ensure that our compiler is set to the `1.8` version of both source code and target bytecode files. This can be easily defined in the POM:

```xml
<properties>
  <maven.compiler.source>1.8</maven.compiler.source>
  <maven.compiler.target>1.8</maven.compiler.target>
</properties>
```

We can now enhance the `Player` code as it is now ensured that the right version of the compiler will be used.

```java
package god;
import java.util.Optional;

public class Player {

	private String name;
	private Dice dice;
	private Optional<Integer> lastValue = Optional.empty();

	public Player(String name, Dice dice) {
		this.name = name;
		this.dice = dice;
	}

	public void play() {
		this.lastValue = Optional.of(dice.roll());
	}

	public Optional<Integer> getLastValue() {
		return lastValue;
	}

}
```
And the test now only check if a value is present or not:

```java
	@Test
	public void lastValueNotInitialized() {
		p = new Player("John Doe", new Dice(new Random()));
		assertFalse(p.getLastValue().isPresent());
	}

	@Test
	public void lastValueInitialized() {
		p = new Player("John Doe", new Dice(new Random()));
		p.play();
		assertTrue(p.getLastValue().isPresent());
	}
```

## Task #3: keeping the max of two rolls

We have to enhance the `play` method to throw the dice twice and keep only the max one.

```java
	public void play() {
		int a = dice.roll(); int b = dice.roll();
		this.lastValue = Optional.of(Math.max(a,b));
	}
```

### Test: Player follows the rules [Mockito]

How it is possible to test that our implementation of players follows the rule and does not throw the dice 42 times instead of 2? As usual, mock objects will help us to assess that, by allowing one to measure the execution flow that goes through a given mock. Using a mocked `Dice`, we simply asks Mockito to check that the `roll` method was called only two times after the `play` method was called.

```java
	@Test
	public void throwDiceOnlyTwice() {
		Dice d = mock(Dice.class);
		p = new Player("John Doe", d);
		p.play();
		verify(d, times(2)).roll();
	}
```
### Test: Player keeps the max value [Mockito]

To assess that our player is rightly keeping the maximum value, we simply need to control the values returned by the associated dice.

```java
	@Test
	public void keepTheMaximum() {
		Dice d = mock(Dice.class);
		p = new Player("John Doe", d);
		
		when(d.roll()).thenReturn(2).thenReturn(5);
		p.play();
		assertEquals(p.getLastValue().get(), new Integer(5));

		when(d.roll()).thenReturn(6).thenReturn(1);
		p.play();
		assertEquals(p.getLastValue().get(), new Integer(6));
	}
```

## Task #4: Playing a Game of Dice

We create the `Game` concept, that implements the specified rule: two players (`left` and `right`) are playing together, and the `play` method will designate a winner. After 5 ex-aequo matches, the game ends with no winner.

```java
package god;

import java.util.Optional;

public class Game {

	private Player left;
	private Player right;

	public Game(Player left, Player right) {
		this.left = left;
		this.right = right;
	}

	public Optional<Player> play() {
		int counter = 0;
		while(counter < 5) {
			left.play();  int l = left.getLastValue().get();
			right.play(); int r = right.getLastValue().get();

			if(l > r )      { return Optional.of(left);  }
			else if (r > l) { return Optional.of(right); }

			counter++;
		}
		return Optional.empty();
	}
}
```

### Testing the "No Winner" case

We use a mocked dice that alway returns the same value. This example allows us to illustrate _partial mocks_, defined as _spies_ in Mockito vocabulary. 

For this test case, we need to modify the behavior of a dice to always return the same value and trigger an ex-aequo situation, so we use a _mock_. But to assess the fact that the players only played 5 times, we do not need to modify the players behavior, we only need to spy at them. The `spy` abstraction provided by Mockito serves this very purpose.

```java
package god;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GameTest {

	Game g;

	@Test
	public void noWinnerAfter5Attempts() {
		Dice single = mock(Dice.class);
		when(single.roll()).thenReturn(1);

		Player p1 =  spy(new Player("John", single));
		Player p2 =  spy(new Player("Jane", single));

		g = new Game(p1,p2);
		assertFalse(g.play().isPresent());
		verify(p1, times(5)).play();
		verify(p2, times(5)).play();
	}

}
```

### Testing the "Winner" case

We simply uses two mocked player to control who is the winner in a given game.

```java
	@Test
	public void andTheWinnerIs() {

		Player p1 = mock(Player.class);
		when(p1.getLastValue()).thenReturn(Optional.of(new Integer(5)));

		Player p2 = mock(Player.class);
		when(p2.getLastValue()).thenReturn(Optional.of(new Integer(2)));

		g = new Game(p1,p2);
		assertEquals(p1, g.play().get());
	}
```

