import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Represents all constants
interface Constants {
  int WSWIDTH = 925;
  int WSHEIGHT = 375;
  int CARDWIDTH = 50;
  int CARDHEIGHT = 70;
  int CARDFONTSIZE = 15;
  int POINTS = 100;
  int DECKSIZE = 52;
  Color BLUEBACKGROUND = new Color(50, 135, 175); 
  Color GOLDTEXT = new Color(225, 215, 0);
  WorldImage SCENEBACKGROUND = new RectangleImage(WSWIDTH, WSHEIGHT, "solid", BLUEBACKGROUND);
  WorldImage CARDBACKGROUND = new OverlayImage(new RectangleImage(CARDWIDTH, CARDHEIGHT, "outline",
      Color.DARK_GRAY), new RectangleImage(CARDWIDTH, CARDHEIGHT, "solid", Color.WHITE));
  int TICKPERIOD = 1;
  WorldImage CARDBACKING = new OverlayImage(new FromFileImage("src/back.png"), CARDBACKGROUND);
}

//Represents a single Card
class Card implements Constants {
  int rank;//1-13 Inclusive
  String suit;//♣, ♦, ♥, or ♠
  Color color;//Either RED or BLACK
  boolean faceUp; //is this card face up (true) or down (false);
  int x; // x position of the center of this card
  int y; // y position of the center of this card

  Card(int rank, String suit, Color color, boolean faceUp) {
    this.rank = rank;
    this.suit = suit;
    this.color = color;
    this.faceUp = faceUp;
  }

  Card(int rank, String suit, Color color, boolean faceUp, int x, int y) {
    this.rank = rank;
    this.suit = suit;
    this.color = color;
    this.faceUp = faceUp;
    this.x = x;
    this.y = y;
  }

  //Determine if this Card is a pair with the given other Card
  public boolean isPair(Card other) {
    return this.rank == other.rank && this.color.equals(other.color)
        && !this.suit.equals(other.suit);
  }

  //Return 1 if face up, 0 is face down
  public int countActive() {
    if (this.faceUp) {
      return 1;
    }
    else {
      return 0;
    }
  }

  //sets X and Y of this card based on position in randomized list
  public Card setPosition(int positionTracker) {
    int newX = 0;
    int newY = 0;
    newX = 40 + ((positionTracker) % (DECKSIZE / 4)) * 70;
    if (positionTracker < DECKSIZE * 0.25) {
      newY = 50;
    }
    else if (positionTracker < DECKSIZE * 0.5) {
      newY = 50 + 90;
    }
    else if (positionTracker < DECKSIZE * 0.75) {
      newY = 50 + 90 * 2;
    }
    else if (positionTracker < DECKSIZE) {
      newY = 50 + 90 * 3;
    }
    return new Card(this.rank, this.suit, this.color, this.faceUp, newX, newY);
  }

  //Draw this card as a WorldImage
  public WorldImage drawCard() {
    if (faceUp) {
      WorldImage info = new TextImage(String.valueOf(this.rank) + " " + this.suit, 
          CARDFONTSIZE, this.color);
      if (this.rank == 1) {
        info = new TextImage("A " + this.suit, CARDFONTSIZE, this.color);
      }
      if (this.rank == 11) {
        info = new TextImage("J " + this.suit, CARDFONTSIZE, this.color);
      }
      if (this.rank == 12) {
        info = new TextImage("Q " + this.suit, CARDFONTSIZE, this.color);
      }
      if (this.rank == 13) {
        info = new TextImage("K " + this.suit, CARDFONTSIZE, this.color);
      }
      return new OverlayImage(info, CARDBACKGROUND);
    }
    else {
      return CARDBACKING;
    }
  }

  //Determine is the given posn is inside this card
  public boolean wasClicked(Posn pos) {
    boolean xThreshold = Math.abs(this.x - pos.x) <= CARDWIDTH / 2;
    boolean yThreshold = Math.abs(this.y - pos.y) <= CARDHEIGHT / 2;
    return xThreshold && yThreshold;
  }
}

//Represents a game of Concentration
class Concentration extends World implements Constants {
  Random rand;
  ArrayList<Card> deck;//Represents a list of all cards excluding found pairs
  ArrayList<Card> active;//Represents a list of found pairs
  int scorePoints;
  int steps;
  int timeInTicks;

  Concentration() {
    this.rand = new Random();
    this.deck = this.initDeck(rand); //setting deck to a random assortment of cards
  }

  Concentration(int seed) {
    this.rand = new Random(seed); 
    this.deck = this.initDeck(rand); //setting deck to a seeded random assortment of cards
  }

  //Set this deck to a new random deck based on the given seed
  //EFFECT: Initialize this active, scorePoints, steps, and timeStart to their starting value
  public ArrayList<Card> initDeck(Random rand) {
    this.active = new ArrayList<Card>();
    this.scorePoints = 0;
    this.steps = DECKSIZE / 2;
    this.timeInTicks = 0;

    ArrayList<Card> newDeck = new ArrayList<Card>();
    ArrayList<Card> randomizedDeck = new ArrayList<Card>();
    int positionTracker = 0;

    for (int i = 1; i < DECKSIZE / 4 + 1; i++) {
      newDeck.add(new Card(i, "♥", Color.RED, false));
      newDeck.add(new Card(i, "♦", Color.RED, false));
      newDeck.add(new Card(i, "♣", Color.BLACK, false));
      newDeck.add(new Card(i, "♠", Color.BLACK, false));
    }

    while (newDeck.size() > 0 && positionTracker < DECKSIZE) {
      int indexToRemove = rand.nextInt(newDeck.size());
      randomizedDeck.add(newDeck.remove(indexToRemove).setPosition(positionTracker));
      positionTracker = positionTracker + 1;
    }
    return randomizedDeck;
  }

  //EFFECT: Add the basic text and background to the given WorldScene
  public void initScene(WorldScene ws) {
    ws.placeImageXY(SCENEBACKGROUND, WSWIDTH / 2, WSHEIGHT / 2);
    ws.placeImageXY(new TextImage("CONCENTRATION", 13, FontStyle.BOLD_ITALIC, 
        GOLDTEXT), WSWIDTH / 2, WSHEIGHT - 367);
    ws.placeImageXY(new TextImage("Points: " + String.valueOf(this.scorePoints), 12,
        FontStyle.BOLD, GOLDTEXT), WSWIDTH / 2, WSHEIGHT - 10);
    ws.placeImageXY(new TextImage("Pairs Left: " + String.valueOf(this.steps), 12,
        FontStyle.BOLD, GOLDTEXT), WSWIDTH - 55, WSHEIGHT - 10);
    this.clock(ws);
  }

  //Print out the deck of cards, with 4 rows of 13 cards onto a WorldScene
  public WorldScene makeScene() {
    WorldScene ws = new WorldScene(WSWIDTH, WSHEIGHT);
    this.initScene(ws);

    for (Card c : deck) {
      ws.placeImageXY(c.drawCard(), c.x, c.y);
    }
    for (Card p : active) {
      ws.placeImageXY(p.drawCard(), p.x, p.y);
    }
    return ws;
  }

  //EFFECT: If the given k is "r", restart the board a new random state, and "s" to solve the
  //entire board, and "f" to flip over the pair of the current card(s).
  public void onKeyEvent(String k) {
    if (k.equals("r") || k.equals("R")) {
      this.deck = initDeck(this.rand);
    }
    if (k.equals("S") || k.equals("s")) {
      this.allUp();
    }
    if (k.equals("F") || k.equals("f")) {
      this.findCard();
    }
  }

