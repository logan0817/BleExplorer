package com.logan.presentation.bluetooth

import java.util.*

class UUIDUtils {
	companion object {
		fun characteristicUuid(characteristicUuidPart: String) : UUID {
			val characteristicString = "0000$characteristicUuidPart-0000-1000-8000-00805f9b34fb"
			return UUID.fromString(characteristicString)
		}

		fun descriptorUuid(descriptorUUidPart: String) : UUID {
			val characteristicString = "0000$descriptorUUidPart-0000-1000-8000-00805f9b34fb"
			return UUID.fromString(characteristicString)
		}
	}
}