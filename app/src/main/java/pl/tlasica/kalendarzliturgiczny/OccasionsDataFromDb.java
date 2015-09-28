package pl.tlasica.kalendarzliturgiczny;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class OccasionsDataFromDb implements OccasionsData {

	private Context context;
	
	public OccasionsDataFromDb(Context context) {
		this.context = context;
	}

	@Override
	public List<String> forDay(int dateNum) {
		DatabaseHelper db = DatabaseHelper.getInstance(context);
		Log.d("FROMDB", "Helper acquired");
		List<OneOccasion> occs = db.fetchDay(dateNum);
		List<String> res = new LinkedList<String>();
		for(OneOccasion o: occs) {
			res.add( o.text );
		}
		if (res.isEmpty()) {
			res.add(context.getString(R.string.celebration_internet));
			res.add(context.getString(R.string.celebration_author));
		}
		return res;
	}

}
