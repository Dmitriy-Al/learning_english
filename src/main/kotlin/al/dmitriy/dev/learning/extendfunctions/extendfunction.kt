package al.dmitriy.dev.learning.extendfunctions

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


fun InlineKeyboardButton.putData(text: String, callbackData: String) {
    this.text = text
    this.callbackData = callbackData
}

fun SendPhoto.putData(stringChatId: String, url: String) { // TODO
    this.chatId = stringChatId
    this.photo = InputFile(url)
}

fun DeleteMessage.putData(stringChatId: String, messageId: Int): DeleteMessage {
    this.chatId = stringChatId
    this.messageId = messageId
    return this
}

fun AbsSender.protectedExecute(deleteMessage: DeleteMessage) {
    try {
        this.execute(deleteMessage)
    } catch (e: TelegramApiException) {
        println("AbsSender.protectedExecute????????????????????????????????????????????????????") // TODO
    }
}

