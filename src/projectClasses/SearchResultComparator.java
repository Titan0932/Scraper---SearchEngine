package projectClasses;


import java.util.Comparator;

/**
 * It implements the Comparator interface, which is a Java interface that defines a method called
 * compare. The compare method takes two pagerank objects and returns an integer.
 * The integer is negative if the first object's score is less than the second object's score,
 * and positive it's greater
 * if the scores are equal then it returns the integer of the similar logic but uses the title of the objects for comparision
 */
public class SearchResultComparator implements Comparator<PagerankData> {

    /**
     * It compares two PagerankData objects and returns the result of the comparison.
     *
     * @param p1 The first object to be compared.
     * @param p2 The second object to compare.
     * @return 1 or -1 based on which is greater.
     */
    public int compare(PagerankData p1, PagerankData p2) {
        if (p1.getScore() == p2.getScore()) {
            return ((p1.getTitle()).compareTo(p2.getTitle()));
        } else if (p1.getScore() < p2.getScore()) {
            return 1;
        } else return -1;
    }
}
