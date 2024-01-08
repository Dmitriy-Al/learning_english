package al.dmitriy.dev.learning.config

import lombok.Data
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Data
@Configuration
@EnableScheduling
class Config {

    final val botUsername: String = "Bot" // t.me/ForEnglishTrainingBot. имя бота
    final val botToken: String = "5684975537" // токен бота
    final val adminAccountName: String = "admin" // имя учетной записи администратора
    final val lessonTextMaxSize: Int = 100000 // максимальная длинна текста из уроков других пользователей
    final val userLessonsLimit: Int = 100 // максимальная количество уроков пользователя
    final val bottomBillboardUrl: String = "https://disk.yandex.ru/i/WrlNU6Z0mWb3Ng" // изображение "низ" на стартовой странице
    final val topBillboardUrl: String = "https://disk.yandex.ru/i/5UnG_DF_1zCHCA" // изображение "верх" на стартовой странице (шапка)

}