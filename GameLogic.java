import java.util.concurrent.ThreadLocalRandom;

class GameLogic{
    private static GameModes gameMode;

    public static enum GameModes{
        Random,
        BasedOnCurrSession,
        BasedOnPrevSessions
    };

    public static enum Choices{
        Well,
        Scrissors,
        Paper
    };

    public static enum Winners{
        User,
        Computer,
        Draw
    };

    public static void SetGameMode(final GameModes gameMode){ GameLogic.gameMode = gameMode; }

    public static Choices GetComputerChoice(){
        Choices computerChoice = null;

        switch (gameMode){
            case GameModes.Random -> {
                computerChoice = MakeRandomChoice();
            }

            case GameModes.BasedOnCurrSession -> {
                computerChoice = MakeChoiceBasedOnCurrSession();
            }

            case GameModes.BasedOnPrevSessions -> {
                computerChoice = MakeChoiceBasedOnPrevSessions();
            }
        }

        return computerChoice;
    }

    private static Choices MakeRandomChoice(){
        return UI.IndexToChoice(ThreadLocalRandom.current().nextInt(0, 3));
    }

    private static Choices MakeChoiceBasedOnCurrSession(){
        Choices choice = null;

        switch (Statistics.GetMostFreqUserChoiceCurrSession()){
            case Choices.Well -> { choice = Choices.Paper; }
            case Choices.Scrissors -> { choice = Choices.Well; }
            case Choices.Paper -> { choice = Choices.Scrissors; }
            case null -> { choice = MakeRandomChoice(); }
        }

        return choice;
    }

    private static Choices MakeChoiceBasedOnPrevSessions(){
        Choices choice = null;

        switch (Statistics.GetMostFreqUserChoiceAllTime()){
            case Choices.Well -> { choice = Choices.Paper; }
            case Choices.Scrissors -> { choice = Choices.Well; }
            case Choices.Paper -> { choice = Choices.Scrissors; }
            case null -> { choice = MakeRandomChoice(); }
        }

        return choice;
    }

    public static Winners ChooseWinner(final Choices userChoice, final Choices computerChoice){
        Winners winner = null;

        switch (userChoice){
            case Choices.Well -> {
                switch (computerChoice){
                    case Choices.Well -> winner = Winners.Draw;
                    case Choices.Scrissors -> winner = Winners.User;
                    case Choices.Paper -> winner = Winners.Computer;
                }
            }

            case Choices.Scrissors -> {
                switch (computerChoice){
                    case Choices.Well -> winner = Winners.Computer;
                    case Choices.Scrissors -> winner = Winners.Draw;
                    case Choices.Paper -> winner = Winners.User;
                }
            }

            case Choices.Paper -> {
                switch (computerChoice){
                    case Choices.Well -> winner = Winners.User;
                    case Choices.Scrissors -> winner = Winners.Computer;
                    case Choices.Paper -> winner = Winners.Draw;
                }
            }
        }

        return winner;
    }
}