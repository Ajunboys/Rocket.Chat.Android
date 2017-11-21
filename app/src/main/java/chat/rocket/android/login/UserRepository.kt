package chat.rocket.android.login

import chat.rocket.common.model.User
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.me

class UserRepository(val client: RocketChatClient) {

    fun getByUsernameAndPassword(username: String,
                                 password: String,
                                 onSuccess: (User) -> Unit,
                                 onFailure: (String) -> Unit) {
        client.login(username, password, {
            client.me(onSuccess, { onFailure.invoke(it.message ?: "me: Connection Error") })
        }, { error ->
            onFailure(error.message ?: "login: Connection Error")
        })
    }
}