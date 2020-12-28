package gbr.utils.models

interface Workable<T> {
    fun work(t: T)
}