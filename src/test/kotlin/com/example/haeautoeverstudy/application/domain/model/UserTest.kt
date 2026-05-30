package com.example.haeautoeverstudy.application.domain.model

import com.example.haeautoeverstudy.application.domain.model.exception.InvalidPhoneNumberException
import com.example.haeautoeverstudy.application.domain.model.exception.InvalidUserNameException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    private fun user(): User =
        User.of(
            id = UserId("user"),
            name = UserName("yongha"),
            phoneNumber = PhoneNumber("01012345678"),
        )
}
