package com.chattriggers.ctjs.minecraft.libs.renderer

import com.chattriggers.ctjs.minecraft.libs.ChatLib
import com.chattriggers.ctjs.minecraft.wrappers.Client
import com.chattriggers.ctjs.minecraft.wrappers.Player
import com.chattriggers.ctjs.utils.vec.Vec3f
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMinecraft
import net.minecraft.client.render.Tessellator
import java.util.*
import kotlin.math.*

object Renderer {
    var colorized: Long? = null
    private var retainTransforms = false
    private var drawMode: UGraphics.DrawMode? = null
    private var firstVertex = true
    private var began = false

    private val tessellator = Tessellator.getInstance()
    private val worldRenderer = UGraphics(tessellator.buffer)

    // The currently-active matrix stack
    internal lateinit var matrixStack: UMatrixStack

    // The current partialTicks value
    @JvmStatic
    var partialTicks = 0f
        internal set

    @JvmStatic
    val BLACK = color(0, 0, 0, 255)

    @JvmStatic
    val DARK_BLUE = color(0, 0, 190, 255)

    @JvmStatic
    val DARK_GREEN = color(0, 190, 0, 255)

    @JvmStatic
    val DARK_AQUA = color(0, 190, 190, 255)

    @JvmStatic
    val DARK_RED = color(190, 0, 0, 255)

    @JvmStatic
    val DARK_PURPLE = color(190, 0, 190, 255)

    @JvmStatic
    val GOLD = color(217, 163, 52, 255)

    @JvmStatic
    val GRAY = color(190, 190, 190, 255)

    @JvmStatic
    val DARK_GRAY = color(63, 63, 63, 255)

    @JvmStatic
    val BLUE = color(63, 63, 254, 255)

    @JvmStatic
    val GREEN = color(63, 254, 63, 255)

    @JvmStatic
    val AQUA = color(63, 254, 254, 255)

    @JvmStatic
    val RED = color(254, 63, 63, 255)

    @JvmStatic
    val LIGHT_PURPLE = color(254, 63, 254, 255)

    @JvmStatic
    val YELLOW = color(254, 254, 63, 255)

    @JvmStatic
    val WHITE = color(255, 255, 255, 255)

    @JvmStatic
    fun getColor(color: Int): Long {
        return when (color) {
            0 -> BLACK
            1 -> DARK_BLUE
            2 -> DARK_GREEN
            3 -> DARK_AQUA
            4 -> DARK_RED
            5 -> DARK_PURPLE
            6 -> GOLD
            7 -> GRAY
            8 -> DARK_GRAY
            9 -> BLUE
            10 -> GREEN
            11 -> AQUA
            12 -> RED
            13 -> LIGHT_PURPLE
            14 -> YELLOW
            else -> WHITE
        }
    }

    @JvmStatic
    fun getFontRenderer() = UMinecraft.getFontRenderer()

    @JvmStatic
    fun getRenderManager() = UMinecraft.getMinecraft().worldRenderer

    @JvmStatic
    fun getStringWidth(text: String) = getFontRenderer().getWidth(ChatLib.addColor(text))

    @JvmStatic
    @JvmOverloads
    fun color(red: Long, green: Long, blue: Long, alpha: Long = 255): Long {
        return (alpha.toInt().coerceIn(0, 255) * 0x1000000
            + red.toInt().coerceIn(0, 255) * 0x10000
            + green.toInt().coerceIn(0, 255) * 0x100
            + blue.toInt().coerceIn(0, 255)).toLong()
    }

    @JvmStatic
    @JvmOverloads
    fun getRainbow(step: Float, speed: Float = 1f): Long {
        val red = ((sin(step / speed) + 0.75) * 170).toLong()
        val green = ((sin(step / speed + 2 * PI / 3) + 0.75) * 170).toLong()
        val blue = ((sin(step / speed + 4 * PI / 3) + 0.75) * 170).toLong()
        return color(red, green, blue, 255)
    }

    @JvmStatic
    @JvmOverloads
    fun getRainbowColors(step: Float, speed: Float = 1f): IntArray {
        val red = ((sin(step / speed) + 0.75) * 170).toInt()
        val green = ((sin(step / speed + 2 * PI / 3) + 0.75) * 170).toInt()
        val blue = ((sin(step / speed + 4 * PI / 3) + 0.75) * 170).toInt()
        return intArrayOf(red, green, blue)
    }

