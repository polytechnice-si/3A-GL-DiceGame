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
		int a = dice.roll();
		int b = dice.roll();
		this.lastValue = Optional.of(Math.max(a,b));
	}

	public Optional<Integer> getLastValue() {
		return lastValue;
	}

}
