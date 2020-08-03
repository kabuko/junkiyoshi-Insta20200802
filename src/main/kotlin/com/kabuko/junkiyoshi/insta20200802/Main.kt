package com.kabuko.junkiyoshi.insta20200802

import com.kabuko.toRadians
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.vertexBuffer
import org.openrndr.draw.vertexFormat
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.perlinQuintic
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.map
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        width = 720
        height = 720
    }

    program {
        extend(ScreenRecorder()) {
            outputFile = "media/insta20200802.mp4"
            quitAfterMaximum = true
            frameRate = 60
            maximumDuration = 15.0
        }

        // Set the seed here in init so that locations and radii are consistent across runs
        Random.seed = "2020080202"

        val particlesPerRing = 12

        val colorList = listOf("ef476f", "ffd166", "06d6a0", "118ab2", "073b4c")
            .map { ColorRGBa.fromHex(it) }

        val vertexOrder = arrayOf(0, 1, 3, 0, 2, 3)

        drawer.strokeWeight = 1.0

        val locationList = mutableListOf<Vector2>()
        val radiusList = mutableListOf<Double>()

        // Plot out locations by randomly generating a position and radius in 3 ranges of sizes from
        // large to small, making sure new circles don't overlap with any previous circles.

        while (locationList.size < 8) {
            val location = Vector2(
                Random.double(140.0, width - 140.0),
                Random.double(140.0, height - 140.0)
            )
            val radius = Random.double(50.0, 120.0)

            var flag = true

            for (i in 0 until locationList.size) {
                if (location.distanceTo(locationList[i]) < radiusList[i] + radius) {
                    flag = false
                    break
                }
            }

            if (flag) {
                locationList.add(location)
                radiusList.add(radius)
            }
        }

        while (locationList.size < 20) {
            val location = Vector2(
                Random.double(100.0, width - 100.0),
                Random.double(100.0, height - 100.0)
            )
            val radius = Random.double(30.0, 60.0)
            var flag = true
            for (i in 0 until locationList.size) {
                if (location.distanceTo(locationList[i]) < radiusList[i] + radius) {
                    flag = false
                    break
                }
            }

            if (flag) {
                locationList.add(location)
                radiusList.add(radius * 1.2)
            }
        }

        while (locationList.size < 36) {
            val location = Vector2(
                Random.double(100.0, width - 100.0),
                Random.double(100.0, height - 100.0)
            )
            val radius = Random.double(10.0, 20.0)
            var flag = true
            for (i in 0..locationList.lastIndex) {
                if (location.distanceTo(locationList[i]) < radiusList[i] + radius) {
                    flag = false
                    break
                }
            }

            if (flag) {
                locationList.add(location)
                radiusList.add(radius * 1.2)
            }
        }

        extend {
            drawer.clear(ColorRGBa(0.95, 0.95, 0.95))

            // Set seed here so that colors and noise are consistent across frames
            Random.seed = "test"

            // Size of the head of the walker
            val headSize = 4.0

            for (m in 0..locationList.lastIndex) {
                drawer.pushTransforms()
                drawer.translate(locationList[m])

                // for each location we want 5 different rings
                repeat(5) {
                    val color = colorList[Random.int0(colorList.lastIndex)]

                    val vertices = mutableListOf<Vector3>()

                    val noiseSeedDeg = Random.int0(1000)
                    val noiseSeedRadius = Random.int0(1000)

                    var lastLocation: Vector3 = Vector3.ZERO

                    var lastTheta = 0.0

                    // 15 sections of the walker's tail
                    for (i in 0 until 15) {
                        val noiseX = frameCount + i % 1000

                        // map noise to an angle
                        val noiseDeg = map(
                            0.0,
                            1.0,
                            -360.0,
                            360.0,
                            perlinQuintic(noiseSeedDeg, noiseX * 0.001)
                        )
                        // map noise to a radius
                        val noiseRadius = map(
                            0.0,
                            1.0,
                            radiusList[m] * -0.98,
                            radiusList[m] * 0.98,
                            perlinQuintic(noiseSeedRadius, noiseX * 0.01)
                        )
                        // map noise to an angle
                        val nextNoiseDeg = map(
                            0.0,
                            1.0,
                            -360.0,
                            360.0,
                            perlinQuintic(noiseSeedDeg, (noiseX + 1) * 0.001)
                        )
                        // map noise to a radius
                        val nextNoiseRadius = map(
                            0.0,
                            1.0,
                            radiusList[m] * -0.98,
                            radiusList[m] * 0.98,
                            perlinQuintic(noiseSeedRadius, (noiseX + 1) * 0.01)
                        )

                        val location = Vector3(
                            cos(noiseDeg.toRadians()),
                            sin(noiseDeg.toRadians()),
                            0.0
                        ) * noiseRadius
                        val next = Vector3(
                            cos(nextNoiseDeg.toRadians()),
                            sin(nextNoiseDeg.toRadians()),
                            0.0
                        ) * nextNoiseRadius

                        val direction = next - location
                        val theta = atan2(direction.y, direction.x)

                        // add left vertex
                        vertices.add(
                            location + Vector3(
                                map(
                                    0.0,
                                    25.0,
                                    0.0,
                                    headSize,
                                    i.toDouble()
                                ) * cos(theta - PI * 0.5),
                                map(
                                    0.0,
                                    25.0,
                                    0.0,
                                    headSize,
                                    i.toDouble()
                                ) * sin(theta - PI * 0.5),
                                0.0
                            )
                        )
                        // add right vertex
                        vertices.add(
                            location + Vector3(
                                map(
                                    0.0,
                                    25.0,
                                    0.0,
                                    headSize,
                                    i.toDouble()
                                ) * cos(theta + PI * 0.5),
                                map(
                                    0.0,
                                    25.0,
                                    0.0,
                                    headSize,
                                    i.toDouble()
                                ) * sin(theta + PI * 0.5),
                                0.0
                            )
                        )

                        lastLocation = location
                        lastTheta = theta
                    }

                    val finalVertices = mutableListOf<Vector3>()

                    // add tail vertices
                    for (i in 0 until vertices.size - 2 step 2) {
                        vertexOrder
                            .map { vertices[i + it] }
                            .also { finalVertices.addAll(it) }
                    }

                    // Calculate head vertices to make a semi circle for the head
                    val headVertices = mutableListOf<Vector3>()
                    var theta = lastTheta - PI * 0.5
                    while (theta <= lastTheta + PI * 0.5) {
                        headVertices.add(
                            lastLocation + Vector3(
                                headSize * 0.5 * cos(theta),
                                headSize * 0.5 * sin(theta),
                                0.0
                            )
                        )

                        theta += PI / 20.0
                    }

                    // Add head vertices
                    for (i in 0 until headVertices.lastIndex) {
                        finalVertices.add(headVertices[0])
                        finalVertices.add(headVertices[i])
                        finalVertices.add(headVertices[i + 1])
                    }

                    // Seems like OPENRNDR's mesh implementation adds duplicate vertices versus
                    // OpenFramework which adds unique vertices and then indexes them to connect.
                    val mesh = vertexBuffer(vertexFormat {
                        position(3)
                    }, finalVertices.size)

                    mesh.put {
                        finalVertices.forEach {
                            write(it)
                        }
                    }

                    repeat(particlesPerRing) {
                        drawer.rotate(360.0 / particlesPerRing)
                        drawer.fill = color
                        drawer.vertexBuffer(mesh, DrawPrimitive.TRIANGLES)
                    }
                }

                drawer.popTransforms()
            }
        }
    }
}
