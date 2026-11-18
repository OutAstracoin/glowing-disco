package com.example.engine

import android.opengl.Matrix
import com.example.model.Vec3
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class Ray(val origin: Vec3, val dir: Vec3)

object Math3D {

    fun identity(): FloatArray = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }

    fun copy(m: FloatArray): FloatArray = m.copyOf()

    fun translate(m: FloatArray, x: Float, y: Float, z: Float): FloatArray =
        m.copyOf().also { Matrix.translateM(it, 0, x, y, z) }

    fun rotateEulerDeg(m: FloatArray, rx: Float, ry: Float, rz: Float): FloatArray {
        val out = m.copyOf()
        if (ry != 0f) Matrix.rotateM(out, 0, ry, 0f, 1f, 0f)
        if (rx != 0f) Matrix.rotateM(out, 0, rx, 1f, 0f, 0f)
        if (rz != 0f) Matrix.rotateM(out, 0, rz, 0f, 0f, 1f)
        return out
    }

    fun scale(m: FloatArray, sx: Float, sy: Float, sz: Float): FloatArray =
        m.copyOf().also { Matrix.scaleM(it, 0, sx, sy, sz) }

    fun multiply(a: FloatArray, b: FloatArray): FloatArray =
        FloatArray(16).also { Matrix.multiplyMM(it, 0, a, 0, b, 0) }

    fun transformPoint(m: FloatArray, p: Vec3): Vec3 {
        val v = floatArrayOf(p.x, p.y, p.z, 1f)
        val out = FloatArray(4)
        Matrix.multiplyMV(out, 0, m, 0, v, 0)
        val w = if (abs(out[3]) > 1e-6f) out[3] else 1f
        return Vec3(out[0] / w, out[1] / w, out[2] / w)
    }

    fun transformDir(m: FloatArray, d: Vec3): Vec3 {
        val v = floatArrayOf(d.x, d.y, d.z, 0f)
        val out = FloatArray(4)
        Matrix.multiplyMV(out, 0, m, 0, v, 0)
        return Vec3(out[0], out[1], out[2]).normalized()
    }

    fun screenToRay(
        screenX: Float,
        screenY: Float,
        viewWidth: Float,
        viewHeight: Float,
        viewProjMatrix: FloatArray
    ): Ray? {
        if (viewWidth <= 0f || viewHeight <= 0f) return null
        val invVP = FloatArray(16)
        if (!Matrix.invertM(invVP, 0, viewProjMatrix, 0)) return null

        val ndcX = (2f * screenX / viewWidth) - 1f
        val ndcY = 1f - (2f * screenY / viewHeight)

        val nearWorld = FloatArray(4)
        val farWorld = FloatArray(4)
        Matrix.multiplyMV(nearWorld, 0, invVP, 0, floatArrayOf(ndcX, ndcY, -1f, 1f), 0)
        Matrix.multiplyMV(farWorld, 0, invVP, 0, floatArrayOf(ndcX, ndcY, 1f, 1f), 0)

        if (abs(nearWorld[3]) < 1e-6f || abs(farWorld[3]) < 1e-6f) return null
        val near = Vec3(
            nearWorld[0] / nearWorld[3],
            nearWorld[1] / nearWorld[3],
            nearWorld[2] / nearWorld[3]
        )
        val far = Vec3(
            farWorld[0] / farWorld[3],
            farWorld[1] / farWorld[3],
            farWorld[2] / farWorld[3]
        )
        return Ray(near, (far - near).normalized())
    }

    fun projectToScreen(
        worldPoint: Vec3,
        viewProjMatrix: FloatArray,
        viewWidth: Float,
        viewHeight: Float
    ): Pair<Float, Float>? {
        val v = floatArrayOf(worldPoint.x, worldPoint.y, worldPoint.z, 1f)
        val clip = FloatArray(4)
        Matrix.multiplyMV(clip, 0, viewProjMatrix, 0, v, 0)
        if (clip[3] <= 1e-4f) return null
        val ndcX = clip[0] / clip[3]
        val ndcY = clip[1] / clip[3]
        val sx = (ndcX + 1f) * 0.5f * viewWidth
        val sy = (1f - ndcY) * 0.5f * viewHeight
        return sx to sy
    }

    fun intersectRaySphere(ray: Ray, center: Vec3, radius: Float): Float? {
        val oc = ray.origin - center
        val b = oc.dot(ray.dir)
        val c = oc.dot(oc) - radius * radius
        val disc = b * b - c
        if (disc < 0f) return null
        val t = -b - sqrt(disc)
        return if (t >= 0f) t else null
    }

    fun intersectRayOBB(ray: Ray, modelMatrix: FloatArray, halfExtents: Vec3): Float? {
        val inv = FloatArray(16)
        if (!Matrix.invertM(inv, 0, modelMatrix, 0)) return null

        val localOrig = transformPoint(inv, ray.origin)
        val vEnd = ray.origin + ray.dir
        val localEnd = transformPoint(inv, vEnd)
        val localDir = (localEnd - localOrig)

        var tMin = 0f
        var tMax = 1e6f

        val axes = arrayOf(
            Triple(localOrig.x, localDir.x, halfExtents.x),
            Triple(localOrig.y, localDir.y, halfExtents.y),
            Triple(localOrig.z, localDir.z, halfExtents.z)
        )
        for ((orig, dir, ext) in axes) {
            if (abs(dir) < 1e-6f) {
                if (orig < -ext || orig > ext) return null
            } else {
                var t1 = (-ext - orig) / dir
                var t2 = (ext - orig) / dir
                if (t1 > t2) {
                    val tmp = t1
                    t1 = t2
                    t2 = tmp
                }
                tMin = max(tMin, t1)
                tMax = min(tMax, t2)
                if (tMin > tMax) return null
            }
        }
        val worldHit = transformPoint(modelMatrix, localOrig + localDir * tMin)
        return (worldHit - ray.origin).length()
    }

    fun intersectRayRing(
        ray: Ray,
        center: Vec3,
        normal: Vec3,
        ringRadius: Float,
        tolerance: Float
    ): Float? {
        val denom = ray.dir.dot(normal)
        if (abs(denom) < 1e-4f) return null
        val t = (center - ray.origin).dot(normal) / denom
        if (t < 0f) return null
        val hitPoint = ray.origin + ray.dir * t
        val distFromCenter = (hitPoint - center).length()
        return if (abs(distFromCenter - ringRadius) <= tolerance) t else null
    }

    fun distanceRayToSegment(
        ray: Ray,
        segStart: Vec3,
        segEnd: Vec3
    ): Pair<Float, Float> {
        val u = ray.dir
        val v = segEnd - segStart
        val w = ray.origin - segStart
        val a = u.dot(u)
        val b = u.dot(v)
        val c = v.dot(v)
        val d = u.dot(w)
        val e = v.dot(w)
        val denom = a * c - b * b

        val sc: Float
        var tc: Float
        if (denom < 1e-6f) {
            sc = 0f
            tc = if (b > c) d / b else e / c
        } else {
            sc = max(0f, (b * e - c * d) / denom)
            tc = (a * e - b * d) / denom
        }
        tc = tc.coerceIn(0f, 1f)
        val pRay = ray.origin + u * sc
        val pSeg = segStart + v * tc
        return (pRay - pSeg).length() to sc
    }
}
