import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;

@SuppressWarnings("FieldMayBeFinal")
class UI extends JFrame{
    private static int numOfRounds, currRound;
    private static JButton[] userButtons, computerButtons;
    private static JButton nextRoundButton;

    private static JPanel mainPanel;
    private static CardLayout cardLayout;

    private static final String FILE_PATH = "Resources";
    private static final String MENU_PANEL = "Menu";
    private static final String GAME_PANEL = "Game";

    private static void Initialize(){
        Statistics.Initialize();

        JFrame frame = new JFrame("Криниця-Ножиці-Папір");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        final JPanel menuPanel = CreateMenuPanel();
        final JPanel gamePanel = CreateGamePanel();

        //Додаємо панелі в головний контейнер
        mainPanel.add(menuPanel, MENU_PANEL);
        mainPanel.add(gamePanel, GAME_PANEL);

        frame.add(mainPanel);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    //ПАНЕЛЬ 1 (ГОЛОВНЕ МЕНЮ)
    private static JPanel CreateMenuPanel(){
        JPanel menuPanel = new JPanel(cardLayout);
        menuPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.ipady = 7;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        //Вибір кількості раундів гри
        menuPanel.add(new JLabel("Кількість раундів:  "));
        JTextField textField = new JTextField();
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new IntegerFilter());
        ++gbc.gridx;
        menuPanel.add(textField, gbc);

        //Випадаючий список з вибором режиму гри
        final String[] gameModes = {
            "Випадкові ходи комп’ютера",
            "На основі ходів гравця протягом поточної гри",
            "На основі ходів гравця за весь час"
        };
        JComboBox<String> comboBox = new JComboBox<>(gameModes);
        comboBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        ++gbc.gridy;
        menuPanel.add(comboBox, gbc);

        //Кнопка гри
        JButton startGameButton = new JButton("Почати гру");
        startGameButton.setEnabled(false);
        startGameButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ++gbc.gridy;
        menuPanel.add(startGameButton, gbc);

        //Кнопка статистики
        JButton statisticsButton = new JButton("Статистика");
        statisticsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ++gbc.gridy;
        menuPanel.add(statisticsButton, gbc);

        //Якщо поле не пусте, вмикаємо кнопку старту
        textField.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e){
                UpdateButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e){
                UpdateButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e){
                UpdateButtonState();
            }

