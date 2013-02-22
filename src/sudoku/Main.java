package sudoku;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Main {

	public static final String TEST_PATH = "res/";
//	public static final String[] TEST_FILES = { "easy1.sudoku", "normal1.sudoku" };
	public static final String[] TEST_FILES = { "easy1.sudoku", "normal1.sudoku", "extreme1.sudoku" };
//	public static final String[] TEST_FILES = { "easy1.sudoku"};

	/**
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// String[] files = { "easy1.sudoku" };
		for (String file : TEST_FILES) {
			long start = System.currentTimeMillis();
			
			SudokuSolver s = new SudokuSolver(TEST_PATH + file);
			s.solve();
			
			System.out.println("time: " + (System.currentTimeMillis() - start)
					+ "ms\n\n");
		}
	}

	public static List<String> load(String file) throws IOException {
		List<String> lines = new LinkedList<>();
	
		try (BufferedReader buff = new BufferedReader(new FileReader(file))) {
			if (!buff.ready()) {
				throw new IOException("File read problem");
			}
	
			for (int i = 0; i < 9; i++) {
				String line = buff.readLine();
				// System.out.println(line);
				lines.add(line);
			}
		}
	
		return lines;
	}

}
