package sudoku;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * //TODO: optimize by rewinding instead of copying half a kilobyte 15k times??
 * 
 * performance probleme?: jede rekursion erzeugt 4*9*9 kopierschritte - sollte
 * jew. im cache liegen?
 * 
 * @author Chris
 */
public class SudokuSolver {
	public int fieldCopies = 0;
	public int recursions = 0;
	public SudokuField f = new SudokuField();
	ArrayList<Spot> openSpots;

	String fileName;

	static class SpotComparator implements Comparator<Spot> {
		@Override
		public int compare(Spot q1, Spot q2) {
			if (q1.initCandidateCount() < q2.initCandidateCount()) {
				return -1;
			}
			return (q1.initCandidateCount() == q2.initCandidateCount()) ? 0 : 1;
		}
	}

	public SudokuSolver(String file) throws IOException {
		this.fileName = file;

		List<String> lines = Main.load(file);

		for (int i = 0; i < 9; i++) {
			String line = lines.get(i);

			for (int j = 0; j < 9; j++) {
				int val = Integer.parseInt(line.substring(j, j + 1));
				if (val != 0) {
					f.set(i, j, val);
				}
			}
		}
		checkAll(); 
	}

	public void solve() {
		System.out.println("Starting field:");
		System.out.println(this);

		if (f.remaining > 0) {
			PriorityQueue<Spot> sortedOpenSpots = new PriorityQueue<>(f.remaining,
					new SpotComparator());
			openSpots = new ArrayList<>(f.remaining);
			for (int x = 0; x < 9; x++) {
				for (int y = 0; y < 9; y++) {
					if (f.getVal(x, y) == 0) {
						Candidates c = f.getCandidates(x, y);
						Spot spot = new Spot(x, y, c);
	
						sortedOpenSpots.add(spot);
					}
				}
			}
	
			// poll priorityqueue to get list
			Spot s;
			while ((s = sortedOpenSpots.poll()) != null) {
				openSpots.add(s);
			}
		}
		System.out.println("solution found: " +solve(0)); // start on first openSpot

		System.out.println("recursions: " + recursions);
		System.out.println("fieldCopies: " + fieldCopies);
		System.out.println(this);
	}

	/**
	 * finde alle kandidaten, sortiere liste aufsteigend nach kandidaten je
	 * spot, bearbeite in der reihenfolge ==> priority queue
	 * 
	 * * delay field copy (more than 300 element copies)
	 * 
	 * * use priority queue, sort "candidatesOfASpot" using candidates.size
	 * 
	 * TODO: use tuning parameter: every N steps, clear candidate List and find
	 * candidates again, to update candidate count? would be more efficient to
	 * directly eliminate non-candidates priority queue may be unnecessary,
	 * simple step over any spots with "too many" candidates, loop until all
	 * spots set remember first spot with minimal candidate count, start again
	 * there using that value as threshold ==> more checks per recursion, less
	 * copies
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
		for (int index = startIndex; index < openSpots.size(); index++) {
			Spot spot = openSpots.get(index);
			// System.out.println("index: " + index + ", " + spot);

			// spot empty?
			if (f.getVal(spot.x, spot.y) != 0) {
				System.out.println("for: spot already filled");
				continue; // should never happen
			}
			Candidates initialCandidates = spot.initialCandidates;

			if (initialCandidates.size() == 1) {
				// update remaining free slots, perhaps in field[][],
				// to recognize unambiguous cases later on
				int guess = initialCandidates.getList().get(0);
				f.set(spot.x, spot.y, guess);
			} else if (initialCandidates.size() == 0) {
				// FEHLER; aufräumen
				System.err
						.println("initialCandidates list empty, index: " + index);
				return false;
			} else { //there were more than 1 initialCandidates

				// eliminate easy case
				Candidates candidates = f.getCandidates(spot.x, spot.y);
				if (candidates.size() == 1) {
					// TODO optimize
					int guess = candidates.getList().get(0);
					f.set(spot.x, spot.y, guess);
				} else if (candidates.size() == 0) {
					// no valid candidate, trace back 
					return false;
				} else {
					// System.err.println("mehrdeutig "+
					// (v++)+": "+spotCandidates.size()+
					// ", f.getCandCount: "+f.getCandCount(spot.x, spot.y));
					// TODO create fewer copies: Field.copyFrom, always backup
					// to the same instance
					// candidates einzeln abarbeiten
					for (int i = candidates.size() - 1; i >= 0; i--) {
						int guess = candidates.getList().get(i);
						
						if (f.getVal(spot.x, spot.y) != 0) {
							System.err.println("ERROR: spot already filled");
							break; // should never happen
						}
						if (!f.check(spot.x, spot.y, guess)) {
							// System.out.println("guess eliminated");
							continue; // eliminates too much??
						}
						
						SudokuField backup = new SudokuField(f, this);
						f.set(spot.x, spot.y, guess);

						if (solve(index + 1)) {
							//  true => solution found
							return true;
						} else {
							f = backup;
						}

						// try next candidate
					}

					// no candidate is solution
					return false;
				}
			}
		}

		return (f.remaining == 0);
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
		
		//XXX ist das feld bereits initialisiert zu diesem zeitpunkt? 
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
		sb.append(f.toString());

		sb.append("remaining: " + f.remaining + "\n");
		return sb.toString();
	}
}
