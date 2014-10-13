package org.apache.flink.streaming.util.nextGenExtractor;

import static org.junit.Assert.*;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.invokable.operator.NextGenExtractor;
import org.junit.Before;
import org.junit.Test;

public class ConcatinatedExtractTest {

	private String[] testStringArray1 = { "1", "2", "3" };
	private int[] testIntArray1 = { 1, 2, 3 };
	private String[] testStringArray2 = { "4", "5", "6" };
	private int[] testIntArray2 = { 4, 5, 6 };
	private String[] testStringArray3 = { "7", "8", "9" };
	private int[] testIntArray3 = { 7, 8, 9 };
	private Tuple2<String[], int[]>[] testTuple2Array;
	private Tuple2<String[], int[]> testTuple2;
	private Tuple2<Tuple2<String[], int[]>, Tuple2<String[], int[]>[]> testData;

	@SuppressWarnings("unchecked")
	@Before
	public void setupData() {
		testTuple2Array = new Tuple2[2];
		testTuple2Array[0] = new Tuple2<String[], int[]>(testStringArray1, testIntArray2);
		testTuple2Array[1] = new Tuple2<String[], int[]>(testStringArray2, testIntArray1);

		testTuple2 = new Tuple2<String[], int[]>(testStringArray3, testIntArray3);

		testData = new Tuple2<Tuple2<String[], int[]>, Tuple2<String[], int[]>[]>(testTuple2,
				testTuple2Array);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void test1() {
		NextGenExtractor ext = new ConcatinatedExtract(new FieldFromTuple(0), new FieldFromTuple(1))
				.add(new FieldsFromArray(Integer.class, 2, 1, 0));
		int[] expected = { testIntArray3[2], testIntArray3[1], testIntArray3[0] };
		assertEquals(new Integer(expected[0]), ((Integer[]) ext.extract(testData))[0]);
		assertEquals(new Integer(expected[1]), ((Integer[]) ext.extract(testData))[1]);
		assertEquals(new Integer(expected[2]), ((Integer[]) ext.extract(testData))[2]);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void test2() {
		NextGenExtractor ext = new ConcatinatedExtract(new FieldFromTuple(1), // Tuple2<String[],int[]>[]
				new FieldsFromArray(Tuple2.class, 1)) // Tuple2<String[],int[]>[]
				.add(new FieldFromArray(0)) // Tuple2<String[],int[]>
				.add(new ArrayFromTuple(0)) // Object[] (Containing String[])
				.add(new FieldFromArray(0)) // String[]
				.add(new FieldFromArray(1)); // String

		String expected2 = testStringArray2[1];
		assertEquals(expected2, ext.extract(testData));

	}

}
