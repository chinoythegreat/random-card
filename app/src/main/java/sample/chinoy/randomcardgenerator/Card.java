package sample.chinoy.randomcardgenerator;

import io.realm.RealmObject;

/**
 * Created by Chinoy on 17/02/16.
 */


public class Card extends RealmObject {

    private int cardNumber;
    private int cardSuit;

    public Card() { }

    public int getcardNumber() {
        return cardNumber;
    }

    public void setCardNumber(int cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getCardSuit() {
        return cardSuit;
    }

    public void setCardSuit(int cardSuit) {
        this.cardSuit = cardSuit;
    }

}
