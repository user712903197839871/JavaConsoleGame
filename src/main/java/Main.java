import org.jline.terminal.Terminal;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.LineReader;

import java.util.List;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;


// change the language as well

public class Main {
    public static void main(String[] args) {
        InputManager.init();

        GameRunner gameRunner = new GameRunner();

        gameRunner.bootGame();

        InputManager.close();
    }
}



/**
 * credits to gemini for this one, it is really something :)
 * jLine magic
 */
class InputManager {
    private static Terminal terminal;
    private static LineReader reader;
    private static String prompt = GameSettings.prompt;

    public static void init() {
        try {
            terminal = org.jline.terminal.TerminalBuilder.builder().system(true).build();
            terminal.enterRawMode();
            reader = LineReaderBuilder.builder().terminal(terminal).build();
        } catch (Exception e) {
            System.out.println("Error initializing input manager: " + e.getMessage());
        }

    }

    /**
     * gets a single letter from input without the need of pressing enter <br>
     * credits to gemini for this one, it is really something
     * @return returns the letter lowercased
     */
    public static String getLetter() {
        try {
            System.out.print(prompt);
            int input = terminal.reader().read();
            System.out.print(String.valueOf((char) input).toLowerCase());
            System.out.println();
            return String.valueOf((char) input).toLowerCase();
        } catch (Exception e) {
            System.err.println("Error reading input: " + e.getMessage());
            return " "; 
        }
    }

    /**
     * is a standart way of getting input from console, like scanner's readline
     * @return returns a line of user's input
     */
    public static String getNextLine() {
        return reader.readLine(prompt);
    }

    public static void close() {
        try {
            terminal.close();
        } catch (Exception e) {
            System.out.println("Error closing terminal: " + e.getMessage());
        }
    }
}


/**
 * the glue that glues these scraps together
 * responds for the game 
 */
class GameRunner {
    private boolean gameBooted = true;

    /**
     * captain represents the main player
     */
    public static HumanPlayer captain = new HumanPlayer();
    /**
     * the guest is a possible second player
     */
    public static HumanPlayer guest = new HumanPlayer();


    /**
     * Game loop runs here
     */
    public void bootGame() {
        String consoleInput = "";

        ScreenManager.printLoadingScreen();

        while (gameBooted) {
            ScreenManager.printMainMenu();
            consoleInput = InputManager.getLetter();

            processMainMenuOptions(consoleInput);
        }
    }

    /**
     * here runs the main menu
     * from here into Battle | Settings | Donate | Game modes
     * @param optionChosenString is the option chosen from main menu
     */
    private void processMainMenuOptions(String optionChosenString) {
        switch (optionChosenString) {
            // battle, choosing game mode
            case "1":
                chooseGameType();
                break;

            // settings
            case "2":
                ScreenManager.printSettingsScreen();
                break;

            // donate?
            case "3":
                System.out.println("Do stuff with donations");
                break;

            // game modes maybe game rules
            case "4":
                System.out.println("game modes info");
                break;

            // ... dont press 7!
            case "6":
                sixSevenMainMenuOption();
                break;

            // exit
            case "0":
                Helpers.slowType("BYE CAPTAIN");
                gameBooted = false;
                break;
        
            // cant read so pressed something else
            default:
                Helpers.printInvalidInputMessage();
                break;
        }
    }

    /** 
     * test na dalbaioba
    */
    private void sixSevenMainMenuOption() {
        Helpers.printMessageAndThreeDotsSlowly("CAPTAIN");
        Helpers.sleep(400);
        Helpers.printMessageAndThreeDotsSlowly("DON'T SAY THAT YOU");
        Helpers.sleep(800);

        Helpers.printMessageAndThreeDotsSlowly("CAPTAIN, FINISH WHAT MUST BE FINISHED");

        String isDalbaiob = InputManager.getLetter();

        if (isDalbaiob.equals("7")) {
            Helpers.printMessageAndThreeDotsSlowly("I KNOW WHAT KIND OF A MAN YOU ARE");
            ScreenManager.print67();
            Helpers.sleep(670);
        } else {
            Helpers.slowType("MEH ");
            Helpers.sleep(400);
            Helpers.slowType("WHATEVER");
        }
    }


    /**
     * here we choose a game type
     * either RandomBot | Algorithmic bot | PVP
     */
    private void chooseGameType() {
        boolean isChoosingGameType = true;
        String gameType;

        while (isChoosingGameType) {
            ScreenManager.chooseGameScreen();
            
            gameType = InputManager.getLetter();

            switch (gameType.toLowerCase()) {
                case "1":
                    // Random bot
                    MatchLoader.loadPVEMatch(captain, BotType.RandomBot);
                    break;

                case "2":
                    // algorithmic bot
                    Helpers.slowType("CAPTAIN THIS FEATURE ISN'T OUT YET");
                    break;

                case "3":
                    // ai stuff
                    Helpers.slowType("CAPTAIN THIS FEATURE ISN'T OUT YET");
                    break;

                case "4":
                    // pvp stuff
                    System.out.println("Run pvp");
                    System.out.println("idk");
                    System.out.println("PVP loop here");
                    break;

                case "0":
                case "e":
                    isChoosingGameType = false;
                    break;
            
                default:
                    Helpers.printInvalidInputMessage();
                    break;
            }

        }
    }

}

/**
 * responds for actual game loops and plays
 */
class MatchLoader {

    /**
     * playes a neverending loop of player vs bot
     * the main thing for pve
     * wanna run another type of bot? just call this with another bot type and thats it
     * @param captain the default player
     * @param botType the bot type
     */
    public static void loadPVEMatch(HumanPlayer captain, BotType botType) {
        boolean playing = true;
        boolean gameFinishedWell = true;

        Bot bot = null;
        
        switch (botType) {
            case RandomBot:
                bot = new RandomBot();
                break;

            case AlgorithmicBot:

                break;
        }


        while (playing) {
            GameModes gameMode = chooseRandomBot_Type();

            bot.changeBotGameMode(gameMode);

            GameSettings.sniperMode = gameMode == GameModes.Sniper;

            ScreenManager.printScore(captain.score, bot.score, captain.playerName, bot.playerName, "o");

            // play a game
            gameFinishedWell = playRound(captain, bot);

            // we quit if something went wrong (if someone ragequited)
            if (!gameFinishedWell) break;

            // ask if bro wanna play again
            if (!playAgain()) {
                break;
            }
        }
    }

    /**
     * playes a round between player1 and player2 from beggining till you lose <br>
     * player type can be any, bot human, woman, whatever!
     * @param player1
     * @param player2
     * @return returns wether the game finished normally, now it's boolean, could be expanded in future
     */
    private static boolean playRound(Player player1, Player player2) {

        player1.prepareForRound();
        player2.prepareForRound();

        ScreenManager.clearConsole();
        
        Helpers.printMessageAndThreeDotsSlowly("DRAMATICALLY PREPARING THE BATTLEFIELD");

        boolean gameOn = true;
        boolean player1ToMove = true;
        boolean player2ToMove = true;
        TurnResult turnResult;


        while (gameOn) {
            player1ToMove = true;
            player2ToMove = true;

            while (player1ToMove) {
                // player1 move
                turnResult = player1.makeTurn(player2);

                // process player move result
                switch (turnResult) {
                    case Win:
                        // round finished on player 1 win
                        printRoundEnd(player1, player2);
                        return true;

                    case GaveUp:
                        // begin a new round
                        // add points to other guy
                        player2.incrementScore();
                        return true;

                    case RageQuited:
                        // go to main menu
                        // say last guy to move is a looser
                        Helpers.slowType(player1.playerName + " is a looser");
                        return false;

                    case PassTurn:
                        player1ToMove = false;
                        break;

                    case Hit:
                        break;
                }
            }

            while (player2ToMove) {
                // player 2 move
                turnResult = player2.makeTurn(player1);
                switch (turnResult) {
                    case Win:
                        // round finished on player 2 win
                        printRoundEnd(player2, player1);
                        return true;

                    case GaveUp:
                        // begin a new round
                        // add points to last guy to move
                        player1.incrementScore();
                        return true;

                    case RageQuited:
                        // go to main menu
                        // say last guy to move is a looser
                        Helpers.slowType(player2.playerName + " is a looser");
                        return false;

                    case PassTurn:
                        player2ToMove = false;
                        break;
                    
                    case Hit:
                        break;
                }
            }
        }
        return false;
    }
    

    /**
     * prompts player to play again until given a valid input
     * @return true if we play again false if not
     */
    private static boolean playAgain() {
        String consoleInput;
        while (true) {
            Helpers.slowType("PLAY AGAIN? (y/n) ", false);
            consoleInput = InputManager.getLetter();

            if (consoleInput.toLowerCase().equals("y")) {
                return true;
            } else if (consoleInput.toLowerCase().equals("n")) {
                return false;
            } else {
                Helpers.printInvalidInputMessage();
            }
        }
    }

    /**
     * chooses random bot's game type, how many moves he has etc 
     * @return returns a gamemode to workwith in future
     */
    private static GameModes chooseRandomBot_Type() {
        String consoleInput;

        while (true) {
            ScreenManager.chooseGameModeScreen();
            
            consoleInput = InputManager.getLetter();

            switch (consoleInput.toLowerCase()) {
                case "1":
                    return GameModes.BotEasy;

                case "2":
                    return GameModes.BotMedium;

                case "3":
                    return GameModes.BotHard;

                case "4":
                    return GameModes.Sniper;

                case "0":
                case "e":
                    return null;
            
                default:
                    Helpers.printInvalidInputMessage();
                    break;
            }

        }
    }

    /**
     * prints a beautifull screen of data about scores/round end
     * @param player1 player1
     * @param player2 player2
     */
    private static void printRoundEnd(Player player1, Player player2) {
        Helpers.slowType("THE BATTLEFIELD IN THE END:");
        Helpers.sleep(500);
        Helpers.slowType(player1.playerName + "'S GRID");
        player1.printGrid();
        Helpers.sleep(1000);
        Helpers.slowType(player2.playerName + "'S GRID");
        player2.printGrid();
        ScreenManager.printScore(player1.score, player2.score, player1.playerName, player2.playerName, "o");
        
        Helpers.sleep(5000);
        Helpers.slowType("GAME OVER.");
        Helpers.sleep(2000);
    }


}


/**
 * an abstract class that has shared methods of all players
 */
abstract class Player {
    /**
     * objects on this' board <br>
     * objects like misses hits etc
     */
    protected List<BoardObject> boardObjects = new ArrayList<>();

    /**
     * player's grid with characters
     * a matrix containing each cell's data
     */
    protected List<List<String>> grid = new ArrayList<>();
    
    /**
     * the starting coordonates and the direction of a valid ship <br>
     * contains all valid position in which a ship can be placed
     */
    List<Pair<Coordonates, Integer>> validShipPositions;

    protected String playerName;

    protected int score = 0;

    protected int hitCount = 0;


    // grid related settings
    
    // each unit means a space
    protected int cellHorizontalPaddingValue = 4;
    
    // each unit means a newline
    protected int cellVerticalPaddingValue = 1;

    protected HashMap<Integer, Character> gridLegendLetters = new HashMap<>(Map.of(
        1, 'A',
        2, 'B',
        3, 'C',
        4, 'D',
        5, 'E',
        6, 'F',
        7, 'G',
        8, 'H',
        9, 'I',
        10, 'J'
    ));

    Player() {
        this.score = 0;
        this.hitCount = 0;
        this.playerName = "Player";
    }

    /**
     * declatation of function make turn
     * @param enemy the enemy object
     * @return the turn result, won, pass turn, gave up || quitted 
     */
    public TurnResult makeTurn(Player enemy) {
        return TurnResult.PassTurn;
    }


    /**
     * prepares player's fields for round
     */
    public void prepareForRound() {
        resetGrid();
        hitCount = 0;
    }
    
    /**
     * resets board objects
     */
    public void resetGrid() {
        boardObjects.clear();
    }


    // fields related stuff
    /**
     * increments player score <br>
     * maybe implement something like score>10 wont match or something
     */
    public void incrementScore() {
        score++;
    }

    /**
     * @return player's overall score
     */
    public int getScore() {
        return score;
    }

    /**
     * increments hits
     */
    public void incrementHitCount() {
        hitCount++;
    }

    /**
     * @return the hit count, how many hits we have
     */
    public int getHitCount() {
        return hitCount;
    }


    // grid related logic
    /**
     * fills THIS OBJECT's grid whit ITS BOARD_OBJECTS 
     */
    public void computeGrid() {
        grid = fillGrid(this.boardObjects);
    }

    /**
     * fills a arbitrary grid with DIFFERENT BOARD OBJECTS<br>
     * does not acess variables, or objects or stuff, works only with what it's given
     * @param differentBoardObjects boardObjects different from its own
     * @return return a filled matrix with characters
     */
    public List<List<String>> computeGrid(List<BoardObject> differentBoardObjects) {
        return fillGrid(differentBoardObjects);
    }

    /**
     * creation/initial creation of an empty grid
     * @return an matrix filled with watercharacters
     */
    private List<List<String>> fillEmptyGrid() {
        List<List<String>> grid = new ArrayList<>();

        for (int i = 0; i < GameSettings.gridSize; i++) {
            grid.add(new ArrayList<>());
            for (int j = 0; j < GameSettings.gridSize; j++) {
                grid.get(i).add(GameSettings.waterCharacter);
            }
        }

        return grid;
    }

