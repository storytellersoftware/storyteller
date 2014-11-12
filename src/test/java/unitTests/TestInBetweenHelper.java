package unitTests;

import static org.junit.Assert.*;

import ide.InBetweenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;


public class TestInBetweenHelper
{
	private static int NUM_OF_PROGRAMMER_BURSTS = 10000;
	private static int NUM_CONSECUTIVE_KEYSTOKES = 20;
	private static long RAND_SEED = 1234567890;
	private Random randomGenerator = new Random(RAND_SEED);
	
	public String assertNewStringInBetween(String num1, String num2)
	{
		//the value in between
		String inbetweenResult = InBetweenHelper.inbetween(num1, num2);
		
		//four combinations of num1 and num2
		//both null
		if(num1 == null && num2 == null)
		{
			//inbetweenResult should be a non-empty string
			assertNotNull(inbetweenResult);
			assertTrue(inbetweenResult.length() > 0);			
		}
		else if(num1 != null && num2 == null) //a first but not a second
		{
			//should be a non-empty string where num1 < inbetweenResult
			assertNotNull(inbetweenResult);
			assertTrue(inbetweenResult.length() > 0);
			assertTrue(num1.compareTo(inbetweenResult) < 0);			
		}
		else if(num1 == null && num2 != null) //a second but not a first
		{
			//should be a non-empty string where inbetweenResult < num2
			assertNotNull(inbetweenResult);
			assertTrue(inbetweenResult.length() > 0);
			assertTrue(inbetweenResult.compareTo(num2) < 0);
		}
		else if(num1 != null && num2 != null) //both strings are non-null
		{
			//should be a non-empty string where inbetweenResult < num2
			assertNotNull(inbetweenResult);
			assertTrue(inbetweenResult.length() > 0);
			assertTrue(num1.compareTo(inbetweenResult) < 0);
			assertTrue(inbetweenResult.compareTo(num2) < 0);
		}
		
		//return the new string in case any tests want to use it
		return inbetweenResult;
	}

	@Test
	public void testSameLengthNumbersSeparatedByOneInTheLastDigit() 
	{
		assertNewStringInBetween("0023", "0024");
		assertNewStringInBetween("123", "124");
		assertNewStringInBetween("2", "3");
		assertNewStringInBetween("9", "A");
		assertNewStringInBetween("Z", "a");
		assertNewStringInBetween("09", "0A");
		assertNewStringInBetween("BB", "BC");
		assertNewStringInBetween("1Z", "1a");
	}
	
	@Test
	public void testSameLengthNumbersSeparatedByMoreThanOneInTheLastDigit() 
	{
		assertNewStringInBetween("123", "128");
		assertNewStringInBetween("2", "4");
		assertNewStringInBetween("2", "8");
		assertNewStringInBetween("1199", "1242");
		assertNewStringInBetween("Z", "b");
		assertNewStringInBetween("09", "0B");
		assertNewStringInBetween("BB", "BD");
		assertNewStringInBetween("1Z", "1b");
	}
	
	@Test
	public void testFirstNumbersShorterThanSecond() 
	{
		assertNewStringInBetween("001", "0011");
		assertNewStringInBetween("001", "00101");
		assertNewStringInBetween("001", "001001");
		assertNewStringInBetween("1231", "125796");
		assertNewStringInBetween("2", "89253");
		assertNewStringInBetween("999", "9999");
		assertNewStringInBetween("9999999", "99999999999");
		assertNewStringInBetween("a", "c89253");
		assertNewStringInBetween("BB", "BBD");
		assertNewStringInBetween("zzzz", "zzzzzzzzzzz");
	}
	
	@Test
	public void testFirstNumbersLongerThanSecond() 
	{
		assertNewStringInBetween("0023", "2");
		assertNewStringInBetween("1241861", "1242");
		assertNewStringInBetween("1237861", "1242");
		assertNewStringInBetween("888888", "89");
		assertNewStringInBetween("aaaa", "ab");
	}
	
	@Test
	public void testNumbersThatWrapAround() 
	{
		assertNewStringInBetween("zzz", "zzz1");
		assertNewStringInBetween("zzz", "zzzz");
		assertNewStringInBetween("zzz", "zzz0001");
		assertNewStringInBetween("zzz", "zzzzzzz");
		assertNewStringInBetween("1241861", "1242");
		assertNewStringInBetween("1237861", "1242");
		assertNewStringInBetween("888888", "89");
		assertNewStringInBetween("aaaa", "ab");
	}
	
	@Test
	public void testNoFirstValue() 
	{
		System.out.println("\n~~Testing no first, small value~~\n\n");
		assertNewStringInBetween(null, "1234");
		assertNewStringInBetween(null, "1230");
		assertNewStringInBetween(null, "1000");
		assertNewStringInBetween(null, "1001");
		assertNewStringInBetween(null, "10");
		assertNewStringInBetween(null, "1");
		assertNewStringInBetween(null, "01");
		assertNewStringInBetween(null, "0001");
		assertNewStringInBetween(null, "990");
		assertNewStringInBetween(null, "230");
		assertNewStringInBetween(null, "2300");
		assertNewStringInBetween(null, "231");
		assertNewStringInBetween(null, "2301");
		assertNewStringInBetween(null, "2310");
		assertNewStringInBetween(null, "aaaa");
		assertNewStringInBetween(null, "AAAA");
	}

