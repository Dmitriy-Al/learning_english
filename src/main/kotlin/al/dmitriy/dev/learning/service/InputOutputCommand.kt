package al.dmitriy.dev.learning.service

import al.dmitriy.dev.learning.dataunit.DataUnit
import al.dmitriy.dev.learning.extendfunctions.putData
import al.dmitriy.dev.learning.dataunit.LessonUnit
import al.dmitriy.dev.learning.extendfunctions.protectedExecute
import al.dmitriy.dev.learning.lesson.Lessons
import al.dmitriy.dev.learning.model.UserData
import al.dmitriy.dev.learning.model.UserDataDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Component
class InputOutputCommand(@Autowired val userDataRepository: UserDataDao) :
    TelegramLongPollingBot("6586869115:AAGaTQdKRxojv5vl4h0uYa2LLIa2cbcU82c") {

    init {
        val botCommandList: List<BotCommand> = listOf(
            BotCommand("/sta", "Запуск бота"),
            BotCommand("/start", "Запуск бота"),
            BotCommand("/mydata", "Данные пользователя"),
            BotCommand("/help", "Полезная информация"),
            BotCommand("/deletedata", "❗Удаление всей информации пользователя"))
            try {
            this.execute(SetMyCommands(botCommandList, BotCommandScopeDefault(), null))
        } catch (e: TelegramApiException){
            println("Exc. botCommandList " + e.message)
        }
    }

    private val randomWords = listOf("I", "You", "he", "she", "they", "him", "her", "it", "them", "mine", "its", "our", "itself", "myself", "herself", "yourselves", "themselves", "ourselves", "himself", "those", "these",
        "that", "what", "which", "whose", "who", "whom", "me", "at", "is", "to", "am", "go", "for", "in", "us", "be")


    private var urlForTopBillboard = ""
    private var urlForLowBillboard = ""
    private var messageForBillboard = ""
    private val localDateTime = LocalDateTime.now()
    private val botMenuFunction: BotMenuFunction = BotMenuFunction()
    private val lessonsMap = HashMap<String, MutableList<String>>()
    private val deleteInDbText = HashMap<String, List<String>>()
    private val lessonUnitMap = HashMap<String, LessonUnit>()
    private val dataUnitMap = HashMap<String, DataUnit>()
    private val userLessonText = HashMap<String, String>()
    private val cashLessonCategory = HashMap<String, String>()
    private val tempData = HashMap<String, String>()
    private val messageIdMap = HashMap<String, Int>()  // Id сообщения для удаления
    private val saveStartMessageId = HashMap<String, Int>()  // Id сообщения для удаления
    private val hintMessageIdMap = HashMap<String, Int>()
    private val saveHintMessageId = HashMap<String, Int>()
    private val saveTrainingMessageId = HashMap<String, Int>()
    private val properlyLessonAnswer = HashMap<String, Int>()
    private val viewAsChat = HashMap<String, Boolean>()

    private final val inputRuText = "INPUT_RU_TEXT"
    private final val inputEnText = "INPUT_EN_TEXT"
    private final val inputRepeatText = "INPUT_REPEAT_TEXT"
    private final val inputTopBillboardUrl = "TOP_BILLBOARD_URL"
    private final val inputLowBillboardUrl = "LOW_BILLBOARD_URL"
    private final val inputUserFirstName = "SAVE_USER_FIRST_NAME"
    private final val inputMessageForBillboard = "MESSAGE_FOR_BILLBOARD"
    private final val inputMessageForAllUsers = "MESSAGE_FOR_ALL_USERS"


    override fun getBotUsername(): String {
        return "t.me/ForEnglishTrainingBot."
    }



        private fun deletePreviousMessage(stringChatId: String, messageId: Int?, vararg messageIdUp: Int){
        if (saveStartMessageId[stringChatId] == null) saveStartMessageId[stringChatId] = 0
        if (saveHintMessageId[stringChatId] == null) saveHintMessageId[stringChatId] = 0
            if (messageId != null && (viewAsChat[stringChatId] != null && !viewAsChat[stringChatId]!!)) messageIdUp.forEach { e -> if(e + messageId != saveStartMessageId[stringChatId]!!
                && e + messageId != saveHintMessageId[stringChatId]!!) protectedExecute(DeleteMessage().putData(stringChatId, e + messageId)) }
        }


    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val longChatId: Long = update.message.chatId
            val intMessageId: Int = update.message.messageId
            val updateMessageText: String = update.message.text
            val stringChatId: String = longChatId.toString()


            when (tempData[stringChatId]) {

                inputEnText -> {
                    tempData[stringChatId] = ""
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0)

                    val forMessageText: String

                    if(botMenuFunction.isTextIncorrect(updateMessageText, cashLessonCategory[stringChatId]!!)){
                     messageIdMap[stringChatId] = saveStartMessageId[stringChatId]!!  // для удаления сообщения, если выполнение прервано командой /start
                        forMessageText = "ㅤㅤㅤㅤ❌  Текст введён не корректноㅤㅤㅤㅤㅤㅤㅤ"
                    } else {
                        forMessageText = "\uD83D\uDCD1 Теперь введите текст на русском языке, затем отправьте сообщение"
                        userLessonText[stringChatId] = updateMessageText // сообщение пользователя сохраняется в Map
                        tempData[stringChatId] = inputRuText // перенаправление в ветку: when (tempData[stringChatId]) -> inputRuText
                    }
                    val sendMessage: SendMessage = botMenuFunction.receiveGoBackMenu(stringChatId, forMessageText)
                    messageIdMap[stringChatId] = execute(sendMessage).messageId - 1
                }

                inputRuText -> {
                    tempData[stringChatId] = ""
                        deletePreviousMessage(stringChatId, messageIdMap[stringChatId],  1)
                    val forMessageText: String

                    if(botMenuFunction.isTextIncorrect(updateMessageText, cashLessonCategory[stringChatId]!!)){
                        messageIdMap[stringChatId] = saveStartMessageId[stringChatId]!!  // для удаления сообщения, если выполнение прервано командой /start
                        messageIdMap[stringChatId] = intMessageId
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
                    val sendMessage: SendMessage = botMenuFunction.receiveGoBackMenu(stringChatId, forMessageText)
                    execute(sendMessage)

                }

                inputRepeatText -> {
                    tempData[stringChatId] = ""
                    val  dataUnit: DataUnit = dataUnitMap[stringChatId]!!

                    val textForMessage: String = if (dataUnit.secondText.equals(updateMessageText, ignoreCase = true)){
                        properlyLessonAnswer[stringChatId] = properlyLessonAnswer[stringChatId]!! + 1
                        "ㅤㅤㅤㅤㅤㅤ✅  Правильно!ㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤ"
                    } else {
                        " ㅤㅤㅤㅤㅤㅤㅤ❌  Не верно ㅤㅤㅤㅤㅤㅤㅤ\nПравильно:\n\uD83D\uDD39 " + dataUnit.secondText + "\n\nВаш вариант: \n\uD83D\uDD38 " + updateMessageText
                    }
                    val editMessageText = botMenuFunction.receiveButtonEditMessage(stringChatId, dataUnit.id, textForMessage, "#tran", listOf("Далее"))
                    execute(editMessageText)
                    messageIdMap[stringChatId] = dataUnit.id - 1// удаление сообщения
                }

                inputUserFirstName -> {
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3, 4)
                    tempData[stringChatId] = ""

                    var checkAdmin = false
                    userDataRepository.findAll().forEach{ el -> if(el.userFirstname == "admin") checkAdmin = true }

                    val forMessageText =  if(updateMessageText.length > 15 || (updateMessageText == "admin" && checkAdmin)){
                        " ㅤㅤㅤㅤㅤㅤ Ошибка ввода ㅤㅤㅤㅤㅤㅤㅤㅤ"
                    } else {
                        val userData: UserData = userDataRepository.findById(longChatId).get()
                        userData.userFirstname = updateMessageText
                        userDataRepository.save(userData)
                        "✅  Имя пользователя было добавленоㅤㅤㅤㅤㅤㅤㅤㅤ"
                    }
                    val sendMessage = SendMessage(stringChatId, forMessageText)
                    execute(sendMessage)
                    messageIdMap[stringChatId] = intMessageId
                }

                inputMessageForAllUsers -> {
                deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3)
                tempData[stringChatId] = ""
                    if(updateMessageText.length > 3) {
                    userDataRepository.findAll().forEach{ usr -> execute(SendMessage(usr.chatId.toString(), updateMessageText)) } // TODO сделать защищ. execute
                    }
                }

                inputMessageForBillboard -> {
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3)
                    tempData[stringChatId] = ""
                    messageForBillboard = updateMessageText
                    val sendMessage = SendMessage(stringChatId, "Заглавие уроков изменено")
                    execute(sendMessage)
                }

                inputTopBillboardUrl -> {
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3)
                    tempData[stringChatId] = ""
                    val sendMessage: SendMessage
                    if(updateMessageText.contains("http")) {
                        urlForTopBillboard = updateMessageText
                        sendMessage = SendMessage(stringChatId, "Изображение верхнего баннера изменено")
                    } else {
                        sendMessage = SendMessage(stringChatId, "Ошибка ввода")
                    }
                    execute(sendMessage)
                }

                inputLowBillboardUrl -> {
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3)
                    tempData[stringChatId] = ""
                    val sendMessage: SendMessage
                    if(updateMessageText.contains("http")) {
                        urlForLowBillboard = updateMessageText
                        sendMessage = SendMessage(stringChatId, "Изображение нижнего баннера изменено")
                    } else {
                        sendMessage = SendMessage(stringChatId, "Ошибка ввода")
                    }
                    execute(sendMessage)
                }
            }


            when {
                updateMessageText == "/sta" -> { // TODO
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3, 4)
                    viewAsChat[stringChatId] = false

                    if (saveHintMessageId[stringChatId] != null) protectedExecute(DeleteMessage().putData(stringChatId, saveHintMessageId[stringChatId]!!))
                    if (saveStartMessageId[stringChatId] != null) protectedExecute(DeleteMessage().putData(stringChatId, saveStartMessageId[stringChatId]!!))
                    if (saveTrainingMessageId[stringChatId] != null) protectedExecute(DeleteMessage().putData(stringChatId, saveTrainingMessageId[stringChatId]!!))

                    val url = urlForTopBillboard.ifEmpty { "https://disk.yandex.ru/i/zuoXMRncWJOvNg" }
                    val sendPhoto = botMenuFunction.receiveBillboard(stringChatId, url)
                    saveStartMessageId[stringChatId] = execute(sendPhoto).messageId
                    messageIdMap[stringChatId] = intMessageId
                    saveHintMessageId[stringChatId] = 0
                }

                updateMessageText == "/start" -> {
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3, 4)
                    viewAsChat[stringChatId] = false

                    if (saveHintMessageId[stringChatId] != null) protectedExecute(DeleteMessage().putData(stringChatId, saveHintMessageId[stringChatId]!!))
                    if (saveStartMessageId[stringChatId] != null) protectedExecute(DeleteMessage().putData(stringChatId, saveStartMessageId[stringChatId]!!))
                    if (saveTrainingMessageId[stringChatId] != null) protectedExecute(DeleteMessage().putData(stringChatId, saveTrainingMessageId[stringChatId]!!))

                    val url = urlForTopBillboard.ifEmpty { "https://disk.yandex.ru/i/zuoXMRncWJOvNg" }
                    val sendPhoto = botMenuFunction.receiveBillboard(stringChatId, url)
                    saveStartMessageId[stringChatId] = execute(sendPhoto).messageId
                    messageIdMap[stringChatId] = intMessageId
                    saveHintMessageId[stringChatId] = 0
                }

                updateMessageText == "/mydata" -> {
                    val user: Optional<UserData> = userDataRepository.findById(longChatId)
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3, 4)
                    val text =  if(user.isPresent) "\nusername пользователя:  " + user.get().username + "\nchat id:  " +  user.get().chatId + "\nдата и время регистрации:  " + user.get().userRegisterDateTime + "\n"  + user.get().userFirstname else "Пользователь не зарегистрирован"
                    execute(SendMessage(stringChatId, text))
                    messageIdMap[stringChatId] = intMessageId
                }

                updateMessageText == "/deletedata" -> {
                    val user: Optional<UserData> = userDataRepository.findById(longChatId)

                    if(user.isEmpty || user.get().isShowHint){
                        if(saveStartMessageId[stringChatId] != null && messageIdMap[stringChatId] == saveStartMessageId[stringChatId]!!){
                            deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 3)
                        } else {
                            deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3) // TODO
                        }
                    } else {
                        if(saveStartMessageId[stringChatId] != null && messageIdMap[stringChatId] == saveStartMessageId[stringChatId]!!){
                            deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1)
                        } else {
                            deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2)
                        }
                    }
                    val text =  if(user.isPresent){
                        userDataRepository.deleteById(longChatId)
                        "Данные пользователя были удалены"
                    } else "Пользователь не зарегистрирован"
                    execute(SendMessage(stringChatId, text))
                    messageIdMap[stringChatId] = intMessageId
                }

                updateMessageText == "/help" -> {
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3, 4)
                    val text = "Полезная информация"
                    execute(SendMessage(stringChatId, text))
                    messageIdMap[stringChatId] = intMessageId
                }

                updateMessageText == "/info" -> {
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3, 4)
                    val user: Optional<UserData> = userDataRepository.findById(longChatId)
                    if(user.isPresent && user.get().chatId < 100) {
                    execute(SendMessage(stringChatId, "Информация для администратора"))
                    messageIdMap[stringChatId] = intMessageId
                    } else {
                        deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0)
                    }
                }

                updateMessageText == "/register" -> {
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3)
                    val user: Optional<UserData> = userDataRepository.findById(longChatId)
                    val text =  if(user.isPresent && user.get().userFirstname.isEmpty()) {
                        tempData[stringChatId] = inputUserFirstName
                        "Введите ваше имя в поле ввода и отправьте сообщение"
                    } else "Ошибка. Имя пользователя уже задано, или пользователь не зарегистрирован"
                    execute(SendMessage(stringChatId, text))
                    messageIdMap[stringChatId] = intMessageId
                }

                updateMessageText == "\uD83D\uDCDA Уроки" -> {
                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3, 4)
                    if (saveTrainingMessageId[stringChatId] != null) protectedExecute(DeleteMessage().putData(stringChatId, saveTrainingMessageId[stringChatId]!!))

                        val data: Optional<UserData> = userDataRepository.findById(longChatId)
                        val userData: UserData

                        if (data.isEmpty) {
                            userData = UserData()
                            userData.chatId = longChatId
                            userData.username = update.message.chat.userName
                            userDataRepository.save(userData)
                        } else {
                            userData = data.get()
                        }

                        viewAsChat[stringChatId] = userData.isViewAsChat

                    val url: String = urlForLowBillboard.ifEmpty { "https://disk.yandex.ru/i/co4spGtnnJ1HbA" }

                    if ((saveHintMessageId[stringChatId] == null || saveHintMessageId[stringChatId] == 0) && userData.isShowHint){
                        val hintMessage = SendPhoto()
                        hintMessage.putData(stringChatId, url)
                        saveHintMessageId[stringChatId] = execute(hintMessage).messageId
                    } else {
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        editMessageMedia.messageId = saveHintMessageId[stringChatId]
                        editMessageMedia.media = InputMediaPhoto(url)
                        try {
                            execute(editMessageMedia)
                        } catch (e: TelegramApiException){
                            println("ERR Уроки -> " + e.message)
                        }
                    }

                        userLessonText[stringChatId] = ""
                        properlyLessonAnswer[stringChatId] = 0
                        if (lessonsMap[stringChatId] != null) lessonsMap[stringChatId]!!.clear()


                        val forAdminMenu = listOf("\uD83D\uDCCA Раздел администратора")
                        val categoryTitles = listOf("\uD83D\uDCD2 Уроки других пользователей", "\uD83D\uDCD6 Добавить/удалить свои тексты")
                        val collectCategoryTitles = mutableListOf<String>()
                        if (userData.userFirstname == "admin") collectCategoryTitles.addAll(forAdminMenu)
                        collectCategoryTitles.addAll(categoryTitles)
                        collectCategoryTitles.addAll(Lessons.PRESENT_SIMPLE.getLessonsTitles())
                        val text = "ㅤㅤㅤㅤㅤㅤㅤ\uD835\uDC0B\uD835\uDC04\uD835\uDC12\uD835\uDC12\uD835\uDC0E\uD835\uDC0D\uD835\uDC12ㅤㅤㅤㅤㅤㅤㅤㅤ"

                        val textForMessage = if (messageForBillboard.length > 1) {
                            messageForBillboard + "\n\n" + text
                        } else {
                            text
                        }

                    val sendMessage = botMenuFunction.categoryMenu(longChatId, textForMessage, collectCategoryTitles)

                    execute(sendMessage)
                    messageIdMap[stringChatId] = intMessageId
                }

                updateMessageText == "Настройки ⚙" -> {

                    deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0, 1, 2, 3, 4)
                    if (saveTrainingMessageId[stringChatId] != null) protectedExecute(DeleteMessage().putData(stringChatId, saveTrainingMessageId[stringChatId]!!))

                    val data: Optional<UserData> = userDataRepository.findById(longChatId)
                    val userData: UserData

                    if (data.isEmpty) {
                        userData = UserData()
                        userData.chatId = longChatId
                        userData.username = update.message.chat.userName
                        userDataRepository.save(userData)
                    } else {
                        userData = data.get()
                    }

                    viewAsChat[stringChatId] = userData.isViewAsChat

                    val url: String = urlForLowBillboard.ifEmpty { "https://disk.yandex.ru/i/va9-A6PzWOJ0qA" }

                    if ((saveHintMessageId[stringChatId] == null || saveHintMessageId[stringChatId] == 0) && userData.isShowHint){
                        val hintMessage = SendPhoto()
                        hintMessage.putData(stringChatId, url)
                        saveHintMessageId[stringChatId] = execute(hintMessage).messageId
                    } else {
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        editMessageMedia.messageId = saveHintMessageId[stringChatId]
                        editMessageMedia.media = InputMediaPhoto(url)
                        try {
                            execute(editMessageMedia)
                        } catch (e: TelegramApiException){
                            println("ERR Уроки -> " + e.message)
                        }
                    }

                    userLessonText[stringChatId] = ""
                    properlyLessonAnswer[stringChatId] = 0
                    if (lessonsMap[stringChatId] != null) lessonsMap[stringChatId]!!.clear()

                    val sendMessage: SendMessage = botMenuFunction.receiveSettingMenu(stringChatId, "ㅤㅤㅤㅤㅤㅤㅤНастройкиㅤㅤㅤㅤㅤㅤ", userData.isViewAsChat, userData.isUsersTexts, userData.isShowHint, userData.isSendTrainingMessage, userData.sinceTime, userData.untilTime)
                    execute(sendMessage)
                    messageIdMap[stringChatId] = intMessageId
                }

                else -> {
                    protectedExecute(DeleteMessage().putData(stringChatId, intMessageId))
                }
            }
            deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0)
