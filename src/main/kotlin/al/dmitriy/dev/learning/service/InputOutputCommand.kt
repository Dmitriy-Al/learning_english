package al.dmitriy.dev.learning.service

import al.dmitriy.dev.learning.apptexts.*
import al.dmitriy.dev.learning.config.Config
import al.dmitriy.dev.learning.service.dataunit.DataUnit
import al.dmitriy.dev.learning.extendfunctions.putData
import al.dmitriy.dev.learning.service.dataunit.LessonUnit
import al.dmitriy.dev.learning.extendfunctions.protectedExecute
import al.dmitriy.dev.learning.lesson.Lessons
import al.dmitriy.dev.learning.model.UserData
import al.dmitriy.dev.learning.model.UserDataDao
import org.slf4j.LoggerFactory
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

val config: Config = Config()

@Component
class InputOutputCommand(@Autowired val userDataRepository: UserDataDao) :
    TelegramLongPollingBot(config.botToken) {

    init { // Команды меню бота
        val botCommandList: List<BotCommand> = listOf(
            BotCommand("/start", "Запуск бота"),
            BotCommand("/help", "Полезная информация"),
            BotCommand("/mydata", "Данные пользователя"),
            BotCommand("/deletedata", "Удаление всех данных пользователя"))
        try {
            this.execute(SetMyCommands(botCommandList, BotCommandScopeDefault(), null))
        } catch (e: TelegramApiException) {
            val logger = LoggerFactory.getLogger("InputOutputCommand <botCommandList>")
            logger.error(e.message)
        }
    }


    private var urlForTopBillboard = "" // линк для замены верхнего изображения (шапки)
    private var messageForBillboard = "" // сообщение для верхнего изображения (шапки)
    private var urlForBottomBillboard = "" // линк для замены изображения с таблицами/подсказками
    private val localDateTime = LocalDateTime.now()

    // Ключами к Map являются ChatId пользователей, т.о. каждый из пользователей имеет доступ к индивидуальным уроками/ресурсами
    private val tempData = HashMap<String, String>() // если в Map добавляется строка-константа, Update-сообщение запускает определённую функцию
    private val dataUnitMap = HashMap<String, DataUnit>() // в Map помещается объект с тренировкой
    private val userLessonText = HashMap<String, String>() // в Map кэшируется текст уроков из бд
    private val saveHintMessageId = HashMap<String, Int>() // в Map помещается id сообщения с таблицами/подсказками
    private val saveStartMessageId = HashMap<String, Int>()  // Id /start - сообщения для удаления
    private val saveAddWordMessageId = HashMap<String, Int>() //  Id сообщения при добавлении слов на изучение
    private val lessonUnitMap = HashMap<String, LessonUnit>() // в Map помещается объект с уроком
    private val properlyLessonAnswer = HashMap<String, Int>() // счет правильных ответов
    private val cashLessonCategory = HashMap<String, String>() // в Map сохраняется выбранная категория уроков
    private val saveTrainingMessageId = HashMap<String, Int>() // раз в интервал времени бот отправляет сообщение с тренировкой, в Map сохраняется messageId сообщений
    private val deleteInDbText = HashMap<String, List<String>>() // текст для замещения исходного текста в бд
    private val lessonsMap = HashMap<String, MutableList<String>>() // в Map сохраняется одно конкретное предложение для LessonUnit

    private val botMenuFunction: BotMenuFunction = BotMenuFunction()

    // Строки-константы для добавления в tempData[stringChatId]
    private final val inputRuText = "INPUT_RU_TEXT"
    private final val inputEnText = "INPUT_EN_TEXT"
    private final val inputTrainingText = "INPUT_REPEAT_TEXT"
    private final val inputTopBillboardUrl = "TOP_BILLBOARD_URL"
    private final val inputUserFirstName = "SAVE_USER_FIRST_NAME"
    private final val inputBottomBillboardUrl = "LOW_BILLBOARD_URL"
    private final val inputMessageForBillboard = "MESSAGE_FOR_BILLBOARD"
    private final val inputMessageForAllUsers = "MESSAGE_FOR_ALL_USERS"


    override fun getBotUsername(): String {
        return config.botUsername
    }


    // Бот получил команду (сообщение от пользователя)
    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val longChatId: Long = update.message.chatId
            val intMessageId: Int = update.message.messageId
            val updateMessageText: String = update.message.text
            val stringChatId: String = longChatId.toString()

            when (tempData[stringChatId]) { // если в Map добавляется строка-константа, Update-сообщение (updateMessageText) запускает одну из функций в блоке
                inputEnText -> inputEnTextExecute(stringChatId, updateMessageText)
                inputRuText -> inputRuTextExecute(stringChatId, longChatId, updateMessageText)
                inputTrainingText -> inputTrainingTextExecute(stringChatId, updateMessageText)
                inputTopBillboardUrl -> inputTopBillboardUrlExecute(stringChatId, updateMessageText)
                inputMessageForAllUsers -> inputMessageForUsersExecute(stringChatId, updateMessageText)
                inputUserFirstName -> inputFirstNameExecute(stringChatId, longChatId, updateMessageText)
                inputBottomBillboardUrl -> inputBottomBillboardUrlExecute(stringChatId, updateMessageText)
                inputMessageForBillboard -> inputMessageForBillboardExecute(stringChatId, updateMessageText)
            }

            when (updateMessageText) { // команды

                "/start" -> { // начало работы бота
                    if (saveHintMessageId[stringChatId] != null) protectedExecute(DeleteMessage().putData(stringChatId, saveHintMessageId[stringChatId]!!))
                    if (saveStartMessageId[stringChatId] != null) protectedExecute(DeleteMessage().putData(stringChatId, saveStartMessageId[stringChatId]!!))
                    if (saveTrainingMessageId[stringChatId] != null) protectedExecute(DeleteMessage().putData(stringChatId,saveTrainingMessageId[stringChatId]!!))

                    val url: String = urlForTopBillboard.ifEmpty { config.topBillboardUrl }
                    val sendPhoto: SendPhoto = botMenuFunction.receiveBillboard(stringChatId, url)
                    saveStartMessageId[stringChatId] = protectedExecute(sendPhoto)
                    saveHintMessageId[stringChatId] = 0
                }

                "/mydata" -> { // данные пользователя
                    val user: Optional<UserData> = userDataRepository.findById(longChatId)
                    val forMessageText: String = if (user.isPresent) "\nusername пользователя:  " + user.get().username +
                                "\nchat id:  " + user.get().chatId + "\nдата и время регистрации:  " +
                            user.get().userRegisterDateTime + "\n" + user.get().userFirstname else textForUnregisterUser
                    protectedExecute(SendMessage(stringChatId, forMessageText))
                }

                "/deletedata" -> { // удалить данные пользователя
                    val sendMessage = SendMessage(stringChatId, textForDelUserdata)
                    sendMessage.replyMarkup = botMenuFunction.receiveTwoButtonsMenu(
                        "Да", callData_delData,
                        "Отмена", callData_cancel
                    )
                    protectedExecute(sendMessage)
                }

                "/help" -> { // справка
                    val forMessageText: String = textForHelp
                    protectedExecute(SendMessage(stringChatId, forMessageText))
                }

                "/register" -> { // зарегистрироваться под именем
                    val user: Optional<UserData> = userDataRepository.findById(longChatId)
                    val forMessageText: String = if (user.isPresent && user.get().userFirstname.isEmpty()) {
                        tempData[stringChatId] = inputUserFirstName
                        textForNameInput // return
                    } else textForErrUser // return
                    protectedExecute(SendMessage(stringChatId, forMessageText))
                }

                "\uD83D\uDCDA Уроки" -> { // список уроков
                    if (saveTrainingMessageId[stringChatId] != null) {
                        protectedExecute(DeleteMessage().putData(stringChatId,saveTrainingMessageId[stringChatId]!!))
                    }

                    val username: String = update.message.chat.userName ?: "unnamed"
                    val userData: UserData = setUserDataInDB(longChatId, username)
                    val url: String = urlForBottomBillboard.ifEmpty { config.bottomBillboardUrl }
                    val hintMessage: SendPhoto = SendPhoto().putData(stringChatId, url)
                    saveHintMessageId[stringChatId] = protectedExecute(hintMessage)

                    clearCashedResources(stringChatId)

                    val collectCategoryTitles = mutableListOf<String>()
                    if (userData.userFirstname == "admin") collectCategoryTitles.addAll(listOf("\uD83D\uDCCA Раздел администратора"))
                    val categoryTitles =
                        listOf("\uD83D\uDCD2 Уроки других пользователей", "\uD83D\uDCD6 Добавить/удалить свои тексты")
                    collectCategoryTitles.addAll(categoryTitles)
                    collectCategoryTitles.addAll(Lessons.PRESENT_SIMPLE.getLessonsTitles())


                    val textForMessage: String = if (messageForBillboard.length > 1) messageForBillboard + "\n\n" + textForGreet else textForGreet
                    val sendMessage: SendMessage = botMenuFunction.categoryMenu(stringChatId, textForMessage, collectCategoryTitles)

                    protectedExecute(sendMessage)
                }

                "Настройки ⚙" -> {
                    if (saveTrainingMessageId[stringChatId] != null) {
                        protectedExecute(DeleteMessage().putData(stringChatId,saveTrainingMessageId[stringChatId]!!))
                    }

                    val username: String = update.message.chat.userName ?: "unnamed"
                    val userData: UserData = setUserDataInDB(longChatId, username)
                    val url: String = urlForBottomBillboard.ifEmpty { config.bottomBillboardUrl }
                    val hintMessage: SendPhoto = SendPhoto().putData(stringChatId, url)
                    saveHintMessageId[stringChatId] = protectedExecute(hintMessage)

                    clearCashedResources(stringChatId)
                    val sendMessage: SendMessage = botMenuFunction.receiveSettingMenu(
                        stringChatId,
                        textForSettings, userData.isUsersTexts, userData.isShowHint,
                        userData.isSendTrainingMessage, userData.sinceTime, userData.untilTime
                    )
                    protectedExecute(sendMessage)
                }

                else -> {
                    protectedExecute(DeleteMessage().putData(stringChatId, intMessageId))
                }
            }
            //  deletePreviousMessage(stringChatId, messageIdMap[stringChatId], 0)

        // Бот получил ответ от выбранной клавиши (экранной кнопки)
        } else if (update.hasCallbackQuery()) {
            val intMessageId: Int = update.callbackQuery.message.messageId
            val stringChatId: String = update.callbackQuery.message.chatId.toString()
            val longChatId: Long = update.callbackQuery.message.chatId
            val callBackData: String = update.callbackQuery.data

            when {
                callBackData == "\uD83D\uDCD6 Добавить/удалить свои тексты" -> {
                    val editMessageText = EditMessageText()
                    editMessageText.putData(stringChatId, intMessageId, textForOwn)
                    editMessageText.replyMarkup = botMenuFunction.createDataButtonMenu(Lessons.COMPARE_WORDS.getLessonsTitles(), callData_own)
                    protectedExecute(editMessageText)
                }

                callBackData == "\uD83D\uDCD2 Уроки других пользователей" -> {
                    val lessonTitles: List<String> = Lessons.PRESENT_SIMPLE.getLessonsTitles()
                    val editMessageText: EditMessageText = botMenuFunction.receiveCategoryMenu(
                        stringChatId, intMessageId, callData_commTxt,
                        textForOthers, lessonTitles
                    )
                    protectedExecute(editMessageText)
                }

                callBackData == "\uD83D\uDCCA Раздел администратора" -> {
                    val buttonTexts = listOf("Статистика и информация", "Сообщение в чат",
                        "Текст заглавия уроков", "Изображение верхнего банера", "Изображение нижнего банера")
                    val editMessageText = botMenuFunction.receiveInfoMenuForAdmin(
                        stringChatId,
                        intMessageId,
                        "Меню администратора",
                        buttonTexts
                    )
                    protectedExecute(editMessageText)
                }

                callBackData == "Сообщение в чат" -> {
                    protectedExecute(EditMessageText().putData(stringChatId, intMessageId, textForAdminSnd))
                    tempData[stringChatId] = inputMessageForAllUsers
                }

                callBackData == "Текст заглавия уроков" -> {
                    protectedExecute(EditMessageText().putData(stringChatId, intMessageId, textForAdminTtl))
                    tempData[stringChatId] = inputMessageForBillboard
                }

                callBackData == "Статистика и информация" -> {
                    val usersCount: Int = userDataRepository.findAll().count()
                    val messageText = "Время работы сервера: $localDateTime\nКэшировано уроков: ${lessonsMap.size}\nВсего пользователей: $usersCount"
                    protectedExecute(EditMessageText().putData(stringChatId, intMessageId, messageText))
                }

                callBackData == "Изображение верхнего банера" -> {
                    protectedExecute(EditMessageText().putData(stringChatId, intMessageId, textForAdminTop))
                    tempData[stringChatId] = inputTopBillboardUrl
                }

                callBackData == "Изображение нижнего банера" -> {
                    protectedExecute(EditMessageText().putData(stringChatId, intMessageId, textForAdminBot))
                    tempData[stringChatId] = inputBottomBillboardUrl
                }

                callBackData.contains(callData_Training) -> { // начать тренировку
                    val userData: UserData = userDataRepository.findById(longChatId).get()
                    val editMessageText: EditMessageText = if (userData.wordsForLearning.isEmpty()) {
                        EditMessageText().putData(stringChatId, intMessageId, "Нет слов для тренировки")
                    } else {
                        createTraining(stringChatId, intMessageId, userData)
                    }
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_commTxt) -> { // получить тексты других пользователей для тренировок
                    val editMessageText: EditMessageText
                    if (userLessonText[stringChatId] == null || userLessonText[stringChatId]!!.isEmpty()) {
                        val categoryText: String = callBackData.replace(callData_commTxt, "")
                        editMessageText = createNewTraining(stringChatId, longChatId, intMessageId, categoryText)
                    } else {
                        lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText[stringChatId]!!, callData_commTxt, randomWords)
                        editMessageText = botMenuFunction.createLessonButtonMenu(
                            stringChatId,
                            intMessageId,
                            properlyLessonAnswer[stringChatId]!!,
                            lessonUnitMap[stringChatId]!!,
                            callData_stepOne
                        )
                    }
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_own) -> {
                    tempData[stringChatId] = "" // сброс временных данных
                    val categoryText = callBackData.replace(callData_own, "")
                    cashLessonCategory[stringChatId] = categoryText.ifEmpty { cashLessonCategory[stringChatId]!! } // кэширование категории
                    val editMessageText: EditMessageText = botMenuFunction.receiveChoseMenu(stringChatId, intMessageId,
                        cashLessonCategory[stringChatId]!!, textForCategory +
                                cashLessonCategory[stringChatId]!! + "\n\n" + textForCategoryEnd)
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_showHint) -> { // показывать/не показывать сообщение с изображением-подсказкой
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
                    val editMessageText = botMenuFunction.receiveSettingMenu(stringChatId, intMessageId,
                        textForSettings, userData.isUsersTexts, userData.isShowHint,
                        userData.isSendTrainingMessage, userData.sinceTime, userData.untilTime)
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_addTxt) -> { // добавить текст для тренировки
                    val categoryText = callBackData.replace(callData_addTxt, "")
                    val userData: UserData = userDataRepository.findById(longChatId).get()
                    val lessonText: String = botMenuFunction.receiveLessonTextFromDb(categoryText, userData)

                    val forMessageText: String = if (lessonText.split("#").size > config.userLessonsLimit) {
                        textForLimit // return
                    } else {
                        saveAddWordMessageId[stringChatId] = intMessageId
                        cashLessonCategory[stringChatId] = categoryText
                        tempData[stringChatId] = inputEnText
                        textForEnInput // return
                    }
                    val editMessageText = EditMessageText()
                    editMessageText.putData(stringChatId, intMessageId, forMessageText)
                    editMessageText.replyMarkup = botMenuFunction.receiveOneButtonMenu("\uD83D\uDD19  Назад", callData_own)
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_delTxt) -> { // удалить текст для тренировки
                    val categoryText = callBackData.replace(callData_delTxt, "")
                    val userData: UserData = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = botMenuFunction.receiveLessonTextFromDb(categoryText, userData)

                    val editMessageText = EditMessageText()
                    editMessageText.chatId = stringChatId
                    editMessageText.messageId = intMessageId

                    if (userLessonText.isEmpty()) {
                        editMessageText.text = textForEmpty
                        editMessageText.replyMarkup = botMenuFunction.receiveOneButtonMenu("\uD83D\uDD19  Назад", callData_own)
                    } else {
                        val lessonTextsList: MutableList<String> = userLessonText.split("#") as MutableList
                        editMessageText.text = textForDel
                        editMessageText.replyMarkup = botMenuFunction.createMenuForTextDelete(lessonTextsList, categoryText)
                        deleteInDbText[stringChatId] = lessonTextsList
                    }
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_show) -> { // показать тексты для тренировки
                    val categoryText = callBackData.replace(callData_show, "")
                    val userData: UserData = userDataRepository.findById(longChatId).get()
                    val lessonTexts: String = botMenuFunction.receiveLessonTextFromDb(categoryText, userData)

                    val forMessageText: String = if (lessonTexts.isEmpty()) {
                        textForUnwatch
                    } else {
                        textForYours + lessonTexts.replace("#", "\n\n\uD83D\uDD39 ").
                        replace("*", "\n\uD83D\uDD38 ")
                    }
                    val editMessageText = EditMessageText()
                    editMessageText.putData(stringChatId, intMessageId, forMessageText)
                    editMessageText.replyMarkup = botMenuFunction.receiveOneButtonMenu("\uD83D\uDD19  Назад", callData_own)
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_userTxt) -> { // показать только свои тексты уроков
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    if (userData.isUsersTexts) {
                        userData.isUsersTexts = false
                        userDataRepository.save(userData)
                    } else {
                        userData.isUsersTexts = true
                        userDataRepository.save(userData)
                    }
                    val editMessageText: EditMessageText = botMenuFunction.receiveSettingMenu(
                        stringChatId,
                        intMessageId,
                        textForSettings,
                        userData.isUsersTexts,
                        userData.isShowHint,
                        userData.isSendTrainingMessage,
                        userData.sinceTime,
                        userData.untilTime
                    )
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_trainMessage) -> { // отправлять/не отправлять сообщения с тренировками
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    if (userData.isSendTrainingMessage) {
                        userData.isSendTrainingMessage = false
                        userDataRepository.save(userData)
                    } else {
                        userData.isSendTrainingMessage = true
                        userDataRepository.save(userData)
                    }
                    val editMessageText: EditMessageText = botMenuFunction.receiveSettingMenu(
                        stringChatId,
                        intMessageId,
                        textForSettings,
                        userData.isUsersTexts,
                        userData.isShowHint,
                        userData.isSendTrainingMessage,
                        userData.sinceTime,
                        userData.untilTime
                    )
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_setTime) -> { // установить время отправки сообщений с тренировками
                    val timeData: String = callBackData.replace(callData_setTime, "")
                    val userData: UserData = userDataRepository.findById(longChatId).get()
                    val currentSinceTime: Int = userData.sinceTime
                    val currentUntilTime: Int = userData.untilTime

                    when (timeData) {
                        "SinceDown" -> if (currentSinceTime > 0) userData.sinceTime = currentSinceTime - 1
                        "SinceUp" -> if (currentSinceTime < 23 && currentSinceTime < currentUntilTime - 1) userData.sinceTime = currentSinceTime + 1
                        "UntilDown" -> if (currentUntilTime > 1 && currentUntilTime > currentSinceTime + 1) userData.untilTime = currentUntilTime - 1
                        "UntilUp" -> if (currentUntilTime < 24) userData.untilTime = currentUntilTime + 1
                    }
                    userDataRepository.save(userData)
                    val editMessageText: EditMessageText = botMenuFunction.receiveSettingMenu(
                        stringChatId,
                        intMessageId,
                        textForSettings,
                        userData.isUsersTexts,
                        userData.isShowHint,
                        userData.isSendTrainingMessage,
                        userData.sinceTime,
                        userData.untilTime
                    )
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_divF) -> { // удалить текст для тренировки из выбранной категории
                    val indexFromList: Int = callBackData.replaceAfter(callData_divF, "").replace(callData_divF, "").toInt()
                    val lessonCategory: String = callBackData.replaceBefore(callData_divF, "").replace(callData_divF, "")

                    val buttonText: String = if (indexFromList >= 0) {
                        deleteInDbText[stringChatId]!![indexFromList].replace("*", "\n\n\uD83D\uDD38 ")
                    } else {
                        "❗ Из раздела «$lessonCategory» будут удалены все тексты пользователя"
                    }

                    val editMessageText = EditMessageText()
                    editMessageText.putData(stringChatId, intMessageId, "\uD83D\uDD39 $buttonText$textForIsDel")
                    editMessageText.replyMarkup = botMenuFunction.receiveTwoButtonsMenu("Удалить",
                        "$indexFromList#&$lessonCategory", "Отмена", callData_cancel)
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_divS) -> { // удаление выбранного текста для тренировки
                    val indexFromList: Int = callBackData.replaceAfter(callData_divS, "").replace(callData_divS, "").toInt()
                    val lessonCategory: String =
                        callBackData.replaceBefore(callData_divS, "").replace(callData_divS, "")
                    val userData: UserData = userDataRepository.findById(longChatId).get()

                    val newLessonText: String = if (indexFromList >= 0) {
                        val lessonTexts: MutableList<String> = deleteInDbText[stringChatId]!! as MutableList
                        if (lessonTexts.size == 1) {
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
                    editMessageText.putData(stringChatId, intMessageId, textForDeleted)
                    editMessageText.replyMarkup = botMenuFunction.receiveOneButtonMenu("\uD83D\uDD19  Назад", callData_own)
                    protectedExecute(editMessageText)
                }

                callBackData == callData_delData -> { // удаление данных пользователя
                    val user: Optional<UserData> = userDataRepository.findById(longChatId)
                    val forMessageText: String = if (user.isPresent) {
                        userDataRepository.deleteById(longChatId)
                        "Данные пользователя были удаленыㅤㅤㅤㅤ"
                    } else textForUnregisterUser
                    protectedExecute(EditMessageText().putData(stringChatId, intMessageId, forMessageText))
                }

                callBackData == callData_cancel -> {
                    protectedExecute(DeleteMessage().putData(stringChatId, intMessageId))
                }
                // Ниже - callBackData с выбранными уроками
                callBackData.contains(Lessons.PRESENT_SIMPLE.title) -> {
                    val user: UserData = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.presentSimple
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.PRESENT_SIMPLE)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId,
                        properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, callData_stepOne)
                    protectedExecute(editMessageText)

                  if (user.isShowHint) protectedExecute(EditMessageMedia().putData(stringChatId,
                      saveHintMessageId[stringChatId], Lessons.PRESENT_SIMPLE.pictureUrl))
                }

                callBackData.contains(Lessons.PRESENT_CONTINUOUS.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.presentContinuous
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.PRESENT_CONTINUOUS)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId,
                        properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, callData_stepOne)
                    protectedExecute(editMessageText)

                   if (user.isShowHint) protectedExecute(EditMessageMedia().putData(stringChatId,
                       saveHintMessageId[stringChatId], Lessons.PRESENT_CONTINUOUS.pictureUrl))
                }

                callBackData.contains(Lessons.PASSIVE_VOICE.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.passiveVoice
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.PASSIVE_VOICE)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId,
                        properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, callData_stepOne)
                    protectedExecute(editMessageText)

                    if (user.isShowHint) protectedExecute(EditMessageMedia().putData(stringChatId,
                        saveHintMessageId[stringChatId], Lessons.PASSIVE_VOICE.pictureUrl))
                }

                callBackData.contains(Lessons.MUCH_MANY_LOT.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.muchManyLot
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.MUCH_MANY_LOT)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId,
                        properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, callData_stepOne)
                    protectedExecute(editMessageText)
                    if (user.isShowHint) protectedExecute(EditMessageMedia().putData(stringChatId,
                        saveHintMessageId[stringChatId],Lessons.MUCH_MANY_LOT.pictureUrl))
                }

                callBackData.contains(Lessons.PERFECT_TENSE.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.perfectSentence
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.PERFECT_TENSE)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId,
                        properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, callData_stepOne)
                    protectedExecute(editMessageText)

                    if (user.isShowHint) protectedExecute(EditMessageMedia().putData(stringChatId,
                        saveHintMessageId[stringChatId], Lessons.PERFECT_TENSE.pictureUrl))
                }

                callBackData.contains(Lessons.DATE_AND_TIME.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.dateAndTime

                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.DATE_AND_TIME)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId,
                        properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, callData_stepOne)
                    protectedExecute(editMessageText)

                    if (user.isShowHint) protectedExecute(EditMessageMedia().putData(stringChatId,
                        saveHintMessageId[stringChatId], Lessons.DATE_AND_TIME.pictureUrl))
                }

                callBackData.contains(Lessons.COMPARE_WORDS.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.compareWords

                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.COMPARE_WORDS)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId,
                        properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, callData_stepOne)
                    protectedExecute(editMessageText)

                    if (user.isShowHint) protectedExecute(EditMessageMedia().putData(stringChatId,
                        saveHintMessageId[stringChatId], Lessons.COMPARE_WORDS.pictureUrl))
                }

                callBackData.contains(Lessons.VARIOUS_WORDS.title) -> {
                    val user = userDataRepository.findById(longChatId).get()
                    val userLessonText: String = user.variousWords
                    lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText, user.isUsersTexts, Lessons.VARIOUS_WORDS)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId,
                        properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, callData_stepOne)
                    protectedExecute(editMessageText)
                    if (user.isShowHint) protectedExecute(EditMessageMedia().putData(stringChatId,
                        saveHintMessageId[stringChatId],Lessons.VARIOUS_WORDS.pictureUrl))
                }

                callBackData.contains(Lessons.LEARN_WORDS.title) -> {
                    cashLessonCategory[stringChatId] = Lessons.LEARN_WORDS.title
                    val editMessageText: EditMessageText =botMenuFunction.receiveLearnWordMenu(stringChatId, intMessageId, textForAddWords)
                    protectedExecute(editMessageText)
                    if (userDataRepository.findById(longChatId).get().isShowHint) protectedExecute(EditMessageMedia().putData(stringChatId,
                        saveHintMessageId[stringChatId], Lessons.LEARN_WORDS.pictureUrl))
                }

                callBackData.contains("ㅤ") -> { // callBackData содержит пустой char (не space(!) - обработка нажатия кнопки экранной клавиатуры без текста)
                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId,
                        properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, callData_stepTwo)
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_stepOne) -> { // передача решаемого урока по шагам от метода к другому и обратно, для корректной обработки нажатий кнопок экранной клавиатуры
                    val word: String = callBackData.replace(callData_stepOne, "")
                    lessonUnitMap[stringChatId]!!.inputText += word

                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId,
                        properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, callData_stepTwo)
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_stepTwo) -> { // передача решаемого урока по шагам от метода к другому и обратно, для корректной обработки нажатий кнопок экранной клавиатуры
                    val word: String = callBackData.replace(callData_stepTwo, "")
                    lessonUnitMap[stringChatId]!!.inputText += word

                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId,
                        properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, callData_stepOne)
                    protectedExecute(editMessageText)
                }

                callBackData == callData_delWord -> { // удалить неправильно введённое слово
                    val splitInputText = lessonUnitMap[stringChatId]!!.inputText.split(" ")

                    if (lessonUnitMap[stringChatId]!!.inputText.isNotEmpty()) lessonUnitMap[stringChatId]!!.inputText =
                        lessonUnitMap[stringChatId]!!.inputText.replace(" " + splitInputText[splitInputText.size - 1], "")

                    val editMessageText: EditMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId,
                        properlyLessonAnswer[stringChatId]!!, lessonUnitMap[stringChatId]!!, callData_stepTwo)
                    protectedExecute(editMessageText)
                }

                callBackData.contains(callData_endOfTxt) -> { // запуск методов обработки ответа для пользователя, когда набор текста урока успешно завершён
                    val lessonUnitInputText = lessonUnitMap[stringChatId]!!.inputText
                    val fullText = (lessonUnitInputText + callBackData.replace(callData_endOfTxt, "")).trim()

                    val messageText: String
                    val callBackText: String

                    if (fullText.equals(lessonUnitMap[stringChatId]!!.englishText, ignoreCase = true)) {
                        properlyLessonAnswer[stringChatId] = properlyLessonAnswer[stringChatId]!! + 1
                        messageText = textForProperlyAnswer + lessonUnitMap[stringChatId]!!.englishText
                        callBackText = lessonUnitMap[stringChatId]!!.lessonTitle // установка имени раздела урока в callBackText для переотправки к выбранному уроку
                    } else {
                        messageText = textForIncorrectAnswer + lessonUnitMap[stringChatId]!!.englishText + "\n\nВаш вариант: \n\uD83D\uDD38 " + fullText
                        callBackText = callData_stepTwo
                        lessonUnitMap[stringChatId]!!.inputText = ""
                    }

                    val editMessageText = EditMessageText()
                    editMessageText.putData(stringChatId, intMessageId, messageText)
                    editMessageText.replyMarkup = botMenuFunction.receiveOneButtonMenu("Далее", callBackText)
                    protectedExecute(editMessageText)
                }
            }
        }
    }

    // Объект с уроком
    private fun createLessonUnit(
        chatId: String,
        userLessonText: String,
        isOnlyUserText: Boolean,
        lesson: Lessons): LessonUnit {
        if (lessonsMap[chatId] == null || lessonsMap[chatId]!!.isEmpty()) { // если Map с коллекцией текстов уроков == null или пустая,
            val lessonList = if (isOnlyUserText && userLessonText.isNotEmpty()) {
                if (userLessonText.contains("#")) userLessonText.split("#") as MutableList<String> else mutableListOf(userLessonText)
            } else {
                lesson.createLessonText(userLessonText) // коллекция заполняется текстами
            }
            lessonsMap[chatId] = lessonList // в Map, где ключи Id пользователя, в качестве значения добавляется коллекция с текстами уроков
        }
        var chooseRandom: Int = (0 until lessonsMap[chatId]!!.size).random() // выбор случайного текста с уроком из коллекции в Map
        var splitText: List<String> = lessonsMap[chatId]!![chooseRandom].split("*") // разделение ру. текста и eng текста

        while (lessonsMap[chatId]!!.size > 1 && lessonUnitMap[chatId] != null && lessonUnitMap[chatId]!!.russianText == splitText[0]) { // lessonUnit без повторного добавления одного и того же текста
            chooseRandom = (0 until lessonsMap[chatId]!!.size).random()
            splitText = lessonsMap[chatId]!![chooseRandom].split("*")
        }

        val forButtonText = mutableListOf<String>()
        val englishText: MutableList<String> = splitText[1].split(" ") as MutableList<String>
        forButtonText.addAll(englishText)

        while (forButtonText.size < 10) { // создание текста из 10 слов для кнопок экранной клавиатуры
            val chooseRandomWord: Int = (0 until randomWords.size - 1).random()
            forButtonText.add(randomWords[chooseRandomWord])
        }

        forButtonText.sortWith { first, second -> first.length - second.length }
        return LessonUnit(splitText[0], splitText[1], forButtonText, "", lesson.title)
    }

    // Объект с уроком
    private fun createLessonUnit(chatId: String, usersLessonText: String, lessonCategory: String, randomWords: List<String>): LessonUnit {
        if (lessonsMap[chatId] == null || lessonsMap[chatId]!!.isEmpty()) { // если Map с коллекцией текстов уроков == null или пустая,
            val splitText = if (usersLessonText.contains("#")) {
                usersLessonText.split("#") as MutableList<String>
            } else {
                mutableListOf(usersLessonText)
            }
            lessonsMap[chatId] = splitText// в Map, где ключи Id пользователя, в качестве значения добавляется коллекция с текстами уроков
        }

        val lessonMapSize: Int = lessonsMap[chatId]!!.size
        var chooseRandom: Int = (0 until lessonMapSize).random() // выбор случайного текста с уроком из коллекции в Map
        val chooseRandomOne: Int =
            (0 until lessonMapSize).random() // выбор случайного текста с уроком из коллекции в Map
        val chooseRandomTwo: Int =
            (0 until lessonMapSize).random() // выбор случайного текста с уроком из коллекции в Map

        var splitText: List<String> = lessonsMap[chatId]!![chooseRandom].split("*") // разделение ру. текста и eng текста
        val splitTextOne: List<String> = lessonsMap[chatId]!![chooseRandomOne].split("*")
        val splitTextTwo: List<String> = lessonsMap[chatId]!![chooseRandomTwo].split("*")

        while (lessonMapSize > 1 && lessonUnitMap[chatId] != null && lessonUnitMap[chatId]!!.russianText == splitText[0]) { // lessonUnit без повторного добавления одного и того же текста
            chooseRandom = (0 until lessonMapSize).random()
            splitText = lessonsMap[chatId]!![chooseRandom].split("*")
        }

        val forButtonText = mutableListOf<String>()
        val englishText: MutableList<String> = splitText[1].split(" ") as MutableList<String>
        val englishAdditionText = mutableListOf<String>()
        englishAdditionText.addAll(splitTextOne[1].split(" "))
        englishAdditionText.addAll(splitTextTwo[1].split(" "))
        forButtonText.addAll(englishText)
        if (forButtonText.size + englishAdditionText.size < 10) forButtonText.addAll(englishAdditionText)

        while (forButtonText.size < 10) { // создание текста из 10 слов для кнопок экранной клавиатуры
            val chooseRandomWord: Int = (0 until randomWords.size - 1).random()
            forButtonText.add(randomWords[chooseRandomWord])
        }

        forButtonText.sortWith { first, second -> first.length - second.length }
        return LessonUnit(splitText[0], splitText[1], forButtonText, "", lessonCategory)
    }

    // Объект с тренировкой
    private fun createDataUnit(chatId: String, usersLessonText: String, messageId: Int): DataUnit {
        if (lessonsMap[chatId] == null || lessonsMap[chatId]!!.isEmpty()) { // если Map с коллекцией текстов уроков == null или пустая,
            val splitText = if (usersLessonText.contains("#")) {
                usersLessonText.split("#") as MutableList<String>
            } else {
                mutableListOf(usersLessonText)
            }
            lessonsMap[chatId] = splitText// в Map, где ключи Id пользователя, в качестве значения добавляется коллекция с текстами уроков
        }

        val chooseRandom: Int = (0 until lessonsMap[chatId]!!.size).random() // выбор случайного текста с уроком из коллекции в Map
        val splitText = lessonsMap[chatId]!![chooseRandom].split("*")

        val dataUnit: DataUnit = if ((0..1).random() == 1) {
            DataUnit(splitText[0], splitText[1], messageId)
        } else {
            DataUnit(splitText[1], splitText[0], messageId)
        }
        return dataUnit
    }

    // Отправка сообщений с тренировками
    @Scheduled(cron = "0 0 * * * *") // @Scheduled(cron = "0 0 * * * *") отправка сообщения один раз в час
    fun sendLesson() {
        val datetimeFormatter = DateTimeFormatter.ofPattern("H")
        val localTime = LocalTime.now()
        val currentHour: Int = datetimeFormatter.format(localTime).toInt()

        for (data in userDataRepository.findAll()) {
            if (saveTrainingMessageId[data.chatId.toString()] != null) {
                protectedExecute(DeleteMessage().putData(data.chatId.toString(), saveTrainingMessageId[data.chatId.toString()]!!))
            }

            val random: Int = (0..3).random() // отправка сообщения с тренировкой в случайное время

            if (data.wordsForLearning.isNotEmpty() && data.isSendTrainingMessage &&
                currentHour in data.sinceTime until data.untilTime && random == 1) {

                val chatId: String = data.chatId.toString()
                clearCashedResources(chatId)
                val lesson: String = data.wordsForLearning
                val wordsCount: Int = lesson.split("#").size

                val word: String = when (wordsCount) { // в зависимости от количества слов на изучении изменяется текст сообщения
                    1, 21, 31, 41, 51, 61, 71, 81, 91 -> "слово"
                    in 2..4, in 22..24, in 32..34,
                    in 42..44, in 52..54, in 62..64,
                    in 72..74, in 82..84, in 92..94 -> "слова"
                    else -> "слов"
                }

                val sendMessage = SendMessage(data.chatId.toString(), "\uD83D\uDD14 У вас на изучении $wordsCount $word, проведём тренировку?")
                sendMessage.replyMarkup = botMenuFunction.receiveTwoButtonsMenu("Да", callData_Training,
                    "Не сейчас", callData_cancel)
                saveTrainingMessageId[data.chatId.toString()] = protectedExecute(sendMessage)
            }
        }
    }

    // Очистка ресурсов
    @Scheduled(cron = "0 0 0 1 * *")
    fun everydayReload() {
        tempData.clear()
        lessonsMap.clear()
        dataUnitMap.clear()
        lessonUnitMap.clear()
        userLessonText.clear()
        deleteInDbText.clear()
        saveHintMessageId.clear()
        saveStartMessageId.clear()
        cashLessonCategory.clear()
        properlyLessonAnswer.clear()
        saveAddWordMessageId.clear()
        saveTrainingMessageId.clear()
    }

    // Добавление данных пользователя в бд
    private fun setUserDataInDB(longChatId: Long, username: String): UserData {
        val data: Optional<UserData> = userDataRepository.findById(longChatId)
        val userData: UserData

        if (data.isEmpty) {
            userData = UserData()
            userData.chatId = longChatId
            userData.username = username
            userDataRepository.save(userData)
        } else {
            userData = data.get()
        }
        return userData
    }

    // Обработка введённого English текста
    private fun inputEnTextExecute(stringChatId: String, updateMessageText: String) {
        tempData[stringChatId] = ""

        val forMessageText: String = if (botMenuFunction.isTextIncorrect(updateMessageText, cashLessonCategory[stringChatId]!!)) {
                textForIncorrectInput // return
            } else {
                userLessonText[stringChatId] = updateMessageText
                tempData[stringChatId] = inputRuText // перенаправление в ветку: when (tempData[stringChatId]) -> inputRuText
                textForRuInput // return
            }

        val editMessageText: EditMessageText = EditMessageText().putData(stringChatId, saveAddWordMessageId[stringChatId]!!, forMessageText)
        editMessageText.replyMarkup = botMenuFunction.receiveOneButtonMenu("\uD83D\uDD19  Назад", callData_own)
        protectedExecute(editMessageText)
    }

    // Обработка введённого Ru текста
    private fun inputRuTextExecute(stringChatId: String, longChatId: Long, updateMessageText: String) {
        tempData[stringChatId] = ""
        val forMessageText: String = if (botMenuFunction.isTextIncorrect(updateMessageText, cashLessonCategory[stringChatId]!!)) {
                textForIncorrectInput // return
            } else {
                val userData = userDataRepository.findById(longChatId).get()

                val lessonText = if (botMenuFunction.receiveLessonTextFromDb(cashLessonCategory[stringChatId]!!, userData).isEmpty()) {
                        updateMessageText + "*" + userLessonText[stringChatId]!! // если в бд нет текстов, первый текст добавляется без символа "#"
                    } else {
                        "#" + updateMessageText + "*" + userLessonText[stringChatId]!! // если в бд есть тексты, новый текст добавляется с символом "#"
                    }
                val updateUserData: UserData =
                    botMenuFunction.updateLessonTextInDb(lessonText, cashLessonCategory[stringChatId]!!, userData)
                    userDataRepository.save(updateUserData)
                    textForSuccessInput // return
            }
        userLessonText[stringChatId] = "" // сообщение пользователя сохранённое в Map
        val editMessageText: EditMessageText = EditMessageText().putData(stringChatId, saveAddWordMessageId[stringChatId]!!, forMessageText)
        editMessageText.replyMarkup = botMenuFunction.receiveOneButtonMenu("\uD83D\uDD19  Назад", callData_own)
        protectedExecute(editMessageText)
    }

    // Обработка введённого текста тренировки
    private fun inputTrainingTextExecute(stringChatId: String, updateMessageText: String) {
        tempData[stringChatId] = ""
        val dataUnit: DataUnit = dataUnitMap[stringChatId]!!

        val textForMessage: String = if (dataUnit.secondText.equals(updateMessageText, ignoreCase = true)) {
            properlyLessonAnswer[stringChatId] = properlyLessonAnswer[stringChatId]!! + 1
            textForSuccessAnswer // return
        } else {
            textForWrongAnswer + dataUnit.secondText + textForWrongAnswerSec + updateMessageText // return
        }
        val editMessageText = botMenuFunction.receiveButtonEditMessage(stringChatId,
            dataUnit.id, textForMessage, callData_Training, listOf("Далее"))
        protectedExecute(editMessageText)
    }

    // Обработка введённого текста с именем пользователя
    private fun inputFirstNameExecute(stringChatId: String, longChatId: Long, updateMessageText: String) {
        tempData[stringChatId] = ""

        var checkAdmin = false
        userDataRepository.findAll()
            .forEach { el -> if (el.userFirstname == config.adminAccountName) checkAdmin = true }

        val forMessageText: String = if (updateMessageText.length > 15 || (updateMessageText == config.adminAccountName && checkAdmin)) {
                textForWrongInput  // return
            } else {
                val userData: UserData = userDataRepository.findById(longChatId).get()
                userData.userFirstname = updateMessageText
                userDataRepository.save(userData)
                textForSuccessAdd // return
            }
        protectedExecute(SendMessage(stringChatId, forMessageText))
    }

    // Очистка ресурсов
    private fun clearCashedResources(stringChatId: String) {
        userLessonText[stringChatId] = ""
        properlyLessonAnswer[stringChatId] = 0
        if (lessonsMap[stringChatId] != null) lessonsMap[stringChatId]!!.clear()
    }

    // Отправка сообщения в чат
    private fun inputMessageForUsersExecute(stringChatId: String, updateMessageText: String) {
        tempData[stringChatId] = ""
        if (updateMessageText.length > 3) {
            userDataRepository.findAll()
                .forEach { usr -> protectedExecute(SendMessage(usr.chatId.toString(), updateMessageText)) }
        }
    }

    // Добавление сообщения к изображению заставки
    private fun inputMessageForBillboardExecute(stringChatId: String, updateMessageText: String) {
        tempData[stringChatId] = ""
        messageForBillboard = updateMessageText
        val sendMessage = SendMessage(stringChatId, "Заглавие уроков изменено")
        protectedExecute(sendMessage)
    }

    // Изменить изображение заставки
    private fun inputTopBillboardUrlExecute(stringChatId: String, updateMessageText: String) {
        tempData[stringChatId] = ""
        val sendMessage: SendMessage
        if (updateMessageText.contains("http")) {
            urlForTopBillboard = updateMessageText
            sendMessage = SendMessage(stringChatId, "Изображение верхнего баннера изменено")
        } else {
            sendMessage = SendMessage(stringChatId, textForWrongInput)
        }
        protectedExecute(sendMessage)
    }

    // Изменить изображение подсказки
    private fun inputBottomBillboardUrlExecute(stringChatId: String, updateMessageText: String) {
        tempData[stringChatId] = ""
        val sendMessage: SendMessage
        if (updateMessageText.contains("http")) {
            urlForBottomBillboard = updateMessageText
            sendMessage = SendMessage(stringChatId, "Изображение нижнего баннера изменено")
        } else {
            sendMessage = SendMessage(stringChatId, textForWrongInput)
        }
        protectedExecute(sendMessage)
    }

    // Создание случайно выбранной тренировки
    private fun createTraining(stringChatId: String, intMessageId: Int, userData: UserData): EditMessageText {
        val editMessageText: EditMessageText
        if (userLessonText[stringChatId] == null || userLessonText[stringChatId]!!.isEmpty()) {
            userLessonText[stringChatId] = userData.wordsForLearning
        }
        if ((0..1).random() == 1) {
            lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText[stringChatId]!!, callData_Training, randomWords)
            if (properlyLessonAnswer[stringChatId] == null) properlyLessonAnswer[stringChatId] = 0
            editMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!,
                lessonUnitMap[stringChatId]!!, callData_stepTwo)
        } else {
            val dataUnit: DataUnit = createDataUnit(stringChatId, userLessonText[stringChatId]!!, intMessageId)
            dataUnitMap[stringChatId] = dataUnit
            editMessageText = EditMessageText().putData(stringChatId, intMessageId, "Введите перевод для слова: " + dataUnit.firstText)
            tempData[stringChatId] = inputTrainingText
        }
        return editMessageText
    }

    // Создание урока-тренировки
    private fun createNewTraining(stringChatId: String, longChatId: Long, intMessageId: Int, categoryText: String): EditMessageText {
        val editMessageText: EditMessageText
        val bufferLessonText = StringBuilder()
        val usersData: Iterable<UserData> = userDataRepository.findAll()

        for (data in usersData) {
            if (bufferLessonText.length > config.lessonTextMaxSize) break  // 100000
            if (data.chatId == longChatId) continue

            if (bufferLessonText.isEmpty()) {
                bufferLessonText.append(botMenuFunction.receiveLessonTextFromDb(categoryText, data))
            } else {
                bufferLessonText.append("#").append(botMenuFunction.receiveLessonTextFromDb(categoryText, data))
            }
        }

        val fromBufferText: String = bufferLessonText.toString()
        userLessonText[stringChatId] = fromBufferText

        if (!fromBufferText.contains("#")) {
            editMessageText = EditMessageText()
            editMessageText.putData(stringChatId, intMessageId, "В данной категории отсутствуют тексты")
        } else {
            lessonUnitMap[stringChatId] = createLessonUnit(stringChatId, userLessonText[stringChatId]!!, callData_commTxt, randomWords)
            editMessageText = botMenuFunction.createLessonButtonMenu(stringChatId, intMessageId, properlyLessonAnswer[stringChatId]!!,
                lessonUnitMap[stringChatId]!!, callData_stepOne)
        }
        return editMessageText
    }

}


