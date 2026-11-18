package com.example.engine

import com.example.model.HairStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MeshData(
    val vertices: FloatArray, // interleaved: x, y, z, nx, ny, nz
    val indices: ShortArray,
    val isLines: Boolean = false
)

object MeshGenerator {

    private class Builder {
        private val verts = ArrayList<Float>(1024)
        private val idx = ArrayList<Short>(1024)

        fun addVertex(x: Float, y: Float, z: Float, nx: Float, ny: Float, nz: Float): Short {
            val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(1e-5f)
            val index = (verts.size / 6).toShort()
            verts.add(x)
            verts.add(y)
            verts.add(z)
            verts.add(nx / len)
            verts.add(ny / len)
            verts.add(nz / len)
            return index
        }

        fun addTri(a: Short, b: Short, c: Short) {
            idx.add(a)
            idx.add(b)
            idx.add(c)
        }

        fun addQuad(a: Short, b: Short, c: Short, d: Short) {
            addTri(a, b, c)
            addTri(a, c, d)
        }

        fun addBox(cx: Float, cy: Float, cz: Float, sx: Float, sy: Float, sz: Float) {
            val hx = sx * 0.5f
            val hy = sy * 0.5f
            val hz = sz * 0.5f

            fun face(
                p0: Triple<Float, Float, Float>,
                p1: Triple<Float, Float, Float>,
                p2: Triple<Float, Float, Float>,
                p3: Triple<Float, Float, Float>,
                nx: Float, ny: Float, nz: Float
            ) {
                val i0 = addVertex(cx + p0.first, cy + p0.second, cz + p0.third, nx, ny, nz)
                val i1 = addVertex(cx + p1.first, cy + p1.second, cz + p1.third, nx, ny, nz)
                val i2 = addVertex(cx + p2.first, cy + p2.second, cz + p2.third, nx, ny, nz)
                val i3 = addVertex(cx + p3.first, cy + p3.second, cz + p3.third, nx, ny, nz)
                addQuad(i0, i1, i2, i3)
            }

            // Front (+Z)
            face(Triple(-hx, -hy, hz), Triple(hx, -hy, hz), Triple(hx, hy, hz), Triple(-hx, hy, hz), 0f, 0f, 1f)
            // Back (-Z)
            face(Triple(hx, -hy, -hz), Triple(-hx, -hy, -hz), Triple(-hx, hy, -hz), Triple(hx, hy, -hz), 0f, 0f, -1f)
            // Right (+X)
            face(Triple(hx, -hy, hz), Triple(hx, -hy, -hz), Triple(hx, hy, -hz), Triple(hx, hy, hz), 1f, 0f, 0f)
            // Left (-X)
            face(Triple(-hx, -hy, -hz), Triple(-hx, -hy, hz), Triple(-hx, hy, hz), Triple(-hx, hy, -hz), -1f, 0f, 0f)
            // Top (+Y)
            face(Triple(-hx, hy, hz), Triple(hx, hy, hz), Triple(hx, hy, -hz), Triple(-hx, hy, -hz), 0f, 1f, 0f)
            // Bottom (-Y)
            face(Triple(-hx, -hy, -hz), Triple(hx, -hy, -hz), Triple(hx, -hy, hz), Triple(-hx, -hy, hz), 0f, -1f, 0f)
        }

        fun addEllipsoid(cx: Float, cy: Float, cz: Float, rx: Float, ry: Float, rz: Float, lat: Int = 14, lon: Int = 20) {
            val base = (verts.size / 6)
            for (i in 0..lat) {
                val theta = i * PI / lat
                val sinT = sin(theta).toFloat()
                val cosT = cos(theta).toFloat()
                for (j in 0..lon) {
                    val phi = j * 2.0 * PI / lon
                    val sinP = sin(phi).toFloat()
                    val cosP = cos(phi).toFloat()
                    val x = sinT * cosP
                    val y = cosT
                    val z = sinT * sinP
                    addVertex(cx + x * rx, cy + y * ry, cz + z * rz, x / rx, y / ry, z / rz)
                }
            }
            for (i in 0 until lat) {
                for (j in 0 until lon) {
                    val first = (base + i * (lon + 1) + j).toShort()
                    val second = (first + lon + 1).toShort()
                    addTri(first, second, (first + 1).toShort())
                    addTri(second, (second + 1).toShort(), (first + 1).toShort())
                }
            }
        }

