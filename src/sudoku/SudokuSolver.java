package sudoku;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * //TODO: infinite loop? 
 *  
 * performance probleme?: jede rekursion erzeugt 4*9*9 kopierschritte - sollte
 * jew. im cache liegen?
 * 
 * @author Chris
 */
public class SudokuSolver {
	static long recursions = 0;
	static long fieldCopies = 0;

	SudokuField f = new SudokuField();
	ArrayList<Spot> candidates;

	String fileName;

	// final List<Integer> ALL = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

	static class Guess {
		int val;

		SudokuField backup;

		public Guess(int val) {
			this.val = val;
		}
	}

	static class Spot {
		int x, y;
		List<Guess> q;

		public Spot(int x, int y, List<Guess> q) {
			this.x = x;
			this.y = y;
			this.q = q;
		}

		int spotCandidateCount; // is always q.size();

		@Override
		public String toString() {
			return "x: " + x + ", y: " + y + ", count: " + spotCandidateCount;
		}
	}

	static class SpotComparator implements Comparator<Spot> {
		@Override
		public int compare(Spot q1, Spot q2) {
			if (q1.spotCandidateCount < q2.spotCandidateCount) {
				return -1;
			}
			return (q1.spotCandidateCount == q2.spotCandidateCount) ? 0 : 1;
		}
	}

	public SudokuSolver(String file) throws IOException {
		this.fileName = file;
		load(file);
	}

	/**
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		 String[] files = { "easy1.sudoku", "normal1.sudoku", "extreme1.sudoku" };
//		String[] files = { "easy1.sudoku" };
		for (String file : files) {
			long start = System.currentTimeMillis();
			SudokuSolver s = new SudokuSolver("src/sudoku/" + file);
			s.solve();
			System.out.println("time: " + (System.currentTimeMillis() - start)
					+ "ms\n\n");
		}
	}

	private void solve() {
		System.out.println("Starting field:");
		System.out.println(this);

		PriorityQueue<Spot> sortedCandidates = new PriorityQueue<>(f.remaining,
				new SpotComparator());
		candidates = new ArrayList<>(f.remaining);
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				if (f.getVal(x, y) == 0) {
					List<Guess> spotCandidates = new LinkedList<>();
					for (int val = 1; val <= 9; val++) {
						if (!f.check(x, y, val))
							continue; // spot taken?
						Guess guess = new Guess(val);
						spotCandidates.add(guess);
					}
					Spot spot = new Spot(x, y, spotCandidates);
					// could only be 0 for invalid fields
					spot.spotCandidateCount = spotCandidates.size();

					sortedCandidates.add(spot);
				}
			}
		}
		Spot s;
		while ((s = sortedCandidates.poll()) != null) {
			candidates.add(s);
		}
//		System.out.println(sortedCandidates.size());
//		System.out.println(candidates.size());
		System.out.println(solve(0)); // first

		System.out.println("recursions: " + recursions);
		System.out.println("fieldCopies: " + fieldCopies);
		System.out.println(this);
	}

	/**
	 * finde alle kandidaten, sortiere liste aufsteigend nach kandidaten je
	 * spot, bearbeite in der reihenfolge ==> priority queue
	 * 
	 * * delay field copy (more than 300 element copies) * use priority queue,
	 * sort "candidatesOfASpot" using candidates.size TODO: use tuning
	 * parameter: every N steps, clear candidate List and find candidates again,
	 * to update candidate count? would be more efficient to directly eliminate
	 * non-candidates priority queue may be unnecessary, simple step over any
	 * spots with "too many" candidates, loop until all spots set remember first
	 * spot with minimal candidate count, start again there using that value as
	 * threshold ==> more checks per recursion, less copies
	 * 
	 * @return
	 */
	private boolean solve(int startIndex) {
		recursions++;
		// *recursively iterate all possible values for non-solved spots
		// *eleminate all invalid candidates for each free spot first
		// *remember all choices to enable backtracking (stack?)

		// erste fallunterscheidung kaputt - stelle sicher,
		// dass jeder index nur einmal bearbeitet wird
		for (int index = startIndex; index < candidates.size(); index++) {
			Spot spot = candidates.get(index);
//			System.out.println("index: " + index + ", " + spot);

			// spot empty?
			if (f.getVal(spot.x, spot.y) != 0) {
				System.out.println("for: spot already filled");
				continue; // should never happen
			}
			List<Guess> spotCandidates = spot.q;

			if (spotCandidates.size() == 1) {
				// update remaining free slots, perhaps in field[][],
				// to recognize unambiguous cases later on
				Guess guess = spotCandidates.get(0);
				f.set(spot.x, spot.y, guess.val);
			} else if (spotCandidates.size() == 0) {
				// FEHLER; aufräumen
				System.out
						.println("spotCandidates list empty, index: " + index);
				return false;
			} else {

				// eliminate easy case
				if (f.getCandCount(spot.x, spot.y) == 1) {
					// finde cand, iteriere einfach die guess.val
					int debug_c = 0;
					for (Guess guess : spotCandidates) {
						if (f.check(spot.x, spot.y, guess.val)) {
							f.set(spot.x, spot.y, guess.val);
							debug_c++;
						}
					}
					if (debug_c != 1) {	//	FIXME
							System.out.println("ERROR, getCandCount stimmt nicht, index: " + index);
					}
				} else {
					// TODO create fewer copies: Field.copyFrom, always backup to the same instance
					// candidates einzeln abarbeiten
					for (int i = spotCandidates.size() - 1; i >= 0; i--) {
						Guess guess = spotCandidates.get(i);
						if (f.getVal(spot.x, spot.y) != 0) {
							System.out.println("spot already filled, ERROR");
							break; // should never happen
						}
						if (!f.check(spot.x, spot.y, guess.val)) {
							// System.out.println("guess eliminated");
							continue; // eliminates too much??
						}
						guess.backup = new SudokuField(f);
						f.set(spot.x, spot.y, guess.val);
						boolean result = solve(index + 1);
						if (result) {
							// wenn true, dann haben wir eine lösung
							return true;
						} else {
							// FEHLER;
							f = guess.backup;
							if (i == 0) {
								// alle candidaten führen nicht zur lösung
								return false;
							} else {
								// versuche nächsten kandidaten
								continue;
							}
						}
					}
				}
			}
		}

		if (f.remaining == 0) {
			return true;
		} else {
			return false;
		}

		// TODO: parallelize, by pursuing different candidates on different
		// threads
	}

