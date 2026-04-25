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



// credits to gemini for this one, it is really something :)
// jLine magic
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

    // gets a single letter from input without the need of pressing enter
    // credits to gemini for this one, it is really something
    // returns the letter lowercased
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


// the glue that glues these scraps together
class GameRunner {
    protected static ScreenManager screenManager = new ScreenManager();
    protected static String consoleInput = "";

    // captain represents the main player
    public static HumanPlayer captain = new HumanPlayer();
    // the guest is a possible second player
    public static HumanPlayer guest = new HumanPlayer();

/* 
    // testing bot
    RandomBot testingBot = new RandomBot();
 */


    // program loop
    public void bootGame() {
        boolean gameBooted = true;

        ScreenManager.printLoadingScreen();

        boolean inBattleMenu = true;

        while (gameBooted) {
            inBattleMenu = true;

            MainMenuOptions option = runMainMenu();

            if (option == MainMenuOptions.Battle) {
                while (inBattleMenu) {
                    GameType gameType = chooseGameType();

                    if (gameType == GameType.LocalPvP) {
                        // boot pvp
                        System.out.println("PVP loop here");
                    } else if (gameType == GameType.RandomBot) {
                        // boot pve
                        runPVEGameLoop();
                    } else if (gameType == GameType.Cancel) {
                        // go back to main menu
                        inBattleMenu = false;
                    }
                }
            } else if (option == MainMenuOptions.Settings) {
                ScreenManager.printSettingsScreen();
            } else if (option == MainMenuOptions.Donate) {
                System.out.println("Do stuff with donations");
            } else if (option == MainMenuOptions.Exit) {
                Helpers.slowType("BYE CAPTAIN");
                gameBooted = false;
            }
        }
    }

    // main menu, runs until a valid option is chosen
    public MainMenuOptions runMainMenu() {
        while (true) {
            ScreenManager.printMainMenu();
            consoleInput = InputManager.getLetter();

            switch (consoleInput) {
                case "1":
                    return MainMenuOptions.Battle;

                case "2":
                    return MainMenuOptions.Settings;

                case "3":
                    return MainMenuOptions.Donate;

                case "6":
                    Helpers.printMessageAndThreeDotsSlowly("CAPTAIN");
                    Helpers.sleep(400);
                    Helpers.printMessageAndThreeDotsSlowly("DON'T SAY THAT YOU");
                    Helpers.sleep(800);

                    Helpers.printMessageAndThreeDotsSlowly("CAPTAIN, FINISH WHAT MUST BE FINISHED");

                    consoleInput = InputManager.getLetter();

                    if (consoleInput.equals("7")) {
                        Helpers.printMessageAndThreeDotsSlowly("I KNOW WHAT KIND OF A MAN YOU ARE");
                        ScreenManager.print67();
                        Helpers.sleep(670);
                    } else {
                        Helpers.slowType("MEH ");
                        Helpers.sleep(400);
                        Helpers.slowType("WHATEVER");
                    }
                    
                    break;

                case "0":
                    return MainMenuOptions.Exit;
            
                default:
                    Helpers.printInvalidInputMessage();
                    break;
            }
            
        }
    }

    public GameType chooseGameType() {
        boolean choosingGame = true;

        while (choosingGame) {
            ScreenManager.chooseGameScreen();
            
            consoleInput = InputManager.getLetter();

            switch (consoleInput.toLowerCase()) {
                case "1":
                    // a bots
                    return GameType.RandomBot;

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
                    return GameType.LocalPvP;

                case "0":
                case "e":
                    choosingGame = false;
                    return GameType.Cancel;
            
                default:
                    Helpers.printInvalidInputMessage();
                    break;
            }

        }

        return null;
    }
    

    public void runPVEGameLoop() {
        boolean playing = true;
        boolean askPlayAgain = true;
        boolean gamePlayed = true;

        RandomBot simpleBot = new RandomBot();

        while (playing) {

            ScreenManager.printScore(captain.score, simpleBot.score, captain.playerName, simpleBot.playerName, "o");

            gamePlayed = runPvERound(captain, simpleBot);

            System.out.println(gamePlayed);

            // we quit if no game was played
            if (!gamePlayed) return;

            // ask if bro wanna play again
            askPlayAgain = true;
            while (askPlayAgain) {
                Helpers.slowType("PLAY AGAIN? (y/n) ", false);
                consoleInput = InputManager.getLetter();

                if (consoleInput.toLowerCase().equals("y")) {
                    playing = true;
                    askPlayAgain = false;
                } else if (consoleInput.toLowerCase().equals("n")) {
                    playing = false;
                    askPlayAgain = false;
                } else {
                    Helpers.printInvalidInputMessage();
                    askPlayAgain = true;
                }
            }
        }
    }

