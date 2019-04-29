package kobramob.rubeg38.ru.gbrnavigation.service

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.experimental.xor

class Coder {

    private val mass1 = byteArrayOf(0x50.toByte(), 0x0D.toByte(), 0x39.toByte(), 0x41.toByte(), 0x3B.toByte(), 0x89.toByte(), 0x33.toByte(), 0x88.toByte(), 0xD1.toByte(), 0x45.toByte(), 0x3C.toByte(), 0x90.toByte(), 0x16.toByte(), 0xC8.toByte(), 0x0E.toByte(), 0x9F.toByte(), 0x64.toByte(), 0x3D.toByte(), 0xA1.toByte(), 0x80.toByte(), 0xB3.toByte(), 0x49.toByte(), 0x34.toByte(), 0xCB.toByte(), 0x4D.toByte(), 0x8A.toByte(), 0x09.toByte(), 0xCE.toByte(), 0x82.toByte(), 0x1F.toByte(), 0x1E.toByte(), 0xA0.toByte(), 0x36.toByte(), 0x98.toByte(), 0xE5.toByte(), 0xC3.toByte(), 0x69.toByte(), 0xEC.toByte(), 0xFD.toByte(), 0x59.toByte(), 0xD4.toByte(), 0x1D.toByte(), 0xB9.toByte(), 0xD6.toByte(), 0xEA.toByte(), 0x11.toByte(), 0x65.toByte(), 0xE1.toByte(), 0x4A.toByte(), 0x9D.toByte(), 0x51.toByte(), 0x55.toByte(), 0x0F.toByte(), 0x4F.toByte(), 0x56.toByte(), 0xF2.toByte(), 0x95.toByte(), 0xA2.toByte(), 0x25.toByte(), 0x24.toByte(), 0x53.toByte(), 0x67.toByte(), 0xD3.toByte(), 0xB1.toByte(), 0x70.toByte(), 0xE4.toByte(), 0xF3.toByte(), 0x03.toByte(), 0xC9.toByte(), 0xA5.toByte(), 0x47.toByte(), 0x9C.toByte(), 0xF7.toByte(), 0x8D.toByte(), 0x28.toByte(), 0x0B.toByte(), 0x05.toByte(), 0x07.toByte(), 0x5D.toByte(), 0xAA.toByte(), 0xAE.toByte(), 0x32.toByte(), 0xF0.toByte(), 0xAD.toByte(), 0x4C.toByte(), 0x57.toByte(), 0x44.toByte(), 0xF1.toByte(), 0xEF.toByte(), 0xF5.toByte(), 0x93.toByte(), 0xDB.toByte(), 0x15.toByte(), 0xBC.toByte(), 0x2D.toByte(), 0xF4.toByte(), 0x5E.toByte(), 0x5F.toByte(), 0x75.toByte(), 0xCA.toByte(), 0x00.toByte(), 0x6D.toByte(), 0x66.toByte(), 0xA4.toByte(), 0xA9.toByte(), 0x61.toByte(), 0x2C.toByte(), 0x8F.toByte(), 0x97.toByte(), 0x26.toByte(), 0xBB.toByte(), 0x7D.toByte(), 0x85.toByte(), 0xB4.toByte(), 0xA7.toByte(), 0xFB.toByte(), 0xA8.toByte(), 0x86.toByte(), 0x04.toByte(), 0xE3.toByte(), 0xD9.toByte(), 0xF9.toByte(), 0xDF.toByte(), 0xC4.toByte(), 0xE6.toByte(), 0xB0.toByte(), 0x35.toByte(), 0xE9.toByte(), 0xC1.toByte(), 0xBF.toByte(), 0x54.toByte(), 0x9E.toByte(), 0x43.toByte(), 0x13.toByte(), 0xC6.toByte(), 0x8E.toByte(), 0x84.toByte(), 0x71.toByte(), 0xCC.toByte(), 0xDC.toByte(), 0x20.toByte(), 0xB5.toByte(), 0xFC.toByte(), 0x0C.toByte(), 0x9A.toByte(), 0x96.toByte(), 0xEB.toByte(), 0xB8.toByte(), 0xBD.toByte(), 0x60.toByte(), 0xC5.toByte(), 0x14.toByte(), 0xCD.toByte(), 0x2E.toByte(), 0x78.toByte(), 0x83.toByte(), 0xAF.toByte(), 0x3A.toByte(), 0x06.toByte(), 0xD5.toByte(), 0xE7.toByte(), 0xDD.toByte(), 0x2B.toByte(), 0xAC.toByte(), 0xD2.toByte(), 0x30.toByte(), 0x01.toByte(), 0x21.toByte(), 0x2A.toByte(), 0x23.toByte(), 0xE2.toByte(), 0x19.toByte(), 0x92.toByte(), 0x17.toByte(), 0x79.toByte(), 0x5C.toByte(), 0xED.toByte(), 0x3E.toByte(), 0xA6.toByte(), 0x46.toByte(), 0x4E.toByte(), 0xFE.toByte(), 0x1B.toByte(), 0x62.toByte(), 0x08.toByte(), 0x4B.toByte(), 0x6F.toByte(), 0x38.toByte(), 0x40.toByte(), 0x6E.toByte(), 0xC2.toByte(), 0xBA.toByte(), 0x76.toByte(), 0x7E.toByte(), 0xFA.toByte(), 0x1C.toByte(), 0x63.toByte(), 0xB6.toByte(), 0xBE.toByte(), 0x1A.toByte(), 0xC7.toByte(), 0x6C.toByte(), 0xDE.toByte(), 0x74.toByte(), 0x29.toByte(), 0xEE.toByte(), 0x31.toByte(), 0xF6.toByte(), 0x02.toByte(), 0x77.toByte(), 0x5B.toByte(), 0xDA.toByte(), 0x0A.toByte(), 0x52.toByte(), 0x42.toByte(), 0x81.toByte(), 0x7F.toByte(), 0xB2.toByte(), 0x7C.toByte(), 0x6B.toByte(), 0x87.toByte(), 0x8C.toByte(), 0xB7.toByte(), 0x94.toByte(), 0x12.toByte(), 0xCF.toByte(), 0x5A.toByte(), 0x6A.toByte(), 0xD7.toByte(), 0xE0.toByte(), 0x48.toByte(), 0x72.toByte(), 0x22.toByte(), 0xFF.toByte(), 0x27.toByte(), 0xC0.toByte(), 0x2F.toByte(), 0x37.toByte(), 0x91.toByte(), 0xD8.toByte(), 0x3F.toByte(), 0x7A.toByte(), 0xD0.toByte(), 0x18.toByte(), 0x73.toByte(), 0xE8.toByte(), 0xF8.toByte(), 0x10.toByte(), 0x58.toByte(), 0x7B.toByte(), 0x8B.toByte(), 0x99.toByte(), 0x68.toByte(), 0x9B.toByte(), 0xA3.toByte(), 0xAB.toByte())

