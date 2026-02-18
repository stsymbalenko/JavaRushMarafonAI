package com.javarush.halloween;

import io.github.cdimascio.dotenv.Dotenv;
import org.checkerframework.checker.units.qual.A;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.Path;

public class HalloweenBoltApp extends SimpleTelegramBot {

    public HalloweenBoltApp(String token) {
        super(token);
    }
    private AIService aiService = new AIService();
    private AppMode mode;
    String imageType = "create_anime";

    //TODO: основний функціонал бота писатимемо тут
    public void startCommand(){
        mode = AppMode.MAIN;

        //Отримання ID користувача
        String currentChatId = getCurrentChatId();

        //Створити папку користувача
        createUserDir(currentChatId);

        hideMainMenu();

        showMainMenu(
                "start", "🧟‍♂️ Головне меню бота",
                "image", "⚰️ Створюємо зображення",
                "edit", "🧙‍♂️ Змінюємо зображення",
                "merge", "🕷️ Об'єднуємо зображення",
                "party", "🎃 Фото для Halloween-вечірки",
                "video", "🎬☠️ Моторошне Halloween-відео з фото"
        );

        sendPhotoMessage("main");
        sendTextMessage(loadMessage("main"));
    }

    public void  imageCommand(){
        mode = AppMode.CREATE;

        sendPhotoMessage("create");

        sendTextButtonsCheckMessage (loadMessage("create"), imageType,
                "create_anime", "\uD83D\uDC67 Аніме",
                "create_photo", "\uD83D\uDCF8 Фото");
    }

    public void imageMessage(){
        String text = getMessageText();
        String userId = getCurrentChatId();
        Path photoPath = Path.of("users/" + userId + "/photo.jpg");

        String prompt = loadPrompt(imageType);
        aiService.createImage(prompt + text, photoPath);
        sendPhotoMessage(photoPath);
    }

    public void editCommand(){
        mode = AppMode.EDIT;
        sendPhotoMessage("edit");
        sendTextMessage(loadMessage("edit"));
    }

    public void editMessage(){
        String text = getMessageText();
        String userId = getCurrentChatId();
        Path photoPath = Path.of("users/" + userId + "/photo.jpg");

        String prompt = loadPrompt("edit");
        aiService.editImage(photoPath, prompt + text, photoPath);
        sendPhotoMessage(photoPath);
    }

    public void onMessage(){
        if(mode == AppMode.CREATE){
            imageMessage();
        }else if(mode==AppMode.EDIT){
            editMessage();
        }
        else{
            String userInputMessage = getMessageText();

            sendTextMessage("*Привіт!*");
            sendTextMessage("Як справи, друже?");
            sendTextMessage("Ти написав: " + userInputMessage);
        }
    }



    public void  imageButtonCallback(){
        imageType = getButtonKey();

        String text = loadMessage("create");
        Message message = getButtonMessage();
        updateMessage(message, text, imageType,
                "create_anime", "\uD83D\uDC67 Аніме",
                "create_photo", "\uD83D\uDCF8 Фото");
    }

    //Ініціалізація застосунку. Додаємо обробники подій
    @Override
    public void onInitialize() {
        //TODO: і ще трохи тут
        addCommandHandler("start", this::startCommand);
        addCommandHandler("image", this::imageCommand);
        addCommandHandler("edit", this::editCommand);

        addMessageTextHandler(this::onMessage);
        addButtonHandler("^create_.*", this::imageButtonCallback);

    }

    //Режими роботи
    enum AppMode{
        MAIN,
        CREATE,
        EDIT,
        MERGE,
        PARTY,
        VIDEO
    }

    // Створюємо Telegram-бота
    public static void main(String[] args) throws TelegramApiException {
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        String telegramToken = env.get("TELEGRAM_TOKEN");

        var botsApplication = new TelegramBotsLongPollingApplication();
        botsApplication.registerBot(telegramToken, new HalloweenBoltApp(telegramToken));
    }
}