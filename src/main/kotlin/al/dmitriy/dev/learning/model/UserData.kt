package al.dmitriy.dev.learning.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity(name = "user_data_storage")
open class UserData : Cloneable {


    @Id
    @Column(name = "string_chat_id")
    open var chatId: Long = 0

    open var username: String =  ""

    @Column(name = "user_firstname")
    open var userFirstname: String =  ""

    @Column(name = "various_words", columnDefinition = "text")
    open var variousWords: String = ""

    @Column(name = "date_and_time", columnDefinition = "text")
    open var dateAndTime: String = ""

    @Column(name = "lot_much_many", columnDefinition = "text")
    open var muchManyLot: String = ""

    @Column(name = "passive_voice", columnDefinition = "text")
    open var passiveVoice: String = ""

    @Column(name = "compare_words", columnDefinition = "text")
    open var compareWords: String = ""

    @Column(name = "present_simple", columnDefinition = "text")
    open var presentSimple: String = ""

    @Column(name = "perfect_sentence", columnDefinition = "text")
    open var perfectSentence: String = ""

    @Column(name = "words_for_learning", columnDefinition = "text")
    open var wordsForLearning: String = ""

    @Column(name = "present_continuous", columnDefinition = "text")
    open var presentContinuous: String = ""

    @Column(name = "pronoun_and_preposition", columnDefinition = "text")
    open var pronounAndPreposition: String = ""




    override fun clone(): Any {
        val any = Any()
        try{
            return super.clone()
        } catch (e: CloneNotSupportedException){
            // TODO
        }
        return any
    }


}


/*
@Entity(name = "user_data_storage")
open class UserDataStorage(@Id open var chatId: Long?) {
constructor() : this(null) {}
 */