    private val sizeHeading = 55

    fun encoderOne(message: String, count: Long, typePacket: Byte, tid: String): ByteBuffer {

        val messageByteArray = message.toByteArray()
        val tidByteArray = tid.toByteArray()
        val length = messageByteArray.size
        val packetSize = length + 2
        val sizeFullPacket = sizeHeading + 2 + 2 + length
        val positionOnMassive = 0
        val allPacket = 1
        val TPacket = 1
        val allSize: Int = sizeFullPacket

        // Формируем заголовок
        val heading: ByteBuffer = ByteBuffer.allocate(sizeHeading + 2)
        heading.order(ByteOrder.LITTLE_ENDIAN)
        xor(heading, typePacket, packetSize, 0, 0, positionOnMassive, allSize, count, allPacket, TPacket, tidByteArray)
        val headingArray = heading.array()
        println(Arrays.toString(headingArray))
        coderZag(headingArray, mass1)

        // Формируем пакет
        val packet: ByteBuffer = ByteBuffer.allocate(packetSize)
        packet.order(ByteOrder.LITTLE_ENDIAN)
        xor2(packet)
        packet.position(2)
        packet.put(messageByteArray)
        val packetArray: ByteArray = packet.array()
        coderPacket(packetArray, mass1, length)

        // Соединили
        val request: ByteBuffer = ByteBuffer.allocate(sizeFullPacket)
        request.order(ByteOrder.LITTLE_ENDIAN)
        request.put(headingArray)
        request.put(packetArray)
        return request
    }
    fun decoderPacketOne(message: ByteArray): String {
        // Декодим заголовок
        val headingArray: ByteArray = ByteArray(57)
        System.arraycopy(message, 0, headingArray, 0, headingArray.size)
        coderZag(headingArray, mass1)
        // Узнаем размер пакета, Декодим пакет
        when (headingArray[4].toInt() and 255) {
            255 -> {
                return Arrays.toString(headingArray)
            }
            0 -> {
                val lengthPacket: Int = calculateSize(headingArray)
                if (lengthPacket> 0)
                    return packetString(message, lengthPacket)
            }
            1 -> {
                val lengthPacket: Int = calculateSize(headingArray)
                if (lengthPacket> 0)
                    return packetString(message, lengthPacket)
            }
            2 -> {
                val lengthPacket: Int = calculateSize(headingArray)
                if (lengthPacket> 0)
                    return packetString(message, lengthPacket)
            }
            else -> { return "Null" }
        }
        return "Null"
    }

