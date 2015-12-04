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
