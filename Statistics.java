import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;
import javax.swing.JOptionPane;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("FieldMayBeFinal")
class Statistics{
    private static File jsonFile;
    private static final String JSON_PATH = "Resources/statistics.json";
    private static Map<String, Integer> allTimeData = new HashMap<>();
    private static Map<String, Integer> currSessionData = new HashMap<>();

    //JSON strings
    private static final String GAMES = "Games";
    private static final String USER_WINS = "User wins";
    private static final String COMPUTER_WINS = "Computer wins";
    private static final String DRAWS = "Draws";
    private static final String USER_CHOSE_WELL = "User chose well";
    private static final String USER_CHOSE_SCISSORS = "User chose scissors";
    private static final String USER_CHOSE_PAPER = "User chose paper";

    private Statistics(){};

    public static void Initialize(){
        jsonFile = new File(JSON_PATH);
        int games = 0,
            userWins = 0,
            computerWins = 0,
            draws = 0,
            userChoseWell = 0,
            userChoseScissors = 0,
            userChosePaper = 0;

        if (jsonFile.isFile()){
            final List<Integer> jsonInfo = ReadFromJSON();
            if (!jsonInfo.contains(-1)){
                games = jsonInfo.get(0);
                userWins = jsonInfo.get(1);
                computerWins = jsonInfo.get(2);
                draws = jsonInfo.get(3);
                userChoseWell = jsonInfo.get(4);
                userChoseScissors = jsonInfo.get(5);
                userChosePaper = jsonInfo.get(6);
            }
        }

        allTimeData.put(GAMES, games);
        allTimeData.put(USER_WINS, userWins);
        allTimeData.put(COMPUTER_WINS, computerWins);
        allTimeData.put(DRAWS, draws);
        allTimeData.put(USER_CHOSE_WELL, userChoseWell);
        allTimeData.put(USER_CHOSE_SCISSORS, userChoseScissors);
        allTimeData.put(USER_CHOSE_PAPER, userChosePaper);

        allTimeData.forEach((key, _) -> currSessionData.put(key, 0)); //Заповнюємо дані поточної сесії нулями
    }

    private static GameLogic.Choices GetMostFreqUserChoice(final Map<String, Integer> data){
        final int wellFrequency = data.get(USER_CHOSE_WELL);
        final int scissorsFrequency = data.get(USER_CHOSE_SCISSORS);
        final int paperFrequency = data.get(USER_CHOSE_PAPER);

        if (wellFrequency == scissorsFrequency && wellFrequency == paperFrequency)
            return null;

        Map<GameLogic.Choices, Integer> frequencies = new HashMap<>();
        frequencies.put(GameLogic.Choices.Well, wellFrequency);
        frequencies.put(GameLogic.Choices.Scrissors, scissorsFrequency);
        frequencies.put(GameLogic.Choices.Paper, paperFrequency);

        GameLogic.Choices mostFrequent = GameLogic.Choices.Well;
        for (final var entry : frequencies.entrySet()){
            if (entry.getValue() > frequencies.get(mostFrequent))
                mostFrequent = entry.getKey();
        }

        return mostFrequent;
    }

    public static GameLogic.Choices GetMostFreqUserChoiceAllTime(){
        return GetMostFreqUserChoice(allTimeData);
    }

    public static GameLogic.Choices GetMostFreqUserChoiceCurrSession(){
        return GetMostFreqUserChoice(currSessionData);
    }

    public static void ResetCurrentSession(){
        currSessionData.forEach((key, _) -> currSessionData.put(key, 0));
    }

    public static String GetAllTimeDataString(){
        final String result = new StringBuilder()
                .append(String.format("Кількість ігор: %d\n", allTimeData.get(GAMES)))
                .append(String.format("Ваших перемог: %d\n", allTimeData.get(USER_WINS)))
                .append(String.format("Перемог комп'ютера: %d\n", allTimeData.get(COMPUTER_WINS)))
                .append(String.format("Нічиїх: %d\n", allTimeData.get(DRAWS)))
                .append('\n')
                .append(String.format("Ви ходили криницею: %d\n", allTimeData.get(USER_CHOSE_WELL)))
                .append(String.format("Ви ходили ножицями: %d\n", allTimeData.get(USER_CHOSE_SCISSORS)))
                .append(String.format("Ви ходили папіром: %d\n", allTimeData.get(USER_CHOSE_PAPER)))
                .toString();

        return result;
    }

