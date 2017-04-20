package org.linguisto.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class VocabularyTestUtil {

    private Random random = new Random();

	public List<Pair<Integer, Integer>> getProportionalIntervals(int rankFrom, int rankTo, int intervalCount) {
		List<Pair<Integer, Integer>> ret = new ArrayList<Pair<Integer, Integer>>();
		for (int i = 0; i < intervalCount; i++) {
			int intervalFrom = rankFrom + i*(rankTo - rankFrom)/intervalCount;
			int intervalTo = rankFrom + (i+1)*(rankTo - rankFrom)/intervalCount;
			//System.out.println("Interval ["+intervalFrom+","+intervalTo+"]");
			ret.add(new ImmutablePair<Integer, Integer>(intervalFrom, intervalTo));
		}
		return ret;
	}

	public List<Pair<Integer, Integer>> getInverseProportionalIntervals(int rankFrom, int rankTo, int intervalCount) {
		List<Pair<Integer, Integer>> ret = new ArrayList<Pair<Integer, Integer>>();
		for (int i = 0; i < intervalCount; i++) {
			int intervalFrom = (int)(((long)intervalCount*rankFrom*rankTo)/(intervalCount*rankTo - i*(rankTo - rankFrom)));
			int intervalTo = (int)(((long)intervalCount*rankFrom*rankTo)/(intervalCount*rankTo - (i+1)*(rankTo - rankFrom)));
			//System.out.println("Interval ["+intervalFrom+","+intervalTo+"]");
			ret.add(new ImmutablePair<Integer, Integer>(intervalFrom, intervalTo));
		}
		return ret;
	}

	public List<Integer> getRandomWordRanksFromIntervals(List<Pair<Integer, Integer>> intervals) {
		List<Integer> ret = new ArrayList<Integer>();
		for (Pair<Integer, Integer> interval : intervals) {
			int rndRank = randomInt(interval.getLeft(), interval.getRight());
			//System.out.println("Interval ["+interval.getLeft()+","+interval.getRight()+"], value = "+rndRank);
			ret.add(rndRank);
		}
		return ret;
	}

	/**
     * @param min generated value. Can't be > then max
     * @param max generated value
     * @return values in range [min, max).
     */
	public int randomInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Wrong interval, min greater then max :[" + min + ", " + max + "]");
        }
        if (min == max) {
            return max;
        }

        return random.nextInt(max - min) + min;
    }
}