        fun addTaperedLimb(
            x0: Float, y0: Float, z0: Float,
            x1: Float, y1: Float, z1: Float,
            r0: Float, r1: Float,
            segments: Int = 16
        ) {
            // Generates a smooth tapered cylinder along Y from (0,0,0) to (0, len, 0) when x0=x1=z0=z1=0,
            // or oriented along arbitrary axis
            val dx = x1 - x0
            val dy = y1 - y0
            val dz = z1 - z0
            val len = sqrt(dx * dx + dy * dy + dz * dz).coerceAtLeast(1e-5f)
            val ux = dx / len
            val uy = dy / len
            val uz = dz / len

            // Pick orthogonal basis
            val refX = if (kotlin.math.abs(uy) < 0.9f) 0f else 1f
            val refY = if (kotlin.math.abs(uy) < 0.9f) 1f else 0f
            val refZ = 0f
            // tangent = normalize(cross(u, ref))
            var tx = uy * refZ - uz * refY
            var ty = uz * refX - ux * refZ
            var tz = ux * refY - uy * refX
            val tLen = sqrt(tx * tx + ty * ty + tz * tz).coerceAtLeast(1e-5f)
            tx /= tLen; ty /= tLen; tz /= tLen

            // bitangent = cross(u, t)
            val bx = uy * tz - uz * ty
            val by = uz * tx - ux * tz
            val bz = ux * ty - uy * tx

            val base = verts.size / 6
            for (ring in 0..1) {
                val r = if (ring == 0) r0 else r1
                val px = if (ring == 0) x0 else x1
                val py = if (ring == 0) y0 else y1
                val pz = if (ring == 0) z0 else z1
                for (i in 0..segments) {
                    val a = i * 2.0 * PI / segments
                    val ca = cos(a).toFloat()
                    val sa = sin(a).toFloat()
                    val nx = tx * ca + bx * sa
                    val ny = ty * ca + by * sa
                    val nz = tz * ca + bz * sa
                    addVertex(px + nx * r, py + ny * r, pz + nz * r, nx, ny, nz)
                }
            }
            for (i in 0 until segments) {
                val i0 = (base + i).toShort()
                val i1 = (base + i + 1).toShort()
                val i2 = (base + segments + 1 + i).toShort()
                val i3 = (base + segments + 2 + i).toShort()
                addTri(i0, i2, i1)
                addTri(i1, i2, i3)
            }
            // Caps
            val botCenter = addVertex(x0, y0, z0, -ux, -uy, -uz)
            val topCenter = addVertex(x1, y1, z1, ux, uy, uz)
            for (i in 0 until segments) {
                val a = i * 2.0 * PI / segments
                val aNext = (i + 1) * 2.0 * PI / segments
                val v0 = addVertex(
                    x0 + (tx * cos(a).toFloat() + bx * sin(a).toFloat()) * r0,
                    y0 + (ty * cos(a).toFloat() + by * sin(a).toFloat()) * r0,
                    z0 + (tz * cos(a).toFloat() + bz * sin(a).toFloat()) * r0,
                    -ux, -uy, -uz
                )
                val v1 = addVertex(
                    x0 + (tx * cos(aNext).toFloat() + bx * sin(aNext).toFloat()) * r0,
                    y0 + (ty * cos(aNext).toFloat() + by * sin(aNext).toFloat()) * r0,
                    z0 + (tz * cos(aNext).toFloat() + bz * sin(aNext).toFloat()) * r0,
                    -ux, -uy, -uz
                )
                addTri(botCenter, v0, v1)

                val t0 = addVertex(
                    x1 + (tx * cos(a).toFloat() + bx * sin(a).toFloat()) * r1,
                    y1 + (ty * cos(a).toFloat() + by * sin(a).toFloat()) * r1,
                    z1 + (tz * cos(a).toFloat() + bz * sin(a).toFloat()) * r1,
                    ux, uy, uz
                )
                val t1 = addVertex(
                    x1 + (tx * cos(aNext).toFloat() + bx * sin(aNext).toFloat()) * r1,
                    y1 + (ty * cos(aNext).toFloat() + by * sin(aNext).toFloat()) * r1,
                    z1 + (tz * cos(aNext).toFloat() + bz * sin(aNext).toFloat()) * r1,
                    ux, uy, uz
                )
                addTri(topCenter, t1, t0)
            }
        }

