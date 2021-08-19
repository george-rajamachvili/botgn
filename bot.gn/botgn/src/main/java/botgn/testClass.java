package botgn;

import java.util.ArrayList;
import java.util.Collections;

public class testClass
{
	static ArrayList<Integer> seedArray = new ArrayList<Integer>();
	static ArrayList<Integer> testArray = new ArrayList<Integer>();
	
	public static void main(String[] args)
	{
		// Generate and randomize arrays
		for(int i = 0; i < 6; i++)
		{
			seedArray.add(i);
		}
		
		for(int i = 0; i < 6; i++)
		{
			testArray.add(i);
		}
		Collections.shuffle(seedArray); 
		
		// Print testArray
		for(int i = 0; i < 6; i++)
		{
			System.out.print(testArray.get(i) + " ");
		}
		
		// Do the swaps
		swap(testArray, seedArray)
		
		
	}
	
	public void swap(ArrayList<Integer> test, ArrayList<Integer> seed)
	{
		
	}

}
