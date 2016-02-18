package sample.chinoy.randomcardgenerator;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;

// Slot machine animation
// http://www.techrepublic.com/blog/software-engineer/building-a-slot-machine-in-android-viewflipper-meet-gesture-detector/

public class MainActivity extends AppCompatActivity {

    private static final int mSpeed = 90;

    private int mCount;
    private int mCardNumber;
    private int mCardSuit;
    private Realm mCardDatabase;
    private String[] mSuitStrings;
    private ViewFlipper mCardViewFlipper;
    private ViewFlipper mNumberViewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        populateCardNumberFlipper();
        populateCardSuitFlipper();
        mSuitStrings = getResources().getStringArray(R.array.cards_array);

        mCardDatabase = Realm.getInstance(MainActivity.this);
    }

    private void populateCardNumberFlipper() {
        mNumberViewFlipper = (ViewFlipper) findViewById(R.id.number_view_flipper);
        String[] cardNumberStrings = getResources().getStringArray(R.array.card_number_array);
        for(int i = 0; i < cardNumberStrings.length; i++) {
            LayoutInflater inflater = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.card_number, null);

            mNumberViewFlipper.addView(view);
            TextView numberText = (TextView) view.findViewById(R.id.card_number);
            numberText.setText(cardNumberStrings[i]);
        }
    }

    private void populateCardSuitFlipper() {
        mCardViewFlipper = (ViewFlipper) findViewById(R.id.suit_view_flipper);
        int cardSuitInt[] = {
                R.drawable.suit_club,
                R.drawable.suit_diamond,
                R.drawable.suit_heart,
                R.drawable.suit_spade};
        for(int i = 0; i < cardSuitInt.length; i++) {
            LayoutInflater inflater = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.card_suit, null);

            mCardViewFlipper.addView(view);
            ImageView suitImage = (ImageView) view.findViewById(R.id.card_suit);
            suitImage.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, cardSuitInt[i]));
        }
    }

    public void onClickLayout(final View view) {
        mCardNumber = new Random().nextInt(14 - 1) + 1;
        mCardSuit = new Random().nextInt(5-1) + 1;
        mCount = 20;

        Handler h = new Handler();
        h.postDelayed(slotMachineRunnable, mSpeed);

        saveGeneratedCardToDatabase(mCardNumber, mCardSuit);
        Toast.makeText(MainActivity.this, mCardNumber + " of " + mSuitStrings[mCardSuit -1], Toast.LENGTH_SHORT).show();

        logAllGeneratedCards();
    }

    private void saveGeneratedCardToDatabase(int cardNumber, int cardSuit) {
        mCardDatabase.beginTransaction();
        Card card = mCardDatabase.createObject(Card.class);
        card.setCardNumber(cardNumber);
        card.setCardSuit(cardSuit);
        mCardDatabase.commitTransaction();
    }

    private void logAllGeneratedCards() {
        RealmResults<Card> cardResults =
                mCardDatabase.where(Card.class).findAll();
        Log.d("card", "num of cards: " + cardResults.size());

        for(Card card:cardResults) {
            Log.d("card", card.getcardNumber() + " of " + card.getCardSuit());
        }
    }

    private Runnable slotMachineRunnable = new Runnable() {

        @Override
        public void run() {
            upAnimation(mCardViewFlipper);
            upAnimation(mNumberViewFlipper);
            if (mCount>1) {
                Handler h = new Handler();
                h.postDelayed(slotMachineRunnable, mSpeed);
            } else {
                mCardViewFlipper.setDisplayedChild(mCardSuit -1);
                mNumberViewFlipper.setDisplayedChild(mCardNumber -1);
            }
        }

    };

    private void upAnimation(final ViewFlipper viewFlipper) {
        mCount--;
        Animation outToBottom = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 1.0f);
        outToBottom.setInterpolator(new AccelerateInterpolator());
        outToBottom.setDuration(mSpeed);
        Animation inFromTop = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromTop.setInterpolator(new AccelerateInterpolator());
        inFromTop.setDuration(mSpeed);
        viewFlipper.clearAnimation();
        viewFlipper.setInAnimation(inFromTop);
        viewFlipper.setOutAnimation(outToBottom);
        if (viewFlipper.getDisplayedChild()==0) {
            viewFlipper.setDisplayedChild(viewFlipper.getChildCount() - 1);
        } else {
            viewFlipper.showNext();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