// TODO >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


        } else if (update.hasCallbackQuery()) {
            val intMessageId: Int = update.callbackQuery.message.messageId
            val stringChatId: String = update.callbackQuery.message.chatId.toString()
            val longChatId: Long = update.callbackQuery.message.chatId
            val callBackData: String = update.callbackQuery.data

            when {
                callBackData == "\uD83D\uDCDA Учить новые слова" -> { // TODO DELETE
                    if(userDataRepository.findById(longChatId).get().isShowHint){
                        deletePreviousMessage(stringChatId, hintMessageIdMap[stringChatId], 1) // удаление окна подсказок
                    }

                    cashLessonCategory[stringChatId] = "\uD83D\uDCDA Учить новые слова"
                    val editMessageText: EditMessageText = botMenuFunction.receiveLearnWordMenu(stringChatId, intMessageId, "❗️ Для того чтобы начать тренировку, сначала необходимо добавить слова для изучения. Добавить или удалить слова и тексты вы можете с помощью меню")
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

                callBackData == "\uD83D\uDCCA Раздел администратора" -> {
                    val buttonTexts = listOf("Статистика и информация", "Сообщение в чат",
                        "Текст заглавия уроков", "Изображение верхнего банера", "Изображение нижнего банера", "Очистка чата перед рестартом", "Очистка чата после рестарта")
                    val editMessageText = botMenuFunction.receiveInfoMenuForAdmin(stringChatId, intMessageId, "Меню администратора", buttonTexts)
                    execute(editMessageText)
                }

                callBackData == "Сообщение в чат" -> {
                    val editMessageText = EditMessageText()
                    editMessageText.text = "Сообщение для всех пользователей. Введите текст сообщения, затем отправьте его"
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    execute(editMessageText)
                    tempData[stringChatId] = inputMessageForAllUsers
                }

                callBackData == "Текст заглавия уроков" -> {
                    val editMessageText = EditMessageText()
                    editMessageText.text = "Сообщение заглавия уроков. Введите текст сообщения, затем отправьте его"
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    execute(editMessageText)
                    tempData[stringChatId] = inputMessageForBillboard
                }

                callBackData == "Статистика и информация" -> {
                    val usersCount: Int = userDataRepository.findAll().count()
                    val messageText = "Время работы сервера: $localDateTime\nКэшировано уроков: ${lessonsMap.size}\nВсего пользователей: $usersCount"
                    val editMessageText = EditMessageText()
                    editMessageText.text = messageText
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    execute(editMessageText)
                }

                callBackData == "Изображение верхнего банера" -> {
                    val editMessageText = EditMessageText()
                    editMessageText.text = "Замена изображения верхнего банера. Введите текст url адреса, затем отправьте его"
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    execute(editMessageText)
                    tempData[stringChatId] = inputTopBillboardUrl
                }

                callBackData == "Изображение нижнего банера" -> {
                    val editMessageText = EditMessageText()
                    editMessageText.text = "Замена изображения нижнего банера. Введите текст url адреса, затем отправьте его"
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    execute(editMessageText)
                    tempData[stringChatId] = inputLowBillboardUrl
                }

                callBackData == "Очистка чата перед рестартом" -> {
                    cleanMessageHistory(-1, 0, 1, 2, 3, 4)
                    val editMessageText = EditMessageText()
                    editMessageText.text = "Процесс очистки чата запущен..."
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId
                    execute(editMessageText)
                    tempData[stringChatId] = inputLowBillboardUrl
                }

                    callBackData == "Очистка чата после рестарта" -> {
                cleanMessageHistory(-15,-14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2) // TODO range {-15 to 2}
                val editMessageText = EditMessageText()
                editMessageText.text = "Процесс очистки чата запущен..."
                editMessageText.chatId = stringChatId
                editMessageText.messageId = intMessageId
                execute(editMessageText)
                tempData[stringChatId] = inputLowBillboardUrl
            }

                 callBackData == "#addw" -> {
                val editMessageText = EditMessageText()
                     editMessageText.text = "Введите слово на английском языке и отправьте сообщение"
                execute(editMessageText)
                }

                callBackData.contains("#tran") -> { // TODO ограничение в 100 текстов #train
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
                         if (properlyLessonAnswer[stringChatId] == null) properlyLessonAnswer[stringChatId] = 0
                         editMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step2")
                         //editMessageText.text = "Выберите перевод для слова:  ㅤㅤㅤㅤㅤㅤㅤ\n\uD83D\uDD39 " + lessonUnitMap[stringChatId]!!.russianText + "  ㅤㅤㅤㅤㅤㅤㅤ\n\n✏ "
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
                        editMessageText  = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step1")
                    }
                    } else {
                        lessonUnitMap[stringChatId] = createLessonUnit(stringChatId,  userLessonText[stringChatId]!!, "#aul", randomWords)
                        editMessageText  = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step1")
                    }
                    execute(editMessageText)
                }

                callBackData.contains("#own") -> {
                    tempData[stringChatId] = "" // сброс временных данных TODO
                    val categoryText = callBackData.replace("#own", "")
                    cashLessonCategory[stringChatId] = categoryText.ifEmpty { cashLessonCategory[stringChatId]!! } // кэширование категории
                    val editMessageText: EditMessageText = botMenuFunction.receiveChoseMenu(stringChatId, intMessageId, cashLessonCategory[stringChatId]!!, "\uD83D\uDD39 Категория:   " + cashLessonCategory[stringChatId]!! + "\n\n" +
                            "   ㅤㅤㅤㅤЗдесь вы можете:ㅤㅤㅤㅤ  ㅤㅤㅤㅤ  ㅤㅤㅤㅤㅤㅤ")
                    execute(editMessageText)

                    messageIdMap[stringChatId] = intMessageId
                }

                callBackData.contains("#hint") -> {
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    if (userData.isShowHint) {
                        userData.isShowHint = false
                        userDataRepository.save(userData)
                        protectedExecute(DeleteMessage().putData(stringChatId, saveHintMessageId[stringChatId]!!))
                        saveHintMessageId[stringChatId] = 0
                    } else {
                        userData.isShowHint = true
                        userDataRepository.save(userData)
                    }
                    val editMessageText = botMenuFunction.receiveSettingMenu(stringChatId, intMessageId, "ㅤㅤㅤㅤㅤㅤㅤНастройкиㅤㅤㅤㅤㅤㅤ", userData.isViewAsChat, userData.isUsersTexts, userData.isShowHint, userData.isSendTrainingMessage, userData.sinceTime, userData.untilTime)
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
                    messageIdMap[stringChatId] = intMessageId
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

                callBackData.contains("#usrtxt") -> {
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    if (userData.isUsersTexts) {
                        userData.isUsersTexts = false
                        userDataRepository.save(userData)
                    } else {
                        userData.isUsersTexts = true
                        userDataRepository.save(userData)
                    }
                    val editMessageText = botMenuFunction.receiveSettingMenu(stringChatId, intMessageId, "ㅤㅤㅤㅤㅤㅤㅤНастройкиㅤㅤㅤㅤㅤㅤ", userData.isViewAsChat, userData.isUsersTexts, userData.isShowHint, userData.isSendTrainingMessage, userData.sinceTime, userData.untilTime)
                    execute(editMessageText)
                }

                callBackData.contains("#aschat") -> {
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    if (userData.isViewAsChat) {
                        userData.isViewAsChat = false
                        viewAsChat[stringChatId] = false
                        userDataRepository.save(userData)
                    } else {
                        viewAsChat[stringChatId] = true
                        userData.isViewAsChat = true
                        userDataRepository.save(userData)
                    }

                    val editMessageText = botMenuFunction.receiveSettingMenu(stringChatId, intMessageId, "ㅤㅤㅤㅤㅤㅤㅤНастройкиㅤㅤㅤㅤㅤㅤ", userData.isViewAsChat, userData.isUsersTexts, userData.isShowHint, userData.isSendTrainingMessage, userData.sinceTime, userData.untilTime)
                    execute(editMessageText)
                }

                callBackData.contains("#trmes") -> {
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    if (userData.isSendTrainingMessage) {
                        userData.isSendTrainingMessage = false
                        userDataRepository.save(userData)
                    } else {
                        userData.isSendTrainingMessage = true
                        userDataRepository.save(userData)
                    }

                    val editMessageText = botMenuFunction.receiveSettingMenu(stringChatId, intMessageId, "ㅤㅤㅤㅤㅤㅤㅤНастройкиㅤㅤㅤㅤㅤㅤ", userData.isViewAsChat, userData.isUsersTexts, userData.isShowHint, userData.isSendTrainingMessage, userData.sinceTime, userData.untilTime)
                    execute(editMessageText)
                }

                callBackData.contains("#time") -> {
                    val timeData: String = callBackData.replace("#time", "")
                    val userData: UserData = userDataRepository.findById(longChatId).get()
                    val currentSinceTime: Int = userData.sinceTime
                    val currentUntilTime: Int = userData.untilTime

                    when (timeData) {
                        "SinceDown" -> if(currentSinceTime > 0) userData.sinceTime = currentSinceTime - 1
                        "SinceUp" -> if(currentSinceTime < 23 && currentSinceTime < currentUntilTime - 1) userData.sinceTime = currentSinceTime + 1
                        "UntilDown" -> if(currentUntilTime > 1 && currentUntilTime > currentSinceTime + 1) userData.untilTime = currentUntilTime - 1
                        "UntilUp" -> if(currentUntilTime < 24) userData.untilTime = currentUntilTime + 1
                    }
                    userDataRepository.save(userData)

                    val editMessageText = botMenuFunction.receiveSettingMenu(stringChatId, intMessageId, "ㅤㅤㅤㅤㅤㅤㅤНастройкиㅤㅤㅤㅤㅤㅤ", userData.isViewAsChat, userData.isUsersTexts, userData.isShowHint, userData.isSendTrainingMessage, userData.sinceTime, userData.untilTime)
                    try {
                        execute(editMessageText)
                    } catch (e: TelegramApiException){
                        println("callBackData.contains(#time) = $e") // TODO
                    }
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

                    val newLessonText: String = if (indexFromList >= 0){
                        val lessonTexts: MutableList<String> = deleteInDbText[stringChatId]!! as MutableList
                        if (lessonTexts.size == 1){
                            "" // return
                        } else {
                            lessonTexts.removeAt(indexFromList)
                            lessonTexts.toString().replace("[", "").replace("]", "").replace(", ", "#") // return
                        }
                    } else {
                        "" // return
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

                callBackData.contains(Lessons.PRESENT_SIMPLE.title) -> {
                    val user: UserData = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.presentSimple
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.PRESENT_SIMPLE)
                   val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step1")
                   execute(editMessageText)

                   if(user.isShowHint){
                       val editMessageMedia = EditMessageMedia() //TODO
                       editMessageMedia.chatId = stringChatId
                       editMessageMedia.messageId = saveHintMessageId[stringChatId]
                       editMessageMedia.media = InputMediaPhoto("https://disk.yandex.ru/i/SbIfecNY8lq1aA")
                       try {
                           execute(editMessageMedia)
                       } catch (e: TelegramApiException){
                           println("hintMessageIdMap[stringChatId] = " + hintMessageIdMap[stringChatId] + " >> ERR Lessons.PRESENT_SIMPLE.title -> " + e.message)
                       }
                   }
                }

                callBackData.contains(Lessons.PRESENT_CONTINUOUS.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.presentContinuous
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.PRESENT_CONTINUOUS)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        editMessageMedia.messageId = saveHintMessageId[stringChatId]
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
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]
                        editMessageMedia.messageId = saveHintMessageId[stringChatId]
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
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]
                        editMessageMedia.messageId = saveHintMessageId[stringChatId]
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
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]
                        editMessageMedia.messageId = saveHintMessageId[stringChatId]
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
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]
                        editMessageMedia.messageId = saveHintMessageId[stringChatId]
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
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]
                        editMessageMedia.messageId = saveHintMessageId[stringChatId]
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
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]
                        editMessageMedia.messageId = saveHintMessageId[stringChatId]
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
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step1")
                    execute(editMessageText)

                    if(user.isShowHint){
                        val editMessageMedia = EditMessageMedia() //TODO
                        editMessageMedia.chatId = stringChatId
                        val test = hintMessageIdMap[stringChatId]
                        editMessageMedia.messageId = saveHintMessageId[stringChatId]
                        editMessageMedia.media = InputMediaPhoto("https://disk.yandex.ru/i/SbIfecNY8lq1aA")
                        try {
                            execute(editMessageMedia)
                        } catch (e: TelegramApiException){

                        }
                    }
                }

                callBackData.contains(Lessons.LEARN_WORDS.title) -> {
                    if(userDataRepository.findById(longChatId).get().isShowHint){
                       deletePreviousMessage(stringChatId, hintMessageIdMap[stringChatId], 1) // удаление окна подсказок
                    }

                    cashLessonCategory[stringChatId] = "\uD83D\uDCDA Учить новые слова"
                    val editMessageText: EditMessageText = botMenuFunction.receiveLearnWordMenu(stringChatId, intMessageId, "❗️ Для того чтобы начать тренировку, сначала необходимо добавить слова для изучения. Добавить или удалить слова и тексты вы можете с помощью соответствующих кнопок меню")
                    execute(editMessageText)
                }

                callBackData.contains("ㅤ") -> { // callBackData содержит пустой char (не space(!) - нажатие кнопки экранной клавиатуры без текста)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step2")
                    execute(editMessageText)
                }

                callBackData.contains("step1") -> {
                    val word: String = callBackData.replace("step1", "")
                    lessonUnitMap[stringChatId]!!.inputText += word

                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, "step2")
                    execute(editMessageText)
                }

                callBackData.contains("step2") -> {
                    val word: String = callBackData.replace("step2", "")
                    lessonUnitMap[stringChatId]!!.inputText += word

                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!,"step1")
                    execute(editMessageText)
                }

                callBackData == "delword" -> {
                    val splitInputText = lessonUnitMap[stringChatId]!!.inputText.split(" ")

                    if (lessonUnitMap[stringChatId]!!.inputText.isNotEmpty()) lessonUnitMap[stringChatId]!!.inputText =
                        lessonUnitMap[stringChatId]!!.inputText.replace(" " + splitInputText[splitInputText.size - 1], "")

                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!,"step2")
                    try {
                        execute(editMessageText)
                    } catch (e: TelegramApiException){

                    }
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
                        properlyLessonAnswer[stringChatId] = properlyLessonAnswer[stringChatId]!! + 1
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

        val lessonMapSize: Int = lessonsMap[chatId]!!.size
        var chooseRandom: Int = (0 until lessonMapSize).random() // выбор случайного текста с уроком из коллекции в Map
        val chooseRandomOne: Int = (0 until lessonMapSize).random() // выбор случайного текста с уроком из коллекции в Map
        val chooseRandomTwo: Int = (0 until lessonMapSize).random() // выбор случайного текста с уроком из коллекции в Map

        var splitText: List<String> = lessonsMap[chatId]!![chooseRandom].split("*") // разделение ру. текста и eng текста
        val splitTextOne: List<String> = lessonsMap[chatId]!![chooseRandomOne].split("*")
        val splitTextTwo: List<String> = lessonsMap[chatId]!![chooseRandomTwo].split("*")

        if (lessonUnitMap[chatId] != null && lessonUnitMap[chatId]!!.russianText == splitText[0]){ // lessonUnit без дублей
            chooseRandom = if (chooseRandom + 1 < lessonsMap[chatId]!!.size - 1) chooseRandom + 1 else chooseRandom
            splitText = lessonsMap[chatId]!![chooseRandom].split("*")
        }

        val forButtonText = mutableListOf<String>()
        val englishText: MutableList<String> = splitText[1].split(" ") as MutableList<String>
        val englishAdditionText = mutableListOf<String>()
        englishAdditionText.addAll(splitTextOne[1].split(" "))
        englishAdditionText.addAll(splitTextTwo[1].split(" "))
        forButtonText.addAll(englishText)
        if(forButtonText.size + englishAdditionText.size < 10)  forButtonText.addAll(englishAdditionText)

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


    @Scheduled(cron = "0 0 * * * *") // @Scheduled(cron = "0 0 * * * *")
    fun sendLesson() {
        val datetimeFormatter = DateTimeFormatter.ofPattern("H")
        val currentHour: Int = datetimeFormatter.format(localDateTime).toInt()

        for(data in userDataRepository.findAll()){
            if (saveTrainingMessageId[data.chatId.toString()] != null) protectedExecute(DeleteMessage().putData(data.chatId.toString(), saveTrainingMessageId[data.chatId.toString()]!!))
            val random: Int = (0 until 2).random()

            if(data.wordsForLearning.isNotEmpty() && data.isSendTrainingMessage && currentHour in data.sinceTime until data.untilTime && random == 1){ //
            if (messageIdMap[data.chatId.toString()] != null) deletePreviousMessage(data.chatId.toString(), messageIdMap[data.chatId.toString()], 0, 1, 2, 3)

            val sendMessage = SendMessage()
            sendMessage.setChatId(data.chatId)
            sendMessage.text = "Потренируем слова?"

            val inlineKeyboardMarkup = InlineKeyboardMarkup()
            val rowsInline = ArrayList<List<InlineKeyboardButton>>()
            val firstRowInlineButton = ArrayList<InlineKeyboardButton>()

            val trainingButton = InlineKeyboardButton()
            trainingButton.putData("Да", "#tran")
            firstRowInlineButton.add(trainingButton)

            val myWordsButton = InlineKeyboardButton()
            myWordsButton.putData("Не сейчас", "#cancel")
            firstRowInlineButton.add(myWordsButton)
            rowsInline.add(firstRowInlineButton)
            inlineKeyboardMarkup.keyboard = rowsInline
            sendMessage.replyMarkup = inlineKeyboardMarkup
            saveTrainingMessageId[data.chatId.toString()] = execute(sendMessage).messageId
            }
            println("fun sendLesson() TEST")
        }
    }

