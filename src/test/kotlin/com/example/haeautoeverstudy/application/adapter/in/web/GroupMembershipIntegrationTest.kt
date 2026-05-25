package com.example.haeautoeverstudy.application.adapter.`in`.web

import com.example.haeautoeverstudy.application.adapter.out.persistence.MapGroupJpaEntity
import com.example.haeautoeverstudy.application.adapter.out.persistence.MapGroupJpaRepository
import com.example.haeautoeverstudy.application.adapter.out.persistence.UserJpaEntity
import com.example.haeautoeverstudy.application.adapter.out.persistence.UserJpaRepository
import com.example.haeautoeverstudy.application.port.out.NotificationChannelType
import com.example.haeautoeverstudy.application.port.out.SendNotificationCommand
import com.example.haeautoeverstudy.application.port.out.SendNotificationPort
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest(
    properties = [
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:group-membership-integration;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
    ],
)
@AutoConfigureMockMvc
class GroupMembershipIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    @Autowired
    private lateinit var mapGroupJpaRepository: MapGroupJpaRepository

    @BeforeEach
    fun setUp() {
        userJpaRepository.deleteAll()
        mapGroupJpaRepository.deleteAll()

        userJpaRepository.saveAll(
            listOf(
                userEntity(id = "owner", phoneNumber = "01010000001", joinedGroupIds = linkedSetOf("group")),
                userEntity(id = "participant", phoneNumber = "01010000002"),
                userEntity(id = "late", phoneNumber = "01010000003"),
                userEntity(id = "creator", phoneNumber = "01010000004"),
            ),
        )
        mapGroupJpaRepository.save(
            MapGroupJpaEntity(
                id = "group",
                ownerId = "owner",
                name = "family",
                maxParticipantCount = 2,
                deleted = false,
                participantIds = linkedSetOf("owner"),
            ),
        )
    }

    @Test
    fun `register user works through http adapter`() {
        mockMvc.post("/api/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"newUser","phoneNumber":"01098765432"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.userId") { isNotEmpty() }
            jsonPath("$.name") { value("newUser") }
            jsonPath("$.phoneNumber") { value("01098765432") }
        }

        val savedUser = userJpaRepository.findAll()
            .first { it.name == "newUser" }
        assertEquals("01098765432", savedUser.phoneNumber)
        assertTrue(savedUser.joinedGroupIds.isEmpty())
    }

    @Test
    fun `invalid register user request returns bad request`() {
        mockMvc.post("/api/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"abc","phoneNumber":"invalid"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `register user with duplicate phone number returns conflict`() {
        mockMvc.post("/api/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"firstUser","phoneNumber":"01098765432"}"""
        }.andExpect {
            status { isCreated() }
        }

        mockMvc.post("/api/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"secondUser","phoneNumber":"01098765432"}"""
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `register user with invalid phone number returns bad request`() {
        mockMvc.post("/api/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"validName","phoneNumber":"phone-number"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `create group and get user groups work through http adapters`() {
        mockMvc.post("/api/groups") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"groupId":"created","ownerId":"creator","name":"travel","maxParticipantCount":3}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.groupId") { value("created") }
            jsonPath("$.ownerId") { value("creator") }
            jsonPath("$.currentParticipantCount") { value(1) }
        }

        val createdGroup = mapGroupJpaRepository.findById("created").orElseThrow()
        val creator = userJpaRepository.findById("creator").orElseThrow()
        assertEquals(setOf("creator"), createdGroup.participantIds)
        assertEquals(setOf("created"), creator.joinedGroupIds)

        mockMvc.post("/api/groups") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"groupId":"created","ownerId":"creator","name":"travel","maxParticipantCount":3}"""
        }.andExpect {
            status { isConflict() }
        }

        mockMvc.get("/api/users/creator/groups")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(1) }
                jsonPath("$[0].groupId") { value("created") }
                jsonPath("$[0].ownerId") { value("creator") }
            }
    }

    @Test
    fun `create group with invalid input or missing owner returns client error`() {
        mockMvc.post("/api/groups") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"groupId":"invalid-name","ownerId":"creator","name":"abc","maxParticipantCount":3}"""
        }.andExpect {
            status { isBadRequest() }
        }

        mockMvc.post("/api/groups") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"groupId":"invalid-max","ownerId":"creator","name":"travel","maxParticipantCount":0}"""
        }.andExpect {
            status { isBadRequest() }
        }

        mockMvc.post("/api/groups") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"groupId":"missing-owner","ownerId":"missing","name":"travel","maxParticipantCount":3}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `join update location get locations and leave work through http adapters`() {
        mockMvc.post("/api/groups/group/members") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"participant"}"""
        }.andExpect {
            status { isNoContent() }
        }

        val joinedGroup = mapGroupJpaRepository.findById("group").orElseThrow()
        val joinedUser = userJpaRepository.findById("participant").orElseThrow()
        assertEquals(setOf("owner", "participant"), joinedGroup.participantIds)
        assertEquals(setOf("group"), joinedUser.joinedGroupIds)

        mockMvc.post("/api/groups/group/members") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"late"}"""
        }.andExpect {
            status { isConflict() }
        }
        assertFalse("late" in mapGroupJpaRepository.findById("group").orElseThrow().participantIds)

        mockMvc.put("/api/users/participant/location") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"latitude":37.5665,"longitude":126.9780}"""
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.get("/api/groups/group/locations")
            .andExpect {
                status { isOk() }
                jsonPath("$.groupId") { value("group") }
                jsonPath("$.locations.length()") { value(1) }
                jsonPath("$.locations[0].userId") { value("participant") }
                jsonPath("$.locations[0].latitude") { value(37.5665) }
                jsonPath("$.locations[0].longitude") { value(126.9780) }
            }

        mockMvc.post("/api/groups/group/members/leave") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"participant"}"""
        }.andExpect {
            status { isNoContent() }
        }

        val leftGroup = mapGroupJpaRepository.findById("group").orElseThrow()
        val leftUser = userJpaRepository.findById("participant").orElseThrow()
        assertEquals(setOf("owner"), leftGroup.participantIds)
        assertTrue(leftUser.joinedGroupIds.isEmpty())

        mockMvc.get("/api/groups/group/locations")
            .andExpect {
                status { isOk() }
                jsonPath("$.locations.length()") { value(0) }
            }
    }

    @Test
    fun `join group rejects duplicate user missing user missing group and over capacity`() {
        mockMvc.post("/api/groups/group/members") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"participant"}"""
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.post("/api/groups/group/members") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"participant"}"""
        }.andExpect {
            status { isBadRequest() }
        }

        mockMvc.post("/api/groups/group/members") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"late"}"""
        }.andExpect {
            status { isConflict() }
        }

        mockMvc.post("/api/groups/group/members") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"missing"}"""
        }.andExpect {
            status { isNotFound() }
        }

        mockMvc.post("/api/groups/missing-group/members") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"late"}"""
        }.andExpect {
            status { isNotFound() }
        }

        val group = mapGroupJpaRepository.findById("group").orElseThrow()
        val participant = userJpaRepository.findById("participant").orElseThrow()
        val late = userJpaRepository.findById("late").orElseThrow()
        assertEquals(setOf("owner", "participant"), group.participantIds)
        assertEquals(setOf("group"), participant.joinedGroupIds)
        assertTrue(late.joinedGroupIds.isEmpty())
    }

    @Test
    fun `user location update is visible only in groups the user belongs to`() {
        mapGroupJpaRepository.save(
            MapGroupJpaEntity(
                id = "another",
                ownerId = "creator",
                name = "travel",
                maxParticipantCount = 3,
                deleted = false,
                participantIds = linkedSetOf("creator"),
            ),
        )
        val creator = userJpaRepository.findById("creator").orElseThrow()
        creator.joinedGroupIds += "another"
        userJpaRepository.save(creator)

        mockMvc.put("/api/users/participant/location") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"latitude":37.5665,"longitude":126.9780}"""
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.get("/api/groups/group/locations")
            .andExpect {
                status { isOk() }
                jsonPath("$.locations.length()") { value(0) }
            }

        mockMvc.post("/api/groups/group/members") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"participant"}"""
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.get("/api/groups/group/locations")
            .andExpect {
                status { isOk() }
                jsonPath("$.locations.length()") { value(1) }
                jsonPath("$.locations[0].userId") { value("participant") }
            }

        mockMvc.get("/api/groups/another/locations")
            .andExpect {
                status { isOk() }
                jsonPath("$.locations.length()") { value(0) }
            }
    }

    @Test
    fun `location update rejects invalid coordinate and missing user`() {
        mockMvc.put("/api/users/owner/location") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"latitude":91.0,"longitude":126.9780}"""
        }.andExpect {
            status { isBadRequest() }
        }

        mockMvc.put("/api/users/missing/location") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"latitude":37.5665,"longitude":126.9780}"""
        }.andExpect {
            status { isNotFound() }
        }

        mockMvc.get("/api/groups/missing-group/locations")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `global user location is not removed when user leaves one of several groups`() {
        mapGroupJpaRepository.save(
            MapGroupJpaEntity(
                id = "another",
                ownerId = "creator",
                name = "travel",
                maxParticipantCount = 3,
                deleted = false,
                participantIds = linkedSetOf("creator", "participant"),
            ),
        )
        val participant = userJpaRepository.findById("participant").orElseThrow()
        participant.joinedGroupIds += setOf("group", "another")
        userJpaRepository.save(participant)
        val group = mapGroupJpaRepository.findById("group").orElseThrow()
        group.participantIds += "participant"
        mapGroupJpaRepository.save(group)

        mockMvc.put("/api/users/participant/location") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"latitude":37.5665,"longitude":126.9780}"""
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.post("/api/groups/group/members/leave") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"participant"}"""
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.get("/api/groups/group/locations")
            .andExpect {
                status { isOk() }
                jsonPath("$.locations.length()") { value(0) }
            }

        mockMvc.get("/api/groups/another/locations")
            .andExpect {
                status { isOk() }
                jsonPath("$.locations.length()") { value(1) }
                jsonPath("$.locations[0].userId") { value("participant") }
            }
    }

    @Test
    fun `delete group is allowed only for owner and clears user memberships`() {
        mockMvc.post("/api/groups/group/members") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"participant"}"""
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.put("/api/users/participant/location") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"latitude":37.5665,"longitude":126.9780}"""
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.delete("/api/groups/group") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"requestedBy":"participant"}"""
        }.andExpect {
            status { isBadRequest() }
        }

        assertFalse(mapGroupJpaRepository.findById("group").orElseThrow().deleted)

        mockMvc.delete("/api/groups/group") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"requestedBy":"owner"}"""
        }.andExpect {
            status { isNoContent() }
        }

        val deletedGroup = mapGroupJpaRepository.findById("group").orElseThrow()
        val owner = userJpaRepository.findById("owner").orElseThrow()
        val participant = userJpaRepository.findById("participant").orElseThrow()
        assertTrue(deletedGroup.deleted)
        assertTrue(owner.joinedGroupIds.isEmpty())
        assertTrue(participant.joinedGroupIds.isEmpty())

        mockMvc.get("/api/users/participant/groups")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(0) }
            }
    }

    @Test
    fun `delete group rejects missing group and already deleted group`() {
        mockMvc.delete("/api/groups/missing-group") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"requestedBy":"owner"}"""
        }.andExpect {
            status { isNotFound() }
        }

        mockMvc.delete("/api/groups/group") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"requestedBy":"owner"}"""
        }.andExpect {
            status { isNoContent() }
        }

        mockMvc.delete("/api/groups/group") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"requestedBy":"owner"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `swagger openapi endpoint is exposed`() {
        mockMvc.get("/v3/api-docs")
            .andExpect {
                status { isOk() }
                jsonPath("$.openapi") { exists() }
                jsonPath("$.paths['/api/users']") { exists() }
                jsonPath("$.paths['/api/groups']") { exists() }
                jsonPath("$.paths['/api/groups/{groupId}']") { exists() }
                jsonPath("$.paths['/api/groups/{groupId}/members']") { exists() }
                jsonPath("$.paths['/api/groups/{groupId}/locations']") { exists() }
                jsonPath("$.paths['/api/users/{userId}/location']") { exists() }
                jsonPath("$.paths['/api/users/{userId}/groups']") { exists() }
            }
    }

    private fun userEntity(
        id: String,
        phoneNumber: String,
        joinedGroupIds: MutableSet<String> = linkedSetOf(),
    ): UserJpaEntity =
        UserJpaEntity(
            id = id,
            name = "${id}User",
            phoneNumber = phoneNumber,
            joinedGroupIds = joinedGroupIds,
        )

    @TestConfiguration
    class TestNotificationConfiguration {
        @Bean
        @Primary
        fun noOpSmsNotificationPort(): SendNotificationPort =
            object : SendNotificationPort {
                override val channelType: NotificationChannelType = NotificationChannelType.SMS

                override suspend fun send(command: SendNotificationCommand) = Unit
            }
    }
}
