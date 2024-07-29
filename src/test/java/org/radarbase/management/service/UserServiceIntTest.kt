package org.radarbase.management.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.hibernate.envers.AuditReaderFactory
import org.hibernate.envers.query.AuditEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.domain.Authority
import org.radarbase.management.domain.Role
import org.radarbase.management.domain.User
import org.radarbase.management.domain.audit.CustomRevisionEntity
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.repository.filters.UserFilter
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.dto.UserDTO
import org.radarbase.management.service.mapper.UserMapper
import org.radarbase.management.web.rest.TestUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.time.Period
import java.time.ZonedDateTime
import java.util.*
import java.util.function.Consumer
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@Transactional
class UserServiceIntTest(
    @Autowired private val userService: UserService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val userMapper: UserMapper,
    @Autowired private val revisionService: RevisionService,

    @Autowired private val entityManagerFactory: EntityManagerFactory,
    @Autowired private val passwordService: PasswordService,
) {
    private lateinit var entityManager: EntityManager
    private lateinit var userDto: UserDTO

    @BeforeEach
    fun setUp() {
        entityManager = entityManagerFactory.createEntityManager(
            entityManagerFactory.properties
        )
        userDto = userMapper.userToUserDTO(createEntity(passwordService))!!

        userRepository.findOneByLogin(userDto.login)?.let { userRepository.delete(it)}
    }

    @Test
    fun assertThatUserMustExistToResetPassword() {
        var maybeUser = userService.requestPasswordReset("john.doe@localhost")
        Assertions.assertThat(maybeUser).isNull()
        maybeUser = userService.requestPasswordReset("admin@localhost")
        Assertions.assertThat(maybeUser).isNotNull()
        Assertions.assertThat(maybeUser?.email).isEqualTo("admin@localhost")
        Assertions.assertThat(maybeUser?.resetDate).isNotNull()
        Assertions.assertThat(maybeUser?.resetKey).isNotNull()
    }

      //TODO this test should fail, remove?
//    @Test
//    @Throws(NotAuthorizedException::class)
//    fun assertThatOnlyActivatedUserCanRequestPasswordReset() {
//        runBlocking {
//            val user = userService.createUser(userDto)
//            val maybeUser = userService.requestPasswordReset(
//                userDto.email!!
//            )
//            Assertions.assertThat(maybeUser).isNull()
//            userRepository.delete(user)
//        }
//    }

    @Test
    @Throws(NotAuthorizedException::class)
    fun assertThatResetKeyMustNotBeOlderThan24Hours() {
        runBlocking {
            val user = userService.createUser(userDto)
            val daysAgo = ZonedDateTime.now().minusHours(25)
            val resetKey = passwordService.generateResetKey()
            user.activated = true
            user.resetDate = daysAgo
            user.resetKey = resetKey
            userService.updateUser(userMapper.userToUserDTO(user)!!)
            val maybeUser = userService.completePasswordReset(
                "johndoe2",
                user.resetKey!!
            )
            Assertions.assertThat(maybeUser).isNull()
            userService.deleteUser(user.login!!)
        }
    }

    //TODO this functionality will be removed, remove test
//    @Test
//    @Throws(NotAuthorizedException::class)
//    fun assertThatResetKeyMustBeValid() {
//        runBlocking {
//            val user = userService.createUser(userDto)
//            val daysAgo = ZonedDateTime.now().minusHours(25)
//            user.activated = true
//            user.resetDate = daysAgo
//            user.resetKey = "1234"
//            userRepository.save(user)
//            val maybeUser = userService.completePasswordReset(
//                "johndoe2",
//                user.resetKey!!
//            )
//            Assertions.assertThat(maybeUser).isNull()
//            userService.deleteUser(user.login!!)
//        }
//    }

    //TODO this functionality will be removed, remove test
//    @Test
//    @Throws(NotAuthorizedException::class)
//    fun assertThatUserCanResetPassword() {
//        runBlocking {
//            val user = userService.createUser(userDto)
//            val oldPassword = user.password
//            val daysAgo = ZonedDateTime.now().minusHours(2)
//            val resetKey = passwordService.generateResetKey()
//            user.activated = true
//            user.resetDate = daysAgo
//            user.resetKey = resetKey
//            userService.updateUser(userMapper.userToUserDTO(user)!!)
//            val maybeUser = userService.completePasswordReset(
//                "johndoe2",
//                user.resetKey!!
//            )
//            Assertions.assertThat(maybeUser).isNotNull()
//            Assertions.assertThat(maybeUser?.resetDate).isNull()
//            Assertions.assertThat(maybeUser?.resetKey).isNull()
//            Assertions.assertThat(maybeUser?.password).isNotEqualTo(oldPassword)
//            userService.deleteUser(user.login!!)
//        }
//    }

    @Test
    fun testFindNotActivatedUsersByCreationDateBefore() {
        val expiredUser = addExpiredUser(userRepository)
        TestUtil.commitTransactionAndStartNew()

        // Update the timestamp of the revision, so it appears to have been created 5 days ago
        val expDateTime = ZonedDateTime.now().minus(Period.ofDays(5)).withNano(0)
        val auditReader = AuditReaderFactory.get(entityManager)
        val firstRevision = auditReader.createQuery()
            .forRevisionsOfEntity(expiredUser.javaClass, false, true)
            .add(AuditEntity.id().eq(expiredUser.id))
            .add(
                AuditEntity.revisionNumber().minimize()
                    .computeAggregationInInstanceContext()
            )
            .singleResult as Array<*>
        val first = firstRevision[1] as CustomRevisionEntity
        first.timestamp = Date.from(expDateTime.toInstant())
        entityManager.joinTransaction()
        val updated = entityManager.merge(first)
        TestUtil.commitTransactionAndStartNew()
        Assertions.assertThat(updated.timestamp).isEqualTo(first.timestamp)
        Assertions.assertThat(updated.timestamp).isEqualTo(Date.from(expDateTime.toInstant()))

        // make sure when we reload the expired user we have the new created date
        Assertions.assertThat(revisionService.getAuditInfo(expiredUser).createdAt).isEqualTo(expDateTime)

        // Now we know we have an 'old' user in the database, we can test our deletion method
        val numUsers = userRepository.findAll().size
        userService.removeNotActivatedUsers()
        val users = userRepository.findAll()
        // make sure have actually deleted some users, otherwise this test is pointless
        Assertions.assertThat(numUsers - users.size).isEqualTo(1)
        // remaining users should be either activated or have a created date less then 3 days ago
        val cutoff = ZonedDateTime.now().minus(Period.ofDays(3))
        users.forEach(Consumer { u: User ->
            Assertions.assertThat(
                u.activated || revisionService.getAuditInfo(u)
                    .createdAt!!.isAfter(cutoff)
            ).isTrue()
        })
        // commit the deletion, otherwise the deletion will be rolled back
        TestUtil.commitTransactionAndStartNew()
    }

    @Test
    fun assertThatAnonymousUserIsNotGet() {
        val pageable = PageRequest.of(0, userRepository.count().toInt())
        val allManagedUsers = userService.findUsers(
            UserFilter(), pageable,
            false
        )
        Assertions.assertThat(
            allManagedUsers!!.content.stream()
                .noneMatch { user: UserDTO -> Constants.ANONYMOUS_USER == user.login })
            .isTrue()
    }

    /**
     * Create an expired user, save it and return the saved object.
     * @param userRepository The UserRepository that will be used to save the object
     * @return the saved object
     */
    fun addExpiredUser(userRepository: UserRepository?): User {
        val adminRole = Role()
        adminRole.id = 1L
        adminRole.authority = Authority(RoleAuthority.SYS_ADMIN)
        adminRole.project = null
        val user = User()
        user.setLogin("expired")
        user.email = "expired@expired"
        user.firstName = "ex"
        user.lastName = "pired"
        user.roles = mutableSetOf(adminRole)
        user.activated = false
        user.password = passwordService.generateEncodedPassword()
        return userRepository!!.save(user)
    }

    companion object {
        const val DEFAULT_LOGIN = "johndoe"
        const val UPDATED_LOGIN = "jhipster"
        const val DEFAULT_PASSWORD = "passjohndoe"
        const val UPDATED_PASSWORD = "passjhipster"
        const val DEFAULT_EMAIL = "johndoe@localhost"
        const val UPDATED_EMAIL = "jhipster@localhost"
        const val DEFAULT_FIRSTNAME = "john"
        const val UPDATED_FIRSTNAME = "jhipsterFirstName"
        const val DEFAULT_LASTNAME = "doe"
        const val UPDATED_LASTNAME = "jhipsterLastName"
        const val DEFAULT_LANGKEY = "en"
        const val UPDATED_LANGKEY = "fr"

        /**
         * Create a User.
         *
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which has a required relationship to the User entity.
         */
        fun createEntity(passwordService: PasswordService?): User {
            val user = User()
            user.setLogin(DEFAULT_LOGIN)
            user.password = passwordService!!.generateEncodedPassword()
            user.activated = true
            user.email = DEFAULT_EMAIL
            user.firstName = DEFAULT_FIRSTNAME
            user.lastName = DEFAULT_LASTNAME
            user.langKey = DEFAULT_LANGKEY
            return user
        }
    }
}