    // runs a round of bot vs player
    // returns true if the game was played until the end
    public boolean runPvERound(HumanPlayer player, Bot bot) {

        bot.resetGrid();
        player.resetGrid();
        player.resetHitGrid();
        GameSettings.sniperMode = false;

        // choose game mode
        GameModes gameMode = chooseGameMode();

        int botTurns = 1;
        if (gameMode == GameModes.BotEasy) {
            botTurns = 1;
        } else if (gameMode == GameModes.BotMedium) {
            botTurns = 2;
        } else if (gameMode == GameModes.BotHard) {
            botTurns = 3;
        } else if (gameMode == GameModes.Sniper) {
            botTurns = 1;
            GameSettings.sniperMode = true;
        } else if (gameMode == null) {
            return false;
        }
        
        ScreenManager.clearConsole();
        
        Helpers.printMessageAndThreeDotsSlowly("DRAMATICALLY PREPARE THE BATTLEFIELD");

        bot.generateShipsPositions();

        Helpers.printMessageAndThreeDotsSlowly("CONFIGURING BOT");

        Helpers.sleep(200);

        System.out.println("BOT READY!");
        
        player.addShips(consoleInput, screenManager);

        Helpers.sleep(400);
        boolean gameOn;


        int shipsRequired = 0;

        List<Pair<Integer, Integer>> temp = GameSettings.copyOfShips();

        for (Pair<Integer,Integer> pair : temp) {
            shipsRequired += pair.second;
        }
    
        if (player.boardObjects.size() != shipsRequired) {
            gameOn = false;
        } else {
            gameOn = true;
        }

        boolean askingMove = true;

        while (gameOn) {
            Helpers.slowType("OUR GRID:");
            player.printGrid();


            // player move
            do {
                bot.computeGrid();
                if (bot.hasLost()) {
                    Helpers.slowType(bot.playerName + "'S GRIND IN THE END:");
                    bot.printGrid();
                    Helpers.printMessageAndThreeDotsSlowly(bot.playerName + " LOST, AS EXPECTED");
                    gameOn = false;
                    player.incrementScore();
                    break;
                }

                ScreenManager.printScore(player.getHitCount(), bot.getHitCount(), player.playerName, bot.playerName, "h");

                Helpers.slowType("CAPTAIN!");
                Helpers.sleep(800);
                Helpers.slowType("THIS IS WHAT WE KNOW ABOUT ENEMY'S GRID (sneak peak):"); 
                bot.printGrid();          
                Helpers.slowType("THIS IS WHAT WE KNOW ABOUT ENEMY'S GRID:");           
                player.printHitList();

                askingMove = true;
                while (askingMove) {
                    Helpers.printRandomAskMove();
                    Helpers.slowType("YOU CAN ALSO");
                    System.out.println("\n[G] - give up\n[Q] - quit game (rage quit, bot does not gain points, return to main menu)");
                    System.out.println("\n");   // 2 smart and efficient newlines

                    consoleInput = InputManager.getNextLine();
                    
                    if (consoleInput.equalsIgnoreCase("g")) {
                        bot.incrementScore(gameMode);

                        printRoundEnd(player, bot);
                        return true;

                    } else if (consoleInput.equalsIgnoreCase("q")) {
                        printRoundEnd(player, bot);
                        Helpers.sleep(1000);

                        Helpers.slowType("CAPTAIN AS A REMAINDER YOU LOST TO: " + bot.playerName);
                        Helpers.sleep(500);

                        Helpers.slowType("LOOSER :))", 200);
                        Helpers.sleep(2000);
                        return false;

                    } else if (!player.validTurn(consoleInput, bot)) {
                        Helpers.printInvalidInputMessage();
                    } else {
                        askingMove = false;
                    }
                }
                
            } while (!player.makeTurn(consoleInput, bot, screenManager));

            // we do not let bot make another turn if he already lost
            if (!gameOn) {
                printRoundEnd(player, bot);
                return true;
            }

            Helpers.sleep(500);
            Helpers.slowType(bot.playerName + "'s TURN");
            Helpers.sleep(500);

            // bot move or moves
            boolean botMove = true;
            for (int i = botTurns; i > 0 && gameOn; i--) {
                botMove = true;
                while (botMove) {
                    player.computeGrid();

                    if (player.hasLost()) {
                        player.printGrid();

                        // increment bot score acording to gameMode
                        bot.incrementScore(gameMode);

                        gameOn = false;
                        botMove = false;

                        Helpers.printMessageAndThreeDotsSlowly("CAPTAIN HOW COULD YOU LOSE?", true);
                        Helpers.printMessageAndThreeDotsSlowly("I MEAN, HOW COULD YOU LOSE TO " + bot.playerName);
                        Helpers.slowType("A PAUSE TO FEEL THIS LOSS");
                        Helpers.sleep(1000);
                        Helpers.slowType("CAPTAIN I WANT YOU TO FEEL THE DOMINANCE OF " + bot.playerName);
                        Helpers.sleep(4000);
                        Helpers.slowType("anyway", 30, false);
                        Helpers.slowType(", you suck :))", 100);
                        break;
                    }

                    if (botTurns > 1) {
                        Helpers.slowType("BOT HAS " + i + " MOVES TO MAKE");
                    }

                    botMove = !bot.makeTurn(consoleInput, player, screenManager);

                    Helpers.slowType("OUR GRID AFTER THE FEROCIOUS ATTACK OF " + bot.playerName);
                    player.printGrid();
                    Helpers.sleep(1000);
                }
            }


            if (gameOn) {
                Helpers.slowType("GAME ON");
            } else {
                printRoundEnd(player, bot);
                return true;
            }
        }
        return false;
    }


    // the actual game running
    public void runPVPGameLoop() {
        boolean playing = true;
        int round = 1;

        runPlacingShips();

        // close to what we'll have
        while (playing) {
            // ask player 1 move
            while (true) {
                System.out.println("[DEBUG] guest grid before:");
                guest.printGrid();
                screenManager.askPlayerMoveScreen(captain.playerName);
                consoleInput = InputManager.getNextLine();

                if (captain.makeTurn(consoleInput, guest, screenManager)) break;
            }

            System.out.println("[DEBUG] guest grid after:");
            guest.printGrid();


            while (true) {
                System.out.println("[DEBUG] captain grid before:");
                captain.printGrid();
                screenManager.askPlayerMoveScreen(guest.playerName);
                consoleInput = InputManager.getNextLine();

                if (guest.makeTurn(consoleInput, captain, screenManager)) break;
            }

            System.out.println("[DEBUG] guest grid after:");
            guest.printGrid();


            System.out.println("after round " + round + " the state is");
            System.out.println("captain: ");
            captain.printGrid();
            System.out.println("guest: ");
            guest.printGrid();


            // check win
            // check for loses*

            boolean captainLost = captain.hasLost();
            boolean guestLost = guest.hasLost();

            if (!captainLost && guestLost) {
                Helpers.slowType("PLAYER 1 AKA. " + captain.playerName + " HAS WON");
            } else if (!guestLost && captainLost) {
                Helpers.slowType("PLAYER 2 AKA. " + guest.playerName + " HAS WON");
            } else if (captainLost && guestLost) {
                Helpers.slowType("IT's A DRAW!!!");
            } else {
                Helpers.slowType("GAME GOES ON");
            }

        }
    }

