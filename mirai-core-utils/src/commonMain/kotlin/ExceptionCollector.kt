/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public class ExceptionCollector : Sequence<Throwable> {

    // TODO: 2021/4/20 drop last

    public constructor()
    public constructor(initial: Throwable?) {
        collect(initial)
    }

    public constructor(vararg initials: Throwable?) {
        for (initial in initials) {
            collect(initial)
        }
    }

    @Volatile
    private var last: Throwable? = null

    @Synchronized
    public fun collect(e: Throwable?) {
        if (e == null) return
        val last = last
        if (last != null) {
            last.itr().forEach { suppressed ->
                if (suppressed.stackTrace.contentEquals(e.stackTrace)) {
                    // filter out useless duplicates.
                    return
                }
            }
            e.addSuppressed(last)
        }
        this.last = e
    }

    public fun collectGet(e: Throwable?): Throwable {
        this.collect(e)
        return getLast()!!
    }

    /**
     * Alias to [collect] to be used inside [withExceptionCollector]
     */
    public fun collectException(e: Throwable?): Unit = collect(e)

    public fun getLast(): Throwable? = last

    @TerminalOperation // to give it a color for a clearer control flow
    public fun collectThrow(exception: Throwable): Nothing {
        collect(exception)
        throw getLast()!!
    }

    @TerminalOperation
    public fun throwLast(): Nothing {
        throw getLast() ?: error("Internal error: expected at least one exception collected.")
    }

    @DslMarker
    private annotation class TerminalOperation

    private fun Throwable.itr(): Iterator<Throwable> {
        return (sequenceOf(this) + this.suppressed.asSequence().flatMap { it.itr().asSequence() }).iterator()
    }

    override fun iterator(): Iterator<Throwable> {
        val last = getLast() ?: return emptyList<Throwable>().iterator()
        return last.itr()
    }
}

/**
 * Run with a coverage of `throw`. All thrown exceptions will be caught and rethrown with [ExceptionCollector.collectThrow]
 */
public inline fun <R> withExceptionCollector(action: ExceptionCollector.() -> R): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    return ExceptionCollector().withExceptionCollector(action)
}

/**
 * Run with a coverage of `throw`. All thrown exceptions will be caught and rethrown with [ExceptionCollector.collectThrow]
 */
public inline fun <R> ExceptionCollector.withExceptionCollector(action: ExceptionCollector.() -> R): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    this.run {
        try {
            return action()
        } catch (e: Throwable) {
            collectThrow(e)
        }
    }
}