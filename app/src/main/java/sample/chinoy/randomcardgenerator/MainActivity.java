package sample.chinoy.randomcardgenerator;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;

// Slot machine animation
// http://www.techrepublic.com/blog/software-engineer/building-a-slot-machine-in-android-viewflipper-meet-gesture-detector/

public class MainActivity extends AppCompatActivity {

    private static final int mSpeed = 90;

    private Boolean mClickable = true;
    private int mCount;
    private int mCardNumber;
    private int mCardSuit;
    private int[] mCardSuitDrawables;
    private Realm mCardDatabase;
    private RecyclerView mCardListView;
    private String[] mCardNumberStrings;
    private String[] mSuitStrings;
    private ViewFlipper mCardViewFlipper;
    private ViewFlipper mNumberViewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCardNumberStrings = getResources().getStringArray(R.array.card_number_array);
        mSuitStrings = getResources().getStringArray(R.array.cards_array);
        populateCardNumberFlipper();
        populateCardSuitFlipper();

        setupRecyclerList();
        mCardDatabase = Realm.getInstance(MainActivity.this);
    }

    private void populateCardNumberFlipper() {
        mNumberViewFlipper = (ViewFlipper) findViewById(R.id.number_view_flipper);
        for (int i = 0; i < mCardNumberStrings.length; i++) {
            LayoutInflater inflater = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.card_number, null);

            mNumberViewFlipper.addView(view);
            TextView numberText = (TextView) view.findViewById(R.id.card_number);
            numberText.setText(mCardNumberStrings[i]);
        }
    }

    private void populateCardSuitFlipper() {
        mCardViewFlipper = (ViewFlipper) findViewById(R.id.suit_view_flipper);
        mCardSuitDrawables = new int[]{
                R.drawable.suit_club,
                R.drawable.suit_diamond,
                R.drawable.suit_heart,
                R.drawable.suit_spade};
        for (int i = 0; i < mCardSuitDrawables.length; i++) {
            LayoutInflater inflater = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.card_suit, null);

            mCardViewFlipper.addView(view);
            ImageView suitImage = (ImageView) view.findViewById(R.id.card_suit);
            suitImage.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, mCardSuitDrawables[i]));
        }
    }

    private void setupRecyclerList() {
        mCardListView = (RecyclerView) findViewById(R.id.card_list);
        mCardListView.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mCardListView.setLayoutManager(llm);
    }

    public void onClickLayout(final View view) {
        if(!mClickable) {
            return;
        }
        mClickable = false;
        mCardNumber = new Random().nextInt(14 - 1) + 1;
        mCardSuit = new Random().nextInt(5 - 1) + 1;
        mCount = 20;

        Handler h = new Handler();
        h.postDelayed(slotMachineRunnable, mSpeed);

        saveGeneratedCardToDatabase(mCardNumber, mCardSuit);
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

        for (Card card : cardResults) {
            Log.d("card", card.getcardNumber() + " of " + card.getCardSuit());
        }
    }

    private Runnable slotMachineRunnable = new Runnable() {

        @Override
        public void run() {
            upAnimation(mCardViewFlipper);
            upAnimation(mNumberViewFlipper);
            if (mCount > 1) {
                Handler h = new Handler();
                h.postDelayed(slotMachineRunnable, mSpeed);
            } else {
                mCardViewFlipper.setDisplayedChild(mCardSuit - 1);
                mNumberViewFlipper.setDisplayedChild(mCardNumber - 1);
                mClickable = true;
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
        if (viewFlipper.getDisplayedChild() == 0) {
            viewFlipper.setDisplayedChild(viewFlipper.getChildCount() - 1);
        } else {
            viewFlipper.showNext();
        }
    }

    public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

        private List<Card> cardList;

        public CardAdapter(RealmResults<Card> cardList) {
            this.cardList = cardList;
        }

        @Override
        public int getItemCount() {
            return cardList.size();
        }

        @Override
        public void onBindViewHolder(CardAdapter.CardViewHolder holder, int position) {
            Card card = cardList.get(position);
            holder.cardNumber.setText(mCardNumberStrings[card.getcardNumber() - 1]);
            holder.cardSuit.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, mCardSuitDrawables[card.getCardSuit() - 1]));
            if(card.getCardSuit() - 1 == 0 || card.getCardSuit() - 1 == 3) {
                holder.cardNumber.setTextColor(getColor(android.R.color.black));
                holder.ofLabel.setTextColor(getColor(android.R.color.black));
            } else {
                holder.cardNumber.setTextColor(getColor(android.R.color.holo_red_dark));
                holder.ofLabel.setTextColor(getColor(android.R.color.holo_red_dark));
            }
        }

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.card_list_row, viewGroup, false);

            return new CardViewHolder(itemView);
        }

        public class CardViewHolder extends RecyclerView.ViewHolder {
            protected TextView cardNumber;
            protected TextView ofLabel;
            protected ImageView cardSuit;

            public CardViewHolder(View view) {
                super(view);
                cardNumber = (TextView) view.findViewById(R.id.card_number);
                ofLabel = (TextView) view.findViewById(R.id.of_label);
                cardSuit = (ImageView) view.findViewById(R.id.card_suit);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_list) {
            RealmResults<Card> cardResults = mCardDatabase.where(Card.class).findAll();

            if (cardResults.size() == 0) {
                Toast.makeText(MainActivity.this, "No generated cards yet!", Toast.LENGTH_SHORT).show();
            } else {
                final LinearLayout layout = (LinearLayout) findViewById(R.id.card_list_layout);
                layout.setVisibility(View.VISIBLE);

                CardAdapter adapter = new CardAdapter(cardResults);
                mCardListView.setAdapter(adapter);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