    @JvmStatic
    fun retainTransforms(retain: Boolean) {
        retainTransforms = retain
        finishDraw()
    }

    @JvmStatic
    fun disableAlpha() = apply { UGraphics.disableAlpha() }

    @JvmStatic
    fun enableAlpha() = apply { UGraphics.enableAlpha() }

    @JvmStatic
    fun enableLighting() = apply { UGraphics.enableLighting() }

    @JvmStatic
    fun disableLighting() = apply { UGraphics.disableLighting() }

    @JvmStatic
    fun enableDepth() = apply { UGraphics.enableDepth() }

    @JvmStatic
    fun disableDepth() = apply { UGraphics.disableDepth() }

    @JvmStatic
    fun depthFunc(func: Int) = apply { UGraphics.depthFunc(func) }

    @JvmStatic
    fun depthMask(flag: Boolean) = apply { UGraphics.depthMask(flag) }

    @JvmStatic
    fun disableBlend() = apply { UGraphics.disableBlend() }

    @JvmStatic
    fun enableBlend() = apply { UGraphics.enableBlend() }

    @JvmStatic
    fun blendFunc(func: Int) = apply { UGraphics.blendEquation(func) }

    @JvmStatic
    fun tryBlendFuncSeparate(sourceFactor: Int, destFactor: Int, sourceFactorAlpha: Int, destFactorAlpha: Int) = apply {
        UGraphics.tryBlendFuncSeparate(sourceFactor, destFactor, sourceFactorAlpha, destFactorAlpha)
    }

    @JvmStatic
    @JvmOverloads
    fun bindTexture(texture: Image, textureIndex: Int = 0) = apply {
        UGraphics.bindTexture(textureIndex, texture.getTexture().glId)
    }

    @JvmStatic
    fun deleteTexture(texture: Image) = apply {
        UGraphics.deleteTexture(texture.getTexture().glId)
    }

    @JvmStatic
    fun pushMatrix() = apply {
        matrixStack.push()
    }

    @JvmStatic
    fun popMatrix() = apply {
        matrixStack.pop()
    }

    /**
     * Begin drawing with the Renderer with default draw mode of quads and textured
     *
     * @param drawMode the GL draw mode
     * @param textured if the Renderer is textured
     * @return the Renderer to allow for method chaining
     * @see com.chattriggers.ctjs.minecraft.libs.renderer.Shape.setDrawMode
     */
    @JvmStatic
    @JvmOverloads
    fun begin(drawMode: Int = 7, textured: Boolean = true) = apply {
        pushMatrix()
        enableBlend()
        tryBlendFuncSeparate(770, 771, 1, 0)

        translate(-Client.camera.getX().toFloat(), -Client.camera.getY().toFloat(), -Client.camera.getZ().toFloat())

        worldRenderer.beginWithDefaultShader(
            Renderer.drawMode ?: UGraphics.DrawMode.QUADS,
            if (textured) UGraphics.CommonVertexFormats.POSITION_TEXTURE else UGraphics.CommonVertexFormats.POSITION,
        )

        firstVertex = true
        began = true
    }

    /**
     * Sets a new vertex in the Tessellator.
     *
     * @param x the x position
     * @param y the y position
     * @param z the z position
     * @return the Tessellator to allow for method chaining
     */
    @JvmStatic
    fun pos(x: Float, y: Float, z: Float) = apply {
        if (!began)
            begin()
        if (!firstVertex)
            worldRenderer.endVertex()
        worldRenderer.pos(matrixStack, x.toDouble(), y.toDouble(), z.toDouble())
        firstVertex = false
    }

    /**
     * Sets the texture location on the last defined vertex.
     * Use directly after using [Tessellator.pos]
     *
     * @param u the u position in the texture
     * @param v the v position in the texture
     * @return the Tessellator to allow for method chaining
     */
    @JvmStatic
    fun tex(u: Float, v: Float) = apply {
        worldRenderer.tex(u.toDouble(), v.toDouble())
    }

    @JvmStatic
    @JvmOverloads
    fun translate(x: Float, y: Float, z: Float = 0.0F) {
        matrixStack.translate(x, y, z)
    }

    @JvmStatic
    @JvmOverloads
    fun scale(scaleX: Float, scaleY: Float = scaleX) {
        matrixStack.scale(scaleX, scaleY, 1f)
    }

    @JvmStatic
    fun rotate(angle: Float) {
        matrixStack.rotate(angle, 0f, 0f, 1f)
    }

