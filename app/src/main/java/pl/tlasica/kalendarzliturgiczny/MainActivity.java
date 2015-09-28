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
import android.support.v4.view.GestureDetectorCompat;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

	//private static final String LINK_FACEBOOK = "http://facebook.com/okazjeapp";
	
	private TextView	mCurrDateTextView;
	private TextView	mOccasionTextView;
	private Calendar	currDate;
	private String		currOccasion;
	private Occasions occasionsDict;
	private ShareActionProvider mShareActionProvider;
	private GestureDetectorCompat 	mDetector;

    private long lastUpdateMillis = 0;

    final String    APP_URL = "http://bit.ly/okazjeapp";
    final String    PIC_URL = "https://raw.githubusercontent.com/tlasica/Okazje/master/icon-128.png";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        
		mDetector = new GestureDetectorCompat(this, new MyGestureListener() );
        
        mCurrDateTextView = (TextView) findViewById( R.id.textview_current_date);
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
        super.onDestroy();
    }


    @Override 
    public boolean onTouchEvent(MotionEvent event){ 
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }	
	
    public void today(View view) {
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

        // adjust card size to 50% of min(width,height)
        ViewGroup layout = (ViewGroup) findViewById(R.id.layout_card);
        ViewGroup.LayoutParams p = layout.getLayoutParams();
        int cardHeight = Math.round( ySize * 0.56f ); 
        p.height = cardHeight;
        layout.requestLayout();
        
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
		mCurrDateTextView.setText( currentDateStr() );
	}

	void updateOccasion() {
		currOccasion = occasionsDict.getRandomOccasion( currDate );
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
		String content = String.format("Okazja na %s:\n%s\n\n" +
				"Okazje Android App: %s\n", currentDateStr(), occ, APP_URL);
		return content;
	}


	// Call to update the share intent
	private void updateShareIntent(String occ) {
		String subject = "Okazja na " + currentDateStr();
		String content = createShareContentText(occ);
		Log.d("SHARE", content);
						
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
                String text = "Data: " + currentDateStr() + "\n" + "Tekst okazji:";
                // open to send email
                Intent email = new Intent(Intent.ACTION_VIEW);
                email.setData(Uri.parse("mailto:"));
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"tomek@3kawki.pl"});
                email.putExtra(Intent.EXTRA_SUBJECT, "[OKAZJE] Propozycja okazji");
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

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
	       
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, 
                float velocityX, float velocityY) {
        	float x1 = event1.getX();
        	float x2 = event2.getX();
        	
        	if (x2 - x1 > 150.0) {
        		prevDay();
        	}
        	if (x2 - x1 < -150.0) {
        		nextDay();
        	}
            return true;
        }
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

                // show fb share button
                findViewById(R.id.facebookShareButton).setVisibility(View.VISIBLE);
            }
            else {
                Log.d("NETWORK", "network is disconnected");
                // hide fb share button
                findViewById(R.id.facebookShareButton).setVisibility(View.INVISIBLE);
            }

        }
    };




    protected String updateSite() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        String site = "http://okazjedowypicia.herokuapp.com/assets/data/";
        if (language.equals("en")) return site + "en/";
        if (language.equals("pl")) return site;
        else return site;
    }

}