    /**
     * fills the grid with characters
     * @param objectsToPrint represets the objects we need to add to matrix
     * @return returs a matrix, where each cell is a character, of ship miss, destroyed ship etc
     */
    protected List<List<String>> fillGrid(List<BoardObject> objectsToPrint) {
        List<List<String>> grid = fillEmptyGrid();

        // render board objects
        if (objectsToPrint.size() == 0) {
            // skip we do not add anything
        } else {
            // render ships first
            for (BoardObject object : objectsToPrint) {
                if (object instanceof Ship) {
                    if (object.direction == 1) {
                        int tempSize = object.size;
                        for (int i = object.coordonates.y(); i < GameSettings.gridSize; i++, tempSize--) {
                            if (tempSize == 0) break;
                            grid.get(i).set(object.coordonates.x(), object.graphic);
                        }
                    } else if (object.direction == 2) {
                        int tempSize = object.size;
                        for (int i = object.coordonates.x(); i < GameSettings.gridSize; i++, tempSize--) {
                            if (tempSize == 0) break;
                            grid.get(object.coordonates.y()).set(i, object.graphic);
                        }
                    }
                }
            }

            // render hits and misses
            for (BoardObject object : objectsToPrint) {
                if (object instanceof Miss || object instanceof Hit) {
                    grid.get(object.coordonates.y()).set(object.coordonates.x(), object.graphic);
                }
            }

            // on top of all that render the destroyed ships and mark the area around them
            for (BoardObject object : objectsToPrint) {
                if (object instanceof Ship && object.destroyed) {
                    // copies of coords to not modify the object's
                    int x = object.coordonates.x();
                    int y = object.coordonates.y();


                    // set the squares around as invalid stuff
                    for (int i = 0; i < object.size; i++) {
                        grid.get(y).set(x, object.graphic);

                        // up
                        if (
                            y > 0 &&
                            grid.get(y-1).get(x).equals(GameSettings.waterCharacter)
                        ) {
                            grid.get(y-1).set(x, GameSettings.spaceAroundDestroyedShipCharacter);
                        }
                        // down
                        if (
                            y < GameSettings.gridSize-1 &&
                            grid.get(y+1).get(x).equals(GameSettings.waterCharacter)
                        ) {
                            grid.get(y+1).set(x, GameSettings.spaceAroundDestroyedShipCharacter);
                        }
                        // left
                        if (
                            x > 0 &&
                            grid.get(y).get(x-1).equals(GameSettings.waterCharacter)
                        ) {
                            grid.get(y).set(x-1, GameSettings.spaceAroundDestroyedShipCharacter);
                        }
                        // right
                        if (
                            x < GameSettings.gridSize-1 &&
                            grid.get(y).get(x+1).equals(GameSettings.waterCharacter)
                        ) {
                            grid.get(y).set(x+1, GameSettings.spaceAroundDestroyedShipCharacter);
                        }
                        // diagonals left up
                        if (
                            x > 0 && y > 0 &&
                            grid.get(y-1).get(x-1).equals(GameSettings.waterCharacter)
                        ) {
                            grid.get(y-1).set(x-1, GameSettings.spaceAroundDestroyedShipCharacter);
                        }
                        // diagonals right up
                        if (
                            x < GameSettings.gridSize-1 && y > 0 &&
                            grid.get(y-1).get(x+1).equals(GameSettings.waterCharacter)
                        ) {
                            grid.get(y-1).set(x+1, GameSettings.spaceAroundDestroyedShipCharacter);
                        }
                        // diagonals left down
                        if (
                            x > 0 && y < GameSettings.gridSize-1 &&
                            grid.get(y+1).get(x-1).equals(GameSettings.waterCharacter)
                        ) {
                            grid.get(y+1).set(x-1, GameSettings.spaceAroundDestroyedShipCharacter);
                        }
                        // diagonals right down
                        if (
                            x < GameSettings.gridSize-1 && y < GameSettings.gridSize-1 &&
                            grid.get(y+1).get(x+1).equals(GameSettings.waterCharacter)
                        ) {
                            grid.get(y+1).set(x+1, GameSettings.spaceAroundDestroyedShipCharacter);
                        }

                        // increment pointers
                        x += (object.direction == 1) ? 0 : 1;
                        y += (object.direction == 1) ? 1 : 0;
                    }
                }
            }
        }


        return grid;
    }

    /**
     * computes/fills the OBJECT's grid<br>
     * then calls an internal function that formats the grid
     */
    public void printGrid() {
        computeGrid();
        printGrid(grid);
    }

    /**
     * a protected method to print the requested grid<br>
     * here we actually format each cell and print to the screen <br>
     * @param gridToPrint
     */
    protected void printGrid(List<List<String>> gridToPrint) {
        String gridHorizontalLineBorder = Colors.BORDER_COLOR +  "─".repeat(2*cellHorizontalPaddingValue) + "─" + Colors.RESET;
        String gridVerticalLineBorder = Colors.BORDER_COLOR + "│" + Colors.RESET;

        // paddings
        String stdSpace = " ".repeat(cellHorizontalPaddingValue);
        String horizontalPadding = " ".repeat(cellHorizontalPaddingValue);


        // BEHOLD, the worst variables names in history of programing
        // borders
        String borderTopBegin = Colors.BORDER_COLOR + "┌" + Colors.RESET;
        String borderTopMid = Colors.BORDER_COLOR + "┬" + Colors.RESET;
        String borderTopEnd = Colors.BORDER_COLOR + "┐" + Colors.RESET;

        String borderMidBegin = Colors.BORDER_COLOR + "├" + Colors.RESET;
        String borderMidMid = Colors.BORDER_COLOR + "┼" + Colors.RESET;
        String borderMidEnd = Colors.BORDER_COLOR + "┤" + Colors.RESET;

        String borderBottomBegin = Colors.BORDER_COLOR + "└" + Colors.RESET;
        String borderBottomMid = Colors.BORDER_COLOR + "┴" + Colors.RESET;
        String borderBottomEnd = Colors.BORDER_COLOR + "┘" + Colors.RESET;


        // format the entire horizontal line/border
        String line = stdSpace + stdSpace + borderMidBegin;
        for (int i = 0; i < GameSettings.gridSize; i++) {
            line = line + gridHorizontalLineBorder;
            if (i < GameSettings.gridSize-1) {
                line = line + borderMidMid;
            }
        }
        line = line + borderMidEnd;

        
        // top letters
        System.out.print(
            stdSpace + stdSpace + " "
        );
        for (int key : gridLegendLetters.keySet()) {
            if (key <= GameSettings.gridSize) {
                System.out.print(horizontalPadding + gridLegendLetters.get(key) + horizontalPadding + " ");
            }
        }
        System.out.println();


        // upper line, border top
        System.out.print(stdSpace + stdSpace + borderTopBegin + gridHorizontalLineBorder);
        for (int i = 0; i < GameSettings.gridSize-1; i++) {
            System.out.print(borderTopMid + gridHorizontalLineBorder);
        }
        System.out.println(borderTopEnd);


        // grid data
        // for each line
        for (int i = 0; i < GameSettings.gridSize; i++) {

            // format each line

            // first format padding
            String paddingTopBottom = "";
            paddingTopBottom = paddingTopBottom + stdSpace + stdSpace + gridVerticalLineBorder;
            for (int j = 0; j < GameSettings.gridSize; j++) {
                // it's water color this section in water color
                if (gridToPrint.get(i).get(j).equals(GameSettings.waterCharacter)) {
                    paddingTopBottom = paddingTopBottom + Colors.WATER_BACKGROUND_COLOR + 
                    horizontalPadding + horizontalPadding + " " + Colors.RESET;
                } 
                // it's ship color ship
                else if (gridToPrint.get(i).get(j).equals(GameSettings.shipCharacter)) {
                    paddingTopBottom = paddingTopBottom + Colors.SHIP_BACKGROUND_COLOR + 
                    horizontalPadding + horizontalPadding + " " + Colors.RESET;
                }
                // it's invalid ship color invalid ship
                else if (gridToPrint.get(i).get(j).equals(GameSettings.shipCharacterInvalid)) {
                    paddingTopBottom = paddingTopBottom + Colors.SHIP_INVALID_BACKGROUND_COLOR + 
                    horizontalPadding + horizontalPadding + " " + Colors.RESET;
                }
                // it's invalid board ship color invalid ship
                else if (gridToPrint.get(i).get(j).equals(GameSettings.shipCharacterInvalidBoard)) {
                    paddingTopBottom = paddingTopBottom + Colors.SHIP_INVALID_BOARD_BACKGROUND_COLOR + 
                    horizontalPadding + horizontalPadding + " " + Colors.RESET;
                }
                // it's ship destroyed color destroyed
                else if (gridToPrint.get(i).get(j).equals(GameSettings.shipDestroyedCharacter)) {
                    paddingTopBottom = paddingTopBottom + Colors.SHIP_DESTROYED_BACKGROUND_COLOR + 
                    horizontalPadding + horizontalPadding + " " + Colors.RESET;
                }
                // it's space around destroyed ship color space around destroyed
                else if (gridToPrint.get(i).get(j).equals(GameSettings.spaceAroundDestroyedShipCharacter)) {
                    paddingTopBottom = paddingTopBottom + Colors.SPACE_AROUND_DESTROYED_SHIP_BACKGROUND_COLOR + 
                    horizontalPadding + horizontalPadding + " " + Colors.RESET;
                }
                // it's a miss color miss
                else if (gridToPrint.get(i).get(j).equals(GameSettings.missCharacter)) {
                    paddingTopBottom = paddingTopBottom + Colors.MISS_BACKGROUND_COLOR + 
                    horizontalPadding + horizontalPadding + " " + Colors.RESET;
                }
                // it's hit color hit
                else if (gridToPrint.get(i).get(j).equals(GameSettings.hitCharacter)) {
                    paddingTopBottom = paddingTopBottom + Colors.HIT_BACKGROUND_COLOR + 
                    horizontalPadding + horizontalPadding + " " + Colors.RESET;
                }

                paddingTopBottom = paddingTopBottom + gridVerticalLineBorder;
            }

            // print vertical padding
            for (int j = 0; j < cellVerticalPaddingValue; j++) {
                System.out.println(paddingTopBottom);
            }

            // print cell data
            // print row number
            System.out.print(stdSpace);
            if (i+1 > 9) System.out.print("\b");
            System.out.print((i+1) + stdSpace + "\b" + gridVerticalLineBorder);

            // format cell&cell data
            for (int j = 0; j < GameSettings.gridSize; j++) {
                // water
                if (gridToPrint.get(i).get(j).equals(GameSettings.waterCharacter)) {
                    System.out.print(
                        Colors.WATER_BACKGROUND_COLOR + horizontalPadding + 
                        gridToPrint.get(i).get(j) + 
                        Colors.WATER_BACKGROUND_COLOR + horizontalPadding + Colors.RESET
                    );
                }
                // ship
                else if (gridToPrint.get(i).get(j).equals(GameSettings.shipCharacter)) {
                    System.out.print(
                        Colors.SHIP_BACKGROUND_COLOR + horizontalPadding + 
                        gridToPrint.get(i).get(j) + 
                        Colors.SHIP_BACKGROUND_COLOR + horizontalPadding + Colors.RESET
                    );
                }
                // invalid ship
                else if (gridToPrint.get(i).get(j).equals(GameSettings.shipCharacterInvalid)) {
                    System.out.print(
                        Colors.SHIP_INVALID_BACKGROUND_COLOR + horizontalPadding + 
                        gridToPrint.get(i).get(j) + 
                        Colors.SHIP_INVALID_BACKGROUND_COLOR + horizontalPadding + Colors.RESET
                    );
                }
                // invalid board
                else if (gridToPrint.get(i).get(j).equals(GameSettings.shipCharacterInvalidBoard)) {
                    System.out.print(
                        Colors.SHIP_INVALID_BOARD_BACKGROUND_COLOR + horizontalPadding + 
                        gridToPrint.get(i).get(j) + 
                        Colors.SHIP_INVALID_BOARD_BACKGROUND_COLOR + horizontalPadding + Colors.RESET
                    );
                }
                // ship destroyed
                else if (gridToPrint.get(i).get(j).equals(GameSettings.shipDestroyedCharacter)) {
                    System.out.print(
                        Colors.SHIP_DESTROYED_BACKGROUND_COLOR + horizontalPadding + 
                        gridToPrint.get(i).get(j) + 
                        Colors.SHIP_DESTROYED_BACKGROUND_COLOR + horizontalPadding + Colors.RESET
                    );
                }
                // ship destroyed space around
                else if (gridToPrint.get(i).get(j).equals(GameSettings.spaceAroundDestroyedShipCharacter)) {
                    System.out.print(
                        Colors.SPACE_AROUND_DESTROYED_SHIP_BACKGROUND_COLOR + horizontalPadding + 
                        gridToPrint.get(i).get(j) + 
                        Colors.SPACE_AROUND_DESTROYED_SHIP_BACKGROUND_COLOR + horizontalPadding + Colors.RESET
                    );
                }
                // miss
                else if (gridToPrint.get(i).get(j).equals(GameSettings.missCharacter)) {
                    System.out.print(
                        Colors.MISS_BACKGROUND_COLOR + horizontalPadding + 
                        gridToPrint.get(i).get(j) + 
                        Colors.MISS_BACKGROUND_COLOR + horizontalPadding + Colors.RESET
                    );
                }
                // hit
                else if (gridToPrint.get(i).get(j).equals(GameSettings.hitCharacter)) {
                    System.out.print(
                        Colors.HIT_BACKGROUND_COLOR + horizontalPadding + 
                        gridToPrint.get(i).get(j) + 
                        Colors.HIT_BACKGROUND_COLOR + horizontalPadding + Colors.RESET
                    );
                }

                System.out.print(gridVerticalLineBorder);
            }

            System.out.println();

            // print bottom padding
            for (int j = 0; j < cellVerticalPaddingValue; j++) {
                System.out.println(paddingTopBottom);
            }

            // print border if not last row
            if (i < GameSettings.gridSize-1) {
                System.out.println(line);
            }
        }

        // bottom border at the end
        System.out.print(stdSpace + stdSpace + borderBottomBegin + gridHorizontalLineBorder);
        for (int i = 0; i < GameSettings.gridSize-1; i++) {
            System.out.print(borderBottomMid + gridHorizontalLineBorder);
        }
        System.out.println(borderBottomEnd);
    }

