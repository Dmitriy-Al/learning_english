package al.dmitriy.dev.learning.service

import al.dmitriy.dev.learning.extendfunctions.putData
import al.dmitriy.dev.learning.dataunit.LessonUnit
import al.dmitriy.dev.learning.lesson.Lessons
import al.dmitriy.dev.learning.model.UserData
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
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
        sendMessage.replyMarkup = createButtonMenu(categories)// createDataButtonMenu(categories, "$categories#")
        return sendMessage
    }


    fun categoryMenu(chatId: String, messageId: Int, messageText: String, categories: List<String>): EditMessageText {
        val editMessageText = EditMessageText()
        editMessageText.chatId = chatId
        editMessageText.messageId = messageId
        editMessageText.text = messageText
        editMessageText.replyMarkup = createButtonMenu(categories)
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


    fun updateLessonTextInDb(lessonText: String, lessonCategory: String, userData: UserData): UserData{
        when(lessonCategory){
            Lessons.PRONOUN_PREPOSITION.title ->  userData.pronounAndPreposition += lessonText
            Lessons.PRESENT_CONTINUOUS.title -> userData.presentContinuous += lessonText
            Lessons.PRESENT_SENTENCE.title -> userData.perfectSentence += lessonText
            Lessons.PRESENT_SIMPLE.title -> userData.presentSimple += lessonText
            Lessons.PASSIVE_VOICE.title -> userData.passiveVoice += lessonText
            Lessons.COMPARE_WORDS.title -> userData.compareWords += lessonText
            Lessons.VARIOUS_WORDS.title -> userData.variousWords += lessonText
            Lessons.MUCH_MANY_LOT.title -> userData.muchManyLot += lessonText
            Lessons.DATE_AND_TIME.title -> userData.dateAndTime += lessonText
            "WFL" -> userData.wordsForLearning += lessonText
        }
        return userData
    }


    fun createLessonButtonMenu(chatId: String, messageId: Int, lessonUnit: LessonUnit, dataText: String): EditMessageText {
        var space = ""
        val editMessageText = EditMessageText()
        editMessageText.chatId = chatId
        editMessageText.messageId = messageId
        editMessageText.text = "\uD83D\uDD39 " + lessonUnit.russianText + "\nㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤ\n✏ " + lessonUnit.inputText

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
        editMessageText.replyMarkup = inlineKeyboardMarkup  // TODO
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


    fun receiveLessonTextFromDb(lessonCategory: String, userData: UserData): String{
        var userLessonText = ""

        when(lessonCategory){
            Lessons.PRONOUN_PREPOSITION.title -> userLessonText = userData.pronounAndPreposition
            Lessons.PRESENT_CONTINUOUS.title -> userLessonText = userData.presentContinuous
            Lessons.PRESENT_SENTENCE.title -> userLessonText = userData.perfectSentence
            Lessons.PRESENT_SIMPLE.title -> userLessonText = userData.presentSimple
            Lessons.COMPARE_WORDS.title -> userLessonText = userData.compareWords
            Lessons.VARIOUS_WORDS.title -> userLessonText = userData.variousWords
            Lessons.PASSIVE_VOICE.title -> userLessonText = userData.passiveVoice
            Lessons.MUCH_MANY_LOT.title -> userLessonText = userData.muchManyLot
            Lessons.DATE_AND_TIME.title -> userLessonText = userData.dateAndTime
            "WFL" -> userLessonText = userData.wordsForLearning
        }
        return userLessonText
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

        val rowInlineButton = ArrayList<InlineKeyboardButton>()
        rowInlineButton.add(delAllButton)
        rowsInline.add(rowInlineButton)

        inlineKeyboardMarkup.keyboard = rowsInline
        return inlineKeyboardMarkup
    }

    fun removeLessonTextFromDb(lessonCategory: String, userData: UserData, lessonText: String) {
        when(lessonCategory){
            Lessons.PRONOUN_PREPOSITION.title -> userData.pronounAndPreposition = lessonText
            Lessons.PRESENT_CONTINUOUS.title -> userData.presentContinuous = lessonText
            Lessons.PRESENT_SENTENCE.title -> userData.perfectSentence = lessonText
            Lessons.PRESENT_SIMPLE.title -> userData.presentSimple = lessonText
            Lessons.COMPARE_WORDS.title -> userData.compareWords = lessonText
            Lessons.VARIOUS_WORDS.title -> userData.variousWords = lessonText
            Lessons.PASSIVE_VOICE.title -> userData.passiveVoice = lessonText
            Lessons.MUCH_MANY_LOT.title -> userData.muchManyLot = lessonText
            Lessons.DATE_AND_TIME.title -> userData.dateAndTime = lessonText
            "WFL" -> userData.wordsForLearning = lessonText // TODO
        }
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
        val thirdRowInlineButton = ArrayList<InlineKeyboardButton>()
        val fourthRowInlineButton = ArrayList<InlineKeyboardButton>()

        val trainingButton = InlineKeyboardButton()
        trainingButton.putData("\uD83C\uDD8E  Тренировка", "#tran")  // 🔤
        firstRowInlineButton.add(trainingButton)

        val myWordsButton = InlineKeyboardButton()
        myWordsButton.putData("\uD83D\uDEAE  Убрать слово из изучаемых", "#todelWFL")
        secondRowInlineButton.add(myWordsButton)

        val learnWordButton = InlineKeyboardButton()
        learnWordButton.putData("\uD83D\uDD24  Добавить слово на изучение", "#addWFL")
        thirdRowInlineButton.add(learnWordButton)

        val fourthButton = InlineKeyboardButton()
        fourthButton.putData("\uD83D\uDCDC  Список изучаемых слов", "#showWFL")
        fourthRowInlineButton.add(fourthButton)

        rowsInline.add(firstRowInlineButton)
        rowsInline.add(secondRowInlineButton)
        rowsInline.add(thirdRowInlineButton)
        rowsInline.add(fourthRowInlineButton)

        inlineKeyboardMarkup.keyboard = rowsInline
        editMessageText.replyMarkup = inlineKeyboardMarkup
        return editMessageText
    }



// TODO >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>










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



