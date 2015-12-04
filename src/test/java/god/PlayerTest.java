package god;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Random;

public class PlayerTest {

	Player p;

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

	@Test
	public void throwDiceOnlyTwice() {
		Dice d = mock(Dice.class);
		p = new Player("John Doe", d);
		p.play();
		verify(d, times(2)).roll();
	}

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

}
