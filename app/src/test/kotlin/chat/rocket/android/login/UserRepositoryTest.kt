package chat.rocket.android.login

import chat.rocket.common.model.BaseUser
import chat.rocket.common.model.Token
import chat.rocket.common.model.User
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.core.TokenProvider
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.fabric8.mockwebserver.DefaultMockServer
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class UserRepositoryTest {

    private lateinit var mockServer: DefaultMockServer
    private lateinit var userRepository: UserRepository
    private val tokenProvider: TokenProvider = mock()
    private val authToken = Token("userId", "authToken")
    private lateinit var client: RocketChatClient

    @Before
    fun setUp() {
        mockServer = DefaultMockServer()
        mockServer.start()

        val baseUrl = HttpUrl.parse(mockServer.url("/"))
        val okHttpClient = OkHttpClient()
        client = RocketChatClient.create {
            httpClient = okHttpClient
            restUrl = baseUrl!!
            websocketUrl = "not needed"
            tokenProvider = this@UserRepositoryTest.tokenProvider
            platformLogger = PlatformLogger.NoOpLogger()
        }

        userRepository = UserRepository(client)
        whenever(tokenProvider.get()).thenReturn(authToken)
    }

    @Test
    fun `given wrong username and password return error`() {
        mockServer.expect()
                .post()
                .withPath("/api/v1/login")
                .andReturn(401, LOGIN_ERROR)
                .once()

        var success = false
        var failure = false
        var errMessage = ""
        val latch = CountDownLatch(1)

        userRepository.getByUsernameAndPassword("wrongUser",
                "wrongPass",
        { user ->
            success = true
            latch.countDown()
        },
        { error ->
            failure = true
            errMessage = error
            latch.countDown()
        })
        latch.await()

        success `should be` false
        failure `should be` true
        "Unauthorized" `should equal` errMessage
    }

    @Test
    fun `given valid username and password should return User`() {
        mockServer.expect()
                .post()
                .withPath("/api/v1/login")
                .andReturn(200, LOGIN_SUCCESS)
                .once()

        mockServer.expect()
                .get()
                .withPath("/api/v1/me")
                .andReturn(200, ME_SUCCESS)
                .once()

        var success = false
        var failure = false
        var errMessage = ""
        var validUser: User? = null
        val latch = CountDownLatch(1)

        userRepository.getByUsernameAndPassword("validUsername",
                "validPassword",
                { user ->
                    validUser = user
                    success = true
                    latch.countDown()
                },
                { error ->
                    failure = true
                    errMessage = error
                    latch.countDown()
                })
        latch.await()

        success `should be` true
        failure `should be` false
        errMessage.isEmpty() `should be` true
        assertFalse(validUser == null)
        validUser?.id `should equal` authToken.userId
        validUser?.username `should equal` "validUsername"
        validUser?.active `should equal` true
        validUser?.success `should equal` true
        validUser?.utcOffset `should be` -2
        validUser?.status `should equal` BaseUser.Status.OFFLINE
        validUser?.statusConnection `should equal` BaseUser.Status.OFFLINE
        validUser?.emails?.isNotEmpty() `should be` true
        validUser?.emails?.get(0)?.address `should equal` "validUsername@email.com"
        validUser?.emails?.get(0)?.verified `should be` false
    }
}