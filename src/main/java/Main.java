import org.jline.terminal.Terminal;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.LineReader;

import java.util.List;
import java.util.ArrayList;
import java.lang.Thread;
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
class InputManager {
    private static Terminal terminal;
    private static LineReader reader;

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
            int input = terminal.reader().read();
            return String.valueOf((char) input).toLowerCase();
        } catch (Exception e) {
            System.err.println("Error reading input: " + e.getMessage());
            return " "; 
        }
    }

    public static String getNextLine() {
        return reader.readLine("> ");
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
class GameRunner extends Main {
    protected static ScreenManager screenManager = new ScreenManager();
    protected static String consoleInput = "";
    public static HumanPlayer player1 = new HumanPlayer();
    public static HumanPlayer player2 = new HumanPlayer();

    // program loop
    public void bootGame() {
        boolean gameBooted = true;

        screenManager.printLoadingScreen();

        while (gameBooted) {

            MainMenuOptions option = runMainMenu();

            if (option == MainMenuOptions.Battle) {
                GameMode gameMode = chooseGameMode();

                if (gameMode == GameMode.LocalPvP) {
                    // boot pvp
                    System.out.println("PVP loop here");
                } else if (gameMode == GameMode.RandomBot) {
                    // boot pve
                    runPVEGameLoop();
                } else if (gameMode == GameMode.Cancel) {
                    // go back to main menu
                    System.out.println("Go back to main menu");
                }
                
            } else if (option == MainMenuOptions.Settings) {
                System.out.println("Settings and stuff");
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
            screenManager.printMainMenu();
            consoleInput = InputManager.getNextLine();

            // proto-input validation
            if (consoleInput.length() > 1) {
                System.out.println("Invalid input");
            } else {
                switch (consoleInput) {
                    case "1":
                        return MainMenuOptions.Battle;

                    case "2":
                        return MainMenuOptions.Settings;

                    case "3":
                        return MainMenuOptions.Donate;

                    case "0":
                        return MainMenuOptions.Exit;

                
                    default:
                        System.out.println("Invalid option");
                        break;
                }
            }
        }
    }

    public GameMode chooseGameMode() {
        boolean choosingGame = true;

        while (choosingGame) {
            screenManager.chooseGameModeScreen();
            
            consoleInput = InputManager.getNextLine();

            switch (consoleInput.toLowerCase()) {
                case "1":
                case "one":
                    // a random bot
                    System.out.println("Run random bot");
                    System.out.println("Here could be settings and configuring random bot class");
                    return GameMode.RandomBot;

                case "2":
                case "two":
                    // algorithmic bot
                    Helpers.slowType("CAPTAIN THIS FEATURE ISN'T OUT YET");
                    break;

                case "3":
                case "three":
                    // ai stuff
                    Helpers.slowType("CAPTAIN THIS FEATURE ISN'T OUT YET");
                    
                    break;

                case "4":
                case "four":
                    // pvp stuff
                    System.out.println("Run pvp");
                    System.out.println("idk");
                    return GameMode.LocalPvP;

                case "0":
                case "e":
                case "exit":
                    choosingGame = false;
                    return GameMode.Cancel;
            
                default:
                    Helpers.slowType("CAPTAIN BE SERIOUS ABOUT THIS");
                    break;
            }

        }

        return null;
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
                System.out.println("[DEBUG] player2 grid before:");
                player2.printGrid();
                screenManager.askPlayerMoveScreen(player1.getPlayerName());
                consoleInput = InputManager.getNextLine();

                if (player1.makeTurn(consoleInput, player2, screenManager)) break;
            }

            System.out.println("[DEBUG] player 2 grid after:");
            player2.printGrid();


            while (true) {
                System.out.println("[DEBUG] player1 grid before:");
                player1.printGrid();
                screenManager.askPlayerMoveScreen(player2.getPlayerName());
                consoleInput = InputManager.getNextLine();

                if (player2.makeTurn(consoleInput, player1, screenManager)) break;
            }

            System.out.println("[DEBUG] player 1 grid after:");
            player1.printGrid();


            System.out.println("after round " + round + " the state is");
            System.out.println("player1: ");
            player1.printGrid();
            System.out.println("player 2: ");
            player2.printGrid();


            // check win
            // check for loses*

            boolean player1Lost = player1.hasLost();
            boolean player2Lost = player2.hasLost();

            if (!player1Lost && player2Lost) {
                Helpers.slowType("PLAYER 1 AKA. " + player1.getPlayerName() + " HAS WON");
            } else if (!player2Lost && player1Lost) {
                Helpers.slowType("PLAYER 2 AKA. " + player2.getPlayerName() + " HAS WON");
            } else if (player1Lost && player2Lost) {
                Helpers.slowType("IT's A DRAW!!!");
            } else {
                Helpers.slowType("GAME GOES ON");
            }

        }
    }


    public void runPVEGameLoop() {
        boolean playing = true;
        boolean askPlayAgain = true;

        RandomBot simpleBot = new RandomBot();

        while (playing) {

            Helpers.slowType("SCORE:\n BOT: " + simpleBot.getScore() + " | " + " PLAYER: " + player1.getScore());

            runPvERound(player1, simpleBot);

            // ask if bro wanna play again
            askPlayAgain = true;
            while (askPlayAgain) {
                Helpers.slowType("PLAY AGAIN? (y/n): ", false);
                consoleInput = InputManager.getNextLine();

                if (consoleInput.toLowerCase().equals("y")) {
                    playing = true;
                    askPlayAgain = false;
                } else if (consoleInput.toLowerCase().equals("n")) {
                    playing = false;
                    askPlayAgain = false;
                } else {
                    Helpers.slowType("WE DIDN'T UNDERSTAND YOU CAPTAIN!");
                    askPlayAgain = true;
                }
            }
        }



    }

    public void runPvERound(HumanPlayer player, Bot bot) {
        Helpers.slowType("CONFIGURING MATCH");

        bot.resetGrid();
        player.resetGrid();
        player.resetHitGrid();

        Helpers.slowType("CONFIGURING BOT", false);
        Helpers.slowType(".", false);

        bot.generateShipsPositions();
        Helpers.slowType(".", false);

        Helpers.sleep(100);
        Helpers.slowType(".");

        Helpers.sleep(200);

        Helpers.slowType("BOT READY!");

        // run placing ships
        player.addShips(consoleInput, screenManager);

        Helpers.slowType("OUR FINAL WORK:");
        player.printGrid();

        Helpers.sleep(400);

        boolean gameOn = true;
        boolean askingMove = true;

        while (gameOn) {
            Helpers.slowType("OUR GRID:");
            player.printGrid();

            // player move
            do {
                bot.computeGrid();
                if (bot.hasLost()) {
                    Helpers.slowType("BOT 1 LOST, AS EXPECTED");
                    gameOn = false;
                    player.incrementScore();
                    break;
                }
                Helpers.slowType("HIT GRID:");                    
                player.printHitList();

                askingMove = true;
                while (askingMove) {
                    Helpers.slowType("OUR MOVE?: ", false);
                    consoleInput = InputManager.getNextLine();
                    
                    if (!player.validTurn(consoleInput, bot)) {
                        Helpers.slowType("CAPTAIN THIS TURN IS INVALID!");
                    } else {
                        askingMove = false;
                    }
                }
                
            } while (!player.makeTurn(consoleInput, bot, screenManager));

            // we do not let bot make another turn if he already lost
            if (!gameOn) break;

            // bot move
            do {
                player.computeGrid();
                if (player.hasLost()) {
                    Helpers.slowType("CAPTAIN HOW COULD YOU LOSE?");
                    bot.incrementScore();
                    gameOn = false;
                    break;
                }
            } while (!bot.makeTurn(consoleInput, player, screenManager));

            Helpers.slowType("GAME ON");
        }
    }

    // the function that handles ship placing interactions
    // and players naming and stuff
    public void runPlacingShips() {
        System.out.println();
        Helpers.slowType("PLAYER 1 WHAT IS YOUR NAME?");
        consoleInput = InputManager.getNextLine();

        player1.setPlayerName(consoleInput);

        player1.addShips(consoleInput, screenManager);

        Helpers.slowType("\n\nAFTER ADDING SHIPS THE GRID FOR PLAYER1:");
        player1.printGrid();
        
        System.out.println();
        Helpers.slowType("PLAYER 2 WHAT IS YOUR NAME?");
        consoleInput = InputManager.getNextLine();

        player2.setPlayerName(consoleInput);

        Helpers.slowType("Player2 place the ships");
        player2.addShips(consoleInput, screenManager);

        Helpers.slowType("\n\nAFTER ADDING SHIPS THE GRID FOR PLAYER2:");
        player2.printGrid();

        Helpers.slowType("PLAYER1: ");
        player1.printGrid();

        Helpers.slowType("PLAYER2: ");
        player2.printGrid();
    }

}