    public GameModes chooseGameMode() {
        boolean choosingMode = true;

        while (choosingMode) {
            ScreenManager.chooseGameModeScreen();
            
            consoleInput = InputManager.getLetter();

            switch (consoleInput.toLowerCase()) {
                case "1":
                case "easy":
                    return GameModes.BotEasy;

                case "2":
                case "medium":
                    return GameModes.BotMedium;

                case "3":
                case "hard":
                    return GameModes.BotHard;

                case "4":
                case "sniper":
                case "duel":
                    return GameModes.Sniper;

                case "0":
                case "e":
                case "exit":
                    choosingMode = false;
                    return null;
            
                default:
                    Helpers.printInvalidInputMessage();
                    break;
            }

        }

        return null;
    }

    // the function that handles ship placing interactions
    // and players naming and stuff
    public void runPlacingShips() {
        System.out.println();
        Helpers.slowType("PLAYER 1 WHAT IS YOUR NAME?");
        consoleInput = InputManager.getNextLine();

        captain.playerName = consoleInput;

        captain.addShips(consoleInput, screenManager);

        Helpers.slowType("\n\nAFTER ADDING SHIPS THE GRID FOR captain:");
        captain.printGrid();
        
        System.out.println();
        Helpers.slowType("PLAYER 2 WHAT IS YOUR NAME?");
        consoleInput = InputManager.getNextLine();

        guest.playerName = consoleInput;

        Helpers.slowType("Guest place the ships");
        guest.addShips(consoleInput, screenManager);

        Helpers.slowType("\n\nAFTER ADDING SHIPS THE GRID FOR GUEST:");
        guest.printGrid();

        Helpers.slowType("captain: ");
        captain.printGrid();

        Helpers.slowType("PLAYER2: ");
        guest.printGrid();
    }


