package com.example.haeautoeverstudy.application.adapter.out.persistence

import com.example.haeautoeverstudy.application.domain.model.PhoneNumber
import com.example.haeautoeverstudy.application.domain.model.User
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserName
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@Transactional
@SpringJUnitConfig(
    classes = [
        JpaPersistenceAdapterTestConfiguration::class,
        JpaUserPersistenceAdapter::class,
    ],
)
class JpaUserPersistenceAdapterTest {
    @Autowired
    private lateinit var adapter: JpaUserPersistenceAdapter

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `saves and loads user`() {
        val user = User.of(
            id = UserId("user"),
            name = UserName("yongha"),
            phoneNumber = PhoneNumber("01012345678"),
        )

        adapter.save(user)
        entityManager.flush()
        entityManager.clear()

        val loaded = adapter.loadById(UserId("user"))

        assertEquals(UserId("user"), loaded.id)
        assertEquals(UserName("yongha"), loaded.name)
        assertEquals(PhoneNumber("01012345678"), loaded.phoneNumber)
    }

    @Test
    fun `updates existing user`() {
        val user = User.of(
            id = UserId("user-to-update"),
            name = UserName("yongha"),
            phoneNumber = PhoneNumber("01012345678"),
        )
        adapter.save(user)

        adapter.save(user)
        entityManager.flush()
        entityManager.clear()

        val loaded = adapter.loadById(UserId("user-to-update"))

        assertEquals(UserId("user-to-update"), loaded.id)
        assertEquals(UserName("yongha"), loaded.name)
        assertEquals(PhoneNumber("01012345678"), loaded.phoneNumber)
    }
}
