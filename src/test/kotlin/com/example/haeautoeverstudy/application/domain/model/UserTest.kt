package com.example.haeautoeverstudy.application.domain.model

import com.example.haeautoeverstudy.application.domain.model.exception.GroupMembershipNotFoundException
import com.example.haeautoeverstudy.application.domain.model.exception.InvalidPhoneNumberException
import com.example.haeautoeverstudy.application.domain.model.exception.InvalidUserNameException
import com.example.haeautoeverstudy.application.domain.model.exception.UserAlreadyJoinedGroupException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

//TODO fixture 등 이용하여 랜덤 값 생성하도록 변경 필요
class UserTest {
    @Test
    fun `creates user`() {
        val user = User.of(
            id = UserId("user"),
            name = UserName("yongha"),
            phoneNumber = PhoneNumber("01012345678"),
        )

        assertEquals(UserId("user"), user.id)
        assertEquals(UserName("yongha"), user.name)
        assertEquals(PhoneNumber("01012345678"), user.phoneNumber)
        assertTrue(user.groupIds.isEmpty())
    }

    @Test
    fun `rejects invalid user name`() {
        assertFailsWith<InvalidUserNameException> {
            UserName("")
        }

        assertFailsWith<InvalidUserNameException> {
            UserName("abcd")
        }
    }

    @Test
    fun `rejects invalid phone number`() {
        assertFailsWith<InvalidPhoneNumberException> {
            PhoneNumber("phone-number")
        }
    }

    @Test
    fun `rejects invalid location`() {
        assertFailsWith<IllegalArgumentException> {
            GeoLocation(latitude = 100.0, longitude = 126.9780)
        }

        assertFailsWith<IllegalArgumentException> {
            GeoLocation(latitude = 37.5665, longitude = 200.0)
        }
    }

    @Test
    fun `joins and leaves group`() {
        val user = user()
        val groupId = GroupId("group")

        user.joinGroup(groupId)
        assertTrue(user.isMemberOf(groupId))

        user.leaveGroup(groupId)
        assertTrue(user.groupIds.isEmpty())
    }

    @Test
    fun `rejects duplicate group membership`() {
        val user = user()
        val groupId = GroupId("group")

        user.joinGroup(groupId)

        assertFailsWith<UserAlreadyJoinedGroupException> {
            user.joinGroup(groupId)
        }
    }

    @Test
    fun `rejects leaving group that user did not join`() {
        assertFailsWith<GroupMembershipNotFoundException> {
            user().leaveGroup(GroupId("group"))
        }
    }

    private fun user(): User =
        User.of(
            id = UserId("user"),
            name = UserName("yongha"),
            phoneNumber = PhoneNumber("01012345678"),
        )
}
