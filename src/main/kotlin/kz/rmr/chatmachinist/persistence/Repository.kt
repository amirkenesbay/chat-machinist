package kz.rmr.chatmachinist.persistence

interface Repository<T> {

    fun save(it: T): T
}