package botgn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class randTestClass
{
	static ArrayList<Integer> roles = new ArrayList<Integer>();
	static ArrayList<Integer> seedArray = new ArrayList<Integer>();
	
	static int[] topRoleCount = new int[5];

	public static void main(String[] args)
	{
		for(int i = 0; i < 100000; i++)
		{
			runTest();
		}
		
		printTopRoleCount();
	}

	private static void runTest()
	{
		// Populate & Randomize seed array
		for (int i = 1; i <= 6; i++)
		{
			seedArray.add(i);
		}
		Collections.shuffle(seedArray);
		
		// Populate regular array
		for (int i = 1; i <= 6; i++)
		{
			roles.add(i);
		}
		
		Random r = new Random(); 
		
		// The shuffle algorithm itself (working and randomized)
	    for (int i = 5; i > 0; i--) 
	    {
	        int change = r.nextInt(i);
	        
	        roles = swap(roles, i, change);
	    }
	    
	    findTopRole();
	    
	    seedArray.clear();
	    roles.clear();
	}

	private static void findTopRole()
	{
		topRoleCount[roles.get(0) - 2] = topRoleCount[roles.get(0) - 2] + 1;
	}

	// Basic swap function
	private static ArrayList<Integer> swap(ArrayList<Integer> role, int i, int change)
	{
		int buffer;
		
		buffer = role.get(i);
		role.set(i, role.get(change));
		role.set(change, buffer);
		
		return role;
	}
	
	private static void printTopRoleCount()
	{
		for(int i = 0; i < topRoleCount.length; i++)
		{
			System.out.print(topRoleCount[i] + ",");
		}
	}
}
