package com.example.haeautoeverstudy.application.domain.model

import com.example.haeautoeverstudy.application.domain.model.exception.GroupMembershipNotFoundException
import com.example.haeautoeverstudy.application.domain.model.exception.InvalidPhoneNumberException
import com.example.haeautoeverstudy.application.domain.model.exception.InvalidUserNameException
import com.example.haeautoeverstudy.application.domain.model.exception.UserAlreadyJoinedGroupException
import java.util.UUID

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId cannot be blank" }
    }

    companion object {
        fun withUUID(): UserId = UserId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class UserName(val value: String) {
    init {
        if (value.isBlank()) {
            throw InvalidUserNameException()
        }

        if (value.length >= 32 || value.length <= 4) {
            throw InvalidUserNameException()
        }
    }
}

@JvmInline
value class PhoneNumber(val value: String) {
    init {
        val normalized = value.filterNot(Char::isWhitespace)
        if (!PHONE_NUMBER_PATTERN.matches(normalized)) {
            throw InvalidPhoneNumberException(value)
        }
    }

    companion object {
        private val PHONE_NUMBER_PATTERN = Regex("^\\+?[0-9]{8,15}$")
    }
}

data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180" }
    }
}

//요구사항에서 이름 /휴대폰 변경 내용은 없음.
class User private constructor(
    val id: UserId,
    val name: UserName,
    val phoneNumber: PhoneNumber,
    private val joinedGroupIds: HashSet<GroupId> = HashSet(),
) {
    val groupIds: Set<GroupId>
        get() = joinedGroupIds.toSet()

    fun joinGroup(groupId: GroupId) {
        if (!joinedGroupIds.add(groupId)) {
            throw UserAlreadyJoinedGroupException(id.value, groupId.value)
        }
    }

    fun leaveGroup(groupId: GroupId) {
        if (!joinedGroupIds.remove(groupId)) {
            throw GroupMembershipNotFoundException(id.value, groupId.value)
        }
    }

    fun isMemberOf(groupId: GroupId): Boolean = groupId in joinedGroupIds

    //팩토리 메서드
    companion object {
        fun of(
            name: UserName,
            phoneNumber: PhoneNumber,
            id: UserId = UserId.withUUID(),
        ): User = User(
            id = id,
            name = name,
            phoneNumber = phoneNumber,
        )

        fun restore(
            id: UserId,
            name: UserName,
            phoneNumber: PhoneNumber,
            joinedGroupIds: Set<GroupId>,
        ): User = User(
            id = id,
            name = name,
            phoneNumber = phoneNumber,
            joinedGroupIds = HashSet(joinedGroupIds),
        )
    }
}