    // functions that work over board objects
    /**
     * adds a new ship object to board objects 
     * @param size ship's size
     * @param coords valid coordonates
     * @param dir ship's direction
     */
    public void registerShip(int size, Coordonates coords, int dir) {
        boardObjects.add(new Ship(size, coords, dir));
    }
    /**
     * adds newHit object to boardObjects
     * @param newHit the new hit object that will be added
     */
    public void registerHit(Hit newHit) {
        boardObjects.add(newHit);
    }
    /**
     * adds newMiss object to boardObjects
     * @param newMiss the new Miss object that will be added
     */
    public void registerMiss(Miss newMiss) {
        boardObjects.add(newMiss);
    }

    /**
     * checks if the turn at coordonates is a valid turn <br>
     * works by checkyng if the enemy GRID (matrix) contains miss, hit, shipdestroyed || spaceAroundDestroyedShip <br>
     * it is more internal logic
     * @param coordonates the turn's coords
     * @param enemy enemy object
     * @return returns wether the turn is valid or not
     */
    protected boolean validTurn(Coordonates coordonates, Player enemy) {
        if (coordonates.x() == -1) {
            return false;
        } else if (
            enemy.grid.get(coordonates.y()).get(coordonates.x()).equals(GameSettings.missCharacter) || 
            enemy.grid.get(coordonates.y()).get(coordonates.x()).equals(GameSettings.hitCharacter) ||
            enemy.grid.get(coordonates.y()).get(coordonates.x()).equals(GameSettings.shipDestroyedCharacter) ||
            enemy.grid.get(coordonates.y()).get(coordonates.x()).equals(GameSettings.spaceAroundDestroyedShipCharacter) 
        ) {
            return false;
        }
        return true;
    }
    /**
     * checks if the string turn is a valid turn <br>
     * works by validating the string, then calling the internal function
     * @param turn playerTurn
     * @param enemy enemy object
     * @return returns true if turn valid false if not
     */
    public boolean validTurn(String turn, Player enemy) {
        if (turn.length() != 2) {
            return false;
        }
        Coordonates validCoordonates = new Coordonates(turn);
        return validTurn(validCoordonates, enemy); 
    }

    /**
     * checks if object has lost <br>
     * internally checks if there are as many hits as cells in ships
     * @return
     */
    public boolean hasLost() {
        int ships = 0;
        int hits = 0;
        for (BoardObject boardObject : boardObjects) {
            if (boardObject instanceof Ship) {
                ships += boardObject.size;
            }
            if (boardObject instanceof Hit) {
                hits++;
            }
        }
        return hits == ships;
    }


