package sudoku;

public class SudokuField {

	private int[][] f = new int[9][9];

	int remaining;

	public SudokuField() {
		// for (int i = 0; i < 9; i++) {
		// xNeighbors[i][0] = 9; // 9 spots frei
		// yNeighbors[i][0] = 9; // 9 spots frei
		// sqNeighbors[i][0] = 9; // 9 spots frei
		// }
		remaining = 81;
	}

	// copy constructor
	public SudokuField(SudokuField old) {
		SudokuSolver.fieldCopies++;
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				f[i][j] = old.f[i][j];
			}
		}
		remaining = old.remaining;
		for (int i = 0; i < 9; i++) {
			for (int j = 1; j < 10; j++) {
				xNeighbors[i][j] = old.xNeighbors[i][j];
				yNeighbors[i][j] = old.yNeighbors[i][j];
				sqNeighbors[i][j] = old.sqNeighbors[i][j];
			}
		}
	}

	// führe "neighborhoods" ein, jeder punkt gehört zu 3en davon
	// jede Neighborhood weiß, welche punkte fehlen
	// ==> neuen wert testen, indem alle drei neighborhoods gefragt werden
	// neighborhood als int[9], mit 0 oder 1 für
	// "schon vorhanden", evtl. int[10] mit info wie viele
	// fehlen im letzten feld?
	/**
	 * TODO: evtl. numberNeighborhood einführen, wie viele 3en fehlen zB noch?
	 */
	public int getVal(int x, int y) {
		// return f[x][y] & (0xF);
		return f[x][y];
	}

	/**
	 * IDEE: lasse set auch prüfen, ob noch platz frei - gebe true/false zurück
	 * je nach erfolg
	 */
	public void set(int x, int y, int val) {
		f[x][y] = val;
		xNeighbors[x][val] = 1;
		yNeighbors[y][val] = 1;
		int sq_id = getSquareId(x, y);
		sqNeighbors[sq_id][val] = 1;

		// update free spots
		// xNeighbors[x][0]--;
		// yNeighbors[y][0]--;
		// sqNeighbors[sq_id][0]--;
		remaining--;
	}

	/**
	 * @return true, wenn val in allen 3 nachbarschaften nicht vorkommt
	 */
	public boolean check(int x, int y, int val) {
		int sq_id = getSquareId(x, y);

		return (xNeighbors[x][val] + yNeighbors[y][val] + sqNeighbors[sq_id][val]) == 0;
	}

	int[][] xNeighbors = new int[9][10];
	int[][] yNeighbors = new int[9][10];
	int[][] sqNeighbors = new int[9][10];


	private int getSquareId(int x, int y) {
		// sq_id = 3*x_id + y_id, x_id = x/3, y_id = y/3
		int x_id = x / 3;
		int y_id = y / 3;
		return 3 * x_id + y_id;
	}
}