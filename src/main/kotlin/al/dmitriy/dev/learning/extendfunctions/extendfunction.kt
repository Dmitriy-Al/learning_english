package al.dmitriy.dev.learning.extendfunctions

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

fun InlineKeyboardButton.putData(text: String, callbackData: String) {
    this.text = text
    this.callbackData = callbackData
}

fun SendPhoto.putData(stringChatId: String, url: String) { // TODO
    this.chatId = stringChatId
    this.photo = InputFile(url)
}
