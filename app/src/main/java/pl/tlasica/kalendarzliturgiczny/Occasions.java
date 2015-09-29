package pl.tlasica.kalendarzliturgiczny;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import android.util.Log;

public class Occasions {

	private static final String LOGTAG = "OCCASIONS";
	private static final List<String> UPS_STH_WENT_WRONG = Arrays.asList("Ups! No Occasions!");
	private int 							todayNum = 0;		//MMDD (bo lata przestępne)
	List<String>							todayOccasions;
	Random									random;
	private int 							lastOccasionIndex = 0;
	private OccasionsData 					dataProvider;
	boolean									enforceReload = false;
	
	public Occasions(OccasionsData data) {
		dataProvider = data;
		random = new Random( Calendar.getInstance().getTimeInMillis() );
	}
	
	public void enforceReload() {
		enforceReload = true;
	}
	
	public String getNextOccasion(Calendar cal) {
		int num = Occasions.dateToDayNum(cal);
		if (enforceReload || (todayNum != num)) {
			prepareDayForBrowsing( num );
			lastOccasionIndex = 0;
			enforceReload = false;
		}
		String occ = todayOccasions.get( lastOccasionIndex );
		nextOccasion();
		return occ;
	}
	
	private void prepareDayForBrowsing(int num) {
		Log.d(LOGTAG, "prepare data for " + String.valueOf( num ));
		todayNum = num;
		todayOccasions = dataProvider.forDay( todayNum );
		if (todayOccasions == null) {
			todayOccasions = UPS_STH_WENT_WRONG;
		}
	}
	
	private void nextOccasion() {
		lastOccasionIndex++;
		if (lastOccasionIndex>=todayOccasions.size()) {
			lastOccasionIndex = 0;
		}
	}
	
	public static int dateToDayNum(Calendar cal) {		
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH);
		return 100 * (month+1) + day;
	}
		
}
