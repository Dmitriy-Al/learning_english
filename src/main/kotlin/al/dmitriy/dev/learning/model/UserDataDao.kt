package al.dmitriy.dev.learning.model

import org.springframework.data.repository.CrudRepository

interface UserDataDao : CrudRepository<UserData, Long?> {

}