            private void UpdateButtonState(){
                startGameButton.setEnabled(!textField.getText().isEmpty());
            }
        });

        //Налаштування слухачів кнопок
        SetupMenuButtons(startGameButton, comboBox, textField, statisticsButton);

        return menuPanel;
    }

    private static void SetupMenuButtons(final JButton startGameButton, final JComboBox<String> comboBox, final JTextField textField, final JButton statisticsButton){
        startGameButton.addActionListener(_ -> {
            switch (comboBox.getSelectedIndex()){
                case 0 -> GameLogic.SetGameMode(GameLogic.GameModes.Random);
                case 1 -> GameLogic.SetGameMode(GameLogic.GameModes.BasedOnCurrSession);
                case 2 -> GameLogic.SetGameMode(GameLogic.GameModes.BasedOnPrevSessions);
            }
            currRound = 1;
            numOfRounds = Integer.parseInt(textField.getText());
            UpdateNextRoundButton(currRound, numOfRounds);
            cardLayout.show(mainPanel, GAME_PANEL);
            Statistics.ResetCurrentSession();
        });

        statisticsButton.addActionListener(_ -> {
            JOptionPane.showMessageDialog(null, Statistics.GetAllTimeDataString(), "Статистика ігор", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    //ПАНЕЛЬ 2 (ГРА)
    private static JPanel CreateGamePanel(){
        JPanel gamePanel = new JPanel(cardLayout);
        gamePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 10, 30, 10);
        gbc.ipady = 7;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 3;

        final JLabel us = new JLabel("----------Ви----------");
        us.setHorizontalAlignment(SwingConstants.CENTER);
        gamePanel.add(us, gbc);
        
        final ImageIcon[] icons = LoadIcons(new String[]{
            String.format("%s/well.png", FILE_PATH),
            String.format("%s/scissors.png", FILE_PATH),
            String.format("%s/paper.png", FILE_PATH)
        });

        userButtons = CreateImageButtons(icons, new Dimension(100, 100), true);
        computerButtons = CreateImageButtons(icons, new Dimension(100, 100), false);

        gbc.gridx = 0;
        ++gbc.gridy;
        gbc.gridwidth = 1;
        for (int i = 0; i < userButtons.length; ++i){
            gamePanel.add(userButtons[i], gbc);
            ++gbc.gridx;
        }

        gbc.gridx = 0;
        ++gbc.gridy;
        gbc.gridwidth = 3;
        final JLabel computerLabel = new JLabel("----------Комп'ютер----------");
        computerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gamePanel.add(computerLabel, gbc);

        gbc.gridx = 0;
        ++gbc.gridy;
        gbc.gridwidth = 1;
        for (int i = 0; i < computerButtons.length; ++i){
            gamePanel.add(computerButtons[i], gbc);
            ++gbc.gridx;
        }

        nextRoundButton = new JButton();
        nextRoundButton.setEnabled(false);
        nextRoundButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 0;
        ++gbc.gridy;
        gbc.gridwidth = 3;
        gamePanel.add(nextRoundButton, gbc);

        //Слухачі кнопок гравця
        SetupPlayerButtons();

        return gamePanel;
    }

    private static void SetupPlayerButtons(){
        userButtons[0].addActionListener(_ -> ShowResultsOfThisRound(GameLogic.Choices.Well));
        userButtons[1].addActionListener(_ -> ShowResultsOfThisRound(GameLogic.Choices.Scrissors));
        userButtons[2].addActionListener(_ -> ShowResultsOfThisRound(GameLogic.Choices.Paper));
        nextRoundButton.addActionListener(_ -> {
            nextRoundButton.setEnabled(false);
            ResetButtons();
            if (currRound != numOfRounds)
                UpdateNextRoundButton(++currRound, numOfRounds);
            else
                cardLayout.show(mainPanel, MENU_PANEL);
        });
    }

    private static void ShowResultsOfThisRound(final GameLogic.Choices userChoice){
        final GameLogic.Choices computerChoice = GameLogic.GetComputerChoice();
        
        DeactivateUserButtons();

        switch (GameLogic.ChooseWinner(userChoice, computerChoice)){
            case GameLogic.Winners.User -> {
                PaintButtons(userChoice, computerChoice, GameLogic.Winners.User);
                Statistics.UserWon(userChoice);
            }

            case GameLogic.Winners.Computer -> {
                PaintButtons(userChoice, computerChoice, GameLogic.Winners.Computer);
                Statistics.ComputerWon(userChoice);
            }

            case GameLogic.Winners.Draw -> {
                PaintButtons(userChoice, computerChoice, GameLogic.Winners.Draw);
                Statistics.Draw(userChoice);
            }
        }

        nextRoundButton.setEnabled(true);
        Statistics.WriteToJSON();
    }

    private static void ResetButtons(){
        for (JButton button : userButtons){
            button.setEnabled(true);
            button.setBorder(UIManager.getBorder("Button.border"));
        }

        for (JButton button : computerButtons)
            button.setBorder(UIManager.getBorder("Button.border"));
    }

    private static void DeactivateUserButtons(){
        for (JButton button : userButtons){
            button.setEnabled(false);
        }
    }

    public static void PaintButtons(final GameLogic.Choices userChoice, final GameLogic.Choices computerChoice, final GameLogic.Winners winner){
        final int userButtonIndex = ChoiceToIndex(userChoice);
        final int computerButtonIndex = ChoiceToIndex(computerChoice);

        switch (winner){
            case GameLogic.Winners.User -> {
                userButtons[userButtonIndex].setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                computerButtons[computerButtonIndex].setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            }
            
            case GameLogic.Winners.Computer -> {
                userButtons[userButtonIndex].setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                computerButtons[computerButtonIndex].setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
            }

            case GameLogic.Winners.Draw -> {
                userButtons[userButtonIndex].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
                computerButtons[computerButtonIndex].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
            }
        }
    }

    private static ImageIcon[] LoadIcons(String[] imagePaths){
        ImageIcon[] icons = new ImageIcon[imagePaths.length];

        for (int i = 0; i < imagePaths.length; ++i){
            icons[i] = new ImageIcon(imagePaths[i]);
        }

        return icons;
    }

    private static JButton[] CreateImageButtons(ImageIcon[] icons, final Dimension iconSize, final boolean active){
        JButton[] buttons = new JButton[icons.length];
        Image scaled;

        for (int i = 0; i < icons.length; ++i){
            scaled = icons[i].getImage().getScaledInstance(iconSize.width, iconSize.height, Image.SCALE_SMOOTH);
            icons[i] = new ImageIcon(scaled);

            buttons[i] = new JButton(icons[i]);
            buttons[i].setPreferredSize(iconSize);
            buttons[i].setMaximumSize(iconSize);
            buttons[i].setMinimumSize(iconSize);
            buttons[i].setEnabled(active);
            buttons[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            buttons[i].setContentAreaFilled(false);
            buttons[i].setBackground(Color.WHITE);
            buttons[i].setFocusPainted(false);
        }

        return buttons;
    }

    public static void UpdateNextRoundButton(final int currRound, final int numOfRounds){
        if (currRound == numOfRounds)
            nextRoundButton.setText(String.format("Завершити (%d/%d)", currRound, numOfRounds));
        else
            nextRoundButton.setText(String.format("Наступний раунд (%d/%d)", currRound, numOfRounds));
    }

    private static int ChoiceToIndex(final GameLogic.Choices choice){
        int index = -1;
        
        switch (choice){
            case GameLogic.Choices.Well -> { index = 0; }
            case GameLogic.Choices.Scrissors -> { index = 1; }
            case GameLogic.Choices.Paper -> { index = 2; }
        }

        return index;
    }

    public static GameLogic.Choices IndexToChoice(final int index){
        GameLogic.Choices choice = null;
        
        switch (index){
            case 0 -> { choice = GameLogic.Choices.Well; }
            case 1 -> { choice = GameLogic.Choices.Scrissors; }
            case 2 -> { choice = GameLogic.Choices.Paper; }
        }

        return choice;
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> UI.Initialize());
    }
}

class IntegerFilter extends DocumentFilter{
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException{
        String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
        
        if (newText.matches("[1-9]\\d*")){ //Тільки додатні цілі числа
            super.replace(fb, offset, length, text, attrs);
        }
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException{
        replace(fb, offset, 0, text, attr);
    }
}