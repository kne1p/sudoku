package sudoku;

import java.util.LinkedList;
import java.util.List;

public class Candidates {
	
	private int candidates;
	private LinkedList<Integer> candList = new LinkedList<>(); 
	
	public List<Integer> getList() {
		return candList;
	}

	public static int bitMask(int val) {
		return 1 << val;
	}
	
	public void set(int val) {
		if (val < 1 || val > 9) {
			throw new IllegalArgumentException();
		}
		
		if (!isSet(val)) {
			candList.add(val);
		}
		candidates |= bitMask(val); 
	}
	
	public void delete(int val) {
		if (val < 1 || val > 9) {
			throw new IllegalArgumentException();
		}

		if (isSet(val)) {
			candList.remove(new Integer(val));
		}
		candidates &= (~bitMask(val));
	}
	
	public int size() {
		return candList.size();
	}
	
	public boolean isSet(int val) {
		if (val < 1 || val > 9) {
			throw new IllegalArgumentException();
		}
		
		return (candidates & bitMask(val)) != 0;
	}
}
