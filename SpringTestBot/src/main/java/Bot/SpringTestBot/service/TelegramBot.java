package Bot.SpringTestBot.service;

import Bot.SpringTestBot.config.BotConfig;
import Bot.SpringTestBot.model.Good;
import Bot.SpringTestBot.model.GoodRepository;
import Bot.SpringTestBot.model.User;
import Bot.SpringTestBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;



@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoodRepository goodRepository;

    final BotConfig config;

    static final String HELP_TEXT = "Этот бот создан для покупки/продажи различных вещей\n\n" +
            "Вы можете выбрать нужную команду в меню или ввести её вручную:\n\n" +
            "Напишите /start для регистрации\n\n" +
            "Напишите /mydata чтобы увидеть информацию о себе\n\n" +
            "Напишите /market для просмотра всего списка товаров\n\n" +
            "Напишите /addgood чтобы выставить свой товар на продажу\n\n" +
            "Напишите /mygoods для просмотра списка своих товаров\n\n" +
            "Также существуют команды /setname, /setprice, /setdescription, предназначенные для добавления имени, цены и описания товара соответственно (данные указываются после комманды через пробел(/setname name1)). Они применяются именно в таком порядке после входа в режим добавления товара с помощью команды /addgood\n\n" +
            "Команда /deletemygood предназначена для удаления товара из списка товаров пользователя (номер товара для удаления указываются после команды через пробел(/deletemygood 1))\n\n" +
            "Напишите /help чтобы увидеть это сообщение ещё раз\n\n";

    /*static final String YES_BUTTON="YES_BUTTON";
    static final String NO_BUTTON="NO_BUTTON";*/
    boolean isAdding = false;
    boolean isNameAdded = false;
    boolean isPriceAdded = false;
    boolean isDescriptionAdded = false;
    boolean isPhotoAdded = false;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "зарегистрироваться"));
        listOfCommands.add(new BotCommand("/mydata", "посмотреть информацию о себе"));
        listOfCommands.add(new BotCommand("/market", "все товары"));
        listOfCommands.add(new BotCommand("/addgood", "выставить товар на продажу"));
        listOfCommands.add(new BotCommand("/mygoods", "ваши товары"));
        listOfCommands.add(new BotCommand("/help", "информация о боте"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException e) {
            log.error("Error setting bot`s command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.contains("/setname")) {
                if (isAdding) {
                    if (isNameAdded) {
                        prepareAndSendMessage(chatId, "Вы уже добавили название товара, перейдите к цене /setprice");
                    } else {
                        String name = messageText.substring(messageText.indexOf(" "));
                        Optional<User> userOptional = userRepository.findById(chatId);
                        User user = userOptional.get();
                        Optional<Good> goodOptional = goodRepository.findById(chatId + user.getNumberOfGoods());
                        Good good = goodOptional.get();
                        good.setGoodName(name);
                        goodRepository.save(good);
                        isNameAdded = true;
                        prepareAndSendMessage(chatId, "Название товара установлено");
                    }
                } else {
                    prepareAndSendMessage(chatId, "Вы не активировали добавление товара. Если хотите добавить товар используйте команду /addgood");
                }
//                if (good.getGoodName() != null && good.getPrice() != null && good.getGoodDescription() != null && good.getImageBytes() != null) {
//                    isAdding = false;
//                    prepareAndSendMessage(chatId, "Товар успешно добавлен!");
//                }
//                else if (good.getGoodName() != null && good.getPrice() != null && good.getGoodDescription() != null && good.getImageBytes() == null) {
//                    prepareAndSendMessage(chatId, "Осталось добавить фото");
//                }
//                else if (good.getGoodName() != null && good.getPrice() != null && good.getGoodDescription() == null && good.getImageBytes() != null) {
//                    prepareAndSendMessage(chatId, "Осталось добавить описание");
//                }
//                else if (good.getGoodName() != null && good.getPrice() == null && good.getGoodDescription() != null && good.getImageBytes() != null) {
//                    prepareAndSendMessage(chatId, "Осталось добавить цену");
//                }
//                else if (good.getGoodName() == null && good.getPrice() != null && good.getGoodDescription() != null && good.getImageBytes() != null) {
//                    prepareAndSendMessage(chatId, "Осталось добавить имя");
//                }
            } else if (messageText.contains("/setprice")) {
                if (isAdding) {
                if (isPriceAdded) {
                    prepareAndSendMessage(chatId, "Вы уже добавили цену товара, перейдите к описанию /setdescription");
                } else {
                    if (isNameAdded) {
                        String price = messageText.substring(messageText.indexOf(" "));
                        Optional<User> userOptional = userRepository.findById(chatId);
                        User user = userOptional.get();
                        Optional<Good> goodOptional = goodRepository.findById(chatId + user.getNumberOfGoods());
                        Good good = goodOptional.get();
                        good.setPrice(price);
                        goodRepository.save(good);
                        isPriceAdded = true;
                        prepareAndSendMessage(chatId, "Цена товара установлена");
                    } else {
                        prepareAndSendMessage(chatId, "Сначала добавьте название товара с помощью команды /setname");
                    }
                }
                } else {
                        prepareAndSendMessage(chatId, "Вы не активировали добавление товара. Если хотите добавить товар используйте команду /addgood");
                }
            } else if (messageText.contains("/setdescription")) {
                if (isAdding) {
                if (isDescriptionAdded) {
                    prepareAndSendMessage(chatId, "Вы уже добавили описание товара, отправьте фото");
                } else {
                    if (isPriceAdded) {
                        String description = messageText.substring(messageText.indexOf(" "));
                        Optional<User> userOptional = userRepository.findById(chatId);
                        User user = userOptional.get();
                        Optional<Good> goodOptional = goodRepository.findById(chatId + user.getNumberOfGoods());
                        Good good = goodOptional.get();
                        good.setGoodDescription(description);
                        goodRepository.save(good);
                        isDescriptionAdded = true;
                        prepareAndSendMessage(chatId, "Описание товара установлено");
                    } else if (isNameAdded) {
                        prepareAndSendMessage(chatId, "Сначала добавьте цену товара с помощью команды /setprice");
                    } else {
                        prepareAndSendMessage(chatId, "Сначала добавьте название товара с помощью команды /setname, потом цену с помощью /setprice");
                    }
                }
                } else {
                    prepareAndSendMessage(chatId, "Вы не активировали добавление товара. Если хотите добавить товар используйте команду /addgood");
                }
            } else if (messageText.contains("/deletemygood")) {
                if (isAdding) {
                    prepareAndSendMessage(chatId, "Вы в режиме добавления товара и не можете выполнять команды кроме /setname, /setprice, /setdescription и отправки изображения");
                }
                else {
                    String number = messageText.substring(messageText.indexOf(" ") + 1);
                    Optional<User> userOptional = userRepository.findById(chatId);
                    User user = userOptional.get();
                    if (goodRepository.existsById(chatId + Integer.parseInt(number))) {
                        goodRepository.deleteById(chatId + Integer.parseInt(number));
                        for (int j = Integer.parseInt(number) + 1; j <= user.getNumberOfGoods(); j++) {
                            if (goodRepository.existsById(chatId + j)) {
                                Optional<Good> goodOptional = goodRepository.findById(chatId + j);
                                Good good = goodOptional.get();
                                good.setiD(chatId + j - 1);
                                goodRepository.save(good);
                                goodRepository.deleteById(chatId + j);
                            }
                        }
                        user.setNumberOfGoods(user.getNumberOfGoods() - 1);
                        userRepository.save(user);
                        prepareAndSendMessage(chatId, "Товар под номером " + number + " удалён");
                    } else {
                        prepareAndSendMessage(chatId, "Товар с таким номером не существует");
                    }
                }
            }
            else {
                switch (messageText) {
                    case "/start":
                        if (isAdding) {
                            prepareAndSendMessage(chatId, "Вы в режиме добавления товара и не можете выполнять команды кроме /setname, /setprice, /setdescription и отправки изображения");
                        }
                        else {
                            registerUser(update.getMessage());
                            startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        }
                        break;
                    case "/help":
                        if (isAdding) {
                            prepareAndSendMessage(chatId, "Вы в режиме добавления товара и не можете выполнять команды кроме /setname, /setprice, /setdescription и отправки изображения");
                        }
                        else {
                            prepareAndSendMessage(chatId, HELP_TEXT);
                        }
                        break;
                    case "/mydata":
                        if (isAdding) {
                            prepareAndSendMessage(chatId, "Вы в режиме добавления товара и не можете выполнять команды кроме /setname, /setprice, /setdescription и отправки изображения");
                        }
                        else {
                            mydataCommandReceived(chatId);
                        }
                        break;
                    case "/addgood":
                        if (isAdding) {
                            prepareAndSendMessage(chatId, "Вы уже в режиме добавления товара и не можете выполнять команды кроме /setname, /setprice, /setdescription и отправки изображения");
                        } else {
                            isAdding = true;
                            Good good = new Good();
                            Optional<User> userOptional = userRepository.findById(chatId);
                            User user = userOptional.get();
                            user.setNumberOfGoods(user.getNumberOfGoods() + 1);
                            userRepository.save(user);//добавить пользователю счётчик товаров и к id добалять его
                            good.setiD(user.getChatId() + user.getNumberOfGoods());
                            good.setSellerUserName(user.getUserName());
                            goodRepository.save(good);
                            prepareAndSendMessage(chatId, "Введите название, цену, описание и фото товара в этом порядке с помощью команд /setname, /setprice и /setdescription (характеристика вводится через пробел после команды) и отправьте фото (без команды):");
                        }
                        break;
                    case "/market":
                        if (isAdding) {
                            prepareAndSendMessage(chatId, "Вы в режиме добавления товара и не можете выполнять команды кроме /setname, /setprice, /setdescription и отправки изображения");
                        }
                        else {
                            var goods = goodRepository.findAll();
                            for (Good element : goods) {
                                prepareAndSendMessage(chatId, "--------------------------");
                                prepareAndSendMessage(chatId, "\n\nНазвание: " + element.getGoodName() +
                                        "\n\nОписание: " + element.getGoodDescription() +
                                        "\n\nЦена: " + element.getPrice() + "\n\nПродавец: @" + element.getSellerUserName());
                                if (element.getImageBytes() != null) {
                                    byte[] imageBytes = element.getImageBytes();
                                    sendImageToUser(chatId, imageBytes);
                                }
                                prepareAndSendMessage(chatId, "--------------------------");
                            }
                        }
                        break;
                    case "/mygoods":
                        if (isAdding) {
                            prepareAndSendMessage(chatId, "Вы в режиме добавления товара и не можете выполнять команды кроме /setname, /setprice, /setdescription и отправки изображения");
                        }
                        else {
                            Optional<User> userOptional = userRepository.findById(chatId);
                            User user = userOptional.get();
                            prepareAndSendMessage(chatId, "Список ваших товаров:");
                            var goods = goodRepository.findAll();
                            int i = 0;
                            for (Good element : goods) {
                                if (element.getiD() >= user.getChatId() && element.getiD() <= user.getChatId() + user.getNumberOfGoods()) {
                                    i++;
                                    prepareAndSendMessage(chatId, "-------------" + i + "-------------");
                                    prepareAndSendMessage(chatId, "\n\nНазвание: " + element.getGoodName() +
                                            "\n\nОписание: " + element.getGoodDescription() +
                                            "\n\nЦена: " + element.getPrice() + "\n\nПродавец: @" + element.getSellerUserName());
                                    if (element.getImageBytes() != null) {
                                        byte[] imageBytes = element.getImageBytes();
                                        sendImageToUser(chatId, imageBytes);
                                    }
                                    prepareAndSendMessage(chatId, "--------------------------");
                                }
                            }
                        }
                        break;
                    default:
                        prepareAndSendMessage(chatId, "Такой команды не существует!");
                }
            }
            //else if (update.hasCallbackQuery()) {
            //String callbackData = update.getCallbackQuery().getData();
            //long messageId = update.getCallbackQuery().getMessage().getMessageId();
            //long chatId = update.getCallbackQuery().getMessage().getChatId();
            //if (callbackData.equals(YES_BUTTON)) {
            //String text = "You pressed YES button";
            //executeEditMessageText(text, chatId, messageId);
            //}
            //else if (callbackData.equals(NO_BUTTON)) {
            //String text = "You pressed NO button";
            //executeEditMessageText(text, chatId, messageId);
            //}
            //}
        }
        else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            if (isAdding) {
                if (isDescriptionAdded) {
                    long chatId = update.getMessage().getChatId();
                    byte[] imageBytes = null;

                    List<PhotoSize> photos = update.getMessage().getPhoto();
                    String fileId = photos.get(photos.size() - 1).getFileId();

                    try {
                        imageBytes = downloadPhotoByFileId(fileId);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                    if (imageBytes != null) {
                        Optional<User> userOptional = userRepository.findById(chatId);
                        User user = userOptional.get();
                        Optional<Good> goodOptional = goodRepository.findById(chatId + user.getNumberOfGoods());
                        Good good = goodOptional.get();
                        good.setImageBytes(imageBytes);
                        goodRepository.save(good);
                        isPhotoAdded = true;
                        prepareAndSendMessage(chatId, "Фото товара установлено");
                        if (isNameAdded && isPriceAdded && isDescriptionAdded && isPhotoAdded) {
                            prepareAndSendMessage(chatId, "Товар успешно добавлен!");
                            isAdding = false;
                            isNameAdded = false;
                            isPriceAdded = false;
                            isDescriptionAdded = false;
                            isPhotoAdded = false;
                        }
                    }
                } else if (isNameAdded && isPriceAdded) {
                    long chatId = update.getMessage().getChatId();
                    prepareAndSendMessage(chatId, "Сначала добавьте описание товара с помощью /setdescription");
                } else if (isNameAdded) {
                    long chatId = update.getMessage().getChatId();
                    prepareAndSendMessage(chatId, "Сначала добавьте цену товара с помощь /setprice, потом описание с помощью /setdescription");
                } else {
                    long chatId = update.getMessage().getChatId();
                    prepareAndSendMessage(chatId, "Сначала добавьте название товара с помощью /setname, потом цену товара с помощь /setprice, потом описание с помощью /setdescription");
                }
            } else {
                long chatId = update.getMessage().getChatId();
                prepareAndSendMessage(chatId, "Вы не активировали добавление товара. Если хотите добавить товар используйте команду /addgood");
            }
        }
    }

    public void sendImageToUser(long chatId, byte[] imageBytes) {
        try {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);

            InputFile inputFile = new InputFile();
            inputFile.setMedia(new ByteArrayInputStream(imageBytes), "temp_image.jpg");

            sendPhoto.setPhoto(inputFile);

            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    private byte[] downloadPhotoByFileId(String fileId) throws TelegramApiException {
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);
        org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);

        if (file != null) {
            try {
                URL url = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream is = connection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }

                is.close();
                connection.disconnect();

                return baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /*private void addgoodCommandReceived(long chatId, String messageText) {
        Good good = new Good();
        Optional<User> userOptional = userRepository.findById(chatId);
        User user = userOptional.get();
        good.setSellerUserName(user.getUserName());


            prepareAndSendMessage(chatId, "Enter good`s name");
            good.setGoodName(messageText);

            prepareAndSendMessage(chatId, "Enter good`s description");
            good.setGoodDescription(messageText);

            prepareAndSendMessage(chatId, "Enter good`s price");
            good.setPrice(messageText);

    }*/


    /*private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want to register?");
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData("YES_BUTTON");
        var noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData("NO_BUTTON");
        rowInLine.add(yesButton);
        rowInLine.add(noButton);
        rowsInLine.add(rowInLine);
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        executeMessage(message);
    }*/

    private void startCommandReceived(long chatId, String name) {

        String answer = "Привет, " + name + ", приятно познакомиться!";
        log.info("Replied to user " + name);

        prepareAndSendMessage(chatId, answer);

    }

    private void mydataCommandReceived(long chatId) {
        Optional<User> userOptional = userRepository.findById(chatId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String answer = "Информация о тебе:\n\nИмя: " + user.getFirstName() + "\n\nФамилия: " + user.getLastName() + "\n\nUsername: " + user.getUserName() + "\n\nКоличество товаров: " + user.getNumberOfGoods();
            prepareAndSendMessage(chatId, answer);
        }
    }

    /*private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("weather");
        row.add("get rndom joke");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("register");
        row.add("check my data");
        row.add("delete my data");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }*/

    private void registerUser(Message msg){

        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setNumberOfGoods(0);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    /*private void executeEditMessageText(String text, long chatId, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int)messageId);

        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }*/

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

}
