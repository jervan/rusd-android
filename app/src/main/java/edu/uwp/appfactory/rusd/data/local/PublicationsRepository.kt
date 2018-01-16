package edu.uwp.appfactory.rusd.data.local

import edu.uwp.appfactory.rusd.data.model.Publication

/**
 * Interface for Publications repository used for connecting to User Interface
 *
 * Created by dakota on 7/4/17.
 */
interface PublicationsRepository : BaseRepository<Publication> {
    /**
     * Define additional Publication related methods here
     */

    fun initPublications(publications: List<Publication>)
    fun getAllPublications() : List<Publication>?
    fun getSupportPublications() : List<Publication>?
    fun updatePublications()
}