package al.dmitriy.dev.learning.service

import al.dmitriy.dev.learning.lesson.LessonUnit
import al.dmitriy.dev.learning.lesson.Lessons
import al.dmitriy.dev.learning.model.UserData
import al.dmitriy.dev.learning.model.UserDataDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
class InputOutputCommand(@Autowired val userDataRepository: UserDataDao) :
    TelegramLongPollingBot("6586869115:AAFJKKL9u6zJwNQ9dHd8vPVlia_I6v6Jdz0") {

    init {
        val botCommandList: List<BotCommand> = listOf(BotCommand("/start", "Запуск бота"),
            BotCommand("/mydata", "Данные пользователя"),
            BotCommand("/deletedata", "Удалить данные пользователя"),
            BotCommand("/help", "Полезная информация"))
            try {
            this.execute(SetMyCommands(botCommandList, BotCommandScopeDefault(), null))
        } catch (e: TelegramApiException){
            println("Exc. ->" + e.message)
        }
    }

    //private val userData: UserData = UserData()
    private val messageIdBuffer = mutableListOf<Int>()
    private val botMenuFunction: BotMenuFunction = BotMenuFunction()
    private val lessonsMap = HashMap<String, MutableList<String>>()
    private val deleteFromDbText = HashMap<String, List<String>>()
    private val lessonUnitMap = HashMap<String, LessonUnit>()
    private val userLessonText = HashMap<String, String>()
    private val lessonCategoryText = HashMap<String, String>()
    private val tempData = HashMap<String, String>()
    private val messageIdMap = HashMap<String, Int>() // TODO
    private val startMessageMap = HashMap<String, Int>() // TODO

    private final val inputRuText = "INPUT_RU_TEXT"
    private final val inputEnText = "INPUT_EN_TEXT"

    private val space = "ㅤ"


    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val messageText: String = update.message.text
            val longChatId: Long = update.message.chatId
            val stringChatId: String = longChatId.toString()
            val intMessageId: Int = update.message.messageId
            messageIdBuffer.add(intMessageId)


            when (tempData[stringChatId]) {
                inputRuText -> {
                    if (messageIdMap[stringChatId] != null) { // TODO
                        val del = DeleteMessage()
                        del.chatId = stringChatId
                        val test = messageIdMap[stringChatId]!!
                        del.messageId = test
                        try {
                            execute(del)
                        } catch (e: TelegramApiException) {
                            println("EXC " + messageIdMap[stringChatId])
                        }
                    }

                    userLessonText[stringChatId] = update.message.text
                    val text = "\uD83D\uDCD1 Введите слово или текст на русском языке, затем отправьте сообщение"

                    execute(SendMessage(stringChatId, text))

                    messageIdMap[stringChatId] = intMessageId
                    tempData[stringChatId] = inputEnText
                }

                inputEnText -> {
                    if (messageIdMap[stringChatId] != null) { // TODO
                        val del = DeleteMessage()
                        del.chatId = stringChatId
                        val test = 1 + messageIdMap[stringChatId]!!
                        del.messageId = test
                        try {
                            execute(del)
                        } catch (e: TelegramApiException) {
                            println("EXC " + messageIdMap[stringChatId])
                        }
                    }
                    userLessonText[stringChatId]

                    val userData = userDataRepository.findById(longChatId).get()

                    val lessonText = if(botMenuFunction.receiveLessonTextFromDb(lessonCategoryText[stringChatId]!!, userData).isEmpty()){
                        update.message.text + "*" + userLessonText[stringChatId]!!
                    } else {
                        "#" + update.message.text + "*" + userLessonText[stringChatId]!!
                    }

                    val updateUserData: UserData = botMenuFunction.updateLessonTextInDb(lessonText, lessonCategoryText[stringChatId]!!, userData)
                    userDataRepository.save(updateUserData)

                    val sendMessage = SendMessage(stringChatId, "ㅤㅤㅤㅤ✅  Текст был добавленㅤㅤㅤㅤㅤㅤ")
                    execute(sendMessage)

                    messageIdMap[stringChatId] = intMessageId
                    tempData[stringChatId] = "" // TODO clear?
                    lessonCategoryText[stringChatId] = "" // TODO clear?
                }
            }


            when {
                messageText == "/start" -> {
                    if (messageIdMap[stringChatId] != null) { // TODO
                        val del = DeleteMessage()
                        del.chatId = stringChatId
                        val test = 1 + messageIdMap[stringChatId]!!
                        del.messageId = test
                        try {
                            execute(del)
                        } catch (e: TelegramApiException) {
                            println("EXC " + messageIdMap[stringChatId])
                        }
                    }

                    if (startMessageMap[stringChatId] != null) { // TODO
                        val del = DeleteMessage()
                        del.chatId = stringChatId
                        val test = 1 + startMessageMap[stringChatId]!!
                        del.messageId = test
                        try {
                            execute(del)
                        } catch (e: TelegramApiException) {
                            println("EXC " + messageIdMap[stringChatId])
                        }
                    }

                    val sendPhoto = SendPhoto()
                    sendPhoto.chatId = stringChatId
                    sendPhoto.caption = "Вас приветствует learning English!"; // прикреплённое сообщения
                    sendPhoto.hasSpoiler = false // заблюренное изображение, открывается по клику
                    sendPhoto.photo =
                    InputFile("https://sun9-19.userapi.com/impf/c850536/v850536720/189211/HVfRCUfowGM.jpg?size=1074x480&quality=96&sign=8308fb4a77a6578d8921fd13c5e2c1be&type=share") // установить изображение из файла на диске

                    val replyKeyboardMarkup = ReplyKeyboardMarkup()
                    replyKeyboardMarkup.resizeKeyboard = true
                    val firstRow = KeyboardRow()
                    firstRow.add("\uD83D\uDCDA Уроки")
                    firstRow.add("Настройки ⚙")
                    val keyboardRows: List<KeyboardRow> = listOf(firstRow)
                    replyKeyboardMarkup.keyboard = keyboardRows
                    sendPhoto.replyMarkup = replyKeyboardMarkup

                    if (userDataRepository.findById(stringChatId.toLong()).isEmpty) {
                        val userData = UserData() // TODO
                        userData.stringChatId = longChatId
                        userData.username = update.message.chat.userName
                        userDataRepository.save(userData)
                    }

                    /*val sendMessage = SendMessage(stringChatId, "Вас приветствует learning English!")
                    botMenuFunction.setKeyBoard(sendMessage)
                    execute(sendMessage) */

                    execute(sendPhoto)
                    startMessageMap[stringChatId] = intMessageId
                }

                messageText == "/mydata" -> {
                    val menu = listOf("1", "2")
                    val sendMessage: SendMessage =
                        botMenuFunction.categoryMenu(longChatId, "Сообщение с кнопками /mydata", menu)
                    botMenuFunction.setKeyBoard(sendMessage)
                    execute(sendMessage)
                }

                messageText == "/deletedata" -> {
                    val menu = listOf("1", "2")
                    val sendMessage: SendMessage =
                        botMenuFunction.categoryMenu(longChatId, "Сообщение с кнопками /deletedata", menu)
                    botMenuFunction.setKeyBoard(sendMessage)
                    execute(sendMessage)
                }

                messageText == "\uD83D\uDCDA Уроки" -> {
                    if (messageIdMap[stringChatId] != null) { // TODO
                        val del = DeleteMessage()
                        del.chatId = stringChatId
                        val test = 1 + messageIdMap[stringChatId]!!
                        del.messageId = test
                        try {
                            execute(del)
                        } catch (e: TelegramApiException) {
                            println("EXC " + messageIdMap[stringChatId])
                        }
                    }

                    if (lessonsMap[stringChatId] != null) lessonsMap[stringChatId]!!.clear()

                    val lessonsTitles = mutableListOf(
                        "\uD83D\uDCDA Учить новые слова",
                        "\uD83D\uDCD2 Уроки других пользователей", "\uD83D\uDCD6 Добавить/удалить свои тексты"
                    )
                    lessonsTitles.addAll(Lessons.COMPARE_WORDS.getLessonsTitles())

                    val sendMessage: SendMessage = botMenuFunction.categoryMenu(
                        longChatId,
                        "ㅤㅤㅤㅤㅤㅤㅤ\uD835\uDC0B\uD835\uDC04\uD835\uDC12\uD835\uDC12\uD835\uDC0E\uD835\uDC0D\uD835\uDC12ㅤㅤㅤㅤㅤㅤㅤㅤ",
                        lessonsTitles
                    )
                    execute(sendMessage)
                    messageIdMap[stringChatId] = intMessageId
                }

                messageText == "Настройки ⚙" -> {
                    if (messageIdMap[stringChatId] != null) { // TODO
                        val del = DeleteMessage()
                        del.chatId = stringChatId
                        val test = 1 + messageIdMap[stringChatId]!!
                        del.messageId = test
                        try {
                            execute(del)
                        } catch (e: TelegramApiException) {
                            println("EXC " + messageIdMap[stringChatId])
                        }
                    }

                    messageIdMap[stringChatId] = intMessageId
                    val menu = listOf("Настройкa 1", "Настройкa 2")
                    val sendMessage: SendMessage = botMenuFunction.categoryMenu(longChatId, "Настройки", menu)
                    execute(sendMessage)
                }

                else -> {
                    val del = DeleteMessage()
                    del.chatId = stringChatId
                    del.messageId = intMessageId
                    execute(del)
                }
            }

            if (messageIdMap[stringChatId] != null) {
                val del = DeleteMessage()
                del.chatId = stringChatId
                del.messageId = messageIdMap[stringChatId]!!
                try {
                    execute(del)
                } catch (e: TelegramApiException) {
                    println("EXC " + messageIdMap[stringChatId])
                }
            }

            if (startMessageMap[stringChatId] != null) {
                val del = DeleteMessage()
                del.chatId = stringChatId
                del.messageId = startMessageMap[stringChatId]!!
                try {
                    execute(del)
                } catch (e: TelegramApiException) {
                    println("EXC " + messageIdMap[stringChatId])
                }
            }


        } else if (update.hasCallbackQuery()) {
            val intMessageId: Int = update.callbackQuery.message.messageId
            val stringChatId: String = update.callbackQuery.message.chatId.toString()
            val longChatId: Long = update.callbackQuery.message.chatId
            val callBackData: String = update.callbackQuery.data
            val callId: String = update.callbackQuery.id
            messageIdBuffer.add(intMessageId)

            when {
                callBackData == "\uD83D\uDCDA Учить новые слова" -> {
                    val menu = listOf("1", "2", "3", "4", "5")
                    execute(
                        botMenuFunction.categoryMenu(
                            stringChatId,
                            intMessageId,
                            "\uD83D\uDCDA Учить новые слова",
                            menu
                        )
                    )
                }

                callBackData == "\uD83D\uDCD6 Добавить/удалить свои тексты" -> {

                    val editMessageText = EditMessageText()
                    editMessageText.text =
                        "В этом разделе вы можете добавить или удалить собственные тексты для выбранной категории.\nОграничения для добавления:\n● Для текста - он должен содержать не менее двух раздельных слов, содержать не менее 3 знаков и не более 100 знаков, не должен содержать спецсимволов\n● Для каждой категории - не более 100 добавленных текстов" +
                                "\nВыберите категорию:"
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    editMessageText.replyMarkup =
                        botMenuFunction.createDataButtonMenu(Lessons.COMPARE_WORDS.getLessonsTitles(), "#own")
                    execute(editMessageText)

                }

                callBackData == "\uD83D\uDCD2 Уроки других пользователей" -> {
                    val menu = listOf("1", "2", "3", "4", "5")
                    execute(
                        botMenuFunction.categoryMenu(
                            stringChatId,
                            intMessageId,
                            "\uD83D\uDCD2 Уроки других пользователей",
                            menu
                        )
                    )
                }
                callBackData.contains("#own") -> {
                    val categoryText = callBackData.replace("#own", "")
                    val editMessageText: EditMessageText = botMenuFunction.receiveChoseMenu(stringChatId, intMessageId, categoryText, "ㅤㅤㅤㅤㅤㅤВы можете:ㅤㅤㅤㅤㅤㅤ")
                    execute(editMessageText)
                }

                callBackData.contains("#add") -> {
                    val categoryText = callBackData.replace("#add", "")
                    val editMessageText = EditMessageText()
                    editMessageText.text =
                        "\uD83D\uDCD1 Для добавления собственных предложений, необходимо сначала ввести в поле ввода и отправить текст на английском языке, затем его перевод на русском языке\n" +
                                " Введите текст на английском языке, затем отправьте сообщение"
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    execute(editMessageText)
                    lessonCategoryText[stringChatId] = categoryText
                    tempData[stringChatId] = inputRuText
                    messageIdMap[stringChatId] = intMessageId // TODO нужно ли, или не удалится
                }

                callBackData.contains("#todel") -> {
                    val categoryText = callBackData.replace("#todel", "")
                    val userData: UserData = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = botMenuFunction.receiveLessonTextFromDb(categoryText, userData)

                    val editMessageText = EditMessageText()
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId

                    if (userLessonText.isEmpty()){
                        editMessageText.text = "ㅤㅤㅤㅤНет текстов для удаленияㅤㅤㅤㅤㅤㅤㅤ"
                    } else {
                        val lessonTextsList: MutableList<String> = userLessonText.split("#") as MutableList
                        editMessageText.text = "Выберите текст для удаления:ㅤㅤㅤㅤㅤㅤㅤ"
                        editMessageText.replyMarkup = botMenuFunction.createButtonMenuForDelete(lessonTextsList, categoryText)
                        deleteFromDbText[stringChatId] = lessonTextsList
                    }
                    execute(editMessageText)
                }

                callBackData.contains("#show") -> {
                    val categoryText = callBackData.replace("#show", "")
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    val lessonTexts: String = botMenuFunction.receiveLessonTextFromDb(categoryText, userData)

                    val editMessageText = EditMessageText()
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId

                    if (lessonTexts.isEmpty()){
                        editMessageText.text = "ㅤㅤㅤНет текстов для просмотраㅤㅤㅤㅤㅤ"
                    } else {
                        editMessageText.text = "ㅤㅤㅤㅤㅤСписок ваших текстов: ㅤㅤㅤㅤㅤㅤㅤ\n\n\uD83D\uDD39 " + lessonTexts.replace("#", "\n\n\uD83D\uDD39 ").replace("*", "\n\uD83D\uDD38 ")
                    }
                    execute(editMessageText)
                }

                callBackData.contains("@") -> {
                    val indexFromList: Int = callBackData.replaceAfter("@", "").replace("@", "").toInt()
                    val lessonCategory: String = callBackData.replaceBefore("@", "").replace("@", "")

                    val buttonText: String = if (indexFromList >= 0){
                        deleteFromDbText[stringChatId]!![indexFromList].replace("*", "\n\n\uD83D\uDD38 ")
                    } else {
                      "❗ Из раздела «$lessonCategory» будут удалены все тексты пользователя"
                    }

                    val editMessageText = EditMessageText()
                    editMessageText.text = "\uD83D\uDD39 $buttonText\n\nㅤㅤㅤㅤㅤㅤУдалить?ㅤㅤㅤㅤㅤㅤ"
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId

                    val inlineKeyboardMarkup = InlineKeyboardMarkup()
                    val rowsInline = ArrayList<List<InlineKeyboardButton>>()
                    val rowInlineButton = ArrayList<InlineKeyboardButton>()

                    val addButton = InlineKeyboardButton()
                    val delButton = InlineKeyboardButton()

                    addButton.text = "Удалить"
                    addButton.callbackData = "$indexFromList&$lessonCategory"

                    delButton.text = "Отмена"
                    delButton.callbackData = "#cancel"

                    rowInlineButton.add(addButton)
                    rowInlineButton.add(delButton)
                    rowsInline.add(rowInlineButton)
                    inlineKeyboardMarkup.keyboard = rowsInline
                    editMessageText.replyMarkup = inlineKeyboardMarkup

                    execute(editMessageText)
                }

                callBackData.contains("&") -> {
                    val indexFromList: Int = callBackData.replaceAfter("&", "").replace("&", "").toInt()
                    val lessonCategory: String = callBackData.replaceBefore("&", "").replace("&", "")
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    val newLessonText = if (indexFromList >= 0){
                        val lessonTexts: MutableList<String> = deleteFromDbText[stringChatId]!! as MutableList
                        lessonTexts.removeAt(indexFromList)
                        lessonTexts.toString().replace("[", "").replace("]", "").replace(", ", "#")
                    } else {
                        ""
                    }

                    botMenuFunction.removeLessonTextFromDb(lessonCategory, userData, newLessonText)
                    userDataRepository.save(userData)

                    val editMessageText = EditMessageText()
                    editMessageText.text = "ㅤㅤㅤㅤㅤㅤㅤ✅ Удаленоㅤㅤㅤㅤㅤㅤㅤ"
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    execute(editMessageText)

                }


                callBackData == "#cancel" -> {
                    val del = DeleteMessage()
                    del.chatId = stringChatId
                    del.messageId = intMessageId
                    try {
                        execute(del)
                    } catch (e: TelegramApiException) {
                        println("del EXC")
                    }
                }
// TODO >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

                callBackData.contains(Lessons.PRESENT_SIMPLE.title) -> {
                    val user = userDataRepository.findById(longChatId)
                    val userLessonText: String = user.get().presentSimple
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, Lessons.PRESENT_SIMPLE)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)
                }

                callBackData.contains(Lessons.PRESENT_CONTINUOUS.title) -> {
                    val user = userDataRepository.findById(longChatId)
                    val userLessonText: String = user.get().presentContinuous
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, Lessons.PRESENT_CONTINUOUS)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)
                }

                callBackData.contains(Lessons.PASSIVE_VOICE.title) -> {
                    val user = userDataRepository.findById(longChatId)
                    val userLessonText: String = user.get().passiveVoice
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, Lessons.PASSIVE_VOICE)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)
                }

                callBackData.contains(Lessons.PRONOUN_PREPOSITION.title) -> {
                    val user = userDataRepository.findById(longChatId)
                    val userLessonText: String = user.get().pronounAndPreposition
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, Lessons.PRONOUN_PREPOSITION)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)
                }

                callBackData.contains(Lessons.MUCH_MANY_LOT.title) -> {
                    val user = userDataRepository.findById(longChatId)
                    val userLessonText: String = user.get().muchManyLot
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, Lessons.MUCH_MANY_LOT)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)
                }

                callBackData.contains(Lessons.PRESENT_SENTENCE.title) -> {
                    val user = userDataRepository.findById(longChatId)
                    val userLessonText: String = user.get().perfectSentence
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, Lessons.PRESENT_SENTENCE)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)
                }

                callBackData.contains(Lessons.DATE_AND_TIME.title) -> {
                    val user = userDataRepository.findById(longChatId)
                    val userLessonText: String = user.get().dateAndTime
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, Lessons.DATE_AND_TIME)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)
                }

                callBackData.contains(Lessons.COMPARE_WORDS.title) -> {
                    val user = userDataRepository.findById(longChatId)
                    val userLessonText: String = user.get().compareWords
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, Lessons.COMPARE_WORDS)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)
                }

                callBackData.contains(Lessons.VARIOUS_WORDS.title) -> {
                    val user = userDataRepository.findById(longChatId)
                    val userLessonText: String = user.get().variousWords
                    println("TEST 1 userLessonText = $userLessonText") // TODO
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, Lessons.VARIOUS_WORDS)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)
                }

                callBackData.contains("ㅤ") -> { // callBackData содержит пустой char (не space(!) - нажатие кнопки экранной клавиатуры без текста)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step2")
                    execute(editMessageText)
                }

                callBackData.contains("step1") -> {
                    val word: String = callBackData.replace("step1", "")
                    lessonUnitMap[stringChatId]!!.inputText += word

                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step2")
                    execute(editMessageText)
                }

                callBackData.contains("step2") -> {
                    val word: String = callBackData.replace("step2", "")
                    lessonUnitMap[stringChatId]!!.inputText += word

                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!,"step1")
                    execute(editMessageText)
                }

                callBackData == "delword" -> {
                    val splitInputText = lessonUnitMap[stringChatId]!!.inputText.split(" ")

                    if (lessonUnitMap[stringChatId]!!.inputText.isNotEmpty()) lessonUnitMap[stringChatId]!!.inputText =
                        lessonUnitMap[stringChatId]!!.inputText.replace(" " + splitInputText[splitInputText.size - 1], "")

                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!,"step2")
                    execute(editMessageText)
                }

                callBackData.contains("finish") -> {
                    val lessonUnitInputText = lessonUnitMap[stringChatId]!!.inputText
                    val fullText = (lessonUnitInputText + callBackData.replace("finish", "")).trim()

                    val editMessageText = EditMessageText()
                    val messageText: String
                    val callBackText: String
                    val inlineKeyboardMarkup = InlineKeyboardMarkup()
                    val rowsInline = ArrayList<List<InlineKeyboardButton>>()
                    val rowInlineButton = ArrayList<InlineKeyboardButton>()
                    val button = InlineKeyboardButton()

                    if (fullText == lessonUnitMap[stringChatId]!!.englishText) {
                        messageText = "ㅤㅤㅤ✅  Отлично, всё правильно!ㅤㅤㅤ\n\n\uD83D\uDD39 " + lessonUnitMap[stringChatId]!!.englishText
                        callBackText = lessonUnitMap[stringChatId]!!.lessonTitle // установка имени раздела урока в callBackText для переотправки к выбранному уроку
                    } else {
                        messageText = " ㅤㅤㅤㅤㅤㅤㅤ❌  Не верно ㅤㅤㅤㅤㅤㅤㅤ\nПравильно:\n\uD83D\uDD39 " + lessonUnitMap[stringChatId]!!.englishText + "\n\nВаш вариант: \n\uD83D\uDD38 " + fullText
                        callBackText = "step2"
                        lessonUnitMap[stringChatId]!!.inputText = ""
                    }

                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    editMessageText.text = messageText


                    button.text = "Далее"
                    button.callbackData = callBackText
                    rowInlineButton.add(button)
                    rowsInline.add(rowInlineButton)
                    inlineKeyboardMarkup.keyboard = rowsInline
                    editMessageText.replyMarkup = inlineKeyboardMarkup

                    try {
                        execute(editMessageText)
                    } catch (e: TelegramApiException) {
                        println("EXC callBackData(false)")
                    }
                }

                callBackData.contains("test") -> {
                    val menu = listOf("1", "2", "3", "4", "5")
                    execute(botMenuFunction.categoryMenu(stringChatId, intMessageId, "Дата и время", menu))

                    val answerCallbackQuery = AnswerCallbackQuery()// TODO
                    answerCallbackQuery.callbackQueryId = callId
                    answerCallbackQuery.text = "✅" // прикреплённое сообщения
                    answerCallbackQuery.showAlert =
                        true // Присутствие/отсутствие всплывающего окна (текст выводится в любом случае)
                    answerCallbackQuery.cacheTime = 1
                    execute(answerCallbackQuery)


                    for (i in messageIdBuffer) {
                        val del = DeleteMessage()
                        del.chatId = stringChatId
                        del.messageId = i
                        try {
                            execute(del)
                        } catch (e: TelegramApiException) {
                            println("del EXC")
                        }
                    }
                }
            }
        }
    }


    fun createLessonUnit(chatId: String, userLessonText: String, lesson: Lessons): LessonUnit {
        if (lessonsMap[chatId] == null || lessonsMap[chatId]!!.isEmpty()) { // если Map с коллекцией текстов уроков == null или пустая,
            val lessonList = lesson.createLessonText(userLessonText) // коллекция заполняется текстами
            lessonsMap[chatId] = lessonList // в Map, где ключи Id пользователя, в качестве значения добавляется коллекция с текстами уроков
        }
        var chooseRandom: Int = (0 until lessonsMap[chatId]!!.size - 1).random() // выбор случайного текста с уроком из коллекции в Map

        var splitText: List<String> = lessonsMap[chatId]!![chooseRandom].split("*") // получение ру. текста и eng текста

        if (lessonUnitMap[chatId] != null && lessonUnitMap[chatId]!!.russianText == splitText[0]){ // lessonUnit без дублей
            chooseRandom = if (chooseRandom + 1 < lessonsMap[chatId]!!.size - 1) chooseRandom + 1 else chooseRandom - 1
            splitText = lessonsMap[chatId]!![chooseRandom].split("*")
        }

        val forButtonText: MutableList<String> = splitText[1].split(" ") as MutableList<String>

        while(forButtonText.size < 10){ // создание текста из 10 слов для кнопок экранной клавиатуры
            val chooseRandomWord: Int = (0 until lesson.randomWords.size - 1).random()
            forButtonText.add(lesson.randomWords[chooseRandomWord])
        }

        forButtonText.sortWith { first, second -> first.length - second.length }

        return LessonUnit(splitText[0], splitText[1], forButtonText, "", lesson.title)

    }


    override fun getBotUsername(): String {
        return "t.me/ForEnglishTrainingBot."
    }


}
