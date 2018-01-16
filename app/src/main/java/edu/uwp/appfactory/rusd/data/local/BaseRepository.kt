package edu.uwp.appfactory.rusd.data.local

/**
 * Base interface for Repositories
 *
 * Created by dakota on 6/30/17.
 */

//Define common repository methods here
interface BaseRepository<in T> {
    fun add(item: T)
    fun remove(item: T)
    fun update(item: T)
    fun close()
}