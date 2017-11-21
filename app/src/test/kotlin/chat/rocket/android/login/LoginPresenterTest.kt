package chat.rocket.android.login

import chat.rocket.common.model.User
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test

class LoginPresenterTest {

    private val userRepository: UserRepository = mock()
    private val loginView: LoginView = mock()
    private val user: User = mock()
    private val success = argumentCaptor<(User) -> Unit>()
    private val failure = argumentCaptor<(String) -> Unit>()
    private lateinit var presenter: LoginPresenter

    @Before
    fun setUp() {
        presenter = LoginPresenter(userRepository, loginView)
    }

    @Test
    fun `login with wrong username and password should show error`() {
        whenever(userRepository.getByUsernameAndPassword(any(), any(),
                success.capture(), failure.capture()))
                .then { failure.firstValue.invoke("Unauthorized") }

        presenter.login("wrongUser", "wrongPass")

        verify(userRepository, only())
                .getByUsernameAndPassword(any(), any(), any(), any())
        verify(loginView, only()).showError("Unauthorized")
    }

    @Test
    fun `login with valid username and password should call onLoggedIn`() {
        whenever(userRepository.getByUsernameAndPassword(any(), any(),
                success.capture(), failure.capture()))
                .then { success.firstValue.invoke(user) }

        presenter.login("validUser", "validPass")

        verify(userRepository, only())
                .getByUsernameAndPassword(any(), any(), any(), any())
        verify(loginView, only()).onLoggedIn(user)
    }
}