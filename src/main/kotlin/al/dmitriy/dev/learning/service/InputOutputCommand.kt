package al.dmitriy.dev.learning.service

import al.dmitriy.dev.learning.dataunit.DataUnit
import al.dmitriy.dev.learning.extendfunctions.putData
import al.dmitriy.dev.learning.dataunit.LessonUnit
import al.dmitriy.dev.learning.lesson.Lessons
import al.dmitriy.dev.learning.model.UserData
import al.dmitriy.dev.learning.model.UserDataDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.lang.StringBuilder

@Component
class InputOutputCommand(@Autowired val userDataRepository: UserDataDao) :
    TelegramLongPollingBot("6586869115:AAGaTQdKRxojv5vl4h0uYa2LLIa2cbcU82c") {

    init {
        val botCommandList: List<BotCommand> = listOf(
            BotCommand("/start", "Запуск бота"),
            BotCommand("/mydata", "Данные пользователя"),
            BotCommand("/deletedata", "Удалить данные пользователя"),
            BotCommand("/help", "Полезная информация"))
            try {
            this.execute(SetMyCommands(botCommandList, BotCommandScopeDefault(), null))
        } catch (e: TelegramApiException){
            println("Exc. ->" + e.message)
        }
    }

    private val randomWords = listOf("I", "You", "he", "she", "they", "him", "her", "it", "them", "mine", "its", "our", "itself", "myself", "herself", "yourselves", "themselves", "ourselves", "himself", "those", "these",
        "that", "what", "which", "whose", "who", "whom", "me", "at", "is", "to", "am", "go", "for", "in", "us")
    private val messageIdBuffer = mutableListOf<Int>()
    private val categoryTitles = listOf("\uD83D\uDCDA Учить новые слова", "\uD83D\uDCD2 Уроки других пользователей", "\uD83D\uDCD6 Добавить/удалить свои тексты")
    private val botMenuFunction: BotMenuFunction = BotMenuFunction()
    private val lessonsMap = HashMap<String, MutableList<String>>()
    private val deleteInDbText = HashMap<String, List<String>>()
    private val lessonUnitMap = HashMap<String, LessonUnit>()
    private val dataUnitMap = HashMap<String, DataUnit>()
    private val userLessonText = HashMap<String, String>()
    private val cashLessonCategory = HashMap<String, String>()
    private val tempData = HashMap<String, String>()
    private val messageIdMap = HashMap<String, Int>()  // Id сообщения для удаления
    private val hintMessageIdMap = HashMap<String, Int>()
    private val startMessageIdMap = HashMap<String, Int>()  // Id сообщения для удаления

    private final val inputRuText = "INPUT_RU_TEXT"
    private final val inputEnText = "INPUT_EN_TEXT"
    private final val inputRepeatText = "INPUT_REPEAT_TEXT"

    private val space = "ㅤ"


    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val messageText: String = update.message.text
            val longChatId: Long = update.message.chatId
            val stringChatId: String = longChatId.toString()
            val intMessageId: Int = update.message.messageId
            messageIdBuffer.add(intMessageId)


            when (tempData[stringChatId]) {

                inputEnText -> {
                    tempData[stringChatId] = ""
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0)

                    val forMessageText: String

                    if(messageText.contains("⚙") || messageText.contains("\uD83D\uDCDA") || messageText.contains("/") ||
                        messageText.contains("#") || messageText.contains("*") || (cashLessonCategory[stringChatId]!! == "WFL" &&
                                messageText.length > 15 ) || (messageText.split(" ").size > 10) || messageText.length > 100) {

                        // lessonCategoryText[stringChatId] = ""
                        forMessageText = "ㅤㅤㅤㅤ❌  Текст введён не корректноㅤㅤㅤㅤㅤㅤㅤ"
                    } else {
                        forMessageText = "\uD83D\uDCD1 Теперь введите текст на русском языке, затем отправьте сообщение"
                        userLessonText[stringChatId] = messageText // сообщение пользователя сохраняется в Map
                        tempData[stringChatId] = inputRuText // перенаправление в ветку: when (tempData[stringChatId]) -> inputRuText
                    }

                    val sendMessage = SendMessage(stringChatId, forMessageText)
                    val inlineKeyboardMarkup = InlineKeyboardMarkup()
                    val rowsInline = ArrayList<List<InlineKeyboardButton>>()
                    val rowInlineButton = ArrayList<InlineKeyboardButton>()
                    val backButton = InlineKeyboardButton()
                    backButton.text = "\uD83D\uDD19  Отмена"
                    backButton.callbackData = "#own"
                    rowInlineButton.add(backButton)
                    rowsInline.add(rowInlineButton)
                    inlineKeyboardMarkup.keyboard = rowsInline
                    sendMessage.replyMarkup = inlineKeyboardMarkup

                    execute(sendMessage)
                    messageIdMap[stringChatId] = intMessageId // Id сообщения для удаления
                }

                inputRuText -> {
                    tempData[stringChatId] = ""
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 1)

                    val forMessageText: String

                    if(messageText.contains("⚙") || messageText.contains("\uD83D\uDCDA") || messageText.contains("/") ||
                        messageText.contains("#") || messageText.contains("*") || (cashLessonCategory[stringChatId]!! == "WFL" &&
                                messageText.length > 15 ) || messageText.length > 100) {

                        // lessonCategoryText[stringChatId] = ""
                        forMessageText = "ㅤㅤㅤㅤㅤㅤ❌  Текст введён не корректноㅤㅤㅤㅤㅤㅤㅤ"
                    } else {
                        forMessageText = "ㅤㅤㅤㅤ✅  Текст был добавленㅤㅤㅤㅤㅤㅤ"
                        val userData = userDataRepository.findById(longChatId).get()

                        val lessonText = if(botMenuFunction.receiveLessonTextFromDb(cashLessonCategory[stringChatId]!!, userData).isEmpty()){
                            update.message.text + "*" + userLessonText[stringChatId]!! // если в бд нет текстов, первый текст добавляется без символа "#"
                        } else {
                            "#" + update.message.text + "*" + userLessonText[stringChatId]!! // если в бд есть тексты, новый текст добавляется с символом "#"
                        }
                        val updateUserData: UserData = botMenuFunction.updateLessonTextInDb(lessonText, cashLessonCategory[stringChatId]!!, userData)
                        userDataRepository.save(updateUserData)
                    }

                    userLessonText[stringChatId] = "" // сообщение пользователя сохранённое в Map
                    // cashLessonCategory[stringChatId] = ""

                    val sendMessage = SendMessage(stringChatId, forMessageText)
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
                    execute(sendMessage)
                    messageIdMap[stringChatId] = intMessageId
                }

                inputRepeatText -> {
                    tempData[stringChatId] = ""

                    val  dataUnit: DataUnit = dataUnitMap[stringChatId]!!

                    val textForMessage: String = if (dataUnit.secondText.equals(messageText, ignoreCase = true)){
                        "ㅤㅤㅤㅤㅤㅤ✅  Правильно!ㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤ"
                    } else {
                        " ㅤㅤㅤㅤㅤㅤㅤ❌  Не верно ㅤㅤㅤㅤㅤㅤㅤ\nПравильно:\n\uD83D\uDD39 " + dataUnit.secondText + "\n\nВаш вариант: \n\uD83D\uDD38 " + messageText
                    }

                    val editMessageText = botMenuFunction.receiveButtonEditMessage(stringChatId, dataUnit.id, textForMessage, "#tran", listOf("Далее"))
                    execute(editMessageText)
                    // messageIdMap[stringChatId] = intMessageId   // Id сообщения для удаления
                }
            }


            when {
                messageText == "/start" -> {
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 1, 2)
                    deletePreviousMessage(stringChatId, startMessageIdMap[stringChatId], 1)
                    val url = "https://disk.yandex.ru/i/3p50prAOAy8SNA"
                    val sendPhoto = botMenuFunction.receiveBillboard(stringChatId, "Вас приветствует learning English!", url)
                    execute(sendPhoto)
                    startMessageIdMap[stringChatId] = intMessageId
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
                    if (userDataRepository.findById(stringChatId.toLong()).isEmpty) {
                        val userData = UserData() // TODO
                        userData.chatId = longChatId
                        userData.username = update.message.chat.userName
                        userDataRepository.save(userData)
                    }

                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    // Сообщение с подсказками будет выведено, если не пользователь не отключил опцию
                    if(userData.isShowHint){
                        deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 1, 2, 3)
                        val hintMessage = SendPhoto()
                        hintMessage.putData(stringChatId, "https://disk.yandex.ru/i/va9-A6PzWOJ0qA")
                        execute(hintMessage)
                        hintMessageIdMap[stringChatId] = intMessageId
                    } else {
                        deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 1, 2)
                    }

                    userLessonText[stringChatId] = ""

                    if (lessonsMap[stringChatId] != null) lessonsMap[stringChatId]!!.clear()

                    val collectCategoryTitles = mutableListOf<String>()
                    collectCategoryTitles.addAll(categoryTitles)
                    collectCategoryTitles.addAll(Lessons.PRESENT_SIMPLE.getLessonsTitles())
                    val agreementText = "ㅤㅤㅤㅤㅤㅤㅤ\uD835\uDC0B\uD835\uDC04\uD835\uDC12\uD835\uDC12\uD835\uDC0E\uD835\uDC0D\uD835\uDC12ㅤㅤㅤㅤㅤㅤㅤㅤ"
                    val sendMessage: SendMessage = botMenuFunction.categoryMenu(longChatId, agreementText, collectCategoryTitles)
                    execute(sendMessage)
                    messageIdMap[stringChatId] = intMessageId
                }

                messageText == "Настройки ⚙" -> {
                    if (userDataRepository.findById(stringChatId.toLong()).isEmpty) {
                        val userData = UserData() // TODO
                        userData.chatId = longChatId
                        userData.username = update.message.chat.userName
                        userDataRepository.save(userData)
                    }

                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    // Сообщение с подсказками будет выведено, если не пользователь не отключил опцию
                    if(userData.isShowHint){
                        deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 1, 2, 3)
                        val hintMessage = SendPhoto()
                        hintMessage.putData(stringChatId, "https://disk.yandex.ru/i/va9-A6PzWOJ0qA")
                        execute(hintMessage)
                        hintMessageIdMap[stringChatId] = intMessageId
                    } else {
                        deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 1, 2)
                    }

                    messageIdMap[stringChatId] = intMessageId

                    val menu = listOf("Только мои тексты", "Только простые тексты", "Показывать изображения")
                    val sendMessage: SendMessage = botMenuFunction.receiveSettingMenu(stringChatId, "Настройки", userData.isSimpleTexts, userData.isUsersTexts, userData.isShowHint)
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

            if (startMessageIdMap[stringChatId] != null) {
                val del = DeleteMessage()
                del.chatId = stringChatId
                del.messageId = startMessageIdMap[stringChatId]!!
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
                    if(userDataRepository.findById(longChatId).get().isShowHint){
                        deletePreviousMessage(stringChatId, hintMessageIdMap[stringChatId], 1) // удаление окна подсказок
                    }

                    cashLessonCategory[stringChatId] = "\uD83D\uDCDA Учить новые слова"
                    val editMessageText: EditMessageText = botMenuFunction.receiveLearnWordMenu(stringChatId, intMessageId, "❗️ Для того чтобы начать тренировку, сначала необходимо добавить слова для изучения. Добавить или удалить слова и тексты вы можете с помощью соответствующих кнопок меню")
                    execute(editMessageText)
                }

                callBackData == "\uD83D\uDCD6 Добавить/удалить свои тексты" -> {
                    if(userDataRepository.findById(longChatId).get().isShowHint){
                        deletePreviousMessage(stringChatId, hintMessageIdMap[stringChatId], 1) // удаление окна подсказок
                    }

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
                    if(userDataRepository.findById(longChatId).get().isShowHint){
                        deletePreviousMessage(stringChatId, hintMessageIdMap[stringChatId], 1) // удаление окна подсказок
                    }

                    val lessonTitles: List<String> = Lessons.PRESENT_SIMPLE.getLessonsTitles()
                    val editMessageText: EditMessageText = botMenuFunction.receiveCategoryMenu(stringChatId, intMessageId,"#aul", "В этом разделе вы можете воспользоваться текстами уроков других пользователей.\nВыберите категорию:", lessonTitles)
                    execute(editMessageText)
                }

                 callBackData == "#addw" -> {
                val editMessageText = EditMessageText()
                     editMessageText.text = "Введите слово на английском языке и отправьте сообщение"
                execute(editMessageText)
                }

                callBackData.contains("#tran") -> {
                    val editMessageText: EditMessageText
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                        if(userData.wordsForLearning.isEmpty()){
                        editMessageText = EditMessageText()
                        editMessageText.text = "Нет слов для тренировки"
                    } else {
                        if(userLessonText[stringChatId] == null || userLessonText[stringChatId]!!.isEmpty()){
                            userLessonText[stringChatId] = userData.wordsForLearning
                        }
                     if((0 .. 1).random() == 1) {
                         lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText[stringChatId]!!, "#tran", randomWords)
                         editMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step2")
                         editMessageText.text = "Выберите перевод для слова:  ㅤㅤㅤㅤㅤㅤㅤ\n\uD83D\uDD39 " + lessonUnitMap[stringChatId]!!.russianText + "  ㅤㅤㅤㅤㅤㅤㅤ\n\n✏ "
                     } else {
                        val dataUnit: DataUnit = createDataUnit(stringChatId, userLessonText[stringChatId]!!, intMessageId)
                         dataUnitMap[stringChatId] = dataUnit

                         editMessageText = EditMessageText()
                         editMessageText.text = "Введите перевод для слова: " + dataUnit.firstText
                         tempData[stringChatId] = inputRepeatText
                         }
                    }
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    execute(editMessageText)
                }

                callBackData.contains("#aul") -> {
                    val editMessageText: EditMessageText

                    if (userLessonText[stringChatId] == null || userLessonText[stringChatId]!!.isEmpty()){

                    val categoryText = callBackData.replace("#aul", "")

                    val bufferLessonText = StringBuilder()

                    val usersData: Iterable<UserData> = userDataRepository.findAll()
                    for(data in usersData){
                        if(bufferLessonText.length > 100000) break
                        if(data.chatId == longChatId) continue

                        if(bufferLessonText.isEmpty()){
                            bufferLessonText.append(botMenuFunction.receiveLessonTextFromDb(categoryText, data))
                        } else {
                            bufferLessonText.append("#").append(botMenuFunction.receiveLessonTextFromDb(categoryText, data))
                        }
                    }

                    val fromBufferText = bufferLessonText.toString()
                    userLessonText[stringChatId] = fromBufferText

                    if(!fromBufferText.contains("#")){
                        editMessageText = EditMessageText()
                        editMessageText.chatId = stringChatId
                        editMessageText.messageId = intMessageId
                        editMessageText.text = "В данной категории отсутствуют тексты"
                    } else {
                        lessonUnitMap[stringChatId] = createLessonUnit(stringChatId,  userLessonText[stringChatId]!!, "#aul", randomWords)
                        editMessageText  = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    }
                    } else {
                        lessonUnitMap[stringChatId] = createLessonUnit(stringChatId,  userLessonText[stringChatId]!!, "#aul", randomWords)
                        editMessageText  = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    }
                    execute(editMessageText)
                }

                callBackData.contains("#own") -> {
                    tempData[stringChatId] = "" // сброс временных данных TODO
                    val categoryText = callBackData.replace("#own", "")
                    cashLessonCategory[stringChatId] = categoryText.ifEmpty { cashLessonCategory[stringChatId]!! } // кэширование категории
                    val editMessageText: EditMessageText = botMenuFunction.receiveChoseMenu(stringChatId, intMessageId, cashLessonCategory[stringChatId]!!, "\uD83D\uDD39 Категория:   " + cashLessonCategory[stringChatId]!! + "\n\n" +
                            "   ㅤㅤㅤㅤㅤㅤВы можете:ㅤㅤㅤㅤㅤㅤ")
                    execute(editMessageText)
                }

                callBackData.contains("#add") -> {
                    val categoryText = callBackData.replace("#add", "")

                    val userData: UserData = userDataRepository.findById(longChatId).get()
                    val lessonText: String = botMenuFunction.receiveLessonTextFromDb(categoryText, userData)

                    val editMessageText = EditMessageText()

                    if(lessonText.split("#").size > 100){
                        editMessageText.text = "ㅤㅤㅤㅤ❌  Достигнут лимит текстов в категорииㅤㅤㅤㅤㅤ"
                    } else {
                        editMessageText.text = "\uD83D\uDCD1 Для добавления собственных предложений, необходимо сначала ввести в поле ввода и отправить текст на английском языке, затем его перевод на русском языке\n" +
                                    "Введите текст на английском языке, затем отправьте сообщение"
                        cashLessonCategory[stringChatId] = categoryText
                        tempData[stringChatId] = inputEnText
                    }

                    val inlineKeyboardMarkup = InlineKeyboardMarkup()
                    val rowsInline = ArrayList<List<InlineKeyboardButton>>()
                    val rowInlineButton = ArrayList<InlineKeyboardButton>()
                    val backButton = InlineKeyboardButton()
                    backButton.text = "\uD83D\uDD19  Отмена"
                    backButton.callbackData = "#own"
                    rowInlineButton.add(backButton)
                    rowsInline.add(rowInlineButton)
                    inlineKeyboardMarkup.keyboard = rowsInline
                    editMessageText.replyMarkup = inlineKeyboardMarkup

                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    execute(editMessageText)

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
                        val inlineKeyboardMarkup = InlineKeyboardMarkup()
                        val rowsInline = ArrayList<List<InlineKeyboardButton>>()
                        val rowInlineButton = ArrayList<InlineKeyboardButton>()
                        val backButton = InlineKeyboardButton()
                        backButton.text = "\uD83D\uDD19  Назад"
                        backButton.callbackData = "#own"
                        rowInlineButton.add(backButton)
                        rowsInline.add(rowInlineButton)
                        inlineKeyboardMarkup.keyboard = rowsInline
                        editMessageText.replyMarkup = inlineKeyboardMarkup
                    } else {
                        val lessonTextsList: MutableList<String> = userLessonText.split("#") as MutableList
                        editMessageText.text = "Выберите текст для удаления:ㅤㅤㅤㅤㅤㅤㅤ"
                        editMessageText.replyMarkup = botMenuFunction.createButtonMenuForDelete(lessonTextsList, categoryText)
                        deleteInDbText[stringChatId] = lessonTextsList
                    }
                    execute(editMessageText)
                }

                callBackData.contains("#hint") -> {
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    if (userData.isShowHint) {
                        userData.isShowHint = false
                        userDataRepository.save(userData)
                    } else {
                        userData.isShowHint = true
                        userDataRepository.save(userData)
                    }

                    val editMessageText = EditMessageText()
                    editMessageText.text = "✅  Изменено"
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    execute(editMessageText)
                }

                callBackData.contains("#usrtxt") -> {
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    if (userData.isUsersTexts) {
                        userData.isUsersTexts = false
                        userDataRepository.save(userData)
                    } else {
                        userData.isUsersTexts = true
                        userDataRepository.save(userData)
                    }

                    val editMessageText = EditMessageText()
                    editMessageText.text = "✅  Изменено"
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    execute(editMessageText)
                }

                callBackData.contains("#simple") -> {
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    if (userData.isSimpleTexts) {
                        userData.isSimpleTexts = false
                        userDataRepository.save(userData)
                    } else {
                        userData.isSimpleTexts = true
                        userDataRepository.save(userData)
                    }

                    val editMessageText = EditMessageText()
                    editMessageText.text = "✅  Изменено"
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
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

                    val inlineKeyboardMarkup = InlineKeyboardMarkup()
                    val rowsInline = ArrayList<List<InlineKeyboardButton>>()
                    val rowInlineButton = ArrayList<InlineKeyboardButton>()
                    val backButton = InlineKeyboardButton()
                    backButton.text = "\uD83D\uDD19  Назад"
                    backButton.callbackData = "#own"
                    rowInlineButton.add(backButton)
                    rowsInline.add(rowInlineButton)
                    inlineKeyboardMarkup.keyboard = rowsInline
                    editMessageText.replyMarkup = inlineKeyboardMarkup


                    execute(editMessageText)
                }

                callBackData.contains("@") -> {
                    val indexFromList: Int = callBackData.replaceAfter("@", "").replace("@", "").toInt()
                    val lessonCategory: String = callBackData.replaceBefore("@", "").replace("@", "")

                    val buttonText: String = if (indexFromList >= 0){
                        deleteInDbText[stringChatId]!![indexFromList].replace("*", "\n\n\uD83D\uDD38 ")
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
                        val lessonTexts: MutableList<String> = deleteInDbText[stringChatId]!! as MutableList
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

                    val inlineKeyboardMarkup = InlineKeyboardMarkup()
                    val rowsInline = ArrayList<List<InlineKeyboardButton>>()
                    val rowInlineButton = ArrayList<InlineKeyboardButton>()
                    val backButton = InlineKeyboardButton()
                    backButton.text = "\uD83D\uDD19  Назад"
                    backButton.callbackData = "#own"
                    rowInlineButton.add(backButton)
                    rowsInline.add(rowInlineButton)
                    inlineKeyboardMarkup.keyboard = rowsInline
                    editMessageText.replyMarkup = inlineKeyboardMarkup

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
                    val user: UserData = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.presentSimple
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.PRESENT_SIMPLE)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]!! + 1
                        editMessageMedia.messageId = test
                        editMessageMedia.media = InputMediaPhoto("https://disk.yandex.ru/i/SbIfecNY8lq1aA")
                        try {
                            execute(editMessageMedia)
                        } catch (e: TelegramApiException){

                        }
                    }
                }

                callBackData.contains(Lessons.PRESENT_CONTINUOUS.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.presentContinuous
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.PRESENT_CONTINUOUS)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]!! + 1
                        editMessageMedia.messageId = test
                        editMessageMedia.media = InputMediaPhoto("https://disk.yandex.ru/i/SbIfecNY8lq1aA")
                        try {
                            execute(editMessageMedia)
                        } catch (e: TelegramApiException){

                        }
                    }
                }

                callBackData.contains(Lessons.PASSIVE_VOICE.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.passiveVoice
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.PASSIVE_VOICE)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]!! + 1
                        editMessageMedia.messageId = test
                        editMessageMedia.media = InputMediaPhoto("https://disk.yandex.ru/i/SbIfecNY8lq1aA")
                        try {
                            execute(editMessageMedia)
                        } catch (e: TelegramApiException){

                        }
                    }
                }

                callBackData.contains(Lessons.PRONOUN_PREPOSITION.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.pronounAndPreposition
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.PRONOUN_PREPOSITION)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]!! + 1
                        editMessageMedia.messageId = test
                        editMessageMedia.media = InputMediaPhoto("https://disk.yandex.ru/i/SbIfecNY8lq1aA")
                        try {
                            execute(editMessageMedia)
                        } catch (e: TelegramApiException){

                        }
                    }
                }

                callBackData.contains(Lessons.MUCH_MANY_LOT.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.muchManyLot
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.MUCH_MANY_LOT)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]!! + 1
                        editMessageMedia.messageId = test
                        editMessageMedia.media = InputMediaPhoto("https://disk.yandex.ru/i/SbIfecNY8lq1aA")
                        try {
                            execute(editMessageMedia)
                        } catch (e: TelegramApiException){

                        }
                    }
                }

                callBackData.contains(Lessons.PRESENT_SENTENCE.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.perfectSentence
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.PRESENT_SENTENCE)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]!! + 1
                        editMessageMedia.messageId = test
                        editMessageMedia.media = InputMediaPhoto("https://disk.yandex.ru/i/SbIfecNY8lq1aA")
                        try {
                            execute(editMessageMedia)
                        } catch (e: TelegramApiException){

                        }
                    }
                }

                callBackData.contains(Lessons.DATE_AND_TIME.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.dateAndTime
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.DATE_AND_TIME)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]!! + 1
                        editMessageMedia.messageId = test
                        editMessageMedia.media = InputMediaPhoto("https://disk.yandex.ru/i/SbIfecNY8lq1aA")
                        try {
                            execute(editMessageMedia)
                        } catch (e: TelegramApiException){

                        }
                    }
                }

                callBackData.contains(Lessons.COMPARE_WORDS.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.compareWords
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.COMPARE_WORDS)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]!! + 1
                        editMessageMedia.messageId = test
                        editMessageMedia.media = InputMediaPhoto("https://disk.yandex.ru/i/SbIfecNY8lq1aA")
                        try {
                            execute(editMessageMedia)
                        } catch (e: TelegramApiException){

                        }
                    }
                }

                callBackData.contains(Lessons.VARIOUS_WORDS.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.variousWords
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.VARIOUS_WORDS)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]!! + 1
                        editMessageMedia.messageId = test
                        editMessageMedia.media = InputMediaPhoto("https://disk.yandex.ru/i/SbIfecNY8lq1aA")
                        try {
                            execute(editMessageMedia)
                        } catch (e: TelegramApiException){

                        }
                    }
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

                    if (fullText.equals(lessonUnitMap[stringChatId]!!.englishText, ignoreCase = true)) {
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

            }
        }
    }


    fun createLessonUnit(chatId: String, userLessonText: String, isOnlyUserText: Boolean, lesson: Lessons): LessonUnit {
        if (lessonsMap[chatId] == null || lessonsMap[chatId]!!.isEmpty()) { // если Map с коллекцией текстов уроков == null или пустая,
            val lessonList = if (isOnlyUserText && userLessonText.isNotEmpty()){
                if (userLessonText.contains("#")) userLessonText.split("#") as MutableList<String> else mutableListOf(userLessonText)
            } else {
                 lesson.createLessonText(userLessonText) // коллекция заполняется текстами
            }

            lessonsMap[chatId] = lessonList // в Map, где ключи Id пользователя, в качестве значения добавляется коллекция с текстами уроков
        }
        var chooseRandom: Int = (0 until lessonsMap[chatId]!!.size).random() // выбор случайного текста с уроком из коллекции в Map

        var splitText: List<String> = lessonsMap[chatId]!![chooseRandom].split("*") // разделение ру. текста и eng текста

        if (lessonUnitMap[chatId] != null && lessonUnitMap[chatId]!!.russianText == splitText[0]){ // lessonUnit без дублей
            chooseRandom = if (chooseRandom + 1 < lessonsMap[chatId]!!.size - 1) chooseRandom + 1 else chooseRandom
            splitText = lessonsMap[chatId]!![chooseRandom].split("*")
        }

        val forButtonText = mutableListOf<String>()
        val englishText: MutableList<String> = splitText[1].split(" ") as MutableList<String>
        forButtonText.addAll(englishText)

        while(forButtonText.size < 10){ // создание текста из 10 слов для кнопок экранной клавиатуры
            val chooseRandomWord: Int = (0 until randomWords.size - 1).random()
            forButtonText.add(randomWords[chooseRandomWord])
        }

        forButtonText.sortWith { first, second -> first.length - second.length }

        return LessonUnit(splitText[0], splitText[1], forButtonText, "", lesson.title)
    }


    fun createLessonUnit(chatId: String, usersLessonText: String, lessonCategory: String, randomWords: List<String>): LessonUnit {
        if (lessonsMap[chatId] == null || lessonsMap[chatId]!!.isEmpty()) { // если Map с коллекцией текстов уроков == null или пустая,
            val splitText = if (usersLessonText.contains("#")){
                usersLessonText.split("#") as MutableList<String>
            } else {
                mutableListOf(usersLessonText)
            }
            lessonsMap[chatId] = splitText// в Map, где ключи Id пользователя, в качестве значения добавляется коллекция с текстами уроков
        }

        var chooseRandom: Int = (0 until lessonsMap[chatId]!!.size).random() // выбор случайного текста с уроком из коллекции в Map

        var splitText: List<String> = lessonsMap[chatId]!![chooseRandom].split("*") // разделение ру. текста и eng текста

        if (lessonUnitMap[chatId] != null && lessonUnitMap[chatId]!!.russianText == splitText[0]){ // lessonUnit без дублей
            chooseRandom = if (chooseRandom + 1 < lessonsMap[chatId]!!.size - 1) chooseRandom + 1 else chooseRandom
            splitText = lessonsMap[chatId]!![chooseRandom].split("*")
        }

        val forButtonText = mutableListOf<String>()
        val englishText: MutableList<String> = splitText[1].split(" ") as MutableList<String>
        forButtonText.addAll(englishText)

        while(forButtonText.size < 10){ // создание текста из 10 слов для кнопок экранной клавиатуры
            val chooseRandomWord: Int = (0 until randomWords.size - 1).random()
            forButtonText.add(randomWords[chooseRandomWord])
        }

        forButtonText.sortWith { first, second -> first.length - second.length }
            return LessonUnit(splitText[0], splitText[1], forButtonText, "", lessonCategory)
    }


    fun createDataUnit(chatId: String, usersLessonText: String, messageId: Int): DataUnit {
        if (lessonsMap[chatId] == null || lessonsMap[chatId]!!.isEmpty()) { // если Map с коллекцией текстов уроков == null или пустая,
            val splitText = if (usersLessonText.contains("#")){
                usersLessonText.split("#") as MutableList<String>
            } else {
                mutableListOf(usersLessonText)
            }
            lessonsMap[chatId] = splitText// в Map, где ключи Id пользователя, в качестве значения добавляется коллекция с текстами уроков
        }

        val chooseRandom: Int = (0 until lessonsMap[chatId]!!.size).random() // выбор случайного текста с уроком из коллекции в Map

        val splitText = lessonsMap[chatId]!![chooseRandom].split("*")

        val dataUnit: DataUnit = if((0 .. 1).random() == 1){
            DataUnit(splitText[0], splitText[1], messageId)
        } else {
            DataUnit(splitText[1], splitText[0], messageId)
        }
        return dataUnit
    }

    // Метод служит для удаления предыдущих сообщений (история чата), чтобы на экране могло присутствовать одновременно тольк
    private fun deletePreviousMessage(stringChatId: String, messageId: Int?, vararg messageIdUp: Int){
        if (messageId != null) {
            for (i in messageIdUp) {
                val deleteMessage = DeleteMessage()
                    deleteMessage.chatId = stringChatId
                    val updatedMessageId = messageId + i
                    deleteMessage.messageId = updatedMessageId
                try {
                    execute(deleteMessage)
                } catch (e: TelegramApiException) {
                    println("deleteMessage EXC " + messageIdMap[stringChatId])
                }
            }
        }
    }





    override fun getBotUsername(): String {
        return "t.me/ForEnglishTrainingBot."
    }


}
