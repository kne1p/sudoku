package sudoku.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import sudoku.Candidates;
import sudoku.Main;
import sudoku.SudokuField;
import sudoku.SudokuSolver;

@RunWith(Parameterized.class)
public class SudokuSolverTest {

	@Parameters
	public static Collection<String[]> files() {
		List<String> files = Arrays.asList(Main.TEST_FILES);
		List<String[]> listOfArrays = new LinkedList<>();
		for (String s: files) {
			listOfArrays.add(new String[] {s});
		}
		return listOfArrays;
	}

	private String fileName;

	public SudokuSolverTest(String s) {
		fileName = Main.TEST_PATH + s;
	}

	public SudokuSolver s;

	@Before
	public void setUp() throws Exception {
		s = new SudokuSolver(fileName);
	}

	@Test(timeout = 2000)
	public void testSolve() throws IOException {
		s.solve();

		assertTrue(s.f.remaining == 0);
		
		String solutionPath = fileName.replace(".sudoku", ".solution.sudoku");
		File solutionFile = new File(solutionPath);
//		System.out.println("debug "+solutionPath);
//		System.out.println("debug "+solutionFile.getAbsolutePath() +" "+solutionFile.exists());
		assertTrue(solutionFile.exists());
		String goldSolution = StringUtils.join(Main.load(solutionPath),"");
		String givenSolution = s.f.toString().replaceAll("\n", "");
//		System.out.println(goldSolution);
//		System.out.println(givenSolution);
//		System.out.println();
		assertEquals(goldSolution, givenSolution);
	}

	@Test
	public void testCheckRange() {
		try {
			SudokuSolver.checkRange(-1);
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			SudokuSolver.checkRange(9);
			fail();
		} catch (IllegalArgumentException e) {
		}

		SudokuSolver.checkRange(0);
		SudokuSolver.checkRange(2);
		SudokuSolver.checkRange(8);
	}

	@Test
	public void testCheckValue() {
		try {
			SudokuSolver.checkValue(-1);
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			SudokuSolver.checkValue(0);
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			SudokuSolver.checkValue(10);
			fail();
		} catch (IllegalArgumentException e) {
		}

		SudokuSolver.checkValue(1);
		SudokuSolver.checkValue(9);
	}

	@Test
	public void testToString() {
		assertEquals(s.f.toString(), (new SudokuField(s.f, s).toString()));
	}

	@Test
	public void testBitmask() {
		assertTrue(Candidates.bitMask(0) == 1);
		assertTrue(Candidates.bitMask(1) == 2);
		assertTrue(Candidates.bitMask(9) == 512);
	}
}
