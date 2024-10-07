package com.ditchoom.buffer

import kotlin.math.min

class WasmBuffer(private var buffer: ByteArray, private var currentPosition: Int = 0) : PlatformBuffer {
    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN

        private var limit: Int = buffer.size

    override val capacity: Int = buffer.size

        // Set the buffer limit
        override fun setLimit(limit: Int) {
            this.limit = limit
        }

        // Reset for reading
        override fun resetForRead() {
            currentPosition = 0
        }

        // Read a single byte
        override fun readByte(): Byte {
            if (currentPosition >= limit) throw IndexOutOfBoundsException("Reached limit")
            return buffer[currentPosition++]
        }

        // Get a byte at a specific index
        override fun get(index: Int): Byte {
            if (index >= limit) throw IndexOutOfBoundsException("Index exceeds limit")
            return buffer[index]
        }

        // Slice the buffer into a read-only sub-buffer
        override fun slice(): WasmBuffer {
            val slicedBuffer = buffer.copyOfRange(currentPosition, limit)
            return WasmBuffer(slicedBuffer)
        }

        // Read a byte array of a given size
        override fun readByteArray(size: Int): ByteArray {
            if (currentPosition + size > limit) throw IndexOutOfBoundsException("Not enough bytes to read")
            val byteArray = buffer.copyOfRange(currentPosition, currentPosition + size)
            currentPosition += size
            return byteArray
        }

        // Read a string with a given charset and length
        override fun readString(length: Int, charset: Charset): String {
            val byteArray = readByteArray(length)

            return byteArray.toString()
        }

        // Get the current limit of the buffer
        override fun limit(): Int {
            return limit
        }

        // Get the current position of the buffer
        override fun position(): Int {
            return currentPosition
        }

        // Set the position of the buffer
        override fun position(newPosition: Int) {
            if (newPosition > limit) throw IndexOutOfBoundsException("New position exceeds limit")
            currentPosition = newPosition
        }

        // Reset for writing
        override fun resetForWrite() {
            currentPosition = 0
            limit = buffer.size
        }

        // Write a byte to the buffer
        override fun writeByte(byte: Byte): WasmBuffer {
            if (currentPosition >= buffer.size) throw IndexOutOfBoundsException("Buffer overflow")
            buffer[currentPosition++] = byte
            return this
        }

        // Set a byte at a specific index
        override fun set(index: Int, byte: Byte): WasmBuffer {
            if (index >= buffer.size) throw IndexOutOfBoundsException("Index exceeds buffer size")
            buffer[index] = byte
            return this
        }


        // Write another buffer to this buffer
//        override fun write(buffer: ReadBuffer) {
//            val remaining = buffer.limit() - buffer.position()
//            if (currentPosition + remaining > this.buffer.size) throw IndexOutOfBoundsException("Buffer overflow")
//            for (i in currentPosition..min(buffer.limit(), limit)) {
//                this.buffer[i] = buffer.get(i)
//            }
//            currentPosition += remaining
//        }
        override fun write(buffer: ReadBuffer) {
            val remaining = buffer.limit() - buffer.position()
            if (currentPosition + remaining > this.buffer.size) throw IndexOutOfBoundsException("Buffer overflow")

            // Copy bytes from the source buffer
            for (i in 0 until remaining) {
                this.buffer[currentPosition + i] = buffer.get(buffer.position() + i)
            }

            // Update currentPosition after writing
            currentPosition += remaining
        }

        // Write a byte array to the buffer
        override fun writeBytes(bytes: ByteArray, offset: Int, length: Int): WasmBuffer {
            if (currentPosition + length > buffer.size) throw IndexOutOfBoundsException("Buffer overflow")
            bytes.copyInto(buffer, currentPosition, offset, offset + length)
            currentPosition += length
            return this
        }

        // Write a string to the buffer with a given charset
        override fun writeString(text: CharSequence, charset: Charset): WasmBuffer {
            val bytes = text.toString().encodeToByteArray()
            return writeBytes(bytes, 0, bytes.size)
        }

        // Close the buffer
        override suspend fun close() {
            // Clean up resources if needed
        }
    }
