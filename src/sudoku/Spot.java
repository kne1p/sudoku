package sudoku;

class Spot {
	int x, y;
	Candidates initialCandidates;

	public Spot(int x, int y, Candidates c) {
		this.x = x;
		this.y = y;
		this.initialCandidates = c;
	}

	/**
	 * could only be 0 for invalid fields
	 */
	int initCandidateCount() {
		return initialCandidates.size();
	}

	@Override
	public String toString() {
		return "x: " + x + ", y: " + y + ", count: " + initCandidateCount();
	}
}