
/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	private int nPlayers, score, rounds;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private static final int N_DICE = 5;
	private int[] dice, numberOfDice, totalScore, completedSections, filledUpperCategories, upperScore, lowerScore,
			upperScoreBonus;
	private int[][] filledCategories;

	public static void main(String[] args) {
		new Yahtzee().start(args);
	}

	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		playGame();
	}

	private void playGame() {
		setUpArrays();
		rounds = 0;
		while (rounds < N_SCORING_CATEGORIES * nPlayers) {
			for (int i = 0; i < nPlayers; i++) {
				rollDice(i + 1);
			}
		}
		printWinningMessage();
	}

	public void setUpArrays() {
		filledCategories = new int[N_CATEGORIES][nPlayers];
		for (int i = 0; i < N_CATEGORIES; i++) {
			for (int j = 0; j < nPlayers; j++)
				filledCategories[i][j] = 0;
		}
		totalScore = new int[nPlayers];
		for (int i = 0; i < nPlayers; i++) {
			totalScore[i] = 0;
		}
		filledUpperCategories = new int[nPlayers];
		for (int i = 0; i < nPlayers; i++) {
			filledUpperCategories[i] = 0;
		}
		upperScore = new int[nPlayers];
		for (int j = 0; j < nPlayers; j++) {
			upperScore[j] = 0;
		}
		lowerScore = new int[nPlayers];
		for (int j = 0; j < nPlayers; j++) {
			upperScore[j] = 0;
		}
		upperScoreBonus = new int[nPlayers];
		for (int j = 0; j < nPlayers; j++) {
			upperScore[j] = 0;
		}
		completedSections = new int[nPlayers];
		for (int j = 0; j < nPlayers; j++) {
			completedSections[j] = 0;
		}
	}

	public void resetArray() {
		numberOfDice = new int[6];
		for (int i = 0; i < 6; i++) {
			numberOfDice[i] = 0;
		}
	}

	public void rollDice(int player) {
		String roll = player + "'s turn! Click 'Roll Dice' button to roll the dice";
		String rollMore = "Select the dice you wish to re-roll and click 'Roll Again'";
		display.printMessage(roll);
		dice = new int[N_DICE];
		int turns = 3;
		for (int i = 0; i < turns; i++) {
			if (i == 0) {
				display.waitForPlayerToClickRoll(player);
				firstRoll();
			} else {
				display.printMessage(rollMore);
				display.waitForPlayerToSelectDice();
				randomRoll();
			}
		}
		resetArray();
		for (int i = 0; i < N_DICE; i++) {
			for (int j = 0; j < 6; j++) {
				if (dice[i] == j + 1) {
					numberOfDice[j] += 1;
				}
			}
		}
		for (int i = 0; i < 6; i++) {
			System.out.print(numberOfDice[i]);
		}
		String category = "Click a category for this roll!";
		display.printMessage(category);
		updateScore(player);
		rounds++;
	}

	public void firstRoll() {

		for (int y = 0; y < N_DICE; y++) {
			int value = rgen.nextInt(1, 6);
			dice[y] = value;
		}
		display.displayDice(dice);
	}

	public void randomRoll() {
		int[] selectedDice = new int[N_DICE];
		for (int x = 0; x < N_DICE; x++) {
			if (display.isDieSelected(x) == true) {
				selectedDice[x] = 1;
			} else {
				selectedDice[x] = 0;
			}
		}
		for (int l = 0; l < N_DICE; l++) {
			if (selectedDice[l] == 1) {
				int value = rgen.nextInt(1, 6);
				dice[l] = value;
			}
		}
		display.displayDice(dice);
	}

	public void updateScore(int player) {
		int category = display.waitForPlayerToSelectCategory();
		while (filledCategories[category][player - 1] == 1) {
			category = display.waitForPlayerToSelectCategory();
		}
		filledCategories[category][player - 1] = 1;
		completedSections[player - 1]++;

		score = 0;
		if (category <= SIXES) {
			upperSection(category, player);
			filledUpperCategories[player - 1]++;
		} else if (category == THREE_OF_A_KIND) {
			threeOfAKind();
		} else if (category == FOUR_OF_A_KIND) {
			fourOfAKind();
		} else if (category == FULL_HOUSE) {
			fullHouse();
		} else if (category == SMALL_STRAIGHT) {
			detectSmallStraight();
		} else if (category == LARGE_STRAIGHT) {
			detectLargeStraight();
		} else if (category == YAHTZEE) {
			yahtzee();
		} else if (category == CHANCE) {
			chance();
		}

		totalScore[player - 1] += score;
		if (category <= 6) {
			upperScore[player - 1] += score;
			if (upperScore[player - 1] > 63) {
				upperScoreBonus[player - 1] = 35;
			}
		} else if (category > 8 && category < 16) {
			lowerScore[player - 1] += score;
		}
		displayScores(player, category);

	}

	public void displayScores(int player, int category) {
		display.updateScorecard(category, player, score);
		display.updateScorecard(17, player, totalScore[player - 1]);
		if (completedSections[player - 1] == N_SCORING_CATEGORIES) {
			display.updateScorecard(7, player, upperScore[player - 1]);
			display.updateScorecard(8, player, upperScoreBonus[player - 1]);
			display.updateScorecard(16, player, lowerScore[player - 1]);
		}
	}

	public void upperSection(int category, int player) {
		for (int i = 0; i < N_DICE; i++) {
			if (dice[i] == category) {
				score = score + category;
			}
		}
	}

	public int findSum() {
		int sum = 0;
		for (int i = 0; i < N_DICE; i++) {
			sum += dice[i];
		}
		return sum;
	}

	public void threeOfAKind() {
		for (int i = 0; i < 6; i++) {
			if (numberOfDice[i] >= 3) {
				score = findSum();
			}
		}
	}

	public void fourOfAKind() {
		for (int i = 0; i < 6; i++) {
			if (numberOfDice[i] >= 4) {
				score = findSum();
			}
		}
	}

	public void fullHouse() {
		for (int i = 0; i < 6; i++) {
			if (numberOfDice[i] == 3) {
				for (int j = 0; j < 6; j++) {
					if (numberOfDice[j] == 2) {
						score = 25;
						break;
					}
				}
			}
		}
	}

	public void yahtzee() {
		for (int i = 0; i < 6; i++) {
			if (numberOfDice[i] == 5) {
				score = 50;
			}
		}
	}

	public void chance() {
		score = findSum();
	}

	public void detectSmallStraight() {
		int consecutiveNumbers = 0;
		for (int i = 0; i < 6; i++) {
			if (numberOfDice[i] >= 1) {
				consecutiveNumbers++;
				if (consecutiveNumbers == 4) {
					score = 30;
					break;
				}
			} else {
				consecutiveNumbers = 0;
			}
		}
	}

	public void detectLargeStraight() {
		int consecutiveNumbers = 0;
		for (int i = 0; i < 6; i++) {
			if (numberOfDice[i] == 1) {
				consecutiveNumbers++;
				if (consecutiveNumbers == 5) {
					score = 40;
					break;
				}
			} else {
				consecutiveNumbers = 0;
			}
		}
	}

	public void printWinningMessage() { 
		int highScore = totalScore[0];
		String winningPlayer = playerNames[0];
		for (int i = 1; i < nPlayers; i++) {
			if (totalScore[i] > highScore) {
				highScore = totalScore[i];
				winningPlayer = playerNames[i];
			}
		}
		String winningMessage = "Congratulations" + winningPlayer + " ,you're the winnner with a total score of "
				+ highScore;
		display.printMessage(winningMessage);
	}

}

