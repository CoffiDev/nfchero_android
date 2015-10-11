package cuenca.alejandro.com.nfcgame;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import cuenca.alejandro.com.nfcgame.Volley.VolleySingleton;
import cuenca.alejandro.com.nfcgame.Models.*;

public class MainActivity extends Activity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    private NfcAdapter mNfcAdapter;

    @Bind(R.id.punohover)ImageView punohover;
    @Bind(R.id.image_life_bar) ImageView lifeBar;

    @Bind(R.id.time_counter) TextView timeCounter;
    @Bind(R.id.text_actions) TextView textActions;

    private Activity activity;
    private int nfcTagValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fresco.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        ButterKnife.bind(this);

        activity = this;

        Uri uri = Uri.parse("http://nfcg.herokuapp.com/images/12035409_10153719305827229_1005534528_n.gif");
        SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.card_image);

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
        .build();
        draweeView.setController(controller);


        new CountDownTimer(300000, 1000) {
            public void onTick(long millisUntilFinished) {
                timeCounter.setText(millisUntilFinished / 1000 + " s");
            }

            public void onFinish() {
                timeCounter.setText("done!");
                //TODO finish the game on 0 s
            }
        }.start();

        handleIntent(getIntent());
    }

    @OnClick(R.id.btn_action1)
    public void actionBtn1(){
        Toast.makeText(activity, "Apretaste el boton de golpe", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_action2)
    public void actionBtn2(){
        Toast.makeText(activity, "Apretaste el boton secundario", Toast.LENGTH_SHORT).show();
    }

    @OnTouch(R.id.btn_action1)
    public boolean changeimage(View v, MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            punohover.setVisibility(View.VISIBLE);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            punohover.setVisibility(View.INVISIBLE);
        }
        return false;
    }

    public void onNFCResponse(String value){
        volleyRequest(value);
    }

    public void volleyRequest(String value) {
        String url ="http://nfcg.herokuapp.com/game/";

        if(value == "")
            url += "hola";
        else
            url += value;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            parseResponse(response);
                        }catch (Exception e){}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity,"That didn't work!", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }


    public void parseResponse(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);

        JSONObject playerJSON = jsonResponse.getJSONObject("player");
        Player player = new Player(
                (String) playerJSON.getString("health"),
                (String) playerJSON.getString("damage"),
                null);

        Log.d("Player_logs", player.getHealth());
        lifeBar.getLayoutParams().width = 45 * (Integer.parseInt(player.getHealth()) -1 );
        lifeBar.getLayoutParams().height = 100;
        lifeBar.requestLayout();

    }

    /*
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    */
    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d("NFCExample", "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    public class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        private static final String TAG = "lol";

        //private TextView mTextView;

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                onNFCResponse(result);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* It's important, that the activity is in the foreground (resumed). Otherwise an IllegalStateException is thrown.*/
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /* Call this before onPause, otherwise an IllegalArgumentException is thrown as well. */
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }


    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

}