        fun addTorus(
            cx: Float, cy: Float, cz: Float,
            majorR: Float, minorR: Float,
            majorSegs: Int = 36, minorSegs: Int = 14,
            axis: Int = 1 // 0=X-normal, 1=Y-normal, 2=Z-normal
        ) {
            val base = verts.size / 6
            for (i in 0..majorSegs) {
                val u = i * 2.0 * PI / majorSegs
                val cu = cos(u).toFloat()
                val su = sin(u).toFloat()
                for (j in 0..minorSegs) {
                    val v = j * 2.0 * PI / minorSegs
                    val cv = cos(v).toFloat()
                    val sv = sin(v).toFloat()
                    val r = majorR + minorR * cv
                    val px = r * cu
                    val pz = r * su
                    val py = minorR * sv
                    val nx = cv * cu
                    val nz = cv * su
                    val ny = sv
                    when (axis) {
                        0 -> addVertex(cx + py, cy + px, cz + pz, ny, nx, nz)
                        1 -> addVertex(cx + px, cy + py, cz + pz, nx, ny, nz)
                        else -> addVertex(cx + px, cy + pz, cz + py, nx, nz, ny)
                    }
                }
            }
            for (i in 0 until majorSegs) {
                for (j in 0 until minorSegs) {
                    val a = (base + i * (minorSegs + 1) + j).toShort()
                    val b = (a + minorSegs + 1).toShort()
                    addTri(a, b, (a + 1).toShort())
                    addTri(b, (b + 1).toShort(), (a + 1).toShort())
                }
            }
        }

        fun build(isLines: Boolean = false): MeshData =
            MeshData(verts.toFloatArray(), idx.toShortArray(), isLines)
    }

    fun sphere(radius: Float = 0.5f): MeshData = Builder().apply {
        addEllipsoid(0f, 0f, 0f, radius, radius, radius, 18, 26)
    }.build()

    fun box(sx: Float = 1f, sy: Float = 1f, sz: Float = 1f): MeshData = Builder().apply {
        addBox(0f, 0f, 0f, sx, sy, sz)
    }.build()

    fun cylinder(rTop: Float = 0.45f, rBot: Float = 0.45f, h: Float = 1f, segs: Int = 24): MeshData = Builder().apply {
        addTaperedLimb(0f, -h * 0.5f, 0f, 0f, h * 0.5f, 0f, rBot, rTop, segs)
    }.build()

    fun cone(): MeshData = cylinder(rTop = 0.005f, rBot = 0.5f, h = 1.0f, segs = 24)

    fun hexPrism(): MeshData = cylinder(rTop = 0.5f, rBot = 0.5f, h = 0.9f, segs = 6)

    fun pyramid(): MeshData = cylinder(rTop = 0.005f, rBot = 0.6f, h = 1.0f, segs = 4)

    fun rectangle(): MeshData = box(1.2f, 0.7f, 0.2f)

    fun wall(): MeshData = box(1.8f, 1.8f, 0.16f)

    fun stairs(): MeshData = Builder().apply {
        val steps = 5
        val w = 1.0f
        val totalH = 1.0f
        val totalD = 1.0f
        val stepH = totalH / steps
        val stepD = totalD / steps
        for (i in 0 until steps) {
            val h = stepH * (i + 1)
            val cy = -totalH * 0.5f + h * 0.5f
            val cz = -totalD * 0.5f + stepD * (i + 0.5f)
            addBox(0f, cy, cz, w, h, stepD)
        }
    }.build()

    fun torus(): MeshData = Builder().apply {
        addTorus(0f, 0f, 0f, 0.42f, 0.14f, 36, 16, axis = 1)
    }.build()