	@Test
	public void testNoSecondValue() 
	{
		System.out.println("\n~~Testing no second, large value~~\n\n");
		assertNewStringInBetween("1234", null);
		assertNewStringInBetween("999", null);
		assertNewStringInBetween("1000", null);
		assertNewStringInBetween("zzz", null);
		assertNewStringInBetween("z", null);
		assertNewStringInBetween("1", null);
		assertNewStringInBetween("01", null);
		assertNewStringInBetween("aBcD", null);
		assertNewStringInBetween("zz0", null);
	}
	
	@Test
	public void testDifferentStrings() 
	{
		System.out.println("\n~~Testing strings~~\n\n");
		assertNewStringInBetween("1231", "125796");
		assertNewStringInBetween("123", "124");
		assertNewStringInBetween("1237861", "1242");
		assertNewStringInBetween("1241861", "1242");
		assertNewStringInBetween("1199", "1242");
		assertNewStringInBetween("999", "9999");
		assertNewStringInBetween("9999999", "99999999999");
		assertNewStringInBetween("1", "2");
		assertNewStringInBetween("101", "2");
		assertNewStringInBetween("1", "201");
		
		assertNewStringInBetween("Z", "z");
		assertNewStringInBetween("a", "b");
		assertNewStringInBetween("a", "z");
		assertNewStringInBetween("zz", "zzz");
		
		assertNewStringInBetween("0", "999999999");
		assertNewStringInBetween("001", "002");
		assertNewStringInBetween("001", "0011");
		assertNewStringInBetween("001", "00101");
		assertNewStringInBetween("001", "001001");	
		assertNewStringInBetween("1446","642");
		assertNewStringInBetween("6426","643");
	}
	
	@Test
	public void testRandomInBetweens() 
	{
		System.out.println("\n~~Testing random numbers in between~~\n\n");

		//a list of all the generated nums
		List < String > vals = new ArrayList < String >();
		
		//values to pass into the test class
		String first = null;
		String second = null;
		
		//create the first value
		String inbetween = assertNewStringInBetween(first, second);

		//add it to the beginning of the list
		vals.add(inbetween);
		
		//add another value at the beginning
		inbetween = InBetweenHelper.inbetween(null, inbetween);
		vals.add(0, inbetween);
		
		//simulate NUM_OF_PROGRAMMER_BURSTS bursts of programmer activity
		for(int i = 0;i < NUM_OF_PROGRAMMER_BURSTS;i++)
		{
			//get a random position within the bounds of the list (minus one 
			//since we'll add one to it below, -1 is possible this represents 
			//adding to the starting position of the document)
			int pos = randomGenerator.nextInt(vals.size()) - 1;
			
			//get a random number of keystrokes (from 1 to NUM_CONSECUTIVE_KEYSTOKES)
			int numKeystrokes = randomGenerator.nextInt(NUM_CONSECUTIVE_KEYSTOKES) + 1;
			
			//for all the keystrokes
			for( int j = 0;j < numKeystrokes;j++)
			{
				//this represents the case when we add to the beginning of a non-empty file
				if(pos == -1)
				{
					//there is no id to the left
					first = null;
					//the first string is the second
					second = vals.get(0);

					//find a value in between
					//inbetween = InBetweenHelper.inbetween(first, second);
					inbetween = assertNewStringInBetween(first, second);
					
					//the inbetween value should be less than second
					assertTrue(inbetween.compareTo(second) < 0);
					
					//add the new value at the beginning
					vals.add(0, inbetween);
				}
				else if((pos + j + 1) >= vals.size() - 1) //add at the very end of a non-empty file
				{
					//the last id is the first
					first = vals.get(vals.size() - 1);
					//there is no id beyond the last
					second = null;

					//find a value in between
					//inbetween = InBetweenHelper.inbetween(first, second);
					inbetween = assertNewStringInBetween(first, second);
					
					//first should be less than the in between value
					assertTrue(first.compareTo(inbetween) < 0);
					
					//add the new value at the end
					vals.add(inbetween);
				}
				else //there are two actual values
				{					
					//pick two adjacent strings
					first = vals.get(pos + j);
					second = vals.get(pos + j + 1);

					//find a value in between
					//inbetween = InBetweenHelper.inbetween(first, second);
					inbetween = assertNewStringInBetween(first, second);
					
					//make sure that the new value is inbetween
					assertTrue(first.compareTo(inbetween) < 0);
					assertTrue(inbetween.compareTo(second) < 0);
					
					//add the new string in the correct position
					vals.add(pos + j + 1, inbetween);
				}
				
				//debug
				/*
				if(i % 1000 == 0)
				{
					System.out.println("F:" + first + " < M: " + inbetween + " < E:" + second);
				}
				*/
			}
		}
		
		//analysis
		int longest = Integer.MIN_VALUE;
		int shortest = Integer.MAX_VALUE;
		
		//holds the lengths of all the strings
		int sum = 0;
		
		//go through each id
		for(int i = 0;i < vals.size();i++)
		{
			//keep a running sum of the id's length
			sum = sum + vals.get(i).length();
			
			//if we have reached a new longest id
			if(vals.get(i).length() > longest)
			{
				longest = vals.get(i).length();
			}
			
			//if we have reached a new shortest id
			if(vals.get(i).length() < shortest)
			{
				shortest = vals.get(i).length();
			}
		}
		
		//print the results
		System.out.println("Number of strings: " + vals.size());
		System.out.println("Avg: " + (double)sum / vals.size());
		System.out.println("Longest: " + longest);
		System.out.println("Shortest: " + shortest);
	}
}