    // generating grid
    /**
     * checks if object still has to place any ships <br>
     * internally checks the quanitity
     * @param shipToPlaceList the list of ships to place
     * @return returns true if stil has ships to place false otherwise
     */
    public boolean hasShipsToPlace(List<ShipToPlace> shipToPlaceList) {
        for (ShipToPlace shipData : shipToPlaceList) {
            if (shipData.shipQuantity > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns the size of the largest ship
     * @param shipsToPlace ships to place list 
     * @return the size of the largest ship
     */
    public int getLargestShip(List<ShipToPlace> shipsToPlace) {
        int biggestShip = 0;
        for (ShipToPlace shipData : shipsToPlace) {
            if (shipData.shipQuantity != 0) {
                biggestShip = Math.max(biggestShip, shipData.shipSize);
            }
        }
        return biggestShip;
    }


    /**
     * checks if starting from xy going in dir for size cells there is nothing in the way
     * @param x x
     * @param y y
     * @param size size
     * @param dir direction of ship
     * @param currentGrid the grid we're talking about
     * @return true if there is nothing in the way false otherwise
     */
    public boolean spaceFree(int x, int y, int size, int dir, List<List<String>> currentGrid) {
        // we've worked everything until now, no need to do anything else
        // the base case for our recursive function
        if (size == 0) {
            return true;
        }

        // check current cell
        if (
            currentGrid.get(y).get(x).equals(GameSettings.shipCharacter) ||
            currentGrid.get(y).get(x).equals(GameSettings.shipCharacterInvalidBoard)
        ) {
            return false;
        }

        // direct neighbors check
        if (
            y > 0 && currentGrid.get(y-1).get(x).equals(GameSettings.shipCharacter) ||
            currentGrid.get(y).get(x).equals(GameSettings.shipCharacterInvalidBoard)
        ) {
            return false;
        }
        if (
            x < GameSettings.gridSize-1 && currentGrid.get(y).get(x+1).equals(GameSettings.shipCharacter) ||
            currentGrid.get(y).get(x).equals(GameSettings.shipCharacterInvalidBoard)
        ) {
            return false;
        }
        if (
            y < GameSettings.gridSize-1 && currentGrid.get(y+1).get(x).equals(GameSettings.shipCharacter) ||
            currentGrid.get(y).get(x).equals(GameSettings.shipCharacterInvalidBoard)
        ) {
            return false;
        }
        if (
            x > 0 && currentGrid.get(y).get(x-1).equals(GameSettings.shipCharacter) ||
            currentGrid.get(y).get(x).equals(GameSettings.shipCharacterInvalidBoard)
        ) {
            return false;
        }

        // diagonal check
        if (
            y > 0 && x > 0 && currentGrid.get(y-1).get(x-1).equals(GameSettings.shipCharacter) ||
            currentGrid.get(y).get(x).equals(GameSettings.shipCharacterInvalidBoard)
        ) {
            return false;
        }
        if (
            y > 0 && x < GameSettings.gridSize-1 && currentGrid.get(y-1).get(x+1).equals(GameSettings.shipCharacter) ||
            currentGrid.get(y).get(x).equals(GameSettings.shipCharacterInvalidBoard)
        ) {
            return false;
        }
        if (
            y < GameSettings.gridSize-1 && x > 0 && currentGrid.get(y+1).get(x-1).equals(GameSettings.shipCharacter) ||
            currentGrid.get(y).get(x).equals(GameSettings.shipCharacterInvalidBoard)
        ) {
            return false;
        }
        if (
            y < GameSettings.gridSize-1 && x < GameSettings.gridSize-1 && currentGrid.get(y+1).get(x+1).equals(GameSettings.shipCharacter) ||
            currentGrid.get(y).get(x).equals(GameSettings.shipCharacterInvalidBoard)
        ) {
            return false;
        }

        // shoot a ray for the rest of ship's size
        if (dir == 1) {
            return spaceFree(x, y+1, size-1, dir, currentGrid);
        } else if (dir == 2) {
            return spaceFree(x+1, y, size-1, dir, currentGrid);
        } else {
            System.out.println("[ERROR] we've somehow got other direction in spaceFree function");
            // though thats kinda impossible
            return false;
        }
    }


    /**
     * computes all valid positions for given ship's Size
     * @param shipSize ship's size
     * @param currentGrid grid we're takling about
     * @return list of pairs with coordonates and directions 
     */
    public List<Pair<Coordonates, Integer>> computeValidPositions(int shipSize, List<List<String>> currentGrid) {
        List<Pair<Coordonates, Integer>> validCoords = new ArrayList<>();

        for (int y = 0; y < currentGrid.size(); y++) {
            for (int x = 0; x < currentGrid.get(y).size(); x++) {

                if (currentGrid.get(y).get(x).equals(GameSettings.waterCharacter)) {

                    if (y + shipSize <= GameSettings.gridSize) {
                        // check if theres noone on the road and nearby
                        if (spaceFree(x, y, shipSize, 1, currentGrid)) {
                            // add a valid vertical(1) position 
                            validCoords.add(new Pair(new Coordonates(x, y), 1));
                        }
                    }

                    if (x + shipSize <= GameSettings.gridSize) {
                        if (spaceFree(x, y, shipSize, 2, currentGrid)) {
                            // add valid horizontal(2) position
                            validCoords.add(new Pair(new Coordonates(x, y), 2));
                        }

                    }
                }
            }
        }

        return validCoords;
    }


    /**
     * generates a random valid grid <br>
     * adds ships to board objects
     */
    public void generateShipsPositions() {
        // initial grid fill
        computeGrid();

        List<ShipToPlace> shipsToPlace = GameSettings.copyOfShips();
        int largestShip = 0;
        // the temp variable for the ship placed, in first has the coords in second has the ship's size
        Pair<Coordonates, Integer> randomShipPlacement;

        while (hasShipsToPlace(shipsToPlace)) {

            // choose the largest ship 
            largestShip = getLargestShip(shipsToPlace);

            // compute it's possible valid positions
            validShipPositions = computeValidPositions(largestShip, this.grid);

            // choose random position
            try {
                randomShipPlacement = validShipPositions.get(Helpers.generateRandomInt(0, validShipPositions.size()-1));
            } catch (Exception exception) {
                // fuck, it generated an impossible grid

                // delete all ships
                boardObjects.clear();

                // renew the list of ships to place
                shipsToPlace = GameSettings.copyOfShips();

                // register the deleted ship on our board
                computeGrid();

                // hope for the best
                continue;
            }

            // place ship
            registerShip(largestShip, randomShipPlacement.first, randomShipPlacement.second);

            // mark ship as placed (remove ship from list of ships to place)
            for (int i = 0; i < shipsToPlace.size(); i++) {
                ShipToPlace shipData = shipsToPlace.get(i);
                if (shipData.shipSize == largestShip && shipData.shipQuantity > 0) {
                    shipsToPlace.set(i, new ShipToPlace(shipData.shipSize, shipData.shipQuantity - 1));
                    break;
                }
            }

            // refill the grid so the program will see ships placed
            computeGrid();
        }
    }

    /**
     * marks ship ALREADY DESTROYED at shipCoordonates as destroyed
     * @param shipCoordonates the last hit
     * @param enemyGrid we need the grid to search for ship and stuff
     * @param enemyBoardObjects - the enemy's board objects
     */
    public void markShipAsDestroyed(Coordonates shipCoordonates, List<List<String>> enemyGrid, List<BoardObject> enemyBoardObjects) {
        Ship ship = getShipByCoordonates(shipCoordonates, enemyGrid, enemyBoardObjects);
        ship.setShipAsDestroyed();
    }

    /**
     * gets one of ship's cell's coordonates, finds the head, finds the object by coords returns the object
     * @param shipCoordonates - one of SHIPS's coordonates
     * @param gridList - the grid, used to find head
     * @param boardObjects - the board objects, used to find object
     * @return returns a ship object or null if not found
     */
    public Ship getShipByCoordonates(Coordonates shipCoordonates, List<List<String>> gridList, List<BoardObject> boardObjects) {
        // first search for ship's head
        int currX = shipCoordonates.x();
        int currY = shipCoordonates.y();

        // up
        while (true) {
            if (currY > 0) {
                if (gridList.get(currY-1).get(currX).equals(GameSettings.hitCharacter)) {
                    currY--;
                } else {
                    break;
                }
            } else {
                break;
            }
        }


        // left
        while (true) {
            if (currX > 0) {
                if (gridList.get(currY).get(currX-1).equals(GameSettings.hitCharacter)) {
                    currX--;
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        // then just search for that ship using it's coordonates
        for (BoardObject boardObject : boardObjects) {
            if (boardObject instanceof Ship && boardObject.coordonates.x() == currX && boardObject.coordonates.y() == currY) {
                return (Ship) boardObject;
            }
        }

        return null;
    }

        /**
     * checks if the HIT ship at (x; y) is destroyed <br>
     * performs fill algorithm 
     * @param x x coordonate
     * @param y y coordonate
     * @param grid is the grid we're discussing
     * @param visited is a matrix where 1 means that we discussed that cell, and 0 means we haven't been there
     * @return returns true if ship is destroyed, false if ship not destroyed
     */
    protected boolean shipDestroyed(int x, int y, List<List<String>> grid, List<List<Integer>> visited) {
        // performing fill algorithm
        visited.get(y).set(x, 1);

        if (grid.get(y).get(x).equals(GameSettings.shipCharacter)) {
            return false;
        }

        // top check
        if (y > 0 && visited.get(y-1).get(x) == 0) {
            if (grid.get(y-1).get(x).equals(GameSettings.hitCharacter)) {
                if (!shipDestroyed(x, y-1, grid, visited)) return false;

            } else if (grid.get(y-1).get(x).equals(GameSettings.shipCharacter)) {
                return false;
            }
        }


        // right check
        if (x < GameSettings.gridSize-1 && visited.get(y).get(x+1) == 0) {
            if (grid.get(y).get(x+1).equals(GameSettings.hitCharacter)) {
                if (!shipDestroyed(x+1, y, grid, visited)) return false;
                
            } else if (grid.get(y).get(x+1).equals(GameSettings.shipCharacter)) {
                return false;
            }
        }


        // bottom check
        if (y < GameSettings.gridSize-1 && visited.get(y+1).get(x) == 0) {
            if (grid.get(y+1).get(x).equals(GameSettings.hitCharacter)) {
                if (!shipDestroyed(x, y+1, grid, visited)) return false;

            } else if (grid.get(y+1).get(x).equals(GameSettings.shipCharacter)) {
                return false;
            }
        }


        // left check
        if (x > 0 && visited.get(y).get(x-1) == 0) {
            if (grid.get(y).get(x-1).equals(GameSettings.hitCharacter)) {
                if (!shipDestroyed(x-1, y, grid, visited)) return false;

            } else if (grid.get(y).get(x-1).equals(GameSettings.shipCharacter)) {
                return false;
            }
        }

        return true;
    }

}

class HumanPlayer extends Player {
    private Ship lastPlacedShip = null;

    /**
     * the second board only the human player has
     */
    private List<BoardObject> hitList = new ArrayList<>();
    private List<List<String>> hitGrid;

    HumanPlayer() {
        // form the hitlist in constructor
        fillHitGrid();
    }

    /**
     * the actual game<br>
     * the individual turn of a player<br>
     * @return returns the turn state Hit | Pass | Quit | Give up | Won
     */
    @Override
    public TurnResult makeTurn(Player enemy) {
        Helpers.sleep(500);
        Helpers.slowType(playerName + "'S TURN");
        Helpers.sleep(500);

        // checck win
        enemy.computeGrid();
        if (enemy.hasLost()) {
            Helpers.slowType(enemy.playerName + "'S GRIND IN THE END:");
            enemy.printGrid();
            Helpers.printMessageAndThreeDotsSlowly(enemy.playerName + " LOST, AS EXPECTED");
            incrementScore();
            return TurnResult.Win;
        }

        ScreenManager.printScore(getHitCount(), enemy.getHitCount(), playerName, enemy.playerName, "h");

        System.out.println();
        Helpers.slowType("YOU CAN ALSO: ");
        System.out.println("\n[G] - give up\n[Q] - quit game (rage quit, bot does not gain points, return to main menu)");
        System.out.println("\n");   // 2 smart and efficient newlines

        Helpers.slowType("CAPTAIN!");
        Helpers.sleep(800);
        Helpers.slowType("THIS IS WHAT WE KNOW ABOUT ENEMY'S GRID:");
        printHitList();

        boolean askingMove = true;
        String consoleInput = "";

        // get player move first
        while (askingMove) {
            Helpers.printRandomAskMove();

            consoleInput = InputManager.getNextLine();
            
            if (consoleInput.equalsIgnoreCase("g")) {
                enemy.incrementScore();
                return TurnResult.GaveUp;

            } else if (consoleInput.equalsIgnoreCase("q")) {
                Helpers.sleep(1000);

                Helpers.slowType("CAPTAIN AS A REMAINDER YOU LOST TO: " + enemy.playerName);
                Helpers.sleep(500);

                Helpers.slowType("LOOSER :))", 200);
                Helpers.sleep(2000);
                return TurnResult.RageQuited;

            } else if (!validTurn(consoleInput, enemy)) {
                Helpers.printInvalidInputMessage();
            } else {
                askingMove = false;
            }
        }
        

        Coordonates turnCoordonates = new Coordonates(consoleInput);

        ScreenManager.printDramaticPauseBeforeMove();

        // miss
        if (enemy.grid.get(turnCoordonates.y()).get(turnCoordonates.x()).equals(GameSettings.waterCharacter)) {
            ScreenManager.printMissMessage();

            enemy.registerMiss(new Miss(consoleInput));

            hitList.add(new Miss(consoleInput));
        }
        // hit
        else if (enemy.grid.get(turnCoordonates.y()).get(turnCoordonates.x()).equals(GameSettings.shipCharacter)) {
            ScreenManager.printHitMessage();
            Helpers.sleep(1000);

            enemy.registerHit(new Hit(consoleInput));

            enemy.computeGrid();

            hitList.add(new Hit(consoleInput));


            // say whether the ship is destroyed or not
            List<List<Integer>> visited = Helpers.createEmptyMatrix(GameSettings.gridSize, GameSettings.gridSize);
            if (!shipDestroyed(turnCoordonates.x(), turnCoordonates.y(), enemy.grid, visited)) {
                Helpers.slowType("CAPTAIN DO NOT RELAX SHIP NOT DESTROYED YET!");
                Helpers.sleep(500);
                System.out.println();

            } else {
                ScreenManager.printShipDestroyedMessage();
                Helpers.sleep(1000);
                Helpers.slowType("DO NOT OPEN SHAMPAGNE YET, ENEMY STIL HAS SHIPS OUT THERE");
                System.out.println();
                Helpers.sleep(500);

                // mark ship as destroyed in enemy's grid
                markShipAsDestroyed(turnCoordonates, enemy.grid, enemy.boardObjects);
                
                // mark it as destroyed in hitlist
                Ship ship = getShipByCoordonates(turnCoordonates, enemy.grid, enemy.boardObjects);
                int shipSize = ship.size;
                int x = ship.coordonates.x();
                int y = ship.coordonates.y();

                Ship destroyedShip = new Ship(shipSize, new Coordonates(x, y), ship.direction);
                destroyedShip.setShipAsDestroyed();
                hitList.add(destroyedShip);
            }

            incrementHitCount();

            return TurnResult.Hit;
        }
        return TurnResult.PassTurn;
    }



    // hitGrid stuff
    /**
     * calls the print and compute grid functions, only for hit list
     */
    public void printHitList() {
        hitGrid = computeGrid(hitList);
        printGrid(hitGrid);
    }

    /**
     * computes hitList into hitGrid
     */
    public void fillHitGrid() {
        hitGrid = computeGrid(hitList);
    }

    /**
     * resets to default a dirty grid
     */
    public void resetHitGrid() {
        hitList.clear();
        fillHitGrid();
    }


    /**
     * prepares player for round, clears grids and shit,<br>your shit*<br>you're shit* 
     */
    @Override
    public void prepareForRound() {
        resetGrid();
        resetHitGrid();
        hitCount = 0;

        addShips();
    }



    /**
     * renders the ship we're trying to add to screen <br>
     * actually renders ship we're trying to add to tempGrid
     * @param tempGrid grid we render the ship to
     * @param tempBoardObjects where we take the ship from
     * @param boardValid if we add the ship, is the board valid
     * @return returns wether the ship position is valid (not board position)
     */
    public boolean fillShip(List<List<String>> tempGrid, List<BoardObject> tempBoardObjects, boolean boardValid) {
        // Get the last added ship
        BoardObject currentShip = tempBoardObjects.get(tempBoardObjects.size() - 1);

        int currentShipSize = currentShip.size;
        int currentShipY = currentShip.coordonates.y();
        int currentShipX = currentShip.coordonates.x();

        // check if if it's position is valid
        // if not we render it in other color
        String shipCharacter = "";
        boolean valid = spaceFree(currentShipX, currentShipY, currentShip.size, currentShip.direction, tempGrid);
        
        if (!valid) {
            shipCharacter = GameSettings.shipCharacterInvalid;
        } else if (!boardValid) {
            shipCharacter = GameSettings.shipCharacterInvalidBoard;
        } else if (valid) {
            shipCharacter = GameSettings.shipCharacter;
        } else {
            System.out.println("GOT A DIFFERENT VALUE IN RENDER SHIPS");
        }

        while (currentShipSize > 0) {
            // render vertically
            if (currentShip.direction == 1) {
                tempGrid.get(currentShipY).set(currentShipX, shipCharacter);
                currentShipY++;
            // render horizontally
            } else if (currentShip.direction == 2) {
                tempGrid.get(currentShipY).set(currentShipX, shipCharacter);
                currentShipX++;
            }
            currentShipSize--;
        }

        return valid;
    }

    /**
     * runs the wasd rotate all on board with the place ship stuff <br>
     * @param size ship size
     * @param shipsToPlace ships to place
     * @return the result of placing the ship
     */
    public AddShipResult addShip(int size, List<ShipToPlace> shipsToPlace) {
        List<List<String>> tempGrid = new ArrayList<>();
        List<BoardObject> tempBoardObjects = new ArrayList<>();
        

        boolean shipPositionValid = false;
        boolean boardValid = true;
        boolean lastShip = true;


        // represent the ships positions as valid indexes
        int shipX = 0;
        int shipY = 0;
        int shipDir = 1;

        while (true) {
            // idk, i did it at 4 am, there is some logic to it, like we draw to check, then we redraw as it should look like
            // but i just wanna sleep and make it work

            int shipTotalQuantity = 0;
            for (ShipToPlace shipDataPair : shipsToPlace) {
                shipTotalQuantity += shipDataPair.shipQuantity;
            }
            lastShip = shipTotalQuantity == 1;

            // add a ship with updated coords and dir
            tempBoardObjects.add(new BoardObject(new Coordonates(shipX, shipY), size, shipDir));

            // first make an empty grid
            tempGrid = fillGrid(boardObjects);
            
            // then we fill a temporary ship 
            shipPositionValid = fillShip(tempGrid, tempBoardObjects, true);
            // check all valid positions
            boardValid = (0 != computeValidPositions(getLargestShip(shipsToPlace), tempGrid).size());

            // skip entirely if it's last ship
            if (lastShip) {
                boardValid = true;
            }

            // then we refill with the actual color of the ship
            tempGrid = fillGrid(boardObjects);
            fillShip(tempGrid, tempBoardObjects, boardValid);

                
            // then print the state of the grid
            printGrid(tempGrid);


            System.out.println();
            System.out.println();
            // legend
            System.out.println(
                """
                    [A] for moving left | [D] for moving right | [R] for rotating   
                    [W] for moving up   | [S] for moving down  | [P] for placing the ship
                    [C] to cancel       | [G] clear the board  | [Z] to undo the placement of last ship
                """
            );

            if (!shipPositionValid) {
                System.out.println("CAPTAIN THIS POSITION IS INVALID, IT TOUCHES OR IS IN PLACE OF ANOTHER SHIP!");
                System.out.println();
                System.out.println();            
            }

            if (!boardValid) {
                System.out.println("CAPTAIN THIS POSITION IS INVALID, PLACING A SHIP HERE WOULD RESULT IN A BOARD THAT CANNOT BE FILLED!");
            }

            if (boardValid && shipPositionValid) {
                System.out.println("THIS IS A GOOD SPOT CAPTAIN");
            }

            String inputStr = InputManager.getLetter();

            // process input
            switch (inputStr) {
                case "a":
                    if (shipX > 0) {
                        shipX--;
                    } else {
                        Helpers.printInvalidInputMessage();
                    }
                    break;

                case "d":
                    if (
                        (shipDir == 1 && shipX + 1 < GameSettings.gridSize) || 
                        (shipDir == 2 && shipX + size < GameSettings.gridSize)
                    ) {
                        shipX++;
                    } else {
                        Helpers.printInvalidInputMessage();
                    }
                    break;

                case "w":
                    if (shipY > 0) {
                        shipY--;
                    } else {
                        Helpers.printInvalidInputMessage();
                    }
                    break;

                case "s":
                    if (
                        (shipDir == 1 && shipY + size < GameSettings.gridSize) || 
                        (shipDir == 2 && shipY + 1 < GameSettings.gridSize)
                    ) {
                        shipY++;
                    } else {
                        Helpers.printInvalidInputMessage();
                    }
                    break;
                
                case "r":
                    if (shipDir == 1) {
                        if (shipX + size < GameSettings.gridSize+1) {
                            shipDir = 2;
                        } else {
                            Helpers.printInvalidInputMessage();
                        }
                    } else if (shipDir == 2) {
                        if (shipY + size < GameSettings.gridSize+1) {
                            shipDir = 1;
                        } else {
                            Helpers.printInvalidInputMessage();
                        }
                    }
                    break;

                case "p":
                    // place the ship and update the quantity


                    // first check if if we place this ship we can place others
                    if (!boardValid) {
                        Helpers.slowType("CAPTAIN!");
                        Helpers.sleep(500);
                        Helpers.slowType("AS I SAID, PLACING THIS SHIP WOULD RESULT IN A BOARD THAT CANNOT BE FILLED!");
                        Helpers.sleep(500);
                        break;
                    }

                    if (!shipPositionValid) {
                        Helpers.slowType("CAPTAIN THIS IS AN INVALID SHIP POSITION");
                        Helpers.sleep(500);
                        break;
                    }

                    // we register ship in our playing grid
                    registerShip(size, new Coordonates(shipX, shipY), shipDir);

                    lastPlacedShip = new Ship(size, new Coordonates(shipX, shipY), shipDir);

                    // then we quit
                    return AddShipResult.ShipAdded;


                case "z":

                    if (lastPlacedShip == null) {
                        Helpers.slowType("CAPTAIN,");
                        Helpers.sleep(300);
                        Helpers.slowType("WE HAVEN'T PLACED ANY SHIPS YET!");
                        Helpers.slowType("OR I DON'T WANT YOU TO DELETE A SHIP");
                        break;
                    }

                    boardObjects.remove(boardObjects.size()-1);
                    for (int i = 0; i < shipsToPlace.size(); i++) {
                        if (shipsToPlace.get(i).shipSize == lastPlacedShip.size) {
                            shipsToPlace.get(i).shipQuantity++;
                            lastPlacedShip = null;
                            break;
                        }
                    }
                    return AddShipResult.DeleteLastShip;

                case "c":
                    // cancel the ship placement, we just return without registering the ship, 
                    // and it will be like we never added it
                    // autocomplete bruh...
                    return AddShipResult.Cancel;

                case "g":
                    boardObjects.clear();
                    shipsToPlace = GameSettings.copyOfShips();
                    return AddShipResult.Clear;
                
                default:
                    Helpers.printInvalidInputMessage();
                    break;
            }

            // so we work on the last ship, and to not pile them up we remove the last one
            tempBoardObjects.remove(tempBoardObjects.size()-1);
        }
    }

    /**
     * runs the place ships thing<br>
     * the vertical ships, choosing which ship to place and shi
     */
    public void addShips() {
        String consoleInput;
        List<ShipToPlace> shipsToPlace = GameSettings.copyOfShips();

        AddShipResult shipPlacingResult;

        // some paddings and shi
        String centerSpace = " ".repeat(20);
        String halfCenterSpace = " ".repeat(5);
        int currentPointerPlace = 0;

        String chooseShipPointer = GameSettings.getChooseShipPointer();


        Helpers.slowType("CAPTAIN WE NEED TO PLACE OUR SHIPS!");
        Helpers.sleep(1000);
        

        while (hasShipsToPlace(shipsToPlace)) {
            computeGrid();

            printGrid();
            System.out.println();

            System.out.println("WE NEED TO PLACE THESE:");

            // even autocomplete does not know how to name these variables
            // inline autocomplete is kinda funny ngl
            // idk what is this autocomplete doing but it is really something


            // ships printed vertically

            // first mannually copy ships
            List<ShipToPlace> shipsToPrint = new ArrayList<>();
            for (ShipToPlace shipData : shipsToPlace) {
                shipsToPrint.add(new ShipToPlace(shipData.shipSize, shipData.shipQuantity));
            }

            // then print them
            printShipsVertically(shipsToPrint, centerSpace);

            System.out.print(centerSpace + "  " + "     ".repeat(currentPointerPlace) + chooseShipPointer);

            System.out.println();
            System.out.println();

            // the legend
            System.out.println(halfCenterSpace + "[A] for moving left [D] for moving right [P] for placing the ship [Q] for quiting");
            System.out.println(halfCenterSpace + "                          (tip* begin with the bigger ships)");

            System.out.println();
            System.out.println("TOO TIRED TO THINK FOR YOURSELF???\nLET THE AI DO THE JOB FOR YOU!!!\nJUST TYPE [M]");

            // read letter input
            String inputStr = InputManager.getLetter();

            switch (inputStr) {
                case "a":
                    if (currentPointerPlace < 1) {
                        Helpers.printInvalidInputMessage();
                        Helpers.sleep(1000);
                        break;
                    }
                    currentPointerPlace--;
                    break;

                case "d":
                    if (currentPointerPlace < shipsToPlace.size()-1) {
                        currentPointerPlace++;
                    } else {
                        Helpers.printInvalidInputMessage();
                        Helpers.sleep(1000);
                    }
                    break;

                case "p":
                    // check if we have any of this ship to place
                    if (shipsToPlace.get(currentPointerPlace).shipQuantity == 0) {
                        Helpers.slowType("CAPTAIN WE DON'T HAVE ANY OF THESE SHIPS LEFT");
                        Helpers.sleep(1000);
                        break;
                    }

                    // place the ship
                    shipPlacingResult = addShip(shipsToPlace.get(currentPointerPlace).shipSize, shipsToPlace);

                    // update the quantity if ship placed
                    if (shipPlacingResult == AddShipResult.ShipAdded) {
                        shipsToPlace.get(currentPointerPlace).shipQuantity = shipsToPlace.get(currentPointerPlace).shipQuantity - 1;
                    }

                    if (shipPlacingResult == AddShipResult.Clear) {
                        shipsToPlace = GameSettings.copyOfShips();
                    }
                    break;


                case "q":
                    return;

                case "m":
                    boolean generatingShips = true;
                    while (generatingShips) {
                        generateShipsPositions();

                        printGrid();
                        System.out.println();
                        System.out.println();

                        Helpers.slowType("DO YOU LIKE IT CAPTAIN?\ny/n", false);
                        consoleInput = InputManager.getLetter();
                        if (consoleInput.equals("y")) {
                            Helpers.slowType("THAT'S WHAT I THUGHT, I KNEW YOU'D LIKE IT ", false);
                            Helpers.slowType("GOOD LITTLE BOY", 70);
                            return;
                        } else if (consoleInput.equals("n")) {
                            Helpers.slowType("LET THE AI TRY AGAIN?\ny/n", false);
                            consoleInput = InputManager.getLetter();
                            
                            if (consoleInput.equals("y")) {
                                continue;
                            } else if (consoleInput.equals("n")) {
                                resetGrid();
                                Helpers.slowType("WELL THERE YOU GO", false);
                                Helpers.sleep(300);
                                Helpers.slowType(", DO IT YOURSELF YOU UNGRATEFULL BASTA", 80);
                                Helpers.sleep(100);

                                generatingShips = false;
                                continue;
                            } else {
                                printMockingMessageOnAIGridNotLiked();
                            }
                        } else {
                            printMockingMessageOnAIGridNotLiked();
                        }
                    }
                    break;

                default:
                    Helpers.printInvalidInputMessage();
                    Helpers.sleep(1000);
                    break;
            }

            System.out.println();
            System.out.println();
        }

        Helpers.slowType("CAPTAIN YOU'RE GOOD TO GO");
    }


    /**
     * prints the ships to place vertically, so it is easier to understand for the player, also it is more cool ngl <br>
     * accepts a copy of default ships cuz it modifies it
     * @param shipsToPlace a list of ships required to be placed
     * @param centerSpace stuff to center shi
     */    
    public void printShipsVertically(List<ShipToPlace> shipsToPlace, String centerSpace) {
        // gaps, paddings verbliud
        String shipGap = " ".repeat(2);



        // this prints each row
        for (int i = 0; i < GameSettings.copyOfShips().get(shipsToPlace.size()-1).shipSize; i++) {
            System.out.print(centerSpace);
            
            // for each ship 
            for (int j = 0; j < shipsToPlace.size(); j++) {
                System.out.print(shipGap);

                if (j+1 == shipsToPlace.size()) {
                    System.out.print(GameSettings.shipCharacter);
                    shipsToPlace.get(j).decrementShipSize();
                } else {
                    if (shipsToPlace.get(j).shipSize == shipsToPlace.get(j+1).shipSize) {
                        System.out.print(GameSettings.shipCharacter);
                        shipsToPlace.get(j).decrementShipSize();
                    } else {
                        System.out.print(" ");
                    }
                }
                System.out.print(shipGap);

            }
            System.out.println();
        }

        System.out.println();

        System.out.print(centerSpace);
        // print the quantity of each ship at the end of the line
        for (int j = 0; j < shipsToPlace.size(); j++) {
            System.out.print(shipGap + shipsToPlace.get(j).shipQuantity + shipGap);
        }

        System.out.println();
    }

    /**
     * in generate ships automatically if user inputs a invalid input, we just generate a new map and say fuck you kinda
     */
    public void printMockingMessageOnAIGridNotLiked() {
        resetGrid();

        Helpers.slowType("SORRY CAPTAIN BUT I CAN'T HEAR YOU");
        Helpers.slowType("GENERATING A NEW MAP IN");
        System.out.println("3");
        Helpers.sleep(100);
        System.out.println("2");
        Helpers.sleep(100);
        System.out.println("1");
    }

}

class Bot extends Player {
    protected int moveCount;
    protected int scoreIncrement = 1;

    Bot() {
        super();
        this.playerName = GameSettings.getRandomDefaultName();
    }
    
    @Override
    public void incrementScore() {
        this.score += scoreIncrement;
    }

    /**
     * preatty self explanatory, just gives a list of coords of valid moves on enemy turn
     * @param enemy
     * @return
     */
    public List<Coordonates> computeValidMoves(Player enemy) {
        enemy.computeGrid();
        List<Coordonates> validTurns = new ArrayList<>();

        for (int y = 0; y < enemy.grid.size(); y++) {
            for (int x = 0; x < enemy.grid.get(y).size(); x++) {
                if (validTurn(new Coordonates(x, y), enemy)) {
                    validTurns.add(new Coordonates(x, y));
                }
            }
        }

        return validTurns;
    }

    /**
     * changes bot's gamemode <br>
     * for now it only changes the turns its allowed to make
     * @param gameMode the game mode we're discussing
     */
    public void changeBotGameMode(GameModes gameMode) {
        switch (gameMode) {
            case BotEasy:
                moveCount = 1;
                scoreIncrement = 3;
                break;
            
            case BotMedium:
                moveCount = 2;
                scoreIncrement = 2;
                break;
            
            case BotHard:
                moveCount = 3;
                scoreIncrement = 1;
                break;

            case Sniper:
                moveCount = 1;
                scoreIncrement = 1;
                break;
        
            default:
                break;
        }
    }
}

class RandomBot extends Bot {

    RandomBot() {
        super();
    }

    /**
     * prepares bot for round, clears grids and other stuff
     */
    @Override
    public void prepareForRound() {
        resetGrid();

        Helpers.printMessageAndThreeDotsSlowly("CONFIGURING BOT");

        Helpers.sleep(200);

        generateShipsPositions();

        Helpers.sleep(200);

        System.out.println("BOT READY!");

        hitCount = 0;

        Helpers.sleep(400);
    }

    /**
     * generates a random coordonate
     * @return returns true if should pass the turn, false if not
     */
    @Override
    public TurnResult makeTurn(Player enemy) {
        Helpers.sleep(500);
        Helpers.slowType(playerName + "'S TURN");
        Helpers.sleep(500);

        List<Coordonates> validMoves;

        boolean botMove;
        for (int i = moveCount; i > 0; i--) {
            botMove = true;
            while (botMove) {
                enemy.computeGrid();

                // if won
                if (enemy.hasLost()) {
                    enemy.printGrid();

                    // increment bot score acording to gameMode
                    incrementScore();

                    Helpers.printMessageAndThreeDotsSlowly("CAPTAIN HOW COULD YOU LOSE?", true);
                    Helpers.printMessageAndThreeDotsSlowly("I MEAN, HOW COULD YOU LOSE TO " + playerName);
                    Helpers.slowType("A PAUSE TO FEEL THIS LOSS");
                    Helpers.sleep(1000);
                    Helpers.slowType("CAPTAIN I WANT YOU TO FEEL THE DOMINANCE OF " + playerName);
                    Helpers.sleep(4000);
                    Helpers.slowType("anyway", 30, false);
                    Helpers.slowType(", you suck :))", 100);
                    return TurnResult.Win;
                }

                if (moveCount > 1) {
                    Helpers.slowType(playerName + " HAS " + i + " MOVES TO MAKE");
                }
                

                validMoves = computeValidMoves(enemy);

                
                int randomInt = Helpers.generateRandomInt(0, validMoves.size()-1);

                Coordonates randomValidMove = validMoves.get(randomInt);
                
                ScreenManager.printBotRandomMoveMessage(this.playerName);

                Helpers.slowType(this.playerName + " HAS THOUGHT");
                Helpers.sleep(500);
                Helpers.slowType("NOW HE WILL HIT THE SPOT");
                Helpers.sleep(500);

                Helpers.slowType(this.playerName + " CHOSE " + randomValidMove.toString() + " AS HIS MOVE");
                
                ScreenManager.printDramaticPauseBeforeMove(this.playerName);

                // hit
                if (enemy.grid.get(randomValidMove.y()).get(randomValidMove.x()).equals(GameSettings.shipCharacter)) {
                    ScreenManager.printBotHit(this.playerName);
                    enemy.registerHit(new Hit(new Coordonates(randomValidMove.x(), randomValidMove.y())));

                    // check ship destroyed
                    if (shipDestroyed(randomValidMove.x(), randomValidMove.y(), enemy.grid, Helpers.createEmptyMatrix(GameSettings.gridSize, GameSettings.gridSize))) {
                        // mark ship as destroyed
                        markShipAsDestroyed(randomValidMove, enemy.grid, enemy.boardObjects);
                    }

                    incrementHitCount();

                } else if (enemy.grid.get(randomValidMove.y()).get(randomValidMove.x()).equals(GameSettings.waterCharacter)) {
                    ScreenManager.printBotMiss(this.playerName);
                    enemy.registerMiss(new Miss(new Coordonates(randomValidMove.x(), randomValidMove.y())));
                    botMove = false;
                }

                Helpers.slowType("OUR GRID AFTER THE FEROCIOUS ATTACK OF " + playerName);
                enemy.printGrid();
                Helpers.sleep(1000);
            }
        }

        return TurnResult.PassTurn;
    }
}


// enums 
enum GameType {
    Cancel, RandomBot, LocalPvP
}
enum GameModes {
    BotEasy, BotMedium, BotHard, Sniper,
}
enum AddShipResult {
    ShipAdded, DeleteLastShip, Cancel, Clear
}
enum BotType {
    RandomBot, AlgorithmicBot 
}
enum TurnResult {
    Win, GaveUp, RageQuited, PassTurn, Hit
}


// 67
/**
 * responds for settings and global variables
 */
class GameSettings {
    public static final String GAME_NAME = "MORSKOY boi";

    // graphics
    // ship states
    public static String shipCharacter = Colors.SHIP_COLOR +  '#' + Colors.RESET;
    public static String shipCharacterInvalid = Colors.SHIP_INVALID_POSITION_COLOR + '#' + Colors.RESET;
    public static String shipCharacterInvalidBoard = Colors.SHIP_INVALID_BOARD_COLOR + '#' + Colors.RESET;
    public static String shipDestroyedCharacter = Colors.SHIP_DESTROYED_COLOR + '#' + Colors.RESET;
    public static String spaceAroundDestroyedShipCharacter = Colors.SHIP_DESTROYED_COLOR + 'o' + Colors.RESET;

    public static String waterCharacter = Colors.WATER_COLOR + '.' + Colors.RESET;
    public static String hitCharacter = Colors.HIT_COLOR + 'X' + Colors.RESET;
    public static String missCharacter = Colors.MISS_COLOR + 'o' + Colors.RESET;

    /**
     * while placing the ships we would want to chose a ship by seeing it's size,<br>
     * that's why we have a pointer 
     */
    public static String chooseShipPointer = "^";

    /**
     * each time when inputing a input, we have this pointer to see that the console is ready to take input
     */
    public static String prompt = "> ";

    /**
     * wether we play normal mode or sniper mode<br>
     * we can also change this to a enum in future
     */
    public static boolean sniperMode = false;

    /**
     * the current grid size we play with
     */
    public static int gridSize = 5;

    // paddings
    private static int STD_SPACE_COUNT = 4;
    public static String STD_SPACE = " ".repeat(STD_SPACE_COUNT);
    public static String STD_SMALL_SPACE = " ".repeat(STD_SPACE_COUNT/2);

    /**
     * a list of default names
     * could be moved into helpers
     */
    private static final String[] DEFAULT_NAMES = {
        "Blackskin Jhon",
        "Bartholomew",
        "Edward Downie",
        "Alchemik Edgar",
        "One-eyed Weiner",
        "Sir Mohamed Allah Abdul",
        "Timmy tough knuckles",
        "Jhonnatan the dihpressed",
    };

    // first is ship size, second is quantity of that ship
    private static List<ShipToPlace> defaultShipsList = List.of(
        new ShipToPlace(1, 4),
        new ShipToPlace(2, 3),
        new ShipToPlace(3, 2),
        new ShipToPlace(4, 1),
        new ShipToPlace(5, 1)
    );

    /**
     * @return a default name ex Bartholomew
     */
    public static String getRandomDefaultName() {
        return DEFAULT_NAMES[Helpers.generateRandomInt(0, DEFAULT_NAMES.length-1)];
    }

    /**
     * @return we return a valid copy of the default ships list, so we can modify it without worrying about the original one<br>
     * and also we return only the ships that are valid for the current grid size, so we do not have to worry about that later<br>
     * autocomplete is cool<br>
     */
    public static List<ShipToPlace> copyOfShips() {
        List<ShipToPlace> copy = new ArrayList<>();


        if (sniperMode) {
            copy.add(new ShipToPlace(1, 1));
            return copy;
        }

        //                                7
        int shipsForGridSize = gridSize > 6 ? defaultShipsList.size() : gridSize - 2;

        int shipQuantityReglator = gridSize > 7 ? 0 : 2;

        for (int i = 0; i < shipsForGridSize; i++) {
            int shipQuantity = defaultShipsList.get(i).shipQuantity - shipQuantityReglator;
            if (shipQuantity <= 0) shipQuantity = 1;

            copy.add(new ShipToPlace(defaultShipsList.get(i).shipSize, shipQuantity));
        }

        return copy;
    }

    /**
     * @return the readonly choose ship pointer
     */
    public static String getChooseShipPointer() {
        String copy = chooseShipPointer;
        return copy;
    }

}

/**
 * responds for colors<br>
 * uses ANSI color codes
 */
class Colors {
    // text colors
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";   // borders
    public static final String RED   = "\u001B[31m";   // hits
    public static final String GREEN = "\u001B[32m";   // ships
    public static final String YELLOW = "\u001B[33m";   // miss
    public static final String BLUE  = "\u001B[34m";   // water
    public static final String MAGENTA  = "\u001B[35m";   // invalidboard if ship placed/shipSunk
    public static final String CYAN = "\u001B[36m";   // ship sunk water around

    public static final String BORDER_COLOR = BLACK;

    public static final String WATER_COLOR = BLUE;
    public static final String SHIP_COLOR = GREEN;
    public static final String SHIP_INVALID_POSITION_COLOR = YELLOW;
    public static final String SHIP_INVALID_BOARD_COLOR = MAGENTA;
    public static final String SHIP_DESTROYED_COLOR = MAGENTA;
    public static final String HIT_COLOR = RED;
    public static final String MISS_COLOR = YELLOW;


    // backgrounds
    public static final String BLACK_BACKGROUND = "\u001B[40m";
    public static final String RED_BACKGROUND = "\u001B[41m";
    public static final String GREEN_BACKGROUND = "\u001B[42m";
    public static final String YELLOW_BACKGROUND = "\u001B[43m";
    public static final String BLUE_BACKGROUND = "\u001B[44m";
    public static final String MAGENTA_BACKGROUND = "\u001B[45m";
    public static final String CYAN_BACKGROUND = "\u001B[46m";
    public static final String WHITE_BACKGROUND = "\u001B[47m";

    public static final String WATER_BACKGROUND_COLOR = BLUE_BACKGROUND;
    public static final String SHIP_BACKGROUND_COLOR = GREEN_BACKGROUND;
    public static final String SHIP_INVALID_BACKGROUND_COLOR = YELLOW_BACKGROUND;
    public static final String SHIP_INVALID_BOARD_BACKGROUND_COLOR = MAGENTA_BACKGROUND;
    public static final String HIT_BACKGROUND_COLOR = RED_BACKGROUND;
    public static final String MISS_BACKGROUND_COLOR = YELLOW_BACKGROUND;
    public static final String SHIP_DESTROYED_BACKGROUND_COLOR = MAGENTA_BACKGROUND;
    public static final String SPACE_AROUND_DESTROYED_SHIP_BACKGROUND_COLOR = CYAN_BACKGROUND;
    

}

/**
 * a coordonates wrapper that stores xy coordonates in one convenient container
 */
class Coordonates {
    // aka j
    private int x;
    // aka i
    private int y;
    
    /**
     * takes input string of the player, and transforms them into workable i j indexes
     * @param input player input
     */
    Coordonates(String input) {
        int x = Helpers.translateLetterToNumber(input.charAt(0)) - 1;
        int y = input.charAt(1) - '0' - 1;

        this.x = x;
        this.y = y;

        validateCoordonates(x, y);
    }

    /**
     * takes valid, workable i,j; integer coordonates <br>
     * though it check if they are between boundaries of grid and sets them to -1-1 if not
     * @param x coordonate aka j
     * @param y coordonate aka i
     */
    Coordonates(int x, int y) {
        this.x = x;
        this.y = y;
        validateCoordonates(x, y);
    }

    /**
     * validates a coordonate <br>
     * checks if it's out of boundaries
     * @param coordonate the coordonate x|y
     * @return returns the coordonate between boundaries
     */
    private void validateCoordonates(int x, int y) {
        if (x > GameSettings.gridSize-1 || y > GameSettings.gridSize-1) {
            this.x = -1;
            this.y = -1;
            System.out.println("COORDONATES TOO HIGH");
        } else if (x < 0 || y < 0) {
            this.x = -1;
            this.y = -1;
            System.out.println("COORDONATES TOO LOW");
        }
    }

    /**
     * a getter for x
     * @return the x coordonate aka j
     */
    public int x() {
        return x;
    }

    /**
     * a getter for y
     * @return the y coordonate aka i
     */
    public int y() {
        return y;
    }

    /**
     * @return the human version of x aka letter
     */
    public char humanizeX() {
        return Helpers.translateNumberToLetter(x);
    }

    /**
     * @return the human version of y
     */
    public int humanizeY() {
        return y+1;
    }

    /**
     * @return human readable coordonates ex A1 B6 etc
     */
    public String toString() {
        return humanizeX() + "" + humanizeY();
    }
}

/**
 * a ship that must be placed container
 */
class ShipToPlace {
    int shipSize;
    int shipQuantity;

    ShipToPlace(int size, int quatnity) {
        shipQuantity = quatnity;
        shipSize = size;
    }

    public void decrementShipSize() {
        shipSize--;
    }
}

/**
 * custom pair class
 */
class Pair<A, B> {
    A first;
    B second;

    Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }
}


/**
 * a encapsulation of whatever could be on the board
 */
class BoardObject {
    public Coordonates coordonates;
    public String graphic;
    
    // only for ships
    protected boolean destroyed;
    // either 1 for vertical or 2 for horizontal
    public int direction;
    // length of the ship
    public int size;

    BoardObject(String coordonatesInput)  {
        this.coordonates = new Coordonates(coordonatesInput);
    }

    BoardObject(Coordonates coordonatesInput)  {
        this.coordonates = coordonatesInput;
    }

    BoardObject(String coordonatesInput, int size, int direction)  {
        this(coordonatesInput);
        this.size = size;
        this.direction = direction;
    }

    BoardObject(Coordonates coordonates, int size, int direction)  {
        this.coordonates = coordonates;
        this.size = size;
        this.direction = direction;
    }
    
    public boolean shipDestroyed() {
        return destroyed;
    }

    public void setShipAsDestroyed() {
        destroyed = true;
        graphic = GameSettings.shipDestroyedCharacter;
    }

    public void printData() {}
}

/**
 * Ship board object
 */
class Ship extends BoardObject {
    
    Ship(int size, String coords, int direction) {
        super(coords, size, direction);
        graphic = GameSettings.shipCharacter;
        destroyed = false;
    }

    Ship(int size, Coordonates coords, int direction) {
        super(coords, size, direction);
        graphic = GameSettings.shipCharacter;
        destroyed = false;
    }

}

/**
 * miss board object
 */
class Miss extends BoardObject {

    Miss(String playerInput) {
        super(playerInput);
        graphic = GameSettings.missCharacter;
    }

    Miss(Coordonates coordonates) {
        super(coordonates);
        graphic = GameSettings.missCharacter;
    }
}

/**
 * hit board object
 */
class Hit extends BoardObject {

    Hit(String playerInput) {
        super(playerInput);
        graphic = GameSettings.hitCharacter;
    }

    Hit(Coordonates coordonates) {
        super(coordonates);
        graphic = GameSettings.hitCharacter;
    }
}



// the screens printer
// needs the game state to know what to print
class ScreenManager {

    // different graphics/embelishments

    // NOTE these embelishments do not print a endline at the end
    private static final String TRANSITION_LINE = "<~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~>";
    private static final String STD_LINE = "════════════════════════════════════════════════════════════════════════════════════════════════════════════";
    private static final String STD_SMALL_LINE = "────────────────────────────────────────────────────────────────────────────────────────────────────────────";
    // graphics
    // welcome to the imperium of bad names
    private static final String borderTopBegin = "╔";
    private static final String borderTopEnd = "╗";
    private static final String borderMidBegin = "╠";
    private static final String borderMidEnd = "╣";
    private static final String borderBottomBegin = "╚";
    private static final String borderBottomEnd = "╝";
    private static final String borderHorizontalLine = "═";
    private static final String borderVerticalLine = "║";
    
    private static final String STD_SPACE = GameSettings.STD_SPACE;
    private static final String MENU_PADDING = " ".repeat(2);
    private static final String MENU_LINE_START = borderVerticalLine + MENU_PADDING;


    private static final String HIT_MESSAGE = """
                                    (X) ─────────────────────────────────────────────────── (X)\r\n
                                        DIRECT HIT! A BREAK IN THE ENEMY SHIP IS DETECTED!\r\n 
                                    (X) ─────────────────────────────────────────────────── (X)
                                    """;

                                    // add something like captain you need to hit the ships not the fishes
    private static final String MISS_MESSAGE = """
                                    (O) ─────────────────────────────────────── (O)\r\n
                                        MISS! EMPTY COORDONATES. NO CONTACT...\r\n
                                    (O) ─────────────────────────────────────── (O)
                                    """;

    private static final String SHIP_DESTROYED_MESSAGE = """
                                    [!] ─────────────────────────────────────────────── [!]\r\n
                                        SHIP DESTROYED! THE ENEMY IS LOSING HIS UNITS!\r\n 
                                    [!] ─────────────────────────────────────────────── [!]
                                    """;

    private static final String GAME_LOGO = """
            ║  __  __  ____  _____   _____ _  ________     __     _           _ 
            ║ |  \\/  |/ __ \\|  __ \\ / ____| |/ / __ \\ \\   / /    | |         (_)
            ║ | \\  / | |  | | |__) | (___ | ' / |  | \\ \\_/ /     | |__   ___  _ 
            ║ | |\\/| | |  | |  _  / \\___ \\|  <| |  | |\\   /      | '_ \\ / _ \\| |
            ║ | |  | | |__| | | \\ \\ ____) | . \\ |__| | | |       | |_) | (_) | |
            ║ |_|  |_|\\____/|_|  \\_\\_____/|_|\\_\\____/  |_|       |_.__/ \\___/|_|
            """;

    private static final String[] SIX_SEVENS = {
                                """
                                        +--------------------+     +-------------------------/
                                        |                    |     |                        /
                                        |                    |     |                       /
                                        |        +-----------+     +-------------+        /
                                        |        |                              /        /
                                        |        +----------+                  /        /
                                        |                   |                 /        /
                                        |        __         |                /        /
                                        |       {  }        |               /        /
                                        |        \\/         |              /        /
                                        |                   |             /        /
                                        +-------------------+            /--------/
                                """,
                                """
 $$$$$$\\  $$$$$$$$\\ 
$$  __$$\\ \\____$$  |
$$ /  \\__|    $$  / 
$$$$$$$\\     $$  /  
$$  __$$\\   $$  /   
$$ /  $$ | $$  /    
 $$$$$$  |$$  /     
 \\______/ \\__/           
                                """,
                                """
                                         _____ _______   __   _____ ________      ________ _   _ 
                                        / ____|_   _\\ \\ / /  / ____|  ____\\ \\    / /  ____| \\ | |
                                        | (___   | |  \\ V /  | (___ | |__   \\ \\  / /| |__  |  \\| |
                                        \\___ \\  | |   > <    \\___ \\|  __|   \\ \\/ / |  __| | . ` |
                                        ____) |_| |_ / . \\   ____) | |____   \\  /  | |____| |\\  |
                                        |_____/|_____/_/ \\_\\ |_____/|______|   \\/   |______|_| \\_|  
                                """,
                                """
 $$$$$$\\  $$$$$$\\ $$\\   $$\\        $$$$$$\\  $$$$$$$$\\ $$\\    $$\\ $$$$$$$$\\ $$\\   $$\\ 
$$  __$$\\ \\_$$  _|$$ |  $$ |      $$  __$$\\ $$  _____|$$ |   $$ |$$  _____|$$$\\  $$ |
$$ /  \\__|  $$ |  \\$$\\ $$  |      $$ /  \\__|$$ |      $$ |   $$ |$$ |      $$$$\\ $$ |
\\$$$$$$\\    $$ |   \\$$$$  /       \\$$$$$$\\  $$$$$\\    \\$$\\  $$  |$$$$$\\    $$ $$\\$$ |
 \\____$$\\   $$ |   $$  $$<         \\____$$\\ $$  __|    \\$$\\$$  / $$  __|   $$ \\$$$$ |
$$\\   $$ |  $$ |  $$  /\\$$\\       $$\\   $$ |$$ |        \\$$$  /  $$ |      $$ |\\$$$ |
\\$$$$$$  |$$$$$$\\ $$ /  $$ |      \\$$$$$$  |$$$$$$$$\\    \\$  /   $$$$$$$$\\ $$ | \\$$ |
 \\______/ \\______|\\__|  \\__|       \\______/ \\________|    \\_/    \\________|\\__|  \\__|
                                """, 
                                """
        6666666677777777777777777777
       6::::::6 7::::::::::::::::::7
      6::::::6  7::::::::::::::::::7
     6::::::6   777777777777:::::::7
    6::::::6               7::::::7 
   6::::::6               7::::::7  
  6::::::6               7::::::7   
 6::::::::66666         7::::::7    
6::::::::::::::66      7::::::7     
6::::::66666:::::6    7::::::7      
6:::::6     6:::::6  7::::::7       
6:::::6     6:::::6 7::::::7        
6::::::66666::::::67::::::7         
 66:::::::::::::667::::::7          
   66:::::::::66 7::::::7           
     666666666  77777777
                                """,
                                """
                _________  
               /         | 
              '-----.   .' 
     .-''''-.     .'  .'   
    /  .--.  \\  .'  .'     
   /  /    '-'.'  .'       
  /  /.--.   '---'         
 /  ' _   \\                
/   .' )   |               
|   (_.'   /               
 \\       '                 
   `----'                       
                                """,
                                """
   __      _____  
U /"/_ u  |___ "| 
\\| '_ \\/     / /  
 | (_) |  u// /\\  
  \\___/    /_/ U  
 _// \\\\_  <<>>_   
(__) (__)(__)__)     
                                """,
                                """
             
 (        )  
 )\\ )  ( /(  
(()/(  )\\()) 
 /(_))((_)\\  
(_) /|__  /  
 / _ \\ / /   
 \\___//_/      
                                """,
                                """
   oo_   wW  Ww wW    Ww       oo_         wWw    wWw     \\\\\\  /// 
  /  _)-<(O)(O)(O)\\  /(O)     /  _)-<  wWw (O)    (O) wWw ((O)(O)) 
  \\__ `.  (..)  `. \\/ .'      \\__ `.   (O)_( \\    / ) (O)_ | \\ ||  
     `. |  ||     \\  /           `. | .' __)\\ \\  / / .' __)||\\\\||  
     _| | _||_    /  \\           _| |(  _)  /  \\/  \\(  _)  || \\ |  
  ,-'   |(_/\\_) .' /\\ `.      ,-'   | `.__) \\ `--' / `.__) ||  ||  
 (_..--'       (_.'  `._)    (_..--'         `-..-'       (_/  \\_) 
                                """,
                                """
  .-')           ) (`-.             .-')      ('-.        (`-.      ('-.       .-') _  
 ( OO ).          ( OO ).          ( OO ).  _(  OO)     _(OO  )_  _(  OO)     ( OO ) ) 
(_)---\\_)  ,-.-')(_/.  \\_)-.      (_)---\\_)(,------.,--(_/   ,. \\(,------.,--./ ,--,'  
/    _ |   |  |OO)\\  `.'  /       /    _ |  |  .---'\\   \\   /(__/ |  .---'|   \\ |  |\\  
\\  :` `.   |  |  \\ \\     /\\       \\  :` `.  |  |     \\   \\ /   /  |  |    |    \\|  | ) 
 '..`''.)  |  |(_/  \\   \\ |        '..`''.)(|  '--.   \\   '   /, (|  '--. |  .     |/  
.-._)   \\ ,|  |_.' .'    \\_)      .-._)   \\ |  .--'    \\     /__) |  .--' |  |\\    |   
\\       /(_|  |   /  .'.  \\       \\       / |  `---.    \\   /     |  `---.|  | \\   |   
 `-----'   `--'  '--'   '--'       `-----'  `------'     `-'      `------'`--'  `--' 
                                """,
                                """
            _____    ____________ _____       _____                   _____     _____\\    \\ _______    ______   _____\\    \\  _____    _____     
       _____\\    \\  /            \\\\    \\     /    /              _____\\    \\   /    / |    |\\      |  |      | /    / |    ||\\    \\   \\    \\    
      /    / \\    ||\\___/\\  \\\\___/|\\    |   |    /              /    / \\    | /    /  /___/| |     /  /     /|/    /  /___/| \\\\    \\   |    |   
     |    |  /___/| \\|____\\  \\___|/ \\    \\ /    /              |    |  /___/||    |__ |___|/ |\\    \\  \\    |/|    |__ |___|/  \\\\    \\  |    |   
  ____\\    \\ |   ||       |  |       \\    |    /            ____\\    \\ |   |||       \\       \\ \\    \\ |    | |       \\         \\|    \\ |    |   
 /    /\\    \\|___|/  __  /   / __    /    |    \\           /    /\\    \\|___|/|     __/ __     \\|     \\|    | |     __/ __       |     \\|    |   
|    |/ \\    \\      /  \\/   /_/  |  /    /|\\    \\         |    |/ \\    \\     |\\    \\  /  \\     |\\         /| |\\    \\  /  \\     /     /\\      \\  
|\\____\\ /____/|    |____________/| |____|/ \\|____|        |\\____\\ /____/|    | \\____\\/    |    | \\_______/ | | \\____\\/    |   /_____/ /______/| 
| |   ||    | |    |           | / |    |   |    |        | |   ||    | |    | |    |____/|     \\ |     | /  | |    |____/|  |      | |     | | 
 \\|___||____|/     |___________|/  |____|   |____|         \\|___||____|/      \\|____|   | |      \\|_____|/    \\|____|   | |  |______|/|_____|/  
                                                                                    |___|/                          |___|/                     
                                """,
                                """
░░      ░░░        ░░  ░░░░  ░░░░░░░░░      ░░░        ░░  ░░░░  ░░        ░░   ░░░  ░
▒  ▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒    ▒▒  ▒
▓▓      ▓▓▓▓▓▓  ▓▓▓▓▓▓▓    ▓▓▓▓▓▓▓▓▓▓▓      ▓▓▓      ▓▓▓▓▓  ▓▓  ▓▓▓      ▓▓▓▓  ▓  ▓  ▓
███████  █████  ██████  ██  ███████████████  ██  ██████████    ████  ████████  ██    █
██      ███        ██  ████  █████████      ███        █████  █████        ██  ███   █
                                """,
                                """
   ___     (_)    __ __     o O O   ___     ___    __ __    ___    _ _    
  (_-<     | |    \\ \\ /    o       (_-<    / -_)   \\ V /   / -_)  | ' \\   
  /__/_   _|_|_   /_\\_\\   TS__[O]  /__/_   \\___|   _\\_/_   \\___|  |_||_|  
_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"| {======|_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"| 
"`-0-0-'"`-0-0-'"`-0-0-'./o--000'"`-0-0-'"`-0-0-'"`-0-0-'"`-0-0-'"`-0-0-'
                                """,
                                """
    __     ____                                                           
   / /    |__  |                                                          
  / _ \\     / /                                                           
  \\___/   _/_/_                                                           
_|\"\"\"\"\"|_|\"\"\"\"\"|                                                          
"`-0-0-'"`-0=0-'
                                """
                            };

    private static final String ONE = """
             __
            |  |
            |  |
            |  |
            |  |
            |__|
            """;

    private static final String TWO = """
             ___  
            |__ \\ 
               ) |
              / / 
             / /_ 
            |____|
            """;

    private static final String THREE = """
             ____  
            |___ \\ 
             __)  |
            |__  < 
             ___) |
            |____/ 
            """;

    // screens

    /**
     * 'clears' console
     */
    public static void clearConsole() {
        System.out.println("\n".repeat(50));
    }

    @Deprecated
    public static void printScreensBottomPadding(int screenOcupied) {
/*         int screenSize = 34;
        System.out.println("\n".repeat(screenSize-screenOcupied)); */
    }

    /**
     * the initial loading screen
     */
    public static void printLoadingScreen() {
        clearConsole();
        Helpers.printMessageAndThreeDotsSlowly("LOADING SYSTEM");
        Helpers.sleep(500);
        Helpers.printMessageAndThreeDotsSlowly("PREPARING AMUNITION");
        Helpers.sleep(500);
        Helpers.printMessageAndThreeDotsSlowly("LAUNCHING TORPEDOS");
        Helpers.sleep(500);
        Helpers.printMessageAndThreeDotsSlowly("HEATING THE ENGINE");
        Helpers.sleep(500);
        System.out.println("FINISHED!");
        Helpers.sleep(1500);
    }

    /**
     * the main menu
     */
    public static void printMainMenu() {
        clearConsole();

        System.out.print(borderTopBegin);
        System.out.println(STD_LINE);
        

        System.out.print(borderVerticalLine);
        System.out.println();
        

        System.out.print(GAME_LOGO);
        System.out.print(borderVerticalLine);
        System.out.println();


        System.out.print(borderVerticalLine);
        System.out.println();


        System.out.print(borderVerticalLine);
        System.out.println(STD_SMALL_LINE);


        System.out.print(MENU_LINE_START);
        System.out.println("[ STATUS ]  " + Colors.GREEN + "SYSTEM READY" + Colors.RESET);


        System.out.print(MENU_LINE_START);
        System.out.println();


        System.out.print(MENU_LINE_START);
        System.out.println("AVALAIBLE COMMANDS:");


        //options 
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[1] FIGHT!");


        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[2] SETTINGS");


        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[3] DONATE");


        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[4] GAME MODES");


        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[5] RULES/MANPAGE");


        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[0] EXIT");


        System.out.print(MENU_LINE_START);
        System.out.println();


        System.out.print(MENU_LINE_START);

        
        System.out.println("[!] CAPTAIN THE SYSTEM IS AWAITING INPUT");


        System.out.print(borderBottomBegin);
        System.out.println(STD_LINE);

    }

    // ...
    public static void print67() {
        int sixSevensToBePrinted = Helpers.generateRandomInt(10, 20);
        String sixSeven;
        for (int i = 0; i < sixSevensToBePrinted; i++) {
            sixSeven = SIX_SEVENS[Helpers.generateRandomInt(0, SIX_SEVENS.length-1)];

            System.out.println(sixSeven);
            Helpers.sleep(100);
        }
        Helpers.slowType("CAPTAIN ", 60, false);
        Helpers.sleep(800);
        Helpers.slowType("YOU", 180, false);
        Helpers.slowType(" MADE ME ", 90, false);
        Helpers.slowType(Colors.RED + "DO" + Colors.RESET, 180, false);
        Helpers.slowType(" IT", 90, false);
        Helpers.slowType("...", 120);
        Helpers.sleep(5000);
    }

    public static void printSettingsScreen() {
        System.out.println(STD_LINE);
        System.out.println("(*) WHAT DO WE WANT TO CHANGE CAPTAIN?");
        System.out.println(STD_LINE);
        System.out.println(STD_SPACE + "(1). CHOOSE SHIP GRAPHICS");
        System.out.println(STD_SPACE + "(2). CHOOSE WATER GRAPHICS");
        System.out.println(STD_SPACE + "(3). CHOOSE HIT GRAPHICS");
        System.out.println(STD_SPACE + "(4). CHOOSE MISS GRAPHICS");
        System.out.println(STD_SPACE + "(5). CHANGE PLAYERS NAMES");
        System.out.println(STD_SPACE + "(0). exit");
    }

    public static void chooseGameScreen() {

        clearConsole();
        System.out.print(borderTopBegin);
        System.out.println(STD_LINE);

        System.out.print(borderVerticalLine);
        System.out.println(STD_SMALL_LINE);

        System.out.print(MENU_LINE_START);
        System.out.println("WHO WE DESTROY TODAY CAPTAIN?");

        System.out.print(borderVerticalLine);
        System.out.println(STD_SMALL_LINE);

        System.out.print(MENU_LINE_START);
        System.out.println();

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[1] A RANDOM BOT");

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[2] AN ALGORITHIC BOT");

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[3] AN AI");

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[4] LOCAL PVP");

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[0] MAIN MENU");
        
        System.out.print(MENU_LINE_START);
        System.out.println();

        System.out.print(MENU_LINE_START);
        System.out.println("[!] CAPTAIN THE SYSTEM IS AWAITING INPUT");

        System.out.print(borderBottomBegin);
        System.out.println(STD_LINE);
    }


    public static void askCaptainName() {
        System.out.println(STD_LINE);
        System.out.println(STD_SPACE + "CAPTAIN HOW SHOULD WE CALL YOU?");
        System.out.println(STD_LINE);
        System.out.println("(if nothing types the default CAPTAIN is chosen)");
    }



    public static void chooseGameModeScreen() {

        clearConsole();
        System.out.print(borderTopBegin);
        System.out.println(STD_LINE);

        System.out.println(borderVerticalLine + STD_SMALL_LINE);
        
        System.out.println(MENU_LINE_START + STD_SPACE + "WHAT GAME MODE WE PLAYIN' CAPTAIN?");
        
        System.out.println(borderVerticalLine + STD_SMALL_LINE);
        
        System.out.println(MENU_LINE_START);
        
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[1] BOT EASY");
        
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "(bot makes a move per your move)");
        
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[2] BOT MEDIUM");
        
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "(bot makes 2 moves per your move)");
        
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[3] BOT HARD");
        
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "(bot makes 3 moves per your move)");
        
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[4] SNIPER DUEL");
        
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "(you and bot have only 1, 1 tiled ship, the first who hits wins!)");
        
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[0] BACK");
        
        System.out.print(borderBottomBegin);
        System.out.println(STD_LINE);
    }

    public static void placingShipsMenu() {
        System.out.println(STD_LINE);
        System.out.println(STD_SPACE);
        Helpers.slowType("Captain,");
        Helpers.sleep(300);
        Helpers.slowType("BEFORE THE FIGHT WE NEED TO PLACE OUR SHIPS!");
        System.out.println(STD_LINE);
    }

    public static void gameStartScreen() {
        System.out.println(STD_LINE);
        Helpers.slowType("PLAYERS ARE YOU READY?");
        System.out.println(STD_LINE);

        Helpers.sleep(600);

        System.out.println(THREE);
        Helpers.sleep(600);

        System.out.println(TWO);
        Helpers.sleep(600);

        System.out.println(ONE);
        Helpers.sleep(300);
    }

    public static void askPlayerMoveScreen(String playerName) {
        System.out.println();
        System.out.println();
        System.out.println(TRANSITION_LINE);
        Helpers.slowType(playerName + " WHAT IS YOUR MOVE?");
        System.out.println();
    }

    /**
     * prints the scores of players  <br>
     * @param player1Score 
     * @param player2Score
     * @param player1Name
     * @param player2Name
     * @param mode the mode takes either o or h for overall or hits
     */
    public static void printScore(int player1Score, int player2Score, String player1Name, String player2Name, String mode) {
        String horizontalPadding = " ".repeat(2);

        
        String centeredText = mode.equals("o") ? GameSettings.GAME_NAME : "HITS SCOREBOARD";

        String scoreDescription = mode.equals("o") ? "POINTS" : "HITS";

        // calculate the width of the score board
        int scoreBoardWidth = 0;
        scoreBoardWidth += horizontalPadding.length() * 8; 
        scoreBoardWidth += player1Name.length() + player2Name.length();
        scoreBoardWidth += String.valueOf(player1Score).length() + String.valueOf(player2Score).length();
        scoreBoardWidth += scoreDescription.length() * 2;
        scoreBoardWidth += 9;   // for spaces and other characters

        boolean borderOdd = scoreBoardWidth % 2 == 1;

        // add one for odd names
        if (borderOdd) {
            scoreBoardWidth++;
        }

        System.out.println(borderTopBegin + borderHorizontalLine.repeat(scoreBoardWidth) + borderTopEnd);


        System.out.println(
            borderVerticalLine + 
            " ".repeat((scoreBoardWidth-centeredText.length())/ 2) +
            centeredText +
            " ".repeat((scoreBoardWidth-centeredText.length())/ 2) +
            " " + borderVerticalLine
        );

        System.out.println(borderMidBegin + borderHorizontalLine.repeat(scoreBoardWidth) + borderMidEnd);

        System.out.print(
            borderVerticalLine + horizontalPadding + 
            "[ " + player1Name + " ]" + horizontalPadding + 
            player1Score + horizontalPadding + scoreDescription + 
            horizontalPadding + "│" + horizontalPadding +
            "[ " + player2Name + " ]" + horizontalPadding + 
            player2Score + horizontalPadding + scoreDescription +
            horizontalPadding
        );

        if (borderOdd) {
            System.out.print(" ");
        }

        System.out.println(borderVerticalLine);

        System.out.println(borderBottomBegin + borderHorizontalLine.repeat(scoreBoardWidth) + borderBottomEnd);        
    }

    /**
     * prints beautifull stuff before hit
     */
    public static void printDramaticPauseBeforeMove() {
        Helpers.printMessageAndThreeDotsSlowly("LAUNCHING TORPEDO");
        Helpers.sleep(500);
        Helpers.slowType("UNTIL IMPACT:");
        print321();
    }

    // this is the same thing but for the bot
    public static void printDramaticPauseBeforeMove(String botName) {
        Helpers.printMessageAndThreeDotsSlowly("LAUNCHING " + botName + "'S TORPEDO");
        Helpers.sleep(500);
        Helpers.slowType("UNTIL IMPACT:");
        print321();
    }

    private static void print321() {
        System.out.println(THREE);
        Helpers.sleep(1000);
        System.out.println(TWO);
        Helpers.sleep(1000);
        System.out.println(ONE);
        Helpers.sleep(600);
    }

    public static void printHitMessage() {
        System.out.println(HIT_MESSAGE);
    }

    public static void printMissMessage() {
        System.out.println(MISS_MESSAGE);
    }

    public static void printShipDestroyedMessage() {
        System.out.println(SHIP_DESTROYED_MESSAGE);
    }

    /**
     * bot message before move
     */
    public static void printBotRandomMoveMessage(String botName) {
        String[] botMoveMessages = {
            botName + " IS PREPARING HIS GIGANT TORPEDO...",
            botName + " IS SOLVING THE 3n+1 PROBLEM...",
            botName + " IS CONSULTING THE HOROSCOPE...",
            botName + " IS SOLVING A EQUATION...",
            botName + " IS TAKING A NAP...",
            botName + " IS CONTEMPLATING ABOUT LIFE...",
            botName + " NEEDS TIME...",
            botName + " KNOWS WHAT HE DOES...",
            "NOW " + botName + " WILL SHOW US SKILL...",
            "NOW " + botName + " WILL SHOW US SOME SKILL...",
            "I THINK " + botName + " FELT ASLEEP...",
            "I THINK " + botName + " FELT ASLEEP WHILE CALCULATING...",
            botName + " IS CALCULATING HIS MOVES...",
            botName + " IS CALCULATING 10 MOVES AHEAD...",
            botName + " SAYS TO TRUST THE PROCESS...",
        };

        // how many random messages to say
        int repeat = Helpers.generateRandomInt(1, 3);
        int said = -1;
        int randomInt = -1;
        for (int i = 0; i < repeat; i++) {
            while (randomInt == said) {
                randomInt = Helpers.generateRandomInt(0, botMoveMessages.length-1);
            }
            Helpers.slowType(botMoveMessages[randomInt]);
            Helpers.sleep(Helpers.generateRandomInt(500, 1500));
            said = randomInt;
        }
    }

    /**
     * when bot hit
     * @param botName botname
     */
    public static void printBotHit(String botName) {
        String[] botHitMessages = {
            botName + " HIT THE TARGET!",
            botName + " SUCCESSFULLY HIT THE TARGET!",
            botName + " IS INDEED SHOWING HIS SKILL! HE HIT",
            botName + " HIT RIGHT IN THE TARGET!",
            botName + " HIT RIGHT IN THE G SPOT!",
            "BLUD THINKS HE'S " + botName + " EINSTEIN! HE HIT",
            botName + " IS ACTUALLY A GENIUS!",
            botName + " IS HIM!",
            botName + " IS ACTUALLY A SNIPER UNDERCOVER!",
            "CAPTAIN I THINK YOU NEED SOME LESSONS FROM " + botName + ", CUZ HIT RIGHT IN THE SPOT!",
        };

        Helpers.slowType(botHitMessages[Helpers.generateRandomInt(0, botHitMessages.length-1)]);
        Helpers.sleep(3000);
    }

    /**
     * when bot missed
     * @param botName botname
     */
    public static void printBotMiss(String botName) {
        String[] botMissMessages = {
            botName + " MISSED THE TARGET!",
            botName + " SUCCESSFULLY MISSED THE TARGET!",
            botName + " IS NOT IN THE MOOD TO HIT TODAY!",
            botName + " MISSED, BUT HE KNOWS WHERE TO HIT NEXT TIME!",
            "BLUD " + botName + " THINKS HE'S EPSTEIN!",
            botName + " THOUGHT HE'S EINSTEIN, UNTIL HE MISSED!",
            botName + " IS ACTUALLY A GENIUS, BUT HE MISSED THIS TIME!",
            botName + " IS ACTUALLY A GENIUS, BUT HE'S UNLUCKY TODAY!",
            "GET A LOAD OF THIS GUY, " + botName + " MISSED!",
        };

        Helpers.slowType(botMissMessages[Helpers.generateRandomInt(0, botMissMessages.length-1)]);
        Helpers.sleep(3000);
    }
}

class Helpers {
    // a balance between speed and ambience
    // 70 for slow, 40 medium (default) 20 really fast
    private static final int SLOW_TYPE_SPEED = 40;

