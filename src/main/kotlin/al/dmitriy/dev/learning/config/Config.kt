package al.dmitriy.dev.learning.config

import lombok.Data
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Data
@Configuration
@EnableScheduling
class Config {

    final val botUsername: String = "t.me"
    final val botToken: String = "658"
    final val adminAccountName: String = "a"
    final val lessonTextMaxSize: Int = 100000
    final val userLessonsLimit: Int = 100
    final val bottomBillboardUrl: String = "https://disk.yandex.ru/i/WrlNU6Z0mWb3Ng"
    final val topBillboardUrl: String = "https://disk.yandex.ru/i/5UnG_DF_1zCHCA"

}