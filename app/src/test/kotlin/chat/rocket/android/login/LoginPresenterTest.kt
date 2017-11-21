package chat.rocket.android.login

import chat.rocket.common.model.User
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.only
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test


class LoginPresenterTest {

    private val userRepository: UserRepository = mock()
    private val loginView: LoginView = mock()
    private val successCallback: (User) -> Unit = mock()
    private val errorCallback: (String) -> Unit = mock()
    private lateinit var presenter: LoginPresenter

    @Before
    fun setUp() {
        presenter = LoginPresenter(userRepository, loginView)
    }

    @Test
    fun `login with wrong username and password should show error`() {
        presenter.login("wrongUser", "wrongPass")
        verify(userRepository, only())
                .getByUsernameAndPassword("wrongUser",
                        "wrongPass",
                        successCallback,
                        errorCallback)
        verify(loginView, only()).showError("Unauthorized")
    }
}