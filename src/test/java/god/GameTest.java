package god;

import org.junit.Test;

import java.util.Optional;

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

	@Test
	public void andTheWinnerIs() {

		Player p1 = mock(Player.class);
		when(p1.getLastValue()).thenReturn(Optional.of(new Integer(5)));

		Player p2 = mock(Player.class);
		when(p2.getLastValue()).thenReturn(Optional.of(new Integer(2)));

		g = new Game(p1,p2);
		assertEquals(p1, g.play().get());
	}

}