// enums 
enum GameMode {
    Cancel,
    RandomBot,
    LocalPvP
}
enum MainMenuOptions {
    Battle,
    Settings,
    Donate,
    Exit
}


// 67
class GameSettings {
    // these static fields should be easily modifyible in settings
    public static String shipCharacter = Colors.SHIP_COLOR +  '#' + Colors.RESET;
    public static String shipCharacterInvalid = Colors.YELLOW + '#' + Colors.RESET;
    public static String waterCharacter = Colors.WATER_COLOR + '.' + Colors.RESET;
    public static String hitCharacter = Colors.HIT_COLOR + 'X' + Colors.RESET;
    public static String missCharacter = Colors.MISS_COLOR + 'o' + Colors.RESET;

    public static String chooseShipPointer = "^";

    // means a grid 5x5
    public static int gridSize = 5;

    public static String STD_SPACE = "\t\t";
    public static String STD_SMALL_SPACE = "\t";

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

        int shipsForGridSize = gridSize > 7 ? defaultShipsList.size() : gridSize - 2;

        int shipQuantityReglator = gridSize > 6 ? 0 : 2;

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
    public static final String HIT_BACKGROUND_COLOR = RED_BACKGROUND;
    public static final String MISS_BACKGROUND_COLOR = YELLOW_BACKGROUND;
    

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