    fun chair(): MeshData = Builder().apply {
        // Resting height 0.90 (-0.45 to +0.45), backrest on -Z so seated characters face +Z
        // Seat cushion at y = -0.02
        addBox(0f, -0.01f, 0.02f, 0.48f, 0.06f, 0.46f)
        // Four tapered legs from floor (-0.45) to seat (-0.04)
        addTaperedLimb(-0.19f, -0.45f, -0.17f, -0.18f, -0.03f, -0.16f, 0.022f, 0.028f, 10)
        addTaperedLimb(0.19f, -0.45f, -0.17f, 0.18f, -0.03f, -0.16f, 0.022f, 0.028f, 10)
        addTaperedLimb(-0.19f, -0.45f, 0.20f, -0.18f, -0.03f, 0.19f, 0.022f, 0.028f, 10)
        addTaperedLimb(0.19f, -0.45f, 0.20f, 0.18f, -0.03f, 0.19f, 0.022f, 0.028f, 10)
        // Backrest uprights + curved top rail
        addTaperedLimb(-0.18f, 0.02f, -0.18f, -0.19f, 0.44f, -0.22f, 0.024f, 0.020f, 10)
        addTaperedLimb(0.18f, 0.02f, -0.18f, 0.19f, 0.44f, -0.22f, 0.024f, 0.020f, 10)
        addBox(0f, 0.30f, -0.205f, 0.44f, 0.24f, 0.05f)
    }.build()

    fun stool(): MeshData = Builder().apply {
        // Total height 0.72 (-0.36 to +0.36)
        addTaperedLimb(0f, 0.26f, 0f, 0f, 0.36f, 0f, 0.21f, 0.20f, 24)
        // 4 splayed legs
        addTaperedLimb(-0.19f, -0.36f, -0.19f, -0.11f, 0.27f, -0.11f, 0.020f, 0.024f, 10)
        addTaperedLimb(0.19f, -0.36f, -0.19f, 0.11f, 0.27f, -0.11f, 0.020f, 0.024f, 10)
        addTaperedLimb(-0.19f, -0.36f, 0.19f, -0.11f, 0.27f, 0.11f, 0.020f, 0.024f, 10)
        addTaperedLimb(0.19f, -0.36f, 0.19f, 0.11f, 0.27f, 0.11f, 0.020f, 0.024f, 10)
        // Footrest ring
        addTorus(0f, -0.14f, 0f, 0.155f, 0.015f, 24, 10, axis = 1)
    }.build()

    fun table(): MeshData = Builder().apply {
        // Total height 0.76 (-0.38 to +0.38)
        addBox(0f, 0.34f, 0f, 1.06f, 0.065f, 0.68f)
        addBox(0f, 0.29f, 0f, 0.94f, 0.045f, 0.56f)
        // Four corner legs
        addTaperedLimb(-0.44f, -0.38f, -0.25f, -0.44f, 0.30f, -0.25f, 0.024f, 0.032f, 10)
        addTaperedLimb(0.44f, -0.38f, -0.25f, 0.44f, 0.30f, -0.25f, 0.024f, 0.032f, 10)
        addTaperedLimb(-0.44f, -0.38f, 0.25f, -0.44f, 0.30f, 0.25f, 0.024f, 0.032f, 10)
        addTaperedLimb(0.44f, -0.38f, 0.25f, 0.44f, 0.30f, 0.25f, 0.024f, 0.032f, 10)
    }.build()

    fun sofa(): MeshData = Builder().apply {
        // Total height 0.80 (-0.40 to +0.40), width 1.28, backrest at -Z
        // Lower plinth + 4 short feet
        addBox(0f, -0.24f, 0.02f, 1.24f, 0.18f, 0.68f)
        addBox(-0.54f, -0.365f, -0.26f, 0.08f, 0.07f, 0.08f)
        addBox(0.54f, -0.365f, -0.26f, 0.08f, 0.07f, 0.08f)
        addBox(-0.54f, -0.365f, 0.28f, 0.08f, 0.07f, 0.08f)
        addBox(0.54f, -0.365f, 0.28f, 0.08f, 0.07f, 0.08f)
        // Two plush seat cushions
        addBox(-0.27f, -0.07f, 0.05f, 0.51f, 0.16f, 0.58f)
        addBox(0.27f, -0.07f, 0.05f, 0.51f, 0.16f, 0.58f)
        // Backrest + armrests
        addBox(0f, 0.17f, -0.24f, 1.24f, 0.44f, 0.18f)
        addBox(-0.56f, 0.02f, 0.02f, 0.14f, 0.34f, 0.66f)
        addBox(0.56f, 0.02f, 0.02f, 0.14f, 0.34f, 0.66f)
    }.build()