    /**
     * works just like python's sleep function
     * @param ms how much time to sleep
     */
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.out.println("[DEBUG] an exception occured: " + e.getLocalizedMessage());
        }
    }

    /**
     * leter -> number
     * @param letter takes a letter
     * @return a number
     */
    public static int translateLetterToNumber(char letter) {
        if (letter >= 'A' && letter <= 'Z') {
            return letter - 'A' + 1;
        } else if (letter >= 'a' && letter <= 'z') {
           return letter - 'a' + 1; 
        } 
        return -1;
    }

    /**
     * number -> letter
     * @param nr takes number
     * @return returns letter
     */
    public static char translateNumberToLetter(int nr) {
        return (char)(nr + 'A');
    }

    /**
     * overloaded function to type slowly text
     * @param msg what to type
     */
    public static void slowType(String msg) {
        for (char chraracter : msg.toCharArray()) {
            System.out.print(chraracter);
            sleep(SLOW_TYPE_SPEED);
        }
        System.out.println();
    }
    public static void slowType(String msg, boolean endline) {
        for (char chraracter : msg.toCharArray()) {
            System.out.print(chraracter);
            sleep(SLOW_TYPE_SPEED);
        }
        if (endline) {
            System.out.println();
        }

    }

    public static void slowType(String msg, int customSpeed) {
        for (char chraracter : msg.toCharArray()) {
            System.out.print(chraracter);
            sleep(customSpeed);
        }
        System.out.println();
    }
    public static void slowType(String msg, int customSpeed, boolean endline) {
        for (char chraracter : msg.toCharArray()) {
            System.out.print(chraracter);
            sleep(customSpeed);
        }
        if (endline) {
            System.out.println();
        }
    }

    /**
     * prints message then slowly 3 dots
     * @param msg message
     */
    public static void printMessageAndThreeDotsSlowly(String msg) {
        slowType(msg, false);
        slowType(".", 500, false);
        slowType(".", 500, false);
        slowType(".", 500, false);
        System.out.println();
    }
    /**
     * prints the same thing as other function but slower
     * @param msg message
     * @param slowly don't try setting slowly to false, it will still be slow :)
     */
    public static void printMessageAndThreeDotsSlowly(String msg, boolean slowly) {
        slowType(msg, 80, false);
        slowType(".", 700, false);
        slowType(".", 700, false);
        slowType(".", 700, false);
        System.out.println();
    }

    /**
     * generates a random number between a and b inclussive
     * @param lowerBound start value
     * @param upperBound end value
     * @return returns a value between lowerBound and upperBound
     */
    public static int generateRandomInt(int lowerBound, int upperBound) {
        // we add 1 to make the bound inclussive
        return ThreadLocalRandom.current().nextInt(lowerBound, upperBound+1);
    }

    /**
     * prints a random message that the input chosen is invalid
     */
    public static void printInvalidInputMessage() {
        String[] invalidInputMessages = {
            "CAPTAIN THIS IS NOT A VALID COMMAND!",
            "INVALID INPUT CAPTAIN, TRY AGAIN!",
            "CAPTAIN, THIS IS NOT A VALID OPTION!",
            "CAPTAIN, WE CAN'T DO THIS, TRY AGAIN THIS IS INVALID!",
            "CAPTAIN TRY AGAIN, NEXT TIME CHOOSE A VALID OPTION!",
            "CAPTAIN ARE YOU DRUNK?, MAKE A VALID CHOICE!",
            "CAPTAIN BE SERIOUS ABOUT THIS, CHOOSE A VALID OPTION!",
            "DID YOU FALL ON YOUR HEAD CAPTAIN?, THIS IS NOT A COMMAND!",
            "CAPTAIN DID YOU JUST MAKE A TYPO?, THIS IS INVALID!",
            "CAPTAIN DID YOU TAKE A LOBOTOMY?, THIS IS NOT A VALID COMMAND!",
            "BAD LUCK, BETTER CHOICE NEXT TIME CAPTAIN! THIS TIME IT'S NOT VALID",
            "DID YOU JUST TRY TO BREAK THE GAME CAPTAIN?, NOT ON MY WATCH! I KNOW THIS IS NOT VALID",
            "DID YOU FALL ASLEEP ON THE KEYBOARD CAPTAIN?, THIS IS NOT A VALID INPUT!",
            "DID YOU FALL FROM THE SHIP CAPTAIN?, THIS IS NOT A VALID COMMAND!",
            "EVEN A DRUNK MONKEY CHOOSES BETTER OPTIONS THAN YOU CATPAIN! THIS IS INVALID"
        };
        slowType(invalidInputMessages[generateRandomInt(0, invalidInputMessages.length - 1)]);
    }

    public static void printRandomAskMove() {
        String[] askMoveMessages = {
            "CAPTAIN WE NEED TO HIT 'EM BEFORE THEY HIT US\nWHAT ARE YOUR COORDONATES?",
            "CAPTAIN WHERE DO YOU WANT TO SHOOT?",
            "CAPTAIN THE ENEMY IS STILL OUT THERE, WHAT IS YOUR MOVE?",
            "CAPTAIN DON'T LET THE ENEMY WAIT, WHAT IS YOUR MOVE?",
            "CAPTAIN THE ENEMY IS MOCKING US, GIVE HIM A LESSON, WHAT ARE YOUR COORDONATES?",
        };

        slowType(askMoveMessages[generateRandomInt(0, askMoveMessages.length - 1)]);
        System.out.println();
    }


    /**
     * creates a empty matrix of lines lines, and colls collumns
     * @param lines the matrix's lines
     * @param colls the matrix's collumns
     * @return a 0 filled matrix of type integer
     */
    public static List<List<Integer>> createEmptyMatrix(int lines, int colls) {
        // manually create empty matrix
        List<List<Integer>> matrix = new ArrayList<>();
        for(int i = 0; i < lines; i++) {
            matrix.add(
                createEmptyList(colls)
            );
            
        }

        return matrix;
    }

    /**
     * creates a list of 0s
     * @param size is the size, duh
     * @return the list of type integer filled with 0s, duh, you brainded
     */
    public static List<Integer> createEmptyList(int size) {
        // manually create empty list, duh
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            list.add(0);
        }

        return list;
    }
}