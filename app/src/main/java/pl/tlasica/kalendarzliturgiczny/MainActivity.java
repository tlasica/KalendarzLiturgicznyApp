package pl.tlasica.kalendarzliturgiczny;

import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;

public class MainActivity extends Activity {

	private BootstrapButton       mCurrDateButton;
	private TextView	mOccasionTextView;
	private Calendar	currDate;
	private String		currOccasion;
	private Occasions occasionsDict;
	private ShareActionProvider mShareActionProvider;

    private long lastUpdateMillis = 0;

    final String    APP_URL = "http://bit.ly/kallit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        mCurrDateButton = (BootstrapButton) findViewById( R.id.button_date_curr);
        mOccasionTextView = (TextView) findViewById( R.id.textview_occasion);
                    
        occasionsDict = new Occasions( new OccasionsDataFromDb(getApplicationContext()) );

        // register to see connectivity changes
        registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        AppRater rater = new AppRater(this);
        rater.appLaunched();
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getMenuInflater().inflate(R.menu.activity_main, menu);
			// Locate MenuItem with ShareActionProvider
		    MenuItem item = menu.findItem(R.id.menu_item_share);
		    // Fetch and store ShareActionProvider
		    mShareActionProvider = (ShareActionProvider) item.getActionProvider();
		    // Update Share Intent
		    if (currOccasion != null ) {
		    	updateShareIntent( currOccasion );
		    }    
		    // Return true to display menu
		    return super.onCreateOptionsMenu(menu);
		}
		else {
			return true;
		}
    }

	@Override
	protected void onStart() {
		super.onStart();
		today();
        Toast.makeText(this.getApplicationContext(), "Pukaj aby przewijać wersy", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		adjustDisplay();
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mConnReceiver);
        super.onDestroy();
    }


    public void onPrevDate(View view) {
        prevDay();
    }

    public void onNextDate(View view) {
        nextDay();
    }

    public void onCurrDate(View view) {
    	today();
    }
    
	private void today() {
		updateCurrentDate(Calendar.getInstance());
		updateOccasion();
	}

	private void prevDay() {
		Calendar prev = currDate;		
		prev.add(Calendar.DAY_OF_YEAR, -1);			
		updateCurrentDate(prev);
		updateOccasion();		
	}
	
	private void nextDay() {
		Calendar next = currDate;		
		next.add(Calendar.DAY_OF_YEAR, +1);			
		updateCurrentDate(next);
		updateOccasion();
	}

	private void adjustDisplay() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.d("DISPLAY", metrics.toString() );
        int xSize = Math.min(metrics.heightPixels, metrics.widthPixels);
        int ySize = Math.max(metrics.heightPixels, metrics.widthPixels);
        Log.d("DISPLAY", "xSize=" + xSize);
        Log.d("DISPLAY", "ySize=" + ySize);


        //adjust font size
        TextView textLabel = (TextView) findViewById(R.id.textview_label);
        TextView textOccasion = (TextView) findViewById( R.id.textview_occasion);

        float fontSize = ySize/24;

        Log.d("DISPLAY", "fontSize=" + fontSize);
    	textLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
    	textOccasion.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);                		
	}

    private String currentDateStr() {
        return DateFormat.getDateFormat(getApplicationContext()).format( currDate.getTime());

    }

    private String currentDateStrNoYear() {
        return DateFormat.format("E d.M",currDate).toString();

    }

	private void updateCurrentDate(Calendar day) {
		currDate = day;
		mCurrDateButton.setText( currentDateStr() );
	}

	void updateOccasion() {
		currOccasion = occasionsDict.getNextOccasion(currDate);
		mOccasionTextView.setText( currOccasion );
		updateShareIntent( currOccasion );
	}

	public void onClickOccasion(View view) {
		updateOccasion();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private String createShareContentText(String occ) {
        String content = "Kalendarz Liturgiczny na Twoim smartfonie: " + APP_URL;
		return content;
	}


	// Call to update the share intent
	private void updateShareIntent(String occ) {
		String subject = "Kalendarz Liturgiczny Android App";
		String content = createShareContentText(occ);
						
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, content);
		
	    //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);		
	    if (mShareActionProvider != null) {
	        mShareActionProvider.setShareIntent(intent);
	    }
	}

    public void addNewOccasion(MenuItem item) {
        Log.d("MENU", "addNewOccation()");
        // open dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.add_occasion_text));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.add_occasion_bttn_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d("ADD", "yes!");
                // Przygotowanie tekstu emaila
                String text = "Data: " + currentDateStr() + "\n" + "Tekst:";
                // open to send email
                Intent email = new Intent(Intent.ACTION_VIEW);
                email.setData(Uri.parse("mailto:"));
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"tomek@3kawki.pl"});
                email.putExtra(Intent.EXTRA_SUBJECT, "[KALENDARZ LITURGICZNY] Propozycja");
                email.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(email);
            }
        });
        builder.setNegativeButton(getString(R.string.add_occasion_bttn_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d("ADD", "cancelled");
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();



    }

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo netInfo = connManager.getActiveNetworkInfo();
            if (netInfo!=null && netInfo.isConnected()) {
                Log.d("NETWORK", "network is connected");

                long curr = System.currentTimeMillis();
                if (curr - lastUpdateMillis > 5 * 60 * 1000) {
                    Log.d("NETWORK", "starting data updater");
                    lastUpdateMillis = curr;
                    String msg = getString(R.string.updater_msg_inprogress);
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    new DataUpdater(updateSite(), getApplicationContext(), occasionsDict).execute();
                }

            }
            else {
                Log.d("NETWORK", "network is disconnected");
            }

        }
    };


    protected String updateSite() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        String site = "http://okazjedowypicia.herokuapp.com/assets/data/kalendarzliturgiczny/pl/";
        return site;
    }

}