    fun lamp(): MeshData = Builder().apply {
        // Total height 1.56 (-0.78 to +0.78)
        addTaperedLimb(0f, -0.78f, 0f, 0f, -0.73f, 0f, 0.20f, 0.18f, 24)
        addTaperedLimb(0f, -0.74f, 0f, 0f, 0.50f, 0f, 0.020f, 0.016f, 12)
        // Conical shade
        addTaperedLimb(0f, 0.44f, 0f, 0f, 0.78f, 0f, 0.24f, 0.11f, 24)
    }.build()

    fun pedestal(): MeshData = Builder().apply {
        // Total height 0.90 (-0.45 to +0.45)
        addBox(0f, -0.39f, 0f, 0.58f, 0.12f, 0.58f)
        addBox(0f, 0f, 0f, 0.42f, 0.68f, 0.42f)
        addBox(0f, 0.39f, 0f, 0.56f, 0.12f, 0.56f)
    }.build()

    fun headSculpt(): MeshData = Builder().apply {
        // Cranium
        addEllipsoid(0f, 0.11f, -0.01f, 0.088f, 0.105f, 0.095f, 16, 22)
        // Jaw/chin taper
        addEllipsoid(0f, 0.045f, 0.022f, 0.062f, 0.055f, 0.068f, 12, 18)
        // Subtle nose ridge so facing direction is unmistakable
        addTaperedLimb(0f, 0.105f, 0.082f, 0f, 0.068f, 0.098f, 0.010f, 0.014f, 8)
    }.build()

