package com.revolut.kompot.sample.utils

private typealias ArgumentSupplier<A> = () -> A

open class LazySingletonHolder<out T, in A>(
    private var instanceCreator: (A) -> T
) {

    @Volatile
    private var savedInstance: T? = null

    @Volatile
    private lateinit var argumentSupplier: ArgumentSupplier<A>

    fun init(argumentSupplier: ArgumentSupplier<A>) {
        this.argumentSupplier = argumentSupplier
        onInitialised()
    }

    open fun onInitialised() = Unit

    /**
     * Be sure to call this when instance has been initialized
     */
    val instance: T
        get() {
            val i = savedInstance
            if (i != null) {
                return i
            }

            return synchronized(this) {
                val i2 = savedInstance
                if (i2 != null) {
                    i2
                } else {
                    val arguments = argumentSupplier.invoke()
                    val created = instanceCreator(arguments)
                    savedInstance = created
                    created
                }
            }
        }

    @Synchronized
    fun clear() {
        savedInstance = null
    }

    @Synchronized
    open fun clearData() = Unit

}