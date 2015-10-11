package cuenca.alejandro.com.nfcgame;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by alejandro on 11/10/15.
 */
public class CustomeTextView extends TextView {
    public CustomeTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/silkscreen.ttf");
        setTypeface(tf, 1);
    }

    public CustomeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /*public CustomeTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/
}