    public void printRoundEnd(Player player1, Player player2) {
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


// enums 
enum GameType {
    Cancel,
    RandomBot,
    LocalPvP
}
enum GameModes {
    BotEasy,
    BotMedium,
    BotHard,
    Sniper,
}
enum MainMenuOptions {
    Battle,
    Settings,
    Donate,
    Exit
}
enum AddShipResult {
    ShipAdded, DeleteLastShip, Cancel, Clear
}


// 67
class GameSettings {
    public static final String GAME_NAME = "MORSKOY boi";

    public static final String[] DEFAULT_NAMES = {
        "Blackskin Jhon",
        "Calicu Jack",
        "Bartholomew",
        "Edward Downie",
        "Alchemik Edgar",
        "One-eyed Weiner",
        "Sir Mohamed Allah Abdul",
        "Timmy tough knuckles",
        "Jhonnatan the dihpressed",
    };


    // these static fields should be easily modifyible in settings
    public static String shipCharacter = Colors.SHIP_COLOR +  '#' + Colors.RESET;
    public static String shipCharacterInvalid = Colors.YELLOW + '#' + Colors.RESET;
    public static String shipCharacterInvalidBoard = Colors.MAGENTA + '#' + Colors.RESET;
    public static String shipDestroyedCharacter = Colors.MAGENTA + '#' + Colors.RESET;
    public static String waterCharacter = Colors.WATER_COLOR + '.' + Colors.RESET;
    public static String hitCharacter = Colors.HIT_COLOR + 'X' + Colors.RESET;
    public static String missCharacter = Colors.MISS_COLOR + 'o' + Colors.RESET;
    public static String spaceAroundDestroyedShipCharacter = Colors.CYAN + 'o' + Colors.RESET;

    public static String chooseShipPointer = "^";

    public static String prompt = "> ";

    public static boolean sniperMode = false;

    // means a grid 5x5
    public static int gridSize = 7;

    private static int STD_SPACE_COUNT = 4;
    public static String STD_SPACE = " ".repeat(STD_SPACE_COUNT);
    public static String STD_SMALL_SPACE = " ".repeat(STD_SPACE_COUNT/2);

    // first is ship size, second is quantity of that ship
    private static List<Pair<Integer, Integer>> defaultShipsList = List.of(
        new Pair<>(1, 4),
        new Pair<>(2, 3),
        new Pair<>(3, 2),
        new Pair<>(4, 1),
        new Pair<>(5, 1)
    );

    public static List<Pair<Integer, Integer>> getDefaultShipsList() {
        return defaultShipsList;
    }

    // we return a valid copy of the default ships list, so we can modify it without worrying about the original one
    // and also we return only the ships that are valid for the current grid size, so we do not have to worry about that later
    // autocomplete is cool
    public static List<Pair<Integer, Integer>> copyOfShips() {
        List<Pair<Integer, Integer>> copy = new ArrayList<>();

        if (sniperMode) {
            copy.add(new Pair<>(1, 1));
            return copy;
        }

        //                                7
        int shipsForGridSize = gridSize > 6 ? defaultShipsList.size() : gridSize - 2;

        int shipQuantityReglator = gridSize > 7 ? 0 : 2;

        for (int i = 0; i < shipsForGridSize; i++) {
            int shipQuantity = defaultShipsList.get(i).second - shipQuantityReglator;
            if (shipQuantity <= 0) shipQuantity = 1;

            copy.add(new Pair<>(defaultShipsList.get(i).first, shipQuantity));
        }

        return copy;
    }

    public static String getChooseShipPointer() {
        String copy = chooseShipPointer;
        return copy;
    }

}

class Colors {
    // text colors
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";   // borders
    public static final String RED   = "\u001B[31m";   // hits
    public static final String GREEN = "\u001B[32m";   // ships
    public static final String YELLOW = "\u001B[33m";   // miss
    public static final String BLUE  = "\u001B[34m";   // water
    public static final String MAGENTA  = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";

    public static final String BORDER_COLOR = BLACK;

    public static final String WATER_COLOR = BLUE;
    public static final String SHIP_COLOR = GREEN;
    public static final String HIT_COLOR = RED;
    public static final String MISS_COLOR = YELLOW;


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

class Coordonates {
    public int x;
    public int y;

    // takes input string of the player, and transforms them into workable i j indexes
    Coordonates(String input) {
        this.x = Helpers.translateLetterToNumber(input.charAt(0)) - 1;
        this.y = input.charAt(1) - '0' - 1;
    }

    // takes valid integer coordonates
    Coordonates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public char humanizeX() {
        return Helpers.translateNumberToLetter(x);
    }

    public int humanizeY() {
        return y+1;
    }

    public String toString() {
        return humanizeX() + "" + humanizeY();
    }
}

// custom pair class
class Pair<A, B> {
    A first;
    B second;

    Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }
}



class Player {
    protected List<BoardObject> boardObjects = new ArrayList<>();

    protected List<List<String>> grid = new ArrayList<>();
    
    // the starting coordonates and the direction of a valid ship 
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

    public void incrementScore() {
        score++;
    }

    public int getScore() {
        return score;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void incrementHitCount() {
        hitCount++;
    }

    public void computeGrid() {
        grid = fillGrid(this.boardObjects);
    }

    public List<List<String>> computeGrid(List<BoardObject> differentBoardObjects) {
        return fillGrid(differentBoardObjects);
    }

    // creation/initial creation of an empty grid
    protected List<List<String>> fillEmptyGrid() {
        List<List<String>> grid = new ArrayList<>();

        for (int i = 0; i < GameSettings.gridSize; i++) {
            grid.add(new ArrayList<>());
            for (int j = 0; j < GameSettings.gridSize; j++) {
                grid.get(i).add(GameSettings.waterCharacter);
            }
        }

        return grid;
    }

    // fills the grid with characters
    public List<List<String>> fillGrid(List<BoardObject> objectsToPrint) {
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
                        for (int i = object.coordonates.y; i < GameSettings.gridSize; i++, tempSize--) {
                            if (tempSize == 0) break;
                            grid.get(i).set(object.coordonates.x, object.graphic);
                        }
                    } else if (object.direction == 2) {
                        int tempSize = object.size;
                        for (int i = object.coordonates.x; i < GameSettings.gridSize; i++, tempSize--) {
                            if (tempSize == 0) break;
                            grid.get(object.coordonates.y).set(i, object.graphic);
                        }
                    }
                }
            }

            // render hits and misses
            for (BoardObject object : objectsToPrint) {
                if (object instanceof Miss || object instanceof Hit) {
                    grid.get(object.coordonates.y).set(object.coordonates.x, object.graphic);
                }
            }

            // on top of all that render the destroyed ships and mark the area around them
            for (BoardObject object : objectsToPrint) {
                if (object instanceof Ship && object.destroyed) {
                    int x = object.coordonates.x;
                    int y = object.coordonates.y;


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

    public void registerShip(int size, String coords, int dir) {
        boardObjects.add(new Ship(size, coords, dir));
    }

    public void registerShip(int size, Coordonates coords, int dir) {
        boardObjects.add(new Ship(size, coords, dir));
    }

    public void registerHit(Hit newHit) {
        boardObjects.add(newHit);
    }

    public void registerMiss(Miss newMiss) {
        boardObjects.add(newMiss);
    }

    public boolean makeTurn(String turn, Player enemy, ScreenManager screenManager) {
        return true;
    }

    public boolean validTurn(int x, int y, Player enemy) {
        if (x > GameSettings.gridSize - 1 || y > GameSettings.gridSize - 1 ||
            x < 0 || y < 0
        ) {
            return false;
        } else if (
            enemy.grid.get(y).get(x).equals(GameSettings.missCharacter) || 
            enemy.grid.get(y).get(x).equals(GameSettings.hitCharacter) ||
            enemy.grid.get(y).get(x).equals(GameSettings.shipDestroyedCharacter) ||
            enemy.grid.get(y).get(x).equals(GameSettings.spaceAroundDestroyedShipCharacter) 
        ) {
            return false;
        }
        return true;
    }

    public boolean validTurn(String turn, Player enemy) {
        if (turn.length() != 2) {
            return false;
        }
        Coordonates validCoordonates = new Coordonates(turn);
        return validTurn(validCoordonates.x, validCoordonates.y, enemy); 
    }

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

    // checks if the hit ship at (x; y) is destroyed
    public boolean shipDestroyed(int x, int y, List<List<String>> grid, List<List<Integer>> visited) {
        // performing fill algorithm
        visited.get(y).set(x, 1);

        if (grid.get(y).get(x).equals(GameSettings.shipCharacter)) {
            return false;
        }

        // top check
        if (y > 0 && visited.get(y-1).get(x) == 0) {
//            System.out.println("At top we have: " + grid.get(y-1).get(x));
            if (grid.get(y-1).get(x).equals(GameSettings.hitCharacter)) {
                // System.out.println("its a hit");
                if (!shipDestroyed(x, y-1, grid, visited)) return false;
            } else if (grid.get(y-1).get(x).equals(GameSettings.shipCharacter)) {
                // System.out.println("Ship");
                return false;
            }
        }


        // right check
        if (x < GameSettings.gridSize-1 && visited.get(y).get(x+1) == 0) {
            // System.out.println("At right we have: " + grid.get(y).get(x+1));
            if (grid.get(y).get(x+1).equals(GameSettings.hitCharacter)) {
                // System.out.println("its a hit");
                if (!shipDestroyed(x+1, y, grid, visited)) return false;
            } else if (grid.get(y).get(x+1).equals(GameSettings.shipCharacter)) {
                // System.out.println("Ship");
                return false;
            }
        }


        // bottom check
        if (y < GameSettings.gridSize-1 && visited.get(y+1).get(x) == 0) {
            // System.out.println("At bottom we have: " + grid.get(y+1).get(x));
            if (grid.get(y+1).get(x).equals(GameSettings.hitCharacter)) {
                // System.out.println("its a hit");
                if (!shipDestroyed(x, y+1, grid, visited)) return false;
            } else if (grid.get(y+1).get(x).equals(GameSettings.shipCharacter)) {
                // System.out.println("Ship");
                return false;
            }
        }


        // left check
        if (x > 0 && visited.get(y).get(x-1) == 0) {
            // System.out.println("At the left we have: " + grid.get(y).get(x-1));
            if (grid.get(y).get(x-1).equals(GameSettings.hitCharacter)) {
                // System.out.println("its a hit");
                if (!shipDestroyed(x-1, y, grid, visited)) return false;
            } else if (grid.get(y).get(x-1).equals(GameSettings.shipCharacter)) {
                // System.out.println("Ship");
                return false;
            }
        }

        // System.out.println("True");

        return true;
    }

    public boolean hasShipsToPlace(List<Pair<Integer, Integer>> shipList) {
        for (Pair<Integer,Integer> shipData : shipList) {
            if (shipData.second > 0) {
                return true;
            }
        }
        return false;
    }

    // returns the size of the largest ship
    public int getLargestShip(List<Pair<Integer, Integer>> ships) {
        int biggestShip = 0;
        for (Pair<Integer, Integer> shipData : ships) {
            if (shipData.second != 0) {
                biggestShip = Math.max(biggestShip, shipData.first);
            }
        }
        return biggestShip;
    }

    // checks if starting from xy going in dir for size cells everything is fine
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

    // computes all valid positions for given ship's Size
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


    public void resetGrid() {
        boardObjects.clear();
    }

    public void printGrid() {
        computeGrid();
        printGrid(grid);
    }

    // a protected method to print the requested grid
    protected void printGrid(List<List<String>> gridToPrint) {
        String gridHorizontalLineBorder = Colors.BORDER_COLOR +  "─".repeat(2*cellHorizontalPaddingValue) + "─" + Colors.RESET;
        String gridVerticalLineBorder = Colors.BORDER_COLOR + "│" + Colors.RESET;

        String stdSpace = " ".repeat(cellHorizontalPaddingValue);
        String horizontalPadding = " ".repeat(cellHorizontalPaddingValue);


        // BEHOLD, the worst variables names in history of programing
        String borderTopBegin = Colors.BORDER_COLOR + "┌" + Colors.RESET;
        String borderTopMid = Colors.BORDER_COLOR + "┬" + Colors.RESET;
        String borderTopEnd = Colors.BORDER_COLOR + "┐" + Colors.RESET;

        String borderMidBegin = Colors.BORDER_COLOR + "├" + Colors.RESET;
        String borderMidMid = Colors.BORDER_COLOR + "┼" + Colors.RESET;
        String borderMidEnd = Colors.BORDER_COLOR + "┤" + Colors.RESET;

        String borderBottomBegin = Colors.BORDER_COLOR + "└" + Colors.RESET;
        String borderBottomMid = Colors.BORDER_COLOR + "┴" + Colors.RESET;
        String borderBottomEnd = Colors.BORDER_COLOR + "┘" + Colors.RESET;

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
            System.out.print(stdSpace);
            if (i+1 > 9) System.out.print("\b");
            System.out.print((i+1) + stdSpace + "\b" + gridVerticalLineBorder);

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

        // generates ships placed randomly
    public void generateShipsPositions() {
        // initial grid fill
        computeGrid();

        List<Pair<Integer, Integer>> shipsToPlace = GameSettings.copyOfShips();
        int largestShip = 0;
        // the temp variable for the ship placed, in first has the coords in second has the ship's size
        Pair<Coordonates, Integer> randomShipPlacement;

        while (hasShipsToPlace(shipsToPlace)) {

/*             System.out.println("Ships to place:");
            for (Pair<Integer,Integer> pair : shipsToPlace) {
                System.out.println(pair.first + " " + pair.second);
            }
            System.out.println('\n'); */

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
                Pair<Integer, Integer> shipData = shipsToPlace.get(i);
                if (shipData.first == largestShip && shipData.second > 0) {
                    shipsToPlace.set(i, new Pair<>(shipData.first, shipData.second - 1));
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
        int currX = shipCoordonates.x;
        int currY = shipCoordonates.y;

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
            if (boardObject instanceof Ship && boardObject.coordonates.x == currX && boardObject.coordonates.y == currY) {
                return (Ship) boardObject;
            }
        }

        return null;
    }
}

class HumanPlayer extends Player {
    Ship lastPlacedShip = null;

    public List<BoardObject> hitList = new ArrayList<>();

    protected List<List<String>> hitGrid;

    // form the hitlist in constructor
    HumanPlayer() {
        fillHitGrid();
    }

    public void printHitList() {

        hitGrid = computeGrid(hitList);
        printGrid(hitGrid);
    }


    @Deprecated
    // really creates it empty, initializes it
    public void fillHitGrid() {
        
    }

    // resets to default a dirty grid
    public void resetHitGrid() {
        hitList.clear();
        fillHitGrid();
    }

    // returns true if we should pass on the turn
    @Override
    public boolean makeTurn(String turn, Player enemy, ScreenManager screenManager) {
        Coordonates turnCoordonates = new Coordonates(turn);

        ScreenManager.printDramaticPauseBeforeMove();

        // hit
        if (enemy.grid.get(turnCoordonates.y).get(turnCoordonates.x).equals(GameSettings.shipCharacter)) {
            ScreenManager.printHitMessage();
            Helpers.sleep(1000);

            enemy.registerHit(new Hit(turn));

            enemy.computeGrid();

            hitList.add(new Hit(turn));


            // say whether the ship is destroyed or not
            List<List<Integer>> visited = Helpers.createEmptyMatrix(GameSettings.gridSize, GameSettings.gridSize);

            if (shipDestroyed(turnCoordonates.x, turnCoordonates.y, enemy.grid, visited)) {
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
                int x = ship.coordonates.x;
                int y = ship.coordonates.y;

                Ship destroyedShip = new Ship(shipSize, new Coordonates(x, y), ship.direction);
                destroyedShip.setShipAsDestroyed();
                hitList.add(destroyedShip);

            } else {
                Helpers.slowType("CAPTAIN DO NOT RELAX SHIP NOT DESTROYED YET!");
                Helpers.sleep(500);
                System.out.println();
            }

            incrementHitCount();

            return false;

        // miss
        } else if (enemy.grid.get(turnCoordonates.y).get(turnCoordonates.x).equals(GameSettings.waterCharacter)) {
            ScreenManager.printMissMessage();

            enemy.registerMiss(new Miss(turn));

            hitList.add(new Miss(turn));
        }

        return true;
    }


    // renders the ships we're trying to add to screen
    public boolean fillShip(List<List<String>> tempGrid, List<BoardObject> tempBoardObjects, boolean boardValid) {
        // Get the last added ship
        BoardObject currentShip = tempBoardObjects.get(tempBoardObjects.size() - 1);

        int currentShipSize = currentShip.size;
        int currentShipY = currentShip.coordonates.y;
        int currentShipX = currentShip.coordonates.x;

        // check if if it's position is valid
        // if not we render it in other color
        String shipCharacter = "";
        boolean valid = spaceFree(currentShip.coordonates.x, currentShip.coordonates.y, currentShip.size, currentShip.direction, tempGrid);
        
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

    // runs the wasd rotate all on board with the place ship stuff
    // returns wether we actually placed the ship or not
    public AddShipResult addShip(int size, List<Pair<Integer, Integer>> shipsToPlace) {
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
            for (Pair<Integer, Integer> shipDataPair : shipsToPlace) {
                shipTotalQuantity += shipDataPair.second;
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
                    [A] for moving left | [D] for moving right      |
                    [W] for moving up   | [S] for moving down       |
                    [R] for rotating    | [P] for placing the ship  | 
                    [C] to cancel       | [G] clear the board       |[Z] to undo the placement of last ship
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
                        if (shipsToPlace.get(i).first == lastPlacedShip.size) {
                            shipsToPlace.get(i).second++;
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

        
        // registerShip(size, coords, dir);
    }


    public void addShips(String consoleInput, ScreenManager screenManager) {
        List<Pair<Integer, Integer>> shipsToPlace = GameSettings.copyOfShips();

        AddShipResult shipPlaced;

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
            List<Pair<Integer, Integer>> shipsToPrint = new ArrayList<>();
            for (Pair<Integer, Integer> shipData : shipsToPlace) {
                shipsToPrint.add(new Pair<>(shipData.first, shipData.second));
            }

            // then print them
            printShipsVertically(shipsToPrint, centerSpace);

            System.out.print(centerSpace + "  " + "     ".repeat(currentPointerPlace) + chooseShipPointer);

            System.out.println();
            System.out.println();

            // the legend
            System.out.println(halfCenterSpace + "[A] for moving left [D] for moving right [P] for placing the ship [Q] for quiting");
            System.out.println(halfCenterSpace.repeat(4) + "(tip* begin with the bigger ships)");

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
                    if (shipsToPlace.get(currentPointerPlace).second == 0) {
                        Helpers.slowType("CAPTAIN WE DON'T HAVE ANY OF THESE SHIPS LEFT");
                        Helpers.sleep(1000);
                        break;
                    }

                    // place the ship
                    shipPlaced = addShip(shipsToPlace.get(currentPointerPlace).first, shipsToPlace);

                    // update the quantity if ship placed
                    if (shipPlaced == AddShipResult.ShipAdded) {
                        shipsToPlace.get(currentPointerPlace).second = shipsToPlace.get(currentPointerPlace).second - 1;
                    }

                    if (shipPlaced == AddShipResult.Clear) {
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

    // in generate ships automatically if user inputs a invalid input, we just generate a new map and say fuck you kinda
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

    // prints the ships to place vertically, so it is easier to understand for the player, also it is more cool ngl
    // accepts a copy of default ships cuz it modifies it
    public void printShipsVertically(List<Pair<Integer, Integer>> shipsToPlace, String centerSpace) {
        // gaps, paddings
        String shipGap = " ".repeat(2);

        // this prints each row
        for (int i = 0; i < GameSettings.getDefaultShipsList().get(shipsToPlace.size()-1).first; i++) {
            System.out.print(centerSpace);

            // for each ship 
            for (int j = 0; j < shipsToPlace.size(); j++) {
                System.out.print(shipGap);

                if (j+1 == shipsToPlace.size()) {
                    System.out.print(GameSettings.shipCharacter);
                    shipsToPlace.get(j).first = shipsToPlace.get(j).first - 1;
                } else {
                    if (shipsToPlace.get(j).first == shipsToPlace.get(j+1).first) {
                        System.out.print(GameSettings.shipCharacter);
                        shipsToPlace.get(j).first = shipsToPlace.get(j).first - 1;
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
            System.out.print(shipGap + shipsToPlace.get(j).second + shipGap);
        }

        System.out.println();
    }

}

class Bot extends Player {

    Bot() {
        super();
        this.playerName = GameSettings.DEFAULT_NAMES[Helpers.generateRandomInt(0, GameSettings.DEFAULT_NAMES.length-1)];
    }
    
    public void incrementScore(GameModes gameMode) {
        if (gameMode == GameModes.BotEasy) {
            this.score += 3;
        } else if (gameMode == GameModes.BotMedium) {
            this.score += 2;
        } else if (gameMode == GameModes.BotHard) {
            this.score += 1;
        } else if (gameMode == GameModes.Sniper) {
            this.score += 1;
        }
    }


    public List<Coordonates> computeValidMoves(Player enemy) {
        enemy.computeGrid();
        List<Coordonates> validTurns = new ArrayList<>();

        for (int y = 0; y < enemy.grid.size(); y++) {
            for (int x = 0; x < enemy.grid.get(y).size(); x++) {
                if (validTurn(x, y, enemy)) {
                    validTurns.add(new Coordonates(x, y));
                }
            }
        }

        return validTurns;
    }


}

class RandomBot extends Bot {


    // 
    // 
    /**
     * generates a random coordonate
     * @return returns true if should pass the turn, false if not
     */
    @Override
    public boolean makeTurn(String turn, Player enemy, ScreenManager screenManager) {
        List<Coordonates> validMoves = computeValidMoves(enemy);

/*         
System.out.println("Valid Moves:");
for (Coordonates coordonates : validMoves) {
    System.out.println(coordonates.x + " " + coordonates.y);
} 
*/
        
        int randomInt = Helpers.generateRandomInt(0, validMoves.size()-1);

        Coordonates randomValidMove = validMoves.get(randomInt);
        
        ScreenManager.printBotRandomMoveMessage(this.playerName);

        Helpers.slowType(this.playerName + " HAS THOUGHT");
        Helpers.sleep(500);
        Helpers.slowType("NOW HE WILL HIT THE SPOT");
        Helpers.sleep(500);

        Helpers.slowType(this.playerName + " CHOSE " + randomValidMove.toString());
        
        ScreenManager.printDramaticPauseBeforeMove(this.playerName);

        if (enemy.grid.get(randomValidMove.y).get(randomValidMove.x).equals(GameSettings.shipCharacter)) {
            ScreenManager.printBotHit(this.playerName);
            enemy.registerHit(new Hit(new Coordonates(randomValidMove.x, randomValidMove.y)));

            // check ship destroyed
            if (shipDestroyed(randomValidMove.x, randomValidMove.y, enemy.grid, Helpers.createEmptyMatrix(GameSettings.gridSize, GameSettings.gridSize))) {
                // mark ship as destroyed
                markShipAsDestroyed(randomValidMove, enemy.grid, enemy.boardObjects);
            }

            incrementHitCount();
            return false;

        } else if (enemy.grid.get(randomValidMove.y).get(randomValidMove.x).equals(GameSettings.waterCharacter)) {
            ScreenManager.printBotMiss(this.playerName);
            enemy.registerMiss(new Miss(new Coordonates(randomValidMove.x, randomValidMove.y)));
            return true;
        }

        return true;
    }
}


class BoardObject {
    public Coordonates coordonates;
    public String graphic;
    // length of the ship
    public int size;
    // either 1 for vertical or 2 for horizontal
    public int direction;
    
    // only for ships
    protected boolean destroyed;

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

    // NOTE these embelishments do not print a endline at the end
    private static final String TRANSITION_LINE = "<~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~>";
    public static final String STD_LINE = "════════════════════════════════════════════════════════════════════════════════════════════════════════════";
    public static final String STD_SMALL_LINE = "────────────────────────────────────────────────────────────────────────────────────────────────────────────";
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

    // 'clears' console
    public static void clearConsole() {
        System.out.println("\n".repeat(50));
    }

    public static void printScreensBottomPadding(int screenOcupied) {
/*         int screenSize = 34;
        System.out.println("\n".repeat(screenSize-screenOcupied)); */
    }

    // the initial loading screen
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

    // the main menu
    public static void printMainMenu() {
        int screenSize = 0;
        clearConsole();

        System.out.print(borderTopBegin);
        System.out.println(STD_LINE);
        screenSize++;

        System.out.print(borderVerticalLine);
        System.out.println();
        screenSize++;

        System.out.print(GAME_LOGO);
        System.out.print(borderVerticalLine);
        System.out.println();
        screenSize++;

        System.out.print(borderVerticalLine);
        System.out.println();
        screenSize++;

        System.out.print(borderVerticalLine);
        System.out.println(STD_SMALL_LINE);
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println("[ STATUS ]  " + Colors.GREEN + "SYSTEM READY" + Colors.RESET);
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println();
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println("AVALAIBLE COMMANDS:");
        screenSize++;

        //options 
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[1] FIGHT!");
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[2] SETTINGS");
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[3] DONATE");
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[4] GAME MODES");
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[5] RULES/MANPAGE");
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[0] EXIT");
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println();
        screenSize++;

        System.out.print(MENU_LINE_START);
        screenSize++;
        
        System.out.println("[!] CAPTAIN THE SYSTEM IS AWAITING INPUT");
        screenSize++;

        System.out.print(borderBottomBegin);
        System.out.println(STD_LINE);
        screenSize++;

        printScreensBottomPadding(screenSize);
    }

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
        int screenSize = 0;

        clearConsole();
        System.out.print(borderTopBegin);
        System.out.println(STD_LINE);
        screenSize++;

        System.out.print(borderVerticalLine);
        System.out.println(STD_SMALL_LINE);
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println("WHO WE DESTROY TODAY CAPTAIN?");
        screenSize++;

        System.out.print(borderVerticalLine);
        System.out.println(STD_SMALL_LINE);
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println();
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[1] A RANDOM BOT");
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[2] AN ALGORITHIC BOT");
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[3] AN AI");
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[4] LOCAL PVP");
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[0] MAIN MENU");
        screenSize++;
        
        System.out.print(MENU_LINE_START);
        System.out.println();
        screenSize++;

        System.out.print(MENU_LINE_START);
        System.out.println("[!] CAPTAIN THE SYSTEM IS AWAITING INPUT");
        screenSize++;

        System.out.print(borderBottomBegin);
        System.out.println(STD_LINE);
        screenSize++;
        
        
        printScreensBottomPadding(screenSize);
    }

    public static void askCaptainName() {
        System.out.println(STD_LINE);
        System.out.println(STD_SPACE + "CAPTAIN HOW SHOULD WE CALL YOU?");
        System.out.println(STD_LINE);
        System.out.println("(if nothing types the default CAPTAIN is chosen)");
    }



    public static void chooseGameModeScreen() {
        int screenSize = 0;

        clearConsole();
        System.out.print(borderTopBegin);
        System.out.println(STD_LINE);
        screenSize++;
        System.out.println(borderVerticalLine + STD_SMALL_LINE);
        screenSize++;
        System.out.println(MENU_LINE_START + STD_SPACE + "WHAT GAME MODE WE PLAYIN' CAPTAIN?");
        screenSize++;
        System.out.println(borderVerticalLine + STD_SMALL_LINE);
        screenSize++;
        System.out.println(MENU_LINE_START);
        screenSize++;
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[1] BOT EASY");
        screenSize++;
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "(bot makes a move per your move)");
        screenSize++;
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[2] BOT MEDIUM");
        screenSize++;
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "(bot makes 2 moves per your move)");
        screenSize++;
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[3] BOT HARD");
        screenSize++;
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "(bot makes 3 moves per your move)");
        screenSize++;
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[4] SNIPER DUEL");
        screenSize++;
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "(you and bot have only 1, 1 tiled ship, the first who hits wins!)");
        screenSize++;
        System.out.print(MENU_LINE_START);
        System.out.println(STD_SPACE + "[0] BACK");
        screenSize++;
        System.out.print(borderBottomBegin);
        System.out.println(STD_LINE);
        screenSize++;
        
        printScreensBottomPadding(screenSize);
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

    // prints the scores of players
    // the mode takes either o or h for overall or hits
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

    // prints beautifull stuff before hit
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

    public static void print321() {
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
        int repeat = Helpers.generateRandomInt(1, 2);
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

    public static void printBotHit(String botName) {
        String[] botHitMessages = {
            botName + " HIT THE TARGET!",
            botName + " SUCCESSFULLY HIT THE TARGET!",
            botName + " IS HITTING THE TARGET!",
            botName + " IS INDEED SHOWING HIS SKILL!",
            botName + " HIT RIGHT IN THE TARGET!",
            botName + " HIT RIGHT IN THE G SPOT!",
            "BLUD THINKS HE'S " + botName + " EINSTEIN!",
            botName + " IS ACTUALLY A GENIUS!",
            botName + " IS HIM!",
            botName + " IS ACTUALLY A SNIPER UNDERCOVER!",
            "CAPTAIN I THINK YOU NEED SOME LESSONS FROM " + botName + ", CUZ HIT RIGHT IN THE SPOT!",
        };

        Helpers.slowType(botHitMessages[Helpers.generateRandomInt(0, botHitMessages.length-1)]);
        Helpers.sleep(3000);
    }

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

    // works just like python's sleep function
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.out.println("[DEBUG] an exception occured: " + e.getLocalizedMessage());
        }
    }

    public static int translateLetterToNumber(char letter) {
        if (letter >= 'A' && letter <= 'Z') {
            return letter - 'A' + 1;
        } else if (letter >= 'a' && letter <= 'z') {
           return letter - 'a' + 1; 
        } 
        return -1;
    }

    public static char translateNumberToLetter(int nr) {
        return (char)(nr + 'A');
    }

    // overloaded function to type slowly text
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

    public static void printMessageAndThreeDotsSlowly(String msg) {
        slowType(msg, false);
        slowType(".", 500, false);
        slowType(".", 500, false);
        slowType(".", 500, false);
        System.out.println();
    }
    // don't try setting slowly to false, it will still be slow :)
    public static void printMessageAndThreeDotsSlowly(String msg, boolean slowly) {
        slowType(msg, 80, false);
        slowType(".", 700, false);
        slowType(".", 700, false);
        slowType(".", 700, false);
        System.out.println();
    }

    // generates a random number between a and b inclussive
    public static int generateRandomInt(int lowerBound, int upperBound) {
        // we add 1 to make the bound inclussive
        return ThreadLocalRandom.current().nextInt(lowerBound, upperBound+1);
    }

    // prints a random message that the input chosen is invalid
    public static void printInvalidInputMessage() {
        String[] invalidInputMessages = {
            "CAPTAIN THIS IS NOT A VALID COMMAND!",
            "INVALID INPUT CAPTAIN, TRY AGAIN!",
            "CAPTAIN, THIS IS NOT A VALID OPTION!",
            "CAPTAIN, WE CAN'T DO THIS, TRY AGAIN!",
            "CAPTAIN TRY AGAIN, NEXT TIME CHOOSE A VALID OPTION!",
            "CAPTAIN ARE YOU DRUNK?, MAKE A VALID CHOICE!",
            "CAPTAIN BE SERIOUS ABOUT THIS, CHOOSE A VALID OPTION!",
            "DID YOU FALL ON YOUR HEAD CAPTAIN?, THIS IS NOT A COMMAND!",
            "CAPTAIN DID YOU JUST MAKE A TYPO?, THIS IS INVALID!",
            "CAPTAIN DID YOU TAKE A LOBOTOMY?, THIS IS NOT A VALID COMMAND!",
            "BAD LUCK, BETTER CHOICE NEXT TIME CAPTAIN!",
            "DID YOU JUST TRY TO BREAK THE GAME CAPTAIN?, NOT ON MY WATCH!",
            "DID YOU FALL ASLEEP ON THE KEYBOARD CAPTAIN?, THIS IS NOT A VALID INPUT!",
            "DID YOU FALL FROM THE SHIP CAPTAIN?, THIS IS NOT A VALID COMMAND!",
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