	private void load(String file) throws IOException {
		BufferedReader buff = new BufferedReader(new FileReader(file));
		if (!buff.ready()) {
			System.out.println("File read problem");
			System.exit(0);
		}
		for (int i = 0; i < 9; i++) {
			String line = buff.readLine();
			// System.out.println(line);
			for (int j = 0; j < 9; j++) {
				int val = Integer.parseInt(line.substring(j, j + 1));
				if (val != 0) {
					f.set(i, j, val);
				}
			}
		}
		buff.close();
		checkAll();
	}

	/**
	 * somewhat redundant to f.check(), but specialized to invalidity check
	 * after load
	 */
	private void checkAll() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (f.getVal(i, j) == 0)
					continue;
				if (!checkValid(i, j, f.getVal(i, j))) {
					System.out.printf(
							"load: field invalid, x:%d, y:%d, val:%d\n", i, j,
							f.getVal(i, j));
					System.exit(0);
				}
			}
		}
	}

	boolean checkValid(int x, int y, int val) {
		checkRange(x);
		checkRange(y);
		checkValue(val);
		for (int i = 0; i < 9; i++) {
			if (i != x && f.getVal(i, y) == val) {
				return false;
			}
		}
		for (int i = 0; i < 9; i++) {
			if (i != y && f.getVal(x, i) == val) {
				return false;
			}
		}

		// check 3X3 square
		int s_x = x / 3; // square id in x direction, starting at 0
		int s_x_off = s_x * 3; // square x offset

		int s_y = y / 3;
		int s_y_off = s_y * 3; // square y offset

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int i_pos = i + s_x_off;
				int j_pos = j + s_y_off;
				if (i_pos != x && j_pos != y && f.getVal(i_pos, j_pos) == val) {
					System.out.printf("check: x:%d, y:%d, val:%d\n", i_pos,
							j_pos, f.getVal(i_pos, j_pos));
					return false;
				}
			}
		}
		return true;
	}

	public static void checkRange(int x) {
		if (x < 0 || x > 8) {
			throw new IllegalArgumentException();
		}
	}

	public static void checkValue(int val) {
		if (val < 1 || val > 9) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Field: " + fileName + "\n");
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				sb.append(f.getVal(i, j));
			}
			sb.append('\n');
		}
		sb.append("remaining: " + f.remaining + "\n");
		return sb.toString();
	}
}