    public int humanizeX() {
        return x+1;
    }

    public int humanizeY() {
        return Helpers.translateNumberToLetter(y);
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

    protected String playerName;

    protected int score;


    // grid related settings
    
    // each unit means a space
    protected int cellHorizontalPaddingValue;
    
    // each unit means a newline
    protected int cellVerticalPaddingValue;

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

        this.cellHorizontalPaddingValue = 4;
        this.cellVerticalPaddingValue = 1;
    }

    public void incrementScore() {
        score++;
    }

    public int getScore() {
        return score;
    }

    public void setPlayerName(String newName) {
        playerName = newName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void computeGrid() {
        grid = fillGrid();
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
    public List<List<String>> fillGrid() {
        List<List<String>> grid = new ArrayList<>();

        grid = fillEmptyGrid();

        // render board objects
        if (boardObjects.size() == 0) {
            // skip we do not add anything
        } else {
            // render ships first
            for (BoardObject object : boardObjects) {
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
            for (BoardObject object : boardObjects) {
                if (object instanceof Miss || object instanceof Hit) {
                    grid.get(object.coordonates.y).set(object.coordonates.x, object.graphic);
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
            enemy.grid.get(y).get(x).equals(GameSettings.hitCharacter)
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

    // checks if starting from xy going in dir for size cells everything is fine
    public boolean spaceFree(int x, int y, int size, int dir) {
        // we've worked everything until now, no need to do anything else
        // the base case for our recursive function
        if (size == 0) {
            return true;
        }

        // check current cell
        if (grid.get(y).get(x).equals(GameSettings.shipCharacter)) {
            return false;
        }

        // direct neighbors check
        if (y > 0 && grid.get(y-1).get(x).equals(GameSettings.shipCharacter)) {
            return false;
        }
        if (x < GameSettings.gridSize-1 && grid.get(y).get(x+1).equals(GameSettings.shipCharacter)) {
            return false;
        }
        if (y < GameSettings.gridSize-1 && grid.get(y+1).get(x).equals(GameSettings.shipCharacter)) {
            return false;
        }
        if (x > 0 && grid.get(y).get(x-1).equals(GameSettings.shipCharacter)) {
            return false;
        }

        // diagonal check
        if (y > 0 && x > 0 && grid.get(y-1).get(x-1).equals(GameSettings.shipCharacter)) {
            return false;
        }
        if (y > 0 && x < GameSettings.gridSize-1 && grid.get(y-1).get(x+1).equals(GameSettings.shipCharacter)) {
            return false;
        }
        if (y < GameSettings.gridSize-1 && x > 0 && grid.get(y+1).get(x-1).equals(GameSettings.shipCharacter)) {
            return false;
        }
        if (y < GameSettings.gridSize-1 && x < GameSettings.gridSize-1 && grid.get(y+1).get(x+1).equals(GameSettings.shipCharacter)) {
            return false;
        }

        // shoot a ray for the rest of ship's size
        if (dir == 1) {
            return spaceFree(x, y+1, size-1, dir);
        } else if (dir == 2) {
            return spaceFree(x+1, y, size-1, dir);
        } else {
            System.out.println("[ERROR] we've somehow got other direction in spaceFree in Bot");
            return false;
        }
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
            System.out.print(stdSpace + (i+1) + stdSpace + "\b" + gridVerticalLineBorder);
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

}

class HumanPlayer extends Player {

    public List<List<String>> hitList = new ArrayList<>();

    // form the hitlist in constructor
    HumanPlayer() {
        fillHitGrid();
    }

    public void printHitList() {
        printGrid(hitList);
    }


    // really creates it empty, initializes it
    public void fillHitGrid() {
        for (int i = 0; i < GameSettings.gridSize; i++) {
            hitList.add(new ArrayList<>());
            for (int j = 0; j < GameSettings.gridSize; j++) {
                hitList.get(i).add(GameSettings.waterCharacter);
            }
        }
    }

    // resets to default a dirty grid
    public void resetHitGrid() {
        hitList.clear();
        fillHitGrid();
    }

    // returns true if we should pass on the turn
    @Override
    public boolean makeTurn(String turn, Player enemy, ScreenManager screenManager) {
        int x = Helpers.translateLetterToNumber(turn.charAt(0)) - 1;
        int y = turn.charAt(1) - '0' - 1;

        if (!validTurn(x, y, enemy)) {
            Helpers.slowType("CAPTAIN, THIS TURN IS INVALID!");
            return false;
        }

        // hit
        if (enemy.grid.get(y).get(x).equals(GameSettings.shipCharacter)) {
            Helpers.slowType("CAPTAIN WE HIT!");

            enemy.registerHit(new Hit(turn));

            enemy.computeGrid();

            hitList.get(y).set(x, GameSettings.hitCharacter);

            // say whether the ship is destroyed or not
            // manually create empty list
            List<List<Integer>> visited = new ArrayList<>();
            for(int i = 0; i < GameSettings.gridSize; i++) {
                visited.add(new ArrayList<>());
                for (int j = 0; j < GameSettings.gridSize; j++) {
                    visited.get(i).add(0);
                }
            }

            if (shipDestroyed(x, y, enemy.grid, visited)) {
                screenManager.printShipDestroyedScreen();
            } else {
                System.out.println("Ship not destroyed");
            }

            return false;

        // miss
        } else if (enemy.grid.get(y).get(x).equals(GameSettings.waterCharacter)) {
            Helpers.slowType("CAPTAIN WE MISSED");

            enemy.registerMiss(new Miss(turn));

            hitList.get(y).set(x, GameSettings.missCharacter);
        }

        return true;
    }


    // renders the ships we're trying to add to screen
    public boolean fillShip(List<List<String>> tempGrid, List<BoardObject> tempBoardObjects) {
        // Get the last added ship
        BoardObject currentShip = tempBoardObjects.get(tempBoardObjects.size() - 1);

        // check if if it's position is valid
        // if not we render it in other color
        String shipCharacter = "";
        boolean valid = spaceFree(currentShip.coordonates.x, currentShip.coordonates.y, currentShip.size, currentShip.direction);
        
        if (valid) {
            shipCharacter = GameSettings.shipCharacter;
        } else {
            shipCharacter = GameSettings.shipCharacterInvalid;
        }

        while (currentShip.size > 0) {
            // render vertically
            if (currentShip.direction == 1) {
                tempGrid.get(currentShip.coordonates.y).set(currentShip.coordonates.x, shipCharacter);
                currentShip.coordonates.y++;
            // render horizontally
            } else if (currentShip.direction == 2) {
                tempGrid.get(currentShip.coordonates.y).set(currentShip.coordonates.x, shipCharacter);
                currentShip.coordonates.x++;
            }
            currentShip.size--;
        }

        return valid;
    }


    public void addShip(int size) {
        List<List<String>> tempGrid = new ArrayList<>();
        List<BoardObject> tempBoardObjects = new ArrayList<>();

        boolean shipPositionValid = false;


        // represent the ships positions as valid indexes
        int shipX = 0;
        int shipY = 0;
        int shipDir = 1;

        while (true) {
            // add a ship with updated coords and dir
            tempBoardObjects.add(new BoardObject(new Coordonates(shipX, shipY), size, shipDir));

            // first make an empty grid
            tempGrid = fillGrid();

            // them fill with our poopoo
            shipPositionValid = fillShip(tempGrid, tempBoardObjects);

            // then print the state of the grid
            printGrid(tempGrid);

            System.out.println();
            System.out.println();
            // legend
            System.out.println(
                """
                    [A] for moving left 
                    [D] for moving right 
                    [W] for moving up 
                    [S] for moving down 
                    [R] for rotating 
                    [P] for placing the ship
                    [C] to cancel
                """
            );

            if (!shipPositionValid) {
                System.out.println("CAPTAIN THIS POSITION IS INVALID, IT TOUCHES OR IS IN PLACE OF ANOTHER SHIP!");
                System.out.println();
                System.out.println();            
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
                    System.out.println("ADD PLACE SHIP STUFF");

                    // we register ship in our playing grid
                    registerShip(size, new Coordonates(shipX, shipY), shipDir);

                    // then we quit
                    return;

                case "c":
                    // cancel the ship placement, we just return without registering the ship, and it will be like we never added it
                    // autocomplete bruh...
                    return;
                
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

        String centerSpace = " ".repeat(20);
        String halfCenterSpace = " ".repeat(5);
        int currentPointerPlace = 0;

        String chooseShipPointer = GameSettings.getChooseShipPointer();


        Helpers.slowType("CAPTAIN WE NEED TO PLACE OUR SHIPS!");
        

        while (hasShipsToPlace(shipsToPlace)) {
            computeGrid();

            printGrid();
            System.out.println();

            System.out.println("WE NEED TO PLACE THESE:");

            // even autocomplete does not know how to name these variables
            // autocomplete is kinda funny ngl
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
            System.out.println(halfCenterSpace + "[A] for moving left [D] for moving right [P] for placing the ship");

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
                    addShip(shipsToPlace.get(currentPointerPlace).first);

                    // update the quantity
                    shipsToPlace.get(currentPointerPlace).second = shipsToPlace.get(currentPointerPlace).second - 1;
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

    List<Pair<Coordonates, Integer>> validPositions;

    public List<Pair<Coordonates, Integer>> computeValidPositions(int shipSize) {
        List<Pair<Coordonates, Integer>> validCoords = new ArrayList<>();

        for (int y = 0; y < grid.size(); y++) {
            for (int x = 0; x < grid.get(y).size(); x++) {

                if (grid.get(y).get(x).equals(GameSettings.waterCharacter)) {

                    if (y + shipSize <= GameSettings.gridSize) {
                        // check if theres noone on the road and nearby
                        if (spaceFree(x, y, shipSize, 1)) {
                            // add a valid vertical(1) position 
                            validCoords.add(new Pair(new Coordonates(x, y), 1));
                        }
                    }

                    if (x + shipSize <= GameSettings.gridSize) {
                        if (spaceFree(x, y, shipSize, 2)) {
                            // add valid horizontal(2) position
                            validCoords.add(new Pair(new Coordonates(x, y), 2));
                        }

                    }
                }
            }
        }

        return validCoords;
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

    // returns the key of the largest ship
    public int getLargestShip(List<Pair<Integer, Integer>> ships) {
        int biggestShip = 0;
        for (Pair<Integer, Integer> shipData : ships) {
            if (shipData.second != 0) {
                biggestShip = Math.max(biggestShip, shipData.first);
            }
        }
        return biggestShip;
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
            // choose the largest ship 
            largestShip = getLargestShip(shipsToPlace);

            // compute it's possible valid positions
            validPositions = computeValidPositions(largestShip);

            // choose random position
            randomShipPlacement = validPositions.get(Helpers.generateRandomInt(0, validPositions.size()-1));

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
}

class RandomBot extends Bot {

    // generates a random coordonate
    // returns true if should pass the turn, false if not
    @Override
    public boolean makeTurn(String turn, Player enemy, ScreenManager screenManager) {
        List<Coordonates> validMoves = computeValidMoves(enemy);

/*         System.out.println("Valid Moves:");
        for (Coordonates coordonates : validMoves) {
            System.out.println(coordonates.x + " " + coordonates.y);
        } */
        
        int randomInt = Helpers.generateRandomInt(0, validMoves.size()-1);

        Coordonates randomValidMove = validMoves.get(randomInt);

        System.out.println("BOT MOVE: (" + randomValidMove.x + " " + randomValidMove.y + ")");

        if (enemy.grid.get(randomValidMove.y).get(randomValidMove.x).equals(GameSettings.shipCharacter)) {
            System.out.println("BOT HIT");
            enemy.registerHit(new Hit(new Coordonates(randomValidMove.x, randomValidMove.y)));
            return false;
        } else if (enemy.grid.get(randomValidMove.y).get(randomValidMove.x).equals(GameSettings.waterCharacter)) {
            System.out.println("BOT MISS");
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

    public void printData() {}
}

class Ship extends BoardObject {
    
    Ship(int size, String coords, int direction) {
        super(coords, size, direction);
        graphic = GameSettings.shipCharacter;
    }

    Ship(int size, Coordonates coords, int direction) {
        super(coords, size, direction);
        graphic = GameSettings.shipCharacter;
    }


    public void printData() {
        System.out.println();
        Helpers.slowType("ship x and y: " + coordonates.x + " " + coordonates.y);
        Helpers.slowType("ships size: " + size);
        Helpers.slowType("ship direction: " + direction);
        System.out.println();
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
    private final String TRANSITION_LINE = "<~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~><~>";
    public static final String STD_LINE = "===============================================";
    private final String STD_SPACE = GameSettings.STD_SPACE;
    private final String HIT_MESSAGE = """
                                    (X) -------------------------------------- (X)\r\n
                                        LOVITURA DIRECTA! RUPERE IN CARENA INAMICA DETECTATA!\r\n 
                                    (X) -------------------------------------- (X)
                                    """;

    private final String MISS_MESSAGE = """
                                    [!] -------------------------------------- [!]\r\n
                                        PlIOSC! COORDONATE PUSTII. FĂRĂ CONTACT...\r\n
                                    [!] -------------------------------------- [!]
                                    """;

    private final String GAME_LOGO = """
             ____       _______ _______ _      ______  _____ _    _ _____ _____   _____  
            |  _ \\   /\\|__   __|__   __| |    |  ____|/ ____| |  | |_   _|  __ \\ / ____| 
            | |_) | /  \\  | |     | |  | |    | |__  | (___ | |__| | | | | |__) | (___   
            |  _ < / /\\ \\ | |     | |  | |    |  __|  \\___ \\|  __  | | | |  ___/ \\___ \\  
            | |_) / ____ \\| |     | |  | |____| |____ ____) | |  | |_| |_| |     ____) | 
            |____/_/    \\_\\_|     |_|  |______|______|_____/|_|  |_|_____|_|    |_____/  
                                                                              
                                                                              
            """;

    private final String SIX_SEVEN = 
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
    """;

    private final String ONE = """
                __
               |  |
               |  |
               |  |
               |  |
               |__|
            """;

    private final String TWO = """
              ___  
             |__ \\ 
                 ) |
                / / 
              / /_ 
             |____|
            """;

    private final String THREE = """
            ____  
            |___ \\ 
            __) |
            |__ < 
            ___) |
            |____/ 
            """;

    private final String FIGHT = """
            ______ _____ _____ _    _ _______ 
            |  ____|_   _/ ____| |  | |__   __|
            | |__    | || |  __| |__| |  | |   
            |  __|   | || | |_ |  __  |  | |   
            | |     _| || |__| | |  | |  | |   
            |_|    |_____\\_____|_|  |_|  |_|   
            """;

    // screens
    public void printMainMenu() {
        System.out.println(STD_LINE);
        Helpers.sleep(50);
        System.out.println(GAME_LOGO);
        Helpers.sleep(50);
        System.out.println(STD_LINE);

        //options 
        System.out.println(STD_SPACE + "[1] LA RAZBOI!");
        Helpers.sleep(50);
        System.out.println(STD_SPACE + "[2] SETARI");
        Helpers.sleep(50);
        System.out.println(STD_SPACE + "[3] DONEAZA");
        Helpers.sleep(50);
        System.out.println(STD_SPACE + "[4] GAME MODES");
        Helpers.sleep(50);
        System.out.println(STD_SPACE + "[0] ESIRE");
        
        System.out.println(STD_LINE);
        System.out.println(STD_SPACE + "SISTEMA E GATA...");
        Helpers.sleep(10);
        System.out.println(STD_SPACE + "ASTEPTAM COMENZI CAPITANE!");
        Helpers.sleep(50);
    }

    public void printLoadingScreen() {
        Helpers.slowType("ICARCARE SISTEMA...");
        Helpers.sleep(500);
        Helpers.slowType("PREGATIRE AMUNITIE...");
        Helpers.sleep(500);
        Helpers.slowType("LANSAREA TARPEDELOR...");
        Helpers.sleep(500);
        Helpers.slowType("INCALZIRE MOTOARE...");
        Helpers.sleep(500);
        System.out.println("FINISAT!");
        Helpers.sleep(1500);
    }

    public void print67() {
        System.out.println(SIX_SEVEN);
        Helpers.sleep(2000);
    }

    public void printSettingsScreen() {
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

    public void askCaptainName() {
        System.out.println(STD_LINE);
        System.out.println(STD_SPACE + "CAPTAIN HOW SHOULD WE CALL YOU?");
        System.out.println(STD_LINE);
        System.out.println("(if nothing types the default CAPTAIN is chosen)");
    }

    public void chooseGameModeScreen() {
        System.out.println(STD_LINE);
        Helpers.slowType(STD_SPACE + "WHO WE DESTROY TODAY CAPTAIN");
        System.out.println(STD_LINE);
        Helpers.slowType(STD_SPACE + "[1] A RANDOM BOT");
        Helpers.slowType(STD_SPACE + "[2] AN ALGORITHIC BOT");
        Helpers.slowType(STD_SPACE + "[3] AN AI");
        Helpers.slowType(STD_SPACE + "[4] LOCAL PVP");
        Helpers.slowType(STD_SPACE + "[0] MAIN MENU");

    }

    public void placingShipsMenu() {
        System.out.println(STD_LINE);
        System.out.println(STD_SPACE);
        Helpers.slowType("Captain,");
        Helpers.sleep(300);
        Helpers.slowType("BEFORE THE FIGHT WE NEED TO PLACE OUR SHIPS!");
        System.out.println(STD_LINE);
        System.out.println("[DEBUG] for now just give coords and size and dir");
        System.out.println("tss simple now but were gonna add cool stuff later on");
    }

    public void gameStartScreen() {
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

        System.out.println(FIGHT);
    }

    public void askPlayerMoveScreen(String playerName) {
        System.out.println();
        System.out.println();
        System.out.println(TRANSITION_LINE);
        Helpers.slowType(playerName + " WHAT IS YOUR MOVE?");
        System.out.println();
    }

    public void printShipDestroyedScreen() {
        System.out.println(STD_LINE);
        Helpers.slowType("CAPTAIN WE DESTROYED ONE OF 'EM");
        System.out.println(STD_LINE);
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
        return letter - 'A' + 1;
    }

    public static int translateNumberToLetter(int nr) {
        return nr + 'A' + 1;
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

    // generates a random number between a and b inclussive
    public static int generateRandomInt(int lowerBound, int upperBound) {
        // we add 1 to make the bound inclussive
        return ThreadLocalRandom.current().nextInt(lowerBound, upperBound+1);
    }

    public static void printInvalidInputMessage() {
        slowType("CAPTAIN THIS IS INVALID");
    }
}