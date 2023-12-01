package al.dmitriy.dev.learning.service
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

interface BotMenuInterface {

    fun createButtonMenu(textForButton: List<String>) : InlineKeyboardMarkup

    fun createDataButtonMenu(textForButton: List<String>, callBackData: String = "") : InlineKeyboardMarkup

}