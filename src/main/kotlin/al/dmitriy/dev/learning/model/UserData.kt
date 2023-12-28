package al.dmitriy.dev.learning.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.sql.Timestamp

@Entity(name = "user_data_storage")
open class UserData {


    @Id
    @Column(name = "chat_id")
    open var chatId: Long = 0

    @Column(name = "username")
    open var username: String = ""

    @Column(name = "user_firstname")
    open var userFirstname: String = ""

    @Column(name = "training_message_since_time")
    open var sinceTime: Int = 12

    @Column(name = "training_message_until_time")
    open var untilTime: Int = 20

    @Column(name = "show_hint_message")
    open var isShowHint: Boolean =  true

    @Column(name = "view_as_chat")
    open var isViewAsChat: Boolean =  false

    @Column(name = "only_users_texts")
    open var isUsersTexts: Boolean =  false

    @Column(name = "send_training_message")
    open var isSendTrainingMessage: Boolean =  false

    @Column(name = "user_register_date_time")
    open var userRegisterDateTime: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "date_and_time", columnDefinition = "text")
    open var dateAndTime: String = ""

    @Column(name = "lot_much_many", columnDefinition = "text")
    open var muchManyLot: String = ""

    @Column(name = "passive_voice", columnDefinition = "text")
    open var passiveVoice: String = ""

    @Column(name = "compare_words", columnDefinition = "text")
    open var compareWords: String = ""

    @Column(name = "various_words", columnDefinition = "text")
    open var variousWords: String = ""

    @Column(name = "present_simple", columnDefinition = "text")
    open var presentSimple: String = ""

    @Column(name = "perfect_sentence", columnDefinition = "text")
    open var perfectSentence: String = ""

    @Column(name = "words_for_learning", columnDefinition = "text")
    open var wordsForLearning: String = ""

    @Column(name = "present_continuous", columnDefinition = "text")
    open var presentContinuous: String = ""


}
