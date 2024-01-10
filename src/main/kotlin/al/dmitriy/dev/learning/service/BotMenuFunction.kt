package al.dmitriy.dev.learning.service

import al.dmitriy.dev.learning.extendfunctions.putData
import al.dmitriy.dev.learning.service.dataunit.LessonUnit
import al.dmitriy.dev.learning.lesson.Lessons
import al.dmitriy.dev.learning.model.UserData
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import al.dmitriy.dev.learning.apptexts.textForButtonMenu
import al.dmitriy.dev.learning.apptexts.textForOff
import al.dmitriy.dev.learning.apptexts.textForOn

class BotMenuFunction : BotMenuInterface {

    // Проверка валидности введенного текста
    fun isTextIncorrect(messageText: String, lessonCategory: String): Boolean{
        return messageText.contains("⚙") || messageText.contains("\uD83D\uDCDA") || messageText.contains("/") ||
                messageText.contains("#") || messageText.contains("*") || (lessonCategory == Lessons.LEARN_WORDS.title &&
                messageText.length > 15 ) || (messageText.split(" ").size > 10) || messageText.length > 70 ||
                messageText.contains("  ")
    }

    // Список уроков (категорий)
    fun categoryMenu(stringChatId: String, messageText: String, categories: List<String>): SendMessage {
        val sendMessage = SendMessage(stringChatId, messageText)
        sendMessage.replyMarkup = createButtonMenu(categories)
        return sendMessage
    }

    // Изменённое сообщение с экранными кнопками
    fun receiveButtonEditMessage(stringChatId: String, messageId: Int,
                                 messageText: String, callBackData: String, buttonTexts: List<String>): EditMessageText {
        val editMessageText = EditMessageText()
        editMessageText.putData(stringChatId, messageId, messageText)
        editMessageText.replyMarkup = createDataButtonMenu(buttonTexts, callBackData)
        return editMessageText
    }

    // Меню пользовательских настроек
    fun receiveSettingMenu(stringChatId: String, messageText: String, isOnlyUsersText: Boolean,
                           isShowHint: Boolean, isSendTrainingMessage: Boolean, sinceTime: Int, untilTime: Int): SendMessage {
        val sendMessage = SendMessage(stringChatId, messageText)
        sendMessage.replyMarkup = receiveButtonsForSettingMenu(isOnlyUsersText, isShowHint, isSendTrainingMessage, sinceTime, untilTime)
        return sendMessage
    }

    // Меню пользовательских настроек
    fun receiveSettingMenu(stringChatId: String, messageId: Int, messageText: String, isOnlyUsersText: Boolean,
                           isShowHint: Boolean, isSendTrainingMessage: Boolean, sinceTime: Int, untilTime: Int): EditMessageText {
        val editMessageText = EditMessageText()
        editMessageText.putData(stringChatId, messageId, messageText)
        editMessageText.replyMarkup = receiveButtonsForSettingMenu(isOnlyUsersText, isShowHint, isSendTrainingMessage, sinceTime, untilTime)
        return editMessageText
    }

    // Список уроков других пользователей
    fun receiveCategoryMenu(stringChatId: String, messageId: Int, callbackData: String,
                            messageText: String, categories: List<String>): EditMessageText {
        val editMessageText = EditMessageText()
        editMessageText.putData(stringChatId, messageId, messageText)
        editMessageText.replyMarkup = createDataButtonMenu(categories, callbackData)
        return editMessageText
    }

    // Меню администратора
    fun receiveInfoMenuForAdmin(stringChatId: String, intMessageId: Int,
                                messageText: String, categories: List<String>): EditMessageText {
        val editMessageText = EditMessageText()
        editMessageText.putData(stringChatId, intMessageId, messageText)
        editMessageText.replyMarkup = createButtonMenu(categories)
        return editMessageText
    }

    // Экранные кнопки
    override fun createButtonMenu(textForButton: List<String>): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()

