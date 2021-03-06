package com.cloudinary.transformation

import com.cloudinary.cldAssert
import org.junit.Test

class BackgroundTest {
    @Test
    fun testBackground() {
        cldAssert("b_blue", Background.color(Color.BLUE))
        cldAssert("b_rgb:9090ff", Background.color(Color.Rgb("9090ff")))
    }
}