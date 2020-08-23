package me.honnold.ladderhero.service.domain

import me.honnold.ladderhero.dao.UserDAO
import me.honnold.ladderhero.dao.domain.User
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService(private val userDAO: UserDAO) {
    fun getUser(username: String): Mono<User> {
        return this.userDAO.findByUsername(username)
    }

    fun createUser(username: String, encodedPassword: String): Mono<User> {
        val user = User(username = username, encodedPassword = encodedPassword)

        return this.userDAO.save(user)
    }
}
