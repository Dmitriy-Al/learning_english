package al.dmitriy.dev.learning.overrideFunctions

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

fun InlineKeyboardButton.putData(text: String, callbackData: String) {
     this.text = text
    this.callbackData = callbackData
 }