  //EFFECT: Flips over the pair of the currently active card(s), add them to this active list
  //and removes them from this deck
  public void findCard() {
    for (Card c : this.deck) {
      if (c.faceUp && !this.active.remove(c)) {
        this.findPair(c);
        break;
      }
    }
    for (Card k : this.active) {
      this.deck.remove(k);
    }
  }

  //EFFECT: Flips over the pair of the currently active card(s)
  public void findPair(Card given) {
    for (Card c : this.deck) {
      if (c.isPair(given)) {
        c.faceUp = true;
        given.faceUp = true;
        this.active.add(c);
        this.active.add(given);
      }
    }
  }

  //EFFECT: Flips up every card in this deck
  public void allUp() {
    for (Card c : this.deck) {
      c.faceUp = true;
    }
  }

  //EFFECT: Determine if the given pos is inside one of the cards in this deck, flip if yes, and
  //then determine if any other active cards are its pair, and count the number of cards in this
  //active list
  public void onMouseClicked(Posn pos) { 
    int tally = 0;
    for (Card ac : deck) {
      tally += ac.countActive();
    }
    this.checkClick(pos, tally);
  }

  //EFFECT: Determine if the given pos is inside one of the cards in this deck, flip if yes, and
  //then determine if any other active cards are its pair
  public void checkClick(Posn pos, int tally) {    
    for (Card c : this.deck) {
      if (c.wasClicked(pos) && !c.faceUp) {
        if (tally == 0) {
          c.faceUp = true;
          tally += 1;
        }
        else if (tally == 1) {
          c.faceUp = true;
          if (this.anyPairs(c)) {
            tally = 0;
          }
          else {
            tally = 2;
          }
        }
        else if (tally == 2) {
          this.allDown();
          this.checkClick(pos, 0);
        }
      }
    }
    for (Card p : this.active) {
      this.deck.remove(p);
    }
  }

  //EFFECT: Flips every card in this deck down as long as it's not in this active list
  public void allDown() {
    for (Card c : this.deck) {
      if (!this.active.remove(c)) {
        c.faceUp = false;
      }
    }
  }

  //Determine if there is a pair in this deck with the given card
  //EFFECT: If any card is a pair with the given, add them to this active and remove from this deck
  public boolean anyPairs(Card given) {
    for (Card c : this.deck) {
      if (given.isPair(c) && c.faceUp && given.faceUp) {
        this.active.add(c);
        this.active.add(given);
        this.scorePoints += POINTS;
        this.steps -= 1;
        return true;
      }
    }
    return false;
  }

  //Determine if the game is over
  public boolean isGameOver() {
    return this.steps == 0;
  }


  //Determine if the world ends, and produce the final WorldScene if the world has ended
  public WorldEnd worldEnds() {
    if (this.isGameOver()) {
      return new WorldEnd(true, this.makeFinal());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  public void onTick() {
    this.timeInTicks = this.timeInTicks + 1;
  }

  //EFFECT: Add the time elapsed since the beginning of the game to the given WorldScene
  public void clock(WorldScene ws) {
    int totalSeconds = this.timeInTicks * TICKPERIOD;
    int hours = totalSeconds / 3600;
    int minutes = (totalSeconds % 3600) / 60;
    int seconds = totalSeconds % 60;
    String timeString =  "Time: " + String.format("%02d:%02d:%02d", hours, minutes, seconds);
    if (isGameOver()) {
      ws.placeImageXY(new TextImage(timeString, 15, FontStyle.BOLD, GOLDTEXT), 
          WSWIDTH / 2, WSHEIGHT / 2 + 30);
    }
    else {
      ws.placeImageXY(new TextImage(timeString, 12, FontStyle.BOLD, 
          GOLDTEXT), WSWIDTH - 865, WSHEIGHT - 10);
    }
  }

  //Make the final WorldScene for the end of the game
  public WorldScene makeFinal() {
    WorldScene ws = new WorldScene(WSWIDTH, WSHEIGHT);
    ws.placeImageXY(SCENEBACKGROUND, WSWIDTH / 2, WSHEIGHT / 2);
    ws.placeImageXY(new TextImage("CONGRATS!!!", 30, FontStyle.BOLD, GOLDTEXT), 
        WSWIDTH / 2, WSHEIGHT / 2);
    ws.placeImageXY(new TextImage(":D", 18, FontStyle.BOLD, GOLDTEXT), 
        WSWIDTH / 2, WSHEIGHT / 2 + 55);
    //We don't know if these party poppers are different on Macs
    ws.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 25, 25);
    ws.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), WSWIDTH - 25, 25);
    ws.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 25, WSHEIGHT - 25);
    ws.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), WSWIDTH - 25, WSHEIGHT - 25);
    this.clock(ws);
    return ws;
  }
}

class ExamplesConcentration implements Constants {
  Card c1;
  Card c2;
  Card c3;
  Card c4;
  Card c5;

  Card c6;
  Card c7;
  Card c8;
  Card c9;

  Concentration d1;
  Concentration d2;
  Concentration d1Dupe;
  Concentration d2Dupe;
  Concentration d3;
  Concentration d3Dupe;
  Concentration d4;
  Concentration d4Dupe;
  Concentration d5;
  Concentration d5Dupe;

  ArrayList<Card> deckTest;
  ArrayList<Card> deckTest2;
  ArrayList<Card> deckTest3;

  WorldScene ws1;
  WorldScene ws2;

  void initData() {
    c1 = new Card(1, "♥", Color.RED, true);
    c2 = new Card(1, "gdfgf", Color.RED, false);
    c3 = new Card(1, "♦", Color.RED, true);
    c4 = new Card(1, " ♦ ", Color.RED, false);
    c5 = new Card(3, "♣", Color.BLACK, true);

    c6 = new Card(6, "♠", Color.BLACK, false, 40, 50);
    c7 = new Card(9, "♠", Color.BLACK, false, 300, 300);
    c8 = new Card(1, " ♦ ", Color.RED, true);
    c9 = new Card(11, "♦", Color.RED, true);

    d1 = new Concentration(1);
    d1Dupe = new Concentration(1);
    d1Dupe.deck = d1Dupe.initDeck(d1Dupe.rand);

    d2 = new Concentration(2);
    d2Dupe = new Concentration(2);
    d2Dupe.deck = d2Dupe.initDeck(d2Dupe.rand);

    deckTest = new ArrayList<Card>(Arrays.asList(this.c7));
    d3 = new Concentration(3);
    this.d3.deck = deckTest;
    d3Dupe = new Concentration(3);
    this.d3Dupe.deck = deckTest;

    deckTest2 = new ArrayList<Card>(Arrays.asList(this.c9));
    d4 = new Concentration(4);
    this.d4.deck = deckTest2;
    d4Dupe = new Concentration(4);
    this.d4Dupe.deck = deckTest2;

    deckTest3 = new ArrayList<Card>(Arrays.asList(this.c5));
    d5 = new Concentration(4);
    this.d5.deck = deckTest3;
    d5Dupe = new Concentration(4);
    this.d5Dupe.deck = deckTest3;

    ws1 = new WorldScene(WSWIDTH, WSHEIGHT);
    ws2 = new WorldScene(1000, 1000);
  }

