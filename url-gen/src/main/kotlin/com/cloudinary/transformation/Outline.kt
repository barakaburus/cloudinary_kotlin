package com.cloudinary.transformation

import com.cloudinary.transformation.effect.innerEffectAction
import com.cloudinary.util.cldRanged

// TODO simplify
class Outline(private val action: Action) : Action by action {

    class Builder private constructor(
        private var mode: OutlineMode? = null,
        private var color: Color? = null,
        private var width: Int? = null,
        private var blur: Int? = null
    ) : TransformationComponentBuilder {
        constructor() : this(null)

        fun mode(mode: OutlineMode) = apply { this.mode = mode }
        fun color(color: Color) = apply { this.color = color }
        fun color(color: String) = apply { this.color = Color.parseString(color) }
        fun width(width: Int) = apply { this.width = width }

        fun blur(blur: Int) = apply { this.blur = blur }
        override fun build(): Outline {
            val values = listOfNotNull(mode, width?.cldRanged(1, 100), blur?.cldRanged(0, 200))
            val params = listOfNotNull(color?.cldAsColor())

            return Outline(innerEffectAction("outline", *((values + params).toTypedArray())))
        }
    }
}

enum class OutlineMode(internal val value: String) {
    INNER("inner"),
    INNER_FILL("inner_fill"),
    OUTER("outer"),
    FILL("fill"), ;

    override fun toString(): String {
        return value
    }
}