    fun hairMesh(style: HairStyle): MeshData = Builder().apply {
        // Base scalp cap shared by all styles except NAKED
        addEllipsoid(0f, 0.125f, -0.015f, 0.096f, 0.098f, 0.104f, 14, 20)
        // Front bangs base
        addTaperedLimb(-0.04f, 0.18f, 0.06f, -0.05f, 0.10f, 0.095f, 0.032f, 0.012f, 8)
        addTaperedLimb(0.0f, 0.19f, 0.07f, 0.0f, 0.11f, 0.098f, 0.034f, 0.010f, 8)
        addTaperedLimb(0.04f, 0.18f, 0.06f, 0.05f, 0.10f, 0.095f, 0.032f, 0.012f, 8)

        when (style) {
            HairStyle.NAKED -> Unit
            HairStyle.MIKU, HairStyle.GZYM, HairStyle.NICO -> {
                // Iconic twin tails
                val spread = if (style == HairStyle.NICO) 0.14f else 0.17f
                val drop = if (style == HairStyle.MIKU) -0.34f else if (style == HairStyle.GZYM) -0.22f else -0.08f
                addEllipsoid(-0.10f, 0.16f, -0.03f, 0.034f, 0.034f, 0.034f, 10, 12)
                addEllipsoid(0.10f, 0.16f, -0.03f, 0.034f, 0.034f, 0.034f, 10, 12)
                addTaperedLimb(-0.10f, 0.16f, -0.03f, -spread, drop, -0.07f, 0.038f, 0.014f, 10)
                addTaperedLimb(0.10f, 0.16f, -0.03f, spread, drop, -0.07f, 0.038f, 0.014f, 10)
            }
            HairStyle.NARUTO, HairStyle.ARCHER -> {
                // Spiky swept-up / layered tufts
                val backTilt = if (style == HairStyle.ARCHER) -0.10f else -0.04f
                addTaperedLimb(0f, 0.19f, 0f, 0f, 0.28f, backTilt, 0.045f, 0.008f, 8)
                addTaperedLimb(-0.05f, 0.17f, 0.01f, -0.11f, 0.25f, backTilt, 0.036f, 0.008f, 8)
                addTaperedLimb(0.05f, 0.17f, 0.01f, 0.11f, 0.25f, backTilt, 0.036f, 0.008f, 8)
                addTaperedLimb(-0.06f, 0.14f, -0.04f, -0.13f, 0.19f, -0.11f, 0.032f, 0.008f, 8)
                addTaperedLimb(0.06f, 0.14f, -0.04f, 0.13f, 0.19f, -0.11f, 0.032f, 0.008f, 8)
                addTaperedLimb(0f, 0.13f, -0.08f, 0f, 0.02f, -0.12f, 0.065f, 0.025f, 10)
            }
            HairStyle.REM, HairStyle.TATARA, HairStyle.DUBIANYAO, HairStyle.YSL -> {
                // Sleek bob / short layered cut
                val lenY = if (style == HairStyle.YSL) 0.02f else -0.04f
                addTaperedLimb(-0.082f, 0.13f, 0.01f, -0.092f, lenY, 0.02f, 0.032f, 0.018f, 8)
                addTaperedLimb(0.082f, 0.13f, 0.01f, 0.092f, lenY, 0.02f, 0.032f, 0.018f, 8)
                addTaperedLimb(0f, 0.13f, -0.075f, 0f, lenY - 0.01f, -0.105f, 0.085f, 0.065f, 12)
            }
            HairStyle.ALICE, HairStyle.MIO, HairStyle.SJWL, HairStyle.UMI, HairStyle.IKAROSU, HairStyle.DONGTIAOXI, HairStyle.YSFX -> {
                // Long flowing / ponytail / side-locks
                val backDrop = if (style == HairStyle.SJWL || style == HairStyle.IKAROSU) -0.28f else -0.18f
                addTaperedLimb(0f, 0.14f, -0.07f, 0f, backDrop, -0.12f, 0.085f, 0.068f, 12)
                addTaperedLimb(-0.082f, 0.14f, 0.02f, -0.095f, -0.10f, 0.03f, 0.028f, 0.012f, 8)
                addTaperedLimb(0.082f, 0.14f, 0.02f, 0.095f, -0.10f, 0.03f, 0.028f, 0.012f, 8)
                if (style == HairStyle.DONGTIAOXI || style == HairStyle.YSFX) {
                    // Side ponytail accent
                    addTaperedLimb(0.09f, 0.16f, -0.04f, 0.15f, -0.14f, -0.09f, 0.036f, 0.014f, 8)
                }
                if (style == HairStyle.ALICE) {
                    // Hair ribbon / bow accent on crown
                    addBox(0f, 0.22f, 0.01f, 0.11f, 0.028f, 0.024f)
                }
            }
        }
    }.build()

    fun gizmoRing(axis: Int, radius: Float = 0.22f): MeshData = Builder().apply {
        addTorus(0f, 0f, 0f, radius, 0.008f, 40, 8, axis = axis)
    }.build()

    fun gizmoArrow(axis: Int, length: Float = 0.34f): MeshData = Builder().apply {
        when (axis) {
            0 -> {
                addTaperedLimb(0f, 0f, 0f, length * 0.8f, 0f, 0f, 0.010f, 0.010f, 10)
                addTaperedLimb(length * 0.8f, 0f, 0f, length, 0f, 0f, 0.026f, 0.002f, 10)
            }
            1 -> {
                addTaperedLimb(0f, 0f, 0f, 0f, length * 0.8f, 0f, 0.010f, 0.010f, 10)
                addTaperedLimb(0f, length * 0.8f, 0f, 0f, length, 0f, 0.026f, 0.002f, 10)
            }
            else -> {
                addTaperedLimb(0f, 0f, 0f, 0f, 0f, length * 0.8f, 0.010f, 0.010f, 10)
                addTaperedLimb(0f, 0f, length * 0.8f, 0f, 0f, length, 0.026f, 0.002f, 10)
            }
        }
    }.build()

