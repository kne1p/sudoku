package sudoku;

public class SudokuField {

	private int[][] f = new int[9][9];

	public int remaining;

	int[][] xNeighbors = new int[9][10]; //TODO: eliminate neighbor[i][0]? 
	int[][] yNeighbors = new int[9][10];
	int[][] sqNeighbors = new int[9][10];

	static int fieldCopies = 0;

	public SudokuField() {
		for (int i = 0; i < 9; i++) {
			xNeighbors[i][0] = 9; // 9 spots frei
			yNeighbors[i][0] = 9; // 9 spots frei
			sqNeighbors[i][0] = 9; // 9 spots frei
		}
		remaining = 81;
	}

	// copy constructor
	public SudokuField(SudokuField old) {
		SudokuField.fieldCopies++;
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				f[i][j] = old.f[i][j];
			}
		}
		remaining = old.remaining;
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 10; j++) {
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
//		return (f[x][y] < 0) ? 0 : f[x][y];
		return f[x][y];
	}

	public Candidates getCandidates(int x, int y) {
		Candidates c = new Candidates();
//		int sq_id = getSquareId(x, y);
//		return (xNeighbors[x][0] < yNeighbors[y][0]) ? 
//				((xNeighbors[x][0] < sqNeighbors[sq_id][0]) ? 
//						xNeighbors[x][0] : sqNeighbors[sq_id][0]) : 
//					((yNeighbors[y][0] < sqNeighbors[sq_id][0]) ? 
//						yNeighbors[y][0] : sqNeighbors[sq_id][0]);
		for (int val = 1; val <= 9; val++) {
			if (check(x, y, val)) {
				c.set(val);
			}
		}
		return c;
	}

	/**
	 * TODO: lasse set auch prüfen, ob noch platz frei - gebe true/false zurück
	 * je nach erfolg
	 */
	public void set(int x, int y, int val) {
		if (val == 0) {
			throw new IllegalArgumentException();
		}
		if (f[x][y] != 0) {
			throw new IllegalArgumentException();
		}
		if (!this.check(x,y,val)) {
			throw new IllegalArgumentException();
		}
		
		f[x][y] = val;
		xNeighbors[x][val] = 1;
		yNeighbors[y][val] = 1;
		int sq_id = getSquareId(x, y);
		sqNeighbors[sq_id][val] = 1;

		// update free spots
		xNeighbors[x][0]--;
		yNeighbors[y][0]--;
		sqNeighbors[sq_id][0]--;
		remaining--;
		if (xNeighbors[x][0] < 0 || yNeighbors[y][0] < 0 || sqNeighbors[sq_id][0] < 0 || remaining < 0) {
			throw new IllegalStateException("ERROR: negative free spots");
		}
	}

	/**
	 * @return true, wenn val in allen 3 nachbarschaften nicht vorkommt
	 */
	public boolean check(int x, int y, int val) {
		int sq_id = getSquareId(x, y);

		return (xNeighbors[x][val] + yNeighbors[y][val] + sqNeighbors[sq_id][val]) == 0;
	}

	private int getSquareId(int x, int y) {
		int x_id = x / 3;
		int y_id = y / 3;
		return 3 * x_id + y_id;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				sb.append(getVal(i, j));
			}
			sb.append('\n');
		}
		return sb.toString();
	}
}