        for (element in textForButton) {
            val rowInlineButton = ArrayList<InlineKeyboardButton>()
            val button = InlineKeyboardButton()
            button.putData(element, element)
            rowInlineButton.add(button)
            rowsInline.add(rowInlineButton)
        }
        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }

    // Экранные кнопки с добавленным текстом для callBackData
    override fun createDataButtonMenu(textForButton: List<String>, callBackData: String): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()

        for (element in textForButton) {
            val rowInlineButton = ArrayList<InlineKeyboardButton>()
            val button = InlineKeyboardButton()
            button.putData(element, callBackData + element)
            rowInlineButton.add(button)
            rowsInline.add(rowInlineButton)
        }
        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }

    // Сообщение и экранные кнопки с текстом урока
    fun createLessonButtonMenu(stringChatId: String, messageId: Int, properlyLessonAnswer: Int,
                               lessonUnit: LessonUnit, dataText: String): EditMessageText {
        var space = ""
        val textForMessage = "Правильных ответов: $properlyLessonAnswer$textForButtonMenu" +
                lessonUnit.russianText + "\n\n✏ " + lessonUnit.inputText

        val editMessageText = EditMessageText()
        editMessageText.putData(stringChatId, messageId, textForMessage)

        val buttonText = mutableListOf<String>()
        buttonText.addAll(lessonUnit.forButtonText)

        val splitInputText = lessonUnit.inputText.split(" ")

        for (i in splitInputText) {
            for (x in 0 until buttonText.size) {
                if (i == buttonText[x]) {

                    val wordLength = buttonText[x].length
                    repeat(wordLength) {
                        space += "ㅤ"
                    }
                    buttonText[x] = space
                    space = ""
                    break
                }
            }
        }
        val dataString = if (lessonUnit.inputText.split(" ").size ==
            lessonUnit.englishText.split(" ").size) callData_endOfTxt else dataText  //

        val inlineKeyboardMarkup: InlineKeyboardMarkup = createLessonKeyBoard(buttonText, dataString)
        editMessageText.replyMarkup = inlineKeyboardMarkup
        return editMessageText
    }

    // Меню для удаления текстов уроков из бд
    fun createMenuForTextDelete(textForButton: List<String>, callBackData: String): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()

        for (i in textForButton.indices) {
            val number = i + 1
            val buttonText = "$number. " + textForButton[i].replace("*", " ◂▸ ")
            val button = InlineKeyboardButton()
            button.putData(buttonText, "$i" + callData_divF + callBackData)

            val rowInlineButton = ArrayList<InlineKeyboardButton>()
            rowInlineButton.add(button)
            rowsInline.add(rowInlineButton)
        }

        val delAllButton = InlineKeyboardButton()
        delAllButton.text = "⭕  Удалить все тексты"
        delAllButton.callbackData = "-1$callData_divF$callBackData"
        val firstRowInlineButton = ArrayList<InlineKeyboardButton>()
        firstRowInlineButton.add(delAllButton)
        rowsInline.add(firstRowInlineButton)

        val backButton = InlineKeyboardButton()
        backButton.putData("\uD83D\uDD19  Назад", callData_own)
        val secondRowInlineButton = ArrayList<InlineKeyboardButton>()
        secondRowInlineButton.add(backButton)
        rowsInline.add(secondRowInlineButton)

        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }

    // Сообщение с изображением (шапка)
    fun receiveBillboard(stringChatId: String, url: String): SendPhoto {
        val sendPhoto = SendPhoto().putData(stringChatId, url)
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        replyKeyboardMarkup.resizeKeyboard = true
        val firstRow = KeyboardRow()
        firstRow.add("\uD83D\uDCDA Уроки")
        firstRow.add("Настройки ⚙")
        val keyboardRows: List<KeyboardRow> = listOf(firstRow)
        replyKeyboardMarkup.keyboard = keyboardRows
        sendPhoto.replyMarkup = replyKeyboardMarkup
        return sendPhoto
    }

    // Меню тренировки слов
    fun receiveLearnWordMenu(stringChatId: String, messageId: Int, messageText: String): EditMessageText {
        val editMessageText = EditMessageText()
        editMessageText.putData(stringChatId, messageId, messageText)

        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()
        val firstRowInlineButton = ArrayList<InlineKeyboardButton>()
        val secondRowInlineButton = ArrayList<InlineKeyboardButton>()

        val trainingButton = InlineKeyboardButton()
        trainingButton.putData("\uD83C\uDD8E  Тренировка", callData_Training)
        firstRowInlineButton.add(trainingButton)

        val myWordsButton = InlineKeyboardButton()
        myWordsButton.putData("Добавить/удалить слова для изучения", callData_own + Lessons.LEARN_WORDS.title)
        secondRowInlineButton.add(myWordsButton)

        rowsInline.add(firstRowInlineButton)
        rowsInline.add(secondRowInlineButton)

        inlineKeyboardMarkup.keyboard = rowsInline
        editMessageText.replyMarkup = inlineKeyboardMarkup
        return editMessageText
    }

    // Меню добавления текстов
    fun receiveChoseMenu(stringChatId: String, messageId: Int, callBackText: String, messageText: String): EditMessageText{
        val editMessageText = EditMessageText()
        editMessageText.putData(stringChatId, messageId, messageText)

        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()
        val firstRowInlineButton = ArrayList<InlineKeyboardButton>()
        val secondRowInlineButton = ArrayList<InlineKeyboardButton>()

        val showButton = InlineKeyboardButton()
        showButton.putData("Посмотреть список моих текстов", callData_show + callBackText)
        firstRowInlineButton.add(showButton)

        val addButton = InlineKeyboardButton()
        addButton.putData("Добавить текст", callData_addTxt + callBackText)
        secondRowInlineButton.add(addButton)

        val delButton = InlineKeyboardButton()
        delButton.putData("Удалить текст", callData_delTxt + callBackText)
        secondRowInlineButton.add(delButton)

        rowsInline.add(firstRowInlineButton)
        rowsInline.add(secondRowInlineButton)
        inlineKeyboardMarkup.keyboard = rowsInline
        editMessageText.replyMarkup = inlineKeyboardMarkup

        return editMessageText
    }

    // Клавиатура с текстом урока
    private fun createLessonKeyBoard(textForButton: List<String>, calBackData: String): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()

        val firstRowInlineButton = ArrayList<InlineKeyboardButton>()
        val secondRowInlineButton = ArrayList<InlineKeyboardButton>()
        val thirdRowInlineButton = ArrayList<InlineKeyboardButton>()
        val fourthRowInlineButton = ArrayList<InlineKeyboardButton>()
        val fifthRowInlineButton = ArrayList<InlineKeyboardButton>()

        val firstButton = InlineKeyboardButton()
        firstButton.putData(textForButton[0], calBackData + " " + textForButton[0])
        firstRowInlineButton.add(firstButton)

        val secondButton = InlineKeyboardButton()
        secondButton.putData(textForButton[1], calBackData + " " + textForButton[1])
        firstRowInlineButton.add(secondButton)

        val thirdButton = InlineKeyboardButton()
        thirdButton.putData(textForButton[2], calBackData + " " + textForButton[2])
        firstRowInlineButton.add(thirdButton)

        val fourthButton = InlineKeyboardButton()
        fourthButton.putData(textForButton[3], calBackData + " " + textForButton[3])
        secondRowInlineButton.add(fourthButton)

        val fifthButton = InlineKeyboardButton()
        fifthButton.putData(textForButton[4], calBackData + " " + textForButton[4])
        secondRowInlineButton.add(fifthButton)

        val sixthButton = InlineKeyboardButton()
        sixthButton.putData(textForButton[5], calBackData + " " + textForButton[5])
        secondRowInlineButton.add(sixthButton)

        val seventhButton = InlineKeyboardButton()
        seventhButton.putData(textForButton[6], calBackData + " " + textForButton[6])
        thirdRowInlineButton.add(seventhButton)

        val eighthButton = InlineKeyboardButton()
        eighthButton.putData(textForButton[7], calBackData + " " + textForButton[7])
        thirdRowInlineButton.add(eighthButton)

        val ninthButton = InlineKeyboardButton()
        ninthButton.putData(textForButton[8], calBackData + " " + textForButton[8])
        fourthRowInlineButton.add(ninthButton)

        val tenthButton = InlineKeyboardButton()
        tenthButton.putData(textForButton[9], calBackData + " " + textForButton[9])
        fourthRowInlineButton.add(tenthButton)

        val deleteButton = InlineKeyboardButton()
        deleteButton.putData("ㅤㅤ\uD83D\uDD19️ Убрать словоㅤㅤ", callData_delWord)
        fifthRowInlineButton.add(deleteButton)

        rowsInline.add(firstRowInlineButton)
        rowsInline.add(secondRowInlineButton)
        rowsInline.add(thirdRowInlineButton)
        rowsInline.add(fourthRowInlineButton)
        rowsInline.add(fifthRowInlineButton)

        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }

    // Меню настроек
    private fun receiveButtonsForSettingMenu(isOnlyUsersText: Boolean, isShowHint: Boolean, isSendTrainingMessage: Boolean,
                                             sinceTime: Int, untilTime: Int): InlineKeyboardMarkup {
        val showHint: String = if(isShowHint) textForOn else textForOff
        val usersText: String = if(isOnlyUsersText) textForOn else textForOff
        val trainingMessage: String = if(isSendTrainingMessage) textForOn else textForOff

        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()
        val firstRowInlineButton = ArrayList<InlineKeyboardButton>()
        val secondRowInlineButton = ArrayList<InlineKeyboardButton>()
        val thirdRowInlineButton = ArrayList<InlineKeyboardButton>()
        val fourthRowInlineButton = ArrayList<InlineKeyboardButton>()

        val showHintButton = InlineKeyboardButton()
        showHintButton.putData("Показывать таблицы и подсказки:  $showHint", callData_showHint)
        firstRowInlineButton.add(showHintButton)

        val userTextButton = InlineKeyboardButton()
        userTextButton.putData("Только свои тексты уроков:  $usersText", callData_userTxt)
            secondRowInlineButton.add(userTextButton)

        val trainingButton = InlineKeyboardButton()
        trainingButton.putData("Сообщения с тренировками:  $trainingMessage", callData_trainMessage)
        thirdRowInlineButton.add(trainingButton)

        rowsInline.add(firstRowInlineButton)
        rowsInline.add(secondRowInlineButton)
        rowsInline.add(thirdRowInlineButton)

        if(isSendTrainingMessage) {
            val sinceTimeDownButton = InlineKeyboardButton()
            sinceTimeDownButton.putData("⏪ с $sinceTime", callData_setTime + "SinceDown")
            fourthRowInlineButton.add(sinceTimeDownButton)

            val sinceTimeUpButton = InlineKeyboardButton()
            sinceTimeUpButton.putData("⏩", callData_setTime + "SinceUp")
            fourthRowInlineButton.add(sinceTimeUpButton)

            val untilTimeDownButton = InlineKeyboardButton()
            untilTimeDownButton.putData("⏪", callData_setTime + "UntilDown")
            fourthRowInlineButton.add(untilTimeDownButton)

            val untilTimeUpButton = InlineKeyboardButton()
            untilTimeUpButton.putData("до $untilTime ⏩", callData_setTime + "UntilUp")
            fourthRowInlineButton.add(untilTimeUpButton)
            rowsInline.add(fourthRowInlineButton)
        }

        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }

    // Меню с одной кнопкой
    fun receiveOneButtonMenu(buttonText: String, buttonData: String): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()

        val rowInlineButton = ArrayList<InlineKeyboardButton>()
        val button = InlineKeyboardButton()
        button.putData(buttonText, buttonData)
        rowInlineButton.add(button)
        rowsInline.add(rowInlineButton)
        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }

    // Меню с двумя кнопками
    fun receiveTwoButtonsMenu(firstButtonText: String, firstData: String,
                              secondButtonText: String, secondData: String): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()
        val firstRowInlineButton = ArrayList<InlineKeyboardButton>()

        val firstButton = InlineKeyboardButton()
        firstButton.putData(firstButtonText, firstData)
        firstRowInlineButton.add(firstButton)

        val secondButton = InlineKeyboardButton()
        secondButton.putData(secondButtonText, secondData)
        firstRowInlineButton.add(secondButton)

        rowsInline.add(firstRowInlineButton)
        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }

    // Удалить текст урока из бд
    fun removeLessonTextFromDb(lessonCategory: String, userData: UserData, lessonText: String) {
        when(lessonCategory){
            Lessons.PRESENT_CONTINUOUS.title -> userData.presentContinuous = lessonText
            Lessons.PERFECT_TENSE.title -> userData.perfectSentence = lessonText
            Lessons.PRESENT_SIMPLE.title -> userData.presentSimple = lessonText
            Lessons.LEARN_WORDS.title -> userData.wordsForLearning = lessonText
            Lessons.COMPARE_WORDS.title -> userData.compareWords = lessonText
            Lessons.VARIOUS_WORDS.title -> userData.variousWords = lessonText
            Lessons.PASSIVE_VOICE.title -> userData.passiveVoice = lessonText
            Lessons.MUCH_MANY_LOT.title -> userData.muchManyLot = lessonText
            Lessons.DATE_AND_TIME.title -> userData.dateAndTime = lessonText
        }
    }

    // Получить текст урока из бд
    fun receiveLessonTextFromDb(lessonCategory: String, userData: UserData): String{
        var userLessonText = ""
        when(lessonCategory){
            Lessons.PRESENT_CONTINUOUS.title -> userLessonText = userData.presentContinuous
            Lessons.PERFECT_TENSE.title -> userLessonText = userData.perfectSentence
            Lessons.PRESENT_SIMPLE.title -> userLessonText = userData.presentSimple
            Lessons.LEARN_WORDS.title -> userLessonText = userData.wordsForLearning
            Lessons.COMPARE_WORDS.title -> userLessonText = userData.compareWords
            Lessons.VARIOUS_WORDS.title -> userLessonText = userData.variousWords
            Lessons.PASSIVE_VOICE.title -> userLessonText = userData.passiveVoice
            Lessons.MUCH_MANY_LOT.title -> userLessonText = userData.muchManyLot
            Lessons.DATE_AND_TIME.title -> userLessonText = userData.dateAndTime
        }
        return userLessonText
    }

    // Добавить текст урока в бд
    fun updateLessonTextInDb(lessonText: String, lessonCategory: String, userData: UserData): UserData{
        when(lessonCategory){
            Lessons.PRESENT_CONTINUOUS.title -> userData.presentContinuous += lessonText
            Lessons.PERFECT_TENSE.title -> userData.perfectSentence += lessonText
            Lessons.LEARN_WORDS.title -> userData.wordsForLearning += lessonText
            Lessons.PRESENT_SIMPLE.title -> userData.presentSimple += lessonText
            Lessons.PASSIVE_VOICE.title -> userData.passiveVoice += lessonText
            Lessons.COMPARE_WORDS.title -> userData.compareWords += lessonText
            Lessons.VARIOUS_WORDS.title -> userData.variousWords += lessonText
            Lessons.MUCH_MANY_LOT.title -> userData.muchManyLot += lessonText
            Lessons.DATE_AND_TIME.title -> userData.dateAndTime += lessonText
        }
        return userData
    }

}