  void testInitDeck(Tester t) {
    //InitDeck is already called when this example is made
    this.initData();
    t.checkExpect(this.d1.initDeck(new Random(1)), this.d1.deck);
    t.checkExpect(this.d2.initDeck(new Random(2)), this.d2.deck);
    t.checkExpect(this.d2.initDeck(new Random(1)), this.d1.deck);
    t.checkExpect(this.d1.initDeck(new Random(2)), this.d2.deck);

    this.initData();
    this.d1.initDeck(new Random(1));
    t.checkExpect(this.d1.active, new ArrayList<Card>());
    t.checkExpect(this.d1.rand, new Random(1));
    t.checkExpect(this.d1.steps, 26);
    t.checkExpect(this.d1.timeInTicks, 0);
    t.checkExpect(this.d1.scorePoints, 0);

    this.initData();
    this.d2.initDeck(new Random(2));
    t.checkExpect(this.d2.active, new ArrayList<Card>());
    t.checkExpect(this.d2.rand, new Random(2));
    t.checkExpect(this.d2.steps, 26);
    t.checkExpect(this.d2.timeInTicks, 0);
    t.checkExpect(this.d2.scorePoints, 0);
  }

  void testOnKeyEvent(Tester t) {
    this.initData();
    this.d1.onKeyEvent("l");
    t.checkExpect(this.d1, new Concentration(1));

    this.initData();
    this.d2.onKeyEvent("k");
    t.checkExpect(this.d2, new Concentration(2));

    this.initData();
    this.d1.onKeyEvent("r");
    t.checkExpect(this.d1.deck, this.d1Dupe.deck);

    this.initData();
    this.d1.onKeyEvent("R");
    t.checkExpect(this.d1.deck, this.d1Dupe.deck);

    this.initData();
    this.d2.onKeyEvent("r");
    t.checkExpect(this.d2.deck, this.d2Dupe.deck);

    this.initData();
    this.d2.onKeyEvent("R");
    t.checkExpect(this.d2.deck, this.d2Dupe.deck);

    this.initData();
    this.d1.onKeyEvent("S");
    for (Card c: this.d1.deck) {
      t.checkExpect(c.faceUp, true);
    }

    this.initData();
    this.d1.onKeyEvent("S");
    t.checkExpect(this.d1.deck.get(0), new Card(5, "♦", Color.RED, true, 40, 50));
    t.checkExpect(this.d1.deck.get(2), new Card(13, "♦", Color.RED, true, 180, 50));
    t.checkExpect(this.d1.deck.get(4), new Card(5, "♥", Color.RED, true, 320, 50));
    t.checkExpect(this.d1.deck.get(6), new Card(2, "♦", Color.RED, true, 460, 50));
    t.checkExpect(this.d1.deck.get(8), new Card(11, "♦", Color.RED, true, 600, 50));
    t.checkExpect(this.d1.deck.get(10), new Card(7, "♣", Color.BLACK, true, 740, 50));

    this.initData();
    this.d1.onKeyEvent("s");
    for (Card c: this.d1.deck) {
      t.checkExpect(c.faceUp, true);
    }

    this.initData();
    this.d1.onKeyEvent("s");
    t.checkExpect(this.d1.deck.get(0), new Card(5, "♦", Color.RED, true, 40, 50));
    t.checkExpect(this.d1.deck.get(2), new Card(13, "♦", Color.RED, true, 180, 50));
    t.checkExpect(this.d1.deck.get(4), new Card(5, "♥", Color.RED, true, 320, 50));
    t.checkExpect(this.d1.deck.get(6), new Card(2, "♦", Color.RED, true, 460, 50));
    t.checkExpect(this.d1.deck.get(8), new Card(11, "♦", Color.RED, true, 600, 50));
    t.checkExpect(this.d1.deck.get(10), new Card(7, "♣", Color.BLACK, true, 740, 50));

    this.initData();
    this.d2.onKeyEvent("s");
    for (Card c: this.d2.deck) {
      t.checkExpect(c.faceUp, true);
    }

    this.initData();
    this.d2.onKeyEvent("s");
    t.checkExpect(this.d2.deck.get(0), new Card(12, "♥", Color.RED, true, 40, 50));
    t.checkExpect(this.d2.deck.get(2), new Card(11, "♦", Color.RED, true, 180, 50));
    t.checkExpect(this.d2.deck.get(4), new Card(6, "♠", Color.BLACK, true, 320, 50));
    t.checkExpect(this.d2.deck.get(6), new Card(2, "♣", Color.BLACK, true, 460, 50));
    t.checkExpect(this.d2.deck.get(8), new Card(5, "♠", Color.BLACK, true, 600, 50));
    t.checkExpect(this.d2.deck.get(10), new Card(3, "♦" , Color.RED, true, 740, 50));

    this.initData();
    this.d2.onKeyEvent("S");
    for (Card c: this.d2.deck) {
      t.checkExpect(c.faceUp, true);
    }

    this.initData();
    this.d2.onKeyEvent("S");
    t.checkExpect(this.d2.deck.get(0), new Card(12, "♥", Color.RED, true, 40, 50));
    t.checkExpect(this.d2.deck.get(2), new Card(11, "♦", Color.RED, true, 180, 50));
    t.checkExpect(this.d2.deck.get(4), new Card(6, "♠", Color.BLACK, true, 320, 50));
    t.checkExpect(this.d2.deck.get(6), new Card(2, "♣", Color.BLACK, true, 460, 50));
    t.checkExpect(this.d2.deck.get(8), new Card(5, "♠", Color.BLACK, true, 600, 50));
    t.checkExpect(this.d2.deck.get(10), new Card(3, "♦" , Color.RED, true, 740, 50));

    this.initData();
    this.d1.deck.get(0).faceUp = true;
    t.checkExpect(this.d1.deck.get(0).faceUp, true);
    t.checkExpect(this.d1.deck.get(17).faceUp, false);
    this.d1.onKeyEvent("f");
    //IT SHOULD HAVE ADDED THEM TO ACTIVE AND REMOVED THEM FROM DECK
    t.checkExpect(this.d1.active.get(0).faceUp, true);
    t.checkExpect(this.d1.active.get(1).faceUp, true);
    t.checkExpect(this.d1.active, new ArrayList<Card>(Arrays.asList(new Card(5, "♥", Color.RED,
        true, 320, 50), new Card(5, "♦", Color.RED, true, 40, 50))));
    t.checkExpect(this.d1.deck.get(0).faceUp, false);
    t.checkExpect(this.d1.deck.get(17).faceUp, false);

    this.initData();
    this.d1.deck.get(0).faceUp = true;
    t.checkExpect(this.d1.deck.get(0).faceUp, true);
    t.checkExpect(this.d1.deck.get(17).faceUp, false);
    this.d1.onKeyEvent("F");
    //IT SHOULD HAVE ADDED THEM TO ACTIVE AND REMOVED THEM FROM DECK
    t.checkExpect(this.d1.active.get(0).faceUp, true);
    t.checkExpect(this.d1.active.get(1).faceUp, true);
    t.checkExpect(this.d1.active, new ArrayList<Card>(Arrays.asList(new Card(5, "♥", Color.RED,
        true, 320, 50), new Card(5, "♦", Color.RED, true, 40, 50))));
    t.checkExpect(this.d1.deck.get(0).faceUp, false);
    t.checkExpect(this.d1.deck.get(17).faceUp, false);

    this.initData();
    this.d2.deck.get(45).faceUp = true;
    t.checkExpect(this.d2.deck.get(45).faceUp, true);
    t.checkExpect(this.d2.deck.get(44).faceUp, false);
    this.d2.onKeyEvent("F");
    //IT SHOULD HAVE ADDED THEM TO ACTIVE AND REMOVED THEM FROM DECK
    t.checkExpect(this.d2.active, new ArrayList<Card>(Arrays.asList(new Card(5, "♠", Color.BLACK,
        true, 600, 50), new Card(5, "♣", Color.BLACK, true, 460, 320))));
    t.checkExpect(this.d2.active.get(0).faceUp, true);
    t.checkExpect(this.d2.active.get(1).faceUp, true);
    t.checkExpect(this.d2.deck.get(44).faceUp, false);
    t.checkExpect(this.d2.deck.get(45).faceUp, false);

    this.initData();
    this.d2.deck.get(45).faceUp = true;
    t.checkExpect(this.d2.deck.get(45).faceUp, true);
    t.checkExpect(this.d2.deck.get(44).faceUp, false);
    this.d2.onKeyEvent("f");
    //IT SHOULD HAVE ADDED THEM TO ACTIVE AND REMOVED THEM FROM DECK
    t.checkExpect(this.d2.active, new ArrayList<Card>(Arrays.asList(new Card(5, "♠", Color.BLACK,
        true, 600, 50), new Card(5, "♣", Color.BLACK, true, 460, 320))));
    t.checkExpect(this.d2.active.get(0).faceUp, true);
    t.checkExpect(this.d2.active.get(1).faceUp, true);
    t.checkExpect(this.d2.deck.get(44).faceUp, false);
    t.checkExpect(this.d2.deck.get(45).faceUp, false);

  }

