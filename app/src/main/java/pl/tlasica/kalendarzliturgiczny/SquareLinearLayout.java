package pl.tlasica.okazje;

import android.content.Context;
import android.widget.LinearLayout;

public class SquareLinearLayout extends LinearLayout {

	public SquareLinearLayout(Context context) {
		super(context);
	}

	/* (non-Javadoc)
	 * @see android.widget.LinearLayout#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    int width = MeasureSpec.getSize(widthMeasureSpec);
	    int height = MeasureSpec.getSize(heightMeasureSpec);
	    int size = width > height ? height : width;
	    System.out.println("onMeasure");
	    setMeasuredDimension( size, size);
	}

	
	
}
