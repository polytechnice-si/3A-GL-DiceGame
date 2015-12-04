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