  void testInitScene(Tester t) {
    this.initData();
    WorldScene wsTest2 = new WorldScene(WSWIDTH, WSHEIGHT);
    t.checkExpect(ws1, new WorldScene(WSWIDTH, WSHEIGHT));
    t.checkExpect(wsTest2, new WorldScene(WSWIDTH, WSHEIGHT));
    d3.initScene(this.ws1);
    wsTest2.placeImageXY(SCENEBACKGROUND, 462, 187);
    wsTest2.placeImageXY(new TextImage("CONCENTRATION", 13, FontStyle.BOLD_ITALIC, 
        GOLDTEXT), 462, 8);
    wsTest2.placeImageXY(new TextImage("Points: 0", 12,
        FontStyle.BOLD, GOLDTEXT), 462, 365);
    wsTest2.placeImageXY(new TextImage("Pairs Left: 26", 12,
        FontStyle.BOLD, GOLDTEXT), 870, 365);
    wsTest2.placeImageXY(new TextImage("Time: 00:00:00",
        12, FontStyle.BOLD, GOLDTEXT), 60, 365);
    t.checkExpect(this.ws1, wsTest2);

    this.initData();
    WorldScene wsTest3 = new WorldScene(1000, 1000);
    t.checkExpect(this.ws2, new WorldScene(1000, 1000));
    t.checkExpect(wsTest3, new WorldScene(1000, 1000));
    d3.initScene(this.ws2);
    wsTest3.placeImageXY(SCENEBACKGROUND, 462, 187);
    wsTest3.placeImageXY(new TextImage("CONCENTRATION", 13, FontStyle.BOLD_ITALIC, 
        GOLDTEXT), 462, 8);
    wsTest3.placeImageXY(new TextImage("Points: 0", 12,
        FontStyle.BOLD, GOLDTEXT), 462, 365);
    wsTest3.placeImageXY(new TextImage("Pairs Left: 26", 12,
        FontStyle.BOLD, GOLDTEXT), 870, 365);
    wsTest3.placeImageXY(new TextImage("Time: 00:00:00",
        12, FontStyle.BOLD, GOLDTEXT), 60, 365);
    t.checkExpect(wsTest3, this.ws2);

    this.initData();
    WorldScene wsTest5 = new WorldScene(WSWIDTH, WSHEIGHT);
    WorldScene wsTest6 = new WorldScene(WSWIDTH, WSHEIGHT);
    t.checkExpect(wsTest5, new WorldScene(WSWIDTH, WSHEIGHT));
    t.checkExpect(wsTest6, new WorldScene(WSWIDTH, WSHEIGHT));
    d3.steps -= 1;
    d3.scorePoints += 100;
    d3.onTick();
    d3.initScene(wsTest5);
    wsTest6.placeImageXY(SCENEBACKGROUND, 462, 187);
    wsTest6.placeImageXY(new TextImage("CONCENTRATION", 13, FontStyle.BOLD_ITALIC, 
        GOLDTEXT), 462, 8);
    wsTest6.placeImageXY(new TextImage("Points: 100", 12,
        FontStyle.BOLD, GOLDTEXT), 462, 365);
    wsTest6.placeImageXY(new TextImage("Pairs Left: 25", 12,
        FontStyle.BOLD, GOLDTEXT), 870, 365);
    wsTest6.placeImageXY(new TextImage("Time: 00:00:01",
        12, FontStyle.BOLD, GOLDTEXT), 60, 365);
    t.checkExpect(wsTest5, wsTest6);
    
    this.initData();
    wsTest5 = new WorldScene(WSWIDTH, WSHEIGHT);
    wsTest6 = new WorldScene(WSWIDTH, WSHEIGHT);
    t.checkExpect(wsTest5, new WorldScene(WSWIDTH, WSHEIGHT));
    t.checkExpect(wsTest6, new WorldScene(WSWIDTH, WSHEIGHT));
    d3.steps -= 2;
    d3.scorePoints += 200;
    d3.onTick();
    d3.onTick();
    d3.initScene(wsTest5);
    wsTest6.placeImageXY(SCENEBACKGROUND, 462, 187);
    wsTest6.placeImageXY(new TextImage("CONCENTRATION", 13, FontStyle.BOLD_ITALIC, 
        GOLDTEXT), 462, 8);
    wsTest6.placeImageXY(new TextImage("Points: 200", 12,
        FontStyle.BOLD, GOLDTEXT), 462, 365);
    wsTest6.placeImageXY(new TextImage("Pairs Left: 24", 12,
        FontStyle.BOLD, GOLDTEXT), 870, 365);
    wsTest6.placeImageXY(new TextImage("Time: 00:00:02",
        12, FontStyle.BOLD, GOLDTEXT), 60, 365);
    t.checkExpect(wsTest5, wsTest6);
  }

  void testMakeScene(Tester t) {
    this.initData();
    this.d3Dupe.initScene(this.ws1);
    this.ws1.placeImageXY(CARDBACKING, 300, 300);
    t.checkExpect(this.d3.makeScene(), this.ws1);

    this.initData();
    this.d4Dupe.initScene(this.ws1);
    this.ws1.placeImageXY(new OverlayImage(new TextImage("J ♦", CARDFONTSIZE, Color.RED),
        CARDBACKGROUND), 0, 0);
    t.checkExpect(this.d4.makeScene(), this.ws1);

    this.initData();
    this.d5Dupe.initScene(this.ws1);
    this.ws1.placeImageXY(new OverlayImage(new TextImage("3 ♣", 
        CARDFONTSIZE, Color.BLACK), CARDBACKGROUND), 0, 0);
    t.checkExpect(d5.makeScene(), this.ws1);

    this.initData();
    this.d3Dupe.initScene(this.ws1);
    this.d3Dupe.steps = 0;
    this.ws1.placeImageXY(CARDBACKING, 300, 300);
    t.checkExpect(this.d3.makeScene(), this.ws1);
  }