    @JvmStatic
    @JvmOverloads
    fun colorize(red: Float, green: Float, blue: Float, alpha: Float = 1f) {
        colorized = fixAlpha(color(red.toLong(), green.toLong(), blue.toLong(), alpha.toLong()))

        worldRenderer.color(red.coerceIn(0f, 1f), green.coerceIn(0f, 1f), blue.coerceIn(0f, 1f), alpha.coerceIn(0f, 1f))
    }

    @JvmStatic
    fun setDrawMode(drawMode: Int) = apply {
        this.drawMode = UGraphics.DrawMode.fromGl(drawMode)
    }

    @JvmStatic
    fun setDrawMode(drawMode: UGraphics.DrawMode) = apply {
        // TODO: Add UGraphics.DrawMode to providedLibs
        this.drawMode = drawMode
    }

    @JvmStatic
    fun getDrawMode() = drawMode

    @JvmStatic
    fun fixAlpha(color: Long): Long {
        val alpha = color shr 24 and 255
        return if (alpha < 10)
            (color and 0xFF_FF_FF) or 0xA_FF_FF_FF
        else color
    }

    /**
     * Finalizes and draws the Tessellator.
     */
    @JvmStatic
    fun draw() {
        if (!began)
            return

        worldRenderer.endVertex()

        tessellator.draw()

        colorize(1f, 1f, 1f, 1f)

        began = false

        disableBlend()
        popMatrix()
    }

    /**
     * Gets a fixed render position from x, y, and z inputs adjusted with partial ticks
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return the Vec3f position to render at
     */
    @JvmStatic
    fun getRenderPos(x: Float, y: Float, z: Float): Vec3f {
        return Vec3f(
            x - Player.getRenderX().toFloat(),
            y - Player.getRenderY().toFloat(),
            z - Player.getRenderZ().toFloat()
        )
    }

    @JvmStatic
    fun drawRect(color: Long, x: Float, y: Float, width: Float, height: Float) {
        val pos = mutableListOf(x, y, x + width, y + height)
        if (pos[0] > pos[2])
            Collections.swap(pos, 0, 2)
        if (pos[1] > pos[3])
            Collections.swap(pos, 1, 3)

        UGraphics.enableBlend()
        UGraphics.tryBlendFuncSeparate(770, 771, 1, 0)
        doColor(color)

        worldRenderer.beginWithDefaultShader(drawMode ?: UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION)
            .pos(matrixStack, pos[0].toDouble(), pos[3].toDouble(), 0.0).endVertex()
            .pos(matrixStack, pos[2].toDouble(), pos[3].toDouble(), 0.0).endVertex()
            .pos(matrixStack, pos[2].toDouble(), pos[1].toDouble(), 0.0).endVertex()
            .pos(matrixStack, pos[0].toDouble(), pos[1].toDouble(), 0.0).endVertex()
            .drawDirect()

        colorize(1f, 1f, 1f, 1f)
        UGraphics.disableBlend()

        finishDraw()
    }

    // TODO: Remove this and replace with Shape()
    // @JvmStatic
    // @JvmOverloads
    // fun drawShape(color: Long, vararg vertexes: List<Float>, drawMode: Int = 9) {
    //     UGraphics.enableBlend()
    //     UGraphics.tryBlendFuncSeparate(770, 771, 1, 0)
    //     doColor(color)
    //
    //     worldRenderer.beginWithDefaultShader(this.drawMode ?: drawMode, DefaultVertexFormats.POSITION)
    //
    //     if (area(vertexes) >= 0)
    //         vertexes.reverse()
    //
    //     vertexes.forEach {
    //         worldRenderer.pos(it[0].toDouble(), it[1].toDouble(), 0.0).endVertex()
    //     }
    //
    //     tessellator.draw()
    //
    //     GlStateManager.color(1f, 1f, 1f, 1f)
    //     GlStateManager.enableTexture2D()
    //     GlStateManager.disableBlend()
    //
    //     finishDraw()
    // }