    private static List<Integer> ReadFromJSON(){
        int games = -1, userWins = -1, computerWins = -1, draws = -1, userChoseWell = -1, userChoseScissors = -1, userChosePaper = -1;
        StringBuilder jsonText = new StringBuilder();

        try (Scanner scanner = new Scanner(new FileReader(JSON_PATH))){
            while (scanner.hasNextLine()){
                jsonText.append(scanner.nextLine());
            }

            JSONObject json = new JSONObject(jsonText.toString());

            if (json.has(GAMES))
                games = json.getInt(GAMES);
            if (json.has(USER_WINS))
                userWins = json.getInt(USER_WINS);
            if (json.has(COMPUTER_WINS))
                computerWins = json.getInt(COMPUTER_WINS);
            if (json.has(DRAWS))
                draws = json.getInt(DRAWS);
            if (json.has(USER_CHOSE_WELL))
                userChoseWell = json.getInt(USER_CHOSE_WELL);
            if (json.has(USER_CHOSE_SCISSORS))
                userChoseScissors = json.getInt(USER_CHOSE_SCISSORS);
            if (json.has(USER_CHOSE_PAPER))
                userChosePaper = json.getInt(USER_CHOSE_PAPER);

        }
        catch (IOException e){
            JOptionPane.showMessageDialog(null, "Помилка зчитування JSON: " + e.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
        }

        return Arrays.asList(games, userWins, computerWins, draws, userChoseWell, userChoseScissors, userChosePaper);
    }

    public static void WriteToJSON(){
        //Додаємо статистику поточної сесії до загальної
        allTimeData.forEach((key, value) -> allTimeData.put(key, value + currSessionData.get(key)));

        JSONObject json = new JSONObject(allTimeData);
        try (FileWriter file = new FileWriter(JSON_PATH)){
            file.write(json.toString(4));
        }
        catch (IOException e){
            JOptionPane.showMessageDialog(null, "Помилка запису JSON: " + e.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void UserWon(final GameLogic.Choices userChoice){
        currSessionData.put(GAMES, currSessionData.get(GAMES) + 1);
        currSessionData.put(USER_WINS, currSessionData.get(USER_WINS) + 1);

        switch (userChoice){
            case GameLogic.Choices.Well -> { currSessionData.put(USER_CHOSE_WELL, currSessionData.get(USER_CHOSE_WELL) + 1); }
            case GameLogic.Choices.Scrissors -> { currSessionData.put(USER_CHOSE_SCISSORS, currSessionData.get(USER_CHOSE_SCISSORS) + 1); }
            case GameLogic.Choices.Paper -> { currSessionData.put(USER_CHOSE_PAPER, currSessionData.get(USER_CHOSE_PAPER) + 1); }
        }
    }

    public static void ComputerWon(final GameLogic.Choices userChoice){
        currSessionData.put(GAMES, currSessionData.get(GAMES) + 1);
        currSessionData.put(COMPUTER_WINS, currSessionData.get(COMPUTER_WINS) + 1);

        switch (userChoice){
            case GameLogic.Choices.Well -> { currSessionData.put(USER_CHOSE_WELL, currSessionData.get(USER_CHOSE_WELL) + 1); }
            case GameLogic.Choices.Scrissors -> { currSessionData.put(USER_CHOSE_SCISSORS, currSessionData.get(USER_CHOSE_SCISSORS) + 1); }
            case GameLogic.Choices.Paper -> { currSessionData.put(USER_CHOSE_PAPER, currSessionData.get(USER_CHOSE_PAPER) + 1); }
        }
    }

    public static void Draw(final GameLogic.Choices userChoice){
        currSessionData.put(GAMES, currSessionData.get(GAMES) + 1);
        currSessionData.put(DRAWS, currSessionData.get(DRAWS) + 1);

        switch (userChoice){
            case GameLogic.Choices.Well -> { currSessionData.put(USER_CHOSE_WELL, currSessionData.get(USER_CHOSE_WELL) + 1); }
            case GameLogic.Choices.Scrissors -> { currSessionData.put(USER_CHOSE_SCISSORS, currSessionData.get(USER_CHOSE_SCISSORS) + 1); }
            case GameLogic.Choices.Paper -> { currSessionData.put(USER_CHOSE_PAPER, currSessionData.get(USER_CHOSE_PAPER) + 1); }
        }
    }
}