  void testMakeFinal(Tester t) {
    this.initData();
    WorldScene wsTest = new WorldScene(WSWIDTH, WSHEIGHT);
    wsTest.placeImageXY(SCENEBACKGROUND, 462, 187);
    wsTest.placeImageXY(new TextImage("CONGRATS!!!", 30, FontStyle.BOLD, GOLDTEXT), 
        462, 187);
    wsTest.placeImageXY(new TextImage(":D", 18, FontStyle.BOLD, GOLDTEXT), 
        462, 242);
    wsTest.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 25, 25);
    wsTest.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 900, 25);
    wsTest.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 25, 350);
    wsTest.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 900, 350);
    wsTest.placeImageXY(new TextImage("Time: 00:00:00",
        12, FontStyle.BOLD, GOLDTEXT), 60, 365);
    t.checkExpect(d3.makeFinal(), wsTest);

    this.initData();
    d3.onTick();
    wsTest = new WorldScene(WSWIDTH, WSHEIGHT);
    wsTest.placeImageXY(SCENEBACKGROUND, 462, 187);
    wsTest.placeImageXY(new TextImage("CONGRATS!!!", 30, FontStyle.BOLD, GOLDTEXT), 
        462, 187);
    wsTest.placeImageXY(new TextImage(":D", 18, FontStyle.BOLD, GOLDTEXT), 
        462, 242);
    wsTest.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 25, 25);
    wsTest.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 900, 25);
    wsTest.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 25, 350);
    wsTest.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 900, 350);
    wsTest.placeImageXY(new TextImage("Time: 00:00:01",
        12, FontStyle.BOLD, GOLDTEXT), 60, 365);
    t.checkExpect(d3.makeFinal(), wsTest);

    d3.onTick();
    wsTest = new WorldScene(WSWIDTH, WSHEIGHT);
    wsTest.placeImageXY(SCENEBACKGROUND, 462, 187);
    wsTest.placeImageXY(new TextImage("CONGRATS!!!", 30, FontStyle.BOLD, GOLDTEXT), 
        462, 187);
    wsTest.placeImageXY(new TextImage(":D", 18, FontStyle.BOLD, GOLDTEXT), 
        462, 242);
    wsTest.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 25, 25);
    wsTest.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 900, 25);
    wsTest.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 25, 350);
    wsTest.placeImageXY(new TextImage("🎉", 35, GOLDTEXT), 900, 350);
    wsTest.placeImageXY(new TextImage("Time: 00:00:02",
        12, FontStyle.BOLD, GOLDTEXT), 60, 365);
    t.checkExpect(d3.makeFinal(), wsTest);
  }

  boolean testDrawCard(Tester t) {
    this.initData();
    return t.checkExpect(this.c1.drawCard(), new OverlayImage(new TextImage("A ♥", 
        CARDFONTSIZE, Color.RED), CARDBACKGROUND))
        && t.checkExpect(this.c2.drawCard(), CARDBACKING)
        && t.checkExpect(this.c3.drawCard(), new OverlayImage(new TextImage("A ♦",
            CARDFONTSIZE, Color.RED), CARDBACKGROUND))
        && t.checkExpect(this.c4.drawCard(), CARDBACKING)
        && t.checkExpect(this.c5.drawCard(), new OverlayImage(new TextImage("3 ♣", 
            CARDFONTSIZE, Color.BLACK), CARDBACKGROUND))
        && t.checkExpect(this.c6.drawCard(), CARDBACKING)
        && t.checkExpect(this.c7.drawCard(), CARDBACKING)
        && t.checkExpect(this.c8.drawCard(), new OverlayImage(new TextImage("A  ♦ ", 
            CARDFONTSIZE, Color.RED), CARDBACKGROUND))
        && t.checkExpect(this.c9.drawCard(), new OverlayImage(new TextImage("J ♦", 
            CARDFONTSIZE, Color.RED), CARDBACKGROUND));
  }

  void testFindCard(Tester t) {
    this.initData();
    this.d1.deck.get(0).faceUp = true;
    t.checkExpect(this.d1.deck.get(0).faceUp, true);
    t.checkExpect(this.d1.deck.get(17).faceUp, false);
    this.d1.findCard();
    //IT SHOULD HAVE ADDED THEM TO ACTIVE AND REMOVED THEM FROM DECK
    t.checkExpect(this.d1.deck.get(0).faceUp, false);
    t.checkExpect(this.d1.deck.get(17).faceUp, false);
    t.checkExpect(this.d1.active, new ArrayList<Card>(Arrays.asList(new Card(
        5, "♥", Color.RED, true, 320, 50), new Card(5, "♦", Color.RED, true, 40, 50))));
    t.checkExpect(this.d1.active.get(0).faceUp, true);
    t.checkExpect(this.d1.active.get(1).faceUp, true);

    this.initData();
    this.d2.deck.get(45).faceUp = true;
    t.checkExpect(this.d2.deck.get(45).faceUp, true);
    t.checkExpect(this.d2.deck.get(44).faceUp, false);
    this.d2.findCard();
    //IT SHOULD HAVE ADDED THEM TO ACTIVE AND REMOVED THEM FROM DECK
    t.checkExpect(this.d2.deck.get(45).faceUp, false);
    t.checkExpect(this.d2.deck.get(44).faceUp, false);
    t.checkExpect(this.d2.active, new ArrayList<Card>(Arrays.asList(new Card(
        5, "♠", Color.BLACK, true, 600, 50), new Card(5, "♣" , Color.BLACK, true, 460, 320))));
    t.checkExpect(this.d2.active.get(0).faceUp, true);
    t.checkExpect(this.d2.active.get(1).faceUp, true);
  }

  void testCheckClick(Tester t) {
    this.initData();
    //ONE CARD IS FACE UP, AND ITS PAIR IS CLICKED
    this.d1.deck.get(0).faceUp = true;
    t.checkExpect(this.d1.deck.get(0).faceUp, true);  
    t.checkExpect(this.d1.deck.get(17).faceUp, false);
    t.checkExpect(this.d1.active, new ArrayList<Card>());
    this.d1.checkClick(new Posn(320, 140), 1);
    t.checkExpect(this.d1.active, new ArrayList<Card>());
    t.checkExpect(this.d1.deck.get(0).faceUp, true);
    t.checkExpect(this.d1.deck.get(17).faceUp, true);

    this.initData();
    //TWO NON-PAIRS ARE FACE UP
    this.d1.deck.get(0).faceUp = true;
    this.d1.deck.get(1).faceUp = true;
    t.checkExpect(this.d1.deck.get(17).faceUp, false);
    t.checkExpect(this.d1.active, new ArrayList<Card>());
    this.d1.checkClick(new Posn(320, 140), 2);
    t.checkExpect(this.d1.deck.get(0).faceUp, false);
    t.checkExpect(this.d1.deck.get(1).faceUp, false);
    t.checkExpect(this.d1.deck.get(17).faceUp, true);

    this.initData();
    //TWO PAIRS ARE FACE UP
    this.d1.deck.get(0).faceUp = true;
    this.d1.deck.get(17).faceUp = true;
    t.checkExpect(this.d1.active, new ArrayList<Card>());
    this.d1.checkClick(new Posn(110, 50), 2);
    t.checkExpect(this.d1.active, new ArrayList<Card>());
    t.checkExpect(this.d1.deck.get(0).faceUp, false);
    t.checkExpect(this.d1.deck.get(1).faceUp, true);
    t.checkExpect(this.d1.deck.get(17).faceUp, false);

    this.initData();
    //NO CARDS ARE FACE UP
    t.checkExpect(this.d1.deck.get(17).faceUp, false);
    t.checkExpect(this.d1.active, new ArrayList<Card>());
    this.d1.checkClick(new Posn(320, 140), 0);
    t.checkExpect(this.d1.deck.get(17).faceUp, true);
    t.checkExpect(this.d1.active, new ArrayList<Card>());

    this.initData();
    //ONE CARD IS FACE UP, AND ITS NON-PAIR IS CLICKED
    this.d1.deck.get(0).faceUp = true;
    t.checkExpect(this.d1.deck.get(0).faceUp, true);  
    t.checkExpect(this.d1.deck.get(1).faceUp, false);
    t.checkExpect(this.d1.active, new ArrayList<Card>());
    this.d1.checkClick(new Posn(110, 50), 1);
    t.checkExpect(this.d1.active, new ArrayList<Card>());
    t.checkExpect(this.d1.deck.get(1).faceUp, true);
    t.checkExpect(this.d1.deck.get(0).faceUp, true);
  }

  void testFindPair(Tester t) {
    this.initData();
    t.checkExpect(this.d1.active, new ArrayList<Card>());
    this.d1.deck.get(0).faceUp = true;
    this.d1.findPair(this.d1.deck.get(0));
    t.checkExpect(this.d1.active, new ArrayList<Card>(Arrays.asList(new Card(
        5, "♥", Color.RED, true, 320, 50), new Card(
            5, "♦", Color.RED, true, 40, 50))));

    this.initData();
    t.checkExpect(this.d2.active, new ArrayList<Card>());
    this.d2.deck.get(51).faceUp = true;
    this.d2.findPair(this.d2.deck.get(51));
    t.checkExpect(this.d2.active, new ArrayList<Card>(Arrays.asList(new Card(
        6, "♦", Color.RED, true, 460, 230), new Card(
            6, "♥", Color.RED, true, 880, 320))));

    this.initData();
    t.checkExpect(this.d1.active, new ArrayList<Card>());
    this.d1.deck.get(51).faceUp = true;
    this.d1.findPair(this.d1.deck.get(51));
    t.checkExpect(this.d1.active, new ArrayList<Card>(Arrays.asList(new Card(
        5, "♣", Color.BLACK, true, 740, 140), new Card(
            5, "♠", Color.BLACK, true, 880, 320))));
  }

  void testOnMouseClicked(Tester t) {
    this.initData();
    t.checkExpect(this.d1.deck.get(0).faceUp, false);

    this.d1.onMouseClicked(new Posn(30, 50));
    t.checkExpect(this.d1.deck.get(0).faceUp, true);
    t.checkExpect(this.d1.active, new ArrayList<Card>());
    this.d1.onMouseClicked(new Posn(120, 70));
    t.checkExpect(this.d1.deck.get(1).faceUp, true);
    t.checkExpect(this.d1.deck.get(0).faceUp, true);
    t.checkExpect(this.d1.active, new ArrayList<Card>());

    this.initData();
    t.checkExpect(this.d2.deck.get(0).faceUp, false);
    this.d2.onMouseClicked(new Posn(60, 70));
    t.checkExpect(this.d2.deck.get(0).faceUp, true);

    this.initData();
    t.checkExpect(this.d2.deck.get(0).faceUp, false);
    t.checkExpect(this.d2.deck.get(1).faceUp, false);
    this.d2.onMouseClicked(new Posn(120, 70));
    t.checkExpect(this.d2.deck.get(0).faceUp, false);
    t.checkExpect(this.d2.deck.get(1).faceUp, true);

    this.initData();
    t.checkExpect(this.d1.deck.get(51).faceUp, false);
    t.checkExpect(this.d1.deck.get(23).faceUp, false);
    this.d1.onMouseClicked(new Posn(880, 320));
    t.checkExpect(this.d1.deck.get(51).faceUp, true);
    t.checkExpect(this.d1.deck.get(23).faceUp, false);
    this.d1.onMouseClicked(new Posn(740, 140));
    t.checkExpect(this.d1.active, new ArrayList<Card>(Arrays.asList(new Card(
        5, "♠", Color.BLACK, true, 880, 320), new Card(5, "♣", Color.BLACK, true, 740, 140))));

    this.initData();
    this.d1.deck.get(51).faceUp = true;
    t.checkExpect(this.d1.deck.get(51).faceUp, true);
    t.checkExpect(this.d1.deck.get(23).faceUp, false);
    this.d1.onMouseClicked(new Posn(880, 320));
    t.checkExpect(this.d1.deck.get(51).faceUp, true);
    t.checkExpect(this.d1.deck.get(23).faceUp, false);
    this.d1.onMouseClicked(new Posn(880, 320));
    t.checkExpect(this.d1.deck.get(51).faceUp, true);
    t.checkExpect(this.d1.deck.get(23).faceUp, false);
  }

  void testAllUp(Tester t) {
    //NEED TO HAVE THEM ALL FACE DOWN FOR TESTINGs
    this.initData();
    for (Card c: this.d1.deck) {
      t.checkExpect(c.faceUp, false);
    }
    t.checkExpect(this.d1.deck.get(0).faceUp, false);
    t.checkExpect(this.d1.deck.get(1).faceUp, false);
    t.checkExpect(this.d1.deck.get(2).faceUp, false);
    t.checkExpect(this.d1.deck.get(3).faceUp, false);
    t.checkExpect(this.d1.deck.get(4).faceUp, false);
    t.checkExpect(this.d1.deck.get(5).faceUp, false);
    t.checkExpect(this.d1.deck.get(6).faceUp, false);
    t.checkExpect(this.d1.deck.get(7).faceUp, false);
    t.checkExpect(this.d1.deck.get(8).faceUp, false);
    t.checkExpect(this.d1.deck.get(9).faceUp, false);
    t.checkExpect(this.d1.deck.get(10).faceUp, false);

    this.d1.allUp();
    for (Card c: this.d1.deck) {
      t.checkExpect(c.faceUp, true);
    }
    t.checkExpect(this.d1.deck.get(0).faceUp, true);
    t.checkExpect(this.d1.deck.get(1).faceUp, true);
    t.checkExpect(this.d1.deck.get(2).faceUp, true);
    t.checkExpect(this.d1.deck.get(3).faceUp, true);
    t.checkExpect(this.d1.deck.get(4).faceUp, true);
    t.checkExpect(this.d1.deck.get(5).faceUp, true);
    t.checkExpect(this.d1.deck.get(6).faceUp, true);
    t.checkExpect(this.d1.deck.get(7).faceUp, true);
    t.checkExpect(this.d1.deck.get(8).faceUp, true);
    t.checkExpect(this.d1.deck.get(9).faceUp, true);
    t.checkExpect(this.d1.deck.get(10).faceUp, true);

    this.initData();
    //NEED TO HAVE THEM ALL FACE DOWN FOR TESTING
    for (Card c: this.d2.deck) {
      t.checkExpect(c.faceUp, false);
    }
    t.checkExpect(this.d2.deck.get(0).faceUp, false);
    t.checkExpect(this.d2.deck.get(1).faceUp, false);
    t.checkExpect(this.d2.deck.get(2).faceUp, false);
    t.checkExpect(this.d2.deck.get(3).faceUp, false);
    t.checkExpect(this.d2.deck.get(4).faceUp, false);
    t.checkExpect(this.d2.deck.get(5).faceUp, false);
    t.checkExpect(this.d2.deck.get(6).faceUp, false);
    t.checkExpect(this.d2.deck.get(7).faceUp, false);
    t.checkExpect(this.d2.deck.get(8).faceUp, false);
    t.checkExpect(this.d2.deck.get(9).faceUp, false);
    t.checkExpect(this.d2.deck.get(10).faceUp, false);

    this.d2.allUp();
    for (Card c: this.d2.deck) {
      t.checkExpect(c.faceUp, true);
    }
    t.checkExpect(this.d2.deck.get(0).faceUp, true);
    t.checkExpect(this.d2.deck.get(1).faceUp, true);
    t.checkExpect(this.d2.deck.get(2).faceUp, true);
    t.checkExpect(this.d2.deck.get(3).faceUp, true);
    t.checkExpect(this.d2.deck.get(4).faceUp, true);
    t.checkExpect(this.d2.deck.get(5).faceUp, true);
    t.checkExpect(this.d2.deck.get(6).faceUp, true);
    t.checkExpect(this.d2.deck.get(7).faceUp, true);
    t.checkExpect(this.d2.deck.get(8).faceUp, true);
    t.checkExpect(this.d2.deck.get(9).faceUp, true);
    t.checkExpect(this.d2.deck.get(10).faceUp, true);
  }

  void testAllDown(Tester t) {
    //NEED TO HAVE THEM FACE UP TO COMPARE CARDS
    this.initData();
    for (Card c: this.d1.deck) {
      c.faceUp = true;
    }
    for (Card c: this.d1.deck) {
      t.checkExpect(c.faceUp, true);
    }
    t.checkExpect(this.d1.deck.get(0).faceUp, true);
    t.checkExpect(this.d1.deck.get(1).faceUp, true);
    t.checkExpect(this.d1.deck.get(2).faceUp, true);
    t.checkExpect(this.d1.deck.get(3).faceUp, true);
    t.checkExpect(this.d1.deck.get(4).faceUp, true);
    t.checkExpect(this.d1.deck.get(5).faceUp, true);
    t.checkExpect(this.d1.deck.get(6).faceUp, true);
    t.checkExpect(this.d1.deck.get(7).faceUp, true);
    t.checkExpect(this.d1.deck.get(8).faceUp, true);
    t.checkExpect(this.d1.deck.get(9).faceUp, true);
    t.checkExpect(this.d1.deck.get(10).faceUp, true);

    this.d1.allDown();
    for (Card c: this.d1.deck) {
      t.checkExpect(c.faceUp, false);
    }
    t.checkExpect(this.d1.deck.get(0).faceUp, false);
    t.checkExpect(this.d1.deck.get(1).faceUp, false);
    t.checkExpect(this.d1.deck.get(2).faceUp, false);
    t.checkExpect(this.d1.deck.get(3).faceUp, false);
    t.checkExpect(this.d1.deck.get(4).faceUp, false);
    t.checkExpect(this.d1.deck.get(5).faceUp, false);
    t.checkExpect(this.d1.deck.get(6).faceUp, false);
    t.checkExpect(this.d1.deck.get(7).faceUp, false);
    t.checkExpect(this.d1.deck.get(8).faceUp, false);
    t.checkExpect(this.d1.deck.get(9).faceUp, false);
    t.checkExpect(this.d1.deck.get(10).faceUp, false);

    this.initData();

    //NEED TO HAVE THEM FACE UP TO COMPARE CARDS
    for (Card c: this.d2.deck) {
      c.faceUp = true;
    }
    for (Card c: this.d2.deck) {
      t.checkExpect(c.faceUp, true);
    }
    t.checkExpect(this.d2.deck.get(0).faceUp, true);
    t.checkExpect(this.d2.deck.get(1).faceUp, true);
    t.checkExpect(this.d2.deck.get(2).faceUp, true);
    t.checkExpect(this.d2.deck.get(3).faceUp, true);
    t.checkExpect(this.d2.deck.get(4).faceUp, true);
    t.checkExpect(this.d2.deck.get(5).faceUp, true);
    t.checkExpect(this.d2.deck.get(6).faceUp, true);
    t.checkExpect(this.d2.deck.get(7).faceUp, true);
    t.checkExpect(this.d2.deck.get(8).faceUp, true);
    t.checkExpect(this.d2.deck.get(9).faceUp, true);
    t.checkExpect(this.d2.deck.get(10).faceUp, true);

    this.d2.allDown(); 
    for (Card c: this.d2.deck) {
      t.checkExpect(c.faceUp, false);
    }
    t.checkExpect(this.d2.deck.get(0).faceUp, false);
    t.checkExpect(this.d2.deck.get(1).faceUp, false);
    t.checkExpect(this.d2.deck.get(2).faceUp, false);
    t.checkExpect(this.d2.deck.get(3).faceUp, false);
    t.checkExpect(this.d2.deck.get(4).faceUp, false);
    t.checkExpect(this.d2.deck.get(5).faceUp, false);
    t.checkExpect(this.d2.deck.get(6).faceUp, false);
    t.checkExpect(this.d2.deck.get(7).faceUp, false);
    t.checkExpect(this.d2.deck.get(8).faceUp, false);
    t.checkExpect(this.d2.deck.get(9).faceUp, false);
    t.checkExpect(this.d2.deck.get(10).faceUp, false);
  }

  void testAnyPairs(Tester t) {
    this.initData();
    this.d1.deck.get(1).faceUp = true;
    t.checkExpect(this.d1.anyPairs(this.d1.deck.get(1)), false);
    this.d1.deck.get(17).faceUp = true;
    t.checkExpect(this.d1.anyPairs(this.d1.deck.get(16)), false);
    t.checkExpect(this.d1.anyPairs(this.d1.deck.get(17)), true);
    t.checkExpect(this.d1.anyPairs(this.d1.deck.get(18)), false);
    t.checkExpect(this.d1.anyPairs(this.d1.deck.get(1)), true);

    this.initData();
    this.d1.deck.get(0).faceUp = true;
    t.checkExpect(this.d1.anyPairs(this.d1.deck.get(0)), false);
    this.d1.deck.get(4).faceUp = true;
    t.checkExpect(this.d1.anyPairs(this.d1.deck.get(4)), true);
    t.checkExpect(this.d1.anyPairs(this.d1.deck.get(5)), false);
    t.checkExpect(this.d1.anyPairs(this.d1.deck.get(3)), false);
    t.checkExpect(this.d1.anyPairs(this.d1.deck.get(0)), true);

    this.initData();
    this.d2.deck.get(14).faceUp = true;
    t.checkExpect(this.d2.anyPairs(this.d2.deck.get(14)), false);
    this.d2.deck.get(13).faceUp = true;
    t.checkExpect(this.d2.anyPairs(this.d2.deck.get(12)), false);
    t.checkExpect(this.d2.anyPairs(this.d2.deck.get(13)), true);
    t.checkExpect(this.d2.anyPairs(this.d2.deck.get(14)), true);

    //NEED TO HAVE THEM FACE UP TO COMPARE CARDS
    this.initData();
    for (Card c: this.d1.deck) {
      c.faceUp = true;
    }
    t.checkExpect(this.d1.anyPairs(this.c1), true);
    t.checkExpect(this.d1.anyPairs(this.c2), false);
    t.checkExpect(this.d1.anyPairs(this.c3), true);
    t.checkExpect(this.d1.anyPairs(this.c4), false);
    t.checkExpect(this.d1.anyPairs(this.c5), true);
    t.checkExpect(this.d1.anyPairs(this.c6), false);
    t.checkExpect(this.d1.anyPairs(this.c7), false);

    //NEED TO HAVE THEM FACE UP TO COMPARE CARDS
    this.initData();
    for (Card c: this.d2.deck) {
      c.faceUp = true;
    }
    t.checkExpect(this.d2.anyPairs(this.c1), true);
    t.checkExpect(this.d2.anyPairs(this.c2), false);
    t.checkExpect(this.d2.anyPairs(this.c3), true);
    t.checkExpect(this.d2.anyPairs(this.c4), false);
    t.checkExpect(this.d2.anyPairs(this.c5), true);
    t.checkExpect(this.d2.anyPairs(this.c6), false);
    t.checkExpect(this.d2.anyPairs(this.c7), false);
  }

  void testIsGameOver(Tester t) {
    this.initData();
    t.checkExpect(this.d1.isGameOver(), false);
    this.d1.steps = 0;
    t.checkExpect(this.d1.isGameOver(), true);

    this.initData();
    t.checkExpect(this.d2.isGameOver(), false);
    this.d2.steps = 0;
    t.checkExpect(this.d2.isGameOver(), true);

    this.initData(); 
    t.checkExpect(this.d1.isGameOver(), false);
    this.d1.steps = 1;
    t.checkExpect(this.d1.isGameOver(), false);

    this.initData(); 
    t.checkExpect(this.d2.isGameOver(), false);
    this.d2.steps = -1;
    t.checkExpect(this.d2.isGameOver(), false);
  }

  void testWorldEnds(Tester t) {
    this.initData();
    t.checkExpect(this.d1.worldEnds(), new WorldEnd(false, this.d1.makeScene()));
    this.d1.steps = 0;
    t.checkExpect(this.d1.worldEnds(), new WorldEnd(true, this.d1.makeFinal()));

    this.initData();
    t.checkExpect(this.d2.worldEnds(), new WorldEnd(false, this.d2.makeScene()));
    this.d2.steps = 0;
    t.checkExpect(this.d2.worldEnds(), new WorldEnd(true, this.d2.makeFinal()));

    this.initData(); 
    t.checkExpect(this.d1.worldEnds(), new WorldEnd(false, this.d1.makeScene()));
    this.d1.steps = 1;
    t.checkExpect(this.d1.worldEnds(), new WorldEnd(false, this.d1.makeScene()));

    this.initData(); 
    t.checkExpect(this.d2.worldEnds(), new WorldEnd(false, this.d2.makeScene()));
    this.d2.steps = -1;
    t.checkExpect(this.d2.worldEnds(), new WorldEnd(false, this.d2.makeScene()));
  }

  void testOnTick(Tester t) {
    this.initData();
    t.checkExpect(this.d1.timeInTicks, 0);
    this.d1.onTick();
    t.checkExpect(this.d1.timeInTicks, 1);
    this.d1.onTick();
    t.checkExpect(this.d1.timeInTicks, 2);
    this.d1.onTick();
    t.checkExpect(this.d1.timeInTicks, 3);

    this.initData();
    t.checkExpect(this.d2.timeInTicks, 0);
    this.d2.onTick();
    t.checkExpect(this.d2.timeInTicks, 1);
    this.d2.onTick();
    t.checkExpect(this.d2.timeInTicks, 2);
    this.d2.onTick();
    t.checkExpect(this.d2.timeInTicks, 3);

    this.initData();
    t.checkExpect(this.d2.timeInTicks, 0);
    this.d2.onTick();
    t.checkExpect(this.d2.timeInTicks, 1);
    this.d2.onTick();
    t.checkExpect(this.d2.timeInTicks, 2);
  }

  void testClock(Tester t) {
    this.initData();
    t.checkExpect(this.d1.timeInTicks, 0);
    this.d1.onTick();
    t.checkExpect(this.d1.timeInTicks, 1);
    this.d1.onTick();
    t.checkExpect(this.d1.timeInTicks, 2);
    this.d1.onTick();
    t.checkExpect(this.d1.timeInTicks, 3);
    this.d1.onTick();
    this.d1.onTick();
    t.checkExpect(this.d1.timeInTicks, 5);

    this.initData();
    t.checkExpect(this.d2.timeInTicks, 0);
    this.d2.onTick();
    t.checkExpect(this.d2.timeInTicks, 1);
    this.d2.onTick();
    t.checkExpect(this.d2.timeInTicks, 2);
    this.d2.onTick();
    t.checkExpect(this.d2.timeInTicks, 3);
    this.d2.onTick();
    this.d2.onTick();
    t.checkExpect(this.d2.timeInTicks, 5);
  }

  boolean testIsPair(Tester t) {
    this.initData();
    return t.checkExpect(this.c1.isPair(this.c2), true)
        && t.checkExpect(this.c1.isPair(this.c3), true)
        && t.checkExpect(this.c4.isPair(this.c1), true)
        && t.checkExpect(this.c1.isPair(this.c1), false)
        && t.checkExpect(this.c1.isPair(this.c5), false)
        && t.checkExpect(this.c1.isPair(this.c3), true)
        && t.checkExpect(this.c8.isPair(this.c3), true);
  }

  boolean testCountActive(Tester t) {
    this.initData();
    return t.checkExpect(this.c1.countActive(), 1)
        && t.checkExpect(this.c2.countActive(), 0)
        && t.checkExpect(this.c3.countActive(), 1)
        && t.checkExpect(this.c4.countActive(), 0)
        && t.checkExpect(this.c5.countActive(), 1)
        && t.checkExpect(this.c6.countActive(), 0)
        && t.checkExpect(this.c7.countActive(), 0)
        && t.checkExpect(this.c8.countActive(), 1)
        && t.checkExpect(this.c9.countActive(), 1);
  }

  boolean testSetPosition(Tester t) {
    this.initData();
    return t.checkExpect(this.c1.setPosition(0), 
        new Card(1, "♥", Color.RED, true, 40, 50))
        && t.checkExpect(this.c5.setPosition(11), 
            new Card(3, "♣", Color.BLACK, true, 810, 50))
        && t.checkExpect(this.c5.setPosition(12), 
            new Card(3, "♣", Color.BLACK, true, 880, 50))
        && t.checkExpect(this.c7.setPosition(-100), 
            new Card(9, "♠", Color.BLACK, false, -590, 50))
        && t.checkExpect(this.c2.setPosition(1), 
            new Card(1, "gdfgf", Color.RED, false, 110, 50));
  }

  boolean testWasClicked(Tester t) {
    this.initData();
    return t.checkExpect(this.c6.wasClicked(new Posn(41, 50)), true)
        && t.checkExpect(this.c6.wasClicked(new Posn(0, 0)), false)
        && t.checkExpect(this.c6.wasClicked(new Posn(40, 85)), true)
        && t.checkExpect(this.c6.wasClicked(new Posn(40, 15)), true)
        && t.checkExpect(this.c6.wasClicked(new Posn(40, 14)), false)
        && t.checkExpect(this.c6.wasClicked(new Posn(15, 50)), true)
        && t.checkExpect(this.c6.wasClicked(new Posn(65, 50)), true)
        && t.checkExpect(this.c6.wasClicked(new Posn(66, 50)), false)
        && t.checkExpect(this.c7.wasClicked(new Posn(0, 0)), false)
        && t.checkExpect(this.c7.wasClicked(new Posn(325, 335)), true)
        && t.checkExpect(this.c7.wasClicked(new Posn(275, 265)), true)
        && t.checkExpect(this.c7.wasClicked(new Posn(274, 265)), false);
  }

  void testGame(Tester t) {
    Concentration c = new Concentration();
    c.bigBang(WSWIDTH, WSHEIGHT, TICKPERIOD);
  }
}