    @JvmStatic
    @JvmOverloads
    fun drawLine(
        color: Long,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        thickness: Float,
        drawMode: UGraphics.DrawMode = UGraphics.DrawMode.QUADS,
    ) {
        val theta = -atan2(y2 - y1, x2 - x1)
        val i = sin(theta) * (thickness / 2)
        val j = cos(theta) * (thickness / 2)

        UGraphics.enableBlend()
        UGraphics.tryBlendFuncSeparate(770, 771, 1, 0)
        doColor(color)

        worldRenderer.beginWithDefaultShader(this.drawMode ?: drawMode, UGraphics.CommonVertexFormats.POSITION)
            .pos(matrixStack, (x1 + i).toDouble(), (y1 + j).toDouble(), 0.0).endVertex()
            .pos(matrixStack, (x2 + i).toDouble(), (y2 + j).toDouble(), 0.0).endVertex()
            .pos(matrixStack, (x2 - i).toDouble(), (y2 - j).toDouble(), 0.0).endVertex()
            .pos(matrixStack, (x1 - i).toDouble(), (y1 - j).toDouble(), 0.0).endVertex()
            .drawDirect()

        colorize(1f, 1f, 1f, 1f)
        UGraphics.disableBlend()

        finishDraw()
    }

    @JvmStatic
    @JvmOverloads
    fun drawCircle(
        color: Long,
        x: Float,
        y: Float,
        radius: Float,
        steps: Int,
        drawMode: UGraphics.DrawMode = UGraphics.DrawMode.TRIANGLE_STRIP,
    ) {
        val theta = 2 * PI / steps
        val cos = cos(theta).toFloat()
        val sin = sin(theta).toFloat()

        var xHolder: Float
        var circleX = 1f
        var circleY = 0f

        UGraphics.enableBlend()
        UGraphics.tryBlendFuncSeparate(770, 771, 1, 0)
        doColor(color)

        worldRenderer.beginWithDefaultShader(this.drawMode ?: drawMode, UGraphics.CommonVertexFormats.POSITION)

        for (i in 0..steps) {
            worldRenderer.pos(matrixStack, x.toDouble(), y.toDouble(), 0.0).endVertex()
            worldRenderer.pos(matrixStack, (circleX * radius + x).toDouble(), (circleY * radius + y).toDouble(), 0.0).endVertex()
            xHolder = circleX
            circleX = cos * circleX - sin * circleY
            circleY = sin * xHolder + cos * circleY
            worldRenderer.pos(matrixStack, (circleX * radius + x).toDouble(), (circleY * radius + y).toDouble(), 0.0).endVertex()
        }

        worldRenderer.drawDirect()

        colorize(1f, 1f, 1f, 1f)
        UGraphics.disableBlend()

        finishDraw()
    }

    @JvmOverloads
    @JvmStatic
    fun drawString(text: String, x: Float, y: Float, shadow: Boolean = false) {
        val fr = getFontRenderer()
        var newY = y

        ChatLib.addColor(text).split("\n").forEach {
            // TODO: Support drawWithOutline
            if (shadow) {
                fr.drawWithShadow(matrixStack.toMC(), it, x, newY, colorized?.toInt() ?: WHITE.toInt())
            } else {
                fr.draw(matrixStack.toMC(), it, x, newY, colorized?.toInt() ?: WHITE.toInt())
            }

            newY += fr.fontHeight
        }

        finishDraw()
    }

    @JvmStatic
    fun drawStringWithShadow(text: String, x: Float, y: Float) = drawString(text, x, y, shadow = true)

