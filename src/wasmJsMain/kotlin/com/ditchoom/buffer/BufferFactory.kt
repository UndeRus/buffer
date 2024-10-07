package com.ditchoom.buffer


actual fun PlatformBuffer.Companion.allocate(
    size: Int,
    zone: AllocationZone,
    byteOrder: ByteOrder,
): PlatformBuffer {
    // byteorder only LE
    return when (zone) {
        AllocationZone.Heap,
        AllocationZone.SharedMemory,
        AllocationZone.Direct,
            -> WasmBuffer(ByteArray(size))

        is AllocationZone.Custom -> zone.allocator(size)
    }
}

actual fun PlatformBuffer.Companion.wrap(
    array: ByteArray,
    byteOrder: ByteOrder,
): PlatformBuffer {
    // byteorder only LE
    return WasmBuffer(array)
}