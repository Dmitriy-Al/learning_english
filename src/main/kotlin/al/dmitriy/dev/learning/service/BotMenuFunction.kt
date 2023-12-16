package al.dmitriy.dev.learning.service

import al.dmitriy.dev.learning.extendfunctions.putData
import al.dmitriy.dev.learning.dataunit.LessonUnit
import al.dmitriy.dev.learning.lesson.Lessons
import al.dmitriy.dev.learning.model.UserData
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class BotMenuFunction : BotMenuInterface {


    fun setKeyBoard(sendMessage: SendMessage) {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        replyKeyboardMarkup.resizeKeyboard = true
        val firstRow = KeyboardRow()
        firstRow.add("\uD83D\uDCDA Уроки")
        firstRow.add("Настройки ⚙")
        val keyboardRows: List<KeyboardRow> = listOf(firstRow)
        replyKeyboardMarkup.keyboard = keyboardRows
        sendMessage.replyMarkup = replyKeyboardMarkup
    }


    fun categoryMenu(chatId: Long, messageText: String, categories: List<String>): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.setChatId(chatId)
        sendMessage.text = messageText
        sendMessage.replyMarkup = createButtonMenu(categories)
        return sendMessage
    }


    fun receiveButtonEditMessage(chatId: String, messageId: Int, messageText: String, callBackData: String, buttonTexts: List<String>): EditMessageText {
        val editMessageText = EditMessageText()
        editMessageText.chatId = chatId
        editMessageText.messageId = messageId
        editMessageText.text = messageText
        editMessageText.replyMarkup = createDataButtonMenu(buttonTexts, callBackData)
        return editMessageText
    }


    override fun createButtonMenu(textForButton: List<String>): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()

        for (element in textForButton) {
            val rowInlineButton = ArrayList<InlineKeyboardButton>()
            val button = InlineKeyboardButton()
            button.text = element
            button.callbackData = element
            rowInlineButton.add(button)
            rowsInline.add(rowInlineButton)
        }
        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }


     override fun createDataButtonMenu(textForButton: List<String>, callBackData: String): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()

        for (element in textForButton) {
            val rowInlineButton = ArrayList<InlineKeyboardButton>()
            val button = InlineKeyboardButton()
            button.text = element
            button.callbackData = callBackData + element
            rowInlineButton.add(button)
            rowsInline.add(rowInlineButton)
        }
        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }


    fun receiveChoseMenu(chatId: String, messageId: Int, callBackText: String, messageText: String): EditMessageText{
        val editMessageText = EditMessageText()
        editMessageText.chatId = chatId
        editMessageText.messageId = messageId
        editMessageText.text = messageText

        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()
        val firstRowInlineButton = ArrayList<InlineKeyboardButton>()
        val secondRowInlineButton = ArrayList<InlineKeyboardButton>()

        val showButton = InlineKeyboardButton()

        showButton.text = "Посмотреть список моих текстов"
        showButton.callbackData = "#show$callBackText"
        firstRowInlineButton.add(showButton)

        val addButton = InlineKeyboardButton()
        val delButton = InlineKeyboardButton()

        addButton.text = "Добавить текст"
        addButton.callbackData = "#add$callBackText"
        secondRowInlineButton.add(addButton)

        delButton.text = "Удалить текст"
        delButton.callbackData = "#todel$callBackText"
        secondRowInlineButton.add(delButton)

        rowsInline.add(firstRowInlineButton)
        rowsInline.add(secondRowInlineButton)
        inlineKeyboardMarkup.keyboard = rowsInline
        editMessageText.replyMarkup = inlineKeyboardMarkup
        return editMessageText
    }



    fun createLessonButtonMenu(stringChatId: String, messageId: Int, properlyLessonAnswer: Int, lessonUnit: LessonUnit, dataText: String): EditMessageText {
        var space = ""
        val editMessageText = EditMessageText()
        editMessageText.chatId = stringChatId
        editMessageText.messageId = messageId
        editMessageText.text = "Правильных ответов: $properlyLessonAnswer ㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤ\n\uD83D\uDD39 " + lessonUnit.russianText + "\n\n✏ " + lessonUnit.inputText

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
        val dataString = if (lessonUnit.inputText.split(" ").size == lessonUnit.englishText.split(" ").size) "finish" else dataText  //

        val inlineKeyboardMarkup: InlineKeyboardMarkup = createLessonKeyBoard(buttonText, dataString)
        editMessageText.replyMarkup = inlineKeyboardMarkup
        return editMessageText
    }


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
        deleteButton.putData("ㅤㅤ\uD83D\uDD19️ Убрать словоㅤㅤ", "delword")
        fifthRowInlineButton.add(deleteButton)

        rowsInline.add(firstRowInlineButton)
        rowsInline.add(secondRowInlineButton)
        rowsInline.add(thirdRowInlineButton)
        rowsInline.add(fourthRowInlineButton)
        rowsInline.add(fifthRowInlineButton)

        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }



    fun createButtonMenuForDelete(textForButton: List<String>, callBackData: String): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()

        for (i in textForButton.indices) {
            val number = i + 1
            val buttonText = "$number. " + textForButton[i].replace("*", " ◂▸ ")
            val button = InlineKeyboardButton()
            button.text = buttonText
            button.callbackData = "$i@$callBackData"

            val rowInlineButton = ArrayList<InlineKeyboardButton>()
            rowInlineButton.add(button)
            rowsInline.add(rowInlineButton)
        }

        val delAllButton = InlineKeyboardButton()
        delAllButton.text = "⭕  Удалить все тексты"
        delAllButton.callbackData = "-1@$callBackData"
        val firstRowInlineButton = ArrayList<InlineKeyboardButton>()
        firstRowInlineButton.add(delAllButton)
        rowsInline.add(firstRowInlineButton)

        val backButton = InlineKeyboardButton()
        backButton.text = "\uD83D\uDD19  Назад"
        backButton.callbackData = "#own"
        val secondRowInlineButton = ArrayList<InlineKeyboardButton>()
        secondRowInlineButton.add(backButton)
        rowsInline.add(secondRowInlineButton)

        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }


    fun receiveCategoryMenu(chatId: String, messageId: Int, callbackData: String, messageText: String, categories: List<String>): EditMessageText {
        val editMessageText = EditMessageText()
        editMessageText.chatId = chatId
        editMessageText.messageId = messageId
        editMessageText.text = messageText
        editMessageText.replyMarkup = createDataButtonMenu(categories, callbackData)
        return editMessageText
    }


    fun receiveLearnWordMenu(chatId: String, messageId: Int, messageText: String): EditMessageText {
        val editMessageText = EditMessageText()
        editMessageText.chatId = chatId
        editMessageText.messageId = messageId
        editMessageText.text = messageText

        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()
        val firstRowInlineButton = ArrayList<InlineKeyboardButton>()
        val secondRowInlineButton = ArrayList<InlineKeyboardButton>()


        val trainingButton = InlineKeyboardButton()
        trainingButton.putData("\uD83C\uDD8E  Тренировка", "#tran")
        firstRowInlineButton.add(trainingButton)

        val myWordsButton = InlineKeyboardButton()
        myWordsButton.putData("Добавить/удалить слова для изучения", "#ownУчить новые слова")
        secondRowInlineButton.add(myWordsButton)

        rowsInline.add(firstRowInlineButton)
        rowsInline.add(secondRowInlineButton)

        inlineKeyboardMarkup.keyboard = rowsInline
        editMessageText.replyMarkup = inlineKeyboardMarkup
        return editMessageText
    }


    fun receiveBillboard(stringChatId: String, url: String): SendPhoto {
        val sendPhoto = SendPhoto()
        sendPhoto.chatId = stringChatId
        sendPhoto.photo = InputFile(url)

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


    fun receiveSettingMenu(chatId: String, messageText: String, isSimpleText: Boolean, isOnlyUsersText: Boolean, isShowHint: Boolean, isSendTrainingMessage: Boolean, sinceTime: Int, untilTime: Int): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.chatId = chatId
        sendMessage.text = messageText
        sendMessage.replyMarkup = receiveButtonsForSettingMenu(isSimpleText, isOnlyUsersText, isShowHint, isSendTrainingMessage, sinceTime, untilTime)
        return sendMessage
    }


    fun receiveSettingMenu(chatId: String, messageId: Int, messageText: String, isSimpleText: Boolean, isOnlyUsersText: Boolean, isShowHint: Boolean, isSendTrainingMessage: Boolean, sinceTime: Int, untilTime: Int): EditMessageText {
        val editMessageText = EditMessageText()
        editMessageText.chatId = chatId
        editMessageText.text = messageText
        editMessageText.messageId = messageId
        editMessageText.replyMarkup = receiveButtonsForSettingMenu(isSimpleText, isOnlyUsersText, isShowHint, isSendTrainingMessage, sinceTime, untilTime)
        return editMessageText
    }


    private fun receiveButtonsForSettingMenu(isViewAsChat: Boolean, isOnlyUsersText: Boolean, isShowHint: Boolean, isSendTrainingMessage: Boolean, sinceTime: Int, untilTime: Int): InlineKeyboardMarkup {
        val showHint: String = if(isShowHint) "\uD835\uDC0E\uD835\uDC0D" else "\uD835\uDC0E\uD835\uDC05\uD835\uDC05"
        val viewAsChat: String = if(isViewAsChat) "\uD835\uDC0E\uD835\uDC0D" else "\uD835\uDC0E\uD835\uDC05\uD835\uDC05"
        val usersText: String = if(isOnlyUsersText) "\uD835\uDC0E\uD835\uDC0D" else "\uD835\uDC0E\uD835\uDC05\uD835\uDC05"
        val trainingMessage: String = if(isSendTrainingMessage) "\uD835\uDC0E\uD835\uDC0D" else "\uD835\uDC0E\uD835\uDC05\uD835\uDC05"

        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()
        val firstRowInlineButton = ArrayList<InlineKeyboardButton>()
        val secondRowInlineButton = ArrayList<InlineKeyboardButton>()
        val thirdRowInlineButton = ArrayList<InlineKeyboardButton>()
        val fourthRowInlineButton = ArrayList<InlineKeyboardButton>()
        val fifthRowInlineButton = ArrayList<InlineKeyboardButton>()

        val showHintButton = InlineKeyboardButton()
        showHintButton.text = "Показывать таблицы и подсказки:  $showHint"
        showHintButton.callbackData = "#hint"
        firstRowInlineButton.add(showHintButton)

        val simpleTextButton = InlineKeyboardButton()
        simpleTextButton.text = "Отображение в формате чата:  $viewAsChat"
        simpleTextButton.callbackData = "#aschat"
        secondRowInlineButton.add(simpleTextButton)

        val userTextButton = InlineKeyboardButton()
        userTextButton.text = "Только свои тексты уроков:  $usersText"
        userTextButton.callbackData = "#usrtxt"
        thirdRowInlineButton.add(userTextButton)

        val trainingButton = InlineKeyboardButton()
        trainingButton.text = "Сообщения с тренировками:  $trainingMessage"
        trainingButton.callbackData = "#trmes"
        fourthRowInlineButton.add(trainingButton)

        rowsInline.add(firstRowInlineButton)
        rowsInline.add(secondRowInlineButton)
        rowsInline.add(thirdRowInlineButton)
        rowsInline.add(fourthRowInlineButton)

        if(isSendTrainingMessage) {
            val sinceTimeDownButton = InlineKeyboardButton()
            sinceTimeDownButton.text = "⏪ с $sinceTime"
            sinceTimeDownButton.callbackData = "#timeSinceDown"
            fifthRowInlineButton.add(sinceTimeDownButton)

            val sinceTimeUpButton = InlineKeyboardButton()
            sinceTimeUpButton.text = "⏩"
            sinceTimeUpButton.callbackData = "#timeSinceUp"
            fifthRowInlineButton.add(sinceTimeUpButton)


            val untilTimeDownButton = InlineKeyboardButton()
            untilTimeDownButton.text = "⏪"
            untilTimeDownButton.callbackData = "#timeUntilDown"
            fifthRowInlineButton.add(untilTimeDownButton)

            val untilTimeUpButton = InlineKeyboardButton()
            untilTimeUpButton.text = "до $untilTime ⏩"
            untilTimeUpButton.callbackData = "#timeUntilUp"
            fifthRowInlineButton.add(untilTimeUpButton)

            rowsInline.add(fifthRowInlineButton)
        }

        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }


    fun receiveGoBackMenu(stringChatId: String, messageText: String): SendMessage{
        val sendMessage = SendMessage(stringChatId, messageText)
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()

        val rowInlineButton = ArrayList<InlineKeyboardButton>()
        val backButton = InlineKeyboardButton()
        backButton.text = "\uD83D\uDD19  Назад"
        backButton.callbackData = "#own"
        rowInlineButton.add(backButton)
        rowsInline.add(rowInlineButton)
        inlineKeyboardMarkup.keyboard = rowsInline
        sendMessage.replyMarkup = inlineKeyboardMarkup
        return sendMessage
    }


    fun removeLessonTextFromDb(lessonCategory: String, userData: UserData, lessonText: String) {
        when(lessonCategory){
            Lessons.PRONOUN_PREPOSITION.title -> userData.pronounAndPreposition = lessonText
            Lessons.PRESENT_CONTINUOUS.title -> userData.presentContinuous = lessonText
            Lessons.PRESENT_SENTENCE.title -> userData.perfectSentence = lessonText
            Lessons.PRESENT_SIMPLE.title -> userData.presentSimple = lessonText
            Lessons.LEARN_WORDS.title -> userData.wordsForLearning = lessonText
            Lessons.COMPARE_WORDS.title -> userData.compareWords = lessonText
            Lessons.VARIOUS_WORDS.title -> userData.variousWords = lessonText
            Lessons.PASSIVE_VOICE.title -> userData.passiveVoice = lessonText
            Lessons.MUCH_MANY_LOT.title -> userData.muchManyLot = lessonText
            Lessons.DATE_AND_TIME.title -> userData.dateAndTime = lessonText
        }
    }


    fun receiveLessonTextFromDb(lessonCategory: String, userData: UserData): String{
        var userLessonText = ""
        when(lessonCategory){
            Lessons.PRONOUN_PREPOSITION.title -> userLessonText = userData.pronounAndPreposition
            Lessons.PRESENT_CONTINUOUS.title -> userLessonText = userData.presentContinuous
            Lessons.PRESENT_SENTENCE.title -> userLessonText = userData.perfectSentence
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


    fun updateLessonTextInDb(lessonText: String, lessonCategory: String, userData: UserData): UserData{
        when(lessonCategory){
            Lessons.PRONOUN_PREPOSITION.title ->  userData.pronounAndPreposition += lessonText
            Lessons.PRESENT_CONTINUOUS.title -> userData.presentContinuous += lessonText
            Lessons.PRESENT_SENTENCE.title -> userData.perfectSentence += lessonText
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


    fun isTextIncorrect(messageText: String, lessonCategory: String): Boolean{
        return messageText.contains("⚙") || messageText.contains("\uD83D\uDCDA") || messageText.contains("/") || // TODO не более 10 слов
                messageText.contains("#") || messageText.contains("*") || (lessonCategory == Lessons.LEARN_WORDS.title &&
                messageText.length > 15 ) || (messageText.split(" ").size > 10) || messageText.length > 100 || messageText.contains("  ")
    }



    // TODO >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>



    fun receiveMainLessonsMenu(stringChatId: String, messageText: String, url: String){
        val hintMessage = SendPhoto()
        hintMessage.putData(stringChatId, "https://mulino58.ru/wp-content/uploads/f/f/1/ff19b56689e24788a517aebe4ae9ede4.jpg")
    }

    fun receiveHintPicture(stringChatId: String, url: String): SendPhoto{
        val sendPhoto = SendPhoto()
        sendPhoto.chatId = stringChatId
        sendPhoto.photo = InputFile(url)
        return sendPhoto
    }

    fun receiveStatisticForAdmin(chatId: String, messageId: Int, usersData: Iterable<UserData>): EditMessageText {
        val editMessageText = EditMessageText()
        editMessageText.chatId = chatId
        editMessageText.messageId = messageId
        editMessageText.text = "messageText"

        return editMessageText
    }


    fun receiveInfoMenuForAdmin(chatId: String, messageId: Int, messageText: String, categories: List<String>): EditMessageText {
        val editMessageText = EditMessageText()
        editMessageText.chatId = chatId
        editMessageText.messageId = messageId
        editMessageText.text = messageText
        editMessageText.replyMarkup = createButtonMenu(categories)
        return editMessageText
    }






    /*

        override fun createButtonMenu(textForButton: List<String>): InlineKeyboardMarkup {
            val inlineKeyboardMarkup = InlineKeyboardMarkup()
            val rowsInline = ArrayList<List<InlineKeyboardButton>>()

            for (element in textForButton) {
                val rowInlineButton = ArrayList<InlineKeyboardButton>()
                val button = InlineKeyboardButton()
                button.text = element
                button.callbackData = element
                rowInlineButton.add(button)
                rowsInline.add(rowInlineButton)
            }
            inlineKeyboardMarkup.keyboard = rowsInline
            return inlineKeyboardMarkup
        }
     */






}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


/*
        /*
        val firstRowInlineButton = ArrayList<InlineKeyboardButton>()
        val secondRowInlineButton = ArrayList<InlineKeyboardButton>()
        val thirdRowInlineButton = ArrayList<InlineKeyboardButton>()
        val fourthRowInlineButton = ArrayList<InlineKeyboardButton>()
        val fifthRowInlineButton = ArrayList<InlineKeyboardButton>()
         */

        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val rowsInline = ArrayList<List<InlineKeyboardButton>>()
        val rowInlineButton = ArrayList<InlineKeyboardButton>()
        val button = InlineKeyboardButton()
        button.text = splitLessonText[0]
        button.callbackData = Lessons.PRESENT_SIMPLE.title
        rowInlineButton.add(button)
        rowsInline.add(rowInlineButton)
        inlineKeyboardMarkup.keyboard = rowsInline
        */