const val callData_divF = "#@" // символ - разделитель данных из callBackData
const val callData_divS = "#&" // символ - разделитель данных из callBackData
const val callData_own = "#own" // callBackData, раздел с собственными тексты
const val callData_show = "#show" // callBackData, показать тексты для тренировки
const val callData_addTxt = "#add" // callBackData, добавить текст для тренировки
const val callData_commTxt = "#aul" // callBackData, получить тексты других пользователей для тренировок
const val callData_endOfTxt = "#fin" // callBackData, запуск методов обработки ответа для пользователя, когда набор текста урока успешно завершён
const val callData_setTime = "#time" // callBackData, установить время отправки сообщений с тренировками
const val callData_delTxt = "#todel" // callBackData, удалить текст для тренировки
const val callData_stepOne = "#step1" // callBackData, передача решаемого урока по шагам от метода к другому и обратно, для корректной обработки нажатий кнопок экранной клавиатуры
const val callData_stepTwo = "#step2" // callBackData, передача решаемого урока по шагам от метода к другому и обратно, для корректной обработки нажатий кнопок экранной клавиатуры
const val callData_showHint = "#hint" // callBackData, показывать/не показывать сообщение с изображением-подсказкой
const val callData_cancel = "#cancel" // callBackData, отмена действия
const val callData_delWord = "#delwrd" // callBackData, удалить неправильно введённое слово
const val callData_userTxt = "#usrtxt" // callBackData, показать только свои тексты уроков
const val callData_Training = "#train" // callBackData, начать тренировку
const val callData_delData = "#deldata" // callBackData, удаление данных пользователя
const val callData_trainMessage = "#trmes" // callBackData, отправлять/не отправлять сообщения с тренировками