    private fun packetString(message: ByteArray, lengthPacket: Int): String {
        val packetArray = ByteArray(lengthPacket)
        System.arraycopy(message, 57, packetArray, 0, lengthPacket)
        coderPacket(packetArray, mass1, lengthPacket - 2)
        val result = ByteArray(lengthPacket - 2)
        System.arraycopy(packetArray, 2, result, 0, lengthPacket - 2)
        return String(result)
    }

    private fun coderZag(temporaryArray: ByteArray, mass1: ByteArray) {
        var shiftRight = temporaryArray[0].toInt() and 255
        var shiftLeft = temporaryArray[1].toInt() and 255
        for (x in 2 until temporaryArray.size) {
            temporaryArray[x] = temporaryArray[x] xor(mass1[shiftRight] xor mass1[shiftLeft])

            if (shiftRight <255)
                shiftRight++
            else
                shiftRight = 0

            if (shiftLeft> 0)
                shiftLeft--
            else
                shiftLeft = 255
        }
    }

    private fun coderPacket(temporaryArray: ByteArray, mass1: ByteArray, length: Int) {
        var shiftRight = temporaryArray[0].toInt() and 255
        var shiftLeft = temporaryArray[1].toInt() and 255
        for (z in 0 until length) {
            temporaryArray[2 + z] = (temporaryArray[2 + z].toInt() xor mass1[shiftRight].toInt() xor mass1[shiftLeft].toInt()).toByte()

            if (shiftRight <255)
                shiftRight++
            else
                shiftRight = 0

            if (shiftLeft> 0)
                shiftLeft--
            else
                shiftLeft = 255
        }
    }

    private fun xor(
        heading: ByteBuffer,
        typepacket: Byte,
        sizepacket: Int,
        sizeFirst: Int,
        sizeSecond: Int,
        positionOnMassive: Int,
        allSize: Int,
        count: Long,
        allPacket: Int,
        tPacket: Int,
        tidByteArray: ByteArray
    ) {
        heading.position(2)
        heading.put(0xAA.toByte())
        heading.put(0xFF.toByte())
        heading.put(typepacket)
        heading.putInt(sizepacket)
        heading.putInt(sizeFirst)
        heading.putInt(sizeSecond)
        heading.putInt(positionOnMassive)
        heading.putInt(allSize)
        heading.putLong(count)
        heading.putInt(allPacket)
        heading.putInt(tPacket)
        heading.put(tidByteArray)
        val a = ByteArray(2)
        var v: Byte
        val r = Random()
        for (j in 0..1) {

            for (ignored in a) {
                r.nextBytes(a)
            }
            v = (a[0] xor a[1])
            heading.position(j)
            heading.put(v)
        }
    }

    private fun xor2(dat: ByteBuffer) {

        val a = ByteArray(2)
        var v: Byte
        val r = Random()
        for (j in 0..1) {
            for (ignored in a) {
                r.nextBytes(a)
            }
            v = (a[0] xor a[1])
            dat.position(j)
            dat.put(v)
        }
        // (Arrays.toString(dat));
    }

    private fun calculateSize(heading: ByteArray): Int {
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(57)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(heading)
        return byteBuffer.getInt(5)
    }

    fun packetType255(message: ByteArray): ByteBuffer {
        var headingArray: ByteArray = ByteArray(57)
        System.arraycopy(message, 0, headingArray, 0, headingArray.size)
        coderZag(headingArray, mass1)

        val byteBuffer: ByteBuffer = ByteBuffer.allocate(57)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(headingArray)

        val tPacket = byteBuffer.getInt(37)
        val allPacket: Int = byteBuffer.getInt(33)
        val count: Long = byteBuffer.getLong(25)
        val allSize: Int = byteBuffer.getInt(21)
        val positionOnMassive = byteBuffer.getInt(17)
        val sizeSecont: Int = byteBuffer.getInt(13)
        val sizeFirst: Int = byteBuffer.getInt(9)
        val typePacket: Byte = 0xFF.toByte()
        val tidByteArray: ByteArray = ByteArray(16)
        byteBuffer.position(41)
        byteBuffer.get(tidByteArray)
        byteBuffer.clear()

        xor(
            byteBuffer,
            typePacket,
            0,
            sizeFirst,
            sizeSecont,
            positionOnMassive,
            allSize,
            count,
            allPacket,
            tPacket,
            tidByteArray
        )
        headingArray = byteBuffer.array()
        println("Подтверждаю что пакет пришел " + Arrays.toString(headingArray))
        coderZag(headingArray, mass1)
        byteBuffer.position(0)
        byteBuffer.put(headingArray)
        return byteBuffer
    }

