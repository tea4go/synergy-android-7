package org.synergy.base

fun interface EventJobInterface {
    fun run(event: Event?)
}

interface EventQueueBuffer {
    // get an event, wait for a period of time
    @Throws(InterruptedException::class)
    fun getEvent(timeout: Double): EventData?

    // No timeout
    @Throws(InterruptedException::class)
    fun getEvent(): EventData?

    @Throws(InterruptedException::class)
    fun addEvent(dataID: Int)

    //public EventQueueTimer newTimer (double duration, boolean oneShot);

    val isEmpty: Boolean

    // public void deleteTimer (EventQueueTimer timer);
}

interface EventQueueInterface {
    fun adoptBuffer(eventQueueBuffer: EventQueueBuffer?)

    @Throws(InvalidMessageException::class)
    fun getEvent(timeout: Double): Event?

    fun dispatchEvent(event: Event?): Boolean

    fun addEvent(event: Event?)

    val isEmpty: Boolean
}

interface EventTarget {
    val eventTarget: Any?
}
