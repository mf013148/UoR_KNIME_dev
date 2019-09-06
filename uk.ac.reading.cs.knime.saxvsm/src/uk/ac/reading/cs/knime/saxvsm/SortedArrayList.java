package uk.ac.reading.cs.knime.saxvsm;

import java.util.ArrayList;

class SortedArrayList<T> extends ArrayList<T> {
	private static final long serialVersionUID = -1538198924211324781L;

	@SuppressWarnings("unchecked")
    public void insertSortedNoDuplicates(T value) {
		if(!contains(value)){
	        add(value);
	        Comparable<T> cmp = (Comparable<T>) value;
	        for (int i = size()-1; i > 0 && cmp.compareTo(get(i-1)) < 0; i--) {
	            T tmp = get(i);
	            set(i, get(i-1));
	            set(i-1, tmp);
	        }
		}
    }
}