package connection;

import java.time.LocalDateTime;

public class Unique 
{	
	public static String ID = null;
	public static String generateID()
	{
		String ID_Unique = "";
		LocalDateTime ldt = LocalDateTime.now();
		ID_Unique = 
				String.valueOf(ldt.getYear()) + "_" +
				String.valueOf(ldt.getMonthValue()) + "_" +
				String.valueOf(ldt.getDayOfMonth()) + "_" +
				String.valueOf(ldt.getHour()) + "_" +
				String.valueOf(ldt.getMinute()) + "_" +
				String.valueOf(ldt.getSecond()) + "_" +
				String.valueOf(ldt.getNano() / 100000);
		
		return ID_Unique;
	}
}
