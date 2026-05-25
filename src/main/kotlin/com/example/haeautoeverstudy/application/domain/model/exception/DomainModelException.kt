package com.example.haeautoeverstudy.application.domain.model.exception

//TODO 분리 필요. 공부 목적이기 때분에 세부 분류 미진행
open class DomainModelException(message: String) : RuntimeException(message)

class InvalidGroupNameException :
    DomainModelException("Group name cannot be blank")

class InvalidMaxParticipantCountException(maxParticipantCount: Int) :
    DomainModelException("Max participant count must be positive. current=$maxParticipantCount")

class GroupCapacityExceededException(groupId: String, maxParticipantCount: Int) :
    DomainModelException("Group[$groupId] is full. maxParticipantCount=$maxParticipantCount")

class ParticipantAlreadyExistsException(groupId: String, userId: String) :
    DomainModelException("User[$userId] is already a participant of Group[$groupId]")

class ParticipantNotFoundException(groupId: String, userId: String) :
    DomainModelException("User[$userId] is not a participant of Group[$groupId]")

class NonParticipantAccessException(groupId: String, userId: String) :
    DomainModelException("User[$userId] cannot access Group[$groupId] because the user is not a participant")

class GroupOwnerCannotLeaveException(groupId: String, userId: String) :
    DomainModelException("Owner User[$userId] cannot leave Group[$groupId]. Delete the group instead")

class GroupDeletionForbiddenException(groupId: String, userId: String) :
    DomainModelException("User[$userId] cannot delete Group[$groupId] because the user is not the owner")

class GroupAlreadyDeletedException(groupId: String) :
    DomainModelException("Group[$groupId] is already deleted")

class GroupAlreadyExistsException(groupId: String) :
    DomainModelException("Group[$groupId] already exists")

class InvalidUserNameException :
    DomainModelException("User name cannot be blank")

class InvalidPhoneNumberException(phoneNumber: String) :
    DomainModelException("Phone number[$phoneNumber] is invalid")

class UserAlreadyExistsException(phoneNumber: String) :
    DomainModelException("User with phone number[$phoneNumber] already exists")

class UserAlreadyJoinedGroupException(userId: String, groupId: String) :
    DomainModelException("User[$userId] already joined Group[$groupId]")

class GroupMembershipNotFoundException(userId: String, groupId: String) :
    DomainModelException("User[$userId] is not a member of Group[$groupId]")

class MembershipStateMismatchException(userId: String, groupId: String) :
    DomainModelException("User[$userId] and Group[$groupId] membership states are inconsistent")

class GroupLockTimeoutException(groupId: String, timeoutMillis: Long) :
    DomainModelException("Could not acquire lock for Group[$groupId] within ${timeoutMillis}ms")