    fun typePacket(message: ByteArray): Int {

        val headingArray: ByteArray = ByteArray(57)
        System.arraycopy(message, 0, headingArray, 0, headingArray.size)
        coderZag(headingArray, mass1)

        return (headingArray[4].toInt() and 255)
    }

    fun countReceiver(message: ByteArray): Long {
        val headingArray: ByteArray = ByteArray(57)
        System.arraycopy(message, 0, headingArray, 0, headingArray.size)
        coderZag(headingArray, mass1)
        val headingBuffer: ByteBuffer = ByteBuffer.allocate(57)
        headingBuffer.put(headingArray)
        return headingBuffer.getLong(25)
    }

   /* private fun shiftPackage(dat: ByteArray, razmerbyte: Int, mass1: ByteArray, mass2: ByteArray, len: Int) {
        var d11 = dat[razmerbyte + 2].toInt() and 255
        var d21 = dat[razmerbyte + 3].toInt() and 255
        for (z in 0 until len) {
            // Перезапись массива ксорами
            dat[razmerbyte + 4 + z] = (dat[razmerbyte + 4 + z].toInt() xor mass1[d11].toInt() xor mass1[d21].toInt()).toByte()
            // (z+4+razmerbyte );

            // ("Проверка " + dat[z] + " z= " +(razmerbyte+4+ z));

            // Проверка чтобы не выйти за границы
            if (d11 < 255) {
                d11++
            } else
                d11 = 0
            if (d21 > 0) {
                d21--
            } else
                d21 = 254
        }
        //  (Arrays.toString(dat));
    }

    private fun shiftZag(dat: ByteArray, mass1: ByteArray, mass2: ByteArray) {
        var d1 = dat[0].toInt() and 255
        var d2 = dat[1].toInt() and 255
        for (x in 2..20) {

            dat[x] = dat[x] xor (mass1[d1] xor mass1[d2])

            if (d1 < 255) {
                d1++
            } else
                d1 = 0
            if (d2 > 0) {
                d2--
            } else
                d2 = 254
        }
        // (Arrays.toString(dat));
    }

    fun decoder(message: String): ByteBuffer? {

        val messageStringByte =
            message.toByteArray()

        val length =
            message.length

        val packageSize = length + 2

        val sizeIntermediate = sizeHeading + 2 + 2 + length

        val typePacket: Byte = 0
        val positionOnMassive = 0
        val allPacket = 1
        val TPacket = 1
        val allSize = sizeIntermediate
        val count: Long = 0

        val request: ByteBuffer = ByteBuffer.allocate(sizeIntermediate)
        request.order(ByteOrder.LITTLE_ENDIAN)

        xor(request, typePacket, packageSize, 0, 0, positionOnMassive, allSize, count, allPacket, TPacket)
        var intermediate = request.array()

        shiftZag(intermediate, mass1, mass2)
        request.position(0)
        request.put(intermediate)
        request.position(sizeHeading + 4)
        request.put(messageStringByte)
        xor2(request, sizeHeading)

        intermediate = request.array()
        shiftPackage(intermediate, sizeHeading, mass1, mass2, length)

        request.position(0)

        return request.put(intermediate)
    }

    fun encoderHead(heading: ByteArray) {
        return shiftZag(heading, mass1, mass2)
    }

    fun calculateSize(heading: ByteArray): Int {
        val first = heading[5].toInt() and 255
        val two = heading[6].toInt() and 255
        val three = heading[7].toInt() and 255
        val four = heading[8].toInt() and 255
        return (first) + (two) * 256 + (three) * 256 * 256 + (four) * 256 * 256 * 256
    }

    fun encoder(packageSize: Int, message: ByteArray, packageResponse: ByteArray): String {
        val headPackage = ByteArray(packageSize + sizeHeading + 4)
        System.arraycopy(message, 0, headPackage, 0, sizeHeading + 2)
        System.arraycopy(packageResponse, 0, headPackage, sizeHeading + 2, packageSize)

        // Дешифруем пакет
        shiftPackage(headPackage, sizeHeading, mass1, mass2, packageSize)

        // Переписываем полученый пакет,на дешифр
        val decodingPackage = ByteArray(packageSize - 2)
        System.arraycopy(headPackage, sizeHeading + 4, decodingPackage, 0, packageSize - 2)

        return String(decodingPackage)
    }
    */
}