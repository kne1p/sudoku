package sudoku;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import sudoku.SudokuSolver.Guess;

/**
 * performance probleme?: jede rekursion erzeugt 4*9*9 kopierschritte -
 * sollte jew. im cache liegen?
 * 
 * @author Chris
 */
public class SudokuSolver {
	static long recursions = 0;
	static long fieldCopies = 0;

	SudokuField f = new SudokuField();
	ArrayList<Queue<Guess>> candidates;
	
	String fileName;

	// final List<Integer> ALL = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

	static class Guess {
		int x, y, val;
		
		SudokuField backup;
		int freeSpotsCell;

		public Guess(int x, int y, int val) {
			this.x = x;
			this.y = y;
			this.val = val;
		}
	}
	
	static class GuessQueueComparator implements Comparator<Queue<Guess>> {
		@Override
		public int compare(Queue<Guess> q1, Queue<Guess> q2) {
			Guess g1 = q1.peek();
			Guess g2 = q2.peek();
			if (g1.freeSpotsCell < g2.freeSpotsCell) {
				return -1;
			}
			return (g1.freeSpotsCell == g2.freeSpotsCell) ? 0 : 1;
		}
	}

	public SudokuSolver(String file) throws IOException {
		this.fileName = file;
		load(file);
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String[] files = {"easy1.sudoku", "normal1.sudoku", "extreme1.sudoku"};
		for (String file : files) {
			long start = System.currentTimeMillis();
			SudokuSolver s = new SudokuSolver("src/sudoku/"+file);
			s.solve();
			System.out.println("time: "+ (System.currentTimeMillis()-start) +"ms\n\n");	
		}
	}

	private void solve() {
		System.out.println("Starting field:");
		System.out.println(this);
		
		//TODO: copy field
		PriorityQueue<Queue<Guess>> sortedCandidates = new PriorityQueue<>(f.remaining, new GuessQueueComparator());
		candidates = new ArrayList<>(f.remaining);
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				if (f.getVal(x, y) == 0) {
					// Field backup;
					Queue<Guess> spotCandidates = new LinkedList<>();
					for (int val = 1; val <= 9; val++) {
						if (!f.check(x, y, val))
							continue;	//spot taken?
						Guess guess = new Guess(x, y, val);
						spotCandidates.add(guess);
					}
					int num = spotCandidates.size();
					Guess guess = spotCandidates.peek(); //could only be null for invalid fields
					guess.freeSpotsCell = num;
//					for (Guess guess : spotCandidates) {
//						guess.freeSpotsCell = num;
//					}
					sortedCandidates.add(spotCandidates);
				}
			}
		}
		candidates.addAll(sortedCandidates);
		System.out.println(sortedCandidates.size());
		System.out.println(candidates.size());
		System.out.println(solve(0)); // first 
		
		System.out.println("recursions: " + recursions);
		System.out.println("fieldCopies: " + fieldCopies);
		System.out.println(this);
	}

	/**
	 * TODO: finde alle kandidaten, sortiere liste aufsteigend nach kandidaten
	 * je spot, bearbeite in der reihenfolge alternativ: mehrere queues für
	 * kandidaten, je nachdem ob mehr oder weniger als threashold je spot (zB 3)
	 * ==> priority queue
	 * 
	 * TODO: delay field copy (more than 300 element copies)
	 * TODO: use priority queue, sort "candidatesOfASpot" using candidates.size
	 * TODO: use tuning parameter: every N steps, clear candidate List and find candidates again, to update candidate count?
	 * 	would be more efficient to directly eliminate non-candidates
	 *  priority queue may be unnecessary, simple step over any spots with "too many" candidates, 
	 *  	loop until all spots set
	 *  	remember first spot with minimal candidate count, start again there using that value as threshold
	 *  	==> more checks per recursion, less copies  
	 * 
	 * @return
	 */
	private boolean solve(int startIndex) {
		recursions++;
		// TODO recursively iterate all possible values for non-solved spots
		// eleminate all invalid candidates for each free spot first
		// remember all choices to enable backtracking (stack?)

		int x, y, val;
		for (int index = startIndex; index < candidates.size(); index++) {
			Queue<Guess> spotCandidates = candidates.get(index);
			
			if (spotCandidates.size() == 1) {
				Guess guess = spotCandidates.remove();
				f.set(guess.x, guess.y, guess.val);
				spotCandidates.clear();
			} else if (spotCandidates.size() == 0) {
				// FEHLER; aufräumen
				return false;
			} else {
				// candidates einzeln abarbeiten
				for (int i = spotCandidates.size() - 1; i >= 0; i--) {
					Guess guess = spotCandidates.remove();
					if (f.getVal(guess.x, guess.y) != 0) {
						System.out.println("spot already filled");
						break; //should not happen
					}
					guess.backup = new SudokuField(f);
					f.set(guess.x, guess.y, guess.val);
					boolean result = solve(index+1);
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
				spotCandidates.clear();
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
	 * TODO: f.check nutzen?
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

	private void checkRange(int x) {
		if (x < 0 || x > 8) {
			throw new IllegalArgumentException();
		}
	}

	private void checkValue(int val) {
		if (val < 1 || val > 9) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Field: "+fileName+"\n");
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
