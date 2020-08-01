package me.honnold.ladderhero.service.domain

import me.honnold.ladderhero.dao.UserDAO
import me.honnold.ladderhero.dao.domain.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService(private val userDAO: UserDAO) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserService::class.java)
    }

    fun createUser(username: String, encodedPassword: String): Mono<User> {
        val user = User(username = username, encodedPassword = encodedPassword)

        return this.userDAO.save(user)
    }
}