    fun gridLines(halfExtent: Int = 5): MeshData {
        val verts = ArrayList<Float>()
        val idx = ArrayList<Short>()
        var c: Short = 0
        val ext = halfExtent.toFloat()
        for (i in -halfExtent..halfExtent) {
            val f = i.toFloat()
            // Z-parallel line
            verts.addAll(listOf(f, 0.001f, -ext, 0f, 1f, 0f))
            verts.addAll(listOf(f, 0.001f, ext, 0f, 1f, 0f))
            idx.add(c++); idx.add(c++)
            // X-parallel line
            verts.addAll(listOf(-ext, 0.001f, f, 0f, 1f, 0f))
            verts.addAll(listOf(ext, 0.001f, f, 0f, 1f, 0f))
            idx.add(c++); idx.add(c++)
        }
        return MeshData(verts.toFloatArray(), idx.toShortArray(), isLines = true)
    }

    fun taperedSegment(r0: Float, r1: Float, length: Float): MeshData = Builder().apply {
        addTaperedLimb(0f, 0f, 0f, 0f, length, 0f, r0, r1, 14)
    }.build()

    fun limbCapsule(): MeshData = Builder().apply {
        // Normalized unit height (-0.5..+0.5) & unit diameter (~1.0 at muscle belly)
        // Fuller proximal belly (+Y side) tapering smoothly toward distal joint (-Y side)
        addTaperedLimb(0f, -0.5f, 0f, 0f, -0.10f, 0f, 0.34f, 0.50f, 14)
        addTaperedLimb(0f, -0.10f, 0f, 0f, 0.25f, 0f, 0.50f, 0.48f, 14)
        addTaperedLimb(0f, 0.25f, 0f, 0f, 0.5f, 0f, 0.48f, 0.38f, 14)
    }.build()

    fun torsoWedge(): MeshData = Builder().apply {
        // Faceted planar wedge normalized inside -0.5..+0.5 cube; wider & deeper at +Y
        val segs = 4
        val yBot = -0.5f
        val yTop = 0.5f
        // Custom 8-corner flat-shaded tapered prism (rotated 45 deg or axis-aligned)
        val bx = 0.38f; val bz = 0.40f
        val tx = 0.56f; val tz = 0.52f
        val p = arrayOf(
            Triple(-bx, yBot, bz), Triple(bx, yBot, bz), Triple(tx, yTop, tz), Triple(-tx, yTop, tz), // front
            Triple(bx, yBot, -bz), Triple(-bx, yBot, -bz), Triple(-tx, yTop, -tz), Triple(tx, yTop, -tz) // back
        )
        fun quad(a: Triple<Float, Float, Float>, b: Triple<Float, Float, Float>, c: Triple<Float, Float, Float>, d: Triple<Float, Float, Float>) {
            val ux = b.first - a.first; val uy = b.second - a.second; val uz = b.third - a.third
            val vx = c.first - a.first; val vy = c.second - a.second; val vz = c.third - a.third
            val nx = uy * vz - uz * vy
            val ny = uz * vx - ux * vz
            val nz = ux * vy - uy * vx
            val i0 = addVertex(a.first, a.second, a.third, nx, ny, nz)
            val i1 = addVertex(b.first, b.second, b.third, nx, ny, nz)
            val i2 = addVertex(c.first, c.second, c.third, nx, ny, nz)
            val i3 = addVertex(d.first, d.second, d.third, nx, ny, nz)
            addQuad(i0, i1, i2, i3)
        }
        // Front (+Z), Back (-Z), Right (+X), Left (-X), Top (+Y), Bottom (-Y)
        quad(p[0], p[1], p[2], p[3])
        quad(p[4], p[5], p[6], p[7])
        quad(p[1], p[4], p[7], p[2])
        quad(p[5], p[0], p[3], p[6])
        quad(p[3], p[2], p[7], p[6])
        quad(p[5], p[4], p[1], p[0])
        if (segs < 0) Unit
    }.build()

    fun planarHead(): MeshData = Builder().apply {
        // Loomis-inspired planar faceted head for clear light/shadow planes
        addBox(0f, 0.125f, -0.008f, 0.165f, 0.175f, 0.175f)
        // Chiseled jaw block
        addBox(0f, 0.042f, 0.024f, 0.115f, 0.085f, 0.120f)
        // Brow ridge + nose plane for clear facing direction
        addBox(0f, 0.135f, 0.082f, 0.135f, 0.028f, 0.024f)
        addBox(0f, 0.085f, 0.092f, 0.024f, 0.048f, 0.022f)
    }.build()
}
