package edu.uwp.appfactory.rusd.data.local

import edu.uwp.appfactory.rusd.data.model.User

/**
 * Interface for User repository used for connecting to User Interface
 *
 * Created by dakota on 8/11/17.
 */
interface UserRepository {

    fun setUser(user: User)
    fun getCurrentUser() : User?
    fun close()

}