    // TODO:
    // @JvmStatic
    // fun drawImage(image: Image, x: Double, y: Double, width: Double, height: Double) {
    //     if (colorized == null)
    //         GlStateManager.color(1f, 1f, 1f, 1f)
    //     GlStateManager.enableBlend()
    //     GlStateManager.scale(1f, 1f, 50f)
    //     GlStateManager.bindTexture(image.getTexture().glTextureId)
    //     GlStateManager.enableTexture2D()
    //
    //     worldRenderer.begin(drawMode ?: 7, DefaultVertexFormats.POSITION_TEX)
    //
    //     worldRenderer.pos(x, y + height, 0.0).tex(0.0, 1.0).endVertex()
    //     worldRenderer.pos(x + width, y + height, 0.0).tex(1.0, 1.0).endVertex()
    //     worldRenderer.pos(x + width, y, 0.0).tex(1.0, 0.0).endVertex()
    //     worldRenderer.pos(x, y, 0.0).tex(0.0, 0.0).endVertex()
    //     tessellator.draw()
    //
    //     finishDraw()
    // }
    //
    // private val renderManager = getRenderManager()
    // private val slimCTRenderPlayer = CTRenderPlayer(renderManager, true)
    // private val normalCTRenderPlayer = CTRenderPlayer(renderManager, false)
    //
    // @JvmStatic
    // @JvmOverloads
    // fun drawPlayer(
    //     player: Any,
    //     x: Int,
    //     y: Int,
    //     rotate: Boolean = false,
    //     showNametag: Boolean = false,
    //     showArmor: Boolean = true,
    //     showCape: Boolean = true,
    //     showHeldItem: Boolean = true,
    //     showArrows: Boolean = true
    // ) {
    //     val mouseX = -30f
    //     val mouseY = 0f
    //
    //     val ent = if (player is PlayerMP) player.player else Player.getPlayer()!!
    //
    //     GlStateManager.enableColorMaterial()
    //     RenderHelper.enableStandardItemLighting()
    //
    //     val f = ent.renderYawOffset
    //     val f1 = ent.rotationYaw
    //     val f2 = ent.rotationPitch
    //     val f3 = ent.prevRotationYawHead
    //     val f4 = ent.rotationYawHead
    //
    //     translate(x.toFloat(), y.toFloat(), 50.0f)
    //     GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f)
    //     GlStateManager.rotate(45.0f, 0.0f, 1.0f, 0.0f)
    //     GlStateManager.rotate(-45.0f, 0.0f, 1.0f, 0.0f)
    //     GlStateManager.rotate(-atan(mouseY / 40.0f) * 20.0f, 1.0f, 0.0f, 0.0f)
    //     scale(-1f, 1f)
    //     if (!rotate) {
    //         ent.renderYawOffset = atan(mouseX / 40.0f) * 20.0f
    //         ent.rotationYaw = atan(mouseX / 40.0f) * 40.0f
    //         ent.rotationPitch = -atan(mouseY / 40.0f) * 20.0f
    //         ent.rotationYawHead = ent.rotationYaw
    //         ent.prevRotationYawHead = ent.rotationYaw
    //     }
    //
    //     renderManager.playerViewY = 180.0f
    //     renderManager.isRenderShadow = false
    //     //#if MC<=10809
    //     val isSmall = (ent as AbstractClientPlayer).skinType == "slim"
    //     val ctRenderPlayer = if (isSmall) slimCTRenderPlayer else normalCTRenderPlayer
    //
    //     ctRenderPlayer.setOptions(showNametag, showArmor, showCape, showHeldItem, showArrows)
    //     ctRenderPlayer.doRender(ent, 0.0, 0.0, 0.0, 0.0f, 1.0f)
    //     //#else
    //     //$$ renderManager.doRenderEntity(ent, 0.0, 0.0, 0.0, 0.0F, 1.0F, false)
    //     //#endif
    //     renderManager.isRenderShadow = true
    //
    //     ent.renderYawOffset = f
    //     ent.rotationYaw = f1
    //     ent.rotationPitch = f2
    //     ent.prevRotationYawHead = f3
    //     ent.rotationYawHead = f4
    //
    //     RenderHelper.disableStandardItemLighting()
    //     GlStateManager.disableRescaleNormal()
    //     GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
    //     GlStateManager.disableTexture2D()
    //     GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
    //
    //     finishDraw()
    // }

    internal fun doColor(longColor: Long) {
        val color = longColor.toInt()

        if (colorized == null) {
            val a = (color shr 24 and 255).toFloat() / 255.0f
            val r = (color shr 16 and 255).toFloat() / 255.0f
            val g = (color shr 8 and 255).toFloat() / 255.0f
            val b = (color and 255).toFloat() / 255.0f
            colorize(r, g, b, a)
        }
    }

    @JvmStatic
    fun finishDraw() {
        if (!retainTransforms) {
            colorized = null
            drawMode = null
            matrixStack.pop()
            matrixStack.push()
        }
    }

    private fun area(points: Array<out List<Float>>): Float {
        var area = 0f

        for (i in points.indices) {
            val (x1, y1) = points[i]
            val (x2, y2) = points[(i + 1) % points.size]

            area += x1 * y2 - x2 * y1
        }

        return area / 2
    }

    object screen {
        @JvmStatic
        fun getWidth(): Int = UMinecraft.getMinecraft().window.scaledWidth

        @JvmStatic
        fun getHeight(): Int = UMinecraft.getMinecraft().window.scaledHeight

        @JvmStatic
        fun getScale(): Double = UMinecraft.getMinecraft().window.scaleFactor
    }
}
