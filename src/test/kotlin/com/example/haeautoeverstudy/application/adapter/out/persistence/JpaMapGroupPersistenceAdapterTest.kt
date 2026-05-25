package com.example.haeautoeverstudy.application.adapter.out.persistence

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.GroupName
import com.example.haeautoeverstudy.application.domain.model.MapGroup
import com.example.haeautoeverstudy.application.domain.model.UserId
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Transactional
@SpringJUnitConfig(
    classes = [
        JpaPersistenceAdapterTestConfiguration::class,
        JpaMapGroupPersistenceAdapter::class,
    ],
)
class JpaMapGroupPersistenceAdapterTest {
    @Autowired
    private lateinit var adapter: JpaMapGroupPersistenceAdapter

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `saves and loads group with participants`() {
        val group = MapGroup.of(
            id = GroupId("group"),
            ownerId = UserId("owner"),
            name = GroupName("family"),
            maxParticipantCount = 3,
        )
        group.addParticipant(UserId("participant"))

        adapter.save(group)
        entityManager.flush()
        entityManager.clear()

        val loaded = adapter.loadById(GroupId("group"))

        assertEquals(GroupId("group"), loaded.id)
        assertEquals(UserId("owner"), loaded.ownerId)
        assertEquals(GroupName("family"), loaded.name)
        assertEquals(3, loaded.maxParticipantCount)
        assertEquals(setOf(UserId("owner"), UserId("participant")), loaded.participants)
    }

    @Test
    fun `updates existing group membership`() {
        val group = MapGroup.of(
            id = GroupId("group-to-update"),
            ownerId = UserId("owner"),
            name = GroupName("family"),
            maxParticipantCount = 3,
        )
        group.addParticipant(UserId("participant"))
        adapter.save(group)

        group.removeParticipant(UserId("participant"))
        adapter.save(group)
        entityManager.flush()
        entityManager.clear()

        val loaded = adapter.loadById(GroupId("group-to-update"))

        assertTrue(UserId("participant") !in loaded.participants)
        assertEquals(setOf(UserId("owner")), loaded.participants)
    }
}