/* */
    @Scheduled(cron = "0 0 0 * * *")
    fun everydayReload() {
    cleanMessageHistory(-1, 0, 1, 2, 3, 4)
    }


    fun cleanMessageHistory(vararg messagesId: Int) {
        for(data in userDataRepository.findAll()){
            if (saveStartMessageId[data.chatId.toString()] != null) protectedExecute(DeleteMessage().putData(data.chatId.toString(), saveStartMessageId[data.chatId.toString()]!!))
            if (hintMessageIdMap[data.chatId.toString()] != null) protectedExecute(DeleteMessage().putData(data.chatId.toString(), hintMessageIdMap[data.chatId.toString()]!!))
            if (saveHintMessageId[data.chatId.toString()] != null) protectedExecute(DeleteMessage().putData(data.chatId.toString(), saveHintMessageId[data.chatId.toString()]!!))
            if (saveTrainingMessageId[data.chatId.toString()] != null) protectedExecute(DeleteMessage().putData(data.chatId.toString(), saveTrainingMessageId[data.chatId.toString()]!!))
            if (messageIdMap[data.chatId.toString()] != null) deletePreviousMessage(data.chatId.toString(), messageIdMap[data.chatId.toString()],  *messagesId)
        }

        lessonsMap.clear()      // HashMap<String, MutableList<String>>()
        deleteInDbText.clear()      // HashMap<String, List<String>>()lessonUnitMap = HashMap<String, LessonUnit>()
        dataUnitMap.clear()      // HashMap<String, DataUnit>()
        userLessonText.clear()      // HashMap<String, String>()
        cashLessonCategory.clear()      // HashMap<String, String>()
        tempData.clear()      // HashMap<String, String>()
        messageIdMap.clear()    // HashMap<String, Int>()  // Id сообщения для удаления
        properlyLessonAnswer.clear()      // HashMap<String, Int>()
        viewAsChat.clear()     // HashMap<String, Boolean>()

        saveStartMessageId.clear()      // HashMap<String, Int>()
        hintMessageIdMap.clear()
        saveHintMessageId.clear()     // HashMap<String, Int>()
        saveTrainingMessageId.clear()       // HashMap<String, Int>()
    }





}
