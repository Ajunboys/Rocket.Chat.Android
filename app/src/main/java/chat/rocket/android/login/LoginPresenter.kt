package chat.rocket.android.login

class LoginPresenter(val userRepository: UserRepository, val view: LoginView) {

    fun login(username: String, password: String) {
        userRepository.getByUsernameAndPassword(username, password, {
            user -> view.onLoggedIn(user)
        }, {
            error -> view.showError(